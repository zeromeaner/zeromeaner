package org.zeromeaner.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.apache.log4j.Logger;
import org.funcish.core.coll.ArrayFunctionalList;
import org.funcish.core.coll.FunctionalList;
import org.funcish.core.fn.Mapper;
import org.funcish.core.impl.AbstractMapper;
import org.zeromeaner.gui.reskin.StandaloneApplet;

import com.googlecode.sardine.Factory;
import com.googlecode.sardine.Sardine;

public class MusicList extends ArrayFunctionalList<String> {
	private static final Logger log = Logger.getLogger(MusicList.class);
	
	private static Sardine s;
	static {
		try {
			s = new Factory().begin("zero", "zero");
		} catch(IOException ioe) {
		}
	}
	
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
	private JavaSoundAudioDevice audio;
	private AdvancedPlayer player;
	
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
		return player != null;
	}
	
	public void play() {
		if(isPlaying() || size() == 0)
			return;
		int m;
		if(selection < 0)
			m = (int) (Math.random() * size());
		else
			m = selection % size();
		log.debug("Playing:" + get(m));
		try {
			InputStream in = MusicList.class.getClassLoader().getResourceAsStream(get(m));
			if(in == null) {
				try {
					in = s.getInputStream("http://" + StandaloneApplet.url.getHost() + "/webdav/bgm/" + get(m));
				} catch(IOException ioe) {
				}
			}
			player = new AdvancedPlayer(in, audio = new JavaSoundAudioDevice() {
				@Override
				protected void createSource() throws JavaLayerException {
					super.createSource();
					updateVolume();
				}
			});
			player.setPlayBackListener(new PlaybackListener() {
				@Override
				public void playbackFinished(PlaybackEvent evt) {
					MusicList.getInstance().stop();
					MusicList.getInstance().play();
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
