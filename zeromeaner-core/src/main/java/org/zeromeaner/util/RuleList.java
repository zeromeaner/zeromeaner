package org.zeromeaner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.TreeMap;

import org.funcish.core.coll.ArrayFunctionalList;
import org.funcish.core.coll.FunctionalList;
import org.funcish.core.fn.Mappicator;
import org.funcish.core.impl.AbstractMappicator;
import org.zeromeaner.game.component.RuleOptions;

public class RuleList extends ArrayFunctionalList<RuleOptions> {

	public static RuleList getRules() {
		RuleList ret = new RuleList();
		for(String rule : Zeroflections.getRules()) {
			rule = rule.replaceAll("^org/zeromeaner/", "");
			ret.add(GeneralUtil.loadRule(rule));
		}
		return ret;
	}
	
	public static Mappicator<RuleOptions, String> RESOURCE_NAME = new AbstractMappicator<RuleOptions, String>(RuleOptions.class, String.class) {
		@Override
		public String map0(RuleOptions key, Integer index) throws Exception {
			return key.resourceName.replaceAll("^org/zeromeaner/", "");
		}
	};
	
	public static Mappicator<String, RuleOptions> FROM_RESOURCE = new AbstractMappicator<String, RuleOptions>(String.class, RuleOptions.class) {
		@Override
		public RuleOptions map0(String key, Integer index) throws Exception {
			return GeneralUtil.loadRule(key);
		}
	};
	
	public static Mappicator<RuleOptions, String> RULE_NAME = new AbstractMappicator<RuleOptions, String>(RuleOptions.class, String.class) {
		@Override
		public String map0(RuleOptions key, Integer index) throws Exception {
			return key.strRuleName;
		}
	};
	
	public RuleList() {
		super(RuleOptions.class);
	}
	
	public FunctionalList<String> getResourceNames() {
		return map(RESOURCE_NAME);
	}
	
	public FunctionalList<String> getNames() {
		return map(RULE_NAME);
	}
	
	public RuleOptions get(String resourceName) {
		return get(getResourceNames().indexOf(resourceName));
	}
	
	public RuleOptions getNamed(String name) {
		return get(getNames().indexOf(name));
	}
}
