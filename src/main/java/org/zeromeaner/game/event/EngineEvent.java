package org.zeromeaner.game.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventObject;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.util.CustomProperties;

public class EngineEvent extends EventObject {
	
	public static enum Type {
		PLAYER_INIT("enginePlayerInit"),
		START_GAME("engineGameStarted"),
		FRAME_FIRST("engineFrameFirst"),
		FRAME_LAST("engineFrameLast"),
		SETTINGS("engineSettings"),
		READY("engineReady"),
		MOVE("engineMove"),
		LOCK_FLASH("engineLockFlash"),
		LINE_CLEAR("engineLineClear"),
		ARE("engineARE"),
		ENDING_START("engineEndingStart"),
		CUSTOM("engineCustom"),
		EXCELLENT("engineExcellent"),
		GAME_OVER("engineGameOver"),
		RESULTS("engineResults"),
		FIELD_EDITOR("engineFieldEditor"),
		RENDER_FIRST("engineRenderFirst"),
		RENDER_LAST("engineRenderLast"),
		RENDER_SETTINGS("engineRenderSettings"),
		RENDER_READY("engineRenderReady"),
		RENDER_MOVE("engineRenderMove"),
		RENDER_LOCK_FLASH("engineRenderLockFlash"),
		RENDER_LINE_CLEAR("engineRenderLineClear"),
		RENDER_ARE("engineRenderARE"),
		RENDER_ENDING_START("engineRenderEndingStart"),
		RENDER_CUSTOM("engineRenderCustom"),
		RENDER_EXCELLENT("engineRenderExcellent"),
		RENDER_GAME_OVER("engineRenderGameOver"),
		RENDER_RESULTS("engineRenderResults"),
		RENDER_FIELD_EDITOR("engineRenderFieldEditor"),
		RENDER_INPUT("engineRenderInput"),
		BLOCK_BREAK("engineBlockBreak"),
		CALC_SCORE("engineCalcScore"),
		AFTER_SOFT_DROP_FALL("engineAfterSoftDropFall"),
		AFTER_HARD_DROP_FALL("engineAfterHardDropFall"),
		FIELD_EDITOR_EXIT("engineFieldEditorExit"),
		PIECE_LOCKED("enginePieceLocked"),
		LINE_CLEAR_END("engineLineClearEnd"),
		SAVE_REPLAY("engineSaveReplay"),
		LOAD_REPLAY("engineLoadReplay")
		;
		
		private Method method;
		
		private Type(String methodName) {
			try {
				this.method = EngineListener.class.getMethod(methodName, EngineEvent.class);
			} catch(NoSuchMethodException nsme) {
				throw new RuntimeException(nsme);
			}
		}

		public Method getMethod() {
			return method;
		}
	}
	
	public static enum Args {
		BLOCK_X,
		BLOCK_Y,
		BLOCK,
		LINES,
		FALL,
		PROPERTIES,
	}
	
	private Type type;
	private int playerId;
	
	private Integer blockX;
	private Integer blockY;
	private Block block;
	
	private Integer lines;
	
	private Integer fall;
	
	private CustomProperties replayProps;
	
	public EngineEvent(GameEngine source, Type type, int playerId, Object... args) {
		super(source);
		this.type = type;
		this.playerId = playerId;
		for(int i = 0; i < args.length; i += 2) {
			Object val = args[i+1];
			switch((Args) args[i]) {
			case BLOCK:
				block = (Block) val;
				break;
			case BLOCK_X:
				blockX = (Integer) val;
				break;
			case BLOCK_Y:
				blockY = (Integer) val;
				break;
			case FALL:
				fall = (Integer) val;
				break;
			case LINES:
				lines = (Integer) val;
				break;
			case PROPERTIES:
				replayProps = (CustomProperties) val;
				break;
			}
		}
	}
	
	public Object invoke(EngineListener l) {
		try {
			return type.getMethod().invoke(l, this);
		} catch(IllegalAccessException iae) {
			throw new InternalError("Unable to access public interface method");
		} catch (InvocationTargetException ite) {
			throw new RuntimeException(ite);
		}
	}
	
	@Override
	public GameEngine getSource() {
		return (GameEngine) super.getSource();
	}
	
	public Type getType() {
		return type;
	}
	
	public int getPlayerId() {
		return playerId;
	}


	public Integer getBlockX() {
		return blockX;
	}


	public Integer getBlockY() {
		return blockY;
	}


	public Block getBlock() {
		return block;
	}


	public Integer getLines() {
		return lines;
	}


	public Integer getFall() {
		return fall;
	}


	public CustomProperties getReplayProps() {
		return replayProps;
	}
}
