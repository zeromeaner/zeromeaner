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
package org.zeromeaner.gui.tool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.filechooser.FileFilter;










import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.log4j.Logger;
import org.funcish.core.Mappings;
import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.randomizer.Randomizer;
import org.zeromeaner.game.subsystem.wallkick.StandardWallkick;
import org.zeromeaner.knet.KNetKryo;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.Localization;
import org.zeromeaner.util.ResourceInputStream;
import org.zeromeaner.util.Zeroflections;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.InputStreamReader;

/**
 * Rule Editor
 */
public class RuleEditorPanel extends JPanel implements ActionListener {
	private static final Localization lz = new Localization();
	
	/** Serial version */
	private static final long serialVersionUID = 1L;

	/** Log */
	static Logger log = Logger.getLogger(RuleEditorPanel.class);

	/** SwingVersion ofSave settingsUseProperty file */
	public CustomProperties propConfig;

	/** Default language file */
	public CustomProperties propLangDefault;

	/** UIFor translationProperty file */
	public CustomProperties propLang;

	//----------------------------------------------------------------------
	/** I&#39;m now openFilename (null:No) */
	private String strNowFile;

	/** Tab */
	private JTabbedPane tabPane;

	//----------------------------------------------------------------------
	/* Basic Settings panel */

	/** Rule name */
	private JTextField txtfldRuleName;

	/** NEXTDisplaycountTextfield */
	private JTextField txtfldNextDisplay;

	/** Game style combobox */
	private JComboBox comboboxStyle;

	/** Of pictureComboBox */
	private JComboBox comboboxSkin;

	/** ghost  is enabled */
	private JCheckBox chkboxGhost;

	/** BlockAnd PeacefieldAttempts off target emerges from */
	private JCheckBox chkboxEnterAboveField;

	/** When the planned site appearance is buriedY-coordinateSlide on theMaximum count */
	private JTextField txtfldEnterMaxDistanceY;

	/** NEXTOrder generation algorithm */
	private JComboBox comboboxRandomizer;

	/** NEXTList of order generation algorithm */
	private Vector<String> vectorRandomizer;

	/** NEXTReset sequence generation algorithm button */
	private JButton btnResetRandomizer;

	//----------------------------------------------------------------------
	/* fieldSettings panel */

	/** fieldThe width of the */
	private JTextField txtfldFieldWidth;

	/** Field height */
	private JTextField txtfldFieldHeight;

	/** fieldThe height of the unseen parts of */
	private JTextField txtfldFieldHiddenHeight;

	/** fieldCeiling */
	private JCheckBox chkboxFieldCeiling;

	/** fieldI would not put to death in the frame */
	private JCheckBox chkboxFieldLockoutDeath;

	/** fieldAttempts off target to death after only protrude */
	private JCheckBox chkboxFieldPartialLockoutDeath;

	//----------------------------------------------------------------------
	/* Hold Setting Panel */

	/** Hold is enabled */
	private JCheckBox chkboxHoldEnable;

	/** Hold preceding */
	private JCheckBox chkboxHoldInitial;

	/** Can not hold prior continuous use */
	private JCheckBox chkboxHoldInitialLimit;

	/** When using the holdBlockThe orientation of the piece back to its initial state */
	private JCheckBox chkboxHoldResetDirection;

	/** You can hold count (-1:Limitless) */
	private JTextField txtfldHoldLimit;

	//----------------------------------------------------------------------
	/* Settings panel drop */

	/** Hard dropAvailability */
	private JCheckBox chkboxDropHardDropEnable;

	/** Hard dropImmediately fixed in */
	private JCheckBox chkboxDropHardDropLock;

	/** Hard dropNot continuous use */
	private JCheckBox chkboxDropHardDropLimit;

	/** Soft dropAvailability */
	private JCheckBox chkboxDropSoftDropEnable;

	/** Soft dropImmediately fixed in */
	private JCheckBox chkboxDropSoftDropLock;

	/** Soft dropNot continuous use */
	private JCheckBox chkboxDropSoftDropLimit;

	/** In the ground stateSoft dropThen immediately fixed */
	private JCheckBox chkboxDropSoftDropSurfaceLock;

	/** Soft dropSpeed */
	private JTextField txtfldDropSoftDropSpeed;

	/** Soft dropSpeedCurrent × normal speednTo double */
	private JCheckBox chkboxDropSoftDropMultiplyNativeSpeed;

	/** Use new soft drop codes */
	private JCheckBox chkboxDropSoftDropGravitySpeedLimit;

	//----------------------------------------------------------------------
	/* rotationSettings panel */

	/** Precedingrotation */
	private JCheckBox chkboxRotateInitial;

	/** PrecedingrotationNot continuous use */
	private JCheckBox chkboxRotateInitialLimit;

	/** Wallkick */
	private JCheckBox chkboxRotateWallkick;

	/** PrecedingrotationButWallkickMake */
	private JCheckBox chkboxRotateInitialWallkick;

	/** TopDirectionToWallkickYou count (-1:Infinite) */
	private JTextField txtfldRotateMaxUpwardWallkick;

	/** falseLeft is positive ifrotation, When true,Right is positiverotation */
	private JCheckBox chkboxRotateButtonDefaultRight;

	/** ReverserotationAllow (falseIf positiverotationThe same as the) */
	private JCheckBox chkboxRotateButtonAllowReverse;

	/** 2rotationAllow (falseIf positiverotationThe same as the) */
	private JCheckBox chkboxRotateButtonAllowDouble;

	/** WallkickAlgorithm */
	private JComboBox comboboxWallkickSystem;

	/** WallkickList of Algorithms */
	private Vector<String> vectorWallkickSystem;

	/** WallkickReset of the algorithm button */
	private JButton btnResetWallkickSystem;

	//----------------------------------------------------------------------
	/* Fixation timeSettings panel */

	/** Minimum fixed time */
	private JTextField txtfldLockDelayMin;

	/** Highest fixed time */
	private JTextField txtfldLockDelayMax;

	/** In the fall fixing timeReset */
	private JCheckBox chkboxLockDelayLockResetFall;

	/** Move fixed timeReset */
	private JCheckBox chkboxLockDelayLockResetMove;

	/** rotationFixed at timeReset */
	private JCheckBox chkboxLockDelayLockResetRotate;

	/** Lock delay reset by wallkick */
	private JCheckBox chkboxLockDelayLockResetWallkick;

	/** Lateral motion counterAndrotation counterShare (Lateral motion counterI use only) */
	private JCheckBox chkboxLockDelayLockResetLimitShareCount;

	/** Lateral motion countLimit */
	private JTextField txtfldLockDelayLockResetLimitMove;

	/** rotation countLimit */
	private JTextField txtfldLockDelayLockResetLimitRotate;

	/** Lateral motion counterOrrotation counterExceeded the fixed timeTo disable the reset */
	private JRadioButton radioLockDelayLockResetLimitOverNoReset;

	/** Lateral motion counterOrrotation counterI fixed the excess is immediately */
	private JRadioButton radioLockDelayLockResetLimitOverInstant;

	/** Lateral motion counterOrrotation counterI exceeded theWallkickDisable */
	private JRadioButton radioLockDelayLockResetLimitOverNoWallkick;

	//----------------------------------------------------------------------
	/* ARESettings panel */

	/** LowestARE */
	private JTextField txtfldAREMin;

	/** HighestARE */
	private JTextField txtfldAREMax;

	/** LowestARE after line clear */
	private JTextField txtfldARELineMin;

	/** HighestARE after line clear */
	private JTextField txtfldARELineMax;

	/** Shining moment fixed frame count */
	private JTextField txtfldARELockFlash;

	/** BlockDedicated shines frame Put */
	private JCheckBox chkboxARELockFlashOnlyFrame;

	/** Line clearBeforeBlockShine frame Put */
	private JCheckBox chkboxARELockFlashBeforeLineClear;

	/** ARE cancel on move checkbox */
	private JCheckBox chkboxARECancelMove;

	/** ARE cancel on rotate checkbox */
	private JCheckBox chkboxARECancelRotate;

	/** ARE cancel on hold checkbox */
	private JCheckBox chkboxARECancelHold;

	//----------------------------------------------------------------------
	/* Line clearSettings panel */

	/** LowestLine clear time */
	private JTextField txtfldLineDelayMin;

	/** HighestLine clear time */
	private JTextField txtfldLineDelayMax;

	/** Animated falling */
	private JCheckBox chkboxLineFallAnim;

	/** Line delay cancel on move checkbox */
	private JCheckBox chkboxLineCancelMove;

	/** Line delay cancel on rotate checkbox */
	private JCheckBox chkboxLineCancelRotate;

	/** Line delay cancel on hold checkbox */
	private JCheckBox chkboxLineCancelHold;

	//----------------------------------------------------------------------
	/* Move the settings panel */

	/** Minimum horizontal reservoir time */
	private JTextField txtfldMoveDASMin;

	/** Maximum horizontal reservoir time */
	private JTextField txtfldMoveDASMax;

	/** Lateral movement interval */
	private JTextField txtfldMoveDASDelay;

	/** ReadyCan accumulate on the screen next to */
	private JCheckBox chkboxMoveDASInReady;

