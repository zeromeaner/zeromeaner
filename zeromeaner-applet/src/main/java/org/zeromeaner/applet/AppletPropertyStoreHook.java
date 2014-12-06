package org.zeromeaner.applet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;

import netscape.javascript.JSObject;

import org.zeromeaner.gui.reskin.StandaloneApplet;
import org.zeromeaner.util.PropertyStoreHook;

public class AppletPropertyStoreHook implements PropertyStoreHook {

	protected Properties props;

	public AppletPropertyStoreHook() throws IOException{
		props = new Properties();
		try {
			String data = "";
			JSObject myBrowser = JSObject.getWindow(StandaloneApplet.getInstance());
			JSObject myDocument = (JSObject) myBrowser.getMember("document");

			String myCookie = (String) myDocument.getMember("cookie");

			if (myCookie.length() > 0) {
				String[] cookies = myCookie.split(";");
				for (String cookie : cookies) {
					int pos = cookie.indexOf("=");
					if (cookie.substring(0, pos).trim().equals("c")) {
						data = cookie.substring(pos + 1);
						break;
					}
				}
			}
			if("".equals(data))
				return;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			for(int i = 0; i < data.length(); i += 2) {
				bout.write(Integer.parseInt(data.substring(i, i+2), 16));
			}
			props = (Properties) new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray())).readObject();
		} catch(Throwable t) {
		}
	}

	protected void store() {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(props);
			out.close();

			StringBuilder sb = new StringBuilder();
			for(byte b : bout.toByteArray()) {
				sb.append(String.format("%02x", b));
			}
			String value = sb.toString();
			JSObject win = JSObject.getWindow(StandaloneApplet.getInstance());
			JSObject doc = (JSObject) win.getMember("document");
			String data = "c=" + value + "; path=/; expires=Thu, 31-Dec-2019 12:00:00 GMT";
			doc.setMember("cookie", data);
		} catch(Throwable t) {
		}
	}

	@Override
	public String get(String key) {
		return props.getProperty(key);
	}

	@Override
	public void put(String key, String value) {
		props.setProperty(key, value);
		store();
	}

	@Override
	public void remove(String key) {
		props.remove(key);
		store();
	}

	@Override
	public Set<String> keySet() {
		return props.stringPropertyNames();
	}

}
