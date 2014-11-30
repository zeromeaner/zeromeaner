package org.zeromeaner.sound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public class MixingQueue implements SampleSource {

	protected AudioFormat format;
	protected PriorityQueue<SampleBuffer> buffers;
	protected List<SampleBuffer> active;
	protected long startTimeNanos;
	protected long positionNanos;
	
	public MixingQueue(AudioFormat format) {
		this.format = format;
	
		buffers = new PriorityQueue<>();
		active = new ArrayList<>();
		
		startTimeNanos = System.nanoTime();
		positionNanos = 0;
		
		if(format.getEncoding().equals(Encoding.PCM_SIGNED) || format.getEncoding().equals(Encoding.PCM_UNSIGNED))
			;
		else
			throw new IllegalArgumentException();
	}
	
	public long getStartTimeNanos() {
		return startTimeNanos;
	}
	
	public long getPositionNanos() {
		return positionNanos;
	}
	
	public AudioFormat getFormat() {
		return format;
	}
	
	public void offer(SampleBuffer buffer) {
		buffers.offer(buffer);
	}

	public int nextSample() {
		long now = startTimeNanos + positionNanos;
		while(buffers.peek() != null && buffers.peek().getStartTimeNanos() <= now) {
			active.add(buffers.poll());
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
		
		positionNanos += (long)(1000000000L * format.getSampleRate());
		
		int sample = 0;
		for(int s : mix)
			sample += s / mix.size();
		return sample;
	}
}
