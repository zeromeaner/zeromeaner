package org.zeromeaner.gui.applet;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class AppletMain extends Applet {
	public static AppletMain instance;

	public static String userId;

	public static boolean isApplet() {
		return instance != null;
	}

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
			desktop.setBackground(Color.decode("0x444488"));
			add(desktop, BorderLayout.CENTER);
			
			userId = CookieAccess.get().get("userId");
			if(userId == null)
				userId = "default";
			
			final JInternalFrame launching = new JInternalFrame("Launching zeromeaner");
			launching.setLayout(new BorderLayout());
			JProgressBar pb = new JProgressBar();
			pb.setIndeterminate(true);
			launching.add(pb, BorderLayout.CENTER);
			launching.pack();
			launching.setSize(400, 150);
			desktop.add(launching);
			launching.setVisible(true);
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					NullpoMinoInternalFrame.main(new String[0]);
					launching.setVisible(false);
				}
			}).start();
		} catch(Throwable t) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.flush();
			JOptionPane.showMessageDialog(this, sw);
		}
	}

	@Override
	public void destroy() {
		NullpoMinoInternalFrame.mainFrame.shutdown();
	}
}
