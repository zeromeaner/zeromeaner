package org.zeromeaner.gui.reskin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.gui.tool.RuleEditorPanel;
import org.zeromeaner.plaf.CornerPileLayout;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.LstResourceMap;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.ResourceInputStream;
import org.zeromeaner.util.ResourceOutputStream;
import org.zeromeaner.util.RuleList;
import org.zeromeaner.util.io.ResourceStreams;

public class StandaloneModeselectPanel extends JPanel {
	private static String formatButtonText(String modeOrRuleName) {
		List<String> lines = new ArrayList<String>(Arrays.asList(modeOrRuleName.split("-+")));
		for(int i = lines.size() - 1; i > 0; i--) {
			if(lines.get(i).length() + lines.get(i-1).length() < 20)
				lines.set(i-1, lines.get(i-1) + " " + lines.remove(i));
		}
		StringBuilder sb = new StringBuilder();
		String sep = "<html><center>";
		boolean first = true;
		for(String line : lines) {
			sb.append(sep);
			if(first)
				line = "<b style=\"color:#000000;\">" + line.charAt(0) + "</b>" + line.substring(1);
			sb.append(line);
			sep = "<br>";
			first = false;
		}
		return sb.toString();
	}
	
	private static final String SELECT_CARD = "select";
	private static final String EDIT_CARD = "edit";
	
	private LstResourceMap recommended = new LstResourceMap("config/list/recommended_rules.lst");
	
	private List<ModeButton> modeButtons = new ArrayList<ModeButton>();
	private List<RuleButton> ruleButtons = new ArrayList<RuleButton>();
	
	private ModeButton currentMode;
	private RuleButton currentRule;
	
	private CardLayout cards = new CardLayout();
	
	private RuleOptions custom;
	
	private RuleEditorPanel ruleEditor = new RuleEditorPanel();
	
	private JPanel buttonsPanel;
	private ButtonGroup ruleButtonGroup;
	
