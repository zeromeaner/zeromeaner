package org.zeromeaner;

import java.lang.reflect.Method;

import org.zeromeaner.jar.JarJarClassloader;

public class Main {

	public static void main(String[] args) throws Exception {
		ClassLoader cl = new JarJarClassloader(Main.class.getClassLoader());
		Thread.currentThread().setContextClassLoader(cl);
		Class<?> StandaloneMain = cl.loadClass("org.zeromeaner.gui.reskin.StandaloneMain");
		Method main = StandaloneMain.getMethod("main", String[].class);
		main.invoke(null, new Object[] {args});
	}
	
}
