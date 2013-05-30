package org.zeromeaner.gui.reskin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.game.randomizer.Randomizer;
import org.zeromeaner.game.subsystem.ai.AbstractAI;
import org.zeromeaner.game.subsystem.mode.AbstractNetMode;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.game.subsystem.mode.MarathonMode;
import org.zeromeaner.game.subsystem.wallkick.Wallkick;
import org.zeromeaner.gui.knet.KNetPanel;
import org.zeromeaner.gui.knet.KNetPanelAdapter;
import org.zeromeaner.gui.knet.KNetPanelEvent;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;
import org.zeromeaner.util.Localization;
import org.zeromeaner.util.ResourceFileSystemView;
import org.zeromeaner.util.ResourceInputStream;

public class StandaloneFrame extends JFrame {
	private static final Logger log = Logger.getLogger(StandaloneFrame.class);
	private static final Localization lz = new Localization();
	
	private static Icon icon(String name) {
		URL url = StandaloneFrame.class.getResource(name + ".png");
		return url == null ? null : new ImageIcon(url);
	}
	
	public static final String CARD_PLAY = "toolbar.play";
	public static final String CARD_PLAY_END = "toolbar.play.end";
	public static final String CARD_MODESELECT = "toolbar.modeselect";
	public static final String CARD_NETPLAY = "toolbar.netplay";
	public static final String CARD_NETPLAY_END = "toolbar.netplay.end";
	public static final String CARD_OPEN = "toolbar.open";
	public static final String CARD_OPEN_ONLINE = "toolbar.open_online";
	public static final String CARD_RULE_1P = "toolbar.rule_1p";
	public static final String CARD_KEYS_1P = "toolbar.keys_1p";
	public static final String CARD_TUNING_1P = "toolbar.tuning_1p";
	public static final String CARD_AI_1P = "toolbar.ai_1p";
	public static final String CARD_RULE_2P = "toolbar.rule_2p";
	public static final String CARD_KEYS_2P = "toolbar.keys_2p";
	public static final String CARD_TUNING_2P = "toolbar.tuning_2p";
	public static final String CARD_AI_2P = "toolbar.ai_2p";
	public static final String CARD_GENERAL = "toolbar.general";
	public static final String CARD_CLOSE = "toolbar.close";
	
	private JToolBar toolbar;
	private CardLayout contentCards;
	private String currentCard;
	private String nextCard;
	private JPanel content;
	
	private JPanel playCard;
	private JPanel netplayCard;

	private JToggleButton playButton;
	private JToggleButton netplayButton;
	
	KNetPanel netLobby;
	GameManager gameManager;
	private StandaloneGamePanel gamePanel;
	private StandaloneMusicVolumePanel musicPanel;
	
	public StandaloneFrame() {
		setTitle("0mino");
		setUndecorated(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		
		setLayout(new BorderLayout());
		add(toolbar = createToolbar(), BorderLayout.EAST);
		add(content = new JPanel(contentCards = new CardLayout()), BorderLayout.CENTER);

		netLobby = new KNetPanel(StandaloneMain.userId, false);
		netLobby.setPreferredSize(new Dimension(800, 250));
		netLobby.addKNetPanelListener(new KNetPanelAdapter() {
			@Override
			public void knetPanelShutdown(KNetPanelEvent e) {
				content.remove(netLobby);
				content.revalidate();
				content.repaint();
			}
		});
		
		gamePanel = new StandaloneGamePanel(this);
		musicPanel = new StandaloneMusicVolumePanel();
		
		createCards();
	}
	
	private static void add(JToolBar toolbar, ButtonGroup g, AbstractButton b) {
		b.setFocusable(false);
		b.setBorder(null);
		b.setHorizontalAlignment(SwingConstants.RIGHT);
		toolbar.add(b);
		g.add(b);
	}
	
	private void createCards() {
		playCard = new JPanel(new BorderLayout());
		content.add(playCard, CARD_PLAY);
		
		JPanel confirm = new JPanel(new GridBagLayout());
		JOptionPane option = new JOptionPane("A game is in open.  End this game?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		option.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(!CARD_PLAY_END.equals(currentCard))
					return;
				if(evt.getNewValue().equals(JOptionPane.YES_OPTION)) {
					gamePanel.shutdown();
					try {
						gamePanel.shutdownWait();
					} catch(InterruptedException ie) {
					}
					contentCards.show(content, nextCard);
					currentCard = nextCard;
				}
				if(evt.getNewValue().equals(JOptionPane.NO_OPTION)) {
					contentCards.show(content, CARD_PLAY);
					currentCard = CARD_PLAY;
					playButton.setSelected(true);
				}
				
				((JOptionPane) evt.getSource()).setValue(-1);
			}
		});
		GridBagConstraints cx = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0);
		confirm.add(option, cx);
		content.add(confirm, CARD_PLAY_END);
		
		content.add(new StandaloneModeselectPanel(), CARD_MODESELECT);
		
		netplayCard = new JPanel(new BorderLayout());
		netplayCard.add(netLobby, BorderLayout.SOUTH);
		content.add(netplayCard, CARD_NETPLAY);

		confirm = new JPanel(new GridBagLayout());
		option = new JOptionPane("A netplay game is open.  End this game and disconnect?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		option.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(!CARD_NETPLAY_END.equals(currentCard))
					return;
				if(evt.getNewValue().equals(JOptionPane.YES_OPTION)) {
					gamePanel.shutdown();
					try {
						gamePanel.shutdownWait();
					} catch(InterruptedException ie) {
					}
					netLobby.disconnect();
					contentCards.show(content, nextCard);
					currentCard = nextCard;
				}
				if(evt.getNewValue().equals(JOptionPane.NO_OPTION)) {
					contentCards.show(content, CARD_NETPLAY);
					currentCard = CARD_NETPLAY;
					netplayButton.setSelected(true);
				}
				
				((JOptionPane) evt.getSource()).setValue(-1);
			}
		});
		cx = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0);
		confirm.add(option, cx);
		content.add(confirm, CARD_NETPLAY_END);
		
		StandaloneKeyConfig kc = new StandaloneKeyConfig(this);
		kc.load(0);
		content.add(kc, CARD_KEYS_1P);
		kc = new StandaloneKeyConfig(this);
		kc.load(1);
		content.add(kc, CARD_KEYS_2P);
		
		StandaloneGameTuningPanel gt = new StandaloneGameTuningPanel();
		gt.load(0);
		content.add(gt, CARD_TUNING_1P);
		gt = new StandaloneGameTuningPanel();
		gt.load(1);
		content.add(gt, CARD_TUNING_2P);
		
		StandaloneAISelectPanel ai = new StandaloneAISelectPanel();
		ai.load(0);
		content.add(ai, CARD_AI_1P);
		ai = new StandaloneAISelectPanel();
		ai.load(1);
		content.add(ai, CARD_AI_2P);
		
		StandaloneGeneralConfigPanel gc = new StandaloneGeneralConfigPanel();
		gc.load();
		content.add(gc, CARD_GENERAL);

		JFileChooser fc = new JFileChooser(new ResourceFileSystemView() {
			@Override
			protected String url() {
				return super.url() + "replay/";
			}
			@Override
			public Boolean isTraversable(File f) {
				if(f.getName().endsWith(".rep"))
					return false;
				return super.isTraversable(f);
			}
		});
		fc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
					return;
				JFileChooser fc = (JFileChooser) e.getSource();
				String path = "replay/" + fc.getSelectedFile().getPath();
				startReplayGame(path.replaceAll("/+", "/"));
			}
		});
		content.add(fc, CARD_OPEN_ONLINE);
		
		if(!StandaloneApplet.isApplet()) {
			fc = new JFileChooser(System.getProperty("user.dir") + File.separator + "local-resources" + File.separator + "replay");
			fc.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(!e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
						return;
					JFileChooser fc = (JFileChooser) e.getSource();
					String path = fc.getSelectedFile().getPath();
					startReplayGame(path);
				}
			});
			content.add(fc, CARD_OPEN);
		}
	}
	
	private void playCardSelected() {
		playCard.add(gamePanel, BorderLayout.CENTER);
		playCard.add(musicPanel, BorderLayout.WEST);
		gamePanel.shutdown();
		try {
			gamePanel.shutdownWait();
		} catch(InterruptedException ie) {
		}
		startNewGame();
		gamePanel.displayWindow();
	}
	
	private void netplayCardSelected() {
		netplayCard.add(gamePanel, BorderLayout.CENTER);
		netplayCard.add(musicPanel, BorderLayout.WEST);
		gamePanel.shutdown();
		try {
			gamePanel.shutdownWait();
		} catch(InterruptedException ie) {
		}
		enterNewMode(null);
		gamePanel.displayWindow();
	}
	
	private JToolBar createToolbar() {
		JToolBar t = new JToolBar(JToolBar.VERTICAL);
		t.setFloatable(false);
		t.setLayout(new GridLayout(0, 1));
		
		ButtonGroup g = new ButtonGroup();
		
		AbstractButton b;
		
		b = playButton = new JToggleButton(new ToolbarAction("toolbar.play") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(CARD_PLAY.equals(currentCard))
					return;
				super.actionPerformed(e);
				playCardSelected();
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.modeselect"));
		add(t, g, b);
		
		b = netplayButton = new JToggleButton(new ToolbarAction("toolbar.netplay") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(CARD_NETPLAY.equals(currentCard))
					return;
				super.actionPerformed(e);
				netplayCardSelected();
			}
		});
		add(t, g, b);
		
		/*
		b = new JToggleButton(new ToolbarAction("toolbar.rule_1p"));
		add(t, g, b);
		*/
		
		b = new JToggleButton(new ToolbarAction("toolbar.ai_1p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.keys_1p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.tuning_1p"));
		add(t, g, b);
		
		/*
		b = new JToggleButton(new ToolbarAction("toolbar.rule_2p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.keys_2p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.tuning_2p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.ai_2p"));
		add(t, g, b);
		*/
		
		b = new JToggleButton(new ToolbarAction("toolbar.general"));
		add(t, g, b);
		
		if(!StandaloneApplet.isApplet()) {
			b = new JToggleButton(new ToolbarAction("toolbar.open"));
			add(t, g, b);
		}
		
		b = new JToggleButton(new ToolbarAction("toolbar.open_online"));
		add(t, g, b);
		
		b = new JButton(new ToolbarAction("toolbar.close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		add(t, g, b);
		
		return t;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		StandaloneMain.saveConfig();
		System.exit(0);
	}
	
	private class ToolbarAction extends AbstractAction {
		private String cardName;
		
		public ToolbarAction(String name) {
			super(lz.s(name), icon(name));
			this.cardName = name;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(CARD_PLAY.equals(currentCard) && gamePanel.isInGame[0]) {
				nextCard = cardName;
				contentCards.show(content, CARD_PLAY_END);
				currentCard = CARD_PLAY_END;
				return;
			}
			if(CARD_NETPLAY.equals(currentCard) && netLobby.getClient() != null) {
				nextCard = cardName;
				contentCards.show(content, CARD_NETPLAY_END);
				currentCard = CARD_NETPLAY_END;
				return;
			}
			contentCards.show(content, cardName);
			currentCard = cardName;
		}
	}

	/**
	 * Enter to a new mode in netplay
	 * @param modeName Mode name
	 */
	public void enterNewMode(String modeName) {
		StandaloneMain.loadGlobalConfig();	// Reload global config file

		if(gameManager == null) {
			StandaloneRenderer rendererSwing = new StandaloneRenderer();
			gameManager = new GameManager(rendererSwing);
		}
		
		GameMode previousMode = gameManager.mode;
		GameMode newModeTemp = (modeName == null) ? new AbstractNetMode() : StandaloneMain.modeManager.get(modeName);

		if(newModeTemp == null) {
			log.error("Cannot find a mode:" + modeName);
		} else if(newModeTemp instanceof AbstractNetMode) {
			log.info("Enter new mode:" + newModeTemp.getName());

			AbstractNetMode newMode = (AbstractNetMode)newModeTemp;

			if(previousMode != null) {
				if(gameManager.engine[0].ai != null) {
					gameManager.engine[0].ai.shutdown(gameManager.engine[0], 0);
					gameManager.engine[0].ai = null;
				}
				previousMode.netplayUnload(netLobby);
			}
			
			newMode.netplayInit(netLobby);
			
			gameManager.mode = newMode;
			gameManager.init();

			// Tuning
			gameManager.engine[0].owRotateButtonDefaultRight = StandaloneMain.propConfig.getProperty(0 + ".tuning.owRotateButtonDefaultRight", -1);
			gameManager.engine[0].owSkin = StandaloneMain.propConfig.getProperty(0 + ".tuning.owSkin", -1);
			gameManager.engine[0].owMinDAS = StandaloneMain.propConfig.getProperty(0 + ".tuning.owMinDAS", -1);
			gameManager.engine[0].owMaxDAS = StandaloneMain.propConfig.getProperty(0 + ".tuning.owMaxDAS", -1);
			gameManager.engine[0].owDasDelay = StandaloneMain.propConfig.getProperty(0 + ".tuning.owDasDelay", -1);
			gameManager.engine[0].owReverseUpDown = StandaloneMain.propConfig.getProperty(0 + ".tuning.owReverseUpDown", false);
			gameManager.engine[0].owMoveDiagonal = StandaloneMain.propConfig.getProperty(0 + ".tuning.owMoveDiagonal", -1);
			gameManager.engine[0].owBlockOutlineType = StandaloneMain.propConfig.getProperty(0 + ".tuning.owBlockOutlineType", -1);
			gameManager.engine[0].owBlockShowOutlineOnly = StandaloneMain.propConfig.getProperty(0 + ".tuning.owBlockShowOutlineOnly", -1);

			// Rule
			RuleOptions ruleopt = null;
			String rulename = StandaloneMain.propConfig.getProperty(0 + ".rule", "");
			if(gameManager.mode.getGameStyle() > 0) {
				rulename = StandaloneMain.propConfig.getProperty(0 + ".rule." + gameManager.mode.getGameStyle(), "");
			}
			if((rulename != null) && (rulename.length() > 0)) {
				log.info("Load rule options from " + rulename);
				ruleopt = GeneralUtil.loadRule(rulename);
			} else {
				log.info("Load rule options from setting file");
				ruleopt = new RuleOptions();
				ruleopt.readProperty(StandaloneMain.propConfig, 0);
			}
			gameManager.engine[0].ruleopt = ruleopt;

			// Randomizer
			if((ruleopt.strRandomizer != null) && (ruleopt.strRandomizer.length() > 0)) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer, gameManager.engine[0]);
				gameManager.engine[0].randomizer = randomizerObject;
			}

			// Wallkick
			if((ruleopt.strWallkick != null) && (ruleopt.strWallkick.length() > 0)) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engine[0].wallkick = wallkickObject;
			}

			// AI
			String aiName = StandaloneMain.propConfig.getProperty(0 + ".ai", "");
			if(aiName.length() > 0) {
				AbstractAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engine[0].ai = aiObj;
				gameManager.engine[0].aiMoveDelay = StandaloneMain.propConfig.getProperty(0 + ".aiMoveDelay", 0);
				gameManager.engine[0].aiThinkDelay = StandaloneMain.propConfig.getProperty(0 + ".aiThinkDelay", 0);
				gameManager.engine[0].aiUseThread = StandaloneMain.propConfig.getProperty(0 + ".aiUseThread", true);
				gameManager.engine[0].aiShowHint = StandaloneMain.propConfig.getProperty(0+".aiShowHint", false);
				gameManager.engine[0].aiPrethink = StandaloneMain.propConfig.getProperty(0+".aiPrethink", false);
				gameManager.engine[0].aiShowState = StandaloneMain.propConfig.getProperty(0+".aiShowState", false);
			}
			gameManager.showInput = StandaloneMain.propConfig.getProperty("option.showInput", false);

			// Initialization for each player
			for(int i = 0; i < gameManager.getPlayers(); i++) {
				gameManager.engine[i].init();
			}

			
		} else {
			log.error("This mode does not support netplay:" + modeName);
		}

