package org.zeromeaner.game.knet;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.junit.Test;
import org.zeromeaner.game.knet.KNetEvent.NetEventArgs;

public class KNetServerTest {
	@Test
	public void testConnectClients() throws Exception {
		KNetServer server = new KNetServer(11223);
		try {
			KNetClient c1 = new KNetClient("c1", "localhost", 11223);
			try {
				c1.start();
				final Semaphore sync = new Semaphore(0);
				KNetClient c2 = new KNetClient("c2", "localhost", 11223);
				try {
					c2.start();
					c2.addKNetListener(new KNetListener() {
						@Override
						public void knetEvented(KNetClient client, KNetEvent e) {
							System.out.println(e);
							sync.release();
						}
					});

					c1.fireTCP(c1.getSource().event(NetEventArgs.PAYLOAD, "hello there"));
					
					sync.acquire();

					c1.fireTCP(c1.getSource().event(NetEventArgs.PAYLOAD, "hello there"));
					
					sync.acquire();
				} finally {
					c2.stop();
				}
			} finally {
				c1.stop();
			}
		} finally {
			server.stop();
		}
	}
}
