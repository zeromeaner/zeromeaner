package org.zeromeaner.gui.knet;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.zeromeaner.game.knet.obj.KNetChannelInfo;
import org.zeromeaner.game.knet.obj.KNetGameInfo;

public class KNetChannelInfoPanel extends JPanel {
	private KNetChannelInfo channel;
	
	private JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
	
	private JTextField name = new JTextField("");
	
	public KNetChannelInfoPanel(KNetChannelInfo channel) {
		this.channel = channel;

		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
		
		JPanel p;
		
		p = new JPanel(new GridLayout(0, 2));
		p.add(new JLabel("Channel Name:")); p.add(name);
		tabs.addTab("General", p);
	}
	
	public void updateChannel() {
		channel.setName(name.getText());
	}
	
	public void updateEditor() {
		name.setText(channel.getName());
	}
}
