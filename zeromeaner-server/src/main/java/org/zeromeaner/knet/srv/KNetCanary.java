package org.zeromeaner.knet.srv;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetListener;

public class KNetCanary implements Runnable {
	private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

	private int port;

	public KNetCanary(int port) {
		this.port = port;
		exec.scheduleAtFixedRate(this, 10, 10, TimeUnit.MINUTES);
	}
	
	@Override
	public void run() {
		try {
			final KNetClient canary = new KNetClient("localhost", port);
			canary.addKNetListener(new KNetListener() {
				@Override
				public void knetEvented(KNetClient client, KNetEvent e) {
					canary.stop();
				}
			});
			canary.start();
		} catch(Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}
}
