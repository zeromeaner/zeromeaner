package org.zeromeaner.gui.applet;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
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
			return;
		}
		
		if(instance != null)
			return;
		
		instance = this;

		try {
			setLayout(new BorderLayout());
			desktop = new JDesktopPane();
			desktop.setBackground(Color.RED.darker().darker());
			add(desktop, BorderLayout.CENTER);
			NullpoMinoInternalFrame.main(new String[0]);
		} catch(Throwable t) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.flush();
			JOptionPane.showMessageDialog(this, sw);
		}
	}
	
	@Override
	public void start() {
		if(!EventQueue.isDispatchThread()) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					start();
				}
			});
			return;
		}

	}
}
