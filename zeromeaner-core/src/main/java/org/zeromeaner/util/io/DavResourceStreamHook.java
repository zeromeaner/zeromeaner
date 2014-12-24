package org.zeromeaner.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.zeromeaner.util.Options;
import org.zeromeaner.util.Session;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.util.SardineException;

public class DavResourceStreamHook implements ResourceStreamHook {
	
	public static final String URL_BASE = "http://www.zeromeaner.org/dav/";
	
	public static final String getUserURLBase() {
		return URL_BASE + Session.getUser() + "/";
	}
	
	protected static final Pattern NON_DAV = Pattern.compile("^(config|gui|res)/.*");
	
	protected Sardine sardine;
	
	public DavResourceStreamHook() throws SardineException {
		sardine = SardineFactory.begin("zero", "zero");
	}

	@Override
	public void addInputHandler(final String resource, PrioritizedHandler<Callable<InputStream>> handlers) {
		if(!Options.general().DAV_ENABLED.value())
			return;
		if(Session.ANONYMOUS_USER.equals(Session.getUser()))
			return;
		if(NON_DAV.matcher(resource).matches())
			return;
		try {
			sardine.getInputStream(URL_BASE + Session.getUser() + "/" + resource).close();;
		} catch(Exception e) {
			return;
		}
		Callable<InputStream> handler = new Callable<InputStream>() {
			@Override
			public InputStream call() throws Exception {
				return sardine.getInputStream(URL_BASE + Session.getUser() + "/" + resource);
			}
		};
		handlers.add(0, handler);
	}

	@Override
	public void addOutputHandler(final String resource, PrioritizedHandler<Callable<OutputStream>> handlers) {
		if(!Options.general().DAV_ENABLED.value())
			return;
		if(Session.ANONYMOUS_USER.equals(Session.getUser()))
			return;
		if(NON_DAV.matcher(resource).matches())
			return;
		Callable<OutputStream> handler = new Callable<OutputStream>() {
			@Override
			public OutputStream call() throws Exception {
				return new DavOutputStream(resource);
			}
		};
		handlers.add(0, handler);
	}
	
	protected class DavOutputStream extends ByteArrayOutputStream {
		protected String resource;
		
		public DavOutputStream(String resource) {
			this.resource = resource;
		}
		
		@Override
		public void close() throws IOException {
			super.close();
			String dirs = (Session.getUser() + "/" + resource).replaceAll("/[^/]*$", "");
			String dir = "";
			for(String s : dirs.split("/")) {
				if(dir.isEmpty())
					dir = s;
				else
					dir = dir + "/" + s;
				try {
					sardine.createDirectory(URL_BASE + dir);
				} catch(SardineException e) {
				}
			}
			sardine.put(URL_BASE + Session.getUser() + "/" + resource, toByteArray());
		}
	}

	@Override
	public void addDeleteHandler(final String resource, PrioritizedHandler<Callable<Boolean>> handlers) {
		if(!Options.general().DAV_ENABLED.value())
			return;
		if(Session.ANONYMOUS_USER.equals(Session.getUser()))
			return;
		if(NON_DAV.matcher(resource).matches())
			return;
		try {
			sardine.getInputStream(URL_BASE + Session.getUser() + "/" + resource).close();;
		} catch(Exception e) {
			return;
		}
		Callable<Boolean> handler = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				sardine.delete(URL_BASE + Session.getUser() + "/" + resource);
				return true;
			}
		};
		handlers.add(0, handler);
	}

}
