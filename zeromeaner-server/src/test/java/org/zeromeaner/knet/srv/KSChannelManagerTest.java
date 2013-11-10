package org.zeromeaner.knet.srv;

import java.util.concurrent.Semaphore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetListener;
import org.zeromeaner.knet.obj.KNetChannelInfo;
import org.zeromeaner.knet.srv.KNetServer;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KSChannelManagerTest {
	private static KNetServer server;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		server = new KNetServer(61899);
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		server.stop();
	}
	
//	@Test
	public void testChannelList() throws Exception {
		KNetClient c = new KNetClient("localhost", server.getPort());
		final Semaphore sync = new Semaphore(0);
		c.addKNetListener(new KNetListener() {
			@Override
			public void knetEvented(KNetClient client, KNetEvent e) {
				System.out.println(e);
				if(e.is(CHANNEL_LIST) && (e.get(CHANNEL_INFO) instanceof KNetChannelInfo[]) )
					sync.release();
			}
		});
		try {
			c.start();
			c.fire(CHANNEL_LIST, true);
			sync.acquire();
		} finally {
			c.stop();
		}
	}
}
