package org.zeromeaner.gui.reskin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.apache.log4j.PropertyConfigurator;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.plaf.ZeroMetalTheme;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.ResourceInputStream;
import org.zeromeaner.util.ResourceOutputStream;
import org.zeromeaner.util.ResourceInputStream.ResourceDownloadStream;

public class StandaloneMain {
	public static CustomProperties propConfig = new CustomProperties();
	public static ModeList<GameMode> modeManager;
	public static String userId;
	
	public static boolean offline = false;
	
	public static void main(String[] args) {
		try {
			_main(args);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void loadGlobalConfig() {
		try {
			InputStream in = new ResourceInputStream("config/setting/swing.cfg");
			propConfig.load(in);
			in.close();
		} catch(FileNotFoundException fnfe) {
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public static void saveConfig() {
		try {
			ResourceOutputStream out = new ResourceOutputStream("config/setting/swing.cfg");
			propConfig.store(out, "zeromeaner Swing-frontend Config");
			out.close();
		} catch(IOException e) {
		}
		try {
			ResourceDownloadStream.commitCache();
		} catch(IOException ioe) {
		}
	}

	private static void _main(String[] args) throws Exception {
		System.setProperty("user.dir", System.getProperty("user.home") + File.separator + ".0mino");
		new File(System.getProperty("user.dir")).mkdirs();
		CookieAccess.setInstance(new MainCookieAccess());

		StandaloneApplet.url = new URL("http://www.0mino.org/" + (GameManager.VERSION.isSnapshot() ? "snapshot" : "play") + "/");

		userId = System.getProperty("user.name");
		if(CookieAccess.get("userId") != null)
			userId = CookieAccess.get("userId");

		offline = Boolean.parseBoolean(System.getProperty("offline"));
		
		try {
			PropertyConfigurator.configure(new ResourceInputStream("config/etc/log_applet.cfg"));
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		MetalLookAndFeel.setCurrentTheme(new ZeroMetalTheme());
		UIManager.setLookAndFeel(new MetalLookAndFeel());
		
		loadGlobalConfig();
		
		modeManager = ModeList.getModes();
		
		StandaloneGameKey.initGlobalGameKeySwing();
		StandaloneGameKey.gamekey[0].loadDefaultKeymap();
		StandaloneGameKey.gamekey[1].loadDefaultKeymap();
		
		StandaloneGameKey.gamekey[0].loadConfig(propConfig);
		StandaloneGameKey.gamekey[1].loadConfig(propConfig);
		
		StandaloneResourceHolder.load();
		
		StandaloneFrame frame = new StandaloneFrame();
		
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
}
