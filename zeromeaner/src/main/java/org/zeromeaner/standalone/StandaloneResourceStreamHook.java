package org.zeromeaner.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.zeromeaner.util.io.PrioritizedHandler;
import org.zeromeaner.util.io.ResourceStreamHook;

public class StandaloneResourceStreamHook implements ResourceStreamHook {
	
	protected static final File FILE_BASE = new File(System.getProperty("user.home"), ".zeromeaner");
	static {
		FILE_BASE.mkdirs();
	}

	@Override
	public void addInputHandler(String resource, PrioritizedHandler<Callable<InputStream>> handlers) {
		final File f = new File(FILE_BASE, resource);
		if(!f.canRead()) 
			return;
		Callable<InputStream> handler = new Callable<InputStream>() {
			@Override
			public InputStream call() throws Exception {
				return new FileInputStream(f);
			}
		};
		handlers.add(-1, handler);
	}

	@Override
	public void addOutputHandler(String resource, PrioritizedHandler<Callable<OutputStream>> handlers) {
		final File f = new File(FILE_BASE, resource);
		f.getParentFile().mkdirs();
		Callable<OutputStream> handler = new Callable<OutputStream>() {
			@Override
			public OutputStream call() throws Exception {
				return new FileOutputStream(f);
			}
		};
		handlers.add(-1, handler);
	}

}
