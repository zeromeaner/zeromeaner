package org.zeromeaner.gui.applet;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
			final JInternalFrame login = new JInternalFrame("User ID");
			login.setLayout(new BorderLayout());
			login.add(new JLabel("Enter User ID"), BorderLayout.NORTH);
			final JTextField uid = new JTextField("default");
			login.add(uid, BorderLayout.CENTER);
			JButton ok = new JButton(new AbstractAction("OK") {
				@Override
				public void actionPerformed(ActionEvent e) {
					userId = uid.getText();
					login.setVisible(false);
					NullpoMinoInternalFrame.main(new String[0]);
				}
			});
			login.add(ok, BorderLayout.SOUTH);
			login.pack();
			login.setSize(200, 200);
			desktop.add(login);
			login.setVisible(true);
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