	/** First frame Can accumulate in the horizontal */
	private JCheckBox chkboxMoveDASInMoveFirstFrame;

	/** BlockPossible reservoir beside the moment it shines */
	private JCheckBox chkboxMoveDASInLockFlash;

	/** Line clearCan I accumulate in horizontal */
	private JCheckBox chkboxMoveDASInLineClear;

	/** ARECan I accumulate in horizontal */
	private JCheckBox chkboxMoveDASInARE;

	/** AREAt the end of the frame Can accumulate in the horizontal */
	private JCheckBox chkboxMoveDASInARELastFrame;

	/** EndingCan accumulate on the screen next to the inrush */
	private JCheckBox chkboxMoveDASInEndingStart;

	/** DAS charge on blocked move checkbox*/
	private JCheckBox chkboxMoveDASChargeOnBlockedMove;

	/** Store DAS Charge on neutral checkbox **/
	private JCheckBox chkboxMoveDASStoreChargeOnNeutral;

	/** Redirect in delay checkbox **/
	private JCheckBox chkboxMoveDASRedirectInDelay;

	/** First frame Can move in the */
	private JCheckBox chkboxMoveFirstFrame;

	/** Diagonal movement */
	private JCheckBox chkboxMoveDiagonal;

	/** Can be pressed together up and down */
	private JCheckBox chkboxMoveUpAndDown;

	/** Can simultaneously pressing the left and right */
	private JCheckBox chkboxMoveLeftAndRightAllow;

	/** Before when I press the left and right simultaneously frame Of input DirectionGive priority to */
	private JCheckBox chkboxMoveLeftAndRightUsePreviousInput;

	/** Shift lock checkbox */
	private JCheckBox chkboxMoveShiftLockEnable;

	//----------------------------------------------------------------------
	/* rotationPanel pattern correction */

	/** rotationPattern correction tab */
	private JTabbedPane tabPieceOffset;

	/** rotationPattern correction(X) input Column */
	private JTextField[][] txtfldPieceOffsetX;

	/** rotationPattern correction(Y) input Column */
	private JTextField[][] txtfldPieceOffsetY;

	//----------------------------------------------------------------------
	/* rotationPanel pattern correction */

	/** rotationPattern correction tab */
	private JTabbedPane tabPieceSpawn;

	/** Appearance position correction(X) input Column */
	private JTextField[][] txtfldPieceSpawnX;

	/** Appearance position correction(Y) input Column */
	private JTextField[][] txtfldPieceSpawnY;

	/** BigAppearance position correction during(X) input Column */
	private JTextField[][] txtfldPieceSpawnBigX;

	/** BigAppearance position correction during(Y) input Column */
	private JTextField[][] txtfldPieceSpawnBigY;

	//----------------------------------------------------------------------
	/* Panel color settings */

	/** Color selectionComboBox */
	private JComboBox[] comboboxPieceColor;

	//----------------------------------------------------------------------
	/* InitialDirectionSettings panel */

	/** InitialDirectionSelectionComboBox */
	private JComboBox[] comboboxPieceDirection;

	//----------------------------------------------------------------------
	/** BlockImage */
	private BufferedImage[] imgBlockSkins;

	private JButton generateBase64;
	
	private JButton loadBase64;
	
	private JTextField base64;

	public JTabbedPane getTabPane() {
		return tabPane;
	}
	
	/**
	 * Constructor
	 */
	public RuleEditorPanel() {
		super();

		init();
		readRuleToUI(new RuleOptions());
	}

	/**
	 * Initialization
	 */
	private void init() {
		// Read configuration file
		propConfig = new CustomProperties();
		try {
			ResourceInputStream in = new ResourceInputStream("config/setting/swing.cfg");
			propConfig.load(in);
			in.close();
		} catch(IOException e) {}

		strNowFile = null;

		loadBlockSkins();

		initUI();
	}

