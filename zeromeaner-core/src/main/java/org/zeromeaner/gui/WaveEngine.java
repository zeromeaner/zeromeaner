/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
 * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
 */
package org.zeromeaner.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import org.apache.log4j.Logger;
import org.zeromeaner.gui.reskin.StandaloneResourceHolder;

/**
 * Sound engine
 * <a href="http://javagame.skr.jp/index.php?%A5%B5%A5%A6%A5%F3%A5%C9%A5%A8%A5%F3%A5%B8%A5%F3">Reprint yuan</a>
 */
public class WaveEngine {
	/** Log */
	private static Logger log = Logger.getLogger(WaveEngine.class);

	private ExecutorService exec = Executors.newCachedThreadPool(new ThreadFactory() {
		private ThreadFactory dtf = Executors.defaultThreadFactory();
		@Override
		public Thread newThread(Runnable r) {
			Thread t = dtf.newThread(r);
			t.setName("audio thread: " + t.getName());
			return t;
		}
	});

	/** You can registerWAVE file OfMaximumcount */
	private int maxClips;

	private Map<String, SourceDataLine> sourceDataLines;

	private Map<String, byte[]> clipBuffers = new HashMap<String, byte[]>();

	/** Was registeredWAVE file count */
	private AtomicInteger counter = new AtomicInteger();

	/** Volume */
	private double volume = 1.0;

	/**
	 * Constructor
	 */
	public WaveEngine() {
		this(30);
	}

	/**
	 * Constructor
	 * @param maxClips You can registerWAVE file OfMaximumcount
	 */
	public WaveEngine(int maxClips) {
		this.maxClips = maxClips;
		sourceDataLines = new HashMap<String, SourceDataLine>();
		sourceDataLines = Collections.synchronizedMap(sourceDataLines);
	}

	/**
	 * Current Get the volume setting
	 * @return Current Volume setting (1.0The default )
	 */
	public double getVolume() {
		return volume;
	}

	/**
	 * Set the volume
	 * @param vol New configuration volume (1.0The default )
	 */
	public void setVolume(double vol) {
		volume = vol;

		synchronized(sourceDataLines) {
			for(SourceDataLine line: sourceDataLines.values()) {
				setVolume(line);
			}
		}
	}
	
	private void setVolume(SourceDataLine line) {
		try {
			FloatControl ctrl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
			ctrl.setValue((float)Math.log10(volume) * 20);
		} catch (Exception e) {}
	}

	/**
	 * WAVE file Read
	 * @param name Registered name
	 * @param filename Filename
	 */
	public void load(String name, String filename) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = StandaloneResourceHolder.getURL(filename).openStream();
			byte[] b = new byte[8192];
			for(int r = in.read(b); r != -1; r = in.read(b))
				out.write(b, 0, r);
			in.close();
			clipBuffers.put(name, out.toByteArray());
		} catch(IOException ioe) {
			log.warn(ioe);
		}
	}


	/**
	 * Playback
	 * @param name Registered name
	 */
	public void play(final String name) {
		stop(name);
		AudioInputStream audioIn;
		try {
			audioIn = AudioSystem.getAudioInputStream(new ByteArrayInputStream(clipBuffers.get(name)));
		} catch(Exception ex) {
			log.warn(ex);
			return;
		}
		exec.submit(new AudioTask(name, audioIn));
	}
	
	private class AudioTask implements Callable<Object> {
		private String name;
		private AudioInputStream audioIn;
		
		public AudioTask(String name, AudioInputStream audioIn) {
			this.name = name;
			this.audioIn = audioIn;
		}
		
		@Override
		public Object call() throws Exception {
			SourceDataLine line;
			synchronized(sourceDataLines) {
				line = AudioSystem.getSourceDataLine(audioIn.getFormat());
				line.open(audioIn.getFormat());
				line.start();
				sourceDataLines.put(name, line);
			}
			setVolume(line);
			byte[] buf = new byte[1024 * audioIn.getFormat().getFrameSize()];
			for(int r = audioIn.read(buf); r != -1; r = audioIn.read(buf)) {
				line.write(buf, 0, r);
			}
			line.drain();
			line.close();
			return null;
		}
	}

	/**
	 * Stop
	 * @param name Registered name
	 */
	public void stop(final String name) {
		SourceDataLine line = sourceDataLines.remove(name);
		if(line != null) {
			line.flush();
			line.close();
		}
	}
}
