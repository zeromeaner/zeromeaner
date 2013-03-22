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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.zeromeaner.contrib.net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.game.subsystem.ai.AbstractAI;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.game.subsystem.mode.AbstractNetMode;
import org.zeromeaner.game.subsystem.wallkick.Wallkick;
import org.zeromeaner.gui.knet.KNetPanelEvent;
import org.zeromeaner.gui.knet.KNetPanelListener;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.ResourceInputStream.ResourceDownloadStream;
import org.zeromeaner.util.ResourceFileSystemView;
import org.zeromeaner.util.ResourceOutputStream;
import org.zeromeaner.util.ResourceInputStream;
import org.zeromeaner.util.GeneralUtil;
import org.zeromeaner.util.ModeManager;

/**
 * zeromeaner SwingVersion
 */
public class NullpoMinoInternalFrame extends JInternalFrame implements ActionListener, KNetPanelListener {
	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/** Log */
	static Logger log = Logger.getLogger(NullpoMinoInternalFrame.class);

	/** Of the main window frame */
	public static NullpoMinoInternalFrame mainFrame;

	/** Of the game window frame */
	public static GameInternalFrame gameFrame;

	/** Key Configuration screen frame */
	public static KeyConfigInternalFrame keyConfigFrame;

	/** Rules of selection screen frame */
	public static RuleSelectInternalFrame ruleSelectFrame;

	/** AISelection screen frame */
	public static AISelectInternalFrame aiSelectFrame;

	/** Other Settings screen frame */
	public static GeneralConfigInternalFrame generalConfigFrame;

	/** Tuning Settings screen frame */
	public static GameTuningInternalFrame gameTuningFrame;

	/** Command that was passed to the programLinesArgumentcount */
	public static String[] programArgs;

	/** Save settingsUseProperty file */
	public static CustomProperties propConfig;

	/** Save settingsUseProperty file (AllVersionCommon) */
	public static CustomProperties propGlobal;

	/** ObserverFor the functionProperty file */
	public static CustomProperties propObserver;

	/** Default language file */
	public static CustomProperties propLangDefault;

	/** Language file */
	public static CustomProperties propLang;

	/** Default game mode description file */
	public static CustomProperties propDefaultModeDesc;

	/** Game mode description file */
	public static CustomProperties propModeDesc;

	/** Mode Management */
	public static ModeManager modeManager;

	/** Layout manager of the main screen */
	public static CardLayout mainLayout;

	/** The main class of the game */
	public static GameManager gameManager;

	/** Game event Processing and rendering process */
	public static RendererApplet rendererSwing;

	/** GameMode nameAn array of */
	public static String[] modeList;

	/** Mode Selection list box */
	public static JList listboxMode;

	/** Rule select listmodel */
	public static DefaultListModel listmodelRule;

	/** Rule select listbox */
	public static JList listboxRule;

	/** Replay file selection dialog */
	public static JFileChooser replayFileChooser;

	/** Lobby screen */
	public static NetLobbyInternalFrame netLobby;

	/** Mode Select the on-screen label(NewVersionIf there is it switches) */
	public static JLabel lModeSelect;

	/** HashMap of rules (ModeName->RuleEntry) */
	protected HashMap<String, RuleEntry> mapRuleEntries;

	/**
	 * Main functioncount
	 * @param args Command that was passed to the programLinesArgumentcount
	 */
	public static void main(String[] args) {
		programArgs = args;
		try {
			PropertyConfigurator.configure(new ResourceInputStream("config/etc/log_swing.cfg"));
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		log.debug("NullpoMinoSwing Start");

		// Read configuration file
		propConfig = new CustomProperties();
		try {
			ResourceInputStream in = new ResourceInputStream("config/setting/swing.cfg");
			propConfig.load(in);
			in.close();
		} catch(IOException e) {}

		propGlobal = new CustomProperties();
		loadGlobalConfig();

		// ModeRead
		modeManager = new ModeManager();
		try {
			BufferedReader txtMode = new BufferedReader(new InputStreamReader(new ResourceInputStream("config/list/mode.lst")));
			modeManager.loadGameModes(txtMode);
			txtMode.close();
			modeList = modeManager.getModeNames(false);
		} catch (IOException e) {
			log.error("Mode list load failed", e);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw, true));
			JOptionPane.showMessageDialog(null, sw);
		}

		// Read language file
		propLangDefault = new CustomProperties();
		try {
			ResourceInputStream in = new ResourceInputStream("config/lang/swing_default.properties");
			propLangDefault.load(in);
			in.close();
		} catch (IOException e) {
			log.error("Couldn't load default UI language file", e);
		}

		propLang = new CustomProperties();
		try {
			ResourceInputStream in = new ResourceInputStream("config/lang/swing_" + Locale.getDefault().getCountry() + ".properties");
			propLang.load(in);
			in.close();
		} catch(IOException e) {}

		// Game mode description
		propDefaultModeDesc = new CustomProperties();
		try {
			ResourceInputStream in = new ResourceInputStream("config/lang/modedesc_default.properties");
			propDefaultModeDesc.load(in);
			in.close();
		} catch(IOException e) {
			log.error("Couldn't load default mode description file", e);
		}

		propModeDesc = new CustomProperties();
		try {
			ResourceInputStream in = new ResourceInputStream("config/lang/modedesc_" + Locale.getDefault().getCountry() + ".properties");
			propModeDesc.load(in);
			in.close();
		} catch(IOException e) {}

		// Set default rule selections
		try {
			CustomProperties propDefaultRule = new CustomProperties();
			ResourceInputStream in = new ResourceInputStream("config/list/global_defaultrule.properties");
			propDefaultRule.load(in);
			in.close();

			for(int pl = 0; pl < 2; pl++)
				for(int i = 0; i < GameEngine.MAX_GAMESTYLE; i++) {
					// TETROMINO
					if(i == 0) {
						if(propGlobal.getProperty(pl + ".rule") == null) {
							propGlobal.setProperty(pl + ".rule", propDefaultRule.getProperty("default.rule", ""));
							propGlobal.setProperty(pl + ".rulefile", propDefaultRule.getProperty("default.rulefile", ""));
							propGlobal.setProperty(pl + ".rulename", propDefaultRule.getProperty("default.rulename", ""));
						}
					}
					// etc
					else {
						if(propGlobal.getProperty(pl + ".rule." + i) == null) {
							propGlobal.setProperty(pl + ".rule." + i, propDefaultRule.getProperty("default.rule." + i, ""));
							propGlobal.setProperty(pl + ".rulefile." + i, propDefaultRule.getProperty("default.rulefile." + i, ""));
							propGlobal.setProperty(pl + ".rulename." + i, propDefaultRule.getProperty("default.rulename." + i, ""));
						}
					}
				}
		} catch (Exception e) {}

		// Load keyboard settings
		GameKeyApplet.initGlobalGameKeySwing();
		GameKeyApplet.gamekey[0].loadConfig(propConfig);
		GameKeyApplet.gamekey[1].loadConfig(propConfig);

		// Look&Feel
		if(propConfig.getProperty("option.usenativelookandfeel", true) == true) {
			try {
				UIManager.getInstalledLookAndFeels();
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch(Exception e) {
				log.warn("Failed to set native look&feel", e);
			}
		}

		// Load images
		try {
			ResourceHolderApplet.load();
		} catch (Exception e) {
			log.error("Image load failed", e);
		}

		// First run?
		if(propConfig.getProperty("option.firstSetupMode", true) == true) {
			// Set various default settings here
			GameKeyApplet.gamekey[0].loadDefaultKeymap();
			GameKeyApplet.gamekey[0].saveConfig(propConfig);
			propConfig.setProperty("option.firstSetupMode", false);

			// Set default rotation button setting (only for first run)
			if(propGlobal.getProperty("global.firstSetupMode", true) == true) {
				for(int pl = 0; pl < 2; pl++) {
					if(propGlobal.getProperty(pl + ".tuning.owRotateButtonDefaultRight") == null) {
						propGlobal.setProperty(pl + ".tuning.owRotateButtonDefaultRight", 0);
					}
				}
				propGlobal.setProperty("global.firstSetupMode", false);
			}

			// Save settings
			saveConfig();
		}

		// Create and display main window
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				mainFrame = new NullpoMinoInternalFrame();
//			}
//		});
	}

	/**
	 * PosttranslationalUIGets a string of
	 * @param str String
	 * @return PosttranslationalUIString (If you do not acceptstrReturns)
	 */
	public static String getUIText(String str) {
		String result = propLang.getProperty(str);
		if(result == null) {
			result = propLangDefault.getProperty(str, str);
		}
		return result;
	}

	/**
	 * Save the configuration file
	 */
	public static void saveConfig() {
		try {
			ResourceOutputStream out = new ResourceOutputStream("config/setting/swing.cfg");
			propConfig.store(out, "zeromeaner Swing-frontend Config");
			out.close();
		} catch(IOException e) {
			log.error("Failed to save Swing-specific config", e);
		}

		try {
			ResourceOutputStream out = new ResourceOutputStream("config/setting/global.cfg");
			propGlobal.store(out, "zeromeaner Global Config");
			out.close();
		} catch(IOException e) {
			log.error("Failed to save global config", e);
		}
		
		try {
			log.info("Committing config cache");
			ResourceDownloadStream.commitCache();
		} catch(IOException e) {
			log.error("Unable to save config cache", e);
		}
	}

	/**
	 * (Re-)Load global config file
	 */
	public static void loadGlobalConfig() {
		try {
			ResourceInputStream in = new ResourceInputStream("config/setting/global.cfg");
			propGlobal.load(in);
			in.close();
		} catch(IOException e) {}
	}

	/**
	 * TextfieldFromintGets the value of the type
	 * @param value TextfieldValue when Failed to get the value from
	 * @param txtfld Textfield
	 * @return TextfieldIf you can get the value from its value, FailedvalueReturns the raw
	 */
	public static int getIntTextField(int value, JTextField txtfld) {
		int v = value;

		try {
			v = Integer.parseInt(txtfld.getText());
		} catch(NumberFormatException e) {}

		return v;
	}

	/**
	 * TextfieldFromdoubleGets the value of the type
	 * @param value TextfieldValue when Failed to get the value from
	 * @param txtfld Textfield
	 * @return TextfieldIf you can get the value from its value, FailedvalueReturns the raw
	 */
	public static double getDoubleTextField(double value, JTextField txtfld) {
		double v = value;

		try {
			v = Double.parseDouble(txtfld.getText());
		} catch(NumberFormatException e) {}

		return v;
	}