	/**
	 * ScreenInitialization
	 */
	private void initUI() {
		setLayout(new BorderLayout());

		// Entire tab --------------------------------------------------
		tabPane = new JTabbedPane();
		add(tabPane, BorderLayout.NORTH);

		// Preferences tab --------------------------------------------------
		JPanel panelBasic = new JPanel();
		panelBasic.setLayout(new BoxLayout(panelBasic, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_Basic"), panelBasic);

		// Rule name
		JPanel pRuleName = new JPanel();
		panelBasic.add(pRuleName);

		JLabel lRuleName = new JLabel(getUIText("Basic_RuleName"));
		pRuleName.add(lRuleName);

		txtfldRuleName = new JTextField("", 15);
		pRuleName.add(txtfldRuleName);

		// NEXTDisplaycount
		JPanel pNextDisplay = new JPanel();
		panelBasic.add(pNextDisplay);

		JLabel lNextDisplay = new JLabel(getUIText("Basic_NextDisplay"));
		pNextDisplay.add(lNextDisplay);

		txtfldNextDisplay = new JTextField("", 5);
		pNextDisplay.add(txtfldNextDisplay);

		// Game style
		JPanel pStyle = new JPanel();
		panelBasic.add(pStyle);

		JLabel lStyle = new JLabel(getUIText("Basic_Style"));
		pStyle.add(lStyle);

		comboboxStyle = new JComboBox(GameEngine.GAMESTYLE_NAMES);
		comboboxStyle.setPreferredSize(new Dimension(100, 30));
		pStyle.add(comboboxStyle);

		// Picture
		JPanel pSkin = new JPanel();
		panelBasic.add(pSkin);

		JLabel lSkin = new JLabel(getUIText("Basic_Skin"));
		pSkin.add(lSkin);

		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for(int i = 0; i < imgBlockSkins.length; i++) {
			model.addElement(new ComboLabel("" + i, new ImageIcon(imgBlockSkins[i])));
		}
		comboboxSkin = new JComboBox(model);
		comboboxSkin.setRenderer(new ComboLabelCellRenderer());
		comboboxSkin.setPreferredSize(new Dimension(190, 30));
		pSkin.add(comboboxSkin);

		// ghost
		chkboxGhost = new JCheckBox(getUIText("Basic_Ghost"));
		panelBasic.add(chkboxGhost);

		// fieldAttempts off target emerges from
		chkboxEnterAboveField = new JCheckBox(getUIText("Basic_EnterAboveField"));
		panelBasic.add(chkboxEnterAboveField);

		// When the planned site appearance is buriedY-coordinateSlide on theMaximum count
		JPanel pEnterMaxDistanceY = new JPanel();
		panelBasic.add(pEnterMaxDistanceY);

		JLabel lEnterMaxDistanceY = new JLabel(getUIText("Basic_EnterMaxDistanceY"));
		pEnterMaxDistanceY.add(lEnterMaxDistanceY);

		txtfldEnterMaxDistanceY = new JTextField("", 5);
		pEnterMaxDistanceY.add(txtfldEnterMaxDistanceY);

		// NEXTOrder generation algorithm
		JPanel pRandomizer = new JPanel();
		panelBasic.add(pRandomizer);

		JLabel lRandomizer = new JLabel(getUIText("Basic_Randomizer"));
		pRandomizer.add(lRandomizer);

//		vectorRandomizer = getTextFileVector("config/list/randomizer.lst");
		vectorRandomizer = new Vector<String>(Mappings.classSimpleName().map(Zeroflections.getRandomizers()));
		Collections.sort(vectorRandomizer);
		comboboxRandomizer = new JComboBox(createShortStringVector(vectorRandomizer));
		comboboxRandomizer.setPreferredSize(new Dimension(200, 30));
		pRandomizer.add(comboboxRandomizer);

		btnResetRandomizer = new JButton(getUIText("Basic_ResetRandomizer"));
		btnResetRandomizer.setMnemonic('R');
		btnResetRandomizer.setActionCommand("ResetRandomizer");
		btnResetRandomizer.addActionListener(this);
		pRandomizer.add(btnResetRandomizer);

		// fieldTab --------------------------------------------------
		JPanel panelField = new JPanel();
		panelField.setLayout(new BoxLayout(panelField, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_Field"), panelField);

		// fieldThe width of the
		JPanel pFieldWidth = new JPanel();
		panelField.add(pFieldWidth);

		JLabel lFieldWidth = new JLabel(getUIText("Field_FieldWidth"));
		pFieldWidth.add(lFieldWidth);

		txtfldFieldWidth = new JTextField("", 5);
		pFieldWidth.add(txtfldFieldWidth);

		// Field height
		JPanel pFieldHeight = new JPanel();
		panelField.add(pFieldHeight);

		JLabel lFieldHeight = new JLabel(getUIText("Field_FieldHeight"));
		pFieldHeight.add(lFieldHeight);

		txtfldFieldHeight = new JTextField("", 5);
		pFieldHeight.add(txtfldFieldHeight);

		// fieldThe height of the unseen parts of
		JPanel pFieldHiddenHeight = new JPanel();
		panelField.add(pFieldHiddenHeight);

		JLabel lFieldHiddenHeight = new JLabel(getUIText("Field_FieldHiddenHeight"));
		pFieldHiddenHeight.add(lFieldHiddenHeight);

		txtfldFieldHiddenHeight = new JTextField("", 5);
		pFieldHiddenHeight.add(txtfldFieldHiddenHeight);

		// fieldCeiling
		chkboxFieldCeiling = new JCheckBox(getUIText("Field_FieldCeiling"));
		panelField.add(chkboxFieldCeiling);

		// fieldI would not put to death in the frame
		chkboxFieldLockoutDeath = new JCheckBox(getUIText("Field_FieldLockoutDeath"));
		panelField.add(chkboxFieldLockoutDeath);

		// fieldAttempts off target to death after only protrude
		chkboxFieldPartialLockoutDeath = new JCheckBox(getUIText("Field_FieldPartialLockoutDeath"));
		panelField.add(chkboxFieldPartialLockoutDeath);

		// Hold tab --------------------------------------------------
		JPanel panelHold = new JPanel();
		panelHold.setLayout(new BoxLayout(panelHold, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_Hold"), panelHold);

		// Hold is enabled
		chkboxHoldEnable = new JCheckBox(getUIText("Hold_HoldEnable"));
		panelHold.add(chkboxHoldEnable);

		// Hold preceding
		chkboxHoldInitial = new JCheckBox(getUIText("Hold_HoldInitial"));
		panelHold.add(chkboxHoldInitial);

		// Can not hold prior continuous use
		chkboxHoldInitialLimit = new JCheckBox(getUIText("Hold_HoldInitialLimit"));
		panelHold.add(chkboxHoldInitialLimit);

		// When using the holdBlockThe orientation of the piece back to its initial state
		chkboxHoldResetDirection = new JCheckBox(getUIText("Hold_HoldResetDirection"));
		panelHold.add(chkboxHoldResetDirection);

		// You can hold count
		JPanel pHoldLimit = new JPanel();
		panelHold.add(pHoldLimit);

		JLabel lHoldLimit = new JLabel(getUIText("Hold_HoldLimit"));
		pHoldLimit.add(lHoldLimit);

		txtfldHoldLimit = new JTextField("", 5);
		pHoldLimit.add(txtfldHoldLimit);

		// Drop tab --------------------------------------------------
		JPanel panelDrop = new JPanel();
		panelDrop.setLayout(new BoxLayout(panelDrop, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_Drop"), panelDrop);

		// Hard dropAvailability
		chkboxDropHardDropEnable = new JCheckBox(getUIText("Drop_HardDropEnable"));
		panelDrop.add(chkboxDropHardDropEnable);

		// Hard dropImmediately fixed in
		chkboxDropHardDropLock = new JCheckBox(getUIText("Drop_HardDropLock"));
		panelDrop.add(chkboxDropHardDropLock);

		// Hard dropNot continuous use
		chkboxDropHardDropLimit = new JCheckBox(getUIText("Drop_HardDropLimit"));
		panelDrop.add(chkboxDropHardDropLimit);

		// Soft dropAvailability
		chkboxDropSoftDropEnable = new JCheckBox(getUIText("Drop_SoftDropEnable"));
		panelDrop.add(chkboxDropSoftDropEnable);

		// Soft dropImmediately fixed in
		chkboxDropSoftDropLock = new JCheckBox(getUIText("Drop_SoftDropLock"));
		panelDrop.add(chkboxDropSoftDropLock);

		// Soft dropNot continuous use
		chkboxDropSoftDropLimit = new JCheckBox(getUIText("Drop_SoftDropLimit"));
		panelDrop.add(chkboxDropSoftDropLimit);

		// In the ground stateSoft dropThen immediately fixed
		chkboxDropSoftDropSurfaceLock = new JCheckBox(getUIText("Drop_SoftDropSurfaceLock"));
		panelDrop.add(chkboxDropSoftDropSurfaceLock);

		// Soft dropSpeedCurrent × normal speednTo double
		chkboxDropSoftDropMultiplyNativeSpeed = new JCheckBox(getUIText("Drop_SoftDropMultiplyNativeSpeed"));
		panelDrop.add(chkboxDropSoftDropMultiplyNativeSpeed);

		// Use new soft drop codes
		chkboxDropSoftDropGravitySpeedLimit = new JCheckBox(getUIText("Drop_SoftDropGravitySpeedLimit"));
		panelDrop.add(chkboxDropSoftDropGravitySpeedLimit);

		// Soft dropSpeed
		JPanel pDropSoftDropSpeed = new JPanel();
		panelDrop.add(pDropSoftDropSpeed);
		JLabel lDropSoftDropSpeed = new JLabel(getUIText("Drop_SoftDropSpeed"));
		pDropSoftDropSpeed.add(lDropSoftDropSpeed);

		txtfldDropSoftDropSpeed = new JTextField("", 5);
		pDropSoftDropSpeed.add(txtfldDropSoftDropSpeed);

		// rotationTab --------------------------------------------------
		JPanel panelRotate = new JPanel();
		panelRotate.setLayout(new BoxLayout(panelRotate, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_Rotate"), panelRotate);

		// Precedingrotation
		chkboxRotateInitial = new JCheckBox(getUIText("Rotate_RotateInitial"));
		panelRotate.add(chkboxRotateInitial);

		// PrecedingrotationNot continuous use
		chkboxRotateInitialLimit = new JCheckBox(getUIText("Rotate_RotateInitialLimit"));
		panelRotate.add(chkboxRotateInitialLimit);

		// Wallkick
		chkboxRotateWallkick = new JCheckBox(getUIText("Rotate_RotateWallkick"));
		panelRotate.add(chkboxRotateWallkick);

		// PrecedingrotationButWallkickMake
		chkboxRotateInitialWallkick = new JCheckBox(getUIText("Rotate_RotateInitialWallkick"));
		panelRotate.add(chkboxRotateInitialWallkick);

		// ARight onrotation
		chkboxRotateButtonDefaultRight = new JCheckBox(getUIText("Rotate_RotateButtonDefaultRight"));
		panelRotate.add(chkboxRotateButtonDefaultRight);

		// ReverserotationPermit
		chkboxRotateButtonAllowReverse = new JCheckBox(getUIText("Rotate_RotateButtonAllowReverse"));
		panelRotate.add(chkboxRotateButtonAllowReverse);

		// 2rotationPermit
		chkboxRotateButtonAllowDouble = new JCheckBox(getUIText("Rotate_RotateButtonAllowDouble"));
		panelRotate.add(chkboxRotateButtonAllowDouble);

		// UpDirectionToWallkickAble to count
		JPanel pRotateMaxUpwardWallkick = new JPanel();
		panelRotate.add(pRotateMaxUpwardWallkick);
		JLabel lRotateMaxUpwardWallkick = new JLabel(getUIText("Rotate_RotateMaxUpwardWallkick"));
		pRotateMaxUpwardWallkick.add(lRotateMaxUpwardWallkick);

		txtfldRotateMaxUpwardWallkick = new JTextField("", 5);
		pRotateMaxUpwardWallkick.add(txtfldRotateMaxUpwardWallkick);

		// WallkickAlgorithm
		JPanel pWallkickSystem = new JPanel();
		panelRotate.add(pWallkickSystem);

		JLabel lWallkickSystem = new JLabel(getUIText("Rotate_WallkickSystem"));
		pWallkickSystem.add(lWallkickSystem);

		vectorWallkickSystem = getTextFileVector("config/list/wallkick.lst");
		comboboxWallkickSystem = new JComboBox(createShortStringVector(vectorWallkickSystem));
		comboboxWallkickSystem.setPreferredSize(new Dimension(200, 30));
		pWallkickSystem.add(comboboxWallkickSystem);

		btnResetWallkickSystem = new JButton(getUIText("Rotate_ResetWallkickSystem"));
		btnResetWallkickSystem.setMnemonic('R');
		btnResetWallkickSystem.setActionCommand("ResetWallkickSystem");
		btnResetWallkickSystem.addActionListener(this);
		pWallkickSystem.add(btnResetWallkickSystem);

		// Fixation timeTab --------------------------------------------------
		JPanel panelLockDelay = new JPanel();
		panelLockDelay.setLayout(new BoxLayout(panelLockDelay, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_LockDelay"), panelLockDelay);

		// Minimum fixed timeAnd the highest fixed time
		JLabel lLockDelayMin = new JLabel(getUIText("LockDelay_LockDelayMinMax"));
		panelLockDelay.add(lLockDelayMin);

		JPanel pLockDelayMinMax = new JPanel();
		panelLockDelay.add(pLockDelayMinMax);

		txtfldLockDelayMin = new JTextField("", 5);
		pLockDelayMinMax.add(txtfldLockDelayMin);
		txtfldLockDelayMax = new JTextField("", 5);
		pLockDelayMinMax.add(txtfldLockDelayMax);

		// In the fall fixing timeReset
		chkboxLockDelayLockResetFall = new JCheckBox(getUIText("LockDelay_LockResetFall"));
		panelLockDelay.add(chkboxLockDelayLockResetFall);

		// Move fixed timeReset
		chkboxLockDelayLockResetMove = new JCheckBox(getUIText("LockDelay_LockResetMove"));
		panelLockDelay.add(chkboxLockDelayLockResetMove);

		// rotationFixed at timeReset
		chkboxLockDelayLockResetRotate = new JCheckBox(getUIText("LockDelay_LockResetRotate"));
		panelLockDelay.add(chkboxLockDelayLockResetRotate);

		// Lock delay reset by wallkick
		chkboxLockDelayLockResetWallkick = new JCheckBox(getUIText("LockDelay_LockResetWallkick"));
		panelLockDelay.add(chkboxLockDelayLockResetWallkick);

		// Lateral motion counterAndrotation counterShare (Lateral motion counterI use only)
		chkboxLockDelayLockResetLimitShareCount = new JCheckBox(getUIText("LockDelay_LockDelayLockResetLimitShareCount"));
		panelLockDelay.add(chkboxLockDelayLockResetLimitShareCount);

		// Lateral motion countLimit
		JPanel pLockDelayLockResetLimitMove = new JPanel();
		panelLockDelay.add(pLockDelayLockResetLimitMove);
		JLabel lLockDelayLockResetLimitMove = new JLabel(getUIText("LockDelay_LockDelayLockResetLimitMove"));
		pLockDelayLockResetLimitMove.add(lLockDelayLockResetLimitMove);

		txtfldLockDelayLockResetLimitMove = new JTextField("", 5);
		pLockDelayLockResetLimitMove.add(txtfldLockDelayLockResetLimitMove);

		// rotation countLimit
		JPanel pLockDelayLockResetLimitRotate = new JPanel();
		panelLockDelay.add(pLockDelayLockResetLimitRotate);
		JLabel lLockDelayLockResetLimitRotate = new JLabel(getUIText("LockDelay_LockDelayLockResetLimitRotate"));
		pLockDelayLockResetLimitRotate.add(lLockDelayLockResetLimitRotate);

		txtfldLockDelayLockResetLimitRotate = new JTextField("", 5);
		pLockDelayLockResetLimitRotate.add(txtfldLockDelayLockResetLimitRotate);

		// Move orrotation countset when limit is exceeded
		JPanel pLockDelayLockResetLimitOver = new JPanel();
		pLockDelayLockResetLimitOver.setLayout(new BoxLayout(pLockDelayLockResetLimitOver, BoxLayout.Y_AXIS));
		panelLockDelay.add(pLockDelayLockResetLimitOver);

		JLabel lLockDelayLockResetLimitOver = new JLabel(getUIText("LockDelay_LockDelayLockResetLimitOver"));
		pLockDelayLockResetLimitOver.add(lLockDelayLockResetLimitOver);

		ButtonGroup gLockDelayLockResetLimitOver = new ButtonGroup();

		radioLockDelayLockResetLimitOverNoReset = new JRadioButton(getUIText("LockDelay_LockDelayLockResetLimitOverNoReset"));
		pLockDelayLockResetLimitOver.add(radioLockDelayLockResetLimitOverNoReset);
		gLockDelayLockResetLimitOver.add(radioLockDelayLockResetLimitOverNoReset);

		radioLockDelayLockResetLimitOverInstant = new JRadioButton(getUIText("LockDelay_LockDelayLockResetLimitOverInstant"));
		pLockDelayLockResetLimitOver.add(radioLockDelayLockResetLimitOverInstant);
		gLockDelayLockResetLimitOver.add(radioLockDelayLockResetLimitOverInstant);

		radioLockDelayLockResetLimitOverNoWallkick = new JRadioButton(getUIText("LockDelay_LockDelayLockResetLimitOverNoWallkick"));
		pLockDelayLockResetLimitOver.add(radioLockDelayLockResetLimitOverNoWallkick);
		gLockDelayLockResetLimitOver.add(radioLockDelayLockResetLimitOverNoWallkick);

		// ARETab --------------------------------------------------
		JPanel panelARE = new JPanel();
		panelARE.setLayout(new BoxLayout(panelARE, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_ARE"), panelARE);

		// LowestAREAnd bestARE
		JLabel lAREMin = new JLabel(getUIText("ARE_MinMax"));
		panelARE.add(lAREMin);

		JPanel pAREMinMax = new JPanel();
		panelARE.add(pAREMinMax);

		txtfldAREMin = new JTextField("", 5);
		pAREMinMax.add(txtfldAREMin);
		txtfldAREMax = new JTextField("", 5);
		pAREMinMax.add(txtfldAREMax);

		// LowestARE after line clearAnd bestARE after line clear
		JLabel lARELineMin = new JLabel(getUIText("ARE_LineMinMax"));
		panelARE.add(lARELineMin);

		JPanel pARELineMinMax = new JPanel();
		panelARE.add(pARELineMinMax);

		txtfldARELineMin = new JTextField("", 5);
		pARELineMinMax.add(txtfldARELineMin);
		txtfldARELineMax = new JTextField("", 5);
		pARELineMinMax.add(txtfldARELineMax);

		// Shining moment fixed frame count
		JLabel lARELockFlash = new JLabel(getUIText("ARE_LockFlash"));
		panelARE.add(lARELockFlash);

		JPanel pARELockFlash = new JPanel();
		panelARE.add(pARELockFlash);

		txtfldARELockFlash = new JTextField("", 5);
		pARELockFlash.add(txtfldARELockFlash);

		// BlockDedicated shines frame Put
		chkboxARELockFlashOnlyFrame = new JCheckBox(getUIText("ARE_LockFlashOnlyFrame"));
		panelARE.add(chkboxARELockFlashOnlyFrame);

		// Line clearBeforeBlockShine frame Put
		chkboxARELockFlashBeforeLineClear = new JCheckBox(getUIText("ARE_LockFlashBeforeLineClear"));
		panelARE.add(chkboxARELockFlashBeforeLineClear);

		// ARE cancel on move
		chkboxARECancelMove = new JCheckBox(getUIText("ARE_CancelMove"));
		panelARE.add(chkboxARECancelMove);

		// ARE cancel on move
		chkboxARECancelRotate = new JCheckBox(getUIText("ARE_CancelRotate"));
		panelARE.add(chkboxARECancelRotate);

		// ARE cancel on move
		chkboxARECancelHold = new JCheckBox(getUIText("ARE_CancelHold"));
		panelARE.add(chkboxARECancelHold);

		// Line clearTab --------------------------------------------------
		JPanel panelLine = new JPanel();
		panelLine.setLayout(new BoxLayout(panelLine, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_Line"), panelLine);

		// LowestLine clear timeAnd bestLine clear time
		JLabel lLineMin = new JLabel(getUIText("Line_MinMax"));
		panelLine.add(lLineMin);

		JPanel pLineMinMax = new JPanel();
		panelLine.add(pLineMinMax);

		txtfldLineDelayMin = new JTextField("", 5);
		pLineMinMax.add(txtfldLineDelayMin);
		txtfldLineDelayMax = new JTextField("", 5);
		pLineMinMax.add(txtfldLineDelayMax);

		// Animated falling
		chkboxLineFallAnim = new JCheckBox(getUIText("Line_FallAnim"));
		panelLine.add(chkboxLineFallAnim);

		// Line delay cancel on move
		chkboxLineCancelMove = new JCheckBox(getUIText("Line_CancelMove"));
		panelLine.add(chkboxLineCancelMove);

		// Line delay cancel on rotate
		chkboxLineCancelRotate = new JCheckBox(getUIText("Line_CancelRotate"));
		panelLine.add(chkboxLineCancelRotate);

		// Line delay cancel on hold
		chkboxLineCancelHold = new JCheckBox(getUIText("Line_CancelHold"));
		panelLine.add(chkboxLineCancelHold);

		// Move tab --------------------------------------------------
		JPanel panelMove = new JPanel();
		panelMove.setLayout(new BoxLayout(panelMove, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_Move"), panelMove);

		// Minimum horizontal reservoir timeAnd maximum horizontal reservoir time
		JLabel lMoveDASMin = new JLabel(getUIText("Move_DASMinMax"));
		panelMove.add(lMoveDASMin);

		JPanel pMoveDASMinMax = new JPanel();
		panelMove.add(pMoveDASMinMax);

		txtfldMoveDASMin = new JTextField("", 5);
		pMoveDASMinMax.add(txtfldMoveDASMin);
		txtfldMoveDASMax = new JTextField("", 5);
		pMoveDASMinMax.add(txtfldMoveDASMax);

		// Lateral movement interval
		JPanel pMoveDASDelay = new JPanel();
		panelMove.add(pMoveDASDelay);

		JLabel lMoveDASDelay1 = new JLabel(getUIText("Move_DASDelay1"));
		pMoveDASDelay.add(lMoveDASDelay1);

		txtfldMoveDASDelay = new JTextField("", 5);
		pMoveDASDelay.add(txtfldMoveDASDelay);

		JLabel lMoveDASDelay2 = new JLabel(getUIText("Move_DASDelay2"));
		pMoveDASDelay.add(lMoveDASDelay2);

		// ○ ○ I collect next possible time
		chkboxMoveDASInReady = new JCheckBox(getUIText("Move_DASInReady"));
		panelMove.add(chkboxMoveDASInReady);
		chkboxMoveDASInMoveFirstFrame = new JCheckBox(getUIText("Move_DASInMoveFirstFrame"));
		panelMove.add(chkboxMoveDASInMoveFirstFrame);
		chkboxMoveDASInLockFlash = new JCheckBox(getUIText("Move_DASInLockFlash"));
		panelMove.add(chkboxMoveDASInLockFlash);
		chkboxMoveDASInLineClear = new JCheckBox(getUIText("Move_DASInLineClear"));
		panelMove.add(chkboxMoveDASInLineClear);
		chkboxMoveDASInARE = new JCheckBox(getUIText("Move_DASInARE"));
		panelMove.add(chkboxMoveDASInARE);
		chkboxMoveDASInARELastFrame = new JCheckBox(getUIText("Move_DASInARELastFrame"));
		panelMove.add(chkboxMoveDASInARELastFrame);
		chkboxMoveDASInEndingStart = new JCheckBox(getUIText("Move_DASInEndingStart"));
		panelMove.add(chkboxMoveDASInEndingStart);
		chkboxMoveDASChargeOnBlockedMove = new JCheckBox(getUIText("Move_DASChargeOnBlockedMove"));
		panelMove.add(chkboxMoveDASChargeOnBlockedMove);
		chkboxMoveDASStoreChargeOnNeutral = new JCheckBox(getUIText("Move_DASStoreChargeOnNeutral"));
      panelMove.add(chkboxMoveDASStoreChargeOnNeutral);
      chkboxMoveDASRedirectInDelay = new JCheckBox(getUIText("Move_DASRedirectInDelay"));
      panelMove.add(chkboxMoveDASRedirectInDelay);

		// First frame Can move in the
		chkboxMoveFirstFrame = new JCheckBox(getUIText("Move_FirstFrame"));
		panelMove.add(chkboxMoveFirstFrame);

		// Diagonal movement
		chkboxMoveDiagonal = new JCheckBox(getUIText("Move_Diagonal"));
		panelMove.add(chkboxMoveDiagonal);

		// UpUnder simultaneous press
		chkboxMoveUpAndDown = new JCheckBox(getUIText("Move_UpAndDown"));
		panelMove.add(chkboxMoveUpAndDown);

		// Simultaneously press the left and right
		chkboxMoveLeftAndRightAllow = new JCheckBox(getUIText("Move_LeftAndRightAllow"));
		panelMove.add(chkboxMoveLeftAndRightAllow);

		// Before when I press the left and right simultaneously frame Of input Priority
		chkboxMoveLeftAndRightUsePreviousInput = new JCheckBox(getUIText("Move_LeftAndRightUsePreviousInput"));
		panelMove.add(chkboxMoveLeftAndRightUsePreviousInput);

		// Shift lock
		chkboxMoveShiftLockEnable = new JCheckBox(getUIText("Move_ShiftLock"));
		panelMove.add(chkboxMoveShiftLockEnable);

		// rotationPattern correction tab ------------------------------------------------
		JPanel panelPieceOffset = new JPanel();
		panelPieceOffset.setLayout(new BoxLayout(panelPieceOffset, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_PieceOffset"), panelPieceOffset);

		tabPieceOffset = new JTabbedPane();
		panelPieceOffset.add(tabPieceOffset);

		// rotationPattern correction(X)Tab --------------------------------------------------
		JPanel panelPieceOffsetX = new JPanel();
		panelPieceOffsetX.setLayout(new BoxLayout(panelPieceOffsetX, BoxLayout.Y_AXIS));
		tabPieceOffset.addTab(getUIText("TabName_PieceOffsetX"), panelPieceOffsetX);

		JPanel[] pPieceOffsetX = new JPanel[Piece.PIECE_COUNT];

		txtfldPieceOffsetX = new JTextField[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			pPieceOffsetX[i] = new JPanel();
			panelPieceOffsetX.add(pPieceOffsetX[i]);

			JLabel lPieceName = new JLabel(getUIText("PieceName" + i));
			pPieceOffsetX[i].add(lPieceName);

			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				txtfldPieceOffsetX[i][j] = new JTextField("", 5);
				pPieceOffsetX[i].add(txtfldPieceOffsetX[i][j]);
			}
		}

		// rotationPattern correction(Y)Tab --------------------------------------------------
		JPanel panelPieceOffsetY = new JPanel();
		panelPieceOffsetY.setLayout(new BoxLayout(panelPieceOffsetY, BoxLayout.Y_AXIS));
		tabPieceOffset.addTab(getUIText("TabName_PieceOffsetY"), panelPieceOffsetY);

		JPanel[] pPieceOffsetY = new JPanel[Piece.PIECE_COUNT];

		txtfldPieceOffsetY = new JTextField[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			pPieceOffsetY[i] = new JPanel();
			panelPieceOffsetY.add(pPieceOffsetY[i]);

			JLabel lPieceName = new JLabel(getUIText("PieceName" + i));
			pPieceOffsetY[i].add(lPieceName);

			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				txtfldPieceOffsetY[i][j] = new JTextField("", 5);
				pPieceOffsetY[i].add(txtfldPieceOffsetY[i][j]);
			}
		}

		// Correction tab appearance position ------------------------------------------------
		JPanel panelPieceSpawn = new JPanel();
		panelPieceSpawn.setLayout(new BoxLayout(panelPieceSpawn, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_PieceSpawn"), panelPieceSpawn);

		tabPieceSpawn = new JTabbedPane();
		panelPieceSpawn.add(tabPieceSpawn);

		// Appearance position correction(X)Tab --------------------------------------------------
		JPanel panelPieceSpawnX = new JPanel();
		panelPieceSpawnX.setLayout(new BoxLayout(panelPieceSpawnX, BoxLayout.Y_AXIS));
		tabPieceSpawn.addTab(getUIText("TabName_PieceSpawnX"), panelPieceSpawnX);

		JPanel[] pPieceSpawnX = new JPanel[Piece.PIECE_COUNT];

		txtfldPieceSpawnX = new JTextField[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			pPieceSpawnX[i] = new JPanel();
			panelPieceSpawnX.add(pPieceSpawnX[i]);

			JLabel lPieceName = new JLabel(getUIText("PieceName" + i));
			pPieceSpawnX[i].add(lPieceName);

			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				txtfldPieceSpawnX[i][j] = new JTextField("", 5);
				pPieceSpawnX[i].add(txtfldPieceSpawnX[i][j]);
			}
		}

		// Appearance position correction(Y)Tab --------------------------------------------------
		JPanel panelPieceSpawnY = new JPanel();
		panelPieceSpawnY.setLayout(new BoxLayout(panelPieceSpawnY, BoxLayout.Y_AXIS));
		tabPieceSpawn.addTab(getUIText("TabName_PieceSpawnY"), panelPieceSpawnY);

		JPanel[] pPieceSpawnY = new JPanel[Piece.PIECE_COUNT];

		txtfldPieceSpawnY = new JTextField[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			pPieceSpawnY[i] = new JPanel();
			panelPieceSpawnY.add(pPieceSpawnY[i]);

			JLabel lPieceName = new JLabel(getUIText("PieceName" + i));
			pPieceSpawnY[i].add(lPieceName);

			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				txtfldPieceSpawnY[i][j] = new JTextField("", 5);
				pPieceSpawnY[i].add(txtfldPieceSpawnY[i][j]);
			}
		}

		// BigAppearance position correction during(X)Tab --------------------------------------------------
		JPanel panelPieceSpawnBigX = new JPanel();
		panelPieceSpawnBigX.setLayout(new BoxLayout(panelPieceSpawnBigX, BoxLayout.Y_AXIS));
		tabPieceSpawn.addTab(getUIText("TabName_PieceSpawnBigX"), panelPieceSpawnBigX);

		JPanel[] pPieceSpawnBigX = new JPanel[Piece.PIECE_COUNT];

		txtfldPieceSpawnBigX = new JTextField[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			pPieceSpawnBigX[i] = new JPanel();
			panelPieceSpawnBigX.add(pPieceSpawnBigX[i]);

			JLabel lPieceName = new JLabel(getUIText("PieceName" + i));
			pPieceSpawnBigX[i].add(lPieceName);

			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				txtfldPieceSpawnBigX[i][j] = new JTextField("", 5);
				pPieceSpawnBigX[i].add(txtfldPieceSpawnBigX[i][j]);
			}
		}

		// BigAppearance position correction during(Y)Tab --------------------------------------------------
		JPanel panelPieceSpawnBigY = new JPanel();
		panelPieceSpawnBigY.setLayout(new BoxLayout(panelPieceSpawnBigY, BoxLayout.Y_AXIS));
		tabPieceSpawn.addTab(getUIText("TabName_PieceSpawnBigY"), panelPieceSpawnBigY);

		JPanel[] pPieceSpawnBigY = new JPanel[Piece.PIECE_COUNT];

		txtfldPieceSpawnBigY = new JTextField[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			pPieceSpawnBigY[i] = new JPanel();
			panelPieceSpawnBigY.add(pPieceSpawnBigY[i]);

			JLabel lPieceName = new JLabel(getUIText("PieceName" + i));
			pPieceSpawnBigY[i].add(lPieceName);

			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				txtfldPieceSpawnBigY[i][j] = new JTextField("", 5);
				pPieceSpawnBigY[i].add(txtfldPieceSpawnBigY[i][j]);
			}
		}

		// Color Settings tab --------------------------------------------------
		JPanel panelPieceColor = new JPanel();
		panelPieceColor.setLayout(new BoxLayout(panelPieceColor, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_PieceColor"), panelPieceColor);

		String[] strColorNames = new String[Block.BLOCK_COLOR_COUNT - 1];
		for(int i = 0; i < strColorNames.length; i++) strColorNames[i] = getUIText("ColorName" + i);

		JPanel[] pPieceColor = new JPanel[Piece.PIECE_COUNT];

		comboboxPieceColor = new JComboBox[Piece.PIECE_COUNT];
		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			pPieceColor[i] = new JPanel();
			panelPieceColor.add(pPieceColor[i]);

			JLabel lPieceName = new JLabel(getUIText("PieceName" + i));
			pPieceColor[i].add(lPieceName);

			comboboxPieceColor[i] = new JComboBox(strColorNames);
			comboboxPieceColor[i].setPreferredSize(new Dimension(100, 30));
			comboboxPieceColor[i].setMaximumRowCount(strColorNames.length);
			pPieceColor[i].add(comboboxPieceColor[i]);
		}

		// InitialDirectionSettings tab --------------------------------------------------
		JPanel panelPieceDirection = new JPanel();
		panelPieceDirection.setLayout(new BoxLayout(panelPieceDirection, BoxLayout.Y_AXIS));
		tabPane.addTab(getUIText("TabName_PieceDirection"), panelPieceDirection);

		String[] strDirectionNames = new String[Piece.DIRECTION_COUNT + 1];
		for(int i = 0; i < strDirectionNames.length; i++) strDirectionNames[i] = getUIText("DirectionName" + i);

		JPanel[] pPieceDirection = new JPanel[Piece.PIECE_COUNT];

		comboboxPieceDirection = new JComboBox[Piece.PIECE_COUNT];
		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			pPieceDirection[i] = new JPanel();
			panelPieceDirection.add(pPieceDirection[i]);

			JLabel lPieceName = new JLabel(getUIText("PieceName" + i));
			pPieceDirection[i].add(lPieceName);

			comboboxPieceDirection[i] = new JComboBox(strDirectionNames);
			comboboxPieceDirection[i].setPreferredSize(new Dimension(150, 30));
			comboboxPieceDirection[i].setMaximumRowCount(strDirectionNames.length);
			pPieceDirection[i].add(comboboxPieceDirection[i]);
		}
		
		// Base 64 store/load
		JPanel base64Panel = new JPanel(new GridBagLayout());
		tabPane.addTab(getUIText("TabName_Base64"), base64Panel);
		GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,10,10), 0, 0);
		
		generateBase64 = new JButton(getUIText("Base64_Generate"));
		generateBase64.setActionCommand("Base64_Generate");
		generateBase64.addActionListener(this);
		base64Panel.add(generateBase64, c);
		
		c.gridx++;
		loadBase64 = new JButton(getUIText("Base64_Load"));
		loadBase64.setActionCommand("Base64_Load");
		loadBase64.addActionListener(this);
		base64Panel.add(loadBase64, c);
		
		c.gridx = 0; c.gridy++; c.gridwidth = 2;
		base64 = new JTextField("", 80);
		base64Panel.add(base64, c);
		
	}

	/**
	 * BlockLoad an image
	 */
	private void loadBlockSkins() {
		String skindir = propConfig.getProperty("custom.skin.directory", "res");

		int numBlocks = 0;
		numBlocks = Zeroflections.getResources(Pattern.compile("^org/zeromeaner/res/graphics/blockskin/normal/n\\d+\\.png$")).size();
		
		log.debug(numBlocks + " block skins found");

		imgBlockSkins = new BufferedImage[numBlocks];

		for(int i = 0; i < numBlocks; i++) {
			BufferedImage imgBlock = (BufferedImage) loadImage(getURL(skindir + "/graphics/blockskin/normal/n" + i + ".png"));
			boolean isSticky = ((imgBlock != null) && (imgBlock.getWidth() >= 400) && (imgBlock.getHeight() >= 304));

			imgBlockSkins[i] = new BufferedImage(144, 16, BufferedImage.TYPE_INT_RGB);

			if(isSticky) {
				for(int j = 0; j < 9; j++) {
					imgBlockSkins[i].getGraphics().drawImage(imgBlock, j * 16, 0, (j * 16) + 16, 16, 0, j * 16, 16, (j * 16) + 16, null);
				}
			} else {
				imgBlockSkins[i].getGraphics().drawImage(imgBlock, 0, 0, 144, 16, 0, 0, 144, 16, null);
			}
		}
	}

	/**
	 * Load an image
	 * @param url Image filesURL
	 * @return Image file (Failurenull)
	 */
	public BufferedImage loadImage(URL url) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(url);
			log.debug("Loaded image from " + url);
		} catch (IOException e) {
			log.error("Failed to load image from " + url, e);
		}
		return img;
	}

	/**
	 * Resource FilesURLReturns
	 * @param str Filename
	 * @return Resource FilesURL
	 */
	public URL getURL(String str) {
		URL url = null;

		url = RuleEditorPanel.class.getClassLoader().getResource("org/zeromeaner/" + str);

		return url;
	}

	/**
	 * Read the text fileVector&lt;String&gt;Add to
	 * @param filename Filename
	 * @return I read a text fileVector&lt;String&gt;
	 */
	public Vector<String> getTextFileVector(String filename) {
		Vector<String> vec = new Vector<String>();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new ResourceInputStream(filename)));

			while(true) {
				String str = in.readLine();
				if((str == null) || (str.length() <= 0)) break;
				vec.add(str);
			}
		} catch (IOException e) {}

		return vec;
	}

	/**
	 * SpecificVector&lt;String&gt;Only the target was removed from the last dot symbolVector&lt;String&gt;Create
	 * @param vecSrc OriginalVector&lt;String&gt;
	 * @return Was processedVector&lt;String&gt;
	 */
	public Vector<String> createShortStringVector(Vector<String> vecSrc) {
		Vector<String> vec = new Vector<String>();

		for(int i = 0; i < vecSrc.size(); i++) {
			String str = vecSrc.get(i);
			int last = str.lastIndexOf('.');

			String newStr = "";
			if(last != -1) {
				newStr = str.substring(last + 1);
			} else {
				newStr = str;
			}

			vec.add(newStr);
		}

		return vec;
	}

	/**
	 * A rule setUIBe reflected in the
	 * @param r Rule Set
	 */
	public void readRuleToUI(RuleOptions r) {
		txtfldRuleName.setText(String.valueOf(r.strRuleName));
		txtfldNextDisplay.setText(String.valueOf(r.nextDisplay));
		comboboxStyle.setSelectedIndex(r.style);
		comboboxSkin.setSelectedIndex(r.skin);
		chkboxGhost.setSelected(r.ghost);
		chkboxEnterAboveField.setSelected(r.pieceEnterAboveField);
		txtfldEnterMaxDistanceY.setText(String.valueOf(r.pieceEnterMaxDistanceY));
		int indexRandomizer = vectorRandomizer.indexOf(r.strRandomizer.substring(r.strRandomizer.lastIndexOf('.') + 1));
		comboboxRandomizer.setSelectedIndex(indexRandomizer);

		txtfldFieldWidth.setText(String.valueOf(r.fieldWidth));
		txtfldFieldHeight.setText(String.valueOf(r.fieldHeight));
		txtfldFieldHiddenHeight.setText(String.valueOf(r.fieldHiddenHeight));
		chkboxFieldCeiling.setSelected(r.fieldCeiling);
		chkboxFieldLockoutDeath.setSelected(r.fieldLockoutDeath);
		chkboxFieldPartialLockoutDeath.setSelected(r.fieldPartialLockoutDeath);

		chkboxHoldEnable.setSelected(r.holdEnable);
		chkboxHoldInitial.setSelected(r.holdInitial);
		chkboxHoldInitialLimit.setSelected(r.holdInitialLimit);
		chkboxHoldResetDirection.setSelected(r.holdResetDirection);
		txtfldHoldLimit.setText(String.valueOf(r.holdLimit));

		chkboxDropHardDropEnable.setSelected(r.harddropEnable);
		chkboxDropHardDropLock.setSelected(r.harddropLock);
		chkboxDropHardDropLimit.setSelected(r.harddropLimit);
		chkboxDropSoftDropEnable.setSelected(r.softdropEnable);
		chkboxDropSoftDropLock.setSelected(r.softdropLock);
		chkboxDropSoftDropLimit.setSelected(r.softdropLimit);
		chkboxDropSoftDropSurfaceLock.setSelected(r.softdropSurfaceLock);
		txtfldDropSoftDropSpeed.setText(String.valueOf(r.softdropSpeed));
		chkboxDropSoftDropMultiplyNativeSpeed.setSelected(r.softdropMultiplyNativeSpeed);
		chkboxDropSoftDropGravitySpeedLimit.setSelected(r.softdropGravitySpeedLimit);

		chkboxRotateInitial.setSelected(r.rotateInitial);
		chkboxRotateInitialLimit.setSelected(r.rotateInitialLimit);
		chkboxRotateWallkick.setSelected(r.rotateWallkick);
		chkboxRotateInitialWallkick.setSelected(r.rotateInitialWallkick);
		txtfldRotateMaxUpwardWallkick.setText(String.valueOf(r.rotateMaxUpwardWallkick));
		chkboxRotateButtonDefaultRight.setSelected(r.rotateButtonDefaultRight);
		chkboxRotateButtonAllowReverse.setSelected(r.rotateButtonAllowReverse);
		chkboxRotateButtonAllowDouble.setSelected(r.rotateButtonAllowDouble);
		int indexWallkick = vectorWallkickSystem.indexOf(r.strWallkick);
		if(indexWallkick == -1)
			indexWallkick = vectorWallkickSystem.indexOf(StandardWallkick.class.getName());
		comboboxWallkickSystem.setSelectedIndex(indexWallkick);
		
		txtfldLockDelayMin.setText(String.valueOf(r.minLockDelay));
		txtfldLockDelayMax.setText(String.valueOf(r.maxLockDelay));
		chkboxLockDelayLockResetFall.setSelected(r.lockresetFall);
		chkboxLockDelayLockResetMove.setSelected(r.lockresetMove);
		chkboxLockDelayLockResetRotate.setSelected(r.lockresetRotate);
		chkboxLockDelayLockResetWallkick.setSelected(r.lockresetWallkick);
		chkboxLockDelayLockResetLimitShareCount.setSelected(r.lockresetLimitShareCount);
		txtfldLockDelayLockResetLimitMove.setText(String.valueOf(r.lockresetLimitMove));
		txtfldLockDelayLockResetLimitRotate.setText(String.valueOf(r.lockresetLimitRotate));
		if(r.lockresetLimitOver == RuleOptions.LOCKRESET_LIMIT_OVER_NORESET)
			radioLockDelayLockResetLimitOverNoReset.setSelected(true);
		else if(r.lockresetLimitOver == RuleOptions.LOCKRESET_LIMIT_OVER_INSTANT)
			radioLockDelayLockResetLimitOverInstant.setSelected(true);
		else if(r.lockresetLimitOver == RuleOptions.LOCKRESET_LIMIT_OVER_NOWALLKICK)
			radioLockDelayLockResetLimitOverNoWallkick.setSelected(true);

		txtfldAREMin.setText(String.valueOf(r.minARE));
		txtfldAREMax.setText(String.valueOf(r.maxARE));
		txtfldARELineMin.setText(String.valueOf(r.minARELine));
		txtfldARELineMax.setText(String.valueOf(r.maxARELine));
		txtfldARELockFlash.setText(String.valueOf(r.lockflash));
		chkboxARELockFlashOnlyFrame.setSelected(r.lockflashOnlyFrame);
		chkboxARELockFlashBeforeLineClear.setSelected(r.lockflashBeforeLineClear);
		chkboxARECancelMove.setSelected(r.areCancelMove);
		chkboxARECancelRotate.setSelected(r.areCancelRotate);
		chkboxARECancelHold.setSelected(r.areCancelHold);

		txtfldLineDelayMin.setText(String.valueOf(r.minLineDelay));
		txtfldLineDelayMax.setText(String.valueOf(r.maxLineDelay));
		chkboxLineFallAnim.setSelected(r.lineFallAnim);
		chkboxLineCancelMove.setSelected(r.lineCancelMove);
		chkboxLineCancelRotate.setSelected(r.lineCancelRotate);
		chkboxLineCancelHold.setSelected(r.lineCancelHold);

		txtfldMoveDASMin.setText(String.valueOf(r.minDAS));
		txtfldMoveDASMax.setText(String.valueOf(r.maxDAS));
		txtfldMoveDASDelay.setText(String.valueOf(r.dasDelay));
		chkboxMoveDASInReady.setSelected(r.dasInReady);
		chkboxMoveDASInMoveFirstFrame.setSelected(r.dasInMoveFirstFrame);
		chkboxMoveDASInLockFlash.setSelected(r.dasInLockFlash);
		chkboxMoveDASInLineClear.setSelected(r.dasInLineClear);
		chkboxMoveDASInARE.setSelected(r.dasInARE);
		chkboxMoveDASInARELastFrame.setSelected(r.dasInARELastFrame);
		chkboxMoveDASInEndingStart.setSelected(r.dasInEndingStart);
		chkboxMoveDASChargeOnBlockedMove.setSelected(r.dasChargeOnBlockedMove);
		chkboxMoveDASStoreChargeOnNeutral.setSelected(r.dasStoreChargeOnNeutral);
		chkboxMoveDASRedirectInDelay.setSelected(r.dasRedirectInDelay);
		chkboxMoveFirstFrame.setSelected(r.moveFirstFrame);
		chkboxMoveDiagonal.setSelected(r.moveDiagonal);
		chkboxMoveUpAndDown.setSelected(r.moveUpAndDown);
		chkboxMoveLeftAndRightAllow.setSelected(r.moveLeftAndRightAllow);
		chkboxMoveLeftAndRightUsePreviousInput.setSelected(r.moveLeftAndRightUsePreviousInput);
		chkboxMoveShiftLockEnable.setSelected(r.shiftLockEnable);

		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				txtfldPieceOffsetX[i][j].setText(String.valueOf(r.pieceOffsetX[i][j]));
				txtfldPieceOffsetY[i][j].setText(String.valueOf(r.pieceOffsetY[i][j]));
				txtfldPieceSpawnX[i][j].setText(String.valueOf(r.pieceSpawnX[i][j]));
				txtfldPieceSpawnY[i][j].setText(String.valueOf(r.pieceSpawnY[i][j]));
				txtfldPieceSpawnBigX[i][j].setText(String.valueOf(r.pieceSpawnXBig[i][j]));
				txtfldPieceSpawnBigY[i][j].setText(String.valueOf(r.pieceSpawnYBig[i][j]));
			}
			comboboxPieceColor[i].setSelectedIndex(r.pieceColor[i] - 1);
			comboboxPieceDirection[i].setSelectedIndex(r.pieceDefaultDirection[i]);
		}
	}

	/**
	 * A rule setUIWritten from the
	 * @param r Rule Set
	 */
	public void writeRuleFromUI(RuleOptions r) {
		r.strRuleName = txtfldRuleName.getText();
		r.nextDisplay = getIntTextField(txtfldNextDisplay);
		r.style = comboboxStyle.getSelectedIndex();
		r.skin = comboboxSkin.getSelectedIndex();
		r.ghost = chkboxGhost.isSelected();
		r.pieceEnterAboveField = chkboxEnterAboveField.isSelected();
		r.pieceEnterMaxDistanceY = getIntTextField(txtfldEnterMaxDistanceY);
		int indexRandomizer = comboboxRandomizer.getSelectedIndex();
		if(indexRandomizer >= 0) {
			r.strRandomizer = vectorRandomizer.get(indexRandomizer);
			boolean found = false;
			for(Class<? extends Randomizer> c : Zeroflections.getRandomizers()) {
				if(r.strRandomizer.equals(c.getSimpleName())) {
					r.strRandomizer = c.getName();
					found = true;
					break;
				}
			}
			if(!found)
				r.strRandomizer = "";
		} else 
			r.strRandomizer = "";

		r.fieldWidth = getIntTextField(txtfldFieldWidth);
		r.fieldHeight = getIntTextField(txtfldFieldHeight);
		r.fieldHiddenHeight = getIntTextField(txtfldFieldHiddenHeight);
		r.fieldCeiling = chkboxFieldCeiling.isSelected();
		r.fieldLockoutDeath = chkboxFieldLockoutDeath.isSelected();
		r.fieldPartialLockoutDeath = chkboxFieldPartialLockoutDeath.isSelected();

		r.holdEnable = chkboxHoldEnable.isSelected();
		r.holdInitial = chkboxHoldInitial.isSelected();
		r.holdInitialLimit = chkboxHoldInitialLimit.isSelected();
		r.holdResetDirection = chkboxHoldResetDirection.isSelected();
		r.holdLimit = getIntTextField(txtfldHoldLimit);

		r.harddropEnable = chkboxDropHardDropEnable.isSelected();
		r.harddropLock = chkboxDropHardDropLock.isSelected();
		r.harddropLimit = chkboxDropHardDropLimit.isSelected();
		r.softdropEnable = chkboxDropSoftDropEnable.isSelected();
		r.softdropLock = chkboxDropSoftDropLock.isSelected();
		r.softdropLimit = chkboxDropSoftDropLimit.isSelected();
		r.softdropSurfaceLock = chkboxDropSoftDropSurfaceLock.isSelected();
		r.softdropSpeed = getFloatTextField(txtfldDropSoftDropSpeed);
		r.softdropMultiplyNativeSpeed = chkboxDropSoftDropMultiplyNativeSpeed.isSelected();
		r.softdropGravitySpeedLimit = chkboxDropSoftDropGravitySpeedLimit.isSelected();

		r.rotateInitial = chkboxRotateInitial.isSelected();
		r.rotateInitialLimit = chkboxRotateInitialLimit.isSelected();
		r.rotateWallkick = chkboxRotateWallkick.isSelected();
		r.rotateInitialWallkick = chkboxRotateInitialWallkick.isSelected();
		r.rotateMaxUpwardWallkick = getIntTextField(txtfldRotateMaxUpwardWallkick);
		r.rotateButtonDefaultRight = chkboxRotateButtonDefaultRight.isSelected();
		r.rotateButtonAllowReverse = chkboxRotateButtonAllowReverse.isSelected();
		r.rotateButtonAllowDouble = chkboxRotateButtonAllowDouble.isSelected();
		int indexWallkick = comboboxWallkickSystem.getSelectedIndex();
		if(indexWallkick >= 0) r.strWallkick = vectorWallkickSystem.get(indexWallkick);
		else r.strWallkick = "";

		r.minLockDelay = getIntTextField(txtfldLockDelayMin);
		r.maxLockDelay = getIntTextField(txtfldLockDelayMax);
		r.lockresetFall = chkboxLockDelayLockResetFall.isSelected();
		r.lockresetMove = chkboxLockDelayLockResetMove.isSelected();
		r.lockresetRotate = chkboxLockDelayLockResetRotate.isSelected();
		r.lockresetWallkick = chkboxLockDelayLockResetWallkick.isSelected();
		r.lockresetLimitShareCount = chkboxLockDelayLockResetLimitShareCount.isSelected();
		r.lockresetLimitMove = getIntTextField(txtfldLockDelayLockResetLimitMove);
		r.lockresetLimitRotate = getIntTextField(txtfldLockDelayLockResetLimitRotate);
		if(radioLockDelayLockResetLimitOverNoReset.isSelected()) r.lockresetLimitOver = RuleOptions.LOCKRESET_LIMIT_OVER_NORESET;
		if(radioLockDelayLockResetLimitOverInstant.isSelected()) r.lockresetLimitOver = RuleOptions.LOCKRESET_LIMIT_OVER_INSTANT;
		if(radioLockDelayLockResetLimitOverNoWallkick.isSelected()) r.lockresetLimitOver = RuleOptions.LOCKRESET_LIMIT_OVER_NOWALLKICK;

		r.minARE = getIntTextField(txtfldAREMin);
		r.maxARE = getIntTextField(txtfldAREMax);
		r.minARELine = getIntTextField(txtfldARELineMin);
		r.maxARELine = getIntTextField(txtfldARELineMax);
		r.lockflash = getIntTextField(txtfldARELockFlash);
		r.lockflashOnlyFrame = chkboxARELockFlashOnlyFrame.isSelected();
		r.lockflashBeforeLineClear = chkboxARELockFlashBeforeLineClear.isSelected();
		r.areCancelMove = chkboxARECancelMove.isSelected();
		r.areCancelRotate = chkboxARECancelRotate.isSelected();
		r.areCancelHold = chkboxARECancelHold.isSelected();

		r.minLineDelay = getIntTextField(txtfldLineDelayMin);
		r.maxLineDelay = getIntTextField(txtfldLineDelayMax);
		r.lineFallAnim = chkboxLineFallAnim.isSelected();
		r.lineCancelMove = chkboxLineCancelMove.isSelected();
		r.lineCancelRotate = chkboxLineCancelRotate.isSelected();
		r.lineCancelHold = chkboxLineCancelHold.isSelected();

		r.minDAS = getIntTextField(txtfldMoveDASMin);
		r.maxDAS = getIntTextField(txtfldMoveDASMax);
		r.dasDelay = getIntTextField(txtfldMoveDASDelay);
		r.dasInReady = chkboxMoveDASInReady.isSelected();
		r.dasInMoveFirstFrame = chkboxMoveDASInMoveFirstFrame.isSelected();
		r.dasInLockFlash = chkboxMoveDASInLockFlash.isSelected();
		r.dasInLineClear = chkboxMoveDASInLineClear.isSelected();
		r.dasInARE = chkboxMoveDASInARE.isSelected();
		r.dasInARELastFrame = chkboxMoveDASInARELastFrame.isSelected();
		r.dasInEndingStart = chkboxMoveDASInEndingStart.isSelected();
		r.dasChargeOnBlockedMove = chkboxMoveDASChargeOnBlockedMove.isSelected();
		r.dasStoreChargeOnNeutral = chkboxMoveDASStoreChargeOnNeutral.isSelected();
		r.dasRedirectInDelay = chkboxMoveDASRedirectInDelay.isSelected();
		r.moveFirstFrame = chkboxMoveFirstFrame.isSelected();
		r.moveDiagonal = chkboxMoveDiagonal.isSelected();
		r.moveUpAndDown = chkboxMoveUpAndDown.isSelected();
		r.moveLeftAndRightAllow = chkboxMoveLeftAndRightAllow.isSelected();
		r.moveLeftAndRightUsePreviousInput = chkboxMoveLeftAndRightUsePreviousInput.isSelected();
		r.shiftLockEnable = chkboxMoveShiftLockEnable.isSelected();

		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				r.pieceOffsetX[i][j] = getIntTextField(txtfldPieceOffsetX[i][j]);
				r.pieceOffsetY[i][j] = getIntTextField(txtfldPieceOffsetY[i][j]);
				r.pieceSpawnX[i][j] = getIntTextField(txtfldPieceSpawnX[i][j]);
				r.pieceSpawnY[i][j] = getIntTextField(txtfldPieceSpawnY[i][j]);
				r.pieceSpawnXBig[i][j] = getIntTextField(txtfldPieceSpawnBigX[i][j]);
				r.pieceSpawnYBig[i][j] = getIntTextField(txtfldPieceSpawnBigY[i][j]);
			}
			r.pieceColor[i] = comboboxPieceColor[i].getSelectedIndex() + 1;
			r.pieceDefaultDirection[i] = comboboxPieceDirection[i].getSelectedIndex();
		}
	}


	/**
	 * PosttranslationalUIGets a string of
	 * @param str String
	 * @return PosttranslationalUIString (If you do not acceptstrReturns)
	 */
	public String getUIText(String str) {
		return lz.s(str);
	}

	/**
	 * TextfieldFromintGets the value of the type
	 * @param txtfld Textfield
	 * @return TextfieldIf you can get the value from its value, Failed0
	 */
	public int getIntTextField(JTextField txtfld) {
		int v = 0;

		try {
			v = Integer.parseInt(txtfld.getText());
		} catch(Exception e) {}

		return v;
	}

	/**
	 * TextfieldFromfloatGets the value of the type
	 * @param txtfld Textfield
	 * @return TextfieldIf you can get the value from its value, Failed0f
	 */
	public float getFloatTextField(JTextField txtfld) {
		float v = 0f;

		try {
			v = Float.parseFloat(txtfld.getText());
		} catch (Exception e) {}

		return v;
	}

	/**
	 * Processing at the time of occurrence of action
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "ResetRandomizer") {
			// NEXTReset selection of order generation algorithm
			comboboxRandomizer.setSelectedItem(null);
		}
		if("Base64_Generate".equals(e.getActionCommand())) {
			try {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				Base64OutputStream b64 = new Base64OutputStream(bout);
				Output kout = new Output(b64, 1024);
				Kryo kryo = new Kryo();
				KNetKryo.configure(kryo);
				RuleOptions rule = new RuleOptions();
				writeRuleFromUI(rule);
				kryo.writeObject(kout, rule);
				kout.flush();
				b64.close();
				base64.setText(new String(bout.toByteArray(), "ASCII"));
			} catch(Exception ex) {
				ex.printStackTrace();
				base64.setText(ex.toString());
			}
			
		}
		if("Base64_Load".equals(e.getActionCommand())) {
			try {
				ByteArrayInputStream bin = new ByteArrayInputStream(base64.getText().getBytes("ASCII"));
				Base64InputStream b64 = new Base64InputStream(bin);
				Input kin = new Input(b64, 1024);
				Kryo kryo = new Kryo();
				KNetKryo.configure(kryo);
				RuleOptions rule = kryo.readObject(kin, RuleOptions.class);
				readRuleToUI(rule);
			} catch(Exception ex) {
				ex.printStackTrace();
				base64.setText(ex.toString());
			}
		}
	}

	/**
	 * Filter file selection screen
	 */
	protected class FileFilterRUL extends FileFilter {
		@Override
		public boolean accept(File f) {
			if(f.isDirectory()) return true;
			if(f.getName().endsWith(".rul")) return true;
			return false;
		}

		@Override
		public String getDescription() {
			return getUIText("FileChooser_RuleFile");
		}
	}

	/**
	 * Image displayComboItems in box<br>
	 * <a href="http://www.javadrive.jp/tutorial/jcombobox/index20.html">Source</a>
	 */
	protected class ComboLabel {
		private String text = "";
		private Icon icon = null;

		public ComboLabel() {
		}

		public ComboLabel(String text) {
			this.text = text;
		}

		public ComboLabel(Icon icon) {
			this.icon = icon;
		}

		public ComboLabel(String text, Icon icon) {
			this.text = text;
			this.icon = icon;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public void setIcon(Icon icon) {
			this.icon = icon;
		}

		public Icon getIcon() {
			return icon;
		}
	}

	/**
	 * Image displayComboOf the boxListCellRenderer<br>
	 * <a href="http://www.javadrive.jp/tutorial/jcombobox/index20.html">Source</a>
	 */
	protected class ComboLabelCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 1L;

		public ComboLabelCellRenderer() {
			this.setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			ComboLabel data = (ComboLabel)value;
			setText(data.getText());
			setIcon(data.getIcon());

			if(isSelected) {
				setForeground(Color.white);
				setBackground(Color.black);
			} else {
				setForeground(Color.black);
				setBackground(Color.white);
			}

			return this;
		}
	}
}
