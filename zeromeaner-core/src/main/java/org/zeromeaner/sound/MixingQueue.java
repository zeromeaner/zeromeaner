package org.zeromeaner.sound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.apache.log4j.Logger;

public class MixingQueue {
	private static final Logger log = Logger.getLogger(MixingQueue.class);

	protected AudioFormat format;
	protected List<SampleBuffer> active;
	protected long startTimeNanos;
	protected long positionSamples;

	public MixingQueue(AudioFormat format) {
		this.format = format;

		active = new ArrayList<>();

		startTimeNanos = System.nanoTime();
		positionSamples = 0;

		if(format.getEncoding().equals(Encoding.PCM_SIGNED) || format.getEncoding().equals(Encoding.PCM_UNSIGNED))
			;
		else
			throw new IllegalArgumentException();
	}

	public long getStartTimeNanos() {
		return startTimeNanos;
	}

	public long getPositionNanos() {
		double samplesPerSecond = format.getSampleRate() * format.getChannels();
		double samplesPerNano = samplesPerSecond / 1000000000L;
		return (long)(positionSamples / samplesPerNano);
	}

	public AudioFormat getFormat() {
		return format;
	}

	public void offer(SampleBuffer buffer) {
		synchronized(active) {
			if(!active.contains(buffer))
				active.add(buffer);
		}
	}
	
	public void writeUntil(SampleLineWriter writer, long stopNanos) {
		List<Integer> samples = new ArrayList<>();
		while(getStartTimeNanos() + getPositionNanos() < stopNanos) 
			samples.add(nextSample());
		writer.writeSample(samples);
	}

	public int nextSample() {
		long sample = 0;
		int count = 0;
		synchronized(active) {
			for(int i = 0; i < active.size(); i++) {

				SampleBuffer buffer = active.get(i);
				if(buffer.remainingSamples() == 0) {
					continue;
				}
				sample += buffer.nextSample();
				count++;

			}
		}
		positionSamples++;

		if(count == 0)
			return 0;
		
		return (int)(sample / count);
	}
}
