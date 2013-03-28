package org.zeromeaner.gui.knet;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.zeromeaner.game.knet.KNetChannelEvent;
import org.zeromeaner.game.knet.KNetChannelListener;
import org.zeromeaner.game.knet.KNetClient;
import org.zeromeaner.game.knet.KNetEvent;
import org.zeromeaner.game.knet.KNetEventSource;
import org.zeromeaner.game.knet.KNetGameClient;
import org.zeromeaner.game.knet.KNetListener;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;
import org.zeromeaner.game.knet.obj.KNetGameInfo;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.gui.applet.AppletMain;
import org.zeromeaner.util.EQInvoker;
import org.zeromeaner.util.KryoCopy;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

public class KNetPanel extends JPanel implements KNetChannelListener {
	private static final String CONNECTION_LIST_PANEL_CARD = ConnectionListPanel.class.getName();
	private static final String CONNECTED_PANEL_CARD = ConnectedPanel.class.getName();
	private static final String CREATE_CHANNEL_PANEL_CARD = CreateChannelPanel.class.getName();
	
	public static void main(String[] args) {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		JFrame f = new JFrame(KNetPanel.class.getName());
		f.add(new KNetPanel(""));
		f.pack();
		f.setSize(600, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
	
	private String defaultUsername;
	
	private CardLayout cards;
	private ConnectionListPanel connectionsListPanel;
	private ConnectedPanel connectedPanel;
	private CreateChannelPanel createChannelPanel;
	
	private Map<Integer, ChannelPanel> channels = new HashMap<Integer, ChannelPanel>();
	private ChannelPanel activeChannel;
	
	private KNetGameClient client;
	
	public class ConnectionListPanel extends JPanel {
		private DefaultListModel connectionsModel = new DefaultListModel();
		private JList connectionsList = new JList(connectionsModel);
		{
			connectionsList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		
		private JButton connect = new JButton(new AbstractAction("Connect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});
		
		private JButton exit = new JButton(new AbstractAction("Exit NetPlay") {
			@Override
			public void actionPerformed(ActionEvent e) {
				shutdown();
			}
		});
		
		private JTextField username = new JTextField(defaultUsername);
		
		public ConnectionListPanel() {
			super(new BorderLayout());
			JPanel p = new JPanel(new BorderLayout());
			JPanel q = new JPanel(new BorderLayout());
			q.add(new JLabel("Username:"), BorderLayout.WEST);
			q.add(username, BorderLayout.CENTER);
			p.add(q, BorderLayout.NORTH);
			p.add(new JScrollPane(connectionsList), BorderLayout.CENTER);
			p.setBorder(BorderFactory.createTitledBorder("Connections List"));
			add(p, BorderLayout.CENTER);
			p = new JPanel(new GridLayout(0, 1));
			p.add(connect);
			p.add(exit);
			add(p, BorderLayout.EAST);
			
			if(!GameManager.DEV_BUILD)
				connectionsModel.addElement("" + AppletMain.url.getHost() + ":61897");
			else {
				connectionsModel.addElement("" + AppletMain.url.getHost() + ":61898");
				connectionsModel.addElement("localhost:61897");
			}
			connectionsList.setSelectedIndex(0);
		}
		
		public void connect() {
			String host = ((String)connectionsList.getSelectedValue()).split(":")[0];
			int port = Integer.parseInt(((String) connectionsList.getSelectedValue()).split(":")[1]);
			
			client = new KNetGameClient("Player", host, port);
			client.addKNetChannelListener(KNetPanel.this);
			client.addKNetListener(new KNetListener() {
				@Override
				public void knetEvented(KNetClient client, KNetEvent e) {
					if(e.is(CONNECTED)) {
						String user = username.getText();
						if(user == null || user.isEmpty())
							user = "anonymous";
						client.getSource().setName(user);
						client.removeKNetListener(this);
						client.fireTCP(UPDATE_SOURCE, client.getSource());
					}
				}
			});
			try {
				client.start();
			} catch(Exception ex) {
				client.stop();
				client = null;
				JOptionPane.showMessageDialog(KNetPanel.this, ex.toString());
				return;
			}
			cards.show(KNetPanel.this, CONNECTED_PANEL_CARD);
			fireKnetPanelConnected();
		}
	}
	
	public class ConnectedPanel extends JPanel {
		private JTabbedPane channels = new JTabbedPane(JTabbedPane.LEFT);
		
		private JButton add = new JButton(new AbstractAction("Add Channel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				add();
			}
		});
		
		private JButton join = new JButton(new AbstractAction("Join Channel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				getVisibleChannel().join();
			}
		});
		
		private JButton leave = new JButton(new AbstractAction("Leave Channel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				getVisibleChannel().leave();
			}
		});
		
		private JButton disconnect = new JButton(new AbstractAction("Disconnect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				disconnect();
			}
		});
		
		public ConnectedPanel() {
			super(new BorderLayout());
			add(channels, BorderLayout.CENTER);
			JPanel p = new JPanel(new GridLayout(0, 1));
			p.add(add);
			p.add(join);
			p.add(leave);
			p.add(disconnect);
			add(p, BorderLayout.EAST);
		}
		
		public void disconnect() {
			client.stop();
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
			p.add(new JScrollPane(history), BorderLayout.CENTER);
			p.setBorder(BorderFactory.createTitledBorder("Chat Messages"));
			add(p, BorderLayout.CENTER);
			
			add(line, BorderLayout.SOUTH);
			
			p = new JPanel(new BorderLayout());
//			p.add(new JLabel("User List:"), BorderLayout.NORTH);
			p.add(new JScrollPane(membersList), BorderLayout.CENTER);
			p.setBorder(BorderFactory.createTitledBorder("User List"));
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
					line.setText("");
				}
			});
			
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
			if(getChannel().getId() == KNetChannelInfo.LOBBY_CHANNEL_ID)
				return;
			activeChannel = this;
			connectedPanel.channels.setIconAt(
					connectedPanel.channels.indexOfComponent(this),
					new ImageIcon(KNetPanel.class.getClassLoader().getResource("org/zeromeaner/game/knet/active-channel.png")));
			revalidate();
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
			membersModel.clear();
			line.setEnabled(false);
			line.setText("Join channel to chat");
			for(KNetEventSource s : channel.getMembers()) {
				membersModel.addElement(s.getName());
				if(s.equals(client.getSource())) {
					line.setEnabled(true);
					line.setText("");
				}
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
			if(EQInvoker.reinvoke(false, this, e))
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
		
		private JButton create = new JButton(new AbstractAction("Create Channel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				create();
			}
		});
		
		private JButton cancel = new JButton(new AbstractAction("Cancel") {
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
			p.setBorder(BorderFactory.createTitledBorder("Channel Editor"));
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
	
	public KNetPanel(String defaultUsername) {
		this.defaultUsername = defaultUsername;
		
		setLayout(cards = new CardLayout());
		
		add(connectionsListPanel = new ConnectionListPanel(), CONNECTION_LIST_PANEL_CARD);
		add(connectedPanel = new ConnectedPanel(), CONNECTED_PANEL_CARD);
		add(createChannelPanel = new CreateChannelPanel(), CREATE_CHANNEL_PANEL_CARD);
		
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
}
