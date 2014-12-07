package org.zeromeaner.gui.knet;

import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_CHAT;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_CREATE;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_ID;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_JOIN;
import static org.zeromeaner.knet.KNetEventArgs.CHANNEL_LIST;
import static org.zeromeaner.knet.KNetEventArgs.CONNECTED;
import static org.zeromeaner.knet.KNetEventArgs.TIMESTAMP;
import static org.zeromeaner.knet.KNetEventArgs.UPDATE_SOURCE;
import static org.zeromeaner.knet.KNetEventArgs.USER_AUTHENTICATE;
import static org.zeromeaner.knet.KNetEventArgs.USER_AUTHENTICATED;
import static org.zeromeaner.knet.KNetEventArgs.USER_CREATE;
import static org.zeromeaner.knet.KNetEventArgs.USER_UPDATED_PASSWORD;
import static org.zeromeaner.knet.KNetEventArgs.USER_UPDATE_PASSWORD;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.zeromeaner.applet.StandaloneApplet;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.knet.KNetChannelEvent;
import org.zeromeaner.knet.KNetChannelListener;
import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetGameClient;
import org.zeromeaner.knet.KNetListener;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.obj.KNetGameInfo;
import org.zeromeaner.util.EQInvoker;
import org.zeromeaner.util.Localization;
import org.zeromeaner.util.LstResourceMap;

public class KNetPanel extends JPanel implements KNetChannelListener, KNetListener {
	private static final Localization lz = new Localization();
	
	private static final String CONNECTION_LIST_PANEL_CARD = ConnectionListPanel.class.getName();
	private static final String LOGGING_IN_PANEL_CARD = LoggingInPanel.class.getName();
	private static final String LOGIN_PANEL_CARD = LoginPanel.class.getName();
	private static final String CONNECTED_PANEL_CARD = ConnectedPanel.class.getName();
	private static final String CREATE_CHANNEL_PANEL_CARD = CreateChannelPanel.class.getName();
	private static final String VIEW_CHANEL_CARD = KNetChannelInfoPanel.class.getName();
	private static final String CHANGE_PASSWORD_CARD = ChangePasswordPanel.class.getName();
	
	private LstResourceMap config;
	
	private String defaultUsername;
	private boolean ai;
	
	private CardLayout cards;
	private ConnectionListPanel connectionsListPanel;
	private LoggingInPanel loggingInPanel;
	private LoginPanel loginPanel;
	private ConnectedPanel connectedPanel;
	private CreateChannelPanel createChannelPanel;
	private ViewChannelPanel viewChannelPanel;
	private ChangePasswordPanel changePasswordPanel;
	
	private Map<Integer, ChannelPanel> channels = new HashMap<Integer, ChannelPanel>();
	private ChannelPanel activeChannel;
	
	private KNetGameClient client;
	
	public class ConnectionListPanel extends JPanel {
		private LstResourceMap servers = new LstResourceMap("config/list/knet_servers.lst");
		
		private DefaultListModel connectionsModel = new DefaultListModel();
		private JList connectionsList = new JList(connectionsModel);
		{
			connectionsList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		
		private JButton connect = new JButton(new AbstractAction(lz.s("conn_list_connect")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});
		
		private JButton add = new JButton(new AbstractAction(lz.s("conn_list_add")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(host.getText().isEmpty())
					return;
				if(port.getText().isEmpty())
					port.setText("61897");
				String c = host.getText() + ":" + port.getText();
				if(connectionsModel.contains(c))
					return;
				connectionsModel.addElement(c);
				connectionsList.setSelectedIndex(connectionsModel.size() - 1);
				servers.get("").add(c);
				servers.write();
			}
		});
		
		private JButton remove = new JButton(new AbstractAction(lz.s("conn_list_remove")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				String c = null;
				if(connectionsList.getSelectedIndex() > 0)
					c = (String) connectionsModel.remove(connectionsList.getSelectedIndex());
				connectionsList.revalidate();
				if(c != null) {
					servers.get("").remove(c);
					servers.write();
				}
			}
		});
		
