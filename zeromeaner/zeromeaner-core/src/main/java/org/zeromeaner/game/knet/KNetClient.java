package org.zeromeaner.game.knet;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.swing.event.EventListenerList;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import static org.zeromeaner.game.knet.KNetEvent.NetEventArgs.*;

public class KNetClient {
	protected String type;
	protected String host;
	protected int port;
	protected Client client;
	
	protected KNetEventSource source;
	
	protected EventListenerList listenerList = new EventListenerList();
	
	protected Listener listener = new Listener() {
		@Override
		public void received(Connection connection, Object object) {
			if(!(object instanceof KNetEvent))
				return;
			KNetClient.this.received(connection, (KNetEvent) object);
		}
		
		@Override
		public void disconnected(Connection connection) {
			issue(source.event(DISCONNECTED, true));
		}
	};
	
	public KNetClient(String host, int port) {
		this("Unknown", host, port);
	}
	
	public KNetClient(String type, String host, int port) {
		this.type = type;
		this.host = host;
		this.port = port;
		client = new Client();
		KNetKryo.configure(client.getKryo());
		client.addListener(new Listener.ThreadedListener(listener));
	}
	
	public void start() throws IOException, InterruptedException {
		final Semaphore sync = new Semaphore(0);
		KNetListener lsync = new KNetListener() {
			@Override
			public void knetEvented(KNetClient client, KNetEvent e) {
				sync.release();
				removeKNetListener(this);
			}
		};
		addKNetListener(lsync);
		client.start();
		client.connect(1000, host, port, port);
		sync.acquire();
	}
	
	public void stop() {
		client.stop();
	}

	protected void received(Connection connection, KNetEvent e) {
		if(e.is(ASSIGN_SOURCE)) {
			source = (KNetEventSource) e.get(ASSIGN_SOURCE);
			source.setType(type);
			issue(source.event(CONNECTED, true));
		}
		issue(e);
	}
	
	protected void issue(KNetEvent e) {
		Object[] ll = listenerList.getListenerList();
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == KNetListener.class) {
				((KNetListener) ll[i+1]).knetEvented(this, e);
			}
		}
	}
	
	public KNetEventSource getSource() {
		return source;
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
