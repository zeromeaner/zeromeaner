package org.zeromeaner.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.funcish.core.Comparisons;
import org.funcish.core.Mappings;
import org.funcish.core.Sequences;
import org.funcish.core.fn.Predicator;
import org.funcish.core.fn.Sequence;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.zeromeaner.contrib.net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;
import org.zeromeaner.game.subsystem.ai.AbstractAI;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.game.subsystem.mode.ModeTypes.ModeType;
import org.zeromeaner.game.subsystem.wallkick.Wallkick;

import static org.reflections.ReflectionUtils.*;
import static com.google.common.base.Predicates.*;

public class Zeroflections {
	private static Reflections classes = new Reflections("org.zeromeaner", new SubTypesScanner());
	private static Reflections config = new Reflections("org.zeromeaner.config", new ResourcesScanner());
	
	private static List<String> list(String listName) {
		InputStream rsrc = Zeroflections.class.getClassLoader().getResourceAsStream("org/zeromeaner/config/list/" + listName);
				Sequence<String> lines = Sequences.lines(new InputStreamReader(rsrc));
		return Sequences.sequencator(String.class, lines).list();
	}
	
	public static List<Class<? extends AbstractAI>> getAIs() {
		List<Class<? extends AbstractAI>> ret = new ArrayList<Class<? extends AbstractAI>>();
		ret.addAll(Mappings.classForName(AbstractAI.class).map(list("ai.lst")));
		for(Class<? extends AbstractAI> c : classes.getSubTypesOf(AbstractAI.class)) {
			if(Modifier.isAbstract(c.getModifiers()))
				continue;
			if(!ret.contains(c))
				ret.add(c);
		}
		return ret;
	}
	
	public static List<Class<? extends GameMode>> getModes() {
		List<Class<? extends GameMode>> order = new ArrayList<Class<? extends GameMode>>();
		order.addAll(Mappings.classForName(GameMode.class).map(list("mode.lst")));
		
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
		return config.getResources(Pattern.compile(".*\\.rul"));
	}

}
