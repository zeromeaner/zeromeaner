package org.zeromeaner.knet.srv;

import java.io.IOException;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;
import org.zeromeaner.dbo.Users;
import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventArgs;
import org.zeromeaner.knet.KNetEventSource;
import org.zeromeaner.knet.KNetListener;
import org.zeromeaner.mq.Control;
import org.zeromeaner.mq.Message;
import org.zeromeaner.mq.Topics;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetUserManager extends KNetClient implements KNetListener {
	private static final Logger log = Logger.getLogger(KNetUserManager.class);

	public KNetUserManager(int port) {
		super("UserManager", "localhost", port);
		
		addKNetListener(this);
	}
	
	@Override
	public KNetClient start() throws IOException, InterruptedException {
		super.start();

		client.subscribe(Topics.PRIVILEGED + Topics.AUTH, this);
		client.setOrigin(Topics.PRIVILEGED + Topics.AUTH);
		
		return this;
	}
	
	@Override
	protected void controlled(Control control) {
		super.controlled(control);
		KNetEventSource s;
		KNetEvent e;
		Message m;
		switch(control.command) {
		case CONNECTED:
			s = new KNetEventSource(control.topic, -1);
			e = s.event(CONNECTED, true);
			m = new Message(Topics.CONNECTION, true).set(kryo, e);
			client.send(m);
			break;
		case DISCONNECTED:
			s = new KNetEventSource(control.topic, -1);
			e = s.event(DISCONNECTED, true);
			m = new Message(Topics.CONNECTION, true).set(kryo, e);
			client.send(m);
			break;
		}
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
