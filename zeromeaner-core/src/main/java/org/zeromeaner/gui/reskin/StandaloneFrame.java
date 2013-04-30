package org.zeromeaner.gui.reskin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

public class StandaloneFrame extends JFrame {
	private JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
	{
		toolbar.setFloatable(false);
	}
	
	private JButton close = new JButton(new AbstractAction("CLOSE") {
		@Override
		public void actionPerformed(ActionEvent e) {
			StandaloneFrame.this.dispose();
		}
	});
	{
		toolbar.add(close);
	}
	
	public StandaloneFrame() {
		setTitle("0mino");
		setUndecorated(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.EAST);
		add(new JLabel(" "), BorderLayout.CENTER);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		System.exit(0);
	}
}
