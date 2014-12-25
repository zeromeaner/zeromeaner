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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.zeromeaner.game.subsystem.ai.AIPlayer;
import org.zeromeaner.game.subsystem.ai.AbstractAI;
import org.zeromeaner.gui.common.Configurable;
import org.zeromeaner.gui.common.Configurable.Configurator;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.Options.AIOptions;
import org.zeromeaner.util.Zeroflections;

/**
 * AISelection screen frame
 */
public class StandaloneAISelectPanel extends JPanel implements ActionListener {
	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/** Log */
	static Logger log = Logger.getLogger(StandaloneAISelectPanel.class);

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


	protected JPanel configuratorPanel;
	
	protected Configurator configurator;
	
	/**
	 * Constructor
	 * @param owner Parent window
	 * @throws HeadlessException Keyboard, Mouse, Exceptions such as the display if there is no
	 */
	public StandaloneAISelectPanel() throws HeadlessException {
		super();

		List<String> aipaths = new ArrayList<String>();
		for(Class<? extends AbstractAI> aic : Zeroflections.getAIs()) {
			aipaths.add(aic.getName());
		}
		aiPathList = aipaths.toArray(new String[0]);
		aiNameList = loadAINames(aiPathList);

		initUI();
	}

	@Override
	public void setVisible(boolean aFlag) {
		if(aFlag)
			load();
		super.setVisible(aFlag);
	}
	
	public void load() {
		load(playerID);
	}
	
