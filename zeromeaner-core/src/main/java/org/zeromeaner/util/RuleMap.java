package org.zeromeaner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.zeromeaner.game.component.RuleOptions;

public class RuleMap extends TreeMap<String, RuleOptions> {

	public static RuleMap getRules() {
		RuleMap ret = new RuleMap();
		try {
			BufferedReader r = new BufferedReader(ResourceInputStream.newReader("config/rule/list.txt"));
			for(String line = r.readLine(); line != null; line = r.readLine()) {
				ret.put(line, GeneralUtil.loadRule("config/rule/" + line));
			}
			r.close();
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
		return ret;
	}
	
	private RuleMap() {
	}
}
