package org.zeromeaner.knet.srv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventArgs;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetKryo;


import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetServer {
	protected int port;
	protected AtomicInteger nextClientId = new AtomicInteger(-1);
	protected Server server;
	
	
	protected Map<Integer, KNetEventSource> sourcesByConnectionId = new HashMap<Integer, KNetEventSource>();
	protected Map<KNetEventSource, Integer> connectionIds = new HashMap<KNetEventSource, Integer>();
	
	protected KNetEventSource source;

	protected KNetChannelManager chanman;
	protected KNetUserManager uman;
	
	protected Listener listener = new Listener() {
		@Override
		public void connected(Connection connection) {
			KNetEventSource evs = new KNetEventSource(nextClientId.incrementAndGet());
			sourcesByConnectionId.put(connection.getID(), evs);
			connectionIds.put(evs, connection.getID());
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

			boolean global = true;
			if(e.is(CHANNEL_ID) || e.is(ADDRESS) || e.is(USER_AUTHENTICATE) || e.is(USER_CREATE) || e.is(USER_UPDATE_PASSWORD))
				global = false;
			if(!global) {
				for(KNetEventArgs arg : e.getArgs().keySet()) {
					if(arg.isGlobal())
						global = true;
				}
			}

			if(global) {
				if(e.is(KNetEventArgs.UDP))
					server.sendToAllExceptUDP(connection.getID(), object);
				else
					server.sendToAllExceptTCP(connection.getID(), object);
			} else {
				List<KNetEventSource> recipients;
				if(e.is(CHANNEL_ID))
					recipients = chanman.getMembers(e.get(CHANNEL_ID, Integer.class));
				else if(e.is(ADDRESS))
					recipients = Arrays.asList(e.get(ADDRESS, KNetEventSource.class));
				else if(e.is(USER_AUTHENTICATE) || e.is(USER_CREATE) || e.is(USER_UPDATE_PASSWORD))
					recipients = Arrays.asList(uman.getSource());
				else
					return;
				recipients = new ArrayList<KNetEventSource>(recipients);
				recipients.add(chanman.getSource());
				for(KNetEventSource r : recipients) {
					if(e.is(UDP))
						server.sendToUDP(connectionIds.get(r), object);
					else
						server.sendToTCP(connectionIds.get(r), object);
				}
			}
		}
		
		@Override
		public void disconnected(Connection connection) {
			KNetEventSource es = sourcesByConnectionId.get(connection.getID());
			KNetEvent e = new KNetEvent(es, DISCONNECTED);
			server.sendToAllExceptTCP(connection.getID(), e);
		}
	};
	
	public KNetServer(int port) throws IOException, InterruptedException {
		this.port = port;
		source = new KNetEventSource(nextClientId.incrementAndGet());
		server = new Server(1024 * 16, 1024 * 256);
		KNetKryo.configure(server.getKryo());
		server.start();
		server.bind(port, port);
		server.addListener(listener);
		chanman = new KNetChannelManager(port);
		chanman.start();
		uman = new KNetUserManager(port);
		uman.start();
	}
	
	public void stop() {
		server.stop();
		chanman.stop();
	}
	
	public int getPort() {
		return port;
	}
}
