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
import org.kryomq.MqServer;
import org.kryomq.kryo.Kryo;
import org.kryomq.kryonet.Connection;
import org.kryomq.kryonet.KryoSerialization;
import org.kryomq.kryonet.Listener;
import org.kryomq.kryonet.Server;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetServer {
	private static final Logger log = Logger.getLogger(KNetServer.class);
	
	public static final int DEFAULT_PORT = 61897;
	
	protected int port;
	protected AtomicInteger nextClientId = new AtomicInteger(-1);
	protected MqServer server;
	
	
	protected Map<Integer, KNetEventSource> sourcesByConnectionId = new HashMap<Integer, KNetEventSource>();
	protected Map<KNetEventSource, Integer> connectionIds = new HashMap<KNetEventSource, Integer>();
//	protected Map<Integer, ExecutorService> senders = new HashMap<Integer, ExecutorService>();
	
	protected KNetChannelManager chanman;
	protected KNetUserManager uman;
	
	public KNetServer(int port) throws IOException, InterruptedException {
		this.port = port;
		Kryo kryo = new Kryo();
		KNetKryo.configure(kryo);
		server = new MqServer(port);
		server.start();
		chanman = new KNetChannelManager(port);
		chanman.start();
		uman = new KNetUserManager(port);
		uman.start();
	}
	
	public void stop() throws IOException {
		server.stop();
		chanman.stop();
	}
	
	public int getPort() {
		return port;
	}
}
