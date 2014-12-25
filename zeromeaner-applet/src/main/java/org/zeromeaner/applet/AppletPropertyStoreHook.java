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

import org.apache.commons.codec.binary.Base64;
import org.zeromeaner.util.io.PropertyStoreHook;

public class AppletPropertyStoreHook implements PropertyStoreHook {

	protected Properties props;

	public AppletPropertyStoreHook() throws IOException{
		props = new Properties();
		try {
			String data = "";
			JSObject myBrowser = JSObject.getWindow(ZeromeanerApplet.getInstance());
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
			byte[] buf = Base64.decodeBase64(data);
			props = (Properties) new ObjectInputStream(new ByteArrayInputStream(buf)).readObject();
		} catch(Throwable t) {
		}
	}

	protected void store() {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(props);
			out.close();

			String value = Base64.encodeBase64String(bout.toByteArray());
			JSObject win = JSObject.getWindow(ZeromeanerApplet.getInstance());
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
