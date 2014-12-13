/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
 * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
 */
package org.zeromeaner.gui.reskin;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.apache.log4j.Logger;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.util.MusicList;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.ServiceHookDispatcher;

/**
 * Game screen frame
 */
public class StandaloneGamePanel extends JPanel implements Runnable {
	public static interface Hook {
		public void gameStarted(StandaloneGamePanel thiz);
		public void frameSynced(StandaloneGamePanel thiz);
		public void gameStopped(StandaloneGamePanel thiz);
	}
	
	protected static final ServiceHookDispatcher<Hook> hooks = new ServiceHookDispatcher<>(Hook.class);
	
	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/** Log */
	static Logger log = Logger.getLogger(StandaloneGamePanel.class);

	protected ScheduledExecutorService gexec = Executors.newScheduledThreadPool(1, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setName("game update thread");
			return t;
		}
	});
	
	public static class FocusableJLabel extends JLabel {
		public FocusableJLabel(Icon image) {
			super(image);
			setFocusable(true);
			setFocusCycleRoot(true);
			enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);
		}
	}

	public static class FramePerSecond implements Delayed {
		private long created = System.nanoTime();
		private long expiry = created + TimeUnit.NANOSECONDS.convert(4, TimeUnit.SECONDS);
		
		@Override
		public int compareTo(Delayed o) {
			return Long.compare(expiry, ((FramePerSecond) o).expiry);
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(expiry - System.nanoTime(), TimeUnit.NANOSECONDS);
		}
		
		public long getCreated() {
			return created;
		}
		
		public long getExpiry() {
			return expiry;
		}
	}
	
	private DelayQueue<FramePerSecond> rps = new DelayQueue<FramePerSecond>();
	private DelayQueue<FramePerSecond> fps = new DelayQueue<FramePerSecond>();
	private DelayQueue<FramePerSecond> dps = new DelayQueue<>();
	
	/** Parent window */
	protected StandaloneFrame owner = null;

	/** The size of the border and title bar */
	protected Insets insets = null;

	protected BufferedImage imageBuffer;

	protected JLabel imageBufferLabel;

	public BufferedImage gameBuffer;
	
	//	/** BufferStrategy */
	//	protected BufferStrategy bufferStrategy = null;

	/** trueThread moves between */
	public AtomicBoolean running = new AtomicBoolean(false);

	/** MaximumFPS (Setting) */
	public int maxfps;
	
	/** Current MaximumFPS */
	protected int maxfpsCurrent = 0;

	/** Current Pause time */
	protected long periodCurrent = 0;

	
	/** ActualFPS */
	public double renderedFPS = 0.0;
	
	public double drawnFPS = 0.0;

	public double totalFPS = 0;

	/** FPSDisplayDecimalFormat */
	public DecimalFormat df = new DecimalFormat("0");
	
	/** True if execute Toolkit.getDefaultToolkit().sync() at the end of each frame */
	public boolean syncDisplay = true;
	
	public boolean syncRender = true;

	/** Pause state */
	protected boolean pause = false;

	/** Pose hidden message */
	protected boolean pauseMessageHide = false;

	/** Pause menuOfCursor position */
	protected int cursor = 0;

	/** Number of frames remaining until pause key can be used */
	protected int pauseFrame = 0;

	/** Double speedMode */
	protected int fastforward = 0;

	/** ScreenshotCreating flag */
	protected boolean ssflag = false;

	/**  frame Step is enabled flag */
	protected boolean enableframestep = false;

	/** FPSDisplay */
	protected boolean showfps = true;
	
	/** Ingame flag */
	public boolean[] isInGame;

	/** If net playtrue */
	public boolean isNetPlay = false;

