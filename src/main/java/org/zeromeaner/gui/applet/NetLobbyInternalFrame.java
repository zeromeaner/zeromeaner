package org.zeromeaner.gui.applet;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;

import org.zeromeaner.gui.net.NetLobbyFrame;
import org.zeromeaner.gui.net.NetLobbyListener;

public class NetLobbyInternalFrame extends JInternalFrame {
	private NetLobbyFrame frame;
	
	public NetLobbyInternalFrame() {
		this.frame = new NetLobbyFrame();
		
		setTitle(frame.getTitle());
		
		setLayout(new BorderLayout());
		add(frame.getContentPane(), BorderLayout.CENTER);

		setSize(800, 300);
		
		AppletMain.instance.desktop.add(this);
		setVisible(true);
		
		setLocation(0, 510);
	}
	
	public void init() {
		frame.init();
	}
	
	public void shutdown() {
		frame.shutdown();
	}
	
	public void addListener(NetLobbyListener l) {
		frame.addListener(l);
	}
}
