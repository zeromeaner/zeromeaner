package org.zeromeaner.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.funcish.core.coll.ArrayFunctionalList;
import org.funcish.core.coll.FunctionalList;
import org.funcish.core.fn.Mapper;
import org.funcish.core.impl.AbstractMapper;

public class MusicList extends ArrayFunctionalList<String> {
	private static final Logger log = Logger.getLogger(MusicList.class);
	
	private static MusicList instance;
	public static MusicList getInstance() {
		if(instance == null)
			instance = new MusicList();
		return instance;
	}
	
	public static Mapper<String, String> FILE_ONLY = new AbstractMapper<String, String>(String.class, String.class) {
		@Override
		public String map0(String key, Integer index) throws Exception {
			String fn = key.substring(key.lastIndexOf('/') + 1);
			fn = fn.substring(3);
			fn = fn.substring(0, fn.length() - 4);
			return fn;
		}
	};
	
	private int selection = -1; // -1 == RANDOM
	private float volume = 1f;
	
	private MusicList() {
		super(String.class);
		List<String> rsrc = new ArrayList<String>(Zeroflections.getResources(Pattern.compile("/bgm/.*\\.mp3$")));
		log.debug(rsrc.size() + " background musics found");
		for(int i = 0; i < rsrc.size(); i++) {
			add(rsrc.get(i));
		}
	}
	
	public FunctionalList<String> filesOnly() {
		return map(FILE_ONLY);
	}
	
	public boolean isPlaying() {
		return false;
	}
	
	public void play() {
		if(isPlaying() || size() == 0)
			return;
	}
	
	public void stop() {
	}
	
	public float getVolume() {
		return volume;
	}
	
	public void setVolume(float volume) {
		if(this.volume == 0 && volume > 0)
			play();
		if(this.volume > 0 && volume == 0)
			stop();
		this.volume = volume;
		updateVolume();
	}
	
	private void updateVolume() {
	}

	public int getSelection() {
		return selection;
	}

	public void setSelection(int selection) {
		int old = this.selection;
		this.selection = selection;
		if(old != selection && isPlaying()) {
			stop();
			play();
		}
	}
}
