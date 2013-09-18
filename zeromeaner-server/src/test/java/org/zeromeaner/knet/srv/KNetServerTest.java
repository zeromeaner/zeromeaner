package org.zeromeaner.knet.srv;

import java.util.concurrent.Semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventArgs;
import org.zeromeaner.knet.KNetListener;
import org.zeromeaner.knet.srv.KNetServer;

public class KNetServerTest {
	private KNetServer server;
	private KNetClient sender;
	private KNetClient receiver;

	@Before
	public void before() throws Exception {
		server = new KNetServer(11223);
		sender = new KNetClient("sender", "localhost", server.getPort()).start();
		receiver = new KNetClient("receiver", "localhost", server.getPort()).start();
	}

	@After
	public void after() throws Exception {
		if(receiver != null)
			receiver.stop();
		if(sender != null)
			sender.stop();
		if(server != null)
			server.stop();
	}

//	@Test
	public void testConnectClients() throws Exception {
		final Semaphore sync = new Semaphore(0);
		receiver.addKNetListener(new KNetListener() {
			@Override
			public void knetEvented(KNetClient client, KNetEvent e) {
				System.out.println(e);
				sync.release();
			}
		});

		sender.fireTCP(sender.getSource().event(KNetEventArgs.PAYLOAD, "hello there"));

		sync.acquire();

		sender.fireTCP(sender.getSource().event(KNetEventArgs.PAYLOAD, "hello there"));

		sync.acquire();
	}
}
