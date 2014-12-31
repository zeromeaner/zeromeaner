package org.zeromeaner.jar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarJarClassloader extends ClassLoader {

	protected Map<String, List<URL>> jarContents;
	protected Map<String, byte[]> innerContents;

	public JarJarClassloader(ClassLoader parent) throws IOException {
		super(parent);
		jarContents = new TreeMap<>();
		innerContents = new TreeMap<>();

		scanJarJar();
	}

	protected void scanJarJar() throws IOException {
		URL selfURL = JarJarClassloader.class.getProtectionDomain().getCodeSource().getLocation();
		selfURL = new URL(selfURL.toString().replaceAll("!.*", ""));
		ZipInputStream zin = new ZipInputStream(selfURL.openStream());
		try {
			for(ZipEntry e = zin.getNextEntry(); e != null; e = zin.getNextEntry()) {
				if(e.isDirectory())
					continue;
				if(e.getName().endsWith(".jar"))
					scanInnerJar(JarJarClassloader.class.getClassLoader().getResource(e.getName()));
			}
		} finally {
			zin.close();
		}
	}

	protected void scanInnerJar(URL innerJar) throws IOException {
		ZipInputStream zin = new ZipInputStream(innerJar.openStream());
		try {
			for(ZipEntry e = zin.getNextEntry(); e != null; e = zin.getNextEntry()) {
				if(e.isDirectory())
					continue;
				if(!jarContents.containsKey(e.getName()))
					jarContents.put(e.getName(), new ArrayList<URL>());
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				jarContents.get(e.getName()).add(innerJar);
				byte[] b = new byte[8192];
				for(int r = zin.read(b); r != -1; r = zin.read(b))
					buf.write(b, 0, r);
				innerContents.put(innerJar + "!" + e.getName(), buf.toByteArray());
			}
		} finally {
			zin.close();
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		String path = name.replace('.', '/').concat(".class");
		if(jarContents.containsKey(path)) {
			byte[] buf = innerContents.get(jarContents.get(path).get(0) + "!" + path);
			return defineClass(name, buf, 0, buf.length);
		}
		throw new ClassNotFoundException(name);
	}

	@Override
	protected URL findResource(String name) {
		Enumeration<URL> urls;
		try {
			urls = findResources(name);
		} catch (IOException e) {
			return null;
		}
		if(!urls.hasMoreElements())
			return null;
		return urls.nextElement();
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		if(!jarContents.containsKey(name))
			return Collections.enumeration(Collections.<URL>emptyList());
		List<URL> urls = new ArrayList<>();
		for(URL innerJar : jarContents.get(name)) {
			urls.add(jarjarURL(name, innerJar));
		}
		return Collections.enumeration(urls);
	}

	protected URL jarjarURL(String path, URL innerJar) throws IOException {
		return new URL(null, "jarjar:" + innerJar.toString() + "!" + path, new JarJarURLStreamHandler());
	}

	protected class JarJarURLStreamHandler extends URLStreamHandler {
		public JarJarURLStreamHandler() {
		}
		
		@Override
		protected URLConnection openConnection(URL u) throws IOException {
			int idx = u.toString().lastIndexOf("!");
			String path = u.toString().substring(idx + 1);
			URL innerJar = new URL(u.toString().substring(0, idx).replaceAll("^jarjar:", ""));
			return new JarJarURLConnection(u, path, innerJar);
		}
	}
	
	protected class JarJarURLConnection extends URLConnection {
		protected String path;
		protected URL innerJar;

		public JarJarURLConnection(URL url, String path, URL innerJar) {
			super(url);
			this.path = path;
			this.innerJar = innerJar;
		}

		@Override
		public void connect() throws IOException {
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(innerContents.get(innerJar + "!" + path));
		}
	}
}
