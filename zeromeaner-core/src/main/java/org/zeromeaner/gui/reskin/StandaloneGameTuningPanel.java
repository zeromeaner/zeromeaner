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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

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
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.zeromeaner.util.Options;
import org.zeromeaner.util.SwingUtils;
import org.zeromeaner.util.Options.TuningOptions;

import static org.zeromeaner.gui.reskin.Localizations.lz;

/**
 * Tuning Settings screen frame
 */
public class StandaloneGameTuningPanel extends JPanel implements ActionListener {
	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/** Outline type names (before translation) */
	protected static final String[] OUTLINE_TYPE_NAMES = {
		"GameTuning_OutlineType_Auto", "GameTuning_OutlineType_None", "GameTuning_OutlineType_Normal",
		"GameTuning_OutlineType_Connect", "GameTuning_OutlineType_SameColor"
	};

	/** Player number */
	protected int playerID;

	/** A buttonInrotationDirectionFollow the rules */
	protected JRadioButton radioRotateButtonDefaultRightAuto;
	/** A buttonInrotationDirectionLeftrotationFixed on */
	protected JRadioButton radioRotateButtonDefaultRightLeft;
	/** A buttonInrotationDirectionRightrotationFixed on */
	protected JRadioButton radioRotateButtonDefaultRightRight;

	/** Of pictureComboBox */
	protected JComboBox comboboxSkin;
	/** BlockImage */
	protected BufferedImage[] imgBlockSkins;

	/** Outline type combobox */
	protected JComboBox comboboxBlockOutlineType;

	/** MinimumDAS */
	protected JTextField txtfldMinDAS;
	/** MaximumDAS */
	protected JTextField txtfldMaxDAS;

	/** Lateral movement speed */
	protected JTextField txtfldDasDelay;

	/** Checkbox to enable swapping the roles of up/down buttons in-game */
	protected JCheckBox chkboxReverseUpDown;

	/** Diagonal move: Auto */
	protected JRadioButton radioMoveDiagonalAuto;
	/** Diagonal move: Disable */
	protected JRadioButton radioMoveDiagonalDisable;
	/** Diagonal move: Enable */
	protected JRadioButton radioMoveDiagonalEnable;

	/** Show Outline Only: Auto */
	protected JRadioButton radioBlockShowOutlineOnlyAuto;
	/** Show Outline Only: Disable */
	protected JRadioButton radioBlockShowOutlineOnlyDisable;
	/** Show Outline Only: Enable */
	protected JRadioButton radioBlockShowOutlineOnlyEnable;

	/**
	 * Constructor
	 * @param owner Parent window
	 * @throws HeadlessException Keyboard, Mouse, Exceptions such as the display if there is no
	 */
	public StandaloneGameTuningPanel() throws HeadlessException {
		super();

		// BlockLoading Images
		loadBlockSkins();

		initUI();
	}

