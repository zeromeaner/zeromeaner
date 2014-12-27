package org.zeromeaner.gui.reskin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class FrameRateCounter {
	private int durationSeconds;
	private Deque<Long> q = new ArrayDeque<>();
	
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
			add(now + delayNanos * i / (rate * durationSeconds));
		}
	}
	
	public synchronized void add() {
		add(System.nanoTime() + TimeUnit.NANOSECONDS.convert(durationSeconds, TimeUnit.SECONDS));
	}
	
	protected synchronized void add(long f) {
		while(q.peekFirst() != null && q.peekFirst() < System.nanoTime())
			q.pollFirst();
		q.offerLast(f);
	}
	
	public synchronized int rate() {
		while(q.peekFirst() != null && q.peekFirst() < System.nanoTime())
			q.pollFirst();
		return Math.round(q.size() / (float) durationSeconds);
	}
}