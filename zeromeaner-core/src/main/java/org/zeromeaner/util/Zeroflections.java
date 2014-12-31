package org.zeromeaner.util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.funcish.core.fn.Predicator;
import org.funcish.core.fn.Sequence;
import org.funcish.core.util.Comparisons;
import org.funcish.core.util.Mappings;
import org.funcish.core.util.Predicates;
import org.funcish.core.util.Sequences;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.zeromeaner.game.randomizer.Randomizer;
import org.zeromeaner.game.subsystem.ai.AbstractAI;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.game.subsystem.mode.ModeTypes.ModeType;
import org.zeromeaner.game.subsystem.wallkick.Wallkick;


public class Zeroflections {
	private static final Pattern ALL = Pattern.compile(".*");
	private static final Pattern RULE = Pattern.compile("(org/zeromeaner/)?config/rule/.*\\.rul");
	private static Reflections classes = Reflections.collect();
	static {
		ServiceHookDispatcher<ZeroflectionsHook> hooks = new ServiceHookDispatcher<>(ZeroflectionsHook.class);
		hooks.dispatcher().reflect(classes);
		
	}
	
	private static List<String> list(String listName) {
		InputStream rsrc = Zeroflections.class.getClassLoader().getResourceAsStream("config/list/" + listName);
		if(rsrc == null)
			rsrc = Zeroflections.class.getClassLoader().getResourceAsStream("org/zeromeaner/config/list/" + listName);
		Sequence<String> lines = Sequences.lines(new InputStreamReader(rsrc));
		return Sequences.sequencer(String.class, lines).list();
	}
	
	public static Set<String> getResources(Pattern fullPattern) {
		TreeSet<String> ret = new TreeSet<>();
		for(CharSequence s : Predicates.patternFind(fullPattern).filter(classes.getResources(ALL)))
			ret.add(s.toString());
		return ret;
	}
	
	public static List<Class<? extends AbstractAI>> getAIs() {
		List<Class<? extends AbstractAI>> ret = new ArrayList<Class<? extends AbstractAI>>();
		Mappings.classForName(AbstractAI.class).map(list("ai.lst")).into(ret);
//		for(Class<? extends AbstractAI> c : classes.getSubTypesOf(AbstractAI.class)) {
//			if(Modifier.isAbstract(c.getModifiers()))
//				continue;
//			if(!ret.contains(c))
//				ret.add(c);
//		}
//		Comparator<Class<? extends AbstractAI>> nameOrder = new Comparator<Class<? extends AbstractAI>>() {
//			@Override
//			public int compare(Class<? extends AbstractAI> o1, Class<? extends AbstractAI> o2) {
//				AbstractAI a1;
//				AbstractAI a2;
//				try {
//					a1 = o1.newInstance();
//					a2 = o2.newInstance();
//				} catch(Exception e) {
//					throw new RuntimeException(e);
//				}
//				return String.CASE_INSENSITIVE_ORDER.compare(a1.getName(), a2.getName());
//			}
//		}; 
//		Collections.sort(ret, nameOrder);
		return ret;
	}
	
	public static List<Class<? extends GameMode>> getModes() {
		List<Class<? extends GameMode>> order = new ArrayList<Class<? extends GameMode>>();
		Mappings.classForName(GameMode.class).map(list("mode.lst")).into(order);
		
		List<Class<? extends GameMode>> ret = new ArrayList<Class<? extends GameMode>>();
		
		Predicator<Class<? extends GameMode>> p = ModeType.forbid(ModeType.HIDDEN);
		
		for(Class<? extends GameMode> c :  p.filter(classes.getSubTypesOf(GameMode.class))) {
			if(Modifier.isAbstract(c.getModifiers()))
				continue;
			ret.add(c);
		}
		
		Collections.sort(ret, Comparisons.indexOf(order));
		
		return ret;
	}
	
	public static Set<Class<? extends Randomizer>> getRandomizers() {
		return classes.getSubTypesOf(Randomizer.class);
	}
	
	public static Set<Class<? extends Wallkick>> getWallkicks() {
		return classes.getSubTypesOf(Wallkick.class);
	}

	public static Set<String> getRules() {
		return getResources(RULE);
	}

}
