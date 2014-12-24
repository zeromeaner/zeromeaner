package org.zeromeaner.gui.reskin;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class FrameRateCounter {
	public static class Frame implements Delayed {
		private long expiry = System.nanoTime() + TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);
		
		@Override
		public int compareTo(Delayed o) {
			return ((Long) getDelay(TimeUnit.NANOSECONDS)).compareTo(o.getDelay(TimeUnit.NANOSECONDS));
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return expiry - System.nanoTime();
		}
	}
	
	private DelayQueue<Frame> q = new DelayQueue<Frame>();
	
	public synchronized void add() {
		add(new Frame());
	}
	
	public synchronized void add(Frame f) {
		while(q.poll() != null)
			;
		q.offer(f);
	}
	
	public synchronized int rate() {
		while(q.poll() != null)
			;
		return q.size();
	}
}