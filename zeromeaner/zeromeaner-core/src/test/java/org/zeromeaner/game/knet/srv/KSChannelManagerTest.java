package org.zeromeaner.game.knet.srv;

import java.util.concurrent.Semaphore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zeromeaner.game.knet.KNetClient;
import org.zeromeaner.game.knet.KNetEvent;
import org.zeromeaner.game.knet.KNetListener;
import org.zeromeaner.game.knet.KNetServer;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

public class KSChannelManagerTest {
	private static KNetServer server;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		server = new KNetServer(61897);
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		server.stop();
	}
	
	@Test
	public void testChannelList() throws Exception {
		KSChannelManager chanman = new KSChannelManager(server.getPort());
		KNetClient c = new KNetClient("localhost", server.getPort());
		final Semaphore sync = new Semaphore(0);
		c.addKNetListener(new KNetListener() {
			@Override
			public void knetEvented(KNetClient client, KNetEvent e) {
				System.out.println(e);
				if(e.is(CHANNEL_LIST) && e.is(PAYLOAD))
					sync.release();
			}
		});
		try {
			chanman.start();
			try {
				c.start();
				c.fire(CHANNEL_LIST, true);
				sync.acquire();
			} finally {
				c.stop();
			}
		} finally {
			chanman.stop();
		}
	}
}
