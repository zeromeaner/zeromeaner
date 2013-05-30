package org.zeromeaner.gui.knet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.funcish.core.Predicates;
import org.funcish.core.fn.Predicate;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.gui.tool.RuleEditorPanel;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.obj.KNetGameInfo;
import org.zeromeaner.util.Localization;
import org.zeromeaner.util.LstResourceMap;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.RuleList;



public class KNetChannelInfoPanel extends JPanel {
	private static Localization lz = new Localization();
	
	private KNetChannelInfo channel;
	
	private JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
	
	private JTextField name = new JTextField("");
	private JSpinner maxPlayers = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1)); {{
		maxPlayers.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				DefaultComboBoxModel model = (DefaultComboBoxModel) mode.getModel();
				model.removeAllElements();
				mode.setModel(model);
				Predicate<GameMode> vs = ModeList.IS_VSMODE;
				if(1 == (Integer)KNetChannelInfoPanel.this.maxPlayers.getValue())
					vs = Predicates.not(vs);
				for(String modeName : ModeList.getModes().getIsNetplay(true).filter(vs).names()) {
					model.addElement(modeName);
				}
				if(model.getSize() > 0)
					mode.setSelectedIndex(0);
			}
		});
	}}
	private JCheckBox autoStart = new JCheckBox();
	
	private JCheckBox ruleLock = new JCheckBox();
	
	private JComboBox rule = new JComboBox();
	{{
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		rule.setModel(model);
		rule.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ruleName = (String) rule.getSelectedItem();
				RuleOptions ropt;
				if(ruleName == null)
					ropt = new RuleOptions();
				else
					ropt = RuleList.getRules().getNamed(ruleName);
				if(ruleEditor != null)
					ruleEditor.readRuleToUI(ropt);
			}
		});
	}}

	private JComboBox mode = new JComboBox();
	{{
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		mode.setModel(model);
		Predicate<GameMode> vs = ModeList.IS_VSMODE;
		if(1 == (Integer)KNetChannelInfoPanel.this.maxPlayers.getValue())
			vs = Predicates.not(vs);
		for(String modeName : ModeList.getModes().getIsNetplay(true).filter(vs).map(ModeList.MODE_NAME)) {
			model.addElement(modeName);
		}
		mode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultComboBoxModel ruleModel = (DefaultComboBoxModel) rule.getModel();
				ruleModel.removeAllElements();
				
				if(mode.getSelectedItem() == null)
					return;
				
				LstResourceMap recdRules = new LstResourceMap("config/list/recommended_rules.lst");
				List<String> ruleResources = recdRules.get(mode.getSelectedItem());
				if(ruleResources == null || ruleResources.size() == 0) {
					ruleResources = Arrays.asList("config/rule/StandardZero.rul", "config/rule/Standard.rul");
				}
				RuleList rules = RuleList.FROM_RESOURCE.map(ruleResources, new RuleList());
				for(String ruleName : rules.getNames()) {
					ruleModel.addElement(ruleName);
				}
			}
		});
		int i = model.getIndexOf("NET-VS-BATTLE");
		if(i < 0)
			i = 0;
		mode.setSelectedIndex(i);
	}}
	
	private KNetGameInfoEditor gameEditor = new KNetGameInfoEditor();
	
	private RuleEditorPanel ruleEditor = new RuleEditorPanel();
	
	
	public KNetChannelInfoPanel(KNetChannelInfo channel) {
		this.channel = channel;

		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
		
		JPanel p;
		
		p = new JPanel(new GridLayout(0, 2));
		p.add(new JLabel(lz.s("name"))); p.add(name);
		p.add(new JLabel(lz.s("max_players"))); p.add(maxPlayers);
		p.add(new JLabel(lz.s("auto_start"))); p.add(autoStart);
		p.add(new JLabel(lz.s("mode"))); p.add(mode);
		p.add(new JLabel(lz.s("rule"))); p.add(rule);
		p.add(new JLabel(lz.s("rule_lock"))); p.add(ruleLock);
		tabs.addTab(lz.s("tab_general"), p);
		
		for(int i = 0; i < gameEditor.getTabCount(); i++) {
			tabs.addTab(lz.s("tab_game") + gameEditor.getTitleAt(i), new JScrollPane(gameEditor.getComponentAt(i)));
		}
		
		for(int i = 0; i < ruleEditor.getTabPane().getTabCount(); i++) {
			tabs.addTab(lz.s("tab_rule") + ruleEditor.getTabPane().getTitleAt(i), new JScrollPane(ruleEditor.getTabPane().getComponentAt(i)));
		}
	}
	
	public void setEditable(boolean editable) {
		setEditable(this, editable);
	}
	
	protected void setEditable(JComponent c, boolean editable) {
		if(c != this) {
			if(c instanceof JComboBox)
				c.setEnabled(editable);
			else {
				try {
					Method sed = c.getClass().getMethod("setEditable", boolean.class);
					sed.invoke(c, editable);
				} catch(NoSuchMethodException nsme) {
					if(c instanceof AbstractButton)
						c.setEnabled(editable);
				} catch(Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		
		for(int i = 0; i < c.getComponentCount(); i++) {
			Component cc = c.getComponent(i);
			if(cc instanceof JComponent)
				setEditable((JComponent) cc, editable);
		}
	}
	
	public void updateChannel() {
		channel.setName(name.getText());
		channel.setMaxPlayers((Integer) maxPlayers.getValue());
		channel.setAutoStart(autoStart.isSelected());
		if(channel.getRule() == null)
			channel.setRule(new RuleOptions());
		ruleEditor.writeRuleFromUI(channel.getRule());
		channel.setRuleLock(ruleLock.isSelected());
		if(channel.getGame() == null)
			channel.setGame(new KNetGameInfo());
		gameEditor.store(channel.getGame());
		channel.setMode((String) mode.getSelectedItem());
	}
	
	public void updateEditor() {
		name.setText(channel.getName());
		if(channel.getMaxPlayers() != 0)
			maxPlayers.setValue(channel.getMaxPlayers());
		autoStart.setSelected(channel.isAutoStart());
		if(channel.getRule() == null)
			channel.setRule(new RuleOptions());
		ruleEditor.readRuleToUI(channel.getRule());
		ruleLock.setSelected(channel.isRuleLock());
		if(channel.getGame() == null)
			channel.setGame(new KNetGameInfo());
		gameEditor.load(channel.getGame());
		mode.setSelectedItem(channel.getMode() == null ? "NET-VS-BATTLE" : channel.getMode());
	}

	public KNetChannelInfo getChannel() {
		return channel;
	}

	public void setChannel(KNetChannelInfo channel) {
		this.channel = channel;
	}
}
