package org.zeromeaner.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class NavigableProperties {
	private NavigableProperties parent;
	private NavigableMap<String, String> backing;
	private String prefix;
	
	public NavigableProperties() {
		this(null, new TreeMap<String, String>(), "");
	}
	
	protected NavigableProperties(
			NavigableProperties parent, 
			NavigableMap<String, String> backing, 
			String prefix) {
		this.parent = parent;
		this.backing = backing;
		this.prefix = prefix;
	}
	
	public void clear() {
		backing.clear();
	}
	
	public String getProperty(String key) {
		return getProperty(key, null);
	}
	
	public String getProperty(String key, String defaultValue) {
		key = prefix + key;
		if(!backing.containsKey(key))
			return defaultValue;
		return backing.get(key);
	}
	
	public String setProperty(String key, String value) {
		key = prefix + key;
		return backing.put(key, value);
	}
	
	public String removeProperty(String key) {
		key = prefix + key;
		return backing.remove(key);
	}
	
	public boolean hasProperty(String key) {
		key = prefix + key;
		return backing.containsKey(key);
	}
	
	public Set<String> stringPropertyNames() {
		Set<String> propertyNames = new TreeSet<String>();
		for(String key : backing.keySet()) {
			propertyNames.add(key.substring(prefix.length()));
		}
		return propertyNames;
	}
	
	protected NavigableProperties newSubProperties(NavigableMap<String, String> submap, String subPrefix) {
		return new NavigableProperties(this, submap, subPrefix);
	}
	
	public NavigableProperties subProperties(String keyPrefix) {
		String subPrefix = prefix + keyPrefix;
		
		char[] from = subPrefix.toCharArray();
		char[] to = subPrefix.toCharArray();
		
		// add 1 to the last character, overflowing as needed
		for(int i = 1; i < to.length; i++) {
			// add 1 to the last character and check for overflow
			if(++to[to.length - i] != '\0')
				break; // done if not overflow
		}
		
		NavigableMap<String, String> submap = backing.subMap(new String(from), true, new String(to), false);
		return newSubProperties(submap, subPrefix);
	}
	
	public NavigableProperties getRoot() {
		NavigableProperties p = this;
		while(p.parent != null)
			p = p.parent;
		return p;
	}
	
	public void store(OutputStream out, String comment) throws IOException {
		Properties p = new Properties();
		p.putAll(backing);
		p.store(out, comment);
	}
	
	public void load(InputStream in) throws IOException {
		Properties p = new Properties();
		p.load(in);
		for(String key : p.stringPropertyNames()) {
			if(!key.startsWith(prefix))
				continue;
			backing.put(key, p.getProperty(key));
		}
	}
}
