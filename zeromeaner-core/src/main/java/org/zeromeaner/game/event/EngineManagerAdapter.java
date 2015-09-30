package org.zeromeaner.game.event;

import org.zeromeaner.game.play.GameManager;

public class EngineManagerAdapter implements EngineListener {

	private GameManager manager;
	
	public EngineManagerAdapter(GameManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void enginePlayerInit(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.playerInit(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.playerInit(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineGameStarted(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.startGame(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.startGame(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineFrameFirst(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.onFirst(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onFirst(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineFrameLast(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.onLast(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onLast(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineSettings(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onSetting(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onSetting(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineReady(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onReady(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onReady(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineMove(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onMove(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onMove(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineLockFlash(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onLockFlash(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onLockFlash(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineLineClear(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onLineClear(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onLineClear(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineARE(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onARE(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onARE(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineEndingStart(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onEndingStart(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onEndingStart(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineCustom(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onCustom(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onCustom(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineExcellent(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onExcellent(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onExcellent(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineGameOver(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onGameOver(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onGameOver(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineResults(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onResult(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onResult(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public boolean engineFieldEditor(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.onFieldEdit(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.onFieldEdit(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public void engineRenderFirst(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderFirst(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderFirst(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderLast(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderLast(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderLast(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderSettings(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderSetting(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderSetting(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderReady(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderReady(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderReady(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderMove(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderMove(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderMove(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderLockFlash(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderLockFlash(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderLockFlash(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderLineClear(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderLineClear(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderLineClear(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderARE(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderARE(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderARE(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderEndingStart(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderEndingStart(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderEndingStart(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderCustom(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderCustom(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderCustom(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderExcellent(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderExcellent(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderExcellent(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderGameOver(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderGameOver(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderGameOver(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderResults(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderResult(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderResult(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderFieldEditor(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderFieldEdit(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderFieldEdit(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderInput(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.renderInput(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.renderInput(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineBlockBreak(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.blockBreak(e.getSource(), e.getPlayerId(), e.getBlockX(), e.getBlockY(), e.getBlock());
		if(manager.receiver != null)
			manager.receiver.blockBreak(e.getSource(), e.getPlayerId(), e.getBlockX(), e.getBlockY(), e.getBlock());
	}

	@Override
	public void engineCalcScore(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.calcScore(e.getSource(), e.getPlayerId(), e.getLines());
		if(manager.receiver != null)
			manager.receiver.calcScore(e.getSource(), e.getPlayerId(), e.getLines());
	}

	@Override
	public void engineAfterSoftDropFall(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.afterSoftDropFall(e.getSource(), e.getPlayerId(), e.getFall());
		if(manager.receiver != null)
			manager.receiver.afterSoftDropFall(e.getSource(), e.getPlayerId(), e.getFall());
	}

	@Override
	public void engineAfterHardDropFall(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.afterHardDropFall(e.getSource(), e.getPlayerId(), e.getFall());
		if(manager.receiver != null)
			manager.receiver.afterHardDropFall(e.getSource(), e.getPlayerId(), e.getFall());
	}

	@Override
	public void engineFieldEditorExit(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.fieldEditExit(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.fieldEditExit(e.getSource(), e.getPlayerId());
	}

	@Override
	public void enginePieceLocked(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.pieceLocked(e.getSource(), e.getPlayerId(), e.getLines());
		if(manager.receiver != null)
			manager.receiver.pieceLocked(e.getSource(), e.getPlayerId(), e.getLines());
	}

	@Override
	public boolean engineLineClearEnd(EngineEvent e) {
		boolean ret = manager.mode != null && manager.mode.lineClearEnd(e.getSource(), e.getPlayerId());
		if(manager.receiver != null)
			manager.receiver.lineClearEnd(e.getSource(), e.getPlayerId());
		return ret;
	}

	@Override
	public void engineSaveReplay(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.saveReplay(e.getSource(), e.getPlayerId(), e.getReplayProps());
		if(manager.receiver != null)
			manager.receiver.saveReplay(manager, e.getReplayProps());
	}

	@Override
	public void engineLoadReplay(EngineEvent e) {
		if(manager.mode != null)
			manager.mode.loadReplay(e.getSource(), e.getPlayerId(), e.getReplayProps());
	}


}
