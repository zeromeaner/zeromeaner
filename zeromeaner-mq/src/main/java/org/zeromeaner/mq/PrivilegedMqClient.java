package org.zeromeaner.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromeaner.mq.Control.Command;

public class PrivilegedMqClient extends MqClient {
	private static final Logger log = LoggerFactory.getLogger(PrivilegedMqClient.class);
	
	public PrivilegedMqClient(String host, int port) {
		super(host, port);
	}

	@Override
	public synchronized void subscribe(String topic, MessageListener subscriber) {
		super.subscribe(topic, subscriber);
		if(topic.startsWith(Topics.PRIVILEGED)) {
			log.trace("{} making privileged subscription request to topic {}", this, topic);
			client.sendTCP(new Control(Command.PRIVILEGED_SUBSCRIBE, topic));
		}
	}
	
	public void setOrigin(String topic) {
		log.trace("{} making privileged set origin request to topic {}", this, topic);
		client.sendTCP(new Control(Command.PRIVILEGED_SET_ORIGIN, topic));
	}
}