		private JButton exit = new JButton(new AbstractAction(lz.s("conn_list_exit")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				shutdown();
			}
		});
		
		private JTextField username = new JTextField(defaultUsername);
		
		private JTextField host = new JTextField("");
		
		private JTextField port = new JTextField("");
		
		public ConnectionListPanel() {
			super(new BorderLayout());
			JPanel p = new JPanel(new BorderLayout());
			JPanel q = new JPanel(new BorderLayout());
			q.add(new JLabel(lz.s("conn_list_username")), BorderLayout.WEST);
			q.add(username, BorderLayout.CENTER);
			p.add(q, BorderLayout.NORTH);
			p.add(new JScrollPane(connectionsList), BorderLayout.CENTER);
			q = new JPanel(new GridLayout(1, 0));
			q.add(host);
			q.add(port);
			p.add(q, BorderLayout.SOUTH);
			p.setBorder(BorderFactory.createTitledBorder(lz.s("conn_list_border")));
			add(p, BorderLayout.CENTER);
			p = new JPanel(new GridLayout(0, 1));
			p.add(connect);
			p.add(add);
			p.add(remove);
			p.add(exit);
			add(p, BorderLayout.EAST);
			
			if(!GameManager.VERSION.isSnapshot())
				connectionsModel.addElement("www.zeromeaner.org:61897");
			else {
				connectionsModel.addElement("www.zeromeaner.org:61896");
			}
			
			for(String c : servers.get("")) {
				connectionsModel.addElement(c);
			}
			
			connectionsList.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if(connectionsList.getSelectedValue() == null) {
						host.setText("");
						port.setText("");
						return;
					}
					String[] f = ((String) connectionsList.getSelectedValue()).split(":", 2);
					host.setText(f[0]);
					port.setText(f.length > 1 ? f[1] : "61897");
				}
			});
			connectionsList.setSelectedIndex(0);
		}
		
		public void connect() {
			String host = this.host.getText();
			int port = Integer.parseInt(this.port.getText().isEmpty() ? "61897" : this.port.getText());
			
			client = new KNetGameClient("Player", host, port);
			client.addKNetChannelListener(KNetPanel.this);
			client.addKNetListener(KNetPanel.this);
			client.addKNetListener(new KNetListener() {
				@Override
				public void knetEvented(KNetClient client, KNetEvent e) {
					if(e.is(CONNECTED)) {
						String user = username.getText();
						if(user == null || user.isEmpty())
							user = "anonymous";
						if(ai)
							user += " [AI]";
						client.getSource().setName(user);
						client.removeKNetListener(this);
						client.fireTCP(UPDATE_SOURCE, client.getSource());
						client.fireTCP(USER_CREATE, null);
						client.fireTCP(USER_AUTHENTICATE, null);
						client.fireTCP(CHANNEL_LIST, true);
					}
				}
			});
			try {
				client.start();
			} catch(Exception ex) {
				try {
					client.stop();
				} catch(IOException ioe) {
				}
				client = null;
				JOptionPane.showMessageDialog(KNetPanel.this, ex.toString());
				return;
			}
			cards.show(KNetPanel.this, LOGGING_IN_PANEL_CARD);
			fireKnetPanelConnected();
		}
	}
	
	public class LoggingInPanel extends JPanel {
		public LoggingInPanel() {
			super(new BorderLayout());
			JLabel l = new JLabel("Logging In...");
			l.setHorizontalAlignment(SwingConstants.CENTER);
			add(l, BorderLayout.CENTER);
		}
	}
	
	public class LoginPanel extends JPanel {
		private JButton login = new JButton(new AbstractAction(lz.s("lp_login")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				String pw = pass.getText();
				if(pw.isEmpty())
					pw = null;
				client.fireTCP(USER_AUTHENTICATE, pw);
			}
		});
		
		private JButton disconnect = new JButton(new AbstractAction(lz.s("lp_disconnect")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				connectedPanel.disconnect();
			}
		});
		
		private JTextField user = new JTextField("");
		
		private JPasswordField pass = new JPasswordField("");
		
		public LoginPanel() {
			super(new BorderLayout());
			JPanel p = new JPanel(new GridLayout(0, 1));
			p.add(login);
			p.add(disconnect);
			add(p, BorderLayout.EAST);
			
			p = new JPanel(new GridLayout(0, 2));
			p.add(new JLabel(lz.s("lp_user")));
			p.add(user);
			p.add(new JLabel(lz.s("lp_pass")));
			p.add(pass);
			add(p, BorderLayout.CENTER);
			
			user.setEnabled(false);
			user.setDocument(connectionsListPanel.username.getDocument());
		}
	}
	
	public class ConnectedPanel extends JPanel {
		private JTabbedPane channels = new JTabbedPane(JTabbedPane.LEFT);
		
		private JButton add = new JButton(new AbstractAction(lz.s("cp_add")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				add();
			}
		});
		
		private JButton view = new JButton(new AbstractAction(lz.s("cp_view")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(getVisibleChannel().getChannel().getId() == KNetChannelInfo.LOBBY_CHANNEL_ID)
					return;
				cards.show(KNetPanel.this, VIEW_CHANEL_CARD);
				viewChannelPanel.channelPanel.setChannel(getVisibleChannel().getChannel());
				viewChannelPanel.channelPanel.updateEditor();
			}
		});
		
		private JButton spectate = new JButton(new AbstractAction(lz.s("cp_spectate")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.spectateChannel(getVisibleChannel().getChannel().getId());
			}
		});
		
		private JButton join = new JButton(new AbstractAction(lz.s("cp_join")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				getVisibleChannel().join();
			}
		});
		
		private JButton leave = new JButton(new AbstractAction(lz.s("cp_leave")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				getVisibleChannel().leave();
			}
		});
		
		private JButton disconnect = new JButton(new AbstractAction(lz.s("cp_disconnect")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				disconnect();
			}
		});
		
		private JButton changePassword = new JButton(new AbstractAction(lz.s("cp_change_password")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				cards.show(KNetPanel.this, CHANGE_PASSWORD_CARD);
			}
		});
		
		public ConnectedPanel() {
			super(new BorderLayout());
			add(channels, BorderLayout.CENTER);
			JPanel p = new JPanel(new GridLayout(0, 1));
			p.add(add);
			p.add(view);
			p.add(spectate);
			p.add(join);
			p.add(leave);
			p.add(disconnect);
			p.add(changePassword);
			add(p, BorderLayout.EAST);
		}
		
		public void disconnect() {
			try {
				client.stop();
			} catch(IOException ioe) {
			}
			client = null;
			cards.show(KNetPanel.this, CONNECTION_LIST_PANEL_CARD);
			fireKnetPanelDisconnected();
			KNetPanel.this.channels.clear();
			channels.removeAll();
		}
		
		public void add() {
			cards.show(KNetPanel.this, CREATE_CHANNEL_PANEL_CARD);
		}
		
		public JTabbedPane getChannels() {
			return channels;
		}
		
		public ChannelPanel getVisibleChannel() {
			return (ChannelPanel) channels.getSelectedComponent();
		}
	}
	
	public class ChannelPanel extends JPanel implements KNetChannelListener {
		private KNetChannelInfo channel;
		
		private DefaultListModel membersModel = new DefaultListModel();
		private JList membersList = new JList(membersModel);
		private JTextArea history = new JTextArea("");
		private JTextField line = new JTextField("");
		
		public ChannelPanel(KNetChannelInfo c) {
			this.channel = c;
			
			setLayout(new BorderLayout());
			
			JPanel p = new JPanel(new BorderLayout());
//			p.add(new JLabel("Chat Messages:"), BorderLayout.NORTH);
			history.setLineWrap(true);
			history.setWrapStyleWord(true);
			history.setEditable(false);
			p.add(new JScrollPane(history), BorderLayout.CENTER);
			p.setBorder(BorderFactory.createTitledBorder(lz.s("ch_chat_border")));
			add(p, BorderLayout.CENTER);
			
			add(line, BorderLayout.SOUTH);
			
			p = new JPanel(new BorderLayout());
//			p.add(new JLabel("User List:"), BorderLayout.NORTH);
			p.add(new JScrollPane(membersList), BorderLayout.CENTER);
			p.setBorder(BorderFactory.createTitledBorder(lz.s("ch_user_border")));
			add(p, BorderLayout.WEST);
			
			line.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() != KeyEvent.VK_ENTER)
						return;
					client.fireTCP(
							CHANNEL_CHAT, line.getText(),
							CHANNEL_ID, channel.getId(),
							TIMESTAMP, System.currentTimeMillis());
					history.setText(
							history.getText() 
							+ (history.getText().isEmpty() ? "" : "\n")
							+ client.getSource().getName()
							+ ": "
							+ line.getText());
					line.setText("");
				}
			});
			
			line.setEnabled(false);
			line.setText(lz.s("ch_join_to_chat"));
			
			client.addKNetChannelListener(this);
			
			update();
		}
		
		public KNetChannelInfo getChannel() {
			return channel;
		}
		
		public void join() {
//			if(activeChannel != null) {
//				activeChannel.leave();
//			}
//			client.fireTCP(CHANNEL_JOIN, CHANNEL_ID, channel.getId());
			client.joinChannel(channel.getId());
		}
		
		private void joined() {
			if(channel.getId() == KNetChannelInfo.LOBBY_CHANNEL_ID)
				return;
			activeChannel = this;
			Icon icon;
			if(channel.getPlayers().contains(client.getSource())) {
				icon = new ImageIcon(KNetPanel.class.getResource("active-channel.png"));
			} else {
				icon = new ImageIcon(KNetPanel.class.getResource("spectator-channel.png"));
			}
			connectedPanel.channels.setIconAt(
					connectedPanel.channels.indexOfComponent(this),
					icon);
			revalidate();
			client.setCurrentChannel(channel);
			fireKnetPanelJoined(getChannel());
		}
		
		public void leave() {
//			client.fireTCP(CHANNEL_LEAVE, CHANNEL_ID, channel.getId());
			client.leaveChannel();
		}
		
		private void left() {
			connectedPanel.channels.setIconAt(
					connectedPanel.channels.indexOfComponent(this),
					null);
			activeChannel = null;
			revalidate();
			fireKnetPanelParted(getChannel());
		}

		private void update() {
			List<String> prevMembers = new ArrayList<String>();
			for(Object m : membersModel.toArray()) {
				prevMembers.add((String) m);
			}
			membersModel.clear();
			boolean isMember = false;
			for(KNetEventSource s : channel.getMembers()) {
				membersModel.addElement((channel.getPlayers().contains(s) ? "\u2297 " : "\u25a2 ") + s.getName());
				if(s.equals(client.getSource())) {
					isMember = true;
					if(!line.isEnabled()) {
						line.setEnabled(true);
						line.setText("");
					}
				}
			}
			if(!isMember) {
				line.setEnabled(false);
				line.setText(lz.s("ch_join_to_chat"));
			}
			
			Collections.sort(new AbstractList<String>() {
				@Override
				public String get(int index) {
					return (String) membersModel.get(index);
				}

				@Override
				public int size() {
					return membersModel.size();
				}
				
				@Override
				public String set(int index, String element) {
					String old = get(index);
					membersModel.set(index, element);
					return old;
				}
			});
			
			List<String> newMembers = new ArrayList<String>();
			for(Object m : membersModel.toArray()) {
				newMembers.add((String) m);
			}
			List<String> parted = new ArrayList<String>(prevMembers); parted.removeAll(newMembers);
			List<String> joined = new ArrayList<String>(newMembers); joined.removeAll(prevMembers);
			for(String m : parted) {
				String text = history.getText();
				text += text.isEmpty() ? "" : "\n";
				text += m + " left the channel.";
				history.setText(text);
			}
			for(String m : joined) {
				String text = history.getText();
				text += text.isEmpty() ? "" : "\n";
				text += m + " joined the channel.";
				history.setText(text);
			}
			revalidate();
		}
		
		@Override
		public void channelJoined(KNetChannelEvent e) {
			if(EQInvoker.reinvoke(true, this, e))
				return;
			if(getChannel().equals(e.getChannel()))
				joined();
		}

		@Override
		public void channelUpdated(KNetChannelEvent e) {
			if(EQInvoker.reinvoke(true, this, e))
				return;
			if(getChannel().equals(e.getChannel()))
				update();
		}

		@Override
		public void channelLeft(KNetChannelEvent e) {
			if(EQInvoker.reinvoke(true, this, e))
				return;
			if(getChannel().equals(e.getChannel()))
				left();
		}

		@Override
		public void channelCreated(KNetChannelEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void channelDeleted(KNetChannelEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void channelChat(KNetChannelEvent e) {
			if(client.isLocal(e.getEvent()))
				return;
			if(e.getChannel().getId() != getChannel().getId())
				return;
			if(EQInvoker.reinvoke(true, this, e))
				return;
			String text = history.getText();
			text += text.isEmpty() ? "" : "\n";
			text += e.getEvent().getSource().getName() + ": ";
			text += (String) e.getEvent().get(CHANNEL_CHAT);
			history.setText(text);
		}
	}
	
	public class CreateChannelPanel extends JPanel {
		private KNetChannelInfo channel;
		private KNetChannelInfoPanel channelPanel;
		
		private JButton create = new JButton(new AbstractAction(lz.s("ccp_create")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				create();
			}
		});
		
		private JButton cancel = new JButton(new AbstractAction(lz.s("ccp_cancel")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		
		public CreateChannelPanel() {
			setLayout(new BorderLayout());
			channel = new KNetChannelInfo();
			channel.setGame(new KNetGameInfo());
			channelPanel = new KNetChannelInfoPanel(channel);
			channelPanel.updateEditor();
			
			JPanel p;
			
			p = new JPanel(new GridLayout(0, 1));
			p.add(create);
			p.add(cancel);
			add(p, BorderLayout.EAST);
			
			p = new JPanel(new BorderLayout());
			p.add(channelPanel, BorderLayout.CENTER);
			p.setBorder(BorderFactory.createTitledBorder(lz.s("ccp_border")));
			add(p, BorderLayout.CENTER);
		}
		
		public KNetChannelInfo getChannel() {
			return channel;
		}
		
		public KNetChannelInfoPanel getChannelPanel() {
			return channelPanel;
		}
		
		public void create() {
			channelPanel.updateChannel();
			client.fireTCP(CHANNEL_CREATE, channel);
			KNetPanel.this.remove(CreateChannelPanel.this);
			KNetPanel.this.add(createChannelPanel = new CreateChannelPanel(), CREATE_CHANNEL_PANEL_CARD);
			cards.show(KNetPanel.this, CONNECTED_PANEL_CARD);
			
		}
		
		public void cancel() {
			KNetPanel.this.remove(CreateChannelPanel.this);
			KNetPanel.this.add(createChannelPanel = new CreateChannelPanel(), CREATE_CHANNEL_PANEL_CARD);
			cards.show(KNetPanel.this, CONNECTED_PANEL_CARD);
		}
	}
	
	public class ViewChannelPanel extends JPanel {
		KNetChannelInfoPanel channelPanel = new KNetChannelInfoPanel(new KNetChannelInfo());
		
		private JButton dismiss = new JButton(new AbstractAction(lz.s("vcp_dismiss")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				cards.show(KNetPanel.this, CONNECTED_PANEL_CARD);
			}
		});
		
		public ViewChannelPanel() {
			channelPanel.setEditable(false);
			
			setLayout(new BorderLayout());
			
			JPanel p;
			
			p = new JPanel(new GridLayout(0, 1));
			p.add(dismiss);
			add(p, BorderLayout.EAST);
			
			p = new JPanel(new BorderLayout());
			p.add(channelPanel, BorderLayout.CENTER);
			p.setBorder(BorderFactory.createTitledBorder(lz.s("vcp_border")));
			add(p, BorderLayout.CENTER);
		}
	}
	
	public class ChangePasswordPanel extends JPanel {
		private JButton change = new JButton(new AbstractAction(lz.s("cpp_change")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!newPw.getText().equals(newPwRetype.getText()))
					return;
				String o = oldPw.getText();
				if(o.isEmpty())
					o = null;
				String n = newPw.getText();
				if(n.isEmpty())
					n = null;
				client.fireTCP(USER_UPDATE_PASSWORD, new String[] {o, n});
			}
		});
		
		private JButton cancel = new JButton(new AbstractAction(lz.s("cpp_cancel")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				cards.show(KNetPanel.this, CONNECTED_PANEL_CARD);
			}
		});
		
		private JPasswordField oldPw = new JPasswordField("");
		private JPasswordField newPw = new JPasswordField("");
		private JPasswordField newPwRetype = new JPasswordField("");
		
		public ChangePasswordPanel() {
			super(new BorderLayout());
			
			JPanel p = new JPanel(new GridLayout(0, 2));
			p.add(new JLabel(lz.s("cpp_old_pw")));
			p.add(oldPw);
			p.add(new JLabel(lz.s("cpp_new_pw")));
			p.add(newPw);
			p.add(new JLabel(lz.s("cpp_new_pw_retype")));
			p.add(newPwRetype);
			add(p, BorderLayout.CENTER);
			
			p = new JPanel(new GridLayout(0, 1));
			p.add(change);
			p.add(cancel);
			add(p, BorderLayout.EAST);
			
			DocumentListener rt = new DocumentListener() {
				private Color bg = newPwRetype.getBackground();
				
				@Override
				public void removeUpdate(DocumentEvent e) {
					updateBg();
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateBg();
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
					updateBg();
				}
				
				private void updateBg() {
					if(!newPw.getText().equals(newPwRetype.getText()))
						newPwRetype.setBackground(Color.PINK);
					else
						newPwRetype.setBackground(bg);
				}
			};
			
			newPw.getDocument().addDocumentListener(rt);
			newPwRetype.getDocument().addDocumentListener(rt);
		}
	}
	
	public KNetPanel(String defaultUsername, boolean ai) {
		this.defaultUsername = defaultUsername;
		this.ai = ai;
		
		setLayout(cards = new CardLayout());
		
		add(connectionsListPanel = new ConnectionListPanel(), CONNECTION_LIST_PANEL_CARD);
		add(loggingInPanel = new LoggingInPanel(), LOGGING_IN_PANEL_CARD);
		add(loginPanel = new LoginPanel(), LOGIN_PANEL_CARD);
		add(connectedPanel = new ConnectedPanel(), CONNECTED_PANEL_CARD);
		add(createChannelPanel = new CreateChannelPanel(), CREATE_CHANNEL_PANEL_CARD);
		add(viewChannelPanel = new ViewChannelPanel(), VIEW_CHANEL_CARD);
		add(changePasswordPanel = new ChangePasswordPanel(), CHANGE_PASSWORD_CARD);
		
		cards.show(this, CONNECTION_LIST_PANEL_CARD);
	}
	
	public ConnectionListPanel getConnectionsListPanel() {
		return connectionsListPanel;
	}
	
	public ConnectedPanel getConnectedPanel() {
		return connectedPanel;
	}
	
	public CreateChannelPanel getCreateChannelPanel() {
		return createChannelPanel;
	}
	
	public void addKNetPanelListener(KNetPanelListener l) {
		listenerList.add(KNetPanelListener.class, l);
	}
	
	public void removeKNetPanelListener(KNetPanelListener l) {
		listenerList.remove(KNetPanelListener.class, l);
	}
	
	public void init() {
		fireKnetPanelInit();
	}
	
	public void disconnect() {
		if(client != null)
			connectedPanel.disconnect.doClick();
	}
	
	public void shutdown() {
		fireKnetPanelShutdown();
	}
	
	protected void fireKnetPanelInit() {
		Object[] ll = listenerList.getListenerList();
		KNetPanelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetPanelListener.class) {
				if(e == null)
					e = new KNetPanelEvent(this);
				((KNetPanelListener) ll[i+1]).knetPanelInit(e);
			}
		}
	}
	
	protected void fireKnetPanelConnected() {
		Object[] ll = listenerList.getListenerList();
		KNetPanelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetPanelListener.class) {
				if(e == null)
					e = new KNetPanelEvent(this, client);
				((KNetPanelListener) ll[i+1]).knetPanelConnected(e);
			}
		}
	}

	protected void fireKnetPanelDisconnected() {
		Object[] ll = listenerList.getListenerList();
		KNetPanelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetPanelListener.class) {
				if(e == null)
					e = new KNetPanelEvent(this, client);
				((KNetPanelListener) ll[i+1]).knetPanelDisconnected(e);
			}
		}
	}

	protected void fireKnetPanelJoined(KNetChannelInfo channel) {
		Object[] ll = listenerList.getListenerList();
		KNetPanelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetPanelListener.class) {
				if(e == null)
					e = new KNetPanelEvent(this, client, channel);
				((KNetPanelListener) ll[i+1]).knetPanelJoined(e);
			}
		}
	}

	protected void fireKnetPanelParted(KNetChannelInfo channel) {
		Object[] ll = listenerList.getListenerList();
		KNetPanelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetPanelListener.class) {
				if(e == null)
					e = new KNetPanelEvent(this, client, channel);
				((KNetPanelListener) ll[i+1]).knetPanelParted(e);
			}
		}
	}

	protected void fireKnetPanelShutdown() {
		Object[] ll = listenerList.getListenerList();
		KNetPanelEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetPanelListener.class) {
				if(e == null)
					e = new KNetPanelEvent(this);
				((KNetPanelListener) ll[i+1]).knetPanelShutdown(e);
			}
		}
	}

	
	public KNetGameClient getClient() {
		return client;
	}

	public Map<Integer, ChannelPanel> getChannels() {
		return channels;
	}

	@Override
	public void channelJoined(KNetChannelEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelUpdated(KNetChannelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void channelLeft(KNetChannelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void channelCreated(final KNetChannelEvent e) {
		if(EQInvoker.reinvoke(true, this, e))
			return;
		ChannelPanel chanPan = new ChannelPanel(e.getChannel());
		connectedPanel.channels.addTab(e.getChannel().getName(), chanPan);
		channels.put(e.getChannel().getId(), chanPan);
		if(e.getChannel().getId() == KNetChannelInfo.LOBBY_CHANNEL_ID)
			client.fireTCP(CHANNEL_JOIN, CHANNEL_ID, KNetChannelInfo.LOBBY_CHANNEL_ID);
		connectedPanel.channels.setSelectedComponent(chanPan);
		revalidate();
		repaint();
	}

	@Override
	public void channelDeleted(final KNetChannelEvent e) {
		if(EQInvoker.reinvoke(true, this, e))
			return;
		ChannelPanel chanPan = channels.remove(e.getChannel().getId());
		connectedPanel.channels.removeTabAt(connectedPanel.channels.indexOfComponent(chanPan));
		revalidate();
		repaint();
	}

	@Override
	public void channelChat(KNetChannelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		if(e.is(USER_AUTHENTICATED)) {
			if(!e.get(USER_AUTHENTICATED, Boolean.class)) {
				System.out.println("User auth failed");
				cards.show(KNetPanel.this, LOGIN_PANEL_CARD);
			} else {
				System.out.println("User auth success");
				cards.show(KNetPanel.this, CONNECTED_PANEL_CARD);
				client.fireTCP(CHANNEL_LIST);
			}
		}
		if(e.is(USER_UPDATED_PASSWORD)) {
			if(!e.get(USER_UPDATED_PASSWORD, Boolean.class)) {
				System.out.println("User auth failed");
				cards.show(KNetPanel.this, LOGIN_PANEL_CARD);
			} else {
				System.out.println("User auth success");
				cards.show(KNetPanel.this, CONNECTED_PANEL_CARD);
				client.fireTCP(CHANNEL_LIST);
			}
		}
	}
}
