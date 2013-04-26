package org.zeromeaner.gui.applet;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventListener;
import java.util.EventObject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.gui.applet.FrameRateCounter.Frame;
import org.zeromeaner.util.Threads;

public class GameDriver implements KeyListener, Callable<Integer> {

	public static class GameDriverEvent extends EventObject {
		private int visibleFrameRate;
		private int totalFrameRate;
		
		public GameDriverEvent(GameDriver source) {
			super(source);
			visibleFrameRate = source.visibleFrames.rate();
			totalFrameRate = source.totalFrames.rate();
		}
		
		@Override
		public GameDriver getSource() {
			return (GameDriver) super.getSource();
		}
		
		public int getVisibleFrameRate() {
			return visibleFrameRate;
		}
		
		public int getTotalFrameRate() {
			return totalFrameRate;
		}
	}
	
	public static interface GameDriverListener extends EventListener {
		public void frameRendered(GameDriverEvent e);
		public void frameSkipped(GameDriverEvent e);
	}
	
	private static final Logger log = Logger.getLogger(GameDriver.class);
	
	private ScheduledExecutorService ticker = 
			Executors.newSingleThreadScheduledExecutor(Threads.namedFactory("game driver"));
	private ExecutorService worker = new ThreadPoolExecutor(
			1, 1, 
			1, TimeUnit.MINUTES, 
			new SynchronousQueue<Runnable>(), 
			Threads.namedFactory("game worker"), 
			new ThreadPoolExecutor.DiscardPolicy());
	
	private FrameRateCounter visibleFrames = new FrameRateCounter();
	private FrameRateCounter totalFrames = new FrameRateCounter();
	
	private int targetRate = 60;

	private class FrameTask extends FutureTask<Integer> {
		public FrameTask() {
			super(GameDriver.this);
		}
		
		@Override
		public void run() {
			super.runAndReset();
		}
	}
	
	private FrameTask frameTask = new FrameTask();
	
	private Runnable driverTask = new Runnable() {
		@Override
		public void run() {
			worker.execute(frameTask);
		}
	};
	
	private Future<?> driverFuture;
	
	protected GameManager manager;

	protected EventListenerList listenerList = new EventListenerList();
	
	public GameDriver(GameManager manager) {
		this.manager = manager;
	}
	
	public GameManager getManager() {
		return manager;
	}
	
	public void addGameDriverListener(GameDriverListener l) {
		listenerList.add(GameDriverListener.class, l);
	}
	
	public void removeGameDriverListener(GameDriverListener l) {
		listenerList.remove(GameDriverListener.class, l);
	}
	
	public synchronized void start() {
		driverFuture = ticker.scheduleAtFixedRate(
				driverTask, 
				0, 
				TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS) / targetRate, 
				TimeUnit.NANOSECONDS);
	}

	public synchronized void stop() {
		driverFuture.cancel(false);
		driverFuture = null;
	}
	
	@Override
	public Integer call() throws Exception {
		int freq;
		if((freq = totalFrames.rate()) > targetRate)
			return freq;

		tick(true);
		Frame frame = new Frame();
		totalFrames.add(frame);
		visibleFrames.add(frame);
		while((freq = totalFrames.rate()) < targetRate) {
			tick(false);
			totalFrames.add(new Frame());
		}
		return freq;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		setButtonPressedState(e.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setButtonPressedState(e.getKeyCode(), false);
	}
	
	private void setButtonPressedState(int keyCode, boolean pressed) {
		for(int playerID = 0; playerID < GameKeyApplet.gamekey.length; playerID++) {
			boolean inGame = manager.engine[playerID].isInGame;
			int[] kmap = inGame ? GameKeyApplet.gamekey[playerID].keymap : GameKeyApplet.gamekey[playerID].keymapNav;

			for(int i = 0; i < GameKeyApplet.MAX_BUTTON; i++) {
				if(keyCode == kmap[i]) {
					//log.debug("KeyCode:" + keyCode + " pressed:" + pressed + " button:" + i);
					GameKeyApplet.gamekey[playerID].setPressState(i, pressed);
				}
			}
		}
	}
	
	protected void fireFrameRendered() {
		Object[] ll = listenerList.getListenerList();
		GameDriverEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == GameDriverListener.class) {
				if(e == null)
					e = new GameDriverEvent(this);
				((GameDriverListener) ll[i+1]).frameRendered(e);
			}
		}
	}
	
	protected void fireFrameSkipped() {
		Object[] ll = listenerList.getListenerList();
		GameDriverEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == GameDriverListener.class) {
				if(e == null)
					e = new GameDriverEvent(this);
				((GameDriverListener) ll[i+1]).frameSkipped(e);
			}
		}
	}
	
	private synchronized void tick(boolean render) {
		update();
		if(render) {
			render();
			fireFrameRendered();
		} else
			fireFrameSkipped();
	}
	
	protected void update() {
		
	}
	
	protected void render() {
		
	}
}
