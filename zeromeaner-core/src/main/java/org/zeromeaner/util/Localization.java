package org.zeromeaner.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class Localization {
	private static Class<?> getCallerClass(int depth) {
		StackTraceElement[] e = Thread.currentThread().getStackTrace();
		try {
			return Class.forName(e[depth].getClassName());
		} catch(ClassNotFoundException cnfe) {
			throw new RuntimeException(cnfe);
		}
	}
	
	private static Map<String, Localization> lzCache = Collections.synchronizedMap(new IdentityHashMap<String, Localization>());
	
	public static Localization lz() {
		return new Localization(4);
	}
	
	public static String lz(String key, String dv) {
		Localization lz;
		synchronized(lzCache) {
			lz = lzCache.get(key);
			if(lz == null)
				lzCache.put(key, lz = new Localization(4));
		}
		return lz.s(key, dv);
	}
	
	public static String lz(String key) {
		return lz(key, key);
	}
	
	private Class<?> base;
	private Properties props = new Properties();
	private String basename;
	
	public Localization(Locale... defaults) {
		this(4, defaults);
	}
	
	private Localization(int depth, Locale... defaults) {
		this.base = getCallerClass(depth);
		List<Locale> locales = new ArrayList<Locale>();
		locales.add(Locale.getDefault());
		locales.addAll(Arrays.asList(defaults));
		InputStream in = base.getResourceAsStream(basename = (base.getSimpleName() + ".properties"));
		try {
			if(in != null) {
				props.load(in);
				in.close();
			}
		} catch(IOException ioe) {
		}
		for(Locale l : locales) {
			in = base.getResourceAsStream(basename = (base.getSimpleName() + "_" + l.getLanguage() + ".properties"));
			if(in == null)
				continue;
			try {
				props.clear();
				props.load(in);
				in.close();
			} catch(IOException ioe) {
			}
			break;
		}
	}
	
	public Class<?> getBase() {
		return base;
	}
	
	public String s(String key, String dv) {
		if(!props.containsKey(key))
			props.setProperty(key, dv);
		String v;
		try {
			v = props.getProperty(key);
		} catch(MissingResourceException mre) {
			v = dv;
		}
		return v;
	}
	
	public String s(String key) {
		return s(key, "\u00ab" + basename + "::" + key + "\u00bb");
	}

}
