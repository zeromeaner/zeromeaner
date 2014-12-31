package org.zeromeaner.gui.reskin;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Insets;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.zeromeaner.util.URLs;

public class StandaloneLicensePanel extends JPanel {
	private JEditorPane editor;
	
	public StandaloneLicensePanel() {
		super(new BorderLayout());
		URL about = StandaloneLicensePanel.class.getClassLoader().getResource("org/zeromeaner/res/About.html");
		try {
			editor = new JEditorPane();
			editor.setEditable(false);
			editor.setContentType("text/html");
			editor.setPage(about);
			editor.setMargin(new Insets(0,0,0,0));
			editor.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if(HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
						URLs.open(e.getURL());
					}
				}
			});
			add(new JScrollPane(editor), BorderLayout.CENTER);
		} catch(IOException ioe) {
			
		}
	}
}
