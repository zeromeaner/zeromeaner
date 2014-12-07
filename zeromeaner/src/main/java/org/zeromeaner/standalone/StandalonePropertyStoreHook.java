package org.zeromeaner.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;

import org.zeromeaner.util.io.PropertyStoreHook;

public class StandalonePropertyStoreHook implements PropertyStoreHook {
	
	protected Properties props;
	
	public StandalonePropertyStoreHook() throws IOException{
		props = new Properties();
		InputStream in = new FileInputStream(new File(System.getProperty("user.dir"), "zeromeaner.properties"));
		try {
			props.load(in);
		} finally {
			in.close();
		}
	}

	protected void store() {
		try {
			OutputStream out = new FileOutputStream(new File(System.getProperty("user.dir"), "zeromeaner.properties"));
			try {
				props.store(out, "zeromeaner");
			} finally {
				out.close();
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String get(String key) {
		return props.getProperty(key);
	}

	@Override
	public void put(String key, String value) {
		props.setProperty(key, value);
		store();
	}

	@Override
	public void remove(String key) {
		props.remove(key);
		store();
	}

	@Override
	public Set<String> keySet() {
		return props.stringPropertyNames();
	}

}
