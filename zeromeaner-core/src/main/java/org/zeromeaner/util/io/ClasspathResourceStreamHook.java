package org.zeromeaner.util.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.Callable;

public class ClasspathResourceStreamHook implements ResourceStreamHook {

	@Override
	public void addInputHandler(String resource, PrioritizedHandler<Callable<InputStream>> handlers) {
		final URL url = ClasspathResourceStreamHook.class.getClassLoader().getResource("org/zeromeaner/" + resource);
		if(url == null)
			return;
		Callable<InputStream> handler = new Callable<InputStream>() {
			@Override
			public InputStream call() throws Exception {
				return url.openStream();
			}
		};
		handlers.add(Integer.MAX_VALUE, handler);
	}

	@Override
	public void addOutputHandler(String resource, PrioritizedHandler<Callable<OutputStream>> handlers) {
		// can't write to the classpath
	}

	@Override
	public void addDeleteHandler(String resource, PrioritizedHandler<Callable<Boolean>> handlers) {
		// TODO Auto-generated method stub
		
	}

}
