package org.zeromeaner.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ResourceOutputStream extends FilterOutputStream {
	public static OutputStream getStream(String resource) throws IOException {
		File localResource = new File("local-resources/" + resource);
		localResource.getParentFile().mkdirs();
		return new FileOutputStream(localResource);
	}
	
	public ResourceOutputStream(String resource) throws IOException {
		super(getStream(resource));
	}
}
