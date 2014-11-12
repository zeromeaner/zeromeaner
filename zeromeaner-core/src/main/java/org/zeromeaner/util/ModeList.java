package org.zeromeaner.util;

import java.util.Collection;

import org.funcish.core.Mappings;
import org.funcish.core.Predicates;
import org.funcish.core.coll.ArrayFunctionalList;
import org.funcish.core.coll.FunctionalList;
import org.funcish.core.fn.Mapper;
import org.funcish.core.fn.Predicate;
import org.funcish.core.fn.Predicator;
import org.funcish.core.impl.AbstractMapper;
import org.funcish.core.impl.AbstractPredicator;
import org.zeromeaner.game.subsystem.mode.AbstractNetMode;
import org.zeromeaner.game.subsystem.mode.GameMode;


public class ModeList<E extends GameMode> extends ArrayFunctionalList<E> {
	public static ModeList<GameMode> getModes() {
		ModeList<GameMode> ret = new ModeList<GameMode>(GameMode.class);
		ret.addAll(Mappings.classNewInstance(GameMode.class).map(Zeroflections.getModes()));
		return ret;
	}
	
	public static final Mapper<GameMode, String> MODE_NAME = new AbstractMapper<GameMode, String>(GameMode.class, String.class) {
		@Override	
		public String map0(GameMode obj, Integer index) throws Exception {
			return obj.getName();
		}
	};

	public static Predicator<GameMode> IS_NETPLAY = new AbstractPredicator<GameMode>(GameMode.class) {
		@Override
		public boolean test0(GameMode value, Integer index) throws Exception {
			return value instanceof AbstractNetMode;
		}
	};
	
	public static Predicator<GameMode> IS_SINGLEPLAY = new AbstractPredicator<GameMode>(GameMode.class) {
		@Override
		public boolean test0(GameMode value, Integer index) throws Exception {
			return !value.isNetplayMode();
		}
	};
	
	public static Predicator<GameMode> IS_VSMODE = new AbstractPredicator<GameMode>(GameMode.class) {
		@Override
		public boolean test0(GameMode value, Integer index) throws Exception {
			return value.isVSMode();
		}
	};
	
	private ModeList(Class<E> clazz) {
		super(clazz);
	}
	
	private ModeList(Class<E> clazz, Collection<? extends E> c) {
		super(clazz, c);
	}
	
	public ModeList<E> getIsNetplay(boolean isNetplay) {
		Predicator<GameMode> p = IS_NETPLAY;
		if(!isNetplay)
			p = IS_SINGLEPLAY;
		return p.filter(this, new ModeList<E>(e()));
	}
	
	public ModeList<E> getIsVs(boolean isVs) {
		Predicator<GameMode> p = IS_VSMODE;
		if(!isVs)
			p = Predicates.not(p);
		return p.filter(this, new ModeList<E>(e()));
	}
	
	public <U extends E> ModeList<U> get(Class<U> u) {
		ModeList<E> us = Predicates.classIsInstance(u).filter(this, new ModeList<E>(e()));
		return Mappings.classCast(e(), u).map(us, new ModeList<U>(u));
	}
	
	public int indexOfName(String name) {
		return names().indexOf(name);
	}
	
	public E get(String name) {
		if(indexOfName(name) == -1)
			return null;
		return get(indexOfName(name));
	}
	
	public FunctionalList<String> names() {
		return map(MODE_NAME);
	}
	
	@Override
	public ModeList<E> filter(Predicate<? super E> p) {
		return filter(p, new ModeList<E>(e()));
	}
}
