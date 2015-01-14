package org.zeromeaner.standalone;

import java.io.File;
import java.net.MalformedURLException;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.zeromeaner.util.ZeroflectionsHook;

public class StandaloneZeroflectionsHook implements ZeroflectionsHook {

	@Override
	public void reflect(Reflections classes) {
		Scanner s;
//		classes.getConfiguration().getScanners().add(s = new ResourcesScanner());
//		s.setStore(classes.getStore().getOrCreate(s.getClass().getSimpleName()));
//		classes.getConfiguration().getScanners().add(new SubTypesScanner());
//		s.setStore(classes.getStore().getOrCreate(s.getClass().getSimpleName()));
		
	}

	@Override
	public void configure(ConfigurationBuilder config) {
		File userDir = new File(System.getProperty("user.dir"));
		try {
			System.out.println("Scanning:" + userDir.toURI().toURL());
			config.addUrls(userDir.toURI().toURL());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
