package org.zeromeaner.game.knet;

import java.io.IOException;

import javax.swing.event.EventListenerList;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import static org.zeromeaner.game.knet.KNetEvent.NetEventArgs.*;

public class KNetClient {
	protected String host;
	protected int port;
	protected Client client;
	
	protected KNetEventSource source;
	
	protected EventListenerList listenerList = new EventListenerList();
	
	protected Listener listener = new Listener() {
		@Override
		public void received(Connection connection, Object object) {
			KNetClient.this.received(connection, object);
		}
	};
	
	public KNetClient(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		client = new Client();
		client.connect(0, host, port, port);
	}
	
	public void start() {
		client.start();
	}
	
	public void stop() {
		client.stop();
	}

	protected void received(Connection connection, Object object) {
		KNetEvent e = (KNetEvent) object;
		if(e.is(ASSIGN_SOURCE))
			source = (KNetEventSource) e.get(ASSIGN_SOURCE);
		issue(e);
	}
	
	protected void issue(KNetEvent e) {
		Object[] ll = listenerList.getListenerList();
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetListener.class) {
				((KNetListener) ll[i+1]).knetEvented(e);
			}
		}
	}
	
	public void addKNetListener(KNetListener l) {
		listenerList.add(KNetListener.class, l);
	}
	
	public void removeKNetListener(KNetListener l) {
		listenerList.remove(KNetListener.class, l);
	}
	
	public void fireTCP(KNetEvent e) {
		issue(e);
		client.sendTCP(e);
	}
	
	public void fireUDP(KNetEvent e) {
		issue(e);
		client.sendUDP(e);
	}
}