	/**
	 * This frame Action to take when you view the
	 * @param pl Player number
	 */
	public void load(int pl) {
		this.playerID = pl;

//		CustomProperties p = Options.GLOBAL_PROPERTIES;
		AIOptions opt = Options.player(playerID).ai;
		
		currentAI = opt.NAME.value();
		aiMoveDelay = opt.MOVE_DELAY.value();
		aiThinkDelay = opt.THINK_DELAY.value();
		aiUseThread = opt.USE_THREAD.value();
		aiShowHint = opt.SHOW_HINT.value();
		aiPrethink = opt.PRETHINK.value();
		aiShowState = opt.SHOW_STATE.value();

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
		
		loadAIConfig();
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

	protected void loadAIConfig() {
		configuratorPanel.removeAll();
		configurator = null;
		if(listboxAI.getSelectedValue() != null) {
			try {
				Class<?> cls = Class.forName((String) aiPathList[listboxAI.getSelectedIndex()], true, StandaloneAISelectPanel.class.getClassLoader());
				if(Configurable.class.isAssignableFrom(cls)) {
					configurator = ((Configurable) cls.newInstance()).getConfigurator();
					configuratorPanel.add(configurator.getConfigurationComponent(), BorderLayout.CENTER);
					configurator.reloadConfiguration(Options.player(playerID).ai.BACKING);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		configuratorPanel.revalidate();
		configuratorPanel.repaint();
	}
	
	/**
	 * GUIAInitialization
	 */
	protected void initUI() {
		setLayout(new GridLayout(0, 2));;
		
		configuratorPanel = new JPanel(new BorderLayout());

		// AIList
		JPanel panelAIList = new JPanel();
		panelAIList.setLayout(new BorderLayout());
		panelAIList.setBorder(BorderFactory.createTitledBorder("AI Class and Name"));
		this.add(panelAIList);
		
		final JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		right.setBorder(BorderFactory.createTitledBorder("AI Configuration"));
		
		JPanel p = new JPanel(new BorderLayout());
		p.add(right, BorderLayout.NORTH);
		this.add(p);

		String[] strList = new String[aiPathList.length];
		for(int i = 0; i < strList.length; i++) {
			strList[i] = aiNameList[i] + " (" + aiPathList[i].replaceAll(".*\\.", "") + ")";
		}
		listboxAI = new JList(strList);
		listboxAI.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				loadAIConfig();
			}
		});

		JScrollPane scpaneAI = new JScrollPane(listboxAI);
		scpaneAI.setPreferredSize(new Dimension(400, 250));
		panelAIList.add(scpaneAI, BorderLayout.CENTER);

		JButton btnNoUse = new JButton(lz.s("AISelect_NoUse"));
		btnNoUse.setMnemonic('N');
		btnNoUse.addActionListener(this);
		btnNoUse.setActionCommand("AISelect_NoUse");
		btnNoUse.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		panelAIList.add(btnNoUse, BorderLayout.SOUTH);

		// AIText box of the movement interval
		JPanel panelTxtfldAIMoveDelay = new JPanel();
		panelTxtfldAIMoveDelay.setLayout(new BorderLayout());
		panelTxtfldAIMoveDelay.setAlignmentX(LEFT_ALIGNMENT);
		right.add(panelTxtfldAIMoveDelay);

		panelTxtfldAIMoveDelay.add(new JLabel(lz.s("AISelect_LabelAIMoveDelay")), BorderLayout.WEST);

		txtfldAIMoveDelay = new JTextField(20);
		panelTxtfldAIMoveDelay.add(txtfldAIMoveDelay, BorderLayout.EAST);

		// AIText box of the movement interval
		JPanel panelTxtfldAIThinkDelay = new JPanel();
		panelTxtfldAIThinkDelay.setLayout(new BorderLayout());
		panelTxtfldAIThinkDelay.setAlignmentX(LEFT_ALIGNMENT);
		right.add(panelTxtfldAIThinkDelay);

		panelTxtfldAIThinkDelay.add(new JLabel(lz.s("AISelect_LabelAIThinkDelay")), BorderLayout.WEST);

		txtfldAIThinkDelay = new JTextField(20);
		panelTxtfldAIThinkDelay.add(txtfldAIThinkDelay, BorderLayout.EAST);

		// AIThread use check Box
		chkboxAIUseThread = new JCheckBox(lz.s("AISelect_CheckboxAIUseThread"));
		chkboxAIUseThread.setAlignmentX(LEFT_ALIGNMENT);
		chkboxAIUseThread.setMnemonic('T');
		right.add(chkboxAIUseThread);

		chkBoxAIShowHint = new JCheckBox(lz.s("AISelect_CheckboxAIShowHint"));
		chkBoxAIShowHint.setAlignmentX(LEFT_ALIGNMENT);
		chkBoxAIShowHint.setMnemonic('H');
		right.add(chkBoxAIShowHint);

		chkBoxAIPrethink = new JCheckBox(lz.s("AISelect_CheckboxAIPrethink"));
		chkBoxAIPrethink.setAlignmentX(LEFT_ALIGNMENT);
		chkBoxAIPrethink.setMnemonic('P');
		right.add(chkBoxAIPrethink);

		chkBoxAIShowState = new JCheckBox(lz.s("AISelect_CheckboxAIShowState"));
		chkBoxAIShowState.setAlignmentX(LEFT_ALIGNMENT);
		chkBoxAIShowState.setMnemonic('S');
		right.add(chkBoxAIShowState);

		configuratorPanel.setAlignmentX(LEFT_ALIGNMENT);
		right.add(configuratorPanel);
		
		//  buttonKind
		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
		panelButtons.setAlignmentX(LEFT_ALIGNMENT);
		right.add(panelButtons);

		JButton btnOK = new JButton(lz.s("AISelect_OK"));
		btnOK.setMnemonic('O');
		btnOK.addActionListener(this);
		btnOK.setActionCommand("AISelect_OK");
		btnOK.setAlignmentX(LEFT_ALIGNMENT);
		btnOK.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		panelButtons.add(btnOK);

		JButton btnCancel = new JButton(lz.s("AISelect_Cancel"));
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

			
//			CustomProperties p = Options.GLOBAL_PROPERTIES;
			AIOptions opt = Options.player(playerID).ai;
			
			if(configurator != null)
				configurator.applyConfiguration(opt.BACKING);

			if(aiID >= 0) 
				opt.NAME.set(aiPathList[aiID]);
			else 
				opt.NAME.set("");
			opt.MOVE_DELAY.set(aiMoveDelay);
			opt.THINK_DELAY.set(aiThinkDelay);
			opt.USE_THREAD.set(aiUseThread);
			opt.SHOW_HINT.set(aiShowHint);
			opt.PRETHINK.set(aiPrethink);
			opt.SHOW_STATE.set(aiShowState);
			StandaloneMain.saveConfig();
		}
		// Cancel
		else if(e.getActionCommand() == "AISelect_Cancel") {
			load(playerID);
		}
	}
}
