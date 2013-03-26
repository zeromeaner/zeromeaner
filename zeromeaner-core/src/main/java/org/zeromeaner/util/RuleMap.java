package org.zeromeaner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.TreeMap;

import org.zeromeaner.game.component.RuleOptions;

public class RuleMap extends TreeMap<String, RuleOptions> {

	public static RuleMap getRules() {
		RuleMap ret = new RuleMap();
		for(String rule : Zeroflections.getRules()) {
			ret.put(rule.substring(rule.lastIndexOf('/') + 1), GeneralUtil.loadRule(rule));
		}
		return ret;
	}
	
	private RuleMap() {
	}
}
