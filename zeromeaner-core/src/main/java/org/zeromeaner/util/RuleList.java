package org.zeromeaner.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.subsystem.mode.GameMode;

public class RuleList extends ArrayList<RuleOptions> {

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
	
	public static Function<RuleOptions, String> RESOURCE_NAME = ((r) -> r.resourceName.replaceAll("^org/zeromeaner/", ""));
	
	public static Function<String, RuleOptions> FROM_RESOURCE = ((r) -> GeneralUtil.loadRule(r));
	
	public static Function<RuleOptions, String> RULE_NAME = ((r) -> r.strRuleName);
	
	public RuleList() {
	}
	
	public List<String> getResourceNames() {
		return map(RESOURCE_NAME);
	}
	
	public List<String> getNames() {
		return map(RULE_NAME);
	}
	
	public RuleOptions get(String resourceName) {
		return get(getResourceNames().indexOf(resourceName));
	}
	
	public RuleOptions getNamed(String name) {
		return get(getNames().indexOf(name));
	}

	public <E> List<E> map(Function<? super RuleOptions, ? extends E> f) {
		List<E> ret = new ArrayList<>();
		for(RuleOptions r : this)
			ret.add(f.apply(r));
		return ret;
	}
	
	public RuleList filter(Predicate<? super RuleOptions> p) {
		RuleList ret = new RuleList();
		for(RuleOptions r : this)
			if(p.test(r))
				ret.add(r);
		return ret;
	}
}
