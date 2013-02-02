package org.zeromeaner.game.knet;

import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public enum KNetEventArgs {
	/**
	 * Issued by a server when a server assigns a {@link KNetEventSource} to a client.
	 * Argument: {@link KNetEventSource}.
	 */
	ASSIGN_SOURCE,
	
	/**
	 * Issued by a client to update fields on the servers' record for that client.
	 * Argument: {@link KNetEventSource} to get the new data from.
	 */
	UPDATE_SOURCE,
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
	
	/** Issued when the game is ending */
	GAME_ENDING,
	
	/** Issued when the game says excellent? */
	GAME_EXCELLENT,
	
	/** Issued when the game is starting? */
	START,
	
	/** Issued when we die? */
	DEAD,
	
	/** Issued when we show the results screen? */
	GAME_RESULTS_SCREEN,
	
	GAME_RETRY,
	
	RESET_1P,
	
	/**
	 * Signal cursor movement?
	 * Argument: {@link Integer}
	 */
	GAME_CURSOR,
	
	PLAYER_UPDATE,
	
	/**
	 * Issued when a player logs out.
	 * Argument: {@link KNetEventSource} the player that logged out
	 */
	PLAYER_LOGOUT,
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