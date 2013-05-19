package org.zeromeaner.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public abstract class Threads {
	
	public static ThreadFactory namedFactory(final String prefix) {
		final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = defaultFactory.newThread(r);
				t.setName(prefix + t.getName());
				return t;
			}
		};
	}
	
	private Threads() {}
}
