package org.zeromeaner.mq;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class MqClientTest extends MqTest {
	@Test
	public void testConnect() throws Exception {
		MqServer server = new MqServer(11223);
		server.start();
		
		MqClient client = new MqClient("localhost", 11223);
		client.start();
		
		client.stop();
		
		server.stop();
	}
	
	@Test
	public void testSubscribe() throws Exception {
		MqServer server = new MqServer(11223);
		server.start();
		
		MqClient client = new MqClient("localhost", 11223);
		client.start();

		final CountDownLatch latch = new CountDownLatch(1);
		
		client.subscribe("test", new MessageListener() {
			@Override
			public void messageReceived(Message message) {
				latch.countDown();
			}
		});

		client.send(new Message("test", new byte[0], true));
		
		Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
		
	}
}
