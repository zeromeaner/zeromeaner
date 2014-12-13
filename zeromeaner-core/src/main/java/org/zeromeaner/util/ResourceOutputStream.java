package org.zeromeaner.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.zeromeaner.util.io.ResourceStreams;

import com.googlecode.sardine.Factory;
import com.googlecode.sardine.Sardine;

public class ResourceOutputStream extends FilterOutputStream {

	public static OutputStream getStream(String resource) throws IOException {
		return ResourceStreams.get().outputStream(resource);
	}
	
	public ResourceOutputStream(String resource) throws IOException {
		super(getStream(resource));
	}
}
