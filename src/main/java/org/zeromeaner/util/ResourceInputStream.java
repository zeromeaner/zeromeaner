package org.zeromeaner.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ResourceInputStream extends FilterInputStream {
	public static InputStream getStream(String resource) throws IOException {
		try {
			File localResource = new File("local-resources/" + resource);
			if(localResource.exists() && !localResource.isDirectory())
				return new FileInputStream(localResource);
		} catch(Throwable t) {
		}
		InputStream in = ResourceInputStream.class.getClassLoader().getResourceAsStream("org/zeromeaner/" + resource);
		if(in != null)
			return in;
		throw new IOException("Resource not found:" + resource);
	}
	
	public Reader newFileReader(String resource) throws IOException {
		return new InputStreamReader(new ResourceInputStream(resource));
	}
	
	public ResourceInputStream(String resource) throws IOException {
		super(getStream(resource));
	}

}
