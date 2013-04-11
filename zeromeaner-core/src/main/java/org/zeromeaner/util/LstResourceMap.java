package org.zeromeaner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class LstResourceMap extends TreeMap<String, List<String>> {
	private static final Logger log = Logger.getLogger(LstResourceMap.class);
	
	private String resource;
	
	public LstResourceMap(String resource) {
		this.resource = resource;
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
	
	public void write() {
		try {
			PrintWriter pw = new PrintWriter(ResourceOutputStream.getStream(resource));
			try {
				for(String key : keySet()) {
					pw.println(":" + key);
					for(String val : get(key)) {
						pw.println(val);
					}
				}
			} finally {
				pw.close();
			}
		} catch(IOException ioe) {
			log.error("Unable to write resource stream for " + resource);
		}
	}
}
