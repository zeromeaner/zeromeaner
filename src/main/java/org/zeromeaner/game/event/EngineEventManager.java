package org.zeromeaner.game.event;

import java.lang.reflect.InvocationTargetException;

import javax.swing.event.EventListenerList;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.event.EngineEvent.Args;
import org.zeromeaner.game.event.EngineEvent.Type;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.play.GameManager;
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
	public void addGameMode(GameMode mode) {
		addEngineListener(new EngineModeAdapter(mode));
	}
	
	@Override
	public void addReceiver(EventRenderer receiver) {
		addEngineListener(new EngineRendererAdapter(receiver));
	}
	
	@Override
	public void removeEngineListener(EngineListener l) {
		listeners.remove(EngineListener.class, l);
	}
	
	private EngineEvent newEvent(Type type, Object... args) {
		return new EngineEvent(source, type, source.playerID, args);
	}
	
	private boolean fire(Type type, Object... args) {
		boolean ret = false;
		Object[] ll = listeners.getListenerList();
		EngineEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == EngineListener.class) {
				if(e == null)
					e = newEvent(type, args);
				EngineListener el = (EngineListener) ll[i+1];
				Object rval = e.invoke(el);
				if(rval instanceof Boolean)
					ret = (ret || (Boolean) rval);
			}
		}
		return ret;
	}
	
	public void enginePlayerInit() {
		fire(Type.PLAYER_INIT);
	}
	public void engineGameStarted() {
		fire(Type.START_GAME);
	}
	public void engineFrameFirst() {
		fire(Type.FRAME_FIRST);
	}
	public void engineFrameLast() {
		fire(Type.FRAME_LAST);
	}
	public boolean engineSettings() {
		return fire(Type.SETTINGS);
	}
	public boolean engineReady() {
		return fire(Type.READY);
	}
	public boolean engineMove() {
		return fire(Type.MOVE);
	}
	public boolean engineLockFlash() {
		return fire(Type.LOCK_FLASH);
	}
	public boolean engineLineClear() {
		return fire(Type.LINE_CLEAR);
	}
	public boolean engineARE() {
		return fire(Type.ARE);
	}
	public boolean engineEndingStart() {
		return fire(Type.ENDING_START);
	}
	public boolean engineCustom() {
		return fire(Type.CUSTOM);
	}
	public boolean engineExcellent() {
		return fire(Type.EXCELLENT);
	}
	public boolean engineGameOver() {
		return fire(Type.GAME_OVER);
	}
	public boolean engineResults() {
		return fire(Type.RESULTS);
	}
	public boolean engineFieldEditor() {
		return fire(Type.FIELD_EDITOR);
	}
	public void engineRenderFirst() {
		fire(Type.RENDER_FIRST);
	}
	public void engineRenderLast() {
		fire(Type.RENDER_LAST);
	}
	public void engineRenderSettings() {
		fire(Type.RENDER_SETTINGS);
	}
	public void engineRenderReady() {
		fire(Type.RENDER_READY);
	}
	public void engineRenderMove() {
		fire(Type.RENDER_MOVE);
	}
	public void engineRenderLockFlash() {
		fire(Type.RENDER_LOCK_FLASH);
	}
	public void engineRenderLineClear() {
		fire(Type.RENDER_LINE_CLEAR);
	}
	public void engineRenderARE() {
		fire(Type.RENDER_ARE);
	}
	public void engineRenderEndingStart() {
		fire(Type.RENDER_ENDING_START);
	}
	public void engineRenderCustom() {
		fire(Type.RENDER_CUSTOM);
	}
	public void engineRenderExcellent() {
		fire(Type.RENDER_EXCELLENT);
	}
	public void engineRenderGameOver() {
		fire(Type.RENDER_GAME_OVER);
	}
	public void engineRenderResults() {
		fire(Type.RENDER_RESULTS);
	}
	public void engineRenderFieldEditor() {
		fire(Type.RENDER_FIELD_EDITOR);
	}
	public void engineRenderInput() {
		fire(Type.RENDER_INPUT);
	}
	public void engineBlockBreak(int x, int y, Block block) {
		fire(Type.BLOCK_BREAK,
				Args.BLOCK_X, x,
				Args.BLOCK_Y, y,
				Args.BLOCK, block
				);
	}
	public void engineCalcScore(int lines) {
		fire(Type.CALC_SCORE,
				Args.LINES, lines);
	}
	public void engineAfterSoftDropFall(int fall) {
		fire(Type.AFTER_SOFT_DROP_FALL,
				Args.FALL, fall);
	}
	public void engineAfterHardDropFall(int fall) {
		fire(Type.AFTER_HARD_DROP_FALL,
				Args.FALL, fall);
	}
	public void engineFieldEditorExit() {
		fire(Type.FIELD_EDITOR_EXIT);
	}
	public void enginePieceLocked(int lines) {
		fire(Type.PIECE_LOCKED,
				Args.LINES, lines);
	}
	public boolean engineLineClearEnd() {
		return fire(Type.LINE_CLEAR_END);
	}
	public void engineSaveReplay(CustomProperties props) {
		fire(Type.SAVE_REPLAY,
				Args.PROPERTIES, props);
	}
	public void engineLoadReplay(CustomProperties props) {
		fire(Type.LOAD_REPLAY,
				Args.PROPERTIES, props);
	}

}
