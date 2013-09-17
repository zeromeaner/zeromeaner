package org.zeromeaner.knet.srv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;
import org.mmmq.Message;
import org.mmmq.MessageListener;
import org.mmmq.MmmqTopics;
import org.mmmq.Topic;
import org.mmmq.io.MessagePacket;
import org.zeromeaner.dbo.Users;
import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventArgs;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetKryo;
import org.zeromeaner.knet.KNetListener;
import org.zeromeaner.knet.KNetTopics;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetUserManager extends KNetClient implements KNetListener {
	private static final Logger log = Logger.getLogger(KNetUserManager.class);

	private Topic auth;
	
	public KNetUserManager(int port) {
		super("UserManager", "localhost", port);
		
		addKNetListener(this);
	}
	
	@Override
	public KNetClient start() throws IOException, InterruptedException {
		super.start();

		auth = new Topic(KNetTopics.AUTH).addTag(Topic.PRIVILEGED_TAG);
		
		client.claimOwnership(auth);
		client.subscribe(auth, this);
//		client.setOrigin(Topics.PRIVILEGED + KNetTopics.AUTH);
		origin = auth;
		
		client.subscribe(MmmqTopics.CLIENT_CONNECTED, new MessageListener() {
			@Override
			public void messageReceived(Message m) {
				String direct = new String(m.message());
				KNetEventSource s = new KNetEventSource(direct, -1);
				KNetEvent e = s.event(CONNECTED, true);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				Output output = new Output(bout);
				Kryo kryo = new Kryo();
				KNetKryo.configure(kryo);
				kryo.writeClassAndObject(output, e);
				client.sendMessage((Message) new MessagePacket(auth, new Topic(KNetTopics.CONNECTION)).withMessage(bout.toByteArray()).tcp());
			}
		});
		client.subscribe(MmmqTopics.CLIENT_DISCONNECTED, new MessageListener() {
			@Override
			public void messageReceived(Message m) {
				String direct = new String(m.message());
				KNetEventSource s = new KNetEventSource(direct, -1);
				KNetEvent e = s.event(DISCONNECTED, true);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				Output output = new Output(bout);
				Kryo kryo = new Kryo();
				KNetKryo.configure(kryo);
				kryo.writeClassAndObject(output, e);
				client.sendMessage((Message) new MessagePacket(auth, new Topic(KNetTopics.CONNECTION)).withMessage(bout.toByteArray()).tcp());
			}
		});
		
		return this;
	}
	
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		try {
			if(e.is(USER_AUTHENTICATE)) {
				String email = e.getSource().getName();
				String pw = e.get(USER_AUTHENTICATE, String.class);
				boolean success = false;
				try {
					success = Users.checkPassword(email, pw);
				} catch(PersistenceException ex) {
					success = true; // if the database is down, authenticate anyway
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				reply(e, USER_AUTHENTICATED, success);
			}
			if(e.is(USER_CREATE)) {
				String email = e.getSource().getName();
				String pw = e.get(USER_CREATE, String.class);
				boolean success = true;
				try {
					Users.insert(email, pw);
				} catch(Exception ex) {
					success = false;
				}
				reply(e, USER_CREATED, success);
			}
			if(e.is(USER_UPDATE_PASSWORD)) {
				String email = e.getSource().getName();
				String pw = e.get(USER_UPDATE_PASSWORD, String[].class)[0];
				String newPw = e.get(USER_UPDATE_PASSWORD, String[].class)[1];
				boolean success = true;
				try {
					Users.updatePassword(Users.select(email), pw, newPw);
				} catch(Exception ex) {
					success = false;
				}
				reply(e, USER_UPDATED_PASSWORD, success);
			}
		} catch(Throwable t) {
			log.error(t);
		}
	}

}
