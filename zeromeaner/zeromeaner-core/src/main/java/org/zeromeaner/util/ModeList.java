package org.zeromeaner.util;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.rkutil.core.Conditions;
import org.rkutil.core.Transform;
import org.rkutil.core.Transforms;
import org.rkutil.core.coll.ConditionTransformArrayList;
import org.zeromeaner.game.subsystem.mode.AbstractMode;
import org.zeromeaner.game.subsystem.mode.GameMode;


public class ModeList<T extends GameMode> extends ConditionTransformArrayList<Class<? extends T>> {
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
	
	public static final Transform<Class<? extends GameMode>, String> MODE_NAME = new Transform<Class<? extends GameMode>, String>() {
		@Override
		public String transform(Class<? extends GameMode> obj) {
			return (Transforms.<GameMode>newInstance().transform(obj)).getName();
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

	public <U extends GameMode> ModeList<U> accept(Class<U> clazz) {
		return new ModeList<U>(clazz, accept(Conditions.isAssignableFrom(clazz)).transform(Transforms.asSubclass(clazz)));
	}
	
	public List<T> newInstances() {
		return transform(Transforms.<T>newInstance());
	}
}
