package org.zeromeaner.gui.knet;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;

import org.rkutil.core.Condition;
import org.rkutil.core.ListTransformProcessor;
import org.rkutil.core.Transform;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;
import org.zeromeaner.game.knet.obj.KNetGameInfo;
import org.zeromeaner.game.subsystem.mode.AbstractNetMode;
import org.zeromeaner.game.subsystem.mode.AbstractNetVSMode;
import org.zeromeaner.util.GeneralUtil;
import org.zeromeaner.util.LstResourceMap;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.ResourceInputStream;
import org.zeromeaner.util.RuleMap;


public class KNetChannelInfoPanel extends JPanel {
	private KNetChannelInfo channel;
	
	private JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
	
	private JTextField name = new JTextField("");
	private JSpinner maxPlayers = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1)); {{
		maxPlayers.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				DefaultComboBoxModel model = (DefaultComboBoxModel) mode.getModel();
				model.removeAllElements();
				mode.setModel(model);
				Condition<Class<? extends AbstractNetMode>> VS_MODE = new Condition<Class<? extends AbstractNetMode>>() {
					@Override
					public boolean accept(Class<? extends AbstractNetMode> obj) {
						int maxPlayers = (Integer) KNetChannelInfoPanel.this.maxPlayers.getValue();
						if(maxPlayers == 1)
							return !AbstractNetVSMode.class.isAssignableFrom(obj);
						else
							return AbstractNetVSMode.class.isAssignableFrom(obj);
					}
				};
				for(String modeName : ModeList.getModes().accept(AbstractNetMode.class).accept(VS_MODE).transform(ModeList.MODE_NAME)) {
					model.addElement(modeName);
				}
				mode.setSelectedIndex(0);
			}
		});
	}}
	private JCheckBox autoStart = new JCheckBox();
	
	private JComboBox rule = new JComboBox();
	{{
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		rule.setModel(model);
	}}

	private JComboBox mode = new JComboBox();
	{{
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		mode.setModel(model);
		Condition<Class<? extends AbstractNetMode>> VS_MODE = new Condition<Class<? extends AbstractNetMode>>() {
			@Override
			public boolean accept(Class<? extends AbstractNetMode> obj) {
				int maxPlayers = (Integer) KNetChannelInfoPanel.this.maxPlayers.getValue();
				if(maxPlayers == 1)
					return !AbstractNetVSMode.class.isAssignableFrom(obj);
				else
					return AbstractNetVSMode.class.isAssignableFrom(obj);
			}
		};
		for(String modeName : ModeList.getModes().accept(AbstractNetMode.class).accept(VS_MODE).transform(ModeList.MODE_NAME)) {
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
				if(ruleResources == null)
					return;
				Transform<String, String> RULE_NAME = new Transform<String, String>() {
					@Override
					public String transform(String obj) {
						return GeneralUtil.loadRule(obj).strRuleName;
					}
				};
				for(String ruleName : new ListTransformProcessor<String, String>(RULE_NAME).appliedTo(ruleResources)) {
					ruleModel.addElement(ruleName);
				}
			}
		});
		mode.setSelectedIndex(0);
	}}
	
	private JCheckBox syncPlay = new JCheckBox();
	
	public KNetChannelInfoPanel(KNetChannelInfo channel) {
		this.channel = channel;

		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
		
		JPanel p;
		
		p = new JPanel(new GridLayout(0, 2));
		p.add(new JLabel("Channel Name:")); p.add(name);
		p.add(new JLabel("Max Players:")); p.add(maxPlayers);
		p.add(new JLabel("Automatic Start:")); p.add(autoStart);
		p.add(new JLabel("Game Mode:")); p.add(mode);
		p.add(new JLabel("Game Mode Rule:")); p.add(rule);
		p.add(new JLabel("Synchronous Play:")); p.add(syncPlay);
		tabs.addTab("General", p);
		
	}
	
	public void updateChannel() {
		channel.setName(name.getText());
	}
	
	public void updateEditor() {
		name.setText(channel.getName());
	}
}
