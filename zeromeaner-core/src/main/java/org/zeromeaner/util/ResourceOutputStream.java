package org.zeromeaner.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.zeromeaner.gui.reskin.StandaloneApplet;
import org.zeromeaner.gui.reskin.StandaloneMain;
import org.zeromeaner.util.ResourceInputStream.ResourceDownloadStream;

import com.googlecode.sardine.Factory;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.util.SardineException;

public class ResourceOutputStream extends FilterOutputStream {
	private static Sardine s;
	static {
		try {
			s = new Factory().begin("zero", "zero");
		} catch(IOException ioe) {
		}
	}

	public static OutputStream getStream(String resource) throws IOException {
		if(
				(resource.startsWith("config/setting/")
						|| StandaloneApplet.isApplet() && resource.startsWith("replay/"))
				&& !ResourceInputStream.dontDownload.contains(resource))
			return new ResourceUploadStream(resource);
		try {
			File localResource = new File(System.getProperty("user.dir"), "local-resources/" + resource);
			localResource.getParentFile().mkdirs();
			return new FileOutputStream(localResource);
		} catch(Throwable t) {
			return new OutputStream() {
				@Override
				public void write(int b) throws IOException {
				}
			};
		}
	}
	
	public static class ResourceUploadStream extends FilterOutputStream {
		public static OutputStream getUploadStream(final String resource) throws IOException {
			final URL url = new URL("http://" + StandaloneApplet.url.getHost() + "/webdav/" + StandaloneMain.userId + "/" + resource);
			System.out.println("Creating new ResourceUploadStream to " + url);
			return new ByteArrayOutputStream() {
				@Override
				public void close() throws IOException {
					super.close();
					if(resource.startsWith("config/")) {
						ResourceDownloadStream.getCache().put(resource, toByteArray());
						return;
					}
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
					byte[] buf = toByteArray();
					System.out.println("Putting " + buf.length + " bytes to " + url);
					s.put(url.toString(), buf);
				}
			};
		}
		
		public ResourceUploadStream(String resource) throws IOException {
			super(getUploadStream(resource));
		}
	}
	
	public ResourceOutputStream(String resource) throws IOException {
		super(getStream(resource));
	}
}
