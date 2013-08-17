package org.zeromeaner.mq;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.zeromeaner.mq.Control.Command;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class MqServer extends Listener {
	protected int port;
	
	protected Server server;
	
	protected TopicRegistry<Connection> registry = new TopicRegistry<>();
	protected TopicRegistry<Connection> privileged = new TopicRegistry<>();
	
	protected Map<Connection, String> origins = new ConcurrentHashMap<>();
	
	public MqServer(int port) {
		this.port = port;
			
	}
	
	public int getPort() {
		return port;
	}
	
	public void start() throws IOException {
		server = new Server(1024*256, 1024*256);
		server.start();
		server.bind(port, port);
		server.addListener(this);
		Kryo k = server.getKryo();
		k.register(byte[].class);
		k.register(Message.class, new FieldSerializer<>(k, Message.class));
		k.register(Control.class, new FieldSerializer<>(k, Control.class));
		k.register(Control.Command.class);
		
	}
	
	public void stop() throws IOException {
		server.close();
		server.stop();
	}
	
	protected String personalTopic(Connection connection) {
		return Topics.PRIVILEGED + Topics.CLIENT + connection.getID();
	}
	
	@Override
	public void connected(Connection connection) {
		String personalTopic = personalTopic(connection);
		privileged.subscribe(personalTopic, connection);
		server.sendToTCP(connection.getID(), new Control(Command.PERSONAL_TOPIC, personalTopic));
		origins.put(connection, personalTopic);
	}
	
	@Override
	public void received(Connection connection, Object object) {
		if(object instanceof Message) {
			Message m = (Message) object;
			m.origin = personalTopic(connection);
			Set<Connection> subscribers = registry.get(m.topic);
			for(Connection c : subscribers) {
				if(m.reliable)
					server.sendToTCP(c.getID(), m);
				else
					server.sendToUDP(c.getID(), m);
			}
		}
		if(object instanceof Control) {
			Control c = (Control) object;
			switch(c.command) {
			case SUBSCRIBE:
				if(!c.topic.startsWith(Topics.PRIVILEGED) || privileged.get(c.topic).contains(connection))
					registry.subscribe(c.topic, connection);
				break;
			case UNSUBSCRIBE:
				registry.unsubscribe(c.topic, connection);
				break;
			case PRIVILEGED_SUBSCRIBE:
				if(connection.getRemoteAddressTCP().getAddress().isLoopbackAddress())
					registry.subscribe(c.topic, connection);
				break;
			case PRIVILEGED_SET_ORIGIN:
				if(connection.getRemoteAddressTCP().getAddress().isLoopbackAddress())
					origins.put(connection, c.topic);
				break;
			}
		}
	}
}
