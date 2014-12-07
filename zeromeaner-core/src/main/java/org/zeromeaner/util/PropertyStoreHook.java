package org.zeromeaner.util;

import java.util.Set;

public interface PropertyStoreHook {
	public String get(String key);
	public void put(String key, String value);
	public void remove(String key);
	public Set<String> keySet();
}
