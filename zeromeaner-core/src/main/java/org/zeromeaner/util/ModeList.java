package org.zeromeaner.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.zeromeaner.game.subsystem.mode.AbstractNetMode;
import org.zeromeaner.game.subsystem.mode.GameMode;


public class ModeList extends ArrayList<GameMode> {
	public static ModeList getModes() {
		ModeList ret = new ModeList();
		for(Class<? extends GameMode> c : Zeroflections.getModes())
			try {
				ret.add(c.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		return ret;
	}
	
	public static final Function<GameMode, String> MODE_NAME = ((m) -> m.getName());

	public static Predicate<GameMode> IS_NETPLAY = ((m) -> m.isNetplayMode());
	
	public static Predicate<GameMode> IS_SINGLEPLAY = ((m) -> !m.isNetplayMode());
	
	public static Predicate<GameMode> IS_VSMODE = ((m) -> m.isVSMode());
	
	public ModeList() {
	}
	
	public ModeList(Collection<? extends GameMode> c) {
		super(c);
	}
	
	public ModeList getIsNetplay(boolean isNetplay) {
		return filter((m) -> m.isNetplayMode() == isNetplay);
	}
	
	public ModeList getIsVs(boolean isVs) {
		return filter((m) -> m.isVSMode() == isVs);
	}
	
	public ModeList get(Class<? extends GameMode> u) {
		return filter((m) -> u.isInstance(m));
	}
	
	public int indexOfName(String name) {
		return names().indexOf(name);
	}
	
	public GameMode get(String name) {
		int idx = indexOfName(name);
		if(idx == -1)
			return null;
		return get(idx);
	}
	
	public List<String> names() {
		return map(MODE_NAME);
	}
	
	public <E> List<E> map(Function<? super GameMode, ? extends E> f) {
		List<E> ret = new ArrayList<>();
		for(GameMode m : this)
			ret.add(f.apply(m));
		return ret;
	}
	
	public ModeList filter(Predicate<? super GameMode> p) {
		ModeList ret = new ModeList();
		for(GameMode m : this)
			if(p.test(m))
				ret.add(m);
		return ret;
	}
}
