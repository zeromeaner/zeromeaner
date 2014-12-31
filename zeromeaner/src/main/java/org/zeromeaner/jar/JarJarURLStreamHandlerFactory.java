package org.zeromeaner.jar;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class JarJarURLStreamHandlerFactory implements URLStreamHandlerFactory {
	protected JarJarClassloader cl;
	
	public JarJarURLStreamHandlerFactory(JarJarClassloader cl) {
		this.cl = cl;
	}
	
	
	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if(protocol.startsWith("jarjar:")) {
			return cl.new JarJarURLStreamHandler();
		}
		return null;
	}

}
