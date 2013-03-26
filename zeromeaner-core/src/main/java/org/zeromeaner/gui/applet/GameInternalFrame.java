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
package org.zeromeaner.gui.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;


import org.apache.log4j.Logger;
import org.zeromeaner.game.play.GameManager;

/**
 * Game screen frame
 */
public class GameInternalFrame extends JInternalFrame implements Runnable {
	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/** Log */
	static Logger log = Logger.getLogger(GameInternalFrame.class);

	/** Parent window */
	protected NullpoMinoInternalFrame owner = null;
	
	/** The size of the border and title bar */
	protected Insets insets = null;

	protected BufferedImage imageBuffer;
	
	protected JLabel imageBufferLabel;
	
	protected BufferedImage gameBuffer;
	
//	/** BufferStrategy */
//	protected BufferStrategy bufferStrategy = null;

	/** Game loop thread */
	protected Thread thread = null;

	/** trueThread moves between */
	public volatile boolean running = false;

	/** FPSFor calculation */
	protected long calcInterval = 0;

	/** FPSFor calculation */
	protected long prevCalcTime = 0;

	/**  frame count */
	protected long frameCount = 0;

	/** MaximumFPS (Setting) */
	public int maxfps;

	/** Current MaximumFPS */
	protected int maxfpsCurrent = 0;

	/** Current Pause time */
	protected long periodCurrent = 0;

	/** ActualFPS */
	public double actualFPS = 0.0;

	/** FPSDisplayDecimalFormat */
	public DecimalFormat df = new DecimalFormat("0.0");

	/** Used by perfect fps mode */
	public long perfectFPSDelay = 0;

	/** True to use perfect FPS */
	public boolean perfectFPSMode = false;

	/** Execute Thread.yield() during Perfect FPS mode */
	public boolean perfectYield = true;

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

	/** Mode name to enter (null=Exit) */
	public String strModeToEnter = "";

	/** Previous ingame flag (Used by title-bar text change) */
	protected boolean prevInGameFlag = false;

	/** Current game mode name */
	public String modeName;

	/**
	 * Constructor
	 * @param owner Parent window
	 * @throws HeadlessException Keyboard, Mouse, Exceptions such as the display if there is no
	 */
	public GameInternalFrame(NullpoMinoInternalFrame owner) throws HeadlessException {
		super();
		this.owner = owner;

		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		setTitle(NullpoMinoInternalFrame.getUIText("Title_Game"));
		setBackground(Color.black);
		setResizable(false);
		
		setDoubleBuffered(true);

		addInternalFrameListener(new GameFrameWindowEvent());
		addKeyListener(new GameFrameKeyEvent());

		imageBuffer = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		gameBuffer = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		
		setLayout(new BorderLayout());
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(imageBufferLabel = new JLabel(new ImageIcon(imageBuffer)));
		imageBufferLabel.setFocusable(true);
		imageBufferLabel.addKeyListener(new GameFrameKeyEvent());
		add(panel, BorderLayout.CENTER);
		panel.setFocusable(true);
		panel.addKeyListener(new GameFrameKeyEvent());
		
		setFocusable(true);
				
		maxfps = NullpoMinoInternalFrame.propConfig.getProperty("option.maxfps", 60);

		
		log.debug("GameFrame created");
		
		pack();

		AppletMain.instance.desktop.add(this);
	}

	/**
	 * Display the game window
	 */
	public void displayWindow() {
		
		int screenWidth = NullpoMinoInternalFrame.propConfig.getProperty("option.screenwidth", 640);
		int screenHeight = NullpoMinoInternalFrame.propConfig.getProperty("option.screenheight", 480);

		imageBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
		imageBufferLabel.setIcon(new ImageIcon(imageBuffer));
		
		pack();
		setVisible(true);
		
		if(!running) {
			thread = new Thread(this, "Game Thread");
			thread.start();
		}
	}

	/**
	 * End processing
	 */
	public void shutdown() {
		if(isNetPlay) {
			if(NullpoMinoInternalFrame.netLobby != null) {
				try {
					NullpoMinoInternalFrame.netLobby.shutdown();
				} catch (Exception e) {
					log.debug("Exception on NetLobby shutdown", e);
				}
				NullpoMinoInternalFrame.netLobby = null;
			}

			// Reload global config (because it can change rules)
			NullpoMinoInternalFrame.loadGlobalConfig();
		}
		running = false;
		owner.setVisible(true);
		setVisible(false);

		// GCCall
		System.gc();
	}

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
		beforeTime = System.nanoTime();
		prevCalcTime = beforeTime;
		pause = false;
		pauseMessageHide = false;
		fastforward = 0;
		cursor = 0;
		prevInGameFlag = false;
		isInGame = new boolean[2];
		GameKeyApplet.gamekey[0].clear();
		GameKeyApplet.gamekey[1].clear();
		updateTitleBarCaption();

