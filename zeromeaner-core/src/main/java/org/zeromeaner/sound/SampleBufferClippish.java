package org.zeromeaner.sound;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.io.IOUtils;
import org.zeromeaner.gui.reskin.StandaloneResourceHolder;

public class SampleBufferClippish extends Thread {
	
	private static final SampleBuffer silence;
	static {
		try {
			AudioInputStream ain = AudioSystem.getAudioInputStream(StandaloneResourceHolder.getURL("res/se/silence.wav"));
			ByteBuffer bytes = ByteBuffer.wrap(IOUtils.toByteArray(ain));
			silence = new SampleBuffer(ain.getFormat(), bytes);
			ain.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected AudioFormat format;
	protected SourceDataLine line;
	protected SampleBuffer sample;
	
	protected byte[] sbuf;
	
	protected Runnable emptiedTask;
	
	protected long writtenSamples;
	protected long sampleOffset;
	
	protected boolean flushing;
	
	public SampleBufferClippish(AudioFormat format, SourceDataLine line) {
		this.format = format;
		this.line = line;
		sbuf = new byte[22050];
		setSample(silence, false);
	}
	
	@Override
	public void run() {
		sampleOffset = line.getLongFramePosition();
		try {
			while(true) {
				if(sample.remainingSamples() == 0 && line.getLongFramePosition() - sampleOffset >= writtenSamples - format.getSampleRate() / 100) {
					if(sample != silence && emptiedTask != null	)
						emptiedTask.run();
					setSample(silence, false);
				}
				int count = Math.min(sample.getBytes().remaining(), sbuf.length);
				sample.getBytes().get(sbuf, 0, count);
				if(flushing)
					line.flush();
				line.write(sbuf, 0, count);
				flushing = false;
				try {
					Thread.sleep(Math.max(0, (long)(count * 1000L / format.getSampleRate()) - 50));
				} catch (InterruptedException e) {
				}
			}
		} catch(RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void setSample(SampleBuffer sample, boolean flush) {
		this.sample = new SampleBuffer(sample.getFormat(), sample.getBytes().duplicate());
		flushing = flush;
		writtenSamples = this.sample.getBytes().remaining();
		sampleOffset = line.getLongFramePosition();
		interrupt();
	}
	
	public void setEmptiedTask(Runnable emptiedTask) {
		this.emptiedTask = emptiedTask;
	}
	
	public SourceDataLine getLine() {
		return line;
	}
}