/*
		if(gameFrame != null) gameFrame.updateTitleBarCaption();
*/
	}


	/**
	 * Start a new game (Rule will be user-selected one))
	 */
	public void startNewGame() {
		startNewGame(null);
	}

	/**
	 * Start a new game
	 * @param strRulePath Rule file path (null if you want to use user-selected one)
	 */
	public void startNewGame(String strRulePath) {
		StandaloneRenderer rendererSwing = new StandaloneRenderer();
		gameManager = new GameManager(rendererSwing);

		// Mode
		String modeName = StandaloneMain.propConfig.getProperty("name.mode", "");
		GameMode modeObj = StandaloneMain.modeManager.get(modeName);
		if(modeObj == null) {
			log.error("Couldn't find mode:" + modeName);
			gameManager.mode = new MarathonMode();
		} else {
			gameManager.mode = modeObj;
		}

		gameManager.init();

		// Initialization for each player
		for(int i = 0; i < gameManager.getPlayers(); i++) {
			// Tuning settings
			gameManager.engine[i].owRotateButtonDefaultRight = StandaloneMain.propConfig.getProperty(i + ".tuning.owRotateButtonDefaultRight", -1);
			gameManager.engine[i].owSkin = StandaloneMain.propConfig.getProperty(i + ".tuning.owSkin", -1);
			gameManager.engine[i].owMinDAS = StandaloneMain.propConfig.getProperty(i + ".tuning.owMinDAS", -1);
			gameManager.engine[i].owMaxDAS = StandaloneMain.propConfig.getProperty(i + ".tuning.owMaxDAS", -1);
			gameManager.engine[i].owDasDelay = StandaloneMain.propConfig.getProperty(i + ".tuning.owDasDelay", -1);
			gameManager.engine[i].owReverseUpDown = StandaloneMain.propConfig.getProperty(i + ".tuning.owReverseUpDown", false);
			gameManager.engine[i].owMoveDiagonal = StandaloneMain.propConfig.getProperty(i + ".tuning.owMoveDiagonal", -1);
			gameManager.engine[i].owBlockOutlineType = StandaloneMain.propConfig.getProperty(i + ".tuning.owBlockOutlineType", -1);
			gameManager.engine[i].owBlockShowOutlineOnly = StandaloneMain.propConfig.getProperty(i + ".tuning.owBlockShowOutlineOnly", -1);

			// Rule
			RuleOptions ruleopt = null;
			String rulename = strRulePath;
			if(rulename == null) {
				rulename = StandaloneMain.propConfig.getProperty(i + ".rule", "");
				if(gameManager.mode.getGameStyle() > 0) {
					rulename = StandaloneMain.propConfig.getProperty(i + ".rule." + gameManager.mode.getGameStyle(), "");
				}
			}
			if((rulename != null) && (rulename.length() > 0)) {
				log.debug("Load rule options from " + rulename);
				ruleopt = GeneralUtil.loadRule(rulename);
			} else {
				log.debug("Load rule options from setting file");
				ruleopt = new RuleOptions();
				ruleopt.readProperty(StandaloneMain.propConfig, i);
			}
			gameManager.engine[i].ruleopt = ruleopt;

			// NEXTOrder generation algorithm
			if((ruleopt.strRandomizer != null) && (ruleopt.strRandomizer.length() > 0)) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer, gameManager.engine[i]);
				gameManager.engine[i].randomizer = randomizerObject;
			}

			// Wallkick
			if((ruleopt.strWallkick != null) && (ruleopt.strWallkick.length() > 0)) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engine[i].wallkick = wallkickObject;
			}

			// AI
			String aiName = StandaloneMain.propConfig.getProperty(i + ".ai", "");
			if(aiName.length() > 0) {
				AbstractAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engine[i].ai = aiObj;
				gameManager.engine[i].aiMoveDelay = StandaloneMain.propConfig.getProperty(i + ".aiMoveDelay", 0);
				gameManager.engine[i].aiThinkDelay = StandaloneMain.propConfig.getProperty(i + ".aiThinkDelay", 0);
				gameManager.engine[i].aiUseThread = StandaloneMain.propConfig.getProperty(i + ".aiUseThread", true);
				gameManager.engine[i].aiShowHint = StandaloneMain.propConfig.getProperty(i+".aiShowHint", false);
				gameManager.engine[i].aiPrethink = StandaloneMain.propConfig.getProperty(i+".aiPrethink", false);
				gameManager.engine[i].aiShowState = StandaloneMain.propConfig.getProperty(i+".aiShowState", false);
			}
			gameManager.showInput = StandaloneMain.propConfig.getProperty("option.showInput", false);

			// Called at initialization
			gameManager.engine[i].init();
		}
	}

	public void startReplayGame(String filename) {
		contentCards.show(content, CARD_PLAY);
		playCard.add(gamePanel, BorderLayout.CENTER);
		gamePanel.shutdown();
		try {
			gamePanel.shutdownWait();
		} catch(InterruptedException ie) {
		}
		startNewGame();
		gamePanel.displayWindow();

		log.info("Loading Replay:" + filename);
		CustomProperties prop = new CustomProperties();

		try {
			ResourceInputStream stream = new ResourceInputStream(filename);
			prop.load(stream);
			stream.close();
		} catch (IOException e) {
			log.error("Couldn't load replay file from " + filename, e);
			return;
		}

		StandaloneRenderer rendererSwing = new StandaloneRenderer();
		gameManager = new GameManager(rendererSwing);
		gameManager.replayMode = true;
		gameManager.replayProp = prop;

		// Mode
		String modeName = prop.getProperty("name.mode", "");
		GameMode modeObj = StandaloneMain.modeManager.get(modeName);
		if(modeObj == null) {
			log.error("Couldn't find mode:" + modeName);
		} else {
			gameManager.mode = modeObj;
		}

		gameManager.init();

		// Initialization for each player
		for(int i = 0; i < gameManager.getPlayers(); i++) {
			// Rule
			RuleOptions ruleopt = new RuleOptions();
			ruleopt.readProperty(prop, i);
			gameManager.engine[i].ruleopt = ruleopt;

			// NEXTOrder generation algorithm
			if((ruleopt.strRandomizer != null) && (ruleopt.strRandomizer.length() > 0)) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer, gameManager.engine[i]);
				gameManager.engine[i].randomizer = randomizerObject;
			}

			// Wallkick
			if((ruleopt.strWallkick != null) && (ruleopt.strWallkick.length() > 0)) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engine[i].wallkick = wallkickObject;
			}

			// AI (For added replay)
			String aiName = StandaloneMain.propConfig.getProperty(i + ".ai", "");
			if(aiName.length() > 0) {
				AbstractAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engine[i].ai = aiObj;
				gameManager.engine[i].aiMoveDelay = StandaloneMain.propConfig.getProperty(i + ".aiMoveDelay", 0);
				gameManager.engine[i].aiThinkDelay = StandaloneMain.propConfig.getProperty(i + ".aiThinkDelay", 0);
				gameManager.engine[i].aiUseThread = StandaloneMain.propConfig.getProperty(i + ".aiUseThread", true);
				gameManager.engine[i].aiShowHint = StandaloneMain.propConfig.getProperty(i+".aiShowHint", false);
				gameManager.engine[i].aiPrethink = StandaloneMain.propConfig.getProperty(i+".aiPrethink", false);
				gameManager.engine[i].aiShowState = StandaloneMain.propConfig.getProperty(i+".aiShowState", false);
			}
			gameManager.showInput = StandaloneMain.propConfig.getProperty("option.showInput", false);

			// Called at initialization
			gameManager.engine[i].init();
		}
	}

}
