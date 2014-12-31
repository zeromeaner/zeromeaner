package org.zeromeaner.gui.reskin;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.IOUtils;
import org.zeromeaner.util.URLs;
import org.zeromeaner.util.Version;

public class StandaloneFeedbackPanel extends JPanel {

	public StandaloneFeedbackPanel() {
		super(new GridBagLayout());
		
		String header = "zeromeaner\n"
				+ "version: " + Version.getBuildVersion() + "\n"
				+ "revision: " + Version.getBuildRevision() + "\n\n";
		JTextArea fbta;
		try {
			fbta = new JTextArea(
					header + 
					IOUtils.toString(StandaloneFrame.class.getResource("feedback.txt"), "UTF-8"));
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		fbta.setEditable(false);
		fbta.setLineWrap(true);
		fbta.setWrapStyleWord(true);
		fbta.setMargin(new Insets(25,25,25,25));
		JScrollPane fbs = new JScrollPane(fbta);
		
		JButton submitFeedback = new JButton(new AbstractAction("Submit Feedback Online") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					URLs.open(new URL("http://jira.robindps.com/secure/CreateIssue!default.jspa?selectedProjectId=10000"));
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
			}
		});
		add(
				submitFeedback, 
				new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(25,25,0,25), 0, 0));
		add(
				fbs, 
				new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(25,25,25,25), 0, 0));
	}
}
