package org.zeromeaner.gui.reskin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.util.LstResourceMap;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.RuleList;

public class StandaloneModeselectPanel extends JPanel {
	private LstResourceMap recommended = new LstResourceMap("config/list/recommended_rules.lst");
	
	private List<ModeButton> modeButtons = new ArrayList<ModeButton>();
	private List<RuleButton> ruleButtons = new ArrayList<RuleButton>();
	
	private ModeButton currentMode;
	private RuleButton currentRule;
	
	public StandaloneModeselectPanel() {
		super(new BorderLayout());

		ButtonGroup g = new ButtonGroup();
		JPanel modeButtons = new JPanel(new GridLayout(0, 8, 10, 10));
		JPanel p = new JPanel(new BorderLayout());
		for(GameMode mode : ModeList.getModes()) {
			ModeButton b = new ModeButton(mode);
			modeButtons.add(b);
			g.add(b);
			this.modeButtons.add(b);
		}
		p.add(modeButtons, BorderLayout.CENTER);
		add(p, BorderLayout.NORTH);
		
		g = new ButtonGroup();
		JPanel ruleButtons = new JPanel(new GridLayout(0, 8, 10, 10));
		p = new JPanel(new BorderLayout());
		for(RuleOptions rule : RuleList.getRules()) {
			RuleButton b = new RuleButton(rule);
			ruleButtons.add(b);
			this.ruleButtons.add(b);
			g.add(b);
			for(ModeButton mb : this.modeButtons) {
				mb.addActionListener(b);
			}
//			if(rule.resourceName.equals(StandaloneMain.propConfig.getProperty("0.rule")))
			if(rule.resourceName.equals(Options.general().RULE_NAME.value()))
				b.setSelected(true);
		}
		p.add(ruleButtons, BorderLayout.CENTER);
		add(p, BorderLayout.SOUTH);
		
		for(ModeButton mb : this.modeButtons) {
//			if(mb.mode.getName().equals(StandaloneMain.propConfig.getProperty("name.mode")))
			if(mb.mode.getName().equals(Options.general().MODE_NAME.value()))
				mb.doClick();

		}
	}
	
	private class ModeButton extends JToggleButton {
		private GameMode mode;
		private RuleButton rule;
		
		public ModeButton(GameMode m) {
			super("<html>" + m.getName().replaceAll("-+", "<br>"));
			this.mode = m;
			setFont(getFont().deriveFont(8f));
			setMargin(new Insets(10, 10, 10, 10));
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					currentMode = ModeButton.this;
//					StandaloneMain.propConfig.setProperty("name.mode", mode.getName());
					Options.general().MODE_NAME.set(mode.getName());
//					String ruleResource = StandaloneMain.propConfig.getProperty("mode." + mode.getName() + ".rule");
					String ruleResource = Options.mode(mode.getName()).RULE_RSOURCE.value();
					if(ruleResource != null) {
						for(RuleButton rb : ruleButtons) {
							if(ruleResource.equals(rb.rule.resourceName))
								rb.doClick();
						}
					}
				}
			});
		}
	}
	
	private class RuleButton extends JToggleButton implements ActionListener {
		private RuleOptions rule;
		public RuleButton(RuleOptions r) {
			super("<html>" + r.strRuleName.replaceAll("-+", "<br>"));
			this.rule = r;
			setFont(getFont().deriveFont(8f));
			setMargin(new Insets(10, 10, 10, 10));
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(currentMode != null)
						currentMode.rule = RuleButton.this;
					currentRule = RuleButton.this;
//					StandaloneMain.propConfig.setProperty("0.rule", rule.resourceName);
					Options.general().RULE_NAME.set(rule.resourceName);
//					StandaloneMain.propConfig.setProperty("mode." + currentMode.mode.getName() + ".rule", rule.resourceName);
					Options.mode(currentMode.mode.getName()).RULE_RSOURCE.set(rule.resourceName);
				}
			});
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(!(e.getSource() instanceof ModeButton))
				return;
			ModeButton mb = (ModeButton) e.getSource();
			if(recommended.get(mb.mode.getName()).contains(rule.resourceName))
				setBorderPainted(true);
			else
				setBorderPainted(false);
		}
	}
}
