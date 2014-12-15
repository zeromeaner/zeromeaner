package org.zeromeaner.standalone;

import java.io.File;
import java.net.MalformedURLException;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.zeromeaner.util.ZeroflectionsHook;

public class StandaloneZeroflectionsHook implements ZeroflectionsHook {

	@Override
	public void reflect(Reflections classes) {
		Scanner s;
		classes.getConfiguration().getScanners().add(s = new ResourcesScanner());
		s.setStore(classes.getStore().getOrCreate(s.getClass().getSimpleName()));
		classes.getConfiguration().getScanners().add(new SubTypesScanner());
		s.setStore(classes.getStore().getOrCreate(s.getClass().getSimpleName()));
		try {
			classes.scan(new File(System.getProperty("user.dir"), "local-resources").toURI().toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

}
