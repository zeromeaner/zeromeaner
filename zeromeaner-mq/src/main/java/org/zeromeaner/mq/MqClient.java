package org.zeromeaner.mq;

import java.io.IOException;
import java.util.Set;

import org.zeromeaner.mq.Control.Command;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class MqClient extends Listener {
	protected String host;
	protected int port;
	protected Client client;
	
	protected TopicRegistry<MessageListener> registry = new TopicRegistry<>();
	
	public MqClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void start() throws IOException {
		client = new Client(256*1024, 256*1024);
		client.start();
		client.connect(10000, host, port, port);
		client.addListener(this);
		Kryo k = client.getKryo();
		k.register(byte[].class);
		k.register(Message.class, new FieldSerializer<>(k, Message.class));
		k.register(Control.class, new FieldSerializer<>(k, Control.class));
		k.register(Control.Command.class);
	}
	
	public void stop() throws IOException {
		client.close();
		client.stop();
	}
	
	@Override
	public void received(Connection connection, Object object) {
		if(object instanceof Message) {
			Message m = (Message) object;
			Set<MessageListener> subscribers = registry.get(m.topic);
			for(MessageListener l : subscribers) {
				l.messageReceived(m);
			}
		}
	}
	
	public synchronized void subscribe(String topic, MessageListener subscriber) {
		registry.subscribe(topic, subscriber);
		client.sendTCP(new Control(Command.SUBSCRIBE, topic));
	}
	
	public synchronized void unsubscribe(String topic, MessageListener subscriber) {
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
