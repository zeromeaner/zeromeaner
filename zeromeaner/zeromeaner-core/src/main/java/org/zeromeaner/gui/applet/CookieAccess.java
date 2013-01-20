package org.zeromeaner.gui.applet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class CookieAccess {
	public static URI uri;
	
	public static Map<String, String> get() {
		try {
			String data = "";
			
			CookieManager manager = (CookieManager) CookieHandler.getDefault();
			List<HttpCookie> cookies = manager.getCookieStore().get(uri);
			for(HttpCookie c : cookies) {
				if("c".equals(c.getName()))
					data = c.getValue();
			}
			
			if("".equals(data))
				return new TreeMap<String, String>();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			for(int i = 0; i < data.length(); i += 2) {
				bout.write(Integer.parseInt(data.substring(i, i+2), 16));
			}
			return (Map<String, String>) new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray())).readObject();
		} catch(Throwable ex) {
//			JOptionPane.showMessageDialog(applet, ex.toString());
			ex.printStackTrace();
			return new TreeMap<String, String>();
		}
	}
	
	public static void set(String key, String value) {
		Map<String, String> c = get();
		c.put(key, value);
		set(c);
	}

	public static void set(Map<String, String> cookie) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(cookie);
			out.close();

			StringBuilder sb = new StringBuilder();
			for(byte b : bout.toByteArray()) {
				sb.append(String.format("%02x", b));
			}
			String value = sb.toString();
			
			CookieManager manager = (CookieManager) CookieHandler.getDefault();
			CookieStore store = manager.getCookieStore();
			HttpCookie c = new HttpCookie("c", value);
			store.add(uri, c);
		} catch(Throwable ex) {
//			JOptionPane.showMessageDialog(applet, ex.toString());
			ex.printStackTrace();
		}
	}
}