	private void saveCustom() {
		try {
			OutputStream out = new ResourceOutputStream(custom.resourceName);
			CustomProperties p = new CustomProperties();
			custom.writeProperty(p, 0);
			p.store(out, "Custom Rule");
			out.close();
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public StandaloneModeselectPanel() {
		setLayout(cards);
		
		JPanel edit = new JPanel(new BorderLayout());
		add(edit, EDIT_CARD);
		edit.add(ruleEditor, BorderLayout.CENTER);
		edit.add(new JButton(new AbstractAction("Done Editing Custom Rule") {
			@Override
			public void actionPerformed(ActionEvent e) {
				ruleEditor.writeRuleFromUI(custom);
				saveCustom();
				for(int i = 0; i < buttonsPanel.getComponentCount(); i++)
					((JComponent) buttonsPanel.getComponent(i)).revalidate();
				cards.show(StandaloneModeselectPanel.this, SELECT_CARD);
			}
		}), BorderLayout.SOUTH);
		custom = new RuleOptions(RuleList.getRules().getNamed("STANDARD-ZERO"));
		custom.strRuleName = "CUSTOM RULE";
		custom.resourceName = "config/rule/Custom.rul";
		saveCustom();
		
		JPanel select = new JPanel(new BorderLayout());
		add(select, SELECT_CARD);
		cards.show(this, SELECT_CARD);

		ButtonGroup g = new ButtonGroup();
		buttonsPanel = new JPanel(new CornerPileLayout());
		for(GameMode mode : ModeList.getModes()) {
			ModeButton b = new ModeButton(mode);
			buttonsPanel.add(
					b,
					CornerPileLayout.NORTH_WEST);
			g.add(b);
			this.modeButtons.add(b);
		}
		
		ruleButtonGroup = new ButtonGroup();
		RuleList rules = RuleList.getRules();
		for(RuleOptions rule : rules) {
			RuleButton b = new RuleButton(rule);
			
			buttonsPanel.add(
					b,
					CornerPileLayout.SOUTH_EAST);

			this.ruleButtons.add(b);
			ruleButtonGroup.add(b);
//			if(rule.resourceName.equals(StandaloneMain.propConfig.getProperty("0.rule")))
			if(rule.resourceName.equals(Options.general().RULE_NAME.value()))
				b.setSelected(true);
		}
		select.add(buttonsPanel, BorderLayout.CENTER);
		
		for(ModeButton mb : this.modeButtons) {
//			if(mb.mode.getName().equals(StandaloneMain.propConfig.getProperty("name.mode")))
			if(mb.mode.getName().equals(Options.general().MODE_NAME.value()))
				mb.doClick();

		}
		
		revalidate();
	}
	
	private class ModeButton extends JToggleButton {
		private GameMode mode;
		private RuleButton rule;
		
		public ModeButton(GameMode m) {
//			super("<html>" + m.getName().replaceAll("-+", "<br>"));
			super(formatButtonText(m.getName()));
			this.mode = m;

			MetalLookAndFeel laf = (MetalLookAndFeel) UIManager.getLookAndFeel();
			Font f = laf.getControlTextFont();
			f = f.deriveFont(Font.PLAIN);
			setFont(f);

			setMargin(new Insets(3, 3, 3, 3));
			
			setBackground(new Color(233, 222, 222));
			setOpaque(true);
			
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
							if(ruleResource.equals(rb.rule.resourceName)) {
								rb.doClick();
								cards.show(StandaloneModeselectPanel.this, SELECT_CARD);
							}
						}
					}
				}
			});
		}
	}
	
	private class RuleButton extends JToggleButton {
		private RuleOptions rule;
		private boolean custom;
		
		@Override
		public boolean isBorderPainted() {
			return custom || super.isBorderPainted();
		}
		
		public RuleButton(RuleOptions r) {
			this.rule = r;
			this.custom = r.resourceName.contains("/Custom_");

			MetalLookAndFeel laf = (MetalLookAndFeel) UIManager.getLookAndFeel();
			Font f = laf.getControlTextFont();
			f = f.deriveFont(Font.PLAIN);
			setFont(f);

			setMargin(new Insets(3, 3, 3, 3));
			setBackground(new Color(222, 233, 233));
			setOpaque(true);
			
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(currentMode != null)
						currentMode.rule = RuleButton.this;
					currentRule = RuleButton.this;
//					StandaloneMain.propConfig.setProperty("0.rule", rule.resourceName);
					Options.general().RULE_NAME.set(rule.resourceName);
					Options.general().RULE_NAME_P2.set(rule.resourceName);
//					StandaloneMain.propConfig.setProperty("mode." + currentMode.mode.getName() + ".rule", rule.resourceName);
					Options.mode(currentMode.mode.getName()).RULE_RSOURCE.set(rule.resourceName);
				}
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if(e.isPopupTrigger()) {
						JPopupMenu menu = new JPopupMenu();
						menu.add(new AbstractAction("Copy to Custom Rule") {
							@Override
							public void actionPerformed(ActionEvent e) {
								RuleOptions custom = new RuleOptions();
								custom.copy(rule);
								
								int custnum = 1;
								String rn = rule.resourceName.replaceAll(".*/([^/]+)\\.rul", "$1");
								custom.resourceName = "config/rule/Custom_" + rn + "_" + custnum + ".rul";
								
								for(;; custnum++) {
									try {
										custom.resourceName = "config/rule/Custom_" + rn + "_" + custnum + ".rul";
										InputStream in = new ResourceInputStream(custom.resourceName);
										in.close();
										continue;
									} catch(FileNotFoundException ex) {
										break;
									} catch(IOException ex) {
										throw new RuntimeException(ex);
									}
								}
								
								
								custom.strRuleName = "Custom " + rule.strRuleName + " " + custnum;
								try {
									ResourceOutputStream out = new ResourceOutputStream(custom.resourceName);
									if(out != null) {
										try {
											CustomProperties p = new CustomProperties();
											custom.writeProperty(p, 0);
											p.store(out, "Custom Rule " + custnum);
										} finally {
											out.close();
										}
									}
								} catch(IOException ex) {
									throw new RuntimeException(ex);
								}
								RuleButton rb = new RuleButton(custom);
								buttonsPanel.add(rb, CornerPileLayout.SOUTH_EAST);
								ruleButtonGroup.add(rb);
								rb.revalidate();
							}
						});
						if(custom) {
							menu.add(new AbstractAction("Edit Rule") {
								@Override
								public void actionPerformed(ActionEvent e) {
									StandaloneModeselectPanel.this.custom = rule;
									ruleEditor.readRuleToUI(rule);
									cards.show(StandaloneModeselectPanel.this, EDIT_CARD);
								}
							});
							menu.add(new AbstractAction("Delete Rule") {
								@Override
								public void actionPerformed(ActionEvent e) {
									ResourceStreams.get().delete(rule.resourceName);
									buttonsPanel.remove(RuleButton.this);
									buttonsPanel.revalidate();
									buttonsPanel.repaint();
								}
							});
						}
						menu.show(RuleButton.this, 0, RuleButton.this.getHeight());
					}
				}
			});
		}
		
		@Override
		public void revalidate() {
			if(rule != null)
				setText(formatButtonText(rule.strRuleName));
			super.revalidate();
		}
	}
}
