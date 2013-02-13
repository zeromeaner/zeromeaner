package org.zeromeaner.util;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zeromeaner.game.subsystem.mode.AbstractMode;
import org.zeromeaner.game.subsystem.mode.GameMode;

import com.robinkirkman.util.FilterMap.FilterMappingArrayList;
import com.robinkirkman.util.Filters;
import com.robinkirkman.util.Mapping;
import com.robinkirkman.util.Mappings;

public class ModeList<T extends GameMode> extends FilterMappingArrayList<Class<? extends T>> {
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
	
	public static final Mapping<Class<? extends GameMode>, String> MODE_NAME = new Mapping<Class<? extends GameMode>, String>() {
		@Override
		public String map(Class<? extends GameMode> obj) {
			return ((GameMode) Mappings.CLASS_NEWINSTANCE.map(obj)).getName();
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

	public <U extends GameMode> ModeList<U> filter(Class<U> clazz) {
		return new ModeList<U>(clazz, filter(Filters.isSubclass(clazz)).map(Mappings.asSubclass(clazz)));
	}
	
	public List<T> newInstances() {
		return map(Mappings.CLASS_NEWINSTANCE).map(Mappings.asInstance(clazz));
	}
}
