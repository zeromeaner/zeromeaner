package org.zeromeaner;

import java.lang.reflect.Method;
import java.net.URL;

import org.zeromeaner.jar.JarJarClassLoader;
import org.zeromeaner.jar.JarJarURLStreamHandlerFactory;

public class Main {

	public static void main(String[] args) throws Exception {
		JarJarClassLoader cl = new JarJarClassLoader(Main.class.getClassLoader());
		URL.setURLStreamHandlerFactory(new JarJarURLStreamHandlerFactory(cl));
		Thread.currentThread().setContextClassLoader(cl);
		Class<?> StandaloneMain = cl.loadClass("org.zeromeaner.gui.reskin.StandaloneMain");
		Method main = StandaloneMain.getMethod("main", String[].class);
		main.invoke(null, new Object[] {args});
	}
	
}