	/**
	 * TextfieldFromfloatGets the value of the type
	 * @param value TextfieldValue when Failed to get the value from
	 * @param txtfld Textfield
	 * @return TextfieldIf you can get the value from its value, FailedvalueReturns the raw
	 */
	public static float getFloatTextField(float value, JTextField txtfld) {
		float v = value;

		try {
			v = Float.parseFloat(txtfld.getText());
		} catch(NumberFormatException e) {}

		return v;
	}

	/**
	 * Get game mode description
	 * @param str Mode name
	 * @return Description
	 */
	protected static String getModeDesc(String str) {
		String str2 = str.replace(' ', '_');
		str2 = str2.replace('(', 'l');
		str2 = str2.replace(')', 'r');
		String result = propModeDesc.getProperty(str2);
		if(result == null) {
			result = propDefaultModeDesc.getProperty(str2, str2);
		}
		return result;
	}

	/**
	 * Constructor
	 * @throws HeadlessException Keyboard, Mouse, Exceptions such as the display if there is no
	 */
	public NullpoMinoInternalFrame() throws HeadlessException {
		super();

		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				shutdown();
			}
		});

		setTitle(getUIText("Title_Main") + " version" + GameManager.getVersionString());
		loadRecommendedRuleList();

		initUI();
		pack();

		if(propConfig.getProperty("mainwindow.width") != null)
			this.setSize(propConfig.getProperty("mainwindow.width", 500), propConfig.getProperty("mainwindow.height", 470));
		if(propConfig.getProperty("mainwindow.x") != null)
			this.setLocation(propConfig.getProperty("mainwindow.x", 0), propConfig.getProperty("mainwindow.y", 0));

		setVisible(true);

		// NewVersion check
		if(propGlobal.getProperty("updatechecker.enable", true)) {
			int startupCount = propGlobal.getProperty("updatechecker.startupCount", 0);
			int startupMax = propGlobal.getProperty("updatechecker.startupMax", 20);

			if(startupCount >= startupMax) {
				String strURL = propGlobal.getProperty("updatechecker.url", "");
				startupCount = 0;
			} else {
				startupCount++;
			}

			if(startupMax >= 1) {
				propGlobal.setProperty("updatechecker.startupCount", startupCount);
				saveConfig();
			}
		}

		// CommandLinesReplay from reproduction
		if((programArgs != null) && (programArgs.length > 0)) {
			startReplayGame(programArgs[0]);
		}
		AppletMain.instance.desktop.add(this);
	}

	/**
	 * GUIOfInitialization
	 */
	protected void initUI() {
		mainLayout = new CardLayout();
		this.setLayout(mainLayout);

		// Top screen
		JPanel panelTop = new JPanel();
		initTopScreenUI(panelTop);
		this.add(panelTop, "top");
	}

	/**
	 * Init top screen
	 */
	protected void initTopScreenUI(JComponent p) {
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		// Label
		lModeSelect = new JLabel(getUIText("Top_ModeSelect"));
		lModeSelect.setAlignmentX(0f);
		p.add(lModeSelect);

		// Mode & rule select panel
		JPanel subpanelModeSelect = new JPanel(new BorderLayout());
		subpanelModeSelect.setBorder(new EtchedBorder());
		subpanelModeSelect.setAlignmentX(0f);
		p.add(subpanelModeSelect);

		// * Mode select listbox
		listboxMode = new JList(modeList);
		listboxMode.addMouseListener(new ListboxModeMouseAdapter());
		listboxMode.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String strMode = (String)listboxMode.getSelectedValue();
				lModeSelect.setText(getModeDesc(strMode));
				prepareRuleList(strMode);
			}
		});
		JScrollPane scpaneListboxMode = new JScrollPane(listboxMode);
		scpaneListboxMode.setPreferredSize(new Dimension(280, 375));
		subpanelModeSelect.add(scpaneListboxMode, BorderLayout.WEST);

		// * Rule select listbox
		listmodelRule = new DefaultListModel();
		listboxRule = new JList(listmodelRule);
		listboxRule.addMouseListener(new ListboxModeMouseAdapter());
		JScrollPane scpaneListBoxRule = new JScrollPane(listboxRule);
		scpaneListBoxRule.setPreferredSize(new Dimension(150, 375));
		subpanelModeSelect.add(scpaneListBoxRule, BorderLayout.CENTER);

		// * Set default selected index
		listboxMode.setSelectedValue(propGlobal.getProperty("name.mode", ""), true);
		if(listboxMode.getSelectedIndex() == -1) listboxMode.setSelectedIndex(0);
		prepareRuleList((String)listboxMode.getSelectedValue());

		// Start button
		JButton buttonStartOffline = new JButton(getUIText("Top_StartOffline"));
		buttonStartOffline.setMnemonic('S');
		buttonStartOffline.addActionListener(this);
		buttonStartOffline.setActionCommand("Top_StartOffline");
		buttonStartOffline.setAlignmentX(0f);
		buttonStartOffline.setMaximumSize(new Dimension(Short.MAX_VALUE, buttonStartOffline.getMaximumSize().height));
		p.add(buttonStartOffline);
		this.getRootPane().setDefaultButton(buttonStartOffline);

		// Menu
		initMenu();
	}

	/**
	 * Menu OfInitialization
	 */
	protected void initMenu() {
		JMenuBar menubar = new JMenuBar();
		this.setJMenuBar(menubar);

		JMenu configMenu = new JMenu("User");
		JMenuItem setConfigId = new JMenuItem(new AbstractAction("Set Config ID") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String userId = (String) JOptionPane.showInternalInputDialog(NullpoMinoInternalFrame.this, "Enter Config ID", "Enter Config ID", JOptionPane.QUESTION_MESSAGE, null, null, AppletMain.userId);
				if(userId != null)
					CookieAccess.set("userId", AppletMain.userId = userId);
			}
		});
		JMenuItem saveConfig = new JMenuItem(new AbstractAction("Save Config") {
			@Override
			public void actionPerformed(ActionEvent e) {
				while("default".equals(AppletMain.userId)) {
					String userId = (String) JOptionPane.showInternalInputDialog(NullpoMinoInternalFrame.this, "Enter Config ID", "Enter Config ID", JOptionPane.QUESTION_MESSAGE, null, null, AppletMain.userId);
					if(userId != null)
						CookieAccess.set("userId", AppletMain.userId = userId);
					else
						return;
				}
				saveConfig();
			}
		});
		configMenu.add(setConfigId);
		configMenu.add(saveConfig);
		menubar.add(configMenu);
		
		// FileMenu
		JMenu menuFile = new JMenu(getUIText("Menu_File"));
		menuFile.setMnemonic('F');
		menubar.add(menuFile);

		// Open the replay
		JMenuItem miOpen = new JMenuItem(getUIText("Menu_Open"));
		miOpen.setMnemonic('O');
		miOpen.addActionListener(this);
		miOpen.setActionCommand("Menu_Open");
		menuFile.add(miOpen);

		if(GameManager.DEV_BUILD) {
			// NetPlay start
			JMenuItem miNetPlay = new JMenuItem(getUIText("Menu_NetPlay"));
			miNetPlay.setMnemonic('N');
			miNetPlay.addActionListener(this);
			miNetPlay.setActionCommand("Menu_NetPlay");
			menuFile.add(miNetPlay);
		}

		// End
