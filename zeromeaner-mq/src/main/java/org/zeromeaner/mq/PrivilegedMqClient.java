package org.zeromeaner.mq;

import org.zeromeaner.mq.Control.Command;

public class PrivilegedMqClient extends MqClient {

	public PrivilegedMqClient(String host, int port) {
		super(host, port);
	}

	@Override
	public synchronized void subscribe(String topic, MessageListener subscriber) {
		registry.subscribe(topic, subscriber);
		if(topic.startsWith(Topics.PRIVILEGED))
			client.sendTCP(new Control(Command.PRIVILEGED_SUBSCRIBE, topic));
		else
			client.sendTCP(new Control(Command.SUBSCRIBE, topic));
	}
	
	public void setOrigin(String topic) {
		client.sendTCP(new Control(Command.PRIVILEGED_SET_ORIGIN, topic));
	}
}
