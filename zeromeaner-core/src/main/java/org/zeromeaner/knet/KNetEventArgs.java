package org.zeromeaner.knet;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;

import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.subsystem.mode.NetVSBattleMode;
import org.zeromeaner.game.subsystem.mode.TGMNetVSBattleMode;
import org.zeromeaner.knet.obj.KNStartInfo;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.obj.KNetPlayerInfo;
import org.zeromeaner.knet.obj.PieceHold;
import org.zeromeaner.knet.obj.PieceMovement;
import org.zeromeaner.knet.obj.Replay;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public enum KNetEventArgs {
	
	/**
	 * Issued by a server when a server assigns a {@link KNetEventSource} to a client.
	 * Argument: {@link KNetEventSource}.
	 */
	@Global
	ASSIGN_SOURCE(KNetEventSource.class),
	
	/**
	 * Issued by a client to update fields on the servers' record for that client.
	 * Argument: {@link KNetEventSource} to get the new data from.
	 */
	@Global
	UPDATE_SOURCE(KNetEventSource.class),
	/**
	 * Issued when a client connects to a server, after receiving a {@link KNetEventSource}.
	 */
	@Global
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
	PAYLOAD(Object.class, true),
	
	/**
	 * A specific {@link KNetEventSource} that should receive this message
	 */
	ADDRESS(KNetEventSource.class),
	
	/**
	 * A {@link String} describing the error
	 */
	ERROR(String.class),
	
	/**
	 * A specific {@link KNetEvent} that this event is in reply to
	 */
	IN_REPLY_TO(KNetEvent.class),
	
	USER_AUTHENTICATE(String.class, true),
	USER_AUTHENTICATED(Boolean.class),
	USER_CREATE(String.class, true),
	USER_CREATED(Boolean.class),
	USER_UPDATE_PASSWORD(String[].class),
	USER_UPDATED_PASSWORD(Boolean.class),
	
	/**
	 * The username of the event sender
	 * Argument: {@link String}
	 */
	USERNAME(String.class),
	
	/**
	 * The timstamp (millis UTC) of this message.
	 * Argument: {@link Long}
	 */
	TIMESTAMP(Long.class),
	
	/**
	 * The room ID for this message.
	 * Argument: {@link Integer}
	 */
	CHANNEL_ID(Integer.class), 
	/**
	 * Issued by a client to request a list of the current rooms.
	 * Issued by a server to respond with the list of rooms.  Server responses place
	 * an array of {@link ChannelInfo} objects in {@link #PAYLOAD}.
	 */
	@Global
	CHANNEL_LIST,
	
	/**
	 * Issued for chats in a room.
	 */
	@Global
	CHANNEL_CHAT(String.class),
	
	@Global
	CHANNEL_INFO(KNetChannelInfo[].class),
	
	/** Issued when joining a room */
	@Global
	CHANNEL_JOIN,
	
	@Global
	CHANNEL_SPECTATE,
	
	/** Issued when leaving a room */
	@Global
	CHANNEL_LEAVE,
	
	@Global
	CHANNEL_CREATE(KNetChannelInfo.class),
	
	@Global
	CHANNEL_DELETE(Integer.class),
	
	@Global
	CHANNEL_UPDATE(KNetChannelInfo.class),
	
	/** Issued for in-game events */
	GAME,
	
	/**
	 * Signal cursor movement?
	 * Argument: {@link Integer}
	 */
	GAME_CURSOR, 
	
	/** Issued when an in-game piece is locked */
	GAME_PIECE_LOCKED,
	
	/** Issued when the field is sent. */ 
	GAME_FIELD(Field.class),
	
	GAME_OPTIONS(Object.class, true),
	
	GAME_STATS(Object.class, true),
	
	GAME_PIECE_MOVEMENT(PieceMovement.class),
	
	/** Issued when the hold piece is sent. */
	GAME_HOLD_PIECE(PieceHold.class),
	
	/** Issued when the next piece list is sent. */
	GAME_NEXT_PIECE(Piece[].class),
	
	/** Issued when the game is ending */
	GAME_ENDING,
	
	/** Issued when the game says excellent? */
	GAME_EXCELLENT,
	
	GAME_RETRY, 
	
	/** Issued when we show the results screen? */
	GAME_RESULTS_SCREEN, 
	
	GAME_SYNCHRONOUS,
	
	GAME_SYNCHRONOUS_LOCKED,
	
	GAME_END_STATS(Object.class, true),
	
	GAME_BONUS_LEVEL_ENTER,
	GAME_BONUS_LEVEL_START,
	
	/** Issued when the game is starting? */
	START(KNStartInfo.class),
	
	START_1P,
	
	/** Issued when we die? */
	DEAD(Integer.class),
	
	DEAD_PLACE(Integer.class),
	
	DEAD_KO(Integer.class),
	
	RESET_1P,
	
	PLAYER_UPDATE(KNetPlayerInfo.class),
	
	/**
	 * Issued when a player logs out.
	 */
	PLAYER_LOGOUT(KNetPlayerInfo.class),
	
	REPLAY_DATA(Replay.class),
	REPLAY_NOT_RECEIVED,
	REPLAY_RECEIVED,
	
	MAPS(Field[].class),
	
	READY(Boolean.class),
	
	AUTOSTART,
	/**
	 * argument: Integer: number of seconds
	 */
	AUTOSTART_BEGIN(Integer.class),
	AUTOSTART_STOP,
	
	CHANGE_STATUS(KNetPlayerInfo.class),
	
	PLAYER_ENTER(KNetPlayerInfo.class),
	PLAYER_LEAVE(KNetPlayerInfo.class),
	
	/**
	 * argument: {@link Boolean}: team win
	 */
	FINISH(Boolean.class),
	
	FINISH_WINNER(KNetEventSource.class),
	
	HURRY_UP,

	RACE_WIN,
	
	NETVSBATTLE_GAME_ATTACK(NetVSBattleMode.AttackInfo.class),
	NETVSBATTLE_GAME_STATS(NetVSBattleMode.StatsInfo.class),
	
	TGMNETVSBATTLE_GAME_ATTACK(TGMNetVSBattleMode.TGMAttackInfo.class),
	
	;
	
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Global {
		
	}
	
	private Class<?> type;
	private boolean nullable;
	private boolean global;
	
	private KNetEventArgs() {
		this(null, false);
	}
	
	private KNetEventArgs(Class<?> type) {
		this(type, false);
	}
	
	private KNetEventArgs(Class<?> type, boolean nullable) {
		this.type = type;
		this.nullable = nullable;
	}
	
	static {
		for(KNetEventArgs arg : values()) {
			try {
				if(arg.getDeclaringClass().getField(arg.name()).isAnnotationPresent(Global.class))
					arg.global = true;
			} catch(Exception ex) {
			}
		}
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public boolean isGlobal() {
		return global;
	}
	
	public void write(Kryo kryo, Output output, Object argValue) {
		if(type == null)
			return;
		if(!type.isInstance(argValue) && argValue != null) {
			new Throwable("Invalid arg for " + this + ":" + argValue).printStackTrace();
			throw new ClassCastException();
		}
		if(nullable) {
			System.err.println(this + " writing " + argValue);
			kryo.writeClassAndObject(output, argValue);
		} else
			kryo.writeObject(output, argValue);
		
	}
	
	public Object read(Kryo kryo, Input input) {
		try {
			if(type == null)
				return true;
			if(nullable) {
				return type.cast(kryo.readClassAndObject(input));
			} else
				return kryo.readObject(input, type);
		} catch(RuntimeException er) {
			throw new KryoException("reading " + this, er);
		}
	}
}