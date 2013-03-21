package org.zeromeaner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class LstResourceMap extends TreeMap<String, List<String>> {
	public LstResourceMap(String resource) {
		try {
			BufferedReader r = new BufferedReader(ResourceInputStream.newReader(resource));
			String key = "";
			put("", new ArrayList<String>());
			for(String line = r.readLine(); line != null; line = r.readLine()) {
				line = line.replaceAll("#.*", "");
				if(line.matches("\\s*"))
					continue;
				if(line.startsWith(":")) {
					key = line.substring(1);
					if(!containsKey(key))
						put(key, new ArrayList<String>());
					continue;
				}
				get(key).add(line);
			}
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}
