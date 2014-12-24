package org.zeromeaner.util;

import java.util.Collections;
import java.util.Comparator;

import org.funcish.core.coll.ArrayFunctionalList;
import org.funcish.core.coll.FunctionalList;
import org.funcish.core.fn.Mapper;
import org.funcish.core.impl.AbstractMapper;
import org.zeromeaner.game.component.RuleOptions;

public class RuleList extends ArrayFunctionalList<RuleOptions> {

	public static RuleList getRules() {
		RuleList ret = new RuleList();
		for(String rule : Zeroflections.getRules()) {
			rule = rule.replaceAll("^org/zeromeaner/", "");
			ret.add(GeneralUtil.loadRule(rule));
		}
		Collections.sort(ret, RULE_ORDER);
		return ret;
	}
	
	public static final Comparator<RuleOptions> RULE_ORDER = new Comparator<RuleOptions>() {
		@Override
		public int compare(RuleOptions o1, RuleOptions o2) {
			boolean c1 = o1.resourceName.matches(".*/Custom_[^/]+");
			boolean c2 = o2.resourceName.matches(".*/Custom_[^/]+");
			if(c1 && !c2)
				return 1;
			if(!c1 && c2)
				return -1;
			return o1.strRuleName.toUpperCase().compareTo(o2.strRuleName.toUpperCase());
		}
	};
	
	public static Mapper<RuleOptions, String> RESOURCE_NAME = new AbstractMapper<RuleOptions, String>(RuleOptions.class, String.class) {
		@Override
		public String map0(RuleOptions key, Integer index) throws Exception {
			return key.resourceName.replaceAll("^org/zeromeaner/", "");
		}
	};
	
	public static Mapper<String, RuleOptions> FROM_RESOURCE = new AbstractMapper<String, RuleOptions>(String.class, RuleOptions.class) {
		@Override
		public RuleOptions map0(String key, Integer index) throws Exception {
			return GeneralUtil.loadRule(key);
		}
	};
	
	public static Mapper<RuleOptions, String> RULE_NAME = new AbstractMapper<RuleOptions, String>(RuleOptions.class, String.class) {
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
