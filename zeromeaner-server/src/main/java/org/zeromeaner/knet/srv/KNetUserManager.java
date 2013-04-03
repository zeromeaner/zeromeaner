package org.zeromeaner.knet.srv;

import org.zeromeaner.dbo.Users;
import org.zeromeaner.knet.KNetClient;
import org.zeromeaner.knet.KNetEvent;
import org.zeromeaner.knet.KNetEventArgs;
import org.zeromeaner.knet.KNetListener;

import static org.zeromeaner.knet.KNetEventArgs.*;

public class KNetUserManager extends KNetClient implements KNetListener {

	public KNetUserManager(int port) {
		super("UserManager", "localhost", port);
		
		addKNetListener(this);
	}
	
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		if(e.is(USER_AUTHENTICATE)) {
			String email = e.getSource().getName();
			String pw = e.get(USER_AUTHENTICATE, String.class);
			boolean success = false;
			try {
				success = Users.checkPassword(email, pw);
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
	}

}
