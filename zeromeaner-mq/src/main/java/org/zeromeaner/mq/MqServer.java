package org.zeromeaner.mq;

import java.io.IOException;
import java.util.Set;

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
	
	@Override
	public void connected(Connection connection) {
		server.sendToTCP(connection.getID(), new Control(Command.PERSONAL_TOPIC, "client." + connection.getID()));
	}
	
	@Override
	public void received(Connection connection, Object object) {
		if(object instanceof Message) {
			Message m = (Message) object;
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
				registry.subscribe(c.topic, connection);
				break;
			case UNSUBSCRIBE:
				registry.unsubscribe(c.topic, connection);
				break;
			}
		}
	}
}
