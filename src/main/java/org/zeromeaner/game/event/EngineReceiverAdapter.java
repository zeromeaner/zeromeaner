package org.zeromeaner.game.event;

public class EngineReceiverAdapter implements EngineListener {

	private EventReceiver renderer;
	
	public EngineReceiverAdapter(EventReceiver receiver) {
		this.renderer = receiver;
	}

	public EventReceiver getRenderer() {
		return renderer;
	}
	
	public void setRenderer(EventReceiver renderer) {
		this.renderer = renderer;
	}
	
	@Override
	public void enginePlayerInit(EngineEvent e) {
		renderer.playerInit(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineGameStarted(EngineEvent e) {
		renderer.startGame(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineFrameFirst(EngineEvent e) {
		renderer.onFirst(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineFrameLast(EngineEvent e) {
		renderer.onLast(e.getSource(), e.getPlayerId());
	}

	@Override
	public boolean engineSettings(EngineEvent e) {
		renderer.onSetting(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineReady(EngineEvent e) {
		renderer.onReady(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineMove(EngineEvent e) {
		renderer.onMove(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineLockFlash(EngineEvent e) {
		renderer.onLockFlash(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineLineClear(EngineEvent e) {
		renderer.onLineClear(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineARE(EngineEvent e) {
		renderer.onARE(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineEndingStart(EngineEvent e) {
		renderer.onEndingStart(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineCustom(EngineEvent e) {
		renderer.onCustom(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineExcellent(EngineEvent e) {
		renderer.onExcellent(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineGameOver(EngineEvent e) {
		renderer.onGameOver(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineResults(EngineEvent e) {
		renderer.onResult(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public boolean engineFieldEditor(EngineEvent e) {
		renderer.onFieldEdit(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public void engineRenderFirst(EngineEvent e) {
		renderer.renderFirst(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderLast(EngineEvent e) {
		renderer.renderLast(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderSettings(EngineEvent e) {
		renderer.renderSetting(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderReady(EngineEvent e) {
		renderer.renderReady(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderMove(EngineEvent e) {
		renderer.renderMove(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderLockFlash(EngineEvent e) {
		renderer.renderLockFlash(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderLineClear(EngineEvent e) {
		renderer.renderLineClear(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderARE(EngineEvent e) {
		renderer.renderARE(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderEndingStart(EngineEvent e) {
		renderer.renderEndingStart(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderCustom(EngineEvent e) {
		renderer.renderCustom(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderExcellent(EngineEvent e) {
		renderer.renderExcellent(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderGameOver(EngineEvent e) {
		renderer.renderGameOver(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderResults(EngineEvent e) {
		renderer.renderResult(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderFieldEditor(EngineEvent e) {
		renderer.renderFieldEdit(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineRenderInput(EngineEvent e) {
		renderer.renderInput(e.getSource(), e.getPlayerId());
	}

	@Override
	public void engineBlockBreak(EngineEvent e) {
		renderer.blockBreak(e.getSource(), e.getPlayerId(), e.getBlockX(), e.getBlockY(), e.getBlock());
	}

	@Override
	public void engineCalcScore(EngineEvent e) {
		renderer.calcScore(e.getSource(), e.getPlayerId(), e.getLines());
	}

	@Override
	public void engineAfterSoftDropFall(EngineEvent e) {
		renderer.afterSoftDropFall(e.getSource(), e.getPlayerId(), e.getFall());
	}

	@Override
	public void engineAfterHardDropFall(EngineEvent e) {
		renderer.afterHardDropFall(e.getSource(), e.getPlayerId(), e.getFall());
	}

	@Override
	public void engineFieldEditorExit(EngineEvent e) {
		renderer.fieldEditExit(e.getSource(), e.getPlayerId());
	}

	@Override
	public void enginePieceLocked(EngineEvent e) {
		renderer.pieceLocked(e.getSource(), e.getPlayerId(), e.getLines());
	}

	@Override
	public boolean engineLineClearEnd(EngineEvent e) {
		renderer.lineClearEnd(e.getSource(), e.getPlayerId());
		return false;
	}

	@Override
	public void engineSaveReplay(EngineEvent e) {
	}

	@Override
	public void engineLoadReplay(EngineEvent e) {
	}

}
