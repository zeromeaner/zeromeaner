package org.zeromeaner.game.event;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.zeromeaner.game.play.GameManager;

public class EngineManagerAdapter implements EngineListener {

	private GameManager manager;
	private EngineModeAdapter mode = new EngineModeAdapter(null);
	private EngineRendererAdapter rend = new EngineRendererAdapter(null);
	
	public EngineManagerAdapter(GameManager manager) {
		this.manager = manager;
	}
	
	private boolean invoke(EngineEvent e) {
		boolean ret = false;
		mode.setMode(manager.mode);
		if(mode.getMode() != null) {
			Object rval = e.invoke(mode);
			if(rval instanceof Boolean)
				ret = (ret || (Boolean) rval);
		}
		rend.setRenderer(manager.receiver);
		if(rend.getRenderer() != null && !ret) {
			Object rval = e.invoke(rend);
			if(rval instanceof Boolean)
				ret = (ret || (Boolean) rval);
		}
		return ret;
	}
	
	@Override
	public void enginePlayerInit(EngineEvent e) {
		invoke(e);
	}

	@Override
	public void engineGameStarted(EngineEvent e) {
		invoke(e);
	}

	@Override
	public void engineFrameFirst(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineFrameLast(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public boolean engineSettings(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineReady(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineMove(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineLockFlash(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineLineClear(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineARE(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineEndingStart(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineCustom(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineExcellent(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineGameOver(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineResults(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public boolean engineFieldEditor(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public void engineRenderFirst(EngineEvent e) {
		invoke(e);
	}

	@Override
	public void engineRenderLast(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderSettings(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderReady(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderMove(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderLockFlash(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderLineClear(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderARE(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderEndingStart(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderCustom(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderExcellent(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderGameOver(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderResults(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderFieldEditor(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineRenderInput(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineBlockBreak(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineCalcScore(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineAfterSoftDropFall(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineAfterHardDropFall(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineFieldEditorExit(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void enginePieceLocked(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public boolean engineLineClearEnd(EngineEvent e) {
		return invoke(e);
	}

	@Override
	public void engineSaveReplay(EngineEvent e) {
		invoke(e);
		
	}

	@Override
	public void engineLoadReplay(EngineEvent e) {
		invoke(e);
		
	}


}
