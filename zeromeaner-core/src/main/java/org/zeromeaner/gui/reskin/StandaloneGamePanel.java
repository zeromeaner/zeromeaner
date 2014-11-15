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
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.apache.log4j.Logger;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.util.MusicList;
import org.zeromeaner.util.Options;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;

/**
 * Game screen frame
 */
public class StandaloneGamePanel extends JPanel implements Runnable {
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
	
	protected ExecutorService videoPool = Executors.newSingleThreadExecutor();
	
	private static class FocusableJLabel extends JLabel {
		private FocusableJLabel(Icon image) {
			super(image);
			setFocusable(true);
			setFocusCycleRoot(true);
			enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);
		}
	}

	private class FramePerSecond implements Delayed {
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
	
	private DelayQueue<FramePerSecond> vps = new DelayQueue<FramePerSecond>();
	private DelayQueue<FramePerSecond> fps = new DelayQueue<FramePerSecond>();
	private DelayQueue<FramePerSecond> videoFrames = new DelayQueue<>();
	
	/** Parent window */
	protected StandaloneFrame owner = null;

	/** The size of the border and title bar */
	protected Insets insets = null;

	protected BufferedImage imageBuffer;

	protected JLabel imageBufferLabel;

	protected BufferedImage gameBuffer;
	
	protected BufferedImage videoBuffer;

	//	/** BufferStrategy */
	//	protected BufferStrategy bufferStrategy = null;

	/** Game loop thread */
	protected Thread thread = null;

	/** trueThread moves between */
	public AtomicBoolean running = new AtomicBoolean(false);

	/** MaximumFPS (Setting) */
	public int maxfps;
	
	public int videoFPS;

	/** Current MaximumFPS */
	protected int maxfpsCurrent = 0;

	/** Current Pause time */
	protected long periodCurrent = 0;

	
	/** ActualFPS */
	public double visibleFPS = 0.0;

	public double totalFPS = 0;

	/** FPSDisplayDecimalFormat */
	public DecimalFormat df = new DecimalFormat("0");

	/** True if execute Toolkit.getDefaultToolkit().sync() at the end of each frame */
	public boolean syncDisplay = true;

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
	
	public IMediaWriter videoWriter;
	
	public File videoFile;

	/**
	 * Constructor
	 * @param owner Parent window
	 * @throws HeadlessException Keyboard, Mouse, Exceptions such as the display if there is no
	 */
	public StandaloneGamePanel(StandaloneFrame owner) throws HeadlessException {
		super(new GridBagLayout());
		this.owner = owner;

		setDoubleBuffered(true);


		imageBuffer = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		gameBuffer = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		videoBuffer = new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);

		add(
				imageBufferLabel = new FocusableJLabel(new ImageIcon(imageBuffer)), 
				new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
		
		imageBufferLabel.setText("No Active Game.  Click \"Play\" to start.");
		imageBufferLabel.setIcon(null);
		
		imageBufferLabel.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, new Color(255, 255, 255, 127)));

		maxfps = Options.standalone().MAX_FPS.value();

		videoFPS = Options.standalone().VIDEO_FPS.value();

		log.debug("GameFrame created");

		setOpaque(true);	
		setFocusable(true);
		setFocusCycleRoot(true);
		setFocusTraversalKeysEnabled(false);
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

		int screenWidth = Options.standalone().SCREEN_WIDTH.value();
		int screenHeight = Options.standalone().SCREEN_HEIGHT.value();

		imageBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
		imageBufferLabel.setText(null);
		imageBufferLabel.setIcon(new ImageIcon(imageBuffer));
		imageBufferLabel.revalidate();

		if(!running.get()) {
			thread = new Thread(this, "Game Thread");
			thread.start();
		}
	}

	/**
	 * End processing
	 */
	public void shutdown() {
		MusicList.getInstance().stop();
		if(isNetPlay) {
			// Reload global config (because it can change rules)
			StandaloneMain.loadGlobalConfig();
		}
		synchronized(running) {
			running.set(false);
			running.notifyAll();
		}
		imageBufferLabel.setText("No Active Game.  Click \"Play\" to start.");
		imageBufferLabel.setIcon(null);
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
	}
	
	private long videoStart;
	
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

		if(Options.standalone().RECORD_VIDEO.value()) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
			try {
				videoFile = new File(System.getProperty("user.dir"), "video/" + df.format(System.currentTimeMillis()) + ".mpg");
				videoFile.getParentFile().mkdirs();
				videoWriter = ToolFactory.makeWriter(videoFile.getCanonicalPath());
				videoWriter.addVideoStream(0, 0, ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_MPEG1VIDEO), IRational.make((double) videoFPS), gameBuffer.getWidth(), gameBuffer.getHeight());
				videoStart = System.currentTimeMillis();
				log.info("Recording video to " + videoFile);
			} catch (IOException e) {
				e.printStackTrace();
				videoWriter = null;
			}
		} else
			videoWriter = null;
		
		// Main loop
		log.debug("Game thread start");
		
		running.set(true);
		
		Runnable task = new Runnable() {
			@Override
			public void run() {
				while(fps.poll() != null)
					;
				while(vps.poll() != null)
					;
				if(vps.size() > maxfps)
					return;
				fps.add(new FramePerSecond());
				doFrame(true);
				while(fps.poll() != null)
					;
				while(vps.poll() != null)
					;
				totalFPS = fps.size();
				visibleFPS = vps.size();
				int unrenderedFrames = 0;
				while(totalFPS < maxfps - 1) {
					fps.add(new FramePerSecond());
					unrenderedFrames++;
					doFrame(false);
					while(fps.poll() != null)
						;
					while(vps.poll() != null)
						;
					totalFPS = fps.size();
					visibleFPS = vps.size();
				}
			}
		};
		
		ScheduledFuture<?> f = gexec.scheduleAtFixedRate(task, 0, TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS) / maxfps, TimeUnit.NANOSECONDS);
		
		while(running.get()) {
			synchronized(running) {
				try {
					running.wait();
				} catch(InterruptedException ie) {
				}
			}
		}
		
		f.cancel(false);

		if(videoWriter != null) {
			synchronized(videoBuffer) {
				videoWriter.close();
				log.info("Finished recording video to " + videoFile);
				videoWriter = null;
			}
		}
		
		owner.gameManager.shutdown();
		owner.gameManager = null;

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
					shutdown();
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
				shutdown();
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
			shutdown();
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
		if(owner.gameManager.engine.length == 0) return;

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
					shutdown();
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
			String newMode = modeToEnter.poll();
			if(newMode != null) {
				if("".equals(newMode)) {
					owner.enterNewMode(null);
					MusicList.getInstance().stop();
				}
				else {
					owner.enterNewMode(newMode);
					modesEntered.offer(newMode);
				}
			}
		} catch (NullPointerException e) {
			try {
				if((owner.gameManager != null) && owner.gameManager.getQuitFlag()) {
					shutdown();
					return;
				} else {
					log.error("update NPE", e);
				}
			} catch (Throwable e2) {}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if((owner.gameManager != null) && owner.gameManager.getQuitFlag()) {
					shutdown();
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
			StandaloneNormalFont.printFont(0, 480-16, df.format(totalFPS) + "/" + maxfpsCurrent + " (" + df.format(visibleFPS) + " RENDERED)", StandaloneNormalFont.COLOR_BLUE, 1.0f);
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
			StandaloneNormalFont.printFont(0, 480-16, df.format(totalFPS) + "/" + maxfpsCurrent + " (" + df.format(visibleFPS) + " RENDERED)", StandaloneNormalFont.COLOR_BLUE, 1.0f);
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

	private boolean syncing = false;
	private Runnable sync = new Runnable() {
		@Override
		public void run() {
			imageBufferLabel.repaint();
			if(syncDisplay)
				Toolkit.getDefaultToolkit().sync();
			syncing = false;
			vps.add(new FramePerSecond());
		}
	};
	
	private void sync() {
		if(syncing)
			return;
		syncing = true;
		if(syncDisplay) {
			try {
				EventQueue.invokeAndWait(sync);
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else
			EventQueue.invokeLater(sync);
		synchronized(videoBuffer) {
			while(videoFrames.poll() != null)
				;
			if(videoFrames.size() < videoFPS) {
				if(videoWriter != null) {
					videoBuffer.getGraphics().drawImage(gameBuffer, 0, 0, null);
					videoWriter.encodeVideo(0, videoBuffer, System.currentTimeMillis() - videoStart, TimeUnit.MILLISECONDS);
				}
				videoFrames.add(new FramePerSecond());
			}
		}
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

	/**
	 * Window event Processing
	 */
	protected class GameFrameWindowEvent extends InternalFrameAdapter {
		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
			shutdown();
		}
	}

	/**
	 * Keyboard event Processing
	 */
	protected class GameFrameKeyEvent extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			setButtonPressedState(e.getKeyCode(), true);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			setButtonPressedState(e.getKeyCode(), false);
		}

		protected void setButtonPressedState(int keyCode, boolean pressed) {
			for(int playerID = 0; playerID < StandaloneGameKey.gamekey.length; playerID++) {
				int[] kmap = isInGame[playerID] ? StandaloneGameKey.gamekey[playerID].keymap : StandaloneGameKey.gamekey[playerID].keymapNav;
				for(int i = 0; i < StandaloneGameKey.MAX_BUTTON; i++) {
					if(keyCode == kmap[i]) {
						//log.debug("KeyCode:" + keyCode + " pressed:" + pressed + " button:" + i);
						StandaloneGameKey.gamekey[playerID].setPressState(i, pressed);
					}
				}
			}
		}
	}
}
