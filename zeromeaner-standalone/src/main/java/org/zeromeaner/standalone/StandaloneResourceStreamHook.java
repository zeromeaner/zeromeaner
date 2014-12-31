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
	
	@Override
	public void addInputHandler(String resource, PrioritizedHandler<Callable<InputStream>> handlers) {
		File FILE_BASE = new File(System.getProperty("user.dir"));
		final File f;
		if(resource.startsWith(FILE_BASE.getPath()))
			f = new File(resource);
		else
			f = new File(FILE_BASE, resource);
		if(!f.canRead()) 
			return;
		Callable<InputStream> handler = new Callable<InputStream>() {
			@Override
			public InputStream call() throws Exception {
				return new FileInputStream(f);
			}
		};
		handlers.add(0, handler);
	}

	@Override
	public void addOutputHandler(String resource, PrioritizedHandler<Callable<OutputStream>> handlers) {
		File FILE_BASE = new File(System.getProperty("user.dir"));
		final File f;
		if(resource.startsWith(FILE_BASE.getPath()))
			f = new File(resource);
		else
			f = new File(FILE_BASE, resource);
		f.getParentFile().mkdirs();
		Callable<OutputStream> handler = new Callable<OutputStream>() {
			@Override
			public OutputStream call() throws Exception {
				return new FileOutputStream(f);
			}
		};
		handlers.add(0, handler);
	}

	@Override
	public void addDeleteHandler(String resource, PrioritizedHandler<Callable<Boolean>> handlers) {
		File FILE_BASE = new File(System.getProperty("user.dir"));
		final File f;
		if(resource.startsWith(FILE_BASE.getPath()))
			f = new File(resource);
		else
			f = new File(FILE_BASE, resource);
		f.getParentFile().mkdirs();
		Callable<Boolean> handler = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return f.delete();
			}
		};
		handlers.add(0, handler);
	}

}
