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
	
	private RuleList() {
		super(RuleOptions.class);
	}
	
	public FunctionalList<String> getResourceNames() {
		return map(RESOURCE_NAME);
	}
}
