package org.zeromeaner.game.knet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

public class KNetServer {
	protected int port;
	protected AtomicInteger nextClientId = new AtomicInteger(-1);
	protected Server server;
	
	protected Map<Integer, KNetEventSource> sourcesByConnectionId = new HashMap<Integer, KNetEventSource>();
	
	protected KNetEventSource source;
	
	protected Listener listener = new Listener() {
		@Override
		public void connected(Connection connection) {
			KNetEventSource evs = new KNetEventSource(nextClientId.incrementAndGet());
			sourcesByConnectionId.put(connection.getID(), evs);
			connection.sendTCP(source.event(
					ASSIGN_SOURCE, evs
					));
		}
		
		@Override
		public void received(Connection connection, Object object) {
			if(!(object instanceof KNetEvent)) {
				return;
			}
			KNetEvent e = (KNetEvent) object;
			KNetEventSource evs = sourcesByConnectionId.get(connection.getID());
			if(evs != null) {
				if(e.is(UPDATE_SOURCE)) {
					evs.updateFrom((KNetEventSource) e.get(UPDATE_SOURCE));
				}
				e.getSource().updateFrom(evs);
			}
			if(e.is(KNetEventArgs.UDP))
				server.sendToAllExceptUDP(connection.getID(), object);
			else
				server.sendToAllExceptTCP(connection.getID(), object);
		}
		
		@Override
		public void disconnected(Connection connection) {
			KNetEventSource es = sourcesByConnectionId.get(connection.getID());
			KNetEvent e = new KNetEvent(es, DISCONNECTED);
			server.sendToAllExceptTCP(connection.getID(), e);
		}
	};
	
	public KNetServer(int port) throws IOException {
		this.port = port;
		source = new KNetEventSource(nextClientId.incrementAndGet());
		server = new Server(1024 * 16, 1024 * 256);
		KNetKryo.configure(server.getKryo());
		server.start();
		server.bind(port, port);
		server.addListener(listener);
	}
	
	public void stop() {
		server.stop();
	}
	
	public int getPort() {
		return port;
	}
}
