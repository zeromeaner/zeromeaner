package org.zeromeaner.util;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

public interface ZeroflectionsHook {
	public void configure(ConfigurationBuilder config);
	public void reflect(Reflections reflect);
}
