package org.zeromeaner.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ServiceHookDispatcher<T> {
	protected Class<T> ifc;
	protected T[] hooks;
	protected T dispatcher;
	
	public ServiceHookDispatcher(Class<T> ifc) {
		this.ifc = ifc;
		
		List<T> hooks = new ArrayList<>();
		for(T hook : ServiceLoader.load(ifc, ifc.getClassLoader())) {
			hooks.add(hook);
			this.hooks = hooks.toArray((T[]) Array.newInstance(ifc, hooks.size()));
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
