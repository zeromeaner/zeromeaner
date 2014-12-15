package org.zeromeaner.gui.reskin;

import static org.zeromeaner.util.Options.player;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;

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
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;
import org.zeromeaner.util.Localization;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.Session;
import org.zeromeaner.util.Options.AIOptions;
import org.zeromeaner.util.Options.TuningOptions;
import org.zeromeaner.util.io.DavFileSystemView;
import org.zeromeaner.util.io.FileSystemViews;
import org.zeromeaner.util.ResourceInputStream;
import org.zeromeaner.util.RuleList;

public class StandaloneFrame extends JFrame {
	private static final Logger log = Logger.getLogger(StandaloneFrame.class);
	private static final Localization lz = new Localization();
	
	private static Icon icon(String name) {
		URL url = StandaloneFrame.class.getResource(name + ".png");
		return url == null ? null : new ImageIcon(url);
	}
	
	public static final String CARD_INTRO = "toolbar.intro";
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
	
	private StandaloneLicensePanel introPanel;
	
	private JPanel playCard;
	private JPanel netplayCard;

	private JToggleButton playButton;
	private JToggleButton netplayButton;
	
	KNetPanel netLobby;
	volatile GameManager gameManager;
	private StandaloneGamePanel gamePanel;
	
	public StandaloneFrame() {
		setTitle("zeromeaner");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		
		setLayout(new BorderLayout());
		add(toolbar = createToolbar(), BorderLayout.EAST);
		add(content = new JPanel(contentCards = new CardLayout()), BorderLayout.CENTER);

		netLobby = new KNetPanel(Session.getUser(), false);
		netLobby.setPreferredSize(new Dimension(800, 250));
		netLobby.addKNetPanelListener(new KNetPanelAdapter() {
			@Override
			public void knetPanelShutdown(KNetPanelEvent e) {
				content.remove(netLobby);
				content.revalidate();
				content.repaint();
			}
			@Override
			public void knetPanelJoined(KNetPanelEvent e) {
				if(e.getChannel().getId() != KNetChannelInfo.LOBBY_CHANNEL_ID && e.getSource().getClient().getCurrentChannel() != null) {
					startNewGame(e.getChannel().getRule().resourceName, null, e.getChannel().getMode(), e);
					gamePanel.displayWindow();
				}
			}
			
			@Override
			public void knetPanelParted(KNetPanelEvent e) {
				gamePanel.shutdown();
				try {
					gamePanel.shutdownWait();
				} catch(InterruptedException ex) {
				}
			}
		});
		
		gamePanel = new StandaloneGamePanel(this);
		
		createCards();
		
	}
	
	private static void add(JToolBar toolbar, ButtonGroup g, AbstractButton b) {
		b.setFocusable(false);
		b.setBorder(null);
		b.setHorizontalAlignment(SwingConstants.LEFT);
		toolbar.add(b);
		g.add(b);
	}
	
