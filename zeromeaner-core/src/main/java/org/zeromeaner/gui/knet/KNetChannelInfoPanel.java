package org.zeromeaner.gui.knet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
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

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.funcish.core.fn.Predicate;
import org.funcish.core.util.Predicates;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.gui.tool.RuleEditorPanel;
import org.zeromeaner.knet.KNetKryo;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.obj.KNetGameInfo;
import org.zeromeaner.util.Localization;
import org.zeromeaner.util.LstResourceMap;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.RuleList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;



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
				RuleList rules = RuleList.FROM_RESOURCE.map(ruleResources).into(new RuleList());
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
	
	private JButton generateBase64 = new JButton(new AbstractAction(lz.s("base64_generate")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				Base64OutputStream b64 = new Base64OutputStream(bout);
				Output kout = new Output(b64, 1024);
				Kryo kryo = new Kryo();
				KNetKryo.configure(kryo);
				KNetChannelInfo ci = new KNetChannelInfo();
				updateChannel(ci);
				kryo.writeObject(kout, ci);
				kout.flush();
				b64.close();
				base64.setText(new String(bout.toByteArray(), "ASCII"));
			} catch(Exception ex) {
				ex.printStackTrace();
				base64.setText(ex.toString());
			}
		}
	});
	
	private JButton loadBase64 = new JButton(new AbstractAction(lz.s("base64_load")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				ByteArrayInputStream bin = new ByteArrayInputStream(base64.getText().getBytes("ASCII"));
				Base64InputStream b64 = new Base64InputStream(bin);
				Input kin = new Input(b64, 1024);
				Kryo kryo = new Kryo();
				KNetKryo.configure(kryo);
				KNetChannelInfo ci = kryo.readObject(kin, KNetChannelInfo.class);
				updateEditor(ci);
			} catch(Exception ex) {
				ex.printStackTrace();
				base64.setText(ex.toString());
			}
		}
	});
	
	private JTextField base64 = new JTextField("", 80);
	
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
		
		p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,10,10), 0, 0);
		
		p.add(generateBase64, c);
		
		c.gridx++; p.add(loadBase64, c);
		
		c.gridx = 0; c.gridy++; c.gridwidth = 2; p.add(base64, c);
		tabs.addTab(lz.s("tab_base64"), p);

		for(int i = 0; i < gameEditor.getTabCount();) { // Don't need to increment
			tabs.addTab(lz.s("tab_game") + gameEditor.getTitleAt(i), new JScrollPane(gameEditor.getComponentAt(i)));
		}
		
		for(int i = 0; i < ruleEditor.getTabPane().getTabCount();) { // Don't need to increment
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
		updateChannel(channel);
	}
	
	public void updateChannel(KNetChannelInfo channel) {
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
		updateEditor(channel);
	}
	
	public void updateEditor(KNetChannelInfo channel) {
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
