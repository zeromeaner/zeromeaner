package org.zeromeaner.util.io;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.zeromeaner.util.ServiceHookDispatcher;

public class ResourceStreams {
	
	private static final Logger log = Logger.getLogger(ResourceStreams.class);
	
	private static ResourceStreams instance = new ResourceStreams();
	public static ResourceStreams get() {
		return instance;
	}
	
	private ServiceHookDispatcher<ResourceStreamHook> hook;
	
	private ResourceStreams() {
		hook = new ServiceHookDispatcher<>(ResourceStreamHook.class);
	}
	
	public InputStream inputStream(String resource) throws FileNotFoundException {
		PrioritizedHandler<Callable<InputStream>> handlers = new PrioritizedHandler<>();
		hook.dispatcher().addInputHandler(resource, handlers);
		for(Callable<InputStream> handler : handlers.get()) {
			try {
				return handler.call();
			} catch(Exception e) {
				log.warn(e);
			}
		}
		throw new FileNotFoundException(resource);
	}
	
	public OutputStream outputStream(String resource) {
		PrioritizedHandler<Callable<OutputStream>> handlers = new PrioritizedHandler<>();
		hook.dispatcher().addOutputHandler(resource, handlers);
		for(Callable<OutputStream> handler : handlers.get()) {
			try {
				return handler.call();
			} catch(Exception e) {
				log.warn(e);
			}
		}
		return null;
	}
}
