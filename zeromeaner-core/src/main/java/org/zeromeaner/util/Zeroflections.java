package org.zeromeaner.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.zeromeaner.game.randomizer.Randomizer;
import org.zeromeaner.game.subsystem.ai.AbstractAI;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.game.subsystem.mode.ModeTypes.ModeType;
import org.zeromeaner.game.subsystem.wallkick.Wallkick;


public class Zeroflections {
	private static final Pattern ALL = Pattern.compile(".*");
	private static final Pattern RULE = Pattern.compile("(org/zeromeaner/)?config/rule/.*\\.rul");
	private static Reflections classes;
	
	public static Reflections getClasses() {
		if(classes == null) {
			ConfigurationBuilder config = ConfigurationBuilder.build("");
			
			Scanner s;
			config.getScanners().add(s = new ResourcesScanner());
			config.getScanners().add(new SubTypesScanner());
			
			ServiceHookDispatcher<ZeroflectionsHook> hooks = new ServiceHookDispatcher<>(ZeroflectionsHook.class);
			hooks.dispatcher().configure(config);
			classes = new Reflections(config);
			classes.collect(Zeroflections.class.getClassLoader().getResourceAsStream("META-INF/reflections/zeromeaner-core-reflections.xml"));
			hooks.dispatcher().reflect(classes);
		}
		return classes;
	}
	
	private static List<String> list(String listName) throws IOException {
		InputStream rsrc = Zeroflections.class.getClassLoader().getResourceAsStream("config/list/" + listName);
		if(rsrc == null)
			rsrc = Zeroflections.class.getClassLoader().getResourceAsStream("org/zeromeaner/config/list/" + listName);
		List<String> lines = new ArrayList<>();
		BufferedReader r = new BufferedReader(new InputStreamReader(rsrc));
		for(String line = r.readLine(); line != null; line = r.readLine())
			lines.add(line);
		return lines;
	}
	
	public static Set<String> getResources(Pattern fullPattern) {
		TreeSet<String> ret = new TreeSet<>();
		for(String s : getClasses().getResources(ALL))
			if(fullPattern.matcher(s).matches())
				ret.add(s);
		return ret;
	}
	
	public static List<Class<? extends AbstractAI>> getAIs() {
		try {
			List<Class<? extends AbstractAI>> ret = new ArrayList<>();
			for(String line : list("ai.lst"))
				ret.add(Class.forName(line).asSubclass(AbstractAI.class));
			return ret;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<Class<? extends GameMode>> getModes() {
		try {
			List<Class<? extends GameMode>> ret = new ArrayList<>();
			for(String line : list("mode.lst"))
				ret.add(Class.forName(line).asSubclass(GameMode.class));
			return ret;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<Class<? extends Randomizer>> getRandomizers() {
		try {
			List<Class<? extends Randomizer>> ret = new ArrayList<>();
			for(String line : list("randomizer.lst"))
				ret.add(Class.forName(line).asSubclass(Randomizer.class));
			return ret;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Set<Class<? extends Wallkick>> getWallkicks() {
		return getClasses().getSubTypesOf(Wallkick.class);
	}

	public static Set<String> getRules() {
		return getResources(RULE);
	}

}
