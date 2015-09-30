package org.zeromeaner.game.event;

import javax.swing.event.EventListenerList;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.event.EngineEvent.Args;
import org.zeromeaner.game.event.EngineEvent.Type;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.util.CustomProperties;

public class EngineEventManager implements EngineEventGenerator {
	private GameEngine source;

	private EventListenerList listeners = new EventListenerList();

	public EngineEventManager(GameEngine source) {
		this.source = source;
	}

	@Override
	public void addEngineListener(EngineListener l) {
		listeners.add(EngineListener.class, l);
	}

	@Override
	public void removeEngineListener(EngineListener l) {
		listeners.remove(EngineListener.class, l);
	}

	private EngineEvent newEvent(Type type, Object... args) {
		return new EngineEvent(source, type, source.getPlayerID(), args);
	}

	public void enginePlayerInit() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.PLAYER_INIT);
				EngineListener el = (EngineListener) ll[i+1];
				el.enginePlayerInit(e);
			}
		}
	}

	public void engineGameStarted() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.PLAYER_INIT);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineGameStarted(e);
			}
		}
	}

	public void engineFrameFirst() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.PLAYER_INIT);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineFrameFirst(e);
			}
		}
	}

	public void engineFrameLast() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.PLAYER_INIT);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineFrameLast(e);
			}
		}
	}

	public boolean engineSettings() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.SETTINGS);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineSettings(e);
			}
		}
		return ret;
	}

	public boolean engineReady() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.READY);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineReady(e);
			}
		}
		return ret;
	}

	public boolean engineMove() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.MOVE);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineMove(e);
			}
		}
		return ret;
	}

	public boolean engineLockFlash() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.LOCK_FLASH);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineLockFlash(e);
			}
		}
		return ret;
	}

	public boolean engineLineClear() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.LINE_CLEAR);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineLineClear(e);
			}
		}
		return ret;
	}

	public boolean engineARE() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.ARE);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineARE(e);
			}
		}
		return ret;
	}

	public boolean engineEndingStart() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.ENDING_START);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineEndingStart(e);
			}
		}
		return ret;
	}

	public boolean engineCustom() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.CUSTOM);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineCustom(e);
			}
		}
		return ret;
	}

	public boolean engineExcellent() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.EXCELLENT);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineExcellent(e);
			}
		}
		return ret;
	}

	public boolean engineGameOver() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.GAME_OVER);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineGameOver(e);
			}
		}
		return ret;
	}

	public boolean engineResults() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RESULTS);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineResults(e);
			}
		}
		return ret;
	}

	public boolean engineFieldEditor() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.FIELD_EDITOR);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineFieldEditor(e);
			}
		}
		return ret;
	}

	public void engineRenderFirst() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_FIRST);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderFirst(e);
			}
		}
	}

	public void engineRenderLast() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_LAST);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderLast(e);
			}
		}
	}

	public void engineRenderSettings() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_SETTINGS);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderSettings(e);
			}
		}
	}

	public void engineRenderReady() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_READY);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderReady(e);
			}
		}
	}

	public void engineRenderMove() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_MOVE);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderMove(e);
			}
		}
	}

	public void engineRenderLockFlash() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_LOCK_FLASH);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderLockFlash(e);
			}
		}
	}

	public void engineRenderLineClear() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_LINE_CLEAR);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderLineClear(e);
			}
		}
	}

	public void engineRenderARE() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_ARE);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderARE(e);
			}
		}
	}

	public void engineRenderEndingStart() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_ENDING_START);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderEndingStart(e);
			}
		}
	}

	public void engineRenderCustom() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_CUSTOM);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderCustom(e);
			}
		}
	}

	public void engineRenderExcellent() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_EXCELLENT);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderExcellent(e);
			}
		}
	}

	public void engineRenderGameOver() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_GAME_OVER);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderGameOver(e);
			}
		}
	}

	public void engineRenderResults() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_RESULTS);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderResults(e);
			}
		}
	}

	public void engineRenderFieldEditor() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_FIELD_EDITOR);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderFieldEditor(e);
			}
		}
	}

	public void engineRenderInput() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.RENDER_INPUT);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineRenderInput(e);
			}
		}
	}

	public void engineBlockBreak(int x, int y, Block block) {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.BLOCK_BREAK, Args.BLOCK_X, x, Args.BLOCK_Y, y, Args.BLOCK, block);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineBlockBreak(e);
			}
		}
	}

	public void engineCalcScore(int lines) {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.CALC_SCORE, Args.LINES, lines);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineCalcScore(e);
			}
		}
	}

	public void engineAfterSoftDropFall(int fall) {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.AFTER_SOFT_DROP_FALL, Args.FALL, fall);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineAfterSoftDropFall(e);
			}
		}
	}

	public void engineAfterHardDropFall(int fall) {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.AFTER_HARD_DROP_FALL, Args.FALL, fall);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineAfterHardDropFall(e);
			}
		}
	}

	public void engineFieldEditorExit() {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.FIELD_EDITOR_EXIT);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineFieldEditorExit(e);
			}
		}
	}

	public void enginePieceLocked(int lines) {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.PIECE_LOCKED, Args.LINES, lines);
				EngineListener el = (EngineListener) ll[i+1];
				el.enginePieceLocked(e);
			}
		}
	}

	public boolean engineLineClearEnd() {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.LINE_CLEAR_END);
				EngineListener el = (EngineListener) ll[i+1];
				ret |= el.engineLineClearEnd(e);
			}
		}
		return ret;
	}

	public void engineSaveReplay(CustomProperties props) {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.SAVE_REPLAY, Args.PROPERTIES, props);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineSaveReplay(e);
			}
		}
	}

	public void engineLoadReplay(CustomProperties props) {
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(Type.LOAD_REPLAY, Args.PROPERTIES, props);
				EngineListener el = (EngineListener) ll[i+1];
				el.engineLoadReplay(e);
			}
		}
	}

}