	/**
	 * GUIOfInitialization
	 */
	protected void initUI() {
		setLayout(new GridLayout(0, 2));
		
		JPanel left = new JPanel(new GridLayout(0, 1));
		JPanel right = new JPanel(new GridLayout(0, 1));

		this.add(left);
		this.add(right);
		
		// ---------- A buttonInrotationDirection ----------
		JPanel pRotateButtonDefaultRight = new JPanel();
		pRotateButtonDefaultRight.setLayout(new BoxLayout(pRotateButtonDefaultRight, BoxLayout.Y_AXIS));
		pRotateButtonDefaultRight.setAlignmentX(LEFT_ALIGNMENT);
		left.add(pRotateButtonDefaultRight);

		JLabel lRotateButtonDefaultRight = new JLabel(lz.s("GameTuning_RotateButtonDefaultRight_Label"));
		pRotateButtonDefaultRight.add(lRotateButtonDefaultRight);

		ButtonGroup gRotateButtonDefaultRight = new ButtonGroup();

		radioRotateButtonDefaultRightAuto = new JRadioButton(lz.s("GameTuning_RotateButtonDefaultRight_Auto"));
		pRotateButtonDefaultRight.add(radioRotateButtonDefaultRightAuto);
		gRotateButtonDefaultRight.add(radioRotateButtonDefaultRightAuto);

		radioRotateButtonDefaultRightLeft = new JRadioButton(lz.s("GameTuning_RotateButtonDefaultRight_Left"));
		pRotateButtonDefaultRight.add(radioRotateButtonDefaultRightLeft);
		gRotateButtonDefaultRight.add(radioRotateButtonDefaultRightLeft);

		radioRotateButtonDefaultRightRight = new JRadioButton(lz.s("GameTuning_RotateButtonDefaultRight_Right"));
		pRotateButtonDefaultRight.add(radioRotateButtonDefaultRightRight);
		gRotateButtonDefaultRight.add(radioRotateButtonDefaultRightRight);

		// ---------- Diagonal Move ----------
		JPanel pMoveDiagonal = new JPanel();
		pMoveDiagonal.setLayout(new BoxLayout(pMoveDiagonal, BoxLayout.Y_AXIS));
		pMoveDiagonal.setAlignmentX(LEFT_ALIGNMENT);
		left.add(pMoveDiagonal);

		JLabel lMoveDiagonal = new JLabel(lz.s("GameTuning_MoveDiagonal_Label"));
		pMoveDiagonal.add(lMoveDiagonal);

		ButtonGroup gMoveDiagonal = new ButtonGroup();

		radioMoveDiagonalAuto = new JRadioButton(lz.s("GameTuning_MoveDiagonal_Auto"));
		pMoveDiagonal.add(radioMoveDiagonalAuto);
		gMoveDiagonal.add(radioMoveDiagonalAuto);

		radioMoveDiagonalDisable = new JRadioButton(lz.s("GameTuning_MoveDiagonal_Disable"));
		pMoveDiagonal.add(radioMoveDiagonalDisable);
		gMoveDiagonal.add(radioMoveDiagonalDisable);

		radioMoveDiagonalEnable = new JRadioButton(lz.s("GameTuning_MoveDiagonal_Enable"));
		pMoveDiagonal.add(radioMoveDiagonalEnable);
		gMoveDiagonal.add(radioMoveDiagonalEnable);

		// ---------- Show Outline Only ----------
		JPanel pBlockShowOutlineOnly = new JPanel();
		pBlockShowOutlineOnly.setLayout(new BoxLayout(pBlockShowOutlineOnly, BoxLayout.Y_AXIS));
		pBlockShowOutlineOnly.setAlignmentX(LEFT_ALIGNMENT);
		left.add(pBlockShowOutlineOnly);

		JLabel lBlockShowOutlineOnly = new JLabel(lz.s("GameTuning_BlockShowOutlineOnly_Label"));
		pBlockShowOutlineOnly.add(lBlockShowOutlineOnly);

		ButtonGroup gBlockShowOutlineOnly = new ButtonGroup();

		radioBlockShowOutlineOnlyAuto = new JRadioButton(lz.s("GameTuning_BlockShowOutlineOnly_Auto"));
		pBlockShowOutlineOnly.add(radioBlockShowOutlineOnlyAuto);
		gBlockShowOutlineOnly.add(radioBlockShowOutlineOnlyAuto);

		radioBlockShowOutlineOnlyDisable = new JRadioButton(lz.s("GameTuning_BlockShowOutlineOnly_Disable"));
		pBlockShowOutlineOnly.add(radioBlockShowOutlineOnlyDisable);
		gBlockShowOutlineOnly.add(radioBlockShowOutlineOnlyDisable);

		radioBlockShowOutlineOnlyEnable = new JRadioButton(lz.s("GameTuning_BlockShowOutlineOnly_Enable"));
		pBlockShowOutlineOnly.add(radioBlockShowOutlineOnlyEnable);
		gBlockShowOutlineOnly.add(radioBlockShowOutlineOnlyEnable);

		// ---------- Picture ----------
		JPanel pSkin = new JPanel();
		pSkin.setAlignmentX(LEFT_ALIGNMENT);
		right.add(pSkin);

		JLabel lSkin = new JLabel(lz.s("GameTuning_Skin_Label"));
		pSkin.add(lSkin);

		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement(new ComboLabel(lz.s("GameTuning_Skin_Auto")));
		for(int i = 0; i < imgBlockSkins.length; i++) {
			model.addElement(new ComboLabel("" + i, new ImageIcon(imgBlockSkins[i])));
		}

		comboboxSkin = new JComboBox(model);
		comboboxSkin.setRenderer(new ComboLabelCellRenderer());
		comboboxSkin.setPreferredSize(new Dimension(190, 30));
		pSkin.add(comboboxSkin);

		// ---------- Outline Type ----------
		JPanel pOutlineType = new JPanel();
		pOutlineType.setAlignmentX(LEFT_ALIGNMENT);
		right.add(pOutlineType);

		JLabel lOutlineType = new JLabel(lz.s("GameTuning_OutlineType_Label"));
		pOutlineType.add(lOutlineType);

		String[] strArrayOutlineType = new String[OUTLINE_TYPE_NAMES.length];
		for(int i = 0; i < OUTLINE_TYPE_NAMES.length; i++) {
			strArrayOutlineType[i] = lz.s(OUTLINE_TYPE_NAMES[i]);
		}
		DefaultComboBoxModel modelOutlineType = new DefaultComboBoxModel(strArrayOutlineType);
		comboboxBlockOutlineType = new JComboBox(modelOutlineType);
		comboboxBlockOutlineType.setPreferredSize(new Dimension(190, 30));
		pOutlineType.add(comboboxBlockOutlineType);

		// ---------- LowestDAS ----------
		JPanel pMinDAS = new JPanel();
		pMinDAS.setAlignmentX(LEFT_ALIGNMENT);
		right.add(pMinDAS);

		JLabel lMinDAS = new JLabel(lz.s("GameTuning_MinDAS_Label"));
		pMinDAS.add(lMinDAS);

		txtfldMinDAS = new JTextField(5);
		pMinDAS.add(txtfldMinDAS);

		// ---------- MaximumDAS ----------
		JPanel pMaxDAS = new JPanel();
		pMaxDAS.setAlignmentX(LEFT_ALIGNMENT);
		right.add(pMaxDAS);

		JLabel lMaxDAS = new JLabel(lz.s("GameTuning_MaxDAS_Label"));
		pMaxDAS.add(lMaxDAS);

		txtfldMaxDAS = new JTextField(5);
		pMaxDAS.add(txtfldMaxDAS);

		// ---------- Lateral movement speed ----------
		JPanel pDasDelay = new JPanel();
		pDasDelay.setAlignmentX(LEFT_ALIGNMENT);
		right.add(pDasDelay);

		JLabel lDasDelay = new JLabel(lz.s("GameTuning_DasDelay_Label"));
		pDasDelay.add(lDasDelay);

		txtfldDasDelay = new JTextField(5);
		pDasDelay.add(txtfldDasDelay);

		// ---------- Reverse Up/Down ----------
		JPanel pReverseUpDown = new JPanel();
		pReverseUpDown.setAlignmentX(LEFT_ALIGNMENT);
		right.add(pReverseUpDown);

		JLabel lReverseUpDown = new JLabel(lz.s("GameTuning_ReverseUpDown_Label"));
		pReverseUpDown.add(lReverseUpDown);

		chkboxReverseUpDown = new JCheckBox();
		pReverseUpDown.add(chkboxReverseUpDown);

		// ---------- The bottom of the screen button ----------
		JPanel pButtons = new JPanel();
		pButtons.setAlignmentX(LEFT_ALIGNMENT);
		right.add(pButtons);

		JButton buttonOK = new JButton(lz.s("GameTuning_OK"));
		buttonOK.setMnemonic('O');
		buttonOK.addActionListener(this);
		buttonOK.setActionCommand("GameTuning_OK");
		pButtons.add(buttonOK);

		JButton buttonCancel = new JButton(lz.s("GameTuning_Cancel"));
		buttonCancel.setMnemonic('C');
		buttonCancel.addActionListener(this);
		buttonCancel.setActionCommand("GameTuning_Cancel");
		pButtons.add(buttonCancel);
	}

