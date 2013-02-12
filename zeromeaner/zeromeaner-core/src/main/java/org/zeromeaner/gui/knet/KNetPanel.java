package org.zeromeaner.gui.knet;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.zeromeaner.game.knet.KNetClient;
import org.zeromeaner.game.knet.KNetEvent;
import org.zeromeaner.game.knet.KNetEventSource;
import org.zeromeaner.game.knet.KNetListener;
import org.zeromeaner.game.knet.obj.KNetChannelInfo;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

public class KNetPanel extends JPanel implements KNetListener {
	private static final String CONNECTION_LIST_PANEL_CARD = ConnectionListPanel.class.getName();
	private static final String CONNECTED_PANEL_CARD = ConnectedPanel.class.getName();
	
	public static void main(String[] args) {
		JFrame f = new JFrame(KNetPanel.class.getName());
		f.add(new KNetPanel());
		f.pack();
		f.setSize(600, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
	
	private CardLayout cards;
	private ConnectionListPanel connectionsListPanel;
	private ConnectedPanel connectedPanel;
	
	private KNetClient client;
	
	private class ConnectionListPanel extends JPanel {
		private DefaultListModel connectionsModel = new DefaultListModel();
		private JList connectionsList = new JList(connectionsModel);
		{
			connectionsList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		
		private JButton connect = new JButton(new AbstractAction("Connect") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String host = ((String)connectionsList.getSelectedValue()).split(":")[0];
				int port = Integer.parseInt(((String) connectionsList.getSelectedValue()).split(":")[1]);
				
				client = new KNetClient("Player", host, port);
				client.addKNetListener(KNetPanel.this);
				try {
					client.start();
				} catch(Exception ex) {
					client.stop();
					client = null;
					throw new RuntimeException(ex);
				}
				cards.show(KNetPanel.this, CONNECTED_PANEL_CARD);
				fireKnetPanelConnected();
			}
		});
		
		private JButton add = new JButton(new AbstractAction("Add Server") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		private JButton remove = new JButton(new AbstractAction("Remove Server") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		public ConnectionListPanel() {
			super(new BorderLayout());
			add(new JScrollPane(connectionsList), BorderLayout.CENTER);
			JPanel p = new JPanel(new GridLayout(0, 1));
			p.add(connect);
			p.add(add);
			p.add(remove);
			add(p, BorderLayout.EAST);
			
			connectionsModel.addElement("localhost:61897");
		}
	}
	
	private class ConnectedPanel extends JPanel {
		private JTabbedPane channels = new JTabbedPane(JTabbedPane.LEFT);
		
		public ConnectedPanel() {
			super(new BorderLayout());
			add(channels, BorderLayout.CENTER);
		}
	}
	
	private class ChannelPanel extends JPanel implements KNetListener {
		private KNetChannelInfo channel;
		
		private DefaultListModel membersModel = new DefaultListModel();
		private JList membersList = new JList(membersModel);
		private JTextArea history = new JTextArea("");
		private JTextField line = new JTextField("");
		
		public ChannelPanel(KNetChannelInfo channel) {
			this.channel = channel;
			
			setLayout(new BorderLayout());
			
			add(new JScrollPane(history), BorderLayout.CENTER);
			add(line, BorderLayout.SOUTH);
			add(new JScrollPane(membersList), BorderLayout.WEST);
			
			client.addKNetListener(this);
		}

		private void update() {
			membersModel.clear();
			for(KNetEventSource s : channel.getMembers()) {
				membersModel.addElement(s.getName());
			}
		}
		
		@Override
		public void knetEvented(KNetClient client, KNetEvent e) {
			if(e.get(PAYLOAD) instanceof KNetChannelInfo) {
				channel = (KNetChannelInfo) e.get(PAYLOAD);
				update();
			}
		}
	}
	
	public KNetPanel() {
		setLayout(cards = new CardLayout());
		
		add(connectionsListPanel = new ConnectionListPanel(), CONNECTION_LIST_PANEL_CARD);
		add(connectedPanel = new ConnectedPanel(), CONNECTED_PANEL_CARD);
		
		cards.show(this, CONNECTION_LIST_PANEL_CARD);
	}
	
	public void addKNetPanelListener(KNetPanelListener l) {
		listenerList.add(KNetPanelListener.class, l);
	}
	
	public void removeKNetPanelListener(KNetPanelListener l) {
		listenerList.remove(KNetPanelListener.class, l);
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

	@Override
	public void knetEvented(final KNetClient client, final KNetEvent e) {
		if(!EventQueue.isDispatchThread()) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					knetEvented(client, e);
				}
			});
			return;
		}
		System.out.println(e);
		if(e.is(CONNECTED)) {
			client.fireTCP(CHANNEL_LIST, true);
			return;
		}
		if(e.is(CHANNEL_LIST) && e.is(PAYLOAD)) {
			KNetChannelInfo[] channels = (KNetChannelInfo[]) e.get(PAYLOAD);
			for(KNetChannelInfo channel : channels) {
				ChannelPanel chanPan = new ChannelPanel(channel);
				connectedPanel.channels.addTab(channel.getName(), chanPan);
				if(channel.getId() == 0) {
					client.fireTCP(CHANNEL_JOIN, CHANNEL_ID, channel.getId());
				}
			}
		}
	}
}
