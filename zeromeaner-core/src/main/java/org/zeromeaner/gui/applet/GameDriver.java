package org.zeromeaner.gui.applet;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.gui.applet.FrameRateCounter.Frame;
import org.zeromeaner.util.Threads;

public class GameDriver implements KeyListener, Callable<Integer> {
	public static interface GameDriverView {
		public void renderGame(GameDriver driver);
		public void addKeyListener(KeyListener l);
		public void removeKeyListener(KeyListener l);
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
	protected GameDriverView view;
	
	public GameDriver(GameManager manager, GameDriverView view) {
		this.manager = manager;
		this.view = view;
	}
	
	public GameDriverView getView() {
		return view;
	}
	
	public void setView(GameDriverView view) {
		if(this.view != null)
			this.view.removeKeyListener(this);
		this.view = view;
		if(this.view != null)
			this.view.addKeyListener(this);
	}
	
	public GameManager getManager() {
		return manager;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
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
	
	private synchronized void tick(boolean render) {
		update();
		if(render && view != null) {
			view.renderGame(this);
			render();
		}
	}
	
	protected void update() {
		
	}
	
	protected void render() {
		
	}
}
