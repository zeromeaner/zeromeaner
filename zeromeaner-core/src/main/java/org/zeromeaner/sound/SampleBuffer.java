package org.zeromeaner.sound;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public class SampleBuffer implements Comparable<SampleBuffer> {

	protected ByteBuffer bytes;
	protected AudioFormat format;

	protected long startTimeNanos;
	
	public SampleBuffer(AudioFormat format, ByteBuffer bytes, long startTimeNanos) {
		this.format = format;
		this.bytes = bytes;
		this.startTimeNanos = startTimeNanos;
		
		if(format.getEncoding().equals(Encoding.PCM_SIGNED) || format.getEncoding().equals(Encoding.PCM_UNSIGNED))
			;
		else
			throw new IllegalArgumentException();
	}

	public int sampleBytes() {
		return format.getSampleSizeInBits() >>> 3;
	}
	
	public int remainingSamples() {
		return bytes.remaining() / sampleBytes();
	}
	
	public int nextSample() {
		int sampleBytes = sampleBytes();
		long sample = 0;
		if(format.isBigEndian()) {
			for(int i = 0; i < sampleBytes; i++) {
				sample |= (0xffl & bytes.get()) << (24 - 8 * i);
			}
		} else {
			for(int i = 0; i < sampleBytes; i++) {
				sample |= (0xffl & bytes.get()) << (8 * i);
			}
			sample = sample << ((4 - sampleBytes) * 8);
		}
		if(format.getEncoding().equals(Encoding.PCM_SIGNED))
			return (int) sample;
		else 
			return (int)(sample - Integer.MAX_VALUE);
	}
	
	public long getStartTimeNanos() {
		return startTimeNanos;
	}
	
	@Override
	public int compareTo(SampleBuffer o) {
		return Long.compare(getStartTimeNanos(), o.getStartTimeNanos());
	}
}
