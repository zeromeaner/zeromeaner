package org.zeromeaner.game.knet;

import java.util.EnumMap;
import java.util.EventObject;
import java.util.Map;

import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.knet.srv.KSChannelManager.ChannelInfo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNetEvent extends EventObject implements KryoSerializable {
	public static enum NetEventArgs {
		/**
		 * Issued when a server assigns a {@link KNetEventSource} to a client.
		 * Argument: {@link KNetEventSource}.
		 */
		ASSIGN_SOURCE,
		/**
		 * Issued when a client connects to a server, after receiving a {@link KNetEventSource}.
		 */
		CONNECTED,
		/**
		 * Issued when a client disconnects from a server.
		 */
		DISCONNECTED,
		
		/** Issued when the packet should be sent via UDP instead of TCP */
		UDP,
		
		/**
		 * Any object payload.
		 * Argument: {@link Object}
		 */
		PAYLOAD,
		
		/**
		 * A specific {@link KNetEventSource} that should receive this message
		 */
		ADDRESS,
		
		/**
		 * A {@link String} describing the error
		 */
		ERROR,
		
		/**
		 * A specific {@link KNetEvent} that this event is in reply to
		 */
		IN_REPLY_TO,
		
		/**
		 * The username of the event sender
		 * Argument: {@link String}
		 */
		USERNAME,
		
		/**
		 * The room ID for this message.  -1 is the lobby.
		 * Argument: {@link Integer}
		 */
		CHANNEL_ID,
		
		/**
		 * The timstamp (millis UTC) of this message.  Optional.
		 * Argument: {@link Long}
		 */
		TIMESTAMP,
		
		/**
		 * Issued by a client to request a list of the current rooms.
		 * Issued by a server to respond with the list of rooms.  Server responses place
		 * an array of {@link ChannelInfo} objects in {@link #PAYLOAD}.
		 */
		CHANNEL_LIST,
		
		/**
		 * Issued for chats in a room.  {@link #PAYLOAD} will be a string.
		 */
		CHANNEL_CHAT,
		
		/** Issued when joining a room */
		CHANNEL_JOIN,
		
		/** Issued when leaving a room */
		CHANNEL_LEAVE,
		
		/** Issued for in-game events */
		GAME,
		
		/** Issued when an in-game piece is locked */
		GAME_PIECE_LOCKED,
		
		/** Issued when the field is sent.  {@link Field} is sent in {@link #PAYLOAD} */ 
		GAME_FIELD,
		
		/** Issued when the hold piece is sent. The {@link Piece} is sent in {@link #PAYLOAD}  */
		GAME_HOLD_PIECE,
		
		/** Issued when the next piece list is sent. The {@link Piece}[] is sent in {@link #PAYLOAD}  */
		GAME_NEXT_PIECE,
		;
		
		public void write(Kryo kryo, Output output, Object argValue) {
			switch(this) {
			case ASSIGN_SOURCE:
				kryo.writeObject(output, (KNetEventSource) argValue);
				break;
			case PAYLOAD:
				kryo.writeClassAndObject(output, argValue);
				break;
			case USERNAME:
				output.writeString((String) argValue);
				break;
			case CHANNEL_ID:
				output.writeInt((Integer) argValue, true);
				break;
			case TIMESTAMP:
				output.writeLong((Long) argValue, true);
				break;
			case ADDRESS:
				kryo.writeObject(output, argValue);
				break;
			case ERROR:
				output.writeString((String) argValue);
				break;
			case IN_REPLY_TO:
				kryo.writeObject(output, argValue);
				break;
			}
		}
		
		public Object read(Kryo kryo, Input input) {
			switch(this) {
			case IN_REPLY_TO:
				return kryo.readObject(input, KNetEvent.class);
			case ERROR:
				return input.readString();
			case ADDRESS:
				return kryo.readObject(input, KNetEventSource.class);
			case ASSIGN_SOURCE:
				return kryo.readObject(input, KNetEventSource.class);
			case PAYLOAD:
				return kryo.readClassAndObject(input);
			case USERNAME:
				return input.readString();
			case CHANNEL_ID:
				return input.readInt(true);
			case TIMESTAMP:
				return input.readLong(true);
			default:
				return true;
			}
		}
	}
	
	private Map<NetEventArgs, Object> args = new EnumMap<NetEventArgs, Object>(NetEventArgs.class);
	
	@Deprecated
	public KNetEvent() {
		super(new Object());
	}
	
	public KNetEvent(KNetEventSource source, Object... args) {
		super(source);
		for(int i = 0; i < args.length; i += 2) {
			this.args.put((NetEventArgs) args[i], args[i+1]);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[source=" + getSource() + ", args=" + args + "]";
	}
	
	@Override
	public KNetEventSource getSource() {
		return (KNetEventSource) super.getSource();
	}
	
	public Object get(NetEventArgs arg) {
		return args.get(arg);
	}
	
	public boolean is(NetEventArgs arg) {
		return args.containsKey(arg);
	}
	
	public void set(NetEventArgs arg, Object value) {
		args.put(arg, value);
	}
	
	public Map<NetEventArgs, Object> getArgs() {
		return args;
	}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, getSource());
		output.writeInt(args.size(), true);
		for(Map.Entry<NetEventArgs, Object> e : args.entrySet()) {
			output.writeInt(e.getKey().ordinal(), true);
			e.getKey().write(kryo, output, e.getValue());
		}
	}

	@Override
	public void read(Kryo kryo, Input input) {
		source = kryo.readObject(input, KNetEventSource.class);
		int size = input.readInt(true);
		for(int i = 0; i < size; i++) {
			int ordinal = input.readInt(true);
			NetEventArgs arg = NetEventArgs.values()[ordinal];
			Object val = arg.read(kryo, input);
			args.put(arg, val);
		}
	}
}
