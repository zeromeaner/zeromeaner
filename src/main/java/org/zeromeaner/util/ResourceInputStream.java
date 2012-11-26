package org.zeromeaner.util;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceInputStream extends FilterInputStream {
	public static InputStream getStream(String resource) throws IOException {
		InputStream in = ResourceInputStream.class.getResourceAsStream(resource);
		if(in != null)
			return in;
		return new FileInputStream(resource);
	}
	
	public ResourceInputStream(String resource) throws IOException {
		super(getStream(resource));
	}

}