	private void createCards() {
		introPanel = new StandaloneLicensePanel();
		content.add(introPanel, CARD_INTRO);
		
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

		final JFileChooser fc;
		
		FileSystemView fsv = FileSystemViews.get().fileSystemView("replay/");
		if(fsv instanceof DavFileSystemView)
			fc = new JFileChooser("", fsv);
		else
			fc = new JFileChooser(new File(System.getProperty("user.dir"), "local-resources/org/zeromeaner/replay"), fsv);
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
		fc.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				fc.rescanCurrentDirectory();
			}
		});
		content.add(fc, CARD_OPEN);
	}
	
	private void playCardSelected() {
		playCard.add(gamePanel, BorderLayout.CENTER);
//		playCard.add(musicPanel, BorderLayout.WEST);
		gamePanel.shutdown();
		try {
			gamePanel.shutdownWait();
		} catch(InterruptedException ie) {
		}
		gamePanel.modeToEnter.offer("");
		startNewGame();
		gamePanel.displayWindow();
	}
	
	private void netplayCardSelected() {
		netplayCard.add(gamePanel, BorderLayout.CENTER);
//		netplayCard.add(musicPanel, BorderLayout.WEST);
		gamePanel.shutdown();
		try {
			gamePanel.shutdownWait();
		} catch(InterruptedException ie) {
		}
//		enterNewMode(null);
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
				super.actionPerformed(e);
				if(CARD_PLAY.equals(currentCard))
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
		
		b = new JToggleButton(new ToolbarAction("toolbar.keys_1p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.tuning_1p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.ai_1p"));
		add(t, g, b);
		
		/*
		b = new JToggleButton(new ToolbarAction("toolbar.rule_2p"));
		add(t, g, b);
		 */
		
		b = new JToggleButton(new ToolbarAction("toolbar.keys_2p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.tuning_2p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.ai_2p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.general"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.open"));
		add(t, g, b);
		
		b = new JButton(new ToolbarAction("toolbar.close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				StandaloneMain.saveConfig();
				System.exit(0);
			}
		});
		add(t, g, b);
		
		return t;
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
	 * Start a new game (Rule will be user-selected one))
	 */
	public void startNewGame() {
		startNewGame(null, null, null, null);
	}

	/**
	 * Start a new game
	 * @param strRulePath Rule file path (null if you want to use user-selected one)
	 */
	public void startNewGame(String strRulePath, String replayPath, String modeName, KNetPanelEvent e) {
//		if(gameManager == null) {
		StandaloneRenderer rendererSwing = new StandaloneRenderer(this);
		gameManager = new GameManager(rendererSwing);
//		}

		boolean isNetPlay = (modeName != null);
		
		// Mode
		if(modeName == null)
			modeName = Options.general().MODE_NAME.value();
		
		if(replayPath != null) {
			log.info("Loading Replay:" + replayPath);
			CustomProperties prop = new CustomProperties();

			try {
				ResourceInputStream stream = new ResourceInputStream(replayPath);
				prop.load(stream);
				stream.close();
			} catch (IOException ex) {
				log.error("Couldn't load replay file from " + replayPath, ex);
				return;
			}

			gameManager.replayMode = true;
			gameManager.replayProp = prop;

			// Mode
			modeName = prop.getProperty("name.mode", "");
		}
		
		GameMode modeObj = StandaloneMain.modeManager.get(modeName);
		if(modeObj == null) {
			log.error("Couldn't find mode:" + modeName);
			gameManager.mode = new MarathonMode();
		} else {
			gameManager.mode = modeObj;
			gamePanel.modeToEnter.offer(modeName);
		}

		if(e != null) {
			((AbstractNetMode) modeObj).setKnetPanel(e.getSource());
		}
		
		gameManager.init();

		if(e != null) {
			((AbstractNetMode) modeObj).netplayInit(e.getSource());
			((AbstractNetMode) modeObj).netOnJoin(e.getClient(), e.getChannel());
		}
		
		// Initialization for each player
		for(int i = 0; i < gameManager.getPlayers(); i++) {
			// Tuning settings
			TuningOptions tune = player(i).tuning;
			gameManager.engine[i].owRotateButtonDefaultRight = tune.ROTATE_BUTTON_DEFAULT_RIGHT.value();
			gameManager.engine[i].owSkin = tune.SKIN.value();
			gameManager.engine[i].owMinDAS = tune.MIN_DAS.value();
			gameManager.engine[i].owMaxDAS = tune.MAX_DAS.value();
			gameManager.engine[i].owDasDelay = tune.DAS_DELAY.value();
			gameManager.engine[i].owReverseUpDown = tune.REVERSE_UP_DOWN.value();
			gameManager.engine[i].owMoveDiagonal = tune.MOVE_DIAGONAL.value();
			gameManager.engine[i].owBlockOutlineType = tune.BLOCK_OUTLINE_TYPE.value();
			gameManager.engine[i].owBlockShowOutlineOnly = tune.BLOCK_SHOW_OUTLINE_ONLY.value();

			// Rule
			RuleOptions ruleopt = null;
			String rulename = strRulePath;
			if(gameManager.replayMode) {
				rulename = gameManager.replayProp.getProperty(i + ".ruleopt.strRuleName");
				ruleopt = RuleList.getRules().getNamed(rulename);
				ruleopt.readProperty(gameManager.replayProp, i);
			}
			if(rulename == null) {
//				rulename = StandaloneMain.propConfig.getProperty(i + ".rule", "");
				rulename = player(i).RULE_NAME.value();
				if(gameManager.mode.getGameStyle() > 0) {
//					rulename = StandaloneMain.propConfig.getProperty(i + ".rule." + gameManager.mode.getGameStyle(), "");
					rulename = player(i).RULE_NAME_FOR_STYLE(gameManager.mode.getGameStyle()).value();
				}
			}
			if((rulename != null) && (rulename.length() > 0)) {
				if(ruleopt == null) {
					log.debug("Load rule options from " + rulename);
					ruleopt = GeneralUtil.loadRule(rulename);
				}
			} else {
				log.debug("Load rule options from setting file");
				ruleopt = new RuleOptions();
				ruleopt.readProperty(Options.GLOBAL_PROPERTIES, i);
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
			if(!gameManager.replayMode && !isNetPlay) {
				AIOptions ai = Options.player(i).ai;
				String aiName = ai.NAME.value();
				if(aiName.length() > 0 && replayPath == null) {
					AbstractAI aiObj = GeneralUtil.loadAIPlayer(aiName);
					gameManager.engine[i].ai = aiObj;
					gameManager.engine[i].aiMoveDelay = ai.MOVE_DELAY.value();
					gameManager.engine[i].aiThinkDelay = ai.THINK_DELAY.value();
					gameManager.engine[i].aiUseThread = ai.USE_THREAD.value();
					gameManager.engine[i].aiShowHint = ai.SHOW_HINT.value();
					gameManager.engine[i].aiPrethink = ai.PRETHINK.value();
					gameManager.engine[i].aiShowState = ai.SHOW_STATE.value();
				}
				gameManager.showInput = Options.standalone().SHOW_INPUT.value();
			}

			// Called at initialization
			gameManager.engine[i].init();
		}
		
		gamePanel.isNetPlay = isNetPlay;
	}

	public void startReplayGame(String filename) {
		contentCards.show(content, currentCard = CARD_PLAY);
		playCard.add(gamePanel, BorderLayout.CENTER);
		gamePanel.shutdown();
		try {
			gamePanel.shutdownWait();
		} catch(InterruptedException ie) {
		}
		startNewGame(null, filename, null, null);
		gamePanel.displayWindow();
	}

}
