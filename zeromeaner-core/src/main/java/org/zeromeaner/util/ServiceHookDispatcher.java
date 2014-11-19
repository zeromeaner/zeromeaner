package org.zeromeaner.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceHookDispatcher<T> {
	private static final Logger log = LoggerFactory.getLogger(ServiceHookDispatcher.class);
	
	protected Class<T> ifc;
	protected T[] hooks;
	protected T dispatcher;
	
	public ServiceHookDispatcher(Class<T> ifc) {
		this.ifc = ifc;
		
		List<T> hooks = new ArrayList<>();
		for(T hook : ServiceLoader.load(ifc, ifc.getClassLoader())) {
			hooks.add(hook);
			this.hooks = hooks.toArray((T[]) Array.newInstance(ifc, hooks.size()));
			LoggerFactory.getLogger(ifc).info("Found hook " + hook);
		}
		
		dispatcher = (T) Proxy.newProxyInstance(ifc.getClassLoader(), new Class<?>[]{ifc}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				if(ServiceHookDispatcher.this.hooks == null)
					return null;
				Object ret = null;
				for(T hook : ServiceHookDispatcher.this.hooks) {
					ret = method.invoke(hook, args);
				}
				return ret;
			}
		});
	}
	
	public T dispatcher() {
		return dispatcher;
	}
}
