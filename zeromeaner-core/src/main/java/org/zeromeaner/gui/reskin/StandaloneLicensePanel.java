package org.zeromeaner.gui.reskin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class StandaloneLicensePanel extends JPanel {
	private JEditorPane editor;
	
	public StandaloneLicensePanel() {
		super(new BorderLayout());
		try {
			editor = new JEditorPane(StandaloneLicensePanel.class.getClassLoader().getResource("org/zeromeaner/About.html"));
			editor.setBackground(new Color(0,0,128));
			editor.setForeground(Color.WHITE);
			add(new JScrollPane(editor), BorderLayout.CENTER);
		} catch(IOException ioe) {
			
		}
	}
}
