package org.zeromeaner.game.evil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class TNProperties {
	private static final File PROPERTIES_FILE = new File(System.getProperty("user.home"), ".tetrevil_nullpo.ini");
	private static final Properties properties = new Properties();

	private static final String NULLPO_DIR_KEY = "nullpo.directory";
	private static final String RUN_EXTERNAL_KEY = "tnmain.run_external";
	private static final String DEVEL_KEY = "tnmain.devel";
	
	public static Properties getProperties() {
		return properties;
	}
	
	public static void args(String... args) {
		for(String arg : args) {
			if(arg.startsWith("-D")) {
				String[] a = arg.substring(2).split("=", 2);
				set(a[0], a[1]);
			}
		}
	}
	
	public static void load() {
		try {
			FileInputStream fin = new FileInputStream(PROPERTIES_FILE);
			try {
				properties.load(fin);
			} finally {
				fin.close();
			}
		} catch(FileNotFoundException fnfe) {
			System.out.println(PROPERTIES_FILE + " not found");
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public static void store() {
		if(isDevel())
			return;
		try {
			FileOutputStream fout = new FileOutputStream(PROPERTIES_FILE);
			try {
				properties.store(fout, "TETREVIL Nullpo Properties");
			} finally {
				fout.close();
			}
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public static Object set(String key, String value) {
		return properties.setProperty(key, value);
	}

	public static boolean contains(Object key) {
		return properties.containsKey(key);
	}

	public static String get(String key) {
		return properties.getProperty(key);
	}

	public static String get(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
	
	public static File getNullpoDirectory() {
		return contains(NULLPO_DIR_KEY) ? new File(get(NULLPO_DIR_KEY)) : null;
	}
	
	public static void setNullpoDirectory(File nullpoDirectory) {
		set(NULLPO_DIR_KEY, nullpoDirectory.getAbsolutePath());
	}
	
	public static File getModeList() {
		return getNullpoDirectory() == null ? null : new File(getNullpoDirectory(), "config/list/mode.lst");
	}
	
	public static File getRandomizerList() {
		return getNullpoDirectory() == null ? null : new File(getNullpoDirectory(), "config/list/randomizer.lst");
	}
	
	public static File getRecommendedRules() {
		return getNullpoDirectory() == null ? null : new File(getNullpoDirectory(), "config/list/recommended_rules.lst");
	}
	
	public static File getNetlobbyMultimode() {
		return getNullpoDirectory() == null ? null : new File(getNullpoDirectory(), "config/list/netlobby_multimode.lst");
	}
	
	public static boolean isRunExternal() {
		return Boolean.parseBoolean(get(RUN_EXTERNAL_KEY, "true"));
	}
	
	public static void setRunExternal(boolean runExternal) {
		set(RUN_EXTERNAL_KEY, "" + runExternal);
	}
	
	public static boolean isDevel() {
		return Boolean.parseBoolean(get(DEVEL_KEY, "false"));
	}
}
