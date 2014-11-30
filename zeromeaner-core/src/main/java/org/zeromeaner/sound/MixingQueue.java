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
	protected Set<SampleBuffer> active;
	protected long startTimeNanos;
	protected long positionSamples;

	public MixingQueue(AudioFormat format) {
		this.format = format;

		active = new HashSet<>();

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
			active.add(buffer);
		}
	}
	
	public void writeUntil(SampleLineWriter writer, long stopNanos) {
		while(getStartTimeNanos() + getPositionNanos() < stopNanos) 
			writer.writeSample(nextSample());
	}

	public int nextSample() {
		List<Integer> mix = new ArrayList<>();
		synchronized(active) {
			Iterator<SampleBuffer> ai = active.iterator();
			ai: while(ai.hasNext()) {
				SampleBuffer buffer = ai.next();
				int sample = 0;
				if(buffer.remainingSamples() == 0) {
					ai.remove();
					continue ai;
				}
				sample = buffer.nextSample();
				mix.add(sample);
			}
		}
		positionSamples++;

		if(mix.size() == 0)
			return 0;
		
		int sample = 0;
		for(int s : mix)
			sample += s / mix.size() ;
		return sample;
	}
}
