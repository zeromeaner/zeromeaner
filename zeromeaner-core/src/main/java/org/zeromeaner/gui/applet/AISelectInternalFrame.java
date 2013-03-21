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
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


import org.apache.log4j.Logger;
import org.zeromeaner.game.subsystem.ai.AIPlayer;
import org.zeromeaner.util.ResourceInputStream;

/**
 * AISelection screen frame
 */
public class AISelectInternalFrame extends JInternalFrame implements ActionListener {
	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/** Log */
	static Logger log = Logger.getLogger(AISelectInternalFrame.class);

	/** Parent window */
	protected NullpoMinoInternalFrame owner;

	/** Player number */
	protected int playerID;

	/** AIList of classes */
	protected String[] aiPathList;

	/** AIOfNameList */
	protected String[] aiNameList;

	/** Current AIClass of */
	protected String currentAI;

	/** AIOfID */
	protected int aiID = 0;

	/** AIMovement interval of */
	protected int aiMoveDelay = 0;

	/** AIThinking of waiting time */
	protected int aiThinkDelay = 0;

	/** AIUsing threads in */
	protected boolean aiUseThread = false;

	protected boolean aiShowHint = false;

	protected boolean aiPrethink = false;

	protected boolean aiShowState = false;

	/** AIList list box */
	protected JList listboxAI;

	/** AIText box of the movement interval */
	protected JTextField txtfldAIMoveDelay;

	/** AIThinking of waiting timeText box */
	protected JTextField txtfldAIThinkDelay;

	/** AIThread Usage in check Box */
	protected JCheckBox chkboxAIUseThread;

	protected JCheckBox chkBoxAIShowHint;

	protected JCheckBox chkBoxAIPrethink;

	protected JCheckBox chkBoxAIShowState;


