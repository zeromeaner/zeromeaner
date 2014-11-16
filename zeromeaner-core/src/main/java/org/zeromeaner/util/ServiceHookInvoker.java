package org.zeromeaner.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ServiceHookInvoker<T> {
	protected Class<T> ifc;
	protected T[] hooks;
	protected T dispatcher;
	
	public ServiceHookInvoker(Class<T> ifc) {
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
				if(ServiceHookInvoker.this.hooks == null)
					return null;
				Object ret = null;
				for(T hook : ServiceHookInvoker.this.hooks) {
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
