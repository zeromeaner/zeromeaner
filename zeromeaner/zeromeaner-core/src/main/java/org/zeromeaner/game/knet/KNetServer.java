package org.zeromeaner.game.knet;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import static org.zeromeaner.game.knet.KNetEvent.NetEventArgs.*;

public class KNetServer {
	protected int port;
	protected AtomicInteger nextClientId = new AtomicInteger();
	protected Server server;
	
	protected KNetEventSource source;
	
	protected Listener listener = new Listener() {
		@Override
		public void connected(Connection connection) {
			connection.sendTCP(source.event(
					ASSIGN_SOURCE, new KNetEventSource(nextClientId.incrementAndGet())
					));
					
		}
	};
	
	public KNetServer(int port) throws IOException {
		this.port = port;
		source = new KNetEventSource(nextClientId.incrementAndGet());
		server = new Server();
		server.bind(port, port);
		server.addListener(new Listener.ThreadedListener(listener));
	}
	
	public void start() {
		server.start();
	}
	
	public void stop() {
		server.stop();
	}
}
