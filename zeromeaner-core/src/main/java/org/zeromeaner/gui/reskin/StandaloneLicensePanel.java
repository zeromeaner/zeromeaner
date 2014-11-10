package org.zeromeaner.gui.reskin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;

public class StandaloneLicensePanel extends JPanel {
	private JEditorPane editor;
	
	public StandaloneLicensePanel() {
		super(new BorderLayout());
		URL about = StandaloneLicensePanel.class.getClassLoader().getResource("org/zeromeaner/About.html");
		try {
			editor = new JEditorPane();
			editor.setEditable(false);
			editor.setContentType("text/html");
			editor.setPage(about);
			editor.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if(HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
						try {
							if(Desktop.isDesktopSupported())
								Desktop.getDesktop().browse(e.getURL().toURI());
							else
								Runtime.getRuntime().exec("xdg-open " + e.getURL());
						} catch (Exception e1) {
							throw new RuntimeException(e1);
						}
					}
				}
			});
			add(new JScrollPane(editor), BorderLayout.CENTER);
		} catch(IOException ioe) {
			
		}
	}
}
