package org.zeromeaner.util;

import java.awt.EventQueue;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.JavaSoundAudioDeviceFactory;
import javazoom.jl.player.NullAudioDevice;
import javazoom.jl.player.Player;

import org.apache.log4j.Logger;
import org.zeromeaner.gui.applet.ResourceHolderApplet;

public class MusicList extends ArrayList<URL> {
	private static final Logger log = Logger.getLogger(MusicList.class);
	
	private static MusicList instance;
	public static MusicList getInstance() {
		if(instance == null)
			instance = new MusicList();
		return instance;
	}
	
	private Player player;
	
	private MusicList() {
		List<String> rsrc = new ArrayList<String>(Zeroflections.getResources(Pattern.compile("/bgm/.*\\.mp3$")));
		log.debug(rsrc.size() + " background musics found");
		for(int i = 0; i < rsrc.size(); i++) {
			add(ResourceHolderApplet.class.getClassLoader().getResource(rsrc.get(i)));
		}
	}
	
	public boolean isPlaying() {
		return player != null;
	}
	
	public void play() {
		if(isPlaying() || size() == 0)
			return;
		int m = (int) (Math.random() * size());
		log.debug("Playing:" + get(m));
		try {
			player = new Player(get(m).openStream(), new JavaSoundAudioDeviceFactory().createAudioDevice());
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						player.play();
					} catch(Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			}).start();
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void stop() {
		if(player != null) {
			player.close();
			player = null;
		}
	}
}
