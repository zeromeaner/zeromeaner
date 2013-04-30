package org.zeromeaner.gui.reskin;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.zeromeaner.util.Localization;

public class StandaloneFrame extends JFrame {
	private static final Localization lz = new Localization();
	
	private static Icon icon(String name) {
		URL url = StandaloneFrame.class.getResource(name + ".png");
		return url == null ? null : new ImageIcon(url);
	}
	
	private JToolBar toolbar;
	
	public StandaloneFrame() {
		setTitle("0mino");
		setUndecorated(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		setLayout(new BorderLayout());
		add(toolbar = createToolbar(), BorderLayout.EAST);
		add(new JLabel(" "), BorderLayout.CENTER);
	}
	
	private static void add(JToolBar toolbar, ButtonGroup g, AbstractButton b) {
		b.setFocusable(false);
		b.setBorder(null);
		toolbar.add(b);
		g.add(b);
	}
	
	private JToolBar createToolbar() {
		JToolBar t = new JToolBar(JToolBar.VERTICAL);
		t.setFloatable(false);
		t.setLayout(new GridLayout(0, 1));
		
		ButtonGroup g = new ButtonGroup();
		
		AbstractButton b;
		
		b = new JToggleButton(new LocalizedAction("toolbar.netplay") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new LocalizedAction("toolbar.open") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new LocalizedAction("toolbar.open_online") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new LocalizedAction("toolbar.rule_1p") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new LocalizedAction("toolbar.tuning_1p") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new LocalizedAction("toolbar.ai_1p") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new LocalizedAction("toolbar.rule_2p") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new LocalizedAction("toolbar.tuning_2p") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new LocalizedAction("toolbar.ai_2p") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new LocalizedAction("toolbar.general") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		add(t, g, b);
		
		b = new JButton(new LocalizedAction("toolbar.close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		add(t, g, b);
		
		return t;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		System.exit(0);
	}
	
	private static abstract class LocalizedAction extends AbstractAction {
		public LocalizedAction(String name) {
			super(lz.s(name), icon(name));
		}
	}
}
