package org.zeromeaner.game.knet;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.zeromeaner.game.knet.KNetEvent.NetEventArgs;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import static org.zeromeaner.game.knet.KNetEvent.NetEventArgs.*;

public class KNetServer {
	protected int port;
	protected AtomicInteger nextClientId = new AtomicInteger(-1);
	protected Server server;
	
	protected KNetEventSource source;
	
	protected Listener listener = new Listener() {
		@Override
		public void connected(Connection connection) {
			connection.sendTCP(source.event(
					ASSIGN_SOURCE, new KNetEventSource(nextClientId.incrementAndGet())
					));
		}
		
		@Override
		public void received(Connection connection, Object object) {
			if(!(object instanceof KNetEvent)) {
				System.out.println("Server discarding " + object);
				return;
			}
			KNetEvent e = (KNetEvent) object;
			if(e.is(NetEventArgs.UDP))
				server.sendToAllExceptUDP(connection.getID(), object);
			else
				server.sendToAllExceptTCP(connection.getID(), object);
		}
	};
	
	public KNetServer(int port) throws IOException {
		this.port = port;
		source = new KNetEventSource(nextClientId.incrementAndGet());
		server = new Server();
		KNetKryo.configure(server.getKryo());
		server.start();
		server.bind(port, port);
		server.addListener(new Listener.ThreadedListener(listener));
	}
	
	public void stop() {
		server.stop();
	}
}
