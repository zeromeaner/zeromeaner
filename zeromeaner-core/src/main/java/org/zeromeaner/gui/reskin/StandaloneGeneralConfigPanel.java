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

import static org.zeromeaner.gui.reskin.Localizations.lz;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.eviline.swing.IntegerDocument;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.ServiceHookDispatcher;
import org.zeromeaner.util.Options.StandaloneOptions;
import org.zeromeaner.util.Session;
import org.zeromeaner.util.io.PropertyStore;
import org.zeromeaner.util.SwingUtils;

/**
 * Setting screen frame
 */
public class StandaloneGeneralConfigPanel extends JPanel implements ActionListener {
	public static interface Hook {
		public void createTabs(JTabbedPane tabs);
		public void saveConfiguration();
		public void loadConfiguration();
	}
	
	protected static final ServiceHookDispatcher<Hook> hooks = new ServiceHookDispatcher<>(Hook.class);
	
	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/** Screen size table */
	protected static final int[][] SCREENSIZE_TABLE =
	{
		{320,240}, {400,300}, {480,360}, {512,384}, {640,480}, {800,600}, {1024,768}, {1152,864}, {1280,960}
	};
	
	/** Model of screen size combobox */
	protected DefaultComboBoxModel modelScreenSize;

	/** Screen size combobox */
	protected JComboBox comboboxScreenSize;

	/** MaximumFPS */
	protected JTextField txtfldMaxFPS;

	/** Sound effectsVolume of */
	protected JSlider volume;

	/** Line clear effect speed */
	protected JTextField txtfldLineClearEffectSpeed;

	/** FPSDisplay */
	protected JCheckBox chkboxShowFPS;

	/** BackgroundDisplay */
	protected JCheckBox chkboxShowBackground;

	/** MeterDisplay */
	protected JCheckBox chkboxShowMeter;

	/** fieldOfBlockDisplay a picture of a ( check Only if there is no border) */
	protected JCheckBox chkboxShowFieldBlockGraphics;

	/** Simple picture ofBlockI use */
	protected JCheckBox chkboxSimpleBlock;

	/** Sound effects */
	protected JCheckBox chkboxSE;

	/**  frame Step */
	protected JCheckBox chkboxEnableFrameStep;

	/** ghost On top of the pieceNEXTDisplay */
	protected JCheckBox chkboxNextShadow;

	/** Linear frameghost Peace */
	protected JCheckBox chkboxOutlineGhost;

	/** Side piece preview */
	protected JCheckBox chkboxSideNext;

	/** Use bigger side piece preview */
	protected JCheckBox chkboxBigSideNext;

	/** Sync Display */
	protected JCheckBox chkboxSyncDisplay;

	protected JCheckBox chkboxSyncRender;

	/** Show line clear effect */
	protected JCheckBox chkboxShowLineClearEffect;

	/** Dark piece preview area */
	protected JCheckBox chkboxDarkNextArea;

	/** Show field BG grid */
	protected JCheckBox chkboxShowFieldBGGrid;

	/** Show field BG grid */
	protected JCheckBox chkboxShowInput;

	protected JTextField userId = new JTextField("", 40);
	
	
	protected JCheckBox increaseEQPriority;
	
	protected JCheckBox davEnabled;
	
	protected JCheckBox hideUnpopular;
	
	/**
	 * Constructor
	 * @param owner Parent window
	 * @throws HeadlessException Keyboard, Mouse, Exceptions such as the display if there is no
	 */
	public StandaloneGeneralConfigPanel() throws HeadlessException {
		super();

		// GUIOfInitialization
		initUI();
	}

	/**
	 * GUIOfInitialization
	 */
	protected void initUI() {
		setLayout(new BorderLayout(10, 10));

		JPanel login = new JPanel(new FlowLayout(FlowLayout.CENTER));
		login.add(new JLabel(lz.s("GeneralConfig_OnlineID")));
		login.add(userId);
//		this.add(login, BorderLayout.NORTH);
		
		// * Tab pane
		JTabbedPane tabPane = new JTabbedPane();
		this.add(tabPane, BorderLayout.CENTER);

		// ** Basic Tab
		JPanel pBasicTab = new JPanel();
		pBasicTab.setLayout(new GridLayout(0, 1));
		tabPane.addTab(lz.s("GeneralConfig_TabName_Basic"), pBasicTab);

		// ** Advanced Tab
		JPanel pAdvancedTab = new JPanel();
		pAdvancedTab.setLayout(new GridLayout(0, 1));
		tabPane.addTab(lz.s("GeneralConfig_TabName_Advanced"), pAdvancedTab);

		// ** Pro Tab
		JPanel pProTab = new JPanel();
		pProTab.setLayout(new GridLayout(0, 1));
		tabPane.addTab(lz.s("GeneralConfig_TabName_Pro"), pProTab);

		davEnabled = new JCheckBox(lz.s("GeneralConfig_DavEnabled"));
		davEnabled.setHorizontalAlignment(SwingConstants.CENTER);
		pBasicTab.add(davEnabled);
		
		pBasicTab.add(login);
		
		// ---------- Sound effectsVolume of ----------
		JPanel pSEVolume = new JPanel();
		pSEVolume.setAlignmentX(CENTER_ALIGNMENT);
		pBasicTab.add(pSEVolume);

		JLabel lSEVolume = new JLabel(lz.s("GeneralConfig_SEVolume"));
		pSEVolume.add(lSEVolume);

		volume = new JSlider(0, 100);
		Dictionary<Integer, Component> volumeLabels = new Hashtable<>();
		volumeLabels.put(0, new JLabel("Off"));
		volumeLabels.put(50, new JLabel("50%"));
		volumeLabels.put(100, new JLabel("100%"));
		volume.setLabelTable(volumeLabels);
		volume.setPaintLabels(true);
		pSEVolume.add(volume);

		chkboxShowBackground = new JCheckBox(lz.s("GeneralConfig_ShowBackground"));
		chkboxShowBackground.setHorizontalAlignment(SwingConstants.CENTER);
		pAdvancedTab.add(chkboxShowBackground);

		chkboxShowMeter = new JCheckBox(lz.s("GeneralConfig_ShowMeter"));
		chkboxShowMeter.setHorizontalAlignment(SwingConstants.CENTER);
		pAdvancedTab.add(chkboxShowMeter);

		chkboxShowFieldBlockGraphics = new JCheckBox(lz.s("GeneralConfig_ShowFieldBlockGraphics"));
		chkboxShowFieldBlockGraphics.setHorizontalAlignment(SwingConstants.CENTER);
		pAdvancedTab.add(chkboxShowFieldBlockGraphics);

		chkboxSimpleBlock = new JCheckBox(lz.s("GeneralConfig_SimpleBlock"));
		chkboxSimpleBlock.setHorizontalAlignment(SwingConstants.CENTER);
		pBasicTab.add(chkboxSimpleBlock);

		chkboxSE = new JCheckBox(lz.s("GeneralConfig_SE"));
		chkboxSE.setHorizontalAlignment(SwingConstants.CENTER);
		pAdvancedTab.add(chkboxSE);

		chkboxNextShadow = new JCheckBox(lz.s("GeneralConfig_NextShadow"));
		chkboxNextShadow.setHorizontalAlignment(SwingConstants.CENTER);
		pProTab.add(chkboxNextShadow);

		chkboxOutlineGhost = new JCheckBox(lz.s("GeneralConfig_OutlineGhost"));
		chkboxOutlineGhost.setHorizontalAlignment(SwingConstants.CENTER);
		pBasicTab.add(chkboxOutlineGhost);

		chkboxSideNext = new JCheckBox(lz.s("GeneralConfig_SideNext"));
		chkboxSideNext.setHorizontalAlignment(SwingConstants.CENTER);
		pAdvancedTab.add(chkboxSideNext);

		chkboxBigSideNext = new JCheckBox(lz.s("GeneralConfig_BigSideNext"));
		chkboxBigSideNext.setHorizontalAlignment(SwingConstants.CENTER);
		pProTab.add(chkboxBigSideNext);

		chkboxDarkNextArea = new JCheckBox(lz.s("GeneralConfig_DarkNextArea"));
		chkboxDarkNextArea.setHorizontalAlignment(SwingConstants.CENTER);
		pProTab.add(chkboxDarkNextArea);

		chkboxShowFieldBGGrid = new JCheckBox(lz.s("GeneralConfig_ShowFieldBGGrid"));
		chkboxShowFieldBGGrid.setHorizontalAlignment(SwingConstants.CENTER);
		pProTab.add(chkboxShowFieldBGGrid);

		chkboxShowInput = new JCheckBox(lz.s("GeneralConfig_ShowInput"));
		chkboxShowInput.setHorizontalAlignment(SwingConstants.CENTER);
//		pBasicTab.add(chkboxShowInput);
		
		// ---------- Screen size ----------
		JPanel pScreenSize = new JPanel();
		pScreenSize.setAlignmentX(CENTER_ALIGNMENT);
		pAdvancedTab.add(pScreenSize);

		JLabel lScreenSize = new JLabel(lz.s("GeneralConfig_ScreenSize"));
		pScreenSize.add(lScreenSize);

		modelScreenSize = new DefaultComboBoxModel();
		for(int i = 0; i < SCREENSIZE_TABLE.length; i++) {
			String strTemp = SCREENSIZE_TABLE[i][0] + "x" + SCREENSIZE_TABLE[i][1];
			modelScreenSize.addElement(strTemp);
		}
		comboboxScreenSize = new JComboBox(modelScreenSize);
		pScreenSize.add(comboboxScreenSize);

		// ---------- MaximumFPS ----------
		JPanel pMaxFPS = new JPanel();
		pMaxFPS.setAlignmentX(CENTER_ALIGNMENT);
		pProTab.add(pMaxFPS);

		JLabel lMaxFPS = new JLabel(lz.s("GeneralConfig_MaxFPS"));
		pMaxFPS.add(lMaxFPS);

		txtfldMaxFPS = new JTextField(5);
		pMaxFPS.add(txtfldMaxFPS);

		// ---------- Line clear effect speed ----------
		JPanel pLineClearEffectSpeed = new JPanel();
		pLineClearEffectSpeed.setAlignmentX(CENTER_ALIGNMENT);
		pProTab.add(pLineClearEffectSpeed);

		JLabel lLineClearEffectSpeed = new JLabel(lz.s("GeneralConfig_LineClearEffectSpeed"));
		pLineClearEffectSpeed.add(lLineClearEffectSpeed);

		txtfldLineClearEffectSpeed = new JTextField(5);
		pLineClearEffectSpeed.add(txtfldLineClearEffectSpeed);

		// ---------- Checkboxes ----------
		chkboxShowFPS = new JCheckBox(lz.s("GeneralConfig_ShowFPS"));
		chkboxShowFPS.setHorizontalAlignment(SwingConstants.CENTER);
		pProTab.add(chkboxShowFPS);

		chkboxEnableFrameStep = new JCheckBox(lz.s("GeneralConfig_EnableFrameStep"));
		chkboxEnableFrameStep.setHorizontalAlignment(SwingConstants.CENTER);
//		pAdvancedTab.add(chkboxEnableFrameStep);

		chkboxSyncDisplay = new JCheckBox(lz.s("GeneralConfig_SyncDisplay"));
		chkboxSyncDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		pProTab.add(chkboxSyncDisplay);
		
		chkboxSyncRender = new JCheckBox(lz.s("GeneralConfig_SyncRender"));
		chkboxSyncRender.setHorizontalAlignment(SwingConstants.CENTER);
		pProTab.add(chkboxSyncRender);
		

		chkboxShowLineClearEffect = new JCheckBox(lz.s("GeneralConfig_ShowLineClearEffect"));
		chkboxShowLineClearEffect.setHorizontalAlignment(SwingConstants.CENTER);
		pAdvancedTab.add(chkboxShowLineClearEffect);

		increaseEQPriority = new JCheckBox(lz.s("GeneralConfig_IncreasedEQPriority"));
		increaseEQPriority.setHorizontalAlignment(SwingConstants.CENTER);
		pProTab.add(increaseEQPriority);
		
		hideUnpopular = new JCheckBox(lz.s("GeneralConfig_HideUnpopular"));
		hideUnpopular.setHorizontalAlignment(SwingConstants.CENTER);
		pProTab.add(hideUnpopular);
		
		hooks.dispatcher().createTabs(tabPane);
		hooks.dispatcher().loadConfiguration();
		
		// ---------- The bottom of the screen button ----------
		JPanel pButtons = new JPanel();
		pButtons.setAlignmentX(CENTER_ALIGNMENT);
		this.add(pButtons, BorderLayout.SOUTH);

		JButton buttonOK = new JButton(lz.s("GeneralConfig_OK"));
		buttonOK.setMnemonic('O');
		buttonOK.addActionListener(this);
		buttonOK.setActionCommand("GeneralConfig_OK");
		pButtons.add(buttonOK);

		JButton buttonCancel = new JButton(lz.s("GeneralConfig_Cancel"));
		buttonCancel.setMnemonic('C');
		buttonCancel.addActionListener(this);
		buttonCancel.setActionCommand("GeneralConfig_Cancel");
		pButtons.add(buttonCancel);
		
		JButton buttonRestart = new JButton(new AbstractAction(lz.s("GeneralConfig_Restart")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				StandaloneMain.restart();
			}
		});
		pButtons.add(buttonRestart);
	}
	
	/**
	 * Current SettingsGUIBe reflected in the
	 */
	public void load() {
		userId.setText(Session.getUser());
		
		StandaloneOptions opt = Options.standalone();
		
		int sWidth = opt.SCREEN_WIDTH.value();
		int sHeight = opt.SCREEN_HEIGHT.value();
		comboboxScreenSize.setSelectedIndex(4);	// Default to 640x480
		for(int i = 0; i < SCREENSIZE_TABLE.length; i++) {
			if((sWidth == SCREENSIZE_TABLE[i][0]) && (sHeight == SCREENSIZE_TABLE[i][1])) {
				comboboxScreenSize.setSelectedIndex(i);
				break;
			}
		}

		davEnabled.setSelected(Options.isDavEnabled());
		
		txtfldMaxFPS.setText("" + opt.MAX_FPS.value());
//		txtfldSEVolume.setText("" + opt.SE_VOLUME.value());
		volume.setValue((int)(100 * opt.SE_VOLUME.value()));
		txtfldLineClearEffectSpeed.setText("" + opt.LINE_EFFECT_SPEED.value());
		chkboxShowFPS.setSelected(opt.SHOW_FPS.value());
		chkboxShowBackground.setSelected(opt.SHOW_BG.value());
		chkboxShowMeter.setSelected(opt.SHOW_METER.value());
		chkboxShowFieldBlockGraphics.setSelected(opt.SHOW_FIELD_BLOCK_GRAPHICS.value());
		chkboxSimpleBlock.setSelected(opt.SIMPLE_BLOCK.value());
		chkboxSE.setSelected(opt.SE_ENABLED.value());
		chkboxEnableFrameStep.setSelected(opt.ENABLE_FRAME_STEP.value());
		chkboxNextShadow.setSelected(opt.NEXT_SHADOW.value());
		chkboxOutlineGhost.setSelected(opt.OUTLINE_GHOST.value());
		chkboxSideNext.setSelected(opt.SIDE_NEXT.value());
		chkboxBigSideNext.setSelected(opt.BIG_SIDE_NEXT.value());
		chkboxDarkNextArea.setSelected(opt.DARK_NEXT_AREA.value());
		chkboxShowFieldBGGrid.setSelected(opt.SHOW_FIELD_BG_GRID.value());
		chkboxShowInput.setSelected(opt.SHOW_INPUT.value());
		chkboxSyncRender.setSelected(opt.SYNC_RENDER.value());
		chkboxSyncDisplay.setSelected(opt.SYNC_DISPLAY.value());
		chkboxShowLineClearEffect.setSelected(opt.SHOW_LINE_EFFECT.value());
		increaseEQPriority.setSelected(opt.INCREASE_EQ_PRIORITY.value());
		hideUnpopular.setSelected(opt.HIDE_UNPOPULAR.value());
		
		hooks.dispatcher().loadConfiguration();
	}

	/*
	 *  Called when button clicked
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "GeneralConfig_OK") {
			boolean restart = false;
			
			// OK
			
			PropertyStore.get().put("userId", userId.getText());
			Session.setUser(userId.getText());
			
			restart |= (davEnabled.isSelected() != Options.isDavEnabled());
			Options.setDavEnabled(davEnabled.isSelected());
			
			StandaloneOptions opt = Options.standalone();
			
			int screenSizeIndex = comboboxScreenSize.getSelectedIndex();
			if((screenSizeIndex >= 0) && (screenSizeIndex < SCREENSIZE_TABLE.length)) {
				opt.SCREEN_WIDTH.set(SCREENSIZE_TABLE[screenSizeIndex][0]);
				opt.SCREEN_HEIGHT.set(SCREENSIZE_TABLE[screenSizeIndex][1]);
			}

			int maxfps = SwingUtils.getIntTextField(60, txtfldMaxFPS);
			opt.MAX_FPS.set(maxfps);

//			double sevolume = SwingUtils.getDoubleTextField(1.0d, txtfldSEVolume);
			double sevolume = volume.getValue() / 100.;
			opt.SE_VOLUME.set(sevolume);

			int lineeffectspeed = SwingUtils.getIntTextField(0, txtfldLineClearEffectSpeed) - 1;
			if(lineeffectspeed < 0) lineeffectspeed = 0;
			opt.LINE_EFFECT_SPEED.set(lineeffectspeed);

			opt.SHOW_FPS.set(chkboxShowFPS.isSelected());
			opt.SHOW_BG.set(chkboxShowBackground.isSelected());
			opt.SHOW_METER.set(chkboxShowMeter.isSelected());
			opt.SHOW_FIELD_BLOCK_GRAPHICS.set(chkboxShowFieldBlockGraphics.isSelected());
			opt.SIMPLE_BLOCK.set(chkboxSimpleBlock.isSelected());
			restart |= opt.SE_ENABLED.set(chkboxSE.isSelected());
			opt.ENABLE_FRAME_STEP.set(chkboxEnableFrameStep.isSelected());
			opt.NEXT_SHADOW.set(chkboxNextShadow.isSelected());
			opt.OUTLINE_GHOST.set(chkboxOutlineGhost.isSelected());
			opt.SIDE_NEXT.set(chkboxSideNext.isSelected());
			opt.BIG_SIDE_NEXT.set(chkboxBigSideNext.isSelected());
			opt.DARK_NEXT_AREA.set(chkboxDarkNextArea.isSelected());
			opt.SHOW_FIELD_BG_GRID.set(chkboxShowFieldBGGrid.isSelected());
			opt.SHOW_INPUT.set(chkboxShowInput.isSelected());
			opt.SYNC_RENDER.set(chkboxSyncRender.isSelected());
			opt.SYNC_DISPLAY.set(chkboxSyncDisplay.isSelected());
			opt.SHOW_LINE_EFFECT.set(chkboxShowLineClearEffect.isSelected());

			StandaloneMain.saveConfig();
			StandaloneResourceHolder.soundManager.setVolume(sevolume);
			if(chkboxShowBackground.isSelected()) {
				StandaloneResourceHolder.loadBackgroundImages();
			}
			if(chkboxShowLineClearEffect.isSelected()) {
				StandaloneResourceHolder.loadLineClearEffectImages();
			}
			
			opt.INCREASE_EQ_PRIORITY.set(increaseEQPriority.isSelected());
			restart |= opt.HIDE_UNPOPULAR.set(hideUnpopular.isSelected());
			
			hooks.dispatcher().saveConfiguration();
			
			if(restart) {
				int c = JOptionPane.showConfirmDialog(this, "Restart required for changes to take effect.  Restart now?", "Restart Now?", JOptionPane.YES_NO_OPTION);
				if(c == JOptionPane.YES_OPTION)
					StandaloneMain.restart();
			}
		}
		else if(e.getActionCommand() == "GeneralConfig_Cancel") {
			// Cancel
			load();
		}
	}
}
