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

import org.zeromeaner.gui.reskin.StandaloneMain;
import org.zeromeaner.util.io.ResourceStreams;

import com.googlecode.sardine.Factory;
import com.googlecode.sardine.Sardine;

public class ResourceInputStream extends FilterInputStream {
	
	public static InputStream getStream(String resource) throws IOException {
		return ResourceStreams.get().inputStream(resource);
	}
	
	public static Reader newReader(String resource) throws IOException {
		return new InputStreamReader(new ResourceInputStream(resource));
	}
	
	public ResourceInputStream(String resource) throws IOException {
		super(getStream(resource));
	}

}