//	/** Mode name to enter (null=Exit) */
//	public volatile String strModeToEnter = "";
	public BlockingQueue<String> modeToEnter = new LinkedBlockingQueue<String>();
	public BlockingQueue<String> modesEntered = new LinkedBlockingQueue<String>();

	/** Previous ingame flag (Used by title-bar text change) */
	protected boolean prevInGameFlag = false;

	/** Current game mode name */
	public String modeName;
	
	/**
	 * Constructor
	 * @param owner Parent window
	 * @throws HeadlessException Keyboard, Mouse, Exceptions such as the display if there is no
	 */
	public StandaloneGamePanel(final StandaloneFrame owner) throws HeadlessException {
		super(new GridBagLayout());
		this.owner = owner;

		setDoubleBuffered(true);


		imageBuffer = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		gameBuffer = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);

		add(
				imageBufferLabel = new FocusableJLabel(new ImageIcon(imageBuffer)), 
				new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
		
		add(
				new JLabel("Press CTRL+ENTER to enter full-screen mode"),
				new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0), 0, 0));
		
		imageBufferLabel.setText("No Active Game.  Click \"Play\" to start.");
		imageBufferLabel.setIcon(null);
		
		maxfps = Options.standalone().MAX_FPS.value();

		log.debug("GameFrame created");

		setOpaque(true);	
		setFocusable(true);
		setFocusCycleRoot(true);
		setFocusTraversalKeysEnabled(false);
		
		addKeyListener(new FullScreenKeyListener(owner));
		addKeyListener(new GameFrameKeyEvent());

		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocus();
			}
		};
		addMouseListener(ml);
		imageBufferLabel.addMouseListener(ml);
		
	}

	/**
	 * Display the game window
	 */
	public void displayWindow() {

		imageBufferLabel.setText(null);
		imageBufferLabel.setIcon(new ImageIcon(imageBuffer));
		imageBufferLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		imageBufferLabel.revalidate();

		new Thread(this, "Game Thread").start();
	}

	public void shutdown() {
		shutdown(false);
	}
	
	/**
	 * End processing
	 */
	public void shutdown(boolean restart) {
		MusicList.getInstance().stop();
		if(isNetPlay) {
			// Reload global config (because it can change rules)
			StandaloneMain.loadGlobalConfig();
		}
		synchronized(running) {
			running.set(false);
			running.notifyAll();
		}
		
		
		imageBufferLabel.setText("No Active Game.  Click to start.");
		imageBufferLabel.setIcon(null);
		imageBufferLabel.setBorder(null);
		
		if(restart) {
			owner.startNewGame();
			displayWindow();
		}
	}
	
	public void shutdownWait() throws InterruptedException {
		synchronized(running) {
			while(running.get())
				running.wait();
		}
	}

	private void doFrame(boolean render) {
		if(isNetPlay) {
			gameUpdateNet();
			if(render)
				gameRenderNet();
		} else {
			gameUpdate();
			if(render)
				gameRender();
//		} else {
//			StandaloneGameKey.gamekey[0].clear();
//			StandaloneGameKey.gamekey[1].clear();
		}
		if(render)
			hooks.dispatcher().frameSynced(this);
	}
	
	protected double fpsOf(DelayQueue<FramePerSecond> q) {
		if(q.size() < 2)
			return 0;
		double durationNanos = System.nanoTime() - q.peek().getCreated();
		return q.size() / (durationNanos / 1000000000L);
	}
	
	protected void updateFPS() {
		while(fps.poll() != null)
			;
		while(rps.poll() != null)
			;
		while(dps.poll() != null)
			;
		totalFPS = fpsOf(fps); // Math.rint(fps.size() / 4.);
		renderedFPS = fpsOf(rps); // Math.rint(rps.size() / 4.);
		drawnFPS = fpsOf(dps); // Math.rint(dps.size() / 4.);
	}
	
	private long lastFrameNanos;
	
	/**
	 * Processing of the thread
	 */
	public void run() {
		boolean sleepFlag;
		long beforeTime, afterTime, timeDiff, sleepTime, sleepTimeInMillis;
		long overSleepTime = 0L;
		int noDelays = 0;

		// Initialization
		maxfpsCurrent = maxfps;
		periodCurrent = (long) (1.0 / maxfpsCurrent * 1000000000);
		pause = false;
		pauseMessageHide = false;
		fastforward = 0;
		cursor = 0;
		prevInGameFlag = false;
		isInGame = new boolean[2];
		StandaloneGameKey.gamekey[0].clear();
		StandaloneGameKey.gamekey[1].clear();
		updateTitleBarCaption();

		// Settings to take effect
		enableframestep = Options.standalone().ENABLE_FRAME_STEP.value();
		showfps = Options.standalone().SHOW_FPS.value();
		syncDisplay = Options.standalone().SYNC_DISPLAY.value();
		syncRender = Options.standalone().SYNC_RENDER.value();
		
		
		// Main loop
		log.debug("Game thread start");
		
		hooks.dispatcher().gameStarted(this);
		
		running.set(true);
		
		Runnable task = new Runnable() {
			private long nanosPerFrame = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS) / maxfps;
			
			@Override
			public void run() {
				try {
				if(lastFrameNanos + nanosPerFrame > System.nanoTime())
					return;
				while(lastFrameNanos + nanosPerFrame <= System.nanoTime() - nanosPerFrame) {
					fps.add(new FramePerSecond());
					doFrame(syncRender);
					lastFrameNanos += nanosPerFrame;
				}
				
				updateFPS();
				doFrame(true);
				fps.add(new FramePerSecond());
				lastFrameNanos += nanosPerFrame;
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}
		};
		
		lastFrameNanos = System.nanoTime();
		
		if(Options.standalone().INCREASE_EQ_PRIORITY.value()) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				}
			});
		}
		
		ScheduledFuture<?> f = gexec.scheduleAtFixedRate(
				task, 
				0, 
				TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS) / maxfps, 
				TimeUnit.NANOSECONDS);
		
		imageBufferLabel.getParent().requestFocus();
		
		while(running.get()) {
			synchronized(running) {
				try {
					running.wait();
				} catch(InterruptedException ie) {
				}
			}
		}
		
		f.cancel(false);

		
		hooks.dispatcher().gameStopped(this);
		
		if(owner.gameManager != null)
			owner.gameManager.shutdown();

		log.debug("Game thread end");
	}

	/**
	 * Update game state
	 */
	protected void gameUpdate() {
		if(owner.gameManager == null) return;

		// Set ingame flag
		for(int i = 0; i < 2; i++) {
			boolean prevInGame = isInGame[i];

			if((owner.gameManager.engine != null) && (owner.gameManager.engine.length > i)) {
				isInGame[i] = owner.gameManager.engine[i].isInGame;
			}
			if(pause && !enableframestep) {
				isInGame[i] = false;
			}

			if(prevInGame != isInGame[i]) {
				StandaloneGameKey.gamekey[i].clear();
				if(isInGame[i])
					MusicList.getInstance().play();
			}
		}

		StandaloneGameKey.gamekey[0].update();
		StandaloneGameKey.gamekey[1].update();

		// Title bar update
		if((owner.gameManager != null) && (owner.gameManager.engine != null) &&
				(owner.gameManager.engine.length > 0) && (owner.gameManager.engine[0] != null))
		{
			boolean nowInGame = owner.gameManager.engine[0].isInGame;
			if(prevInGameFlag != nowInGame) {
				prevInGameFlag = nowInGame;
				updateTitleBarCaption();
			}
		}

		// Pause button
		if(StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_PAUSE) || StandaloneGameKey.gamekey[1].isPushKey(StandaloneGameKey.BUTTON_PAUSE)) {
			if(!pause) {
				if((owner.gameManager != null) && (owner.gameManager.isGameActive()) && (pauseFrame <= 0)) {
					StandaloneResourceHolder.soundManager.play("pause");
					pause = true;
					if(!enableframestep) pauseFrame = 5;
					cursor = 0;
				}
			} else {
				StandaloneResourceHolder.soundManager.play("pause");
				pause = false;
				pauseFrame = 0;
			}
			updateTitleBarCaption();
		}
		// Pause menu
		if(pause && !enableframestep && !pauseMessageHide) {
			// Cursor movement
			if(StandaloneGameKey.gamekey[0].isMenuRepeatKey(StandaloneGameKey.BUTTON_UP)) {
				StandaloneResourceHolder.soundManager.play("cursor");
				cursor--;

				if(cursor < 0) {
					if(owner.gameManager.replayMode && !owner.gameManager.replayRerecord)
						cursor = 3;
					else
						cursor = 2;
				}
			}
			if(StandaloneGameKey.gamekey[0].isMenuRepeatKey(StandaloneGameKey.BUTTON_DOWN)) {
				StandaloneResourceHolder.soundManager.play("cursor");
				cursor++;
				if(cursor > 3) cursor = 0;

				if((!owner.gameManager.replayMode || owner.gameManager.replayRerecord) && (cursor > 2))
					cursor = 0;
			}

			// Confirm
			if(StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_A)) {
				StandaloneResourceHolder.soundManager.play("decide");
				if(cursor == 0) {
					// Resumption
					pause = false;
					pauseFrame = 0;
					StandaloneGameKey.gamekey[0].clear();
				} else if(cursor == 1) {
					// Retry
					pause = false;
					owner.gameManager.reset();
				} else if(cursor == 2) {
					// End
					shutdown(true);
					return;
				} else if(cursor == 3) {
					// Replay re-record
					owner.gameManager.replayRerecord = true;
					cursor = 0;
				}
				updateTitleBarCaption();
			}
			// Unpause by cancel key
			else if(StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_B) && (pauseFrame <= 0)) {
				StandaloneResourceHolder.soundManager.play("pause");
				pause = false;
				pauseFrame = 5;
				StandaloneGameKey.gamekey[0].clear();
				updateTitleBarCaption();
			}
		}
		if(pauseFrame > 0) pauseFrame--;

		// Hide pause menu
		pauseMessageHide = StandaloneGameKey.gamekey[0].isPressKey(StandaloneGameKey.BUTTON_C);

		if(owner.gameManager.replayMode && !owner.gameManager.replayRerecord && owner.gameManager.engine[0].gameActive) {
			// Replay speed
			if(StandaloneGameKey.gamekey[0].isMenuRepeatKey(StandaloneGameKey.BUTTON_LEFT)) {
				if(fastforward > 0) {
					fastforward--;
				}
			}
			if(StandaloneGameKey.gamekey[0].isMenuRepeatKey(StandaloneGameKey.BUTTON_RIGHT)) {
				if(fastforward < 98) {
					fastforward++;
				}
			}

			// Replay re-record
			if(StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_D)) {
				owner.gameManager.replayRerecord = true;
				cursor = 0;
			}
			// Show invisible blocks in replay
			if(StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_E)) {
				owner.gameManager.replayShowInvisible = !owner.gameManager.replayShowInvisible;
				cursor = 0;
			}
		} else {
			fastforward = 0;
		}

		// Execute game loops
		if(!pause || (StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_FRAMESTEP) && enableframestep)) {
			if(owner.gameManager != null) {
				for(int i = 0; i < Math.min(owner.gameManager.getPlayers(), 2); i++) {
					if(!owner.gameManager.replayMode || owner.gameManager.replayRerecord ||
							!owner.gameManager.engine[i].gameActive)
					{
						StandaloneGameKey.gamekey[i].inputStatusUpdate(owner.gameManager.engine[i].ctrl);
					}
				}

				for(int i = 0; i <= fastforward; i++) owner.gameManager.updateAll();
			}
		}

		if(owner.gameManager != null) {
			// Retry button
			if(StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_RETRY) || StandaloneGameKey.gamekey[1].isPushKey(StandaloneGameKey.BUTTON_RETRY)) {
				pause = false;
				owner.gameManager.reset();
			}

			// Return to title
			if(owner.gameManager.getQuitFlag() ||
					StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_GIVEUP) ||
					StandaloneGameKey.gamekey[1].isPushKey(StandaloneGameKey.BUTTON_GIVEUP))
			{
				shutdown(true);
				return;
			}
		}

		// Screenshot button
		if(StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_SCREENSHOT) || StandaloneGameKey.gamekey[1].isPushKey(StandaloneGameKey.BUTTON_SCREENSHOT)) {
			ssflag = true;
		}

		// Quit button
		if(StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_QUIT) ||
				StandaloneGameKey.gamekey[1].isPushKey(StandaloneGameKey.BUTTON_QUIT))
		{
			shutdown(true);
/*
			owner.shutdown();
*/
			return;
		}
	}

	/**
	 * Update game state (for netplay)
	 */
	protected void gameUpdateNet() {
		if(owner.gameManager == null) return;
//		if(owner.gameManager.engine.length == 0) return;

		try {
			// Set ingame flag
			boolean prevInGame = isInGame[0];

			if((owner.gameManager.engine != null) && (owner.gameManager.engine.length > 0)) {
				isInGame[0] = owner.gameManager.engine[0].isInGame;
			}
			if(pause && !enableframestep) {
				isInGame[0] = false;
			}

			if(prevInGame != isInGame[0]) {
				StandaloneGameKey.gamekey[0].clear();
				if(isInGame[0])
					MusicList.getInstance().play();
			}

			// Update button inputs
			if(isVisible()) {
				StandaloneGameKey.gamekey[0].update();
			} else {
				StandaloneGameKey.gamekey[0].clear();
			}

			// Title bar update
			if((owner.gameManager != null) && (owner.gameManager.engine != null) &&
					(owner.gameManager.engine.length > 0) && (owner.gameManager.engine[0] != null))
			{
				boolean nowInGame = owner.gameManager.engine[0].isInGame;
				if(prevInGameFlag != nowInGame) {
					prevInGameFlag = nowInGame;
					updateTitleBarCaption();
				}
			}

			// Execute game loops
			if((owner.gameManager != null) && (owner.gameManager.mode != null) && owner.gameManager.engine != null && owner.gameManager.engine.length > 0) {
				StandaloneGameKey.gamekey[0].inputStatusUpdate(owner.gameManager.engine[0].ctrl);
				owner.gameManager.updateAll();

				// Return to title
				if(owner.gameManager.getQuitFlag()) {
					shutdown(true);
					return;
				}

				// Retry button
				if(StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_RETRY)) {
					owner.gameManager.mode.netplayOnRetryKey(owner.gameManager.engine[0], 0);
				}
			}

			// Screenshot button
			if(StandaloneGameKey.gamekey[0].isPushKey(StandaloneGameKey.BUTTON_SCREENSHOT) || StandaloneGameKey.gamekey[1].isPushKey(StandaloneGameKey.BUTTON_SCREENSHOT)) {
				ssflag = true;
			}

			// Enter to new mode
/*
			if(strModeToEnter == null) {
				owner.enterNewMode(null);
				strModeToEnter = "";
				MusicList.getInstance().stop();
			} else if (strModeToEnter.length() > 0) {
				owner.enterNewMode(strModeToEnter);
				strModeToEnter = "";
			}
*/
//			String newMode = modeToEnter.poll();
//			if(newMode != null) {
//				if("".equals(newMode)) {
//					owner.enterNewMode(null);
//					MusicList.getInstance().stop();
//				}
//				else {
//					owner.enterNewMode(newMode);
//					modesEntered.offer(newMode);
//				}
//			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			try {
				if((owner.gameManager != null) && owner.gameManager.getQuitFlag()) {
					shutdown(true);
					return;
				} else {
					log.error("update NPE", e);
				}
			} catch (Throwable e2) {}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if((owner.gameManager != null) && owner.gameManager.getQuitFlag()) {
					shutdown(true);
					return;
				} else {
					log.error("update fail", e);
				}
			} catch (Throwable e2) {}
		}
	}

	/**
	 * Rendering
	 */
	protected void gameRender() {
		if(owner.gameManager == null) return;

		// Prepare the screen

		Graphics g = null;
		//		if(ssflag || (screenWidth != 640) || (screenHeight != 480)) {
		g = gameBuffer.getGraphics();
		//		} else {
		//			g = bufferStrategy.getDrawGraphics();
		//			if(insets != null) g.translate(insets.left, insets.top);
		//		}

		// Game screen
		StandaloneNormalFont.graphics = (Graphics2D) g;
		owner.gameManager.receiver.setGraphics(g);
		owner.gameManager.renderAll();

		if((owner.gameManager.engine.length > 0) && (owner.gameManager.engine[0] != null)) {
			int offsetX = owner.gameManager.receiver.getFieldDisplayPositionX(owner.gameManager.engine[0], 0);
			int offsetY = owner.gameManager.receiver.getFieldDisplayPositionY(owner.gameManager.engine[0], 0);

			// Pause menu
			if(pause && !enableframestep && !pauseMessageHide) {
				StandaloneNormalFont.printFont(offsetX + 12, offsetY + 188 + (cursor * 16), "b", StandaloneNormalFont.COLOR_RED);

				StandaloneNormalFont.printFont(offsetX + 28, offsetY + 188, "CONTINUE", (cursor == 0));
				StandaloneNormalFont.printFont(offsetX + 28, offsetY + 204, "RETRY", (cursor == 1));
				StandaloneNormalFont.printFont(offsetX + 28, offsetY + 220, "END", (cursor == 2));
				if(owner.gameManager.replayMode && !owner.gameManager.replayRerecord)
					StandaloneNormalFont.printFont(offsetX + 28, offsetY + 236, "RERECORD", (cursor == 3));
			}

			// Fast forward
			if(fastforward != 0)
				StandaloneNormalFont.printFont(offsetX, offsetY + 376, "e" + (fastforward + 1), StandaloneNormalFont.COLOR_ORANGE);
			if(owner.gameManager.replayShowInvisible)
				StandaloneNormalFont.printFont(offsetX, offsetY + 392, "SHOW INVIS", StandaloneNormalFont.COLOR_ORANGE);
		}

		// FPSDisplay
		if(showfps) {
			String fps = df.format(totalFPS) + "/" + maxfpsCurrent + " FPS";
			if(!syncDisplay && !syncRender)
				fps += " (" + df.format(drawnFPS) + "/" + df.format(renderedFPS) + " DRAWN)";
			else if(!syncDisplay)
				fps += " (" + df.format(renderedFPS) + " RENDERED)";
			StandaloneNormalFont.printFont(0, 480-16, fps, StandaloneNormalFont.COLOR_BLUE, 1.0f);
		}

		// Displayed on the screen /ScreenshotCreating
		g.dispose();
		//		if(ssflag || (screenWidth != 640) || (screenHeight != 480)) {
		if(ssflag) saveScreenShot();

		Graphics g2 = imageBuffer.getGraphics();
		g2.drawImage(gameBuffer, 0, 0, imageBuffer.getWidth(), imageBuffer.getHeight(), null);
		g2.dispose();
		
		sync();

		ssflag = false;
		//		} else if((bufferStrategy != null) && !bufferStrategy.contentsLost()) {
		//			bufferStrategy.show();
		//			if(syncDisplay) Toolkit.getDefaultToolkit().sync();
		//		}
	}

	/**
	 * Rendering(For net play)
	 */
	protected void gameRenderNet() {
		if(owner.gameManager == null) return;

		Graphics g = null;
		//		if(ssflag || (screenWidth != 640) || (screenHeight != 480)) {
		g = gameBuffer.getGraphics();
		//		} else {
		//			g = bufferStrategy.getDrawGraphics();
		//			if(insets != null) g.translate(insets.left, insets.top);
		//		}

		// Game screen
		try {
			StandaloneNormalFont.graphics = (Graphics2D) g;
			owner.gameManager.receiver.setGraphics(g);
			owner.gameManager.renderAll();
		} catch (NullPointerException e) {
			try {
				if((owner.gameManager == null) || !owner.gameManager.getQuitFlag()) {
					log.error("render NPE", e);
				}
			} catch (Throwable e2) {}
		} catch (Exception e) {
			try {
				if((owner.gameManager == null) || !owner.gameManager.getQuitFlag()) {
					log.error("render fail", e);
				}
			} catch (Throwable e2) {}
		}

		// FPSDisplay
		if(showfps) {
			String fps = df.format(totalFPS) + "/" + maxfpsCurrent + " FPS";
			if(!syncDisplay && !syncRender)
				fps += " (" + df.format(drawnFPS) + "/" + df.format(renderedFPS) + " DRAWN)";
			else if(!syncDisplay)
				fps += " (" + df.format(renderedFPS) + " RENDERED)";
			StandaloneNormalFont.printFont(0, 480-16, fps, StandaloneNormalFont.COLOR_BLUE, 1.0f);
		}

		// Displayed on the screen /ScreenshotCreating
		g.dispose();
		//		if(ssflag || (screenWidth != 640) || (screenHeight != 480)) {
		if(ssflag) saveScreenShot();

		Graphics g2 = imageBuffer.getGraphics();
		g2.drawImage(gameBuffer, 0, 0, imageBuffer.getWidth(), imageBuffer.getHeight(), null);
		g2.dispose();
		
		sync();

		ssflag = false;
		//		} else if((bufferStrategy != null) && !bufferStrategy.contentsLost()) {
		//			bufferStrategy.show();
		//			if(syncDisplay) Toolkit.getDefaultToolkit().sync();
		//		}
	}

	private AtomicBoolean syncing = new AtomicBoolean(false);
	
	private Runnable sync = new Runnable() {
		@Override
		public void run() {
			imageBufferLabel.repaint();
			if(syncDisplay)
				Toolkit.getDefaultToolkit().sync();
			dps.add(new FramePerSecond());
			syncing.set(false);
		}
	};
	
	private void sync() {
		if(syncDisplay) {
			try {
				EventQueue.invokeAndWait(sync);
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			if(!syncing.get()) {
				syncing.set(true);
				EventQueue.invokeLater(sync);
			}
		}
		rps.add(new FramePerSecond());
		hooks.dispatcher().frameSynced(this);
	}
	
	/**
	 * Save a screen shot
	 */
	protected void saveScreenShot() {
		// Create filename
		String dir = "ss";
		Calendar c = Calendar.getInstance();
		DateFormat dfm = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String filename = dir + "/" + dfm.format(c.getTime()) + ".png";
		log.info("Saving screenshot to " + filename);

		// Create ss folder if not exist
		File ssfolder = new File(dir);
		if (!ssfolder.exists()) {
			if (ssfolder.mkdir()) {
				log.info("Created screenshot folder: " + dir);
			} else {
				log.info("Couldn't create screenshot folder at "+ dir);
			}
		}

		// Write
		try {
			javax.imageio.ImageIO.write(gameBuffer, "PNG", new File(filename));
		} catch (Exception e) {
			log.warn("Failed to save screenshot to " + filename, e);
		}
	}

	/**
	 * Update title bar text
	 */
	public void updateTitleBarCaption() {
		GameManager gameManager = owner.gameManager;

		String strModeName = null;
		if((gameManager != null) && (gameManager.mode != null)) {
			strModeName = gameManager.mode.getName();
		}

		String strBaseTitle = "zeromeaner - " + strModeName;
		if(isNetPlay) strBaseTitle = "zeromeaner NetPlay - " + strModeName;

		String strTitle = strBaseTitle;

		if(isNetPlay && strModeName.equals("NET-DUMMY")) {
			strTitle = "zeromeaner NetPlay";
		} else if((gameManager != null) && (gameManager.engine != null) && (gameManager.engine.length > 0) && (gameManager.engine[0] != null)) {
			if(pause && !enableframestep)
				strTitle = "[PAUSE] " + strBaseTitle;
			else if(gameManager.engine[0].isInGame && !gameManager.replayMode && !gameManager.replayRerecord)
				strTitle = "[PLAY] " + strBaseTitle;
			else if(gameManager.replayMode && gameManager.replayRerecord)
				strTitle = "[RERECORD] " + strBaseTitle;
			else if(gameManager.replayMode && !gameManager.replayRerecord)
				strTitle = "[REPLAY] " + strBaseTitle;
			else
				strTitle = "[MENU] " + strBaseTitle;
		}

/*
		this.setTitle(strTitle);
*/
	}

	private class FullScreenKeyListener extends KeyAdapter {
		private final StandaloneFrame owner;
		private boolean fullscreen;
		private int exstate;
		private Dimension size;
		private Point location;
		private Container contentPane;
	
		private FullScreenKeyListener(StandaloneFrame owner) {
			this.owner = owner;
		}
	
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() != KeyEvent.VK_ENTER || e.getModifiersEx() != KeyEvent.CTRL_DOWN_MASK)
				return;
			e.consume();
			if(!fullscreen) {
				exstate = owner.getExtendedState();
				contentPane = owner.getContentPane();
				size = owner.getSize();
				location = owner.getLocation();
				owner.getContentPane().remove(imageBufferLabel);
				JPanel content = new JPanel(new GridBagLayout());
				content.add(
						imageBufferLabel, 
						new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
				content.add(
						new JLabel("Press CTRL+ENTER to leave full-screen mode"), 
						new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
				content.addKeyListener(this);
				content.addKeyListener(new GameFrameKeyEvent());
				owner.setContentPane(content);
				owner.setExtendedState(JFrame.MAXIMIZED_BOTH);
				content.requestFocus();
				EventQueue.invokeLater(new Runnable() {
					private Dimension last = null;
					@Override
					public void run() {
						Dimension d = owner.getSize();
						if(d.equals(last))
							return;
						last = d;
						d = new Dimension(d.width - 48, d.height - 48);
						Dimension id;
						if(d.width * 3 / 4 > d.height) {
							id = new Dimension(d.height * 4 / 3, d.height);
						} else {
							id = new Dimension(d.width, d.width * 3 / 4);
						}
						imageBuffer = new BufferedImage((int) id.getWidth(), (int) id.getHeight(), BufferedImage.TYPE_INT_ARGB);
						imageBufferLabel.setIcon(new ImageIcon(imageBuffer));
						imageBufferLabel.revalidate();
						EventQueue.invokeLater(this);
					}
				});
				fullscreen = true;
			} else {
				owner.getContentPane().remove(imageBufferLabel);
				owner.setContentPane(contentPane);
				add(
						imageBufferLabel, 
						new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
				owner.setExtendedState(exstate);
				owner.setSize(size);
				owner.setLocation(location);
				imageBuffer = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
				imageBufferLabel.setIcon(new ImageIcon(imageBuffer));
				imageBufferLabel.revalidate();
				fullscreen = false;
				requestFocus();
			}
		}
	}

	/**
	 * Keyboard event Processing
	 */
	protected class GameFrameKeyEvent extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK)
				return;

			setButtonPressedState(e.getKeyCode(), true);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK)
				return;

			setButtonPressedState(e.getKeyCode(), false);
		}

		protected void setButtonPressedState(int keyCode, boolean pressed) {
			if(StandaloneGamePanel.this.isInGame == null)
				return;
			boolean[] isInGame = Arrays.copyOf(StandaloneGamePanel.this.isInGame, StandaloneGamePanel.this.isInGame.length);
			for(int playerID = 0; playerID < StandaloneGameKey.gamekey.length; playerID++) {
				int[] kmap = isInGame[playerID] ? StandaloneGameKey.gamekey[playerID].keymap : StandaloneGameKey.gamekey[playerID].keymapNav;
				for(int i = 0; i < StandaloneGameKey.MAX_BUTTON; i++) {
					if(keyCode == kmap[i]) {
						if(playerID != 0 && (i == StandaloneGameKey.BUTTON_UP || i == StandaloneGameKey.BUTTON_DOWN))
							continue;
//						log.debug("KeyCode:" + keyCode + " pressed:" + pressed + " button:" + i);
						StandaloneGameKey.gamekey[playerID].setPressState(i, pressed);
					}
				}
			}
		}
	}
}
