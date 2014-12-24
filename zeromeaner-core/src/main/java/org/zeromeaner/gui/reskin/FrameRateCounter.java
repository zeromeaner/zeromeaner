package org.zeromeaner.gui.reskin;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class FrameRateCounter {
	public class Frame implements Delayed {
		private long expiry;
		
		public Frame() {
			expiry = System.nanoTime() + TimeUnit.NANOSECONDS.convert(durationSeconds, TimeUnit.SECONDS);
		}
		
		public Frame(long expiry) {
			this.expiry = expiry;
		}
		
		@Override
		public int compareTo(Delayed o) {
			return ((Long) getDelay(TimeUnit.NANOSECONDS)).compareTo(o.getDelay(TimeUnit.NANOSECONDS));
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return expiry - System.nanoTime();
		}
	}
	
	private int durationSeconds;
	private DelayQueue<Frame> q = new DelayQueue<Frame>();
	
	public FrameRateCounter() {
		this(10);
	}
	
	public FrameRateCounter(int durationSeconds) {
		this.durationSeconds = durationSeconds;
	}
	
	public synchronized void set(int rate) {
		q.clear();
		long now = System.nanoTime();
		long delayNanos = TimeUnit.NANOSECONDS.convert(durationSeconds, TimeUnit.SECONDS);
		for(int i = 0; i < rate * durationSeconds; i++) {
			add(new Frame(now + delayNanos * i / (rate * durationSeconds)));
		}
	}
	
	public synchronized void add() {
		add(new Frame());
	}
	
	protected synchronized void add(Frame f) {
		while(q.poll() != null)
			;
		q.offer(f);
	}
	
	public synchronized int rate() {
		while(q.poll() != null)
			;
		return Math.round(q.size() / (float) durationSeconds);
	}
}