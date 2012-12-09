package org.zeromeaner.game.event;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.zeromeaner.game.play.GameManager;

public class EngineManagerAdapter implements InvocationHandler {
	public static EngineListener newEngineManagerAdapter(GameManager manager) {
		return (EngineListener) Proxy.newProxyInstance(EngineModeAdapter.class.getClassLoader(), new Class[] {EngineListener.class}, new EngineManagerAdapter(manager));
	}
	
	private GameManager manager;
	private EngineModeAdapter mode;
	private EngineRendererAdapter renderer;
	
	private EngineManagerAdapter(GameManager manager) {
		this.manager = manager;
		mode = new EngineModeAdapter(manager.mode);
		renderer = new EngineRendererAdapter(manager.receiver);
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		boolean ret = false;
		mode.setMode(manager.mode);
		Object rval = method.invoke(mode, args);
		if(rval instanceof Boolean)
			ret = (ret || (Boolean) rval);
		renderer.setRenderer(manager.receiver);
		rval = method.invoke(renderer, args);
		if(rval instanceof Boolean)
			ret = (ret || (Boolean) rval);
		return rval;
	}
}
