package org.zeromeaner.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class Localization extends ResourceBundle {
	private static Class<?> getCallerClass(int depth) {
		StackTraceElement[] e = Thread.currentThread().getStackTrace();
		try {
			return Class.forName(e[depth].getClassName());
		} catch(ClassNotFoundException cnfe) {
			throw new RuntimeException(cnfe);
		}
	}
	
	private static Map<String, Localization> lzCache = Collections.synchronizedMap(new IdentityHashMap<String, Localization>());
	private static Map<String, String> stringCache = Collections.synchronizedMap(new IdentityHashMap<String, String>());
	
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
	private ResourceBundle bundle;
	private Map<String, Object> vals = Collections.synchronizedMap(new TreeMap<String, Object>());
	
	public Localization(Locale... defaults) {
		this(4, defaults);
	}
	
	private Localization(int depth, Locale... defaults) {
		this.base = getCallerClass(depth);
		bundle = this;
		defaults = Arrays.copyOf(defaults, defaults.length + 2);
		defaults[defaults.length - 2] = Locale.ENGLISH;
		for(final Locale dl : defaults) {
			ResourceBundle.Control ctrl = new ResourceBundle.Control() {
				@Override
				public Locale getFallbackLocale(String baseName, Locale locale) {
					return dl;
				}
			};
			try {
				bundle = ResourceBundle.getBundle(base.getName(), ctrl);
				break;
			} catch(MissingResourceException mre) {
			}
		}
	}
	
	public Class<?> getBase() {
		return base;
	}
	
	public String s(String key, String dv) {
		if(stringCache.containsKey(key))
			return stringCache.get(key);
		if(!vals.containsKey(key))
			vals.put(key, dv);
		String v;
		try {
			v = bundle.getString(key);
		} catch(MissingResourceException mre) {
			v = dv;
		}
		stringCache.put(key, v);
		return v;
	}
	
	public String s(String key) {
		String resource = base.getName().replaceAll("\\.", "/") + ".properties";
		if(!bundle.getLocale().getLanguage().isEmpty())
			resource += "_" + bundle.getLocale().getLanguage();
		return s(key, "\u00ab" + resource + "::" + key + "\u00bb");
	}

	@Override
	protected Object handleGetObject(String key) {
		return vals.get(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		synchronized(vals) {
			return Collections.enumeration(new ArrayList<String>(vals.keySet()));
		}
	}
}
