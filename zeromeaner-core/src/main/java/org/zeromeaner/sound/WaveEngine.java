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
package org.zeromeaner.sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
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

	private Map<AudioFormat, AudioThread> threads = new ConcurrentHashMap<>();
	
	private Map<String, SampleBuffer> buffers = new HashMap<>();

	/** Volume */
	private double volume = 1.0;

	/**
	 * Constructor
	 */
	public WaveEngine() {
		
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

		synchronized(threads) {
			for(AudioThread t : threads.values())
				setVolume(t.line);
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
			AudioInputStream in = AudioSystem.getAudioInputStream(StandaloneResourceHolder.getURL(filename).openStream());
			byte[] b = new byte[8192];
			for(int r = in.read(b); r != -1; r = in.read(b))
				out.write(b, 0, r);
			in.close();
			buffers.put(name, new SampleBuffer(in.getFormat(), ByteBuffer.wrap(out.toByteArray())));
		} catch(Exception e) {
			log.warn(e);
		}
	}


	/**
	 * Playback
	 * @param name Registered name
	 */
	public void play(final String name) {
		if(buffers.get(name) == null)
			return;
		
		AudioFormat format = buffers.get(name).getFormat();
		
		try {
			AudioThread match = null;
			synchronized(threads) {
				for(AudioThread t : threads.values()) {
					if(format.matches(t.format))
						match = t;
				}
				if(match == null) {
					SourceDataLine line = AudioSystem.getSourceDataLine(format);
					line.open(format);
					line.start();
					setVolume(line);
					match = new AudioThread(format, line);
					threads.put(format, match);
					match.start();
				}
			}

			match.offer(buffers.get(name).reset());
		} catch(Exception ex) {
		}
	}
	
	private class AudioThread extends Thread {
		protected AudioFormat format;
		protected SourceDataLine line;
		
		protected MixingQueue mixer;
		protected SampleLineWriter writer;
		
		public AudioThread(AudioFormat format, SourceDataLine line) {
			this.format = format;
			this.line = line;
			
			this.mixer = new MixingQueue(format);
			this.writer = new SampleLineWriter(format, line);
			
			setName(format.toString());
		}
		
		public void offer(SampleBuffer buffer) {
			mixer.offer(buffer);
		}
		
		@Override
		public void run() {
			long fillNanos = TimeUnit.NANOSECONDS.convert(10, TimeUnit.MILLISECONDS);
			try {
				while(true) {
					long until = mixer.getStartTimeNanos() + mixer.getPositionNanos() + fillNanos;
					mixer.writeUntil(writer, until);
					Thread.sleep(1);
				}
			} catch(Throwable e) {
				log.warn(e);
			}
		}
	}
	
}
