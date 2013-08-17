package org.zeromeaner.mq;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromeaner.mq.Control.Command;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;

public class MqClient extends Listener {
	private static final Logger log = LoggerFactory.getLogger(MqClient.class);
	
	protected String host;
	protected int port;
	protected Client client;
	
	protected TopicRegistry<MessageListener> registry = new TopicRegistry<>();
	
	protected CountDownLatch personalTopicLatch = new CountDownLatch(1);
	protected String personalTopic;
	
	public MqClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void start() throws IOException {
		log.debug("{} connecting to {}:{}", this, host, port);
		client = new Client(256*1024, 256*1024, new KryoSerialization(new MqKryo()));
		client.start();
		client.connect(10000, host, port, port);
		client.addListener(this);
		log.debug("{} connected and registered", this);
	}
	
	public void stop() throws IOException {
		log.debug("{} stoppping", this);
		client.close();
		client.stop();
	}
	
	public String getPersonalTopic() {
		try {
			personalTopicLatch.await();
		} catch(InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
		return personalTopic;
	}
	
	@Override
	public void received(Connection connection, Object object) {
		if(object instanceof Message) {
			Message m = (Message) object;
			log.trace("{} dispatching {}", this, m);
			Set<MessageListener> subscribers = registry.get(m.topic);
			for(MessageListener l : subscribers) {
				l.messageReceived(m);
			}
		}
		if(object instanceof Control) {
			Control c = (Control) object;
			switch(c.command) {
			case PERSONAL_TOPIC:
				personalTopic = c.topic;
				personalTopicLatch.countDown();
				log.debug("{} received personal topic:{}", this, personalTopic);
				break;
			}
		}
	}
	
	public synchronized void subscribe(String topic, MessageListener subscriber) {
		log.debug("{} subscribing {} to topic {}", this, subscriber, topic);
		registry.subscribe(topic, subscriber);
		client.sendTCP(new Control(Command.SUBSCRIBE, topic));
	}
	
	public synchronized void unsubscribe(String topic, MessageListener subscriber) {
		log.debug("{} unsubscribing {} from topic {}", this, subscriber, topic);
		if(registry.unsubscribe(topic, subscriber).size() == 0)
			client.sendTCP(new Control(Command.UNSUBSCRIBE, topic));
	}
	
	public void send(Message message) {
		if(message.reliable)
			client.sendTCP(message);
		else
			client.sendUDP(message);
	}
}
