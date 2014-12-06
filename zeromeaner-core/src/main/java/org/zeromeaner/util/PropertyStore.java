package org.zeromeaner.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

public class PropertyStore extends AbstractMap<String, String> {

	private PropertyStore instance = new PropertyStore();
	
	public PropertyStore get() {
		return instance;
	}
	
	protected ServiceHookDispatcher<PropertyStoreHook> hook = new ServiceHookDispatcher<>(PropertyStoreHook.class);
	
	@Override
	public String get(Object key) {
		if(!(key instanceof String))
			return null;
		return hook.dispatcher().get((String) key);
	}
	
	@Override
	public String put(String key, String value) {
		String old = get(key);
		hook.dispatcher().put(key, value);
		return old;
	}
	
	@Override
	public String remove(Object key) {
		if(!(key instanceof String))
			return null;
		String old = get(key);
		hook.dispatcher().remove((String) key);
		return old;
	}
	
	@Override
	public Set<Entry<String, String>> entrySet() {
		return new Entries();
	}

	protected class Entries extends AbstractSet<Entry<String, String>> {
		@Override
		public Iterator<Entry<String, String>> iterator() {
			return new Iterator<Entry<String,String>>() {
				protected Iterator<String> keys = hook.dispatcher().keySet().iterator();
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
				
				@Override
				public Entry<String, String> next() {
					String k = keys.next();
					return new SimpleImmutableEntry<>(k, hook.dispatcher().get(k));
				}
				
				@Override
				public boolean hasNext() {
					return keys.hasNext();
				}
			};
		}
	
		@Override
		public int size() {
			return hook.dispatcher().keySet().size();
		}
	}

}
