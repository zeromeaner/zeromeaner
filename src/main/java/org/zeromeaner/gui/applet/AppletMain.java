package org.zeromeaner.gui.applet;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
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

		final JLabel consoleLabel = new JLabel(" ");
//		PipedOutputStream pout = new PipedOutputStream();
//		PipedInputStream pin;
//		try {
//			pin = new PipedInputStream(pout);
//		} catch(IOException ioe) {
//			pin = null;
//		}
//		if(pin != null) {
//			final PipedInputStream fpin = pin;
//			final PrintStream sout = System.out;
//			System.setOut(new PrintStream(pout));
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					BufferedReader r = new BufferedReader(new InputStreamReader(fpin));
//					try {
//						for(String line = r.readLine(); line != null; line = r.readLine()) {
//							final String fline = line;
//							EventQueue.invokeLater(new Runnable() {
//								public void run() {
//									consoleLabel.setText(fline);
//									sout.println(fline);
//								}
//							});
//						}
//					} catch(IOException ioe) {
//						ioe.printStackTrace();
//					}
//				}
//			}).start();
//		}
		
		final JInternalFrame launching = new JInternalFrame("Launching zeromeaner");
		launching.setLayout(new BorderLayout());
		JProgressBar pb = new JProgressBar();
		pb.setIndeterminate(true);
		launching.add(new JLabel("Launching zeromeaner..."), BorderLayout.NORTH);
		launching.add(pb, BorderLayout.CENTER);
		launching.add(consoleLabel, BorderLayout.SOUTH);
		launching.pack();
		launching.setSize(500, 125);
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
						autoLaunch();
					}
				});
			}
		}).start();
	}

	@Override
	public void destroy() {
		NullpoMinoInternalFrame.mainFrame.shutdown();
	}

	public void autoLaunch() {
		URL url;
		try {
			url = new URL(getParameter("zero_url"));
		} catch(MalformedURLException me) {
			return;
		}
		String query = url.getQuery();
		if(query == null)
			return;
		Iterator<String> commands = Arrays.asList(query.split("/")).iterator();
		while(commands.hasNext()) {
			String cmd = commands.next();
			if("net".equals(cmd)) {
				autoNetplay(commands);
			}
		}
	}

	private void autoNetplay(Iterator<String> commands) {
		final String room;
		if(commands.hasNext())
			room = commands.next();
		else
			room = null;
		
		final boolean customize = commands.hasNext() && "customize".equals(commands.next());
		
		final AtomicBoolean createdRoom = new AtomicBoolean(false);
		
		// Launch netplay
		NullpoMinoInternalFrame.mainFrame.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Menu_NetPlay"));

		// Connect to the server
		final NetLobbyFrame nlf = NullpoMinoInternalFrame.netLobby.frame;
		nlf.listboxServerList.setSelectedIndex(0);
		nlf.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ServerSelect_Connect"));
		
		if(room == null || room.isEmpty())
			return;

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(4000);
				} catch(InterruptedException ie) {
				}
			}
		});
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Find the room
				for(int row = 0; row < nlf.tablemodelRoomList.getRowCount(); row++) {
					int namecol = nlf.tablemodelRoomList.findColumn(nlf.getUIText(nlf.ROOMTABLE_COLUMNNAMES[1]));
					if(room.equals(nlf.tablemodelRoomList.getValueAt(row, namecol))) {
						//								if(room.equals(nlf.tablemodelRoomList.getValueAt(row, 1))) {
						int columnID = nlf.tablemodelRoomList.findColumn(nlf.getUIText(nlf.ROOMTABLE_COLUMNNAMES[0]));
						String strRoomID = (String)nlf.tablemodelRoomList.getValueAt(row, columnID);
						int roomID = Integer.parseInt(strRoomID);
						nlf.joinRoom(roomID, false);
						return;
					}
				}

				// Room not found
				nlf.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Lobby_RoomCreate"));
				nlf.txtfldCreateRatedName.setText(room);
				
				createdRoom.set(true);
			}
		});
		
		if(!customize)
			return;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(!createdRoom.get())
					return;
//				nlf.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CreateRated_Custom"));
				nlf.btnCreateRatedCustom.doClick();
			}
		});
		while(commands.hasNext()) {
			String cmd = commands.next();
			if("mode".equals(cmd)) {
				final String mode = commands.next();
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						nlf.comboboxCreateRoomMode.getModel().setSelectedItem(mode);
					}
				});
			} else if("ok".equals(cmd)) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						if(!createdRoom.get())
							return;
						nlf.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CreateRated_OK"));
					}
				});
			}
			
		}
	}

}
