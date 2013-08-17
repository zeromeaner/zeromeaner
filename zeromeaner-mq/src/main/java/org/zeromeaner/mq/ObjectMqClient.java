package org.zeromeaner.mq;

import java.io.IOException;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;

public class ObjectMqClient implements MessageListener {

	protected Kryo kryo;
	
	protected String host;
	protected int port;
	
	protected MqClient client;
	
	protected TopicRegistry<ObjectListener> registry = new TopicRegistry<>();
	
	public ObjectMqClient(Kryo kryo, String host, int port) {
		this.kryo = kryo;
		this.host = host;
		this.port = port;
	}
	
	public void start() throws IOException {
		client = new MqClient(host, port);
		client.start();
	}
	
	public void stop() throws IOException {
		client.stop();
	}
	
	public String getPersonalTopic() {
		return client.getPersonalTopic();
	}
	
	public void subscribe(String topic, ObjectListener subscriber) {
		registry.subscribe(topic, subscriber);
		client.subscribe(topic, this);
	}
	
	public void unsubscribe(String topic, ObjectListener subscriber) {
		if(registry.unsubscribe(topic, subscriber).size() == 0)
			client.unsubscribe(topic, this);
	}
	
	@Override
	public void messageReceived(Message message) {
		Set<ObjectListener> subscribers = registry.get(message.topic);
		Object value = message.get(kryo);
		for(ObjectListener l : subscribers) {
			l.objectReceived(message, value);
		}
	}
	
	public void send(String topic, boolean reliable, Object value) {
		Message message = new Message(topic, reliable).set(kryo, value);
		client.send(message);
	}
}
