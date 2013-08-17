package org.zeromeaner.mq;

import org.junit.Test;

public class MqServerTest {
	@Test
	public void testStartStop() throws Exception {
		MqServer server = new MqServer(11223);
		server.start();
		server.stop();
	}
}
