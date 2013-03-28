package org.zeromeaner.util;

import java.awt.EventQueue;
import java.lang.reflect.Method;

public class EQInvoker {
	private static Method findMethod(Class<?> cls, String name, Object... args) {
		for(Method m : cls.getMethods()) {
			if(!m.getName().equals(name))
				continue;
			if(m.getParameterTypes().length != args.length)
				continue;
			boolean match = true;
			for(int i = 0; i < args.length; i++) {
				if(args[i] != null && !m.getParameterTypes()[i].isInstance(args[i]))
					match = false;
			}
			if(!match)
				continue;
			return m;
		}
		throw new NoSuchMethodError(name);
	}
	
	public static boolean reinvoke(boolean wait, final Object thiz, final Object... args) {
		if(EventQueue.isDispatchThread())
			return false;
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		final Method m = findMethod(thiz.getClass(), ste[2].getMethodName(), args);
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					m.invoke(thiz, args);
				} catch(Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		};
		if(wait) {
			try {
				EventQueue.invokeAndWait(task);
			} catch(Exception ex) {
				throw new RuntimeException(ex);
			}
		} else
			EventQueue.invokeLater(task);
		return true;
	}
}
