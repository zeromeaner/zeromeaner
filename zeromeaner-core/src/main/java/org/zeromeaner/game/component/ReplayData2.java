package org.zeromeaner.game.component;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.zeromeaner.util.CustomProperties;

public class ReplayData2 extends ReplayData {
	private int max;
	private TreeMap<Integer, Integer> inputData;
	private Map<String, TreeMap<Integer, Integer>> additionalData;
	
	public ReplayData2() {
	}

	@Override
	public void reset() {
		inputData = new TreeMap<>();
		additionalData = new HashMap<>();
		max = 0;
	}

	@Override
	public void copy(ReplayData r) {
		reset();
		
		for(int frame = 0; frame < r.max(); frame++) {
			setInputData(r.getInputData(frame), frame);
		}
	}

	@Override
	public void setInputData(int input, int frame) {
		TreeMap<Integer, Integer> data = inputData;
		boolean skip = false;
		Map.Entry<Integer, Integer> e = data.floorEntry(frame);
		if(e != null && e.getValue() == input)
			skip = true;
		if(!skip)
			data.put(frame, input);
		max = Math.max(max, frame);
	}

	@Override
	public void setAdditionalData(String key, int value, int frame) {
		if(!additionalData.containsKey(key))
			additionalData.put(key, new TreeMap<Integer, Integer>());
		TreeMap<Integer, Integer> data = additionalData.get(key);
		boolean skip = false;
		Map.Entry<Integer, Integer> e = data.floorEntry(frame);
		if(e != null && e.getValue() == value)
			skip = true;
		if(!skip)
			data.put(frame, value);
		max = Math.max(max, frame);
	}

	@Override
	public int getInputData(int frame) {
		TreeMap<Integer, Integer> data = inputData;
		Map.Entry<Integer, Integer> e = data.floorEntry(frame);
		if(e != null)
			return e.getValue();
		throw new IllegalArgumentException();
	}

	@Override
	public Integer getAdditionalData(String key, int frame) {
		TreeMap<Integer, Integer> data = additionalData.get(key);
		return data.get(frame);
	}

	@Override
	public void writeProperty(CustomProperties p, int id, int maxFrame) {
		p.setProperty(id + ".r.max", max());
		for(int frame = 0; frame < max(); frame++) {
			Integer data = inputData.get(frame);
			String type = ".r.input";
			if(data != null)
				p.setProperty(id + "." + frame + type, (int) data);
			for(String atype : additionalData.keySet()) {
				data = additionalData.get(atype).get(frame);
				if(data != null)
					p.setProperty(id + "." + frame + ".r." + atype, (int) data);
			}
		}
	}

	@Override
	public int max() {
		return max;
	}

	@Override
	public void readProperty(CustomProperties p, int id) {
		max = p.getProperty(id + ".r.max", 0);
		for(int frame = 0; frame < max(); frame++) {
			CustomProperties p2 = p.subProperties(id + "." + frame + ".r.");
			for(String type : p2.stringPropertyNames()) {
				if("input".equals(type))
					inputData.put(frame, p2.getProperty("input", 0));
				else {
					if(!additionalData.containsKey(type))
						additionalData.put(type, new TreeMap<Integer, Integer>());
					additionalData.get(type).put(frame, p2.getProperty(type, 0));
				}
			}
		}
	}

}