		// Settings to take effect
		enableframestep = NullpoMinoInternalFrame.propConfig.getProperty("option.enableframestep", false);
		showfps = NullpoMinoInternalFrame.propConfig.getProperty("option.showfps", true);
		perfectFPSMode = NullpoMinoInternalFrame.propConfig.getProperty("option.perfectFPSMode", false);
		perfectYield = NullpoMinoInternalFrame.propConfig.getProperty("option.perfectYield", true);
		syncDisplay = NullpoMinoInternalFrame.propConfig.getProperty("option.syncDisplay", true);

		// Main loop
		log.debug("Game thread start");
		running = true;
		perfectFPSDelay = System.nanoTime();
		while(running) {
			if(isNetPlay) {
				gameUpdateNet();
				gameRenderNet();
			} else if(isVisible() && isSelected()) {
				gameUpdate();
				gameRender();
			} else {
				GameKeyApplet.gamekey[0].clear();
				GameKeyApplet.gamekey[1].clear();
			}

			// FPS cap
			sleepFlag = false;

			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;

			sleepTime = (periodCurrent - timeDiff) - overSleepTime;
			sleepTimeInMillis = sleepTime / 1000000L;

			if((sleepTimeInMillis >= 4) && (!perfectFPSMode)) {
				// If it is possible to use sleep
				if(maxfps > 0) {
					try {
						Thread.sleep(sleepTimeInMillis);
					} catch(InterruptedException e) {
						log.debug("Game thread interrupted", e);
					}
				}
				// sleep() oversleep
				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
				perfectFPSDelay = System.nanoTime();
				sleepFlag = true;
			} else if((perfectFPSMode) || (sleepTime > 0)) {
				// Perfect FPS
				overSleepTime = 0L;
				if(maxfpsCurrent > maxfps + 5) maxfpsCurrent = maxfps + 5;
				if(perfectYield) {
					while(System.nanoTime() < perfectFPSDelay + 1000000000 / maxfps) {Thread.yield();}
				} else {
					while(System.nanoTime() < perfectFPSDelay + 1000000000 / maxfps) {}
				}
				perfectFPSDelay += 1000000000 / maxfps;

				// Don't run in super fast after the heavy slowdown
				if(System.nanoTime() > perfectFPSDelay + 2000000000 / maxfps) {
					perfectFPSDelay = System.nanoTime();
				}

				sleepFlag = true;
			}

			if(!sleepFlag) {
				// Impossible to sleep!
				overSleepTime = 0L;
				if(++noDelays >= 16) {
					Thread.yield();
					noDelays = 0;
				}
				perfectFPSDelay = System.nanoTime();
			}

			beforeTime = System.nanoTime();
			calcFPS(periodCurrent);
		}

		NullpoMinoInternalFrame.gameManager.shutdown();
		NullpoMinoInternalFrame.gameManager = null;

