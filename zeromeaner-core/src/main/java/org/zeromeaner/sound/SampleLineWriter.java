package org.zeromeaner.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat.Encoding;

public class SampleLineWriter {

	protected AudioFormat format;
	protected SourceDataLine line;
	
	protected byte[] buf;
	
	public SampleLineWriter(AudioFormat format, SourceDataLine line) {
		this.format = format;
		this.line = line;
		
		buf = new byte[sampleBytes()];
		
		if(format.getEncoding().equals(Encoding.PCM_SIGNED) || format.getEncoding().equals(Encoding.PCM_UNSIGNED))
			;
		else
			throw new IllegalArgumentException();
	}
	
	public int sampleBytes() {
		return format.getSampleSizeInBits() >>> 3;
	}
	
	public void writeSample(int sample) {
		int sampleBytes = sampleBytes();
		long s = sample;
		if(format.getEncoding().equals(Encoding.PCM_UNSIGNED))
			s += Integer.MAX_VALUE;
		if(format.isBigEndian()) {
			for(int i = 0; i < sampleBytes; i++) {
				buf[i] = (byte)(s >>> (24 - 8 * i));
			}
		} else {
			long t = s >>> ((4 - sampleBytes) * 8);
			for(int i = 0; i < sampleBytes; i++) {
				buf[i] = (byte)(t >>> (8 * i));
			}
		}
		line.write(buf, 0, buf.length);
	}

}
