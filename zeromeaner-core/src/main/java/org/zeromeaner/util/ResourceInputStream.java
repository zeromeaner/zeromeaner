package org.zeromeaner.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.zeromeaner.gui.reskin.StandaloneApplet;
import org.zeromeaner.gui.reskin.StandaloneMain;

import com.googlecode.sardine.Factory;
import com.googlecode.sardine.Sardine;

public class ResourceInputStream extends FilterInputStream {
	private static Sardine s;
	static {
		try {
			s = new Factory().begin("zero", "zero");
		} catch(IOException ioe) {
		}
	}
	
	static Collection<String> dontDownload = Arrays.asList(
			"config/setting/netlobby_serverlist_dev.cfg",
			"config/setting/netlobby_serverlist.cfg"
			);
	
	public static InputStream getStream(String resource) throws IOException {
		InputStream in = null;
//		if(ResourceDownloadStream.getCache().containsKey(resource))
//			return new ByteArrayInputStream(ResourceDownloadStream.getCache().get(resource));
//		if(
//				StandaloneApplet.isApplet()
//				&& (resource.startsWith("config/setting/") || resource.startsWith("replay/"))
//				&& !dontDownload.contains(resource)
//				)
//			try {
//				in = new ResourceDownloadStream(resource);
//			} catch(IOException ioe) {
//			}
//		if(in != null)
//			return in;
		try {
			File localResource = new File(System.getProperty("user.dir"), "local-resources/" + resource);
			if(localResource.exists() && !localResource.isDirectory())
				return new FileInputStream(localResource);
		} catch(Throwable t) {
		}
		in = ResourceInputStream.class.getClassLoader().getResourceAsStream("org/zeromeaner/" + resource);
		if(in != null)
			return in;
//		throw new IOException("Resource not found:" + resource);
		return new FileInputStream(resource);
	}
	
	public static URL getURL(String resource) {
		URL in = null;
		if(
				StandaloneApplet.isApplet() 
				&& (resource.startsWith("config/setting/") || resource.startsWith("replay/"))
				&& !dontDownload.contains(resource))
			try {
				in = new URL("http://" + StandaloneApplet.url.getHost() + "/webdav/" + StandaloneMain.userId + "/" + resource);
			} catch(IOException ioe) {
			}
		if(in != null)
			return in;
		try {
			File localResource = new File(System.getProperty("user.dir"), "local-resources/" + resource);
			if(localResource.exists() && !localResource.isDirectory())
				return localResource.toURI().toURL();
		} catch(Throwable t) {
		}
		in = ResourceInputStream.class.getClassLoader().getResource("org/zeromeaner/" + resource);
		if(in != null)
			return in;
//		throw new IOException("Resource not found:" + resource);
		try {
			return new File(resource).toURI().toURL();
		} catch(Throwable t) {
		}
		return null;
	}
	
	/*
	public static class ResourceDownloadStream extends FilterInputStream {
		private static final Logger log = Logger.getLogger(ResourceDownloadStream.class);
		
		private static Map<String, byte[]> cache;
		public static Map<String, byte[]> getCache() {
			if(cache != null)
				return cache;
			cache = new TreeMap<String, byte[]>();
			try {
				log.info("Loading resource cache");
				URL url = new URL("http://" + StandaloneApplet.url.getHost() + "/webdav/" + StandaloneMain.userId + "/cache.jdk");
				InputStream in = s.getInputStream(url.toString());
				cache.putAll((Map<String, byte[]>) new ObjectInputStream(in).readObject());
				in.close();
			} catch(Exception ex) {
			}
			return cache;
		}
		
		public static void commitCache() throws IOException {
			URL url = new URL("http://" + StandaloneApplet.url.getHost() + "/webdav/" + StandaloneMain.userId + "/cache.jdk");
			String dir = url.toString().substring(0, url.toString().lastIndexOf("/"));
			List<String> dirs = new ArrayList<String>();
			while(!dir.equals("http://" + StandaloneApplet.url.getHost() + "/webdav")) {
				dirs.add(0, dir);
				dir = dir.substring(0, dir.lastIndexOf("/"));
			}
			for(String d : dirs) {
				try {
					s.createDirectory(d);
				} catch(SardineException se) {
				}
			}
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(bout);
			oout.writeObject(getCache());
			oout.close();
			
			s.put(url.toString(), bout.toByteArray());
			
		}
		
		public static InputStream getDownloadStream(String resource) throws IOException {
			if(getCache().containsKey(resource)) {
				log.info("Returning cached copy of resource " + resource);
				return new ByteArrayInputStream(getCache().get(resource));
			}
			final URL url = new URL("http://" + StandaloneApplet.url.getHost() + "/webdav/" + StandaloneMain.userId + "/" + resource);
			return s.getInputStream(url.toString());
		}
		
		public ResourceDownloadStream(String resource) throws IOException {
			super(getDownloadStream(resource));
		}
	}
	*/
	
	public static Reader newReader(String resource) throws IOException {
		return new InputStreamReader(new ResourceInputStream(resource));
	}
	
	public ResourceInputStream(String resource) throws IOException {
		super(getStream(resource));
	}

}