	/**
	 * Constructor
	 * @param owner Parent window
	 * @throws HeadlessException Keyboard, Mouse, Exceptions such as the display if there is no
	 */
	public AISelectInternalFrame(NullpoMinoInternalFrame owner) throws HeadlessException {
		super();
		this.owner = owner;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new ResourceInputStream("config/list/ai.lst")));
			aiPathList = loadAIList(in);
			aiNameList = loadAINames(aiPathList);
			in.close();
		} catch (IOException e) {
			log.warn("Failed to load AI list", e);
		}

		// GUIOfInitialization
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		initUI();
		pack();
		AppletMain.instance.desktop.add(this);
	}

	/**
	 * This frame Action to take when you view the
	 * @param pl Player number
	 */
	public void load(int pl) {
		this.playerID = pl;

		setTitle(NullpoMinoInternalFrame.getUIText("Title_AISelect") + " (" + (playerID+1) + "P)");

		currentAI = NullpoMinoInternalFrame.propGlobal.getProperty(playerID + ".ai", "");
		aiMoveDelay = NullpoMinoInternalFrame.propGlobal.getProperty(playerID + ".aiMoveDelay", 0);
		aiThinkDelay = NullpoMinoInternalFrame.propGlobal.getProperty(playerID + ".aiThinkDelay", 0);
		aiUseThread = NullpoMinoInternalFrame.propGlobal.getProperty(playerID + ".aiUseThread", true);
		aiShowHint = NullpoMinoInternalFrame.propGlobal.getProperty(playerID + ".aiShowHint", false);
		aiPrethink = NullpoMinoInternalFrame.propGlobal.getProperty(playerID + ".aiPrethink", false);
		aiShowState = NullpoMinoInternalFrame.propGlobal.getProperty(playerID + ".aiShowState", false);

		aiID = -1;
		listboxAI.clearSelection();
		for(int i = 0; i < aiPathList.length; i++) {
			if(currentAI.equals(aiPathList[i])) {
				aiID = i;
				listboxAI.setSelectedIndex(i);
				break;
			}
		}

		txtfldAIMoveDelay.setText(String.valueOf(aiMoveDelay));
		txtfldAIThinkDelay.setText(String.valueOf(aiThinkDelay));
		chkboxAIUseThread.setSelected(aiUseThread);
		chkBoxAIShowHint.setSelected(aiShowHint);
		chkBoxAIPrethink.setSelected(aiPrethink);
		chkBoxAIShowState.setSelected(aiShowState);
	}

	/**
	 * AIReads the list
	 * @param bf To read from a text file
	 * @return AIList
	 */
	public String[] loadAIList(BufferedReader bf) {
		ArrayList<String> aiArrayList = new ArrayList<String>();

		while(true) {
			String name = null;
			try {
				name = bf.readLine();
			} catch (Exception e) {
				break;
			}
			if(name == null) break;
			if(name.length() == 0) break;

			if(!name.startsWith("#"))
				aiArrayList.add(name);
		}

		String[] aiStringList = new String[aiArrayList.size()];
		for(int i = 0; i < aiArrayList.size(); i++) aiStringList[i] = aiArrayList.get(i);

		return aiStringList;
	}

	/**
	 * AIOfNameCreate a list
	 * @param aiPath AIList of classes
	 * @return AIOfNameList
	 */
	public String[] loadAINames(String[] aiPath) {
		String[] aiName = new String[aiPath.length];

		for(int i = 0; i < aiPath.length; i++) {
			Class<?> aiClass;
			AIPlayer aiObj;
			aiName[i] = "[Invalid]";

			try {
				aiClass = Class.forName(aiPath[i]);
				aiObj = (AIPlayer) aiClass.newInstance();
				aiName[i] = aiObj.getName();
			} catch(ClassNotFoundException e) {
				log.warn("AI class " + aiPath[i] + " not found", e);
			} catch(Throwable e) {
				log.warn("AI class " + aiPath[i] + " load failed", e);
			}
		}

		return aiName;
	}

	/**
	 * GUIAInitialization
	 */
	protected void initUI() {
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

		// AIList
		JPanel panelAIList = new JPanel();
		panelAIList.setLayout(new BorderLayout());
		panelAIList.setAlignmentX(LEFT_ALIGNMENT);
		this.add(panelAIList);

		String[] strList = new String[aiPathList.length];
		for(int i = 0; i < strList.length; i++) {
			strList[i] = aiNameList[i] + " (" + aiPathList[i] + ")";
		}
		listboxAI = new JList(strList);

		JScrollPane scpaneAI = new JScrollPane(listboxAI);
		scpaneAI.setPreferredSize(new Dimension(400, 250));
		panelAIList.add(scpaneAI, BorderLayout.CENTER);

		JButton btnNoUse = new JButton(NullpoMinoInternalFrame.getUIText("AISelect_NoUse"));
		btnNoUse.setMnemonic('N');
		btnNoUse.addActionListener(this);
		btnNoUse.setActionCommand("AISelect_NoUse");
		btnNoUse.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		panelAIList.add(btnNoUse, BorderLayout.SOUTH);

		// AIText box of the movement interval
		JPanel panelTxtfldAIMoveDelay = new JPanel();
		panelTxtfldAIMoveDelay.setLayout(new BorderLayout());
		panelTxtfldAIMoveDelay.setAlignmentX(LEFT_ALIGNMENT);
		this.add(panelTxtfldAIMoveDelay);

		panelTxtfldAIMoveDelay.add(new JLabel(NullpoMinoInternalFrame.getUIText("AISelect_LabelAIMoveDelay")), BorderLayout.WEST);

		txtfldAIMoveDelay = new JTextField(20);
		panelTxtfldAIMoveDelay.add(txtfldAIMoveDelay, BorderLayout.EAST);

		// AIText box of the movement interval
		JPanel panelTxtfldAIThinkDelay = new JPanel();
		panelTxtfldAIThinkDelay.setLayout(new BorderLayout());
		panelTxtfldAIThinkDelay.setAlignmentX(LEFT_ALIGNMENT);
		this.add(panelTxtfldAIThinkDelay);

		panelTxtfldAIThinkDelay.add(new JLabel(NullpoMinoInternalFrame.getUIText("AISelect_LabelAIThinkDelay")), BorderLayout.WEST);

		txtfldAIThinkDelay = new JTextField(20);
		panelTxtfldAIThinkDelay.add(txtfldAIThinkDelay, BorderLayout.EAST);

		// AIThread use check Box
		chkboxAIUseThread = new JCheckBox(NullpoMinoInternalFrame.getUIText("AISelect_CheckboxAIUseThread"));
		chkboxAIUseThread.setAlignmentX(LEFT_ALIGNMENT);
		chkboxAIUseThread.setMnemonic('T');
		this.add(chkboxAIUseThread);

		chkBoxAIShowHint = new JCheckBox(NullpoMinoInternalFrame.getUIText("AISelect_CheckboxAIShowHint"));
		chkBoxAIShowHint.setAlignmentX(LEFT_ALIGNMENT);
		chkBoxAIShowHint.setMnemonic('H');
		this.add(chkBoxAIShowHint);

		chkBoxAIPrethink = new JCheckBox(NullpoMinoInternalFrame.getUIText("AISelect_CheckboxAIPrethink"));
		chkBoxAIPrethink.setAlignmentX(LEFT_ALIGNMENT);
		chkBoxAIPrethink.setMnemonic('P');
		this.add(chkBoxAIPrethink);

		chkBoxAIShowState = new JCheckBox(NullpoMinoInternalFrame.getUIText("AISelect_CheckboxAIShowState"));
		chkBoxAIShowState.setAlignmentX(LEFT_ALIGNMENT);
		chkBoxAIShowState.setMnemonic('S');
		this.add(chkBoxAIShowState);

		//  buttonKind
		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
		panelButtons.setAlignmentX(LEFT_ALIGNMENT);
		this.add(panelButtons);

		JButton btnOK = new JButton(NullpoMinoInternalFrame.getUIText("AISelect_OK"));
		btnOK.setMnemonic('O');
		btnOK.addActionListener(this);
		btnOK.setActionCommand("AISelect_OK");
		btnOK.setAlignmentX(LEFT_ALIGNMENT);
		btnOK.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		panelButtons.add(btnOK);
		this.getRootPane().setDefaultButton(btnOK);

		JButton btnCancel = new JButton(NullpoMinoInternalFrame.getUIText("AISelect_Cancel"));
		btnCancel.setMnemonic('C');
		btnCancel.addActionListener(this);
		btnCancel.setActionCommand("AISelect_Cancel");
		btnCancel.setAlignmentX(LEFT_ALIGNMENT);
		btnCancel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		panelButtons.add(btnCancel);
	}

	/*
	 *  Called when button clicked
	 */
	public void actionPerformed(ActionEvent e) {
		// AIUnused button
		if(e.getActionCommand() == "AISelect_NoUse") {
			listboxAI.clearSelection();
		}
		// OK
		else if(e.getActionCommand() == "AISelect_OK") {
			aiID = listboxAI.getSelectedIndex();
			try {
				aiMoveDelay = Integer.parseInt(txtfldAIMoveDelay.getText());
			} catch (NumberFormatException e2) {
				aiMoveDelay = -1;
			}
			try {
				aiThinkDelay = Integer.parseInt(txtfldAIThinkDelay.getText());
			} catch (NumberFormatException e2) {
				aiThinkDelay = 0;
			}
			aiUseThread = chkboxAIUseThread.isSelected();
			aiShowHint = chkBoxAIShowHint.isSelected();
			aiPrethink = chkBoxAIPrethink.isSelected();
			aiShowState = chkBoxAIShowState.isSelected();

			if(aiID >= 0) NullpoMinoInternalFrame.propGlobal.setProperty(playerID + ".ai", aiPathList[aiID]);
			else NullpoMinoInternalFrame.propGlobal.setProperty(playerID + ".ai", "");
			NullpoMinoInternalFrame.propGlobal.setProperty(playerID + ".aiMoveDelay", aiMoveDelay);
			NullpoMinoInternalFrame.propGlobal.setProperty(playerID + ".aiThinkDelay", aiThinkDelay);
			NullpoMinoInternalFrame.propGlobal.setProperty(playerID + ".aiUseThread", aiUseThread);
			NullpoMinoInternalFrame.propGlobal.setProperty(playerID + ".aiShowHint", aiShowHint);
			NullpoMinoInternalFrame.propGlobal.setProperty(playerID + ".aiPrethink", aiPrethink);
			NullpoMinoInternalFrame.propGlobal.setProperty(playerID + ".aiShowState", aiShowState);
			NullpoMinoInternalFrame.saveConfig();

			this.setVisible(false);
		}
		// Cancel
		else if(e.getActionCommand() == "AISelect_Cancel") {
			this.setVisible(false);
		}
	}
}
