package org.zeromeaner.gui.applet;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JDesktopPane;
import javax.swing.UIManager;

public class AppletMain extends Applet {
	public static AppletMain instance;
	
	public JDesktopPane desktop;
	
	@Override
	public synchronized void init() {
		if(!EventQueue.isDispatchThread()) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					init();
				}
			});
		}
		
		if(instance != null)
			return;
		
		instance = this;

		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch(Exception ex) {
		}
		
		setLayout(new BorderLayout());
		desktop = new JDesktopPane();
		NullpoMinoInternalFrame.main(new String[0]);
		add(desktop, BorderLayout.CENTER);
	}
}
