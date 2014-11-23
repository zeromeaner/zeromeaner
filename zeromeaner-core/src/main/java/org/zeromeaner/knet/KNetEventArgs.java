package org.zeromeaner.knet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
	
	/**
	 * The username of the event sender
	 * Argument: {@link String}
	 */
	USER(KNetEventSource.class),
	
	PASSWORD(String.class),
	
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

	CHANNEL_LISTING(KNetChannelInfo[].class),
	
	CHANNEL_INFO, /**
	 * Issued for chats in a room.
	 */
	CHANNEL_CHAT_MESSAGE(String.class),
	
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
	@Global
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