package org.zeromeaner.knet.srv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventArgs;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetKryo;


import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetServer {
	private static final Logger log = Logger.getLogger(KNetServer.class);
	
	public static final int DEFAULT_PORT = 61897;
	
	protected int port;
	protected AtomicInteger nextClientId = new AtomicInteger(-1);
	protected Server server;
	
	
	protected Map<Integer, KNetEventSource> sourcesByConnectionId = new HashMap<Integer, KNetEventSource>();
	protected Map<KNetEventSource, Integer> connectionIds = new HashMap<KNetEventSource, Integer>();
	protected Map<Integer, ExecutorService> senders = new HashMap<Integer, ExecutorService>();
	
	protected KNetEventSource source;

	protected KNetChannelManager chanman;
	protected KNetUserManager uman;
	
	protected Listener listener = new Listener() {
		@Override
		public void connected(Connection connection) {
			try {
				KNetEventSource evs = new KNetEventSource(nextClientId.incrementAndGet());
				sourcesByConnectionId.put(connection.getID(), evs);
				connectionIds.put(evs, connection.getID());
				senders.put(connection.getID(), Executors.newSingleThreadExecutor());
				connection.sendTCP(source.event(
						ASSIGN_SOURCE, evs
						));
				log.info("Client connected:" + evs);
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
		
		@Override
		public void received(Connection connection, final Object object) {
			if(!(object instanceof KNetEvent)) {
				return;
			}
			try {
				KNetEvent e = (KNetEvent) object;
				KNetEventSource evs = sourcesByConnectionId.get(connection.getID());
				if(evs != null) {
					if(e.is(UPDATE_SOURCE)) {
						evs.updateFrom((KNetEventSource) e.get(UPDATE_SOURCE));
						log.info("Client updated source info:" + evs);
						return;
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

				List<KNetEventSource> recipients;
				if(global) {
					recipients = new ArrayList<KNetEventSource>(connectionIds.keySet());
					recipients.remove(e.getSource());
				} else {
					if(e.is(USER_AUTHENTICATE) || e.is(USER_CREATE) || e.is(USER_UPDATE_PASSWORD))
						recipients = Arrays.asList(uman.getSource());
					else if(e.is(ADDRESS))
						recipients = Arrays.asList(e.get(ADDRESS, KNetEventSource.class));
					else if(e.is(CHANNEL_ID))
						recipients = chanman.getMembers(e.get(CHANNEL_ID, Integer.class));
					else
						return;
					recipients = new ArrayList<KNetEventSource>(recipients);
					recipients.add(chanman.getSource());
				}
				for(final KNetEventSource r : recipients) {
					if(r.equals(e.getSource()))
						continue;
					if(e.is(UDP))
						server.sendToUDP(connectionIds.get(r), object);
					else {
						if(senders.get(connectionIds.get(r)) != null)
							senders.get(connectionIds.get(r)).execute(new Runnable() {
								@Override
								public void run() {
									server.sendToTCP(connectionIds.get(r), object);
								}
							});
					}
				}
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
		
		@Override
		public void disconnected(Connection connection) {
			try {
				KNetEventSource es = sourcesByConnectionId.get(connection.getID());
				KNetEvent e = new KNetEvent(es, DISCONNECTED);
				server.sendToAllExceptTCP(connection.getID(), e);
				senders.remove(connection.getID()).shutdown();
				log.info("Client disconnected:" + es);
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
	};
	
	public KNetServer(int port) throws IOException, InterruptedException {
		this.port = port;
		source = new KNetEventSource(nextClientId.incrementAndGet());
		server = new Server(1024 * 256, 1024 * 256);
		KNetKryo.configure(server.getKryo());
		server.getKryo().setClassLoader(KNetServer.class.getClassLoader());
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
