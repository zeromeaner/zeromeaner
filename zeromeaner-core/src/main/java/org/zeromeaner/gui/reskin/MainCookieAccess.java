package org.zeromeaner.gui.reskin;

import java.applet.Applet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.zeromeaner.gui.applet.AppletMain;

public class MainCookieAccess extends CookieAccess {
	private File prefs = new File(System.getProperty("user.dir"), "0mino.properties");

	@Override
	protected Map<String, String> get(Applet applet) {
		try {
			InputStream in = new FileInputStream(prefs);
			Properties p = new Properties();
			try {
				p.load(in);
			} finally {
				in.close();
			}
			return new TreeMap<String, String>((Map) p);
		} catch(Throwable t) {
			return new TreeMap<String, String>();
		}
	}

	@Override
	protected void set(AppletMain applet, Map<String, String> cookie) {
		try {
			Properties p = new Properties();
			for(Map.Entry<String, String> e : cookie.entrySet()) {
				p.setProperty(e.getKey(), e.getValue());
			}
			OutputStream out = new FileOutputStream(prefs);
			try {
				p.store(out, "0mino config");
			} finally {
				out.close();
			}
		} catch(Throwable t) {
		}
	}
}