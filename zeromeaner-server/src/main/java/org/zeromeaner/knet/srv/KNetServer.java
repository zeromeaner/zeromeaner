package org.zeromeaner.knet.srv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetKryo;

import com.esotericsoftware.kryo.Kryo;
import org.mmmq.io.MasterServer;

public class KNetServer {
	private static final Logger log = Logger.getLogger(KNetServer.class);
	
	public static final int DEFAULT_PORT = 61897;
	
	protected int port;
	protected AtomicInteger nextClientId = new AtomicInteger(-1);
	protected MasterServer server;
	
	
	protected Map<Integer, KNetEventSource> sourcesByConnectionId = new HashMap<Integer, KNetEventSource>();
	protected Map<KNetEventSource, Integer> connectionIds = new HashMap<KNetEventSource, Integer>();
//	protected Map<Integer, ExecutorService> senders = new HashMap<Integer, ExecutorService>();
	
	protected KNetChannelManager chanman;
	protected KNetUserManager uman;
	
	public KNetServer(int port) throws IOException, InterruptedException {
		this.port = port;
		Kryo kryo = new Kryo();
		KNetKryo.configure(kryo);
		server = new MasterServer();
		server.start();
		server.bind(port, port);
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
