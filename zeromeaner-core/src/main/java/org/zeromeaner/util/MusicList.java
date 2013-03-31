package org.zeromeaner.util;

import java.awt.EventQueue;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.JavaSoundAudioDeviceFactory;
import javazoom.jl.player.NullAudioDevice;
import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

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
	
	private float volume = 1f;
	private JavaSoundAudioDevice audio;
	private AdvancedPlayer player;
	
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
			player = new AdvancedPlayer(get(m).openStream(), audio = new JavaSoundAudioDevice() {
				@Override
				protected void createSource() throws JavaLayerException {
					super.createSource();
					updateVolume();
				}
			});
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
	
	public float getVolume() {
		return volume;
	}
	
	public void setVolume(float volume) {
		this.volume = volume;
		updateVolume();
	}
	
	private void updateVolume() {
		if(player == null)
			return;
		try {
			Field f = JavaSoundAudioDevice.class.getDeclaredField("source");
			f.setAccessible(true);
			SourceDataLine source = (SourceDataLine) f.get(audio);
			if(source == null)
				return;
			FloatControl volControl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
	        float newGain = Math.min(Math.max((float)Math.log10(volume) * 20, volControl.getMinimum()), volControl.getMaximum());

	        volControl.setValue(newGain);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
