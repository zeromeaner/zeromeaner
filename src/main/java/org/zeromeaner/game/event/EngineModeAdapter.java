package org.zeromeaner.game.event;

import org.zeromeaner.game.subsystem.mode.GameMode;

public class EngineModeAdapter implements EngineListener {

	private GameMode mode;
	
	public EngineModeAdapter(GameMode mode) {
		this.mode = mode;
	}
	
	public GameMode getMode() {
		return mode;
	}
	
	public void setMode(GameMode mode) {
		this.mode = mode;
	}
	
	@Override
	public void enginePlayerInit(EngineEvent e) {
		mode.playerInit(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineGameStarted(EngineEvent e) {
		mode.startGame(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineFrameFirst(EngineEvent e) {
		mode.onFirst(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineFrameLast(EngineEvent e) {
		mode.onLast(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineSettings(EngineEvent e) {
		return mode.onSetting(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineReady(EngineEvent e) {
		return mode.onReady(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineMove(EngineEvent e) {
		return mode.onMove(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineLockFlash(EngineEvent e) {
		return mode.onLockFlash(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineLineClear(EngineEvent e) {
		return mode.onLineClear(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineARE(EngineEvent e) {
		return mode.onARE(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineEndingStart(EngineEvent e) {
		return mode.onEndingStart(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineCustom(EngineEvent e) {
		return mode.onCustom(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineExcellent(EngineEvent e) {
		return mode.onExcellent(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineGameOver(EngineEvent e) {
		return mode.onGameOver(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineResults(EngineEvent e) {
		return mode.onResult(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineFieldEditor(EngineEvent e) {
		return mode.onFieldEdit(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderFirst(EngineEvent e) {
		mode.renderFirst(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderLast(EngineEvent e) {
		mode.renderLast(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderSettings(EngineEvent e) {
		mode.renderSetting(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderReady(EngineEvent e) {
		mode.renderReady(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderMove(EngineEvent e) {
		mode.renderMove(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderLockFlash(EngineEvent e) {
		mode.renderLockFlash(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderLineClear(EngineEvent e) {
		mode.renderLineClear(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderARE(EngineEvent e) {
		mode.renderARE(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderEndingStart(EngineEvent e) {
		mode.renderEndingStart(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderCustom(EngineEvent e) {
		mode.renderCustom(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderExcellent(EngineEvent e) {
		mode.renderExcellent(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderGameOver(EngineEvent e) {
		mode.renderGameOver(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderResults(EngineEvent e) {
		mode.renderResult(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderFieldEditor(EngineEvent e) {
		mode.renderFieldEdit(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderInput(EngineEvent e) {
		mode.renderInput(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineBlockBreak(EngineEvent e) {
		mode.blockBreak(e.getSource(), e.getPlayerId(), e.getBlockX(), e.getBlockY(), e.getBlock());
	}

	@Override
	public void engineCalcScore(EngineEvent e) {
		mode.calcScore(e.getSource(), e.getPlayerId(), e.getLines());
	}

	@Override
	public void engineAfterSoftDropFall(EngineEvent e) {
		mode.afterSoftDropFall(e.getSource(), e.getPlayerId(), e.getFall());
	}

	@Override
	public void engineAfterHardDropFall(EngineEvent e) {
		mode.afterHardDropFall(e.getSource(), e.getPlayerId(), e.getFall());
	}

	@Override
	public void engineFieldEditorExit(EngineEvent e) {
		mode.fieldEditExit(e.getSource(), e.getPlayerId());
	}

	@Override
	public void enginePieceLocked(EngineEvent e) {
		mode.pieceLocked(e.getSource(), e.getPlayerId(), e.getLines());
	}

	@Override
	public boolean engineLineClearEnd(EngineEvent e) {
		return mode.lineClearEnd(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineSaveReplay(EngineEvent e) {
		mode.saveReplay(e.getSource(), e.getPlayerId(), e.getReplayProps());
	}

	@Override
	public void engineLoadReplay(EngineEvent e) {
		mode.loadReplay(e.getSource(), e.getPlayerId(), e.getReplayProps());
	}

}
