package org.zeromeaner.sound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.apache.log4j.Logger;

public class MixingQueue implements SampleSource {
	private static final Logger log = Logger.getLogger(MixingQueue.class);

	protected AudioFormat format;
	protected PriorityQueue<SampleBuffer> buffers;
	protected List<SampleBuffer> active;
	protected long startTimeNanos;
	protected long positionSamples;
	
	public MixingQueue(AudioFormat format) {
		this.format = format;
	
		buffers = new PriorityQueue<>();
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
		return positionSamples * 1000000000L / ((int)format.getSampleRate() * format.getChannels());
	}
	
	public AudioFormat getFormat() {
		return format;
	}
	
	public void offer(SampleBuffer buffer) {
		synchronized(buffers) {
			buffers.offer(buffer);
		}
	}

	public void writeUntil(SampleLineWriter writer, long stopNanos) {
		while(getStartTimeNanos() + getPositionNanos() < stopNanos) 
			writer.writeSample(nextSample());
	}
	
	public int nextSample() {
		long now = startTimeNanos + getPositionNanos();

		synchronized(buffers) {
			while(buffers.peek() != null && buffers.peek().getStartTimeNanos() <= now) {
				active.add(buffers.poll());
			}
		}
		
		List<Integer> mix = new ArrayList<>();
		Iterator<SampleBuffer> ai = active.iterator();
		ai: while(ai.hasNext()) {
			SampleBuffer buffer = ai.next();
			int sample = 0;
			while(buffer.getStartTimeNanos() + buffer.getPositionNanos() <= now) {
				if(buffer.remainingSamples() == 0) {
					ai.remove();
					continue ai;
				}
				sample = buffer.nextSample();
			}
			mix.add(sample);
		}
		
		positionSamples++;
		
		int sample = 0;
		for(int s : mix)
			sample += s / mix.size();
		return sample;
	}
}
