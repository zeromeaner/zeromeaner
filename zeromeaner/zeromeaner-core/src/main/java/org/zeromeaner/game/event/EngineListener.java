package org.zeromeaner.game.event;

import java.util.EventListener;

import org.zeromeaner.game.subsystem.mode.GameMode;

public interface EngineListener extends EventListener {
	/**
	 * @see GameMode#playerInit(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void enginePlayerInit(EngineEvent e);
	
	/**
	 * @see GameMode#startGame(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineGameStarted(EngineEvent e);
	
	/**
	 * @see GameMode#onFirst(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineFrameFirst(EngineEvent e);
	
	/**
	 * @see GameMode#onLast(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineFrameLast(EngineEvent e);
	
	/**
	 * @see GameMode#onSetting(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineSettings(EngineEvent e);
	
	/**
	 * @see GameMode#onReady(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineReady(EngineEvent e);
	
	/**
	 * @see GameMode#onMove(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineMove(EngineEvent e);
	
	/**
	 * @see GameMode#onLockFlash(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineLockFlash(EngineEvent e);
	
	/**
	 * @see GameMode#onLineClear(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineLineClear(EngineEvent e);
	
	/**
	 * @see GameMode#onARE(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineARE(EngineEvent e);
	
	/**
	 * @see GameMode#onEndingStart(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineEndingStart(EngineEvent e);
	
	/**
	 * @see GameMode#onCustom(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineCustom(EngineEvent e);
	
	/**
	 * @see GameMode#onExcellent(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineExcellent(EngineEvent e);
	
	/**
	 * @see GameMode#onGameOver(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineGameOver(EngineEvent e);
	
	/**
	 * @see GameMode#onResult(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineResults(EngineEvent e);
	
	/**
	 * @see GameMode#onFieldEdit(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineFieldEditor(EngineEvent e);
	
	/**
	 * @see GameMode#renderFirst(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderFirst(EngineEvent e);
	
	/**
	 * @see GameMode#renderLast(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderLast(EngineEvent e);
	
	/**
	 * @see GameMode#renderSetting(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderSettings(EngineEvent e);
	
	/**
	 * @see GameMode#renderReady(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderReady(EngineEvent e);
	
	/**
	 * @see GameMode#renderMove(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderMove(EngineEvent e);
	
	/**
	 * @see GameMode#renderLockFlash(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderLockFlash(EngineEvent e);
	
	/**
	 * @see GameMode#renderLineClear(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderLineClear(EngineEvent e);
	
	/**
	 * @see GameMode#renderARE(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderARE(EngineEvent e);
	
	/**
	 * @see GameMode#renderEndingStart(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderEndingStart(EngineEvent e);
	
	/**
	 * @see GameMode#renderCustom(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderCustom(EngineEvent e);
	
	/**
	 * @see GameMode#renderExcellent(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderExcellent(EngineEvent e);
	
	/**
	 * @see GameMode#renderGameOver(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderGameOver(EngineEvent e);
	
	/**
	 * @see GameMode#renderResult(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderResults(EngineEvent e);
	
	/**
	 * @see GameMode#renderFieldEdit(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderFieldEditor(EngineEvent e);
	
	/**
	 * @see GameMode#renderInput(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineRenderInput(EngineEvent e);
	
	/**
	 * @see GameMode#blockBreak(org.zeromeaner.game.play.GameEngine, int, int, int, org.zeromeaner.game.component.Block)
	 * @param e
	 */
	public void engineBlockBreak(EngineEvent e);
	
	/**
	 * @see GameMode#calcScore(org.zeromeaner.game.play.GameEngine, int, int)
	 * @param e
	 */
	public void engineCalcScore(EngineEvent e);
	
	/**
	 * @see GameMode#afterSoftDropFall(org.zeromeaner.game.play.GameEngine, int, int)
	 * @param e
	 */
	public void engineAfterSoftDropFall(EngineEvent e);
	
	/**
	 * @see GameMode#afterHardDropFall(org.zeromeaner.game.play.GameEngine, int, int)
	 * @param e
	 */
	public void engineAfterHardDropFall(EngineEvent e);
	
	/**
	 * @see GameMode#fieldEditExit(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 */
	public void engineFieldEditorExit(EngineEvent e);
	
	/**
	 * @see GameMode#pieceLocked(org.zeromeaner.game.play.GameEngine, int, int)
	 * @param e
	 */
	public void enginePieceLocked(EngineEvent e);
	
	/**
	 * @see GameMode#lineClearEnd(org.zeromeaner.game.play.GameEngine, int)
	 * @param e
	 * @return
	 */
	public boolean engineLineClearEnd(EngineEvent e);
	
	/**
	 * @see GameMode#saveReplay(org.zeromeaner.game.play.GameEngine, int, org.zeromeaner.util.CustomProperties)
	 * @param e
	 */
	public void engineSaveReplay(EngineEvent e);
	
	/**
	 * @see GameMode#loadReplay(org.zeromeaner.game.play.GameEngine, int, org.zeromeaner.util.CustomProperties)
	 * @param e
	 */
	public void engineLoadReplay(EngineEvent e);
}
