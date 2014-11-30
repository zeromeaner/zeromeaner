package org.zeromeaner.sound;

public interface SampleSource {
	public long getStartTimeNanos();
	public long getPositionNanos();
	public int nextSample();
}
