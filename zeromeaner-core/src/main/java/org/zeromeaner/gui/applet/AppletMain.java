package org.zeromeaner.gui.applet;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.eviline.Block;
import org.eviline.Shape;
import org.eviline.ShapeType;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.gui.knet.KNetPanel;

public class AppletMain extends Applet {
	public static AppletMain instance;

	public static String userId;

	public static boolean isApplet() {
		return instance != null;
	}
	
	public static URL url;
	
	public JDesktopPane desktop;
	
	public Component notification;

	public AppletMain() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
		}
	}
	
	public void notifyUser(Icon icon, String message) {
		if(notification != null)
			remove(notification);
		JPanel p = new JPanel(new BorderLayout());
		if(icon != null)
			p.add(new JLabel(icon), BorderLayout.WEST);
		if(message != null)
			p.add(new JLabel(message), BorderLayout.CENTER);
		p.add(new JButton(new AbstractAction("X") {
			@Override
			public void actionPerformed(ActionEvent e) {
				notifyUser(null, null);
			}
		}), BorderLayout.EAST);
		if(icon != null || message != null)
			add(notification = p, BorderLayout.SOUTH);
		validate();
	}
	
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

		url = getDocumentBase();
		if(System.getProperty("zero_url") != null)
			try {
				url = new URL(System.getProperty("zero_url"));
			} catch(Exception ex) {
			}
		
		setLayout(new BorderLayout());
		
		desktop = new JDesktopPane() {
			@Override
			protected void paintComponent(Graphics g) {
				
			}
		};
		desktop.setBackground(new Color(64, 64, 128, 0));
		desktop.setDoubleBuffered(true);
		add(desktop, BorderLayout.CENTER);

		userId = CookieAccess.get("userId");
		if(userId == null)
			userId = getParameter("userId");
		while(userId == null || "default".equals(userId)) {
			userId = "none";
			int create = JOptionPane.showInternalConfirmDialog(desktop, "To save user configuration, such as custom keys, you must create a user id.\nThere is no need to remember a password.\nIf you choose not to create a user ID the default settings will be used.\n\nCreate a user ID now?", "Create User ID?", JOptionPane.YES_NO_OPTION);
			if(create == JOptionPane.YES_OPTION) {
				userId = (String) JOptionPane.showInternalInputDialog(desktop, "Enter Config ID", "Enter Config ID", JOptionPane.QUESTION_MESSAGE, null, null, "");
				if(userId != null)
					CookieAccess.put("userId", userId);
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
			} else if("replay".equals(cmd)) {
				autoReplay(commands);
			}
		}
	}
	
	private void autoReplay(Iterator<String> commands) {
		String path = "replay";
		while(commands.hasNext()) {
			path = path + "/" + commands.next();
		}
		NullpoMinoInternalFrame.mainFrame.startReplayGame(path);
		NullpoMinoInternalFrame.mainFrame.hideAllSubWindows();
		NullpoMinoInternalFrame.mainFrame.setVisible(false);
		NullpoMinoInternalFrame.gameFrame = new GameInternalFrame(NullpoMinoInternalFrame.mainFrame);
		NullpoMinoInternalFrame.gameFrame.displayWindow();
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
		final KNetPanel knp = NullpoMinoInternalFrame.netLobby.getKnetPanel();
		knp.getConnectionsListPanel().connect();
		
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
				JTabbedPane tabs = knp.getConnectedPanel().getChannels();
				for(int i = 0; i < tabs.getTabCount(); i++) {
					KNetPanel.ChannelPanel chp = (KNetPanel.ChannelPanel) tabs.getComponentAt(i);
					if(chp.getChannel().getName().equals(room)) {
						chp.join();
						return;
					}
				}

				// Room not found
				knp.getConnectedPanel().add();
				
				knp.getCreateChannelPanel().getChannel().setName(room);
				knp.getCreateChannelPanel().getChannelPanel().updateEditor();
				
				createdRoom.set(true);
			}
		});
		
//		if(!customize)
//			return;
//		EventQueue.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				if(!createdRoom.get())
//					return;
////				nlf.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CreateRated_Custom"));
//				nlf.btnCreateRatedCustom.doClick();
//			}
//		});
//		while(commands.hasNext()) {
//			String cmd = commands.next();
//			if("mode".equals(cmd)) {
//				final String mode = commands.next();
//				EventQueue.invokeLater(new Runnable() {
//					@Override
//					public void run() {
//						nlf.comboboxCreateRoomMode.getModel().setSelectedItem(mode);
//					}
//				});
//			} else if("ok".equals(cmd)) {
//				EventQueue.invokeLater(new Runnable() {
//					@Override
//					public void run() {
//						if(!createdRoom.get())
//							return;
//						nlf.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "CreateRated_OK"));
//					}
//				});
//			}
//			
//		}
	}

}
