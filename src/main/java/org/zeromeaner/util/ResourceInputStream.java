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

import org.zeromeaner.gui.applet.AppletMain;

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
	
	private static Collection<String> dontDownload = Arrays.asList(
			"config/setting/netlobby_serverlist_dev.cfg",
			"config/setting/netlobby_serverlist.cfg"
			);
	
	public static InputStream getStream(String resource) throws IOException {
		InputStream in = null;
		if(
				AppletMain.isApplet() 
				&& resource.startsWith("config/setting/")
				&& !dontDownload.contains(resource))
			try {
				in = new ResourceDownloadStream(resource);
			} catch(IOException ioe) {
			}
		if(in != null)
			return in;
		try {
			File localResource = new File("local-resources/" + resource);
			if(localResource.exists() && !localResource.isDirectory())
				return new FileInputStream(localResource);
		} catch(Throwable t) {
		}
		in = ResourceInputStream.class.getClassLoader().getResourceAsStream("org/zeromeaner/" + resource);
		if(in != null)
			return in;
		throw new IOException("Resource not found:" + resource);
	}
	
	public static class ResourceDownloadStream extends FilterInputStream {
		public static InputStream getDownloadStream(String resource) throws IOException {
			final URL url = new URL("http://www.zeromeaner.org/webdav/" + AppletMain.userId + "/" + resource);
			return s.getInputStream(url.toString());
		}
		
		public ResourceDownloadStream(String resource) throws IOException {
			super(getDownloadStream(resource));
		}
	}
	
	public static Reader newReader(String resource) throws IOException {
		return new InputStreamReader(new ResourceInputStream(resource));
	}
	
	public ResourceInputStream(String resource) throws IOException {
		super(getStream(resource));
	}

}