//		JMenuItem miExit = new JMenuItem(getUIText("Menu_Exit"));
//		miExit.setMnemonic('X');
//		miExit.addActionListener(this);
//		miExit.setActionCommand("Menu_Exit");
//		menuFile.add(miExit);

		// SettingMenu
		JMenu menuConfig = new JMenu(getUIText("Menu_Config"));
		menuConfig.setMnemonic('C');
		menubar.add(menuConfig);

		// Selection rules
		JMenuItem miRuleSelect = new JMenuItem(getUIText("Menu_RuleSelect"));
		miRuleSelect.setMnemonic('R');
		miRuleSelect.addActionListener(this);
		miRuleSelect.setActionCommand("Menu_RuleSelect");
		menuConfig.add(miRuleSelect);

		// Selection rules(2P)
		JMenuItem miRuleSelect2P = new JMenuItem(getUIText("Menu_RuleSelect2P"));
		miRuleSelect2P.setMnemonic('S');
		miRuleSelect2P.addActionListener(this);
		miRuleSelect2P.setActionCommand("Menu_RuleSelect2P");
		menuConfig.add(miRuleSelect2P);

		// Tuning settings
		JMenuItem miGameTuning = new JMenuItem(getUIText("Menu_GameTuning"));
		miGameTuning.setMnemonic('T');
		miGameTuning.addActionListener(this);
		miGameTuning.setActionCommand("Menu_GameTuning");
		menuConfig.add(miGameTuning);

		// Tuning settings(2P)
		JMenuItem miGameTuning2P = new JMenuItem(getUIText("Menu_GameTuning2P"));
		miGameTuning2P.setMnemonic('U');
		miGameTuning2P.addActionListener(this);
		miGameTuning2P.setActionCommand("Menu_GameTuning2P");
		menuConfig.add(miGameTuning2P);

		// AISetting
		JMenuItem miAIConfig = new JMenuItem(getUIText("Menu_AIConfig"));
		miAIConfig.setMnemonic('A');
		miAIConfig.addActionListener(this);
		miAIConfig.setActionCommand("Menu_AIConfig");
		menuConfig.add(miAIConfig);

		// AISetting(2P)
		JMenuItem miAIConfig2P = new JMenuItem(getUIText("Menu_AIConfig2P"));
		miAIConfig2P.setMnemonic('Z');
		miAIConfig2P.addActionListener(this);
		miAIConfig2P.setActionCommand("Menu_AIConfig2P");
		menuConfig.add(miAIConfig2P);

		// Key settings
		JMenuItem miKeyConfig = new JMenuItem(getUIText("Menu_KeyConfig"));
		miKeyConfig.setMnemonic('K');
		miKeyConfig.addActionListener(this);
		miKeyConfig.setActionCommand("Menu_KeyConfig");
		menuConfig.add(miKeyConfig);

		// Key settings(2P)
		JMenuItem miKeyConfig2P = new JMenuItem(getUIText("Menu_KeyConfig2P"));
		miKeyConfig2P.setMnemonic('E');
		miKeyConfig2P.addActionListener(this);
		miKeyConfig2P.setActionCommand("Menu_KeyConfig2P");
		menuConfig.add(miKeyConfig2P);

		// Other Settings
		JMenuItem miGeneralConfig = new JMenuItem(getUIText("Menu_GeneralConfig"));
		miGeneralConfig.setMnemonic('G');
		miGeneralConfig.addActionListener(this);
		miGeneralConfig.setActionCommand("Menu_GeneralConfig");
		menuConfig.add(miGeneralConfig);
	}

	/**
	 * Load list file
	 */
	protected void loadRecommendedRuleList() {
		mapRuleEntries = new HashMap<String, RuleEntry>();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new ResourceInputStream("config/list/recommended_rules.lst")));
			String strMode = "";

			String str;
			while((str = in.readLine()) != null) {
				str = str.trim();	// Trim the space

				if(str.startsWith("#")) {
					// Commment-line. Ignore it.
				} else if(str.startsWith(":")) {
					// Mode change
					strMode = str.substring(1);
				} else {
					// File Path
						try {
							ResourceInputStream ruleIn = new ResourceInputStream(str);
							CustomProperties propRule = new CustomProperties();
							propRule.load(ruleIn);
							ruleIn.close();

							String strRuleName = propRule.getProperty("0.ruleopt.strRuleName", "");
							if(strRuleName.length() > 0) {
								RuleEntry entry = mapRuleEntries.get(strMode);
								if(entry == null) {
									entry = new RuleEntry();
									mapRuleEntries.put(strMode, entry);
								}
								entry.listName.add(strRuleName);
								entry.listPath.add(str);
							}
						} catch (IOException e2) {
							log.error("File " + str + " doesn't exist", e2);
						}
				}
			}

			in.close();
		} catch (IOException e) {
			log.error("Failed to load recommended rules list", e);
		}
	}

	/**
	 * Prepare rule list
	 */
	protected void prepareRuleList(String strCurrentMode) {
		listmodelRule.clear();
		listmodelRule.addElement(getUIText("Top_CurrentRule"));

		if(strCurrentMode != null) {
			RuleEntry entry = mapRuleEntries.get(strCurrentMode);

			if(entry != null) {
				for(int i = 0; i < entry.listName.size(); i++) {
					listmodelRule.addElement(entry.listName.get(i));
				}
			}
		}

		listboxRule.setSelectedIndex(0);
		String strLastRule = propGlobal.getProperty("lastrule." + strCurrentMode);
		if((strLastRule != null) && (strLastRule.length() > 0)) {
			listboxRule.setSelectedValue(strLastRule, true);
		}
	}

	/**
	 * OffLinesStart game buttonWhen is pressed
	 */
	protected void onStartOfflineClicked() {
		String strMode = (String)listboxMode.getSelectedValue();
		propGlobal.setProperty("name.mode", strMode);

		String strRulePath = null;
		if(listboxRule.getSelectedIndex() >= 1) {
			int index = listboxRule.getSelectedIndex();
			String strRuleName = (String)listboxRule.getSelectedValue();
			RuleEntry entry = mapRuleEntries.get(strMode);
			if(entry != null) {
				strRulePath = entry.listPath.get(index - 1);
				propGlobal.setProperty("lastrule." + strMode, strRuleName);
			}
		} else {
			propGlobal.setProperty("lastrule." + strMode, "");
		}

		saveConfig();

		startNewGame(strRulePath);
		if(gameFrame == null) {
			gameFrame = new GameInternalFrame(this);
		}
		if((gameManager != null) && (gameManager.mode != null)) {
			gameFrame.setTitle(getUIText("Title_Game") + " - " + gameManager.mode.getName());
			gameFrame.maxfps = propConfig.getProperty("option.maxfps", 60);
			gameFrame.isNetPlay = false;
		}
		hideAllSubWindows();
		this.setVisible(false);
		gameFrame.displayWindow();
	}

	@Override
	public void setVisible(boolean aFlag) {
		super.setVisible(aFlag);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				AppletMain.instance.repaint();
			}
		});
	}
	
	/**
	 * Shutdown this application
	 */
	public void shutdown() {
		log.debug("Main shutdown() called");
		propConfig.setProperty("mainwindow.width", getSize().width);
		propConfig.setProperty("mainwindow.height", getSize().height);
		propConfig.setProperty("mainwindow.x", getLocation().x);
		propConfig.setProperty("mainwindow.y", getLocation().y);
		saveConfig();
//		System.exit(0);
	}

	/*
	 * Menu What Happens at Runtime
	 */
	public void actionPerformed(ActionEvent e) {
		// OffLinesStart game
		if(e.getActionCommand() == "Top_StartOffline") {
			onStartOfflineClicked();
		}
		// Open Replay
		else if(e.getActionCommand() == "Menu_Open") {
			if(replayFileChooser == null) {
//				File dir = new File(propGlobal.getProperty("custom.replay.directory", "replay"));
				replayFileChooser = new JFileChooser(new ResourceFileSystemView() {
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
				replayFileChooser.addChoosableFileFilter(new ReplayFileFilter());
			}
			if(replayFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION && replayFileChooser.getSelectedFile().getName().endsWith(".rep")) {
				startReplayGame("replay/" + replayFileChooser.getSelectedFile().getPath());
				if(gameFrame == null) {
					gameFrame = new GameInternalFrame(this);
				}
				if((gameManager != null) && (gameManager.mode != null)) {
					gameFrame.setTitle(getUIText("Title_Game") + " - " + gameManager.mode.getName() + " (Replay)");
					gameFrame.maxfps = propConfig.getProperty("option.maxfps", 60);
					gameFrame.isNetPlay = false;
				}
				hideAllSubWindows();
				this.setVisible(false);
				gameFrame.displayWindow();
			}
		}
		// NetPlay start
		else if(e.getActionCommand() == "Menu_NetPlay") {
			startNetPlayGame();
			if(gameFrame == null) {
				gameFrame = new GameInternalFrame(this);
			}
			if((gameManager != null) && (gameManager.mode != null)) {
				gameFrame.setTitle(getUIText("Title_Game") + " - " + gameManager.mode.getName());
				gameFrame.maxfps = 60;
				gameFrame.isNetPlay = true;
			}
			hideAllSubWindows();
			this.setVisible(false);
			gameFrame.displayWindow();
		}
		// Selection rules
		else if(e.getActionCommand() == "Menu_RuleSelect") {
			if(ruleSelectFrame == null) {
				ruleSelectFrame = new RuleSelectInternalFrame(this);
			}
			ruleSelectFrame.load(0);
			ruleSelectFrame.setVisible(true);
		}
		// Selection rules(2P)
		else if(e.getActionCommand() == "Menu_RuleSelect2P") {
			if(ruleSelectFrame == null) {
				ruleSelectFrame = new RuleSelectInternalFrame(this);
			}
			ruleSelectFrame.load(1);
			ruleSelectFrame.setVisible(true);
		}
		// Keyboard settings
		else if(e.getActionCommand() == "Menu_KeyConfig") {
			if(keyConfigFrame == null) {
				keyConfigFrame = new KeyConfigInternalFrame(this);
			}
			keyConfigFrame.load(0);
			keyConfigFrame.setVisible(true);
		}
		// Keyboard settings(2P)
		else if(e.getActionCommand() == "Menu_KeyConfig2P") {
			if(keyConfigFrame == null) {
				keyConfigFrame = new KeyConfigInternalFrame(this);
			}
			keyConfigFrame.load(1);
			keyConfigFrame.setVisible(true);
		}
		// AISetting
		else if(e.getActionCommand() == "Menu_AIConfig") {
			if(aiSelectFrame == null) {
				aiSelectFrame = new AISelectInternalFrame(this);
			}
			aiSelectFrame.load(0);
			aiSelectFrame.setVisible(true);
		}
		// AISetting(2P)
		else if(e.getActionCommand() == "Menu_AIConfig2P") {
			if(aiSelectFrame == null) {
				aiSelectFrame = new AISelectInternalFrame(this);
			}
			aiSelectFrame.load(1);
			aiSelectFrame.setVisible(true);
		}
		// Tuning settings
		else if(e.getActionCommand() == "Menu_GameTuning") {
			if(gameTuningFrame == null) {
				gameTuningFrame = new GameTuningInternalFrame(this);
			}
			gameTuningFrame.load(0);
			gameTuningFrame.setVisible(true);
		}
		// Tuning settings(2P)
		else if(e.getActionCommand() == "Menu_GameTuning2P") {
			if(gameTuningFrame == null) {
				gameTuningFrame = new GameTuningInternalFrame(this);
			}
			gameTuningFrame.load(1);
			gameTuningFrame.setVisible(true);
		}
		// Other Settings
		else if(e.getActionCommand() == "Menu_GeneralConfig") {
			if(generalConfigFrame == null) {
				generalConfigFrame = new GeneralConfigInternalFrame(this);
			}
			generalConfigFrame.load();
			generalConfigFrame.setVisible(true);
		}
		// End
		else if(e.getActionCommand() == "Menu_Exit") {
			shutdown();
		}
	}

	/**
	 * Hide all subwindows
	 */
	public void hideAllSubWindows() {
		if(keyConfigFrame != null) keyConfigFrame.setVisible(false);
		if(ruleSelectFrame != null) ruleSelectFrame.setVisible(false);
		if(aiSelectFrame != null) aiSelectFrame.setVisible(false);
		if(generalConfigFrame != null) generalConfigFrame.setVisible(false);
		if(gameTuningFrame != null) gameTuningFrame.setVisible(false);
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
		rendererSwing = new RendererApplet();
		gameManager = new GameManager(rendererSwing);

		// Mode
		String modeName = propGlobal.getProperty("name.mode", "");
		GameMode modeObj = modeManager.getMode(modeName);
		if(modeObj == null) {
			log.error("Couldn't find mode:" + modeName);
		} else {
			gameManager.mode = modeObj;
		}

		gameManager.init();

		// Initialization for each player
		for(int i = 0; i < gameManager.getPlayers(); i++) {
			// Tuning settings
			gameManager.engine[i].owRotateButtonDefaultRight = propGlobal.getProperty(i + ".tuning.owRotateButtonDefaultRight", -1);
			gameManager.engine[i].owSkin = propGlobal.getProperty(i + ".tuning.owSkin", -1);
			gameManager.engine[i].owMinDAS = propGlobal.getProperty(i + ".tuning.owMinDAS", -1);
			gameManager.engine[i].owMaxDAS = propGlobal.getProperty(i + ".tuning.owMaxDAS", -1);
			gameManager.engine[i].owDasDelay = propGlobal.getProperty(i + ".tuning.owDasDelay", -1);
			gameManager.engine[i].owReverseUpDown = propGlobal.getProperty(i + ".tuning.owReverseUpDown", false);
			gameManager.engine[i].owMoveDiagonal = propGlobal.getProperty(i + ".tuning.owMoveDiagonal", -1);
			gameManager.engine[i].owBlockOutlineType = propGlobal.getProperty(i + ".tuning.owBlockOutlineType", -1);
			gameManager.engine[i].owBlockShowOutlineOnly = propGlobal.getProperty(i + ".tuning.owBlockShowOutlineOnly", -1);

			// Rule
			RuleOptions ruleopt = null;
			String rulename = strRulePath;
			if(rulename == null) {
				rulename = propGlobal.getProperty(i + ".rule", "");
				if(gameManager.mode.getGameStyle() > 0) {
					rulename = propGlobal.getProperty(i + ".rule." + gameManager.mode.getGameStyle(), "");
				}
			}
			if((rulename != null) && (rulename.length() > 0)) {
				log.debug("Load rule options from " + rulename);
				ruleopt = GeneralUtil.loadRule(rulename);
			} else {
				log.debug("Load rule options from setting file");
				ruleopt = new RuleOptions();
				ruleopt.readProperty(propGlobal, i);
			}
			gameManager.engine[i].ruleopt = ruleopt;

			// NEXTOrder generation algorithm
			if((ruleopt.strRandomizer != null) && (ruleopt.strRandomizer.length() > 0)) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer);
				gameManager.engine[i].randomizer = randomizerObject;
			}

			// Wallkick
			if((ruleopt.strWallkick != null) && (ruleopt.strWallkick.length() > 0)) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engine[i].wallkick = wallkickObject;
			}

			// AI
			String aiName = propGlobal.getProperty(i + ".ai", "");
			if(aiName.length() > 0) {
				AbstractAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engine[i].ai = aiObj;
				gameManager.engine[i].aiMoveDelay = propGlobal.getProperty(i + ".aiMoveDelay", 0);
				gameManager.engine[i].aiThinkDelay = propGlobal.getProperty(i + ".aiThinkDelay", 0);
				gameManager.engine[i].aiUseThread = propGlobal.getProperty(i + ".aiUseThread", true);
				gameManager.engine[i].aiShowHint = propGlobal.getProperty(i+".aiShowHint", false);
				gameManager.engine[i].aiPrethink = propGlobal.getProperty(i+".aiPrethink", false);
				gameManager.engine[i].aiShowState = NullpoMinoInternalFrame.propGlobal.getProperty(i+".aiShowState", false);
			}
			gameManager.showInput = NullpoMinoInternalFrame.propConfig.getProperty("option.showInput", false);

			// Called at initialization
			gameManager.engine[i].init();
		}
	}

	/**
	 * Load and play the replay
	 * @param filename Replay dataOfFilename
	 */
	public void startReplayGame(String filename) {
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

		rendererSwing = new RendererApplet();
		gameManager = new GameManager(rendererSwing);
		gameManager.replayMode = true;
		gameManager.replayProp = prop;

		// Mode
		String modeName = prop.getProperty("name.mode", "");
		GameMode modeObj = modeManager.getMode(modeName);
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
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer);
				gameManager.engine[i].randomizer = randomizerObject;
			}

			// Wallkick
			if((ruleopt.strWallkick != null) && (ruleopt.strWallkick.length() > 0)) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engine[i].wallkick = wallkickObject;
			}

			// AI (For added replay)
			String aiName = propGlobal.getProperty(i + ".ai", "");
			if(aiName.length() > 0) {
				AbstractAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engine[i].ai = aiObj;
				gameManager.engine[i].aiMoveDelay = propGlobal.getProperty(i + ".aiMoveDelay", 0);
				gameManager.engine[i].aiThinkDelay = propGlobal.getProperty(i + ".aiThinkDelay", 0);
				gameManager.engine[i].aiUseThread = propGlobal.getProperty(i + ".aiUseThread", true);
				gameManager.engine[i].aiShowHint = propGlobal.getProperty(i+".aiShowHint", false);
				gameManager.engine[i].aiPrethink = propGlobal.getProperty(i+".aiPrethink", false);
				gameManager.engine[i].aiShowState = NullpoMinoInternalFrame.propGlobal.getProperty(i+".aiShowState", false);
			}
			gameManager.showInput = NullpoMinoInternalFrame.propConfig.getProperty("option.showInput", false);

			// Called at initialization
			gameManager.engine[i].init();
		}
	}

	/**
	 * NetPlay start processing
	 */
	public void startNetPlayGame() {
		// gameManager Initialization
		rendererSwing = new RendererApplet();
		gameManager = new GameManager(rendererSwing);

		// Lobby Initialization
		netLobby = new NetLobbyInternalFrame();
		netLobby.getKnetPanel().addKNetPanelListener(this);

		// Mode initialization
		enterNewMode(null);

		// Lobby start
		netLobby.init();
		netLobby.setVisible(true);
	}

	/**
	 * Enter to a new mode in netplay
	 * @param modeName Mode name
	 */
	public void enterNewMode(String modeName) {
		loadGlobalConfig();	// Reload global config file

		GameMode previousMode = gameManager.mode;
		GameMode newModeTemp = (modeName == null) ? new AbstractNetMode() : NullpoMinoInternalFrame.modeManager.getMode(modeName);

		if(newModeTemp == null) {
			log.error("Cannot find a mode:" + modeName);
		} else if(newModeTemp instanceof AbstractNetMode) {
			log.info("Enter new mode:" + newModeTemp.getName());

			AbstractNetMode newMode = (AbstractNetMode)newModeTemp;

			if(previousMode != null) {
				if(gameManager.engine[0].ai != null) {
					gameManager.engine[0].ai.shutdown(gameManager.engine[0], 0);
				}
				previousMode.netplayUnload(netLobby.getKnetPanel());
			}
			gameManager.mode = newMode;
			gameManager.init();

			// Tuning
			gameManager.engine[0].owRotateButtonDefaultRight = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".tuning.owRotateButtonDefaultRight", -1);
			gameManager.engine[0].owSkin = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".tuning.owSkin", -1);
			gameManager.engine[0].owMinDAS = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".tuning.owMinDAS", -1);
			gameManager.engine[0].owMaxDAS = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".tuning.owMaxDAS", -1);
			gameManager.engine[0].owDasDelay = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".tuning.owDasDelay", -1);
			gameManager.engine[0].owReverseUpDown = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".tuning.owReverseUpDown", false);
			gameManager.engine[0].owMoveDiagonal = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".tuning.owMoveDiagonal", -1);
			gameManager.engine[0].owBlockOutlineType = propGlobal.getProperty(0 + ".tuning.owBlockOutlineType", -1);
			gameManager.engine[0].owBlockShowOutlineOnly = propGlobal.getProperty(0 + ".tuning.owBlockShowOutlineOnly", -1);

			// Rule
			RuleOptions ruleopt = null;
			String rulename = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".rule", "");
			if(gameManager.mode.getGameStyle() > 0) {
				rulename = propGlobal.getProperty(0 + ".rule." + gameManager.mode.getGameStyle(), "");
			}
			if((rulename != null) && (rulename.length() > 0)) {
				log.info("Load rule options from " + rulename);
				ruleopt = GeneralUtil.loadRule(rulename);
			} else {
				log.info("Load rule options from setting file");
				ruleopt = new RuleOptions();
				ruleopt.readProperty(NullpoMinoInternalFrame.propGlobal, 0);
			}
			gameManager.engine[0].ruleopt = ruleopt;

			// Randomizer
			if((ruleopt.strRandomizer != null) && (ruleopt.strRandomizer.length() > 0)) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer);
				gameManager.engine[0].randomizer = randomizerObject;
			}

			// Wallkick
			if((ruleopt.strWallkick != null) && (ruleopt.strWallkick.length() > 0)) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engine[0].wallkick = wallkickObject;
			}

			// AI
			String aiName = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".ai", "");
			if(aiName.length() > 0) {
				AbstractAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engine[0].ai = aiObj;
				gameManager.engine[0].aiMoveDelay = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".aiMoveDelay", 0);
				gameManager.engine[0].aiThinkDelay = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".aiThinkDelay", 0);
				gameManager.engine[0].aiUseThread = NullpoMinoInternalFrame.propGlobal.getProperty(0 + ".aiUseThread", true);
				gameManager.engine[0].aiShowHint = NullpoMinoInternalFrame.propGlobal.getProperty(0+".aiShowHint", false);
				gameManager.engine[0].aiPrethink = NullpoMinoInternalFrame.propGlobal.getProperty(0+".aiPrethink", false);
				gameManager.engine[0].aiShowState = NullpoMinoInternalFrame.propGlobal.getProperty(0+".aiShowState", false);
			}
			gameManager.showInput = NullpoMinoInternalFrame.propConfig.getProperty("option.showInput", false);

			// Initialization for each player
			for(int i = 0; i < gameManager.getPlayers(); i++) {
				gameManager.engine[i].init();
			}

			newMode.netplayInit(netLobby.getKnetPanel());
		} else {
			log.error("This mode does not support netplay:" + modeName);
		}

		if(gameFrame != null) gameFrame.updateTitleBarCaption();
	}

	@Override
	public void knetPanelInit(KNetPanelEvent e) {
	}
	
	@Override
	public void knetPanelDisconnected(KNetPanelEvent e) {
		if(gameFrame != null) 
			gameFrame.strModeToEnter = null;
	}
	
	@Override
	public void knetPanelShutdown(KNetPanelEvent e) {
		if(gameManager != null) {
			gameManager.engine[0].quitflag = true;
		}
	}

	@Override
	public void knetPanelConnected(KNetPanelEvent e) {
	}

	@Override
	public void knetPanelJoined(KNetPanelEvent e) {
		if(gameFrame != null)
			gameFrame.strModeToEnter = e.getChannel().getMode();
	}

	@Override
	public void knetPanelParted(KNetPanelEvent e) {
		if(gameFrame != null) 
			gameFrame.strModeToEnter = null;
	}
	
	/**
	 * Filter for selecting files replay
	 */
	protected class ReplayFileFilter extends FileFilter {
		/*
		 * Decision whether or not to display the file
		 */
		@Override
		public boolean accept(File f) {
			// If the directory displayed unconditional
			// Or the end of the file is.repIf it was displayed
			if(f.isDirectory() || f.getName().endsWith(".rep")) return true;
			return false;
		}

		/*
		 * Returns the display name for this filter
		 */
		@Override
		public String getDescription() {
			return getUIText("FileChooser_ReplayFile");
		}
	}

	/**
	 * Mode For selection list boxMouseAdapter
	 */
	protected class ListboxModeMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if((e.getClickCount() == 2) && (e.getButton() == MouseEvent.BUTTON1)) {
				onStartOfflineClicked();
			}
		}
	}

	/**
	 * RuleEntry
	 */
	protected class RuleEntry {
		public LinkedList<String> listPath = new LinkedList<String>();
		public LinkedList<String> listName = new LinkedList<String>();
	}
}
