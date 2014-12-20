package org.zeromeaner.util.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PrioritizedHandler<T> {
	private Map<Integer, List<T>> handlers;
	
	public PrioritizedHandler() {
		handlers = new TreeMap<>();
	}
	
	public void add(int priority, T handler) {
		if(!handlers.containsKey(priority))
			handlers.put(priority, new ArrayList<T>());
		handlers.get(priority).add(handler);
	}
	
	public List<T> get() {
		List<T> h = new ArrayList<>();
		for(List<T> hl : handlers.values())
			h.addAll(hl);
		return h;
	}
}
