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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.swing.table.DefaultTableModel;

import org.zeromeaner.gui.net.NetLobbyFrame;

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

		setLayout(new BorderLayout());
		desktop = new JDesktopPane();
		desktop.setBackground(Color.decode("0x444488"));
		add(desktop, BorderLayout.CENTER);

		userId = CookieAccess.get().get("userId");
		if(userId == null)
			userId = getParameter("userId");
		while(userId == null || "default".equals(userId)) {
			userId = "none";
			int create = JOptionPane.showInternalConfirmDialog(desktop, "To save user configuration, such as custom keys, you must create a user id.\nThere is no need to remember a password.\nIf you choose not to create a user ID the default settings will be used.\n\nCreate a user ID now?", "Create User ID?", JOptionPane.YES_NO_OPTION);
			if(create == JOptionPane.YES_OPTION) {
				userId = (String) JOptionPane.showInternalInputDialog(desktop, "Enter Config ID", "Enter Config ID", JOptionPane.QUESTION_MESSAGE, null, null, "");
				if(userId != null)
					CookieAccess.set("userId", userId);
				else
					userId = "default";
			}
		}

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
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						autostartNetplay();
					}
				});
			}
		}).start();
	}

	@Override
	public void destroy() {
		NullpoMinoInternalFrame.mainFrame.shutdown();
	}
	
	private static Pattern AUTOSTART_NETPLAY = Pattern.compile("launch/net(/(.*))?");
	
	public void autostartNetplay() {
		URL url;
		try {
			url = new URL(getParameter("zero_url"));
		} catch(MalformedURLException me) {
			return;
		}
		String path = url.getPath();
		if(path == null)
			return;
		if(path.startsWith("dev/"))
			path = path.substring("dev/".length());
		JOptionPane.showMessageDialog(this, path);
		Matcher m = AUTOSTART_NETPLAY.matcher(path);
		if(m.matches()) {
			String room = m.group(2);
			
			// Launch netplay
			NullpoMinoInternalFrame.mainFrame.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Menu_NetPlay"));
			
			// Connect to the server
			NetLobbyFrame nlf = NullpoMinoInternalFrame.netLobby.frame;
			nlf.listboxServerList.setSelectedIndex(0);
			nlf.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ServerSelect_Connect"));
			
			if(room == null || room.isEmpty())
				return;
			// Find the room
			for(int i = 0; i < nlf.tablemodelRoomList.getRowCount(); i++) {
				if(room.equals(nlf.tablemodelRoomList.getValueAt(i, 1))) {
					nlf.tableRoomList.getSelectionModel().setSelectionInterval(i, i);
					nlf.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Room_Join"));
					return;
				}
			}
			
			// Room not found
			nlf.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Lobby_RoomCreate"));
			nlf.txtfldCreateRoomName.setText(room);
		}
	}
}
