package org.zeromeaner.util.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public interface ResourceStreamHook {
	public void addInputHandler(String resource, PrioritizedHandler<Callable<InputStream>> handlers);
	public void addOutputHandler(String resource, PrioritizedHandler<Callable<OutputStream>> handlers);
}
