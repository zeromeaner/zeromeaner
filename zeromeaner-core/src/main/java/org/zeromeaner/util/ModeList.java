package org.zeromeaner.util;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.List;

import org.funcish.core.Mappings;
import org.funcish.core.Predicates;
import org.funcish.core.coll.ArrayFunctionalList;
import org.funcish.core.fn.Mappicator;
import org.funcish.core.impl.AbstractMappicator;
import org.zeromeaner.game.subsystem.mode.GameMode;


public class ModeList<T extends GameMode> extends ArrayFunctionalList<Class<? extends T>> {
	public static ModeList<GameMode> getModes() {
		ModeList<GameMode> ret = new ModeList<GameMode>(GameMode.class);
		try {
			BufferedReader r = new BufferedReader(ResourceInputStream.newReader("config/list/mode.lst"));
			for(String line = r.readLine(); line != null; line = r.readLine()) {
				ret.add(Class.forName(line).asSubclass(GameMode.class));
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		return ret;
	}
	
	public static final Mappicator<Class<? extends GameMode>, String> MODE_NAME = 
			new AbstractMappicator<Class<? extends GameMode>, String>((Class) Class.class, String.class) {
		@Override
		public String map0(Class<? extends GameMode> obj, Integer index) throws Exception {
			return obj.newInstance().getName();
		}
	};

	
	
	private Class<T> clazz;
	
	private ModeList(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	private ModeList(Class<T> clazz, Collection<? extends Class<? extends T>> c) {
		super(c);
		this.clazz = clazz;
	}

	public <U extends T> ModeList<U> accept(Class<U> clazz) {
		return filter(Predicates.classIsAssignableFrom(clazz)).map(Mappings.<T, U>classAsSubclass(clazz), new ModeList<U>(clazz));
	}
	
	public List<T> newInstances() {
		return map(Mappings.<T>classNewInstance(clazz));
	}
}