		log.debug("Game thread end");
	}

	/**
	 * Update game state
	 */
	protected void gameUpdate() {
		if(NullpoMinoInternalFrame.gameManager == null) return;

		// Set ingame flag
		for(int i = 0; i < 2; i++) {
			boolean prevInGame = isInGame[i];

			if((NullpoMinoInternalFrame.gameManager.engine != null) && (NullpoMinoInternalFrame.gameManager.engine.length > i)) {
				isInGame[i] = NullpoMinoInternalFrame.gameManager.engine[i].isInGame;
			}
			if(pause && !enableframestep) {
				isInGame[i] = false;
			}

			if(prevInGame != isInGame[i]) {
				GameKeyApplet.gamekey[i].clear();
			}
		}

		GameKeyApplet.gamekey[0].update();
		GameKeyApplet.gamekey[1].update();

		// Title bar update
		if((NullpoMinoInternalFrame.gameManager != null) && (NullpoMinoInternalFrame.gameManager.engine != null) &&
		   (NullpoMinoInternalFrame.gameManager.engine.length > 0) && (NullpoMinoInternalFrame.gameManager.engine[0] != null))
		{
			boolean nowInGame = NullpoMinoInternalFrame.gameManager.engine[0].isInGame;
			if(prevInGameFlag != nowInGame) {
				prevInGameFlag = nowInGame;
				updateTitleBarCaption();
			}
		}

		// Pause button
		if(GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_PAUSE) || GameKeyApplet.gamekey[1].isPushKey(GameKeyApplet.BUTTON_PAUSE)) {
			if(!pause) {
				if((NullpoMinoInternalFrame.gameManager != null) && (NullpoMinoInternalFrame.gameManager.isGameActive()) && (pauseFrame <= 0)) {
					ResourceHolderApplet.soundManager.play("pause");
					pause = true;
					if(!enableframestep) pauseFrame = 5;
					cursor = 0;
				}
			} else {
				ResourceHolderApplet.soundManager.play("pause");
				pause = false;
				pauseFrame = 0;
			}
			updateTitleBarCaption();
		}
		// Pause menu
		if(pause && !enableframestep && !pauseMessageHide) {
			// Cursor movement
			if(GameKeyApplet.gamekey[0].isMenuRepeatKey(GameKeyApplet.BUTTON_UP)) {
				ResourceHolderApplet.soundManager.play("cursor");
				cursor--;

				if(cursor < 0) {
					if(NullpoMinoInternalFrame.gameManager.replayMode && !NullpoMinoInternalFrame.gameManager.replayRerecord)
						cursor = 3;
					else
						cursor = 2;
				}
			}
			if(GameKeyApplet.gamekey[0].isMenuRepeatKey(GameKeyApplet.BUTTON_DOWN)) {
				ResourceHolderApplet.soundManager.play("cursor");
				cursor++;
				if(cursor > 3) cursor = 0;

				if((!NullpoMinoInternalFrame.gameManager.replayMode || NullpoMinoInternalFrame.gameManager.replayRerecord) && (cursor > 2))
					cursor = 0;
			}

			// Confirm
			if(GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_A)) {
				ResourceHolderApplet.soundManager.play("decide");
				if(cursor == 0) {
					// Resumption
					pause = false;
					pauseFrame = 0;
					GameKeyApplet.gamekey[0].clear();
				} else if(cursor == 1) {
					// Retry
					pause = false;
					NullpoMinoInternalFrame.gameManager.reset();
				} else if(cursor == 2) {
					// End
					shutdown();
					return;
				} else if(cursor == 3) {
					// Replay re-record
					NullpoMinoInternalFrame.gameManager.replayRerecord = true;
					cursor = 0;
				}
				updateTitleBarCaption();
			}
			// Unpause by cancel key
			else if(GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_B) && (pauseFrame <= 0)) {
				ResourceHolderApplet.soundManager.play("pause");
				pause = false;
				pauseFrame = 5;
				GameKeyApplet.gamekey[0].clear();
				updateTitleBarCaption();
			}
		}
		if(pauseFrame > 0) pauseFrame--;

		// Hide pause menu
		pauseMessageHide = GameKeyApplet.gamekey[0].isPressKey(GameKeyApplet.BUTTON_C);

		if(NullpoMinoInternalFrame.gameManager.replayMode && !NullpoMinoInternalFrame.gameManager.replayRerecord && NullpoMinoInternalFrame.gameManager.engine[0].gameActive) {
			// Replay speed
			if(GameKeyApplet.gamekey[0].isMenuRepeatKey(GameKeyApplet.BUTTON_LEFT)) {
				if(fastforward > 0) {
					fastforward--;
				}
			}
			if(GameKeyApplet.gamekey[0].isMenuRepeatKey(GameKeyApplet.BUTTON_RIGHT)) {
				if(fastforward < 98) {
					fastforward++;
				}
			}

			// Replay re-record
			if(GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_D)) {
				NullpoMinoInternalFrame.gameManager.replayRerecord = true;
				cursor = 0;
			}
			// Show invisible blocks in replay
			if(GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_E)) {
				NullpoMinoInternalFrame.gameManager.replayShowInvisible = !NullpoMinoInternalFrame.gameManager.replayShowInvisible;
				cursor = 0;
			}
		} else {
			fastforward = 0;
		}

		// Execute game loops
		if(!pause || (GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_FRAMESTEP) && enableframestep)) {
			if(NullpoMinoInternalFrame.gameManager != null) {
				for(int i = 0; i < Math.min(NullpoMinoInternalFrame.gameManager.getPlayers(), 2); i++) {
					if(!NullpoMinoInternalFrame.gameManager.replayMode || NullpoMinoInternalFrame.gameManager.replayRerecord ||
					   !NullpoMinoInternalFrame.gameManager.engine[i].gameActive)
					{
						GameKeyApplet.gamekey[i].inputStatusUpdate(NullpoMinoInternalFrame.gameManager.engine[i].ctrl);
					}
				}

				for(int i = 0; i <= fastforward; i++) NullpoMinoInternalFrame.gameManager.updateAll();
			}
		}

		if(NullpoMinoInternalFrame.gameManager != null) {
			// Retry button
			if(GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_RETRY) || GameKeyApplet.gamekey[1].isPushKey(GameKeyApplet.BUTTON_RETRY)) {
				pause = false;
				NullpoMinoInternalFrame.gameManager.reset();
			}

			// Return to title
			if(NullpoMinoInternalFrame.gameManager.getQuitFlag() ||
			   GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_GIVEUP) ||
			   GameKeyApplet.gamekey[1].isPushKey(GameKeyApplet.BUTTON_GIVEUP))
			{
				shutdown();
				return;
			}
		}

		// Screenshot button
		if(GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_SCREENSHOT) || GameKeyApplet.gamekey[1].isPushKey(GameKeyApplet.BUTTON_SCREENSHOT)) {
			ssflag = true;
		}

		// Quit button
		if(GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_QUIT) ||
		   GameKeyApplet.gamekey[1].isPushKey(GameKeyApplet.BUTTON_QUIT))
		{
			shutdown();
			owner.shutdown();
			return;
		}
	}

	/**
	 * Update game state (for netplay)
	 */
	protected void gameUpdateNet() {
		if(NullpoMinoInternalFrame.gameManager == null) return;

		try {
			// Set ingame flag
			boolean prevInGame = isInGame[0];

			if((NullpoMinoInternalFrame.gameManager.engine != null) && (NullpoMinoInternalFrame.gameManager.engine.length > 0)) {
				isInGame[0] = NullpoMinoInternalFrame.gameManager.engine[0].isInGame;
			}
			if(pause && !enableframestep) {
				isInGame[0] = false;
			}

			if(prevInGame != isInGame[0]) {
				GameKeyApplet.gamekey[0].clear();
			}

			// Update button inputs
			if(isVisible() && isSelected()) {
				GameKeyApplet.gamekey[0].update();
			} else {
				GameKeyApplet.gamekey[0].clear();
			}

			// Title bar update
			if((NullpoMinoInternalFrame.gameManager != null) && (NullpoMinoInternalFrame.gameManager.engine != null) &&
			   (NullpoMinoInternalFrame.gameManager.engine.length > 0) && (NullpoMinoInternalFrame.gameManager.engine[0] != null))
			{
				boolean nowInGame = NullpoMinoInternalFrame.gameManager.engine[0].isInGame;
				if(prevInGameFlag != nowInGame) {
					prevInGameFlag = nowInGame;
					updateTitleBarCaption();
				}
			}

			// Execute game loops
			if((NullpoMinoInternalFrame.gameManager != null) && (NullpoMinoInternalFrame.gameManager.mode != null)) {
				GameKeyApplet.gamekey[0].inputStatusUpdate(NullpoMinoInternalFrame.gameManager.engine[0].ctrl);
				NullpoMinoInternalFrame.gameManager.updateAll();

				// Return to title
				if(NullpoMinoInternalFrame.gameManager.getQuitFlag()) {
					shutdown();
					return;
				}

				// Retry button
				if(GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_RETRY)) {
					NullpoMinoInternalFrame.gameManager.mode.netplayOnRetryKey(NullpoMinoInternalFrame.gameManager.engine[0], 0);
				}
			}

			// Screenshot button
			if(GameKeyApplet.gamekey[0].isPushKey(GameKeyApplet.BUTTON_SCREENSHOT) || GameKeyApplet.gamekey[1].isPushKey(GameKeyApplet.BUTTON_SCREENSHOT)) {
				ssflag = true;
			}

			// Enter to new mode
			if(strModeToEnter == null) {
				owner.enterNewMode(null);
				strModeToEnter = "";
			} else if (strModeToEnter.length() > 0) {
				owner.enterNewMode(strModeToEnter);
				strModeToEnter = "";
			}
		} catch (NullPointerException e) {
			try {
				if((NullpoMinoInternalFrame.gameManager != null) && NullpoMinoInternalFrame.gameManager.getQuitFlag()) {
					shutdown();
					return;
				} else {
					log.error("update NPE", e);
				}
			} catch (Throwable e2) {}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if((NullpoMinoInternalFrame.gameManager != null) && NullpoMinoInternalFrame.gameManager.getQuitFlag()) {
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
		if(NullpoMinoInternalFrame.gameManager == null) return;

		// Prepare the screen

		Graphics g = null;
//		if(ssflag || (screenWidth != 640) || (screenHeight != 480)) {
			g = gameBuffer.getGraphics();
//		} else {
//			g = bufferStrategy.getDrawGraphics();
//			if(insets != null) g.translate(insets.left, insets.top);
//		}

		// Game screen
		NormalFontApplet.graphics = (Graphics2D) g;
		NullpoMinoInternalFrame.gameManager.receiver.setGraphics(g);
		NullpoMinoInternalFrame.gameManager.renderAll();

		if((NullpoMinoInternalFrame.gameManager.engine.length > 0) && (NullpoMinoInternalFrame.gameManager.engine[0] != null)) {
			int offsetX = NullpoMinoInternalFrame.gameManager.receiver.getFieldDisplayPositionX(NullpoMinoInternalFrame.gameManager.engine[0], 0);
			int offsetY = NullpoMinoInternalFrame.gameManager.receiver.getFieldDisplayPositionY(NullpoMinoInternalFrame.gameManager.engine[0], 0);

			// Pause menu
			if(pause && !enableframestep && !pauseMessageHide) {
				NormalFontApplet.printFont(offsetX + 12, offsetY + 188 + (cursor * 16), "b", NormalFontApplet.COLOR_RED);

				NormalFontApplet.printFont(offsetX + 28, offsetY + 188, "CONTINUE", (cursor == 0));
				NormalFontApplet.printFont(offsetX + 28, offsetY + 204, "RETRY", (cursor == 1));
				NormalFontApplet.printFont(offsetX + 28, offsetY + 220, "END", (cursor == 2));
				if(NullpoMinoInternalFrame.gameManager.replayMode && !NullpoMinoInternalFrame.gameManager.replayRerecord)
					NormalFontApplet.printFont(offsetX + 28, offsetY + 236, "RERECORD", (cursor == 3));
			}

			// Fast forward
			if(fastforward != 0)
				NormalFontApplet.printFont(offsetX, offsetY + 376, "e" + (fastforward + 1), NormalFontApplet.COLOR_ORANGE);
			if(NullpoMinoInternalFrame.gameManager.replayShowInvisible)
				NormalFontApplet.printFont(offsetX, offsetY + 392, "SHOW INVIS", NormalFontApplet.COLOR_ORANGE);
		}

		// FPSDisplay
		if(showfps) {
			if(perfectFPSMode)
				NormalFontApplet.printFont(0, 480-16, df.format(actualFPS), NormalFontApplet.COLOR_BLUE, 1.0f);
			else
				NormalFontApplet.printFont(0, 480-16, df.format(actualFPS) + "/" + maxfpsCurrent, NormalFontApplet.COLOR_BLUE, 1.0f);
		}

		// Displayed on the screen /ScreenshotCreating
		g.dispose();
//		if(ssflag || (screenWidth != 640) || (screenHeight != 480)) {
			if(ssflag) saveScreenShot();

			Graphics g2 = imageBuffer.getGraphics();
			g2.drawImage(gameBuffer, 0, 0, imageBuffer.getWidth(), imageBuffer.getHeight(), null);
			g2.dispose();
			Runnable r = new Runnable() {
				@Override
				public void run() {
					revalidate();
					repaint();
				}
			};
			if(syncDisplay) 
				try {
					EventQueue.invokeAndWait(r);
				} catch(Exception ex) {
				}
			else
				EventQueue.invokeLater(r);

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
		if(NullpoMinoInternalFrame.gameManager == null) return;

		Graphics g = null;
//		if(ssflag || (screenWidth != 640) || (screenHeight != 480)) {
			g = gameBuffer.getGraphics();
//		} else {
//			g = bufferStrategy.getDrawGraphics();
//			if(insets != null) g.translate(insets.left, insets.top);
//		}

		// Game screen
		try {
			NormalFontApplet.graphics = (Graphics2D) g;
			NullpoMinoInternalFrame.gameManager.receiver.setGraphics(g);
			NullpoMinoInternalFrame.gameManager.renderAll();
		} catch (NullPointerException e) {
			try {
				if((NullpoMinoInternalFrame.gameManager == null) || !NullpoMinoInternalFrame.gameManager.getQuitFlag()) {
					log.error("render NPE", e);
				}
			} catch (Throwable e2) {}
		} catch (Exception e) {
			try {
				if((NullpoMinoInternalFrame.gameManager == null) || !NullpoMinoInternalFrame.gameManager.getQuitFlag()) {
					log.error("render fail", e);
				}
			} catch (Throwable e2) {}
		}

		// FPSDisplay
		if(showfps) {
			NormalFontApplet.printFont(0, 480-16, df.format(actualFPS) + "/" + maxfpsCurrent, NormalFontApplet.COLOR_BLUE, 1.0f);
		}

		// Displayed on the screen /ScreenshotCreating
		g.dispose();
//		if(ssflag || (screenWidth != 640) || (screenHeight != 480)) {
			if(ssflag) saveScreenShot();

				Graphics g2 = imageBuffer.getGraphics();
				g2.drawImage(gameBuffer, 0, 0, imageBuffer.getWidth(), imageBuffer.getHeight(), null);
				g2.dispose();
				Runnable r = new Runnable() {
					@Override
					public void run() {
						revalidate();
						repaint();
					}
				};
				if(syncDisplay) 
					try {
						EventQueue.invokeAndWait(r);
					} catch(Exception ex) {
					}
				else
					EventQueue.invokeLater(r);

			ssflag = false;
//		} else if((bufferStrategy != null) && !bufferStrategy.contentsLost()) {
//			bufferStrategy.show();
//			if(syncDisplay) Toolkit.getDefaultToolkit().sync();
//		}
	}

	/**
	 * FPSCalculation of
	 * @param period FPSInterval to calculate the
	 */
	protected void calcFPS(long period) {
		frameCount++;
		calcInterval += period;

		// 1Second intervalsFPSRecalculate the
		if(calcInterval >= 1000000000L) {
			long timeNow = System.nanoTime();

			// Actual elapsed timeMeasure
			long realElapsedTime = timeNow - prevCalcTime; // Unit: ns

			// FPSCalculate the
			// realElapsedTimeThe unit ofnsSosConverted to
			actualFPS = ((double) frameCount / realElapsedTime) * 1000000000L;

			frameCount = 0L;
			calcInterval = 0L;
			prevCalcTime = timeNow;

			// Set new target fps
			if((maxfps > 0) && (!perfectFPSMode)) {
				if(actualFPS < maxfps - 1) {
					// Too slow
					maxfpsCurrent++;
					if(maxfpsCurrent > maxfps + 20) maxfpsCurrent = maxfps + 20;
					periodCurrent = (long) (1.0 / maxfpsCurrent * 1000000000);
				} else if(actualFPS > maxfps + 1) {
					// Too fast
					maxfpsCurrent--;
					if(maxfpsCurrent < maxfps - 0) maxfpsCurrent = maxfps - 0;
					if(maxfpsCurrent < 0) maxfpsCurrent = 0;
					periodCurrent = (long) (1.0 / maxfpsCurrent * 1000000000);
				}
			}
		}
	}

	/**
	 * Save a screen shot
	 */
	protected void saveScreenShot() {
		// Create filename
		String dir = NullpoMinoInternalFrame.propGlobal.getProperty("custom.screenshot.directory", "ss");
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
		GameManager gameManager = NullpoMinoInternalFrame.gameManager;

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

		this.setTitle(strTitle);
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
			for(int playerID = 0; playerID < GameKeyApplet.gamekey.length; playerID++) {
				int[] kmap = isInGame[playerID] ? GameKeyApplet.gamekey[playerID].keymap : GameKeyApplet.gamekey[playerID].keymapNav;

				for(int i = 0; i < GameKeyApplet.MAX_BUTTON; i++) {
					if(keyCode == kmap[i]) {
						//log.debug("KeyCode:" + keyCode + " pressed:" + pressed + " button:" + i);
						GameKeyApplet.gamekey[playerID].setPressState(i, pressed);
					}
				}
			}
		}
	}
}