	/**
	 * BlockLoad an image
	 */
	protected void loadBlockSkins() {
		int numSkins = StandaloneResourceHolder.imgNormalBlockList.size();
		imgBlockSkins = new BufferedImage[numSkins];

		for(int i = 0; i < numSkins; i++) {
			BufferedImage imgBlock = (BufferedImage) StandaloneResourceHolder.imgNormalBlockList.get(i);
			boolean isSticky = StandaloneResourceHolder.blockStickyFlagList.get(i);

			imgBlockSkins[i] = new BufferedImage(144, 16, BufferedImage.TYPE_INT_ARGB);

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
	 * This frame Action to take when you view the
	 * @param pl Player number
	 */
	public void load(int pl) {
		this.playerID = pl;

		TuningOptions tune = Options.player(pl).tuning;
		
		int owRotateButtonDefaultRight = tune.ROTATE_BUTTON_DEFAULT_RIGHT.value();
		if(owRotateButtonDefaultRight == -1) radioRotateButtonDefaultRightAuto.setSelected(true);
		if(owRotateButtonDefaultRight ==  0) radioRotateButtonDefaultRightLeft.setSelected(true);
		if(owRotateButtonDefaultRight ==  1) radioRotateButtonDefaultRightRight.setSelected(true);

		int owMoveDiagonal = tune.MOVE_DIAGONAL.value();
		if(owMoveDiagonal == -1) radioMoveDiagonalAuto.setSelected(true);
		if(owMoveDiagonal ==  0) radioMoveDiagonalDisable.setSelected(true);
		if(owMoveDiagonal ==  1) radioMoveDiagonalEnable.setSelected(true);

		int owBlockShowOutlineOnly = tune.BLOCK_SHOW_OUTLINE_ONLY.value();
		if(owBlockShowOutlineOnly == -1) radioBlockShowOutlineOnlyAuto.setSelected(true);
		if(owBlockShowOutlineOnly ==  0) radioBlockShowOutlineOnlyDisable.setSelected(true);
		if(owBlockShowOutlineOnly ==  1) radioBlockShowOutlineOnlyEnable.setSelected(true);

		int owSkin = tune.SKIN.value();
		comboboxSkin.setSelectedIndex(owSkin + 1);

		int owBlockOutlineType = tune.BLOCK_OUTLINE_TYPE.value();
		comboboxBlockOutlineType.setSelectedIndex(owBlockOutlineType + 1);

		txtfldMinDAS.setText("" + tune.MIN_DAS.value());
		txtfldMaxDAS.setText("" + tune.MAX_DAS.value());
		txtfldDasDelay.setText("" + tune.DAS_DELAY.value());
		chkboxReverseUpDown.setSelected(tune.REVERSE_UP_DOWN.value());
	}

	/**
	 * Save
	 */
	protected void save() {
		TuningOptions tune = Options.player(playerID).tuning;
		
		int owRotateButtonDefaultRight = -1;
		if(radioRotateButtonDefaultRightAuto.isSelected()) owRotateButtonDefaultRight = -1;
		if(radioRotateButtonDefaultRightLeft.isSelected()) owRotateButtonDefaultRight =  0;
		if(radioRotateButtonDefaultRightRight.isSelected()) owRotateButtonDefaultRight = 1;
		tune.ROTATE_BUTTON_DEFAULT_RIGHT.set(owRotateButtonDefaultRight);

		int owMoveDiagonal = -1;
		if(radioMoveDiagonalAuto.isSelected()) owMoveDiagonal = -1;
		if(radioMoveDiagonalDisable.isSelected()) owMoveDiagonal = 0;
		if(radioMoveDiagonalEnable.isSelected()) owMoveDiagonal = 1;
		tune.MOVE_DIAGONAL.set(owMoveDiagonal);

		int owBlockShowOutlineOnly = -1;
		if(radioBlockShowOutlineOnlyAuto.isSelected()) owBlockShowOutlineOnly = -1;
		if(radioBlockShowOutlineOnlyDisable.isSelected()) owBlockShowOutlineOnly = 0;
		if(radioBlockShowOutlineOnlyEnable.isSelected()) owBlockShowOutlineOnly = 1;
		tune.BLOCK_SHOW_OUTLINE_ONLY.set(owBlockShowOutlineOnly);

		int owSkin = comboboxSkin.getSelectedIndex() - 1;
		tune.SKIN.set(owSkin);

		int owBlockOutlineType = comboboxBlockOutlineType.getSelectedIndex() - 1;
		tune.BLOCK_OUTLINE_TYPE.set(owBlockOutlineType);

		int owMinDAS = SwingUtils.getIntTextField(-1, txtfldMinDAS);
		tune.MIN_DAS.set(owMinDAS);
		int owMaxDAS = SwingUtils.getIntTextField(-1, txtfldMaxDAS);
		tune.MAX_DAS.set(owMaxDAS);
		int owDasDelay = SwingUtils.getIntTextField(-1, txtfldDasDelay);
		tune.DAS_DELAY.set(owDasDelay);
		boolean owReverseUpDown = chkboxReverseUpDown.isSelected();
		tune.REVERSE_UP_DOWN.set(owReverseUpDown);

		StandaloneMain.saveConfig();
	}

	/*
	 *  Called when button clicked
	 */
	public void actionPerformed(ActionEvent e) {
		// OK
		if(e.getActionCommand() == "GameTuning_OK") {
			save();
		}
		// Cancel
		else if(e.getActionCommand() == "GameTuning_Cancel") {
			load(playerID);
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
