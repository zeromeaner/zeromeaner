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
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.PropertyConstant;
import org.zeromeaner.util.ResourceInputStream;
import org.zeromeaner.util.ResourceOutputStream;
import org.zeromeaner.util.ResourceInputStream.ResourceDownloadStream;

public class StandaloneMain {
	public static ModeList<GameMode> modeManager;
	public static String userId;
	
	public static void main(String[] args) {
		try {
			_main(args);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void loadGlobalConfig() {
		try {
			InputStream in = new ResourceInputStream("config/setting/global.cfg");
			Options.GLOBAL_PROPERTIES.load(in);
			in.close();
		} catch(FileNotFoundException fnfe) {
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
		try {
			InputStream in = new ResourceInputStream("config/setting/swing.cfg");
			Options.GUI_PROPERTIES.load(in);
			in.close();
		} catch(FileNotFoundException fnfe) {
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
		Options.RUNTIME_PROPERTIES.putAll(CookieAccess.getInstance().get());
	}

	public static void saveConfig() {
		try {
			ResourceOutputStream out = new ResourceOutputStream("config/setting/global.cfg");
			Options.GLOBAL_PROPERTIES.store(out, "zeromeaner global Config");
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		try {
			ResourceOutputStream out = new ResourceOutputStream("config/setting/swing.cfg");
			Options.GUI_PROPERTIES.store(out, "zeromeaner Swing-frontend Config");
			out.close();
		} catch(IOException e) {
		}
		CookieAccess.getInstance().set(Options.RUNTIME_PROPERTIES.getAll());
		try {
			ResourceDownloadStream.commitCache();
		} catch(IOException ioe) {
		}
	}

	private static void _main(String[] args) throws Exception {
		System.setProperty("user.dir", System.getProperty("user.home") + File.separator + ".nettromino");
		new File(System.getProperty("user.dir")).mkdirs();
		CookieAccess.setInstance(new MainCookieAccess());

		StandaloneApplet.url = new URL("http://www.nettromino.org/" + (GameManager.VERSION.isSnapshot() ? "snapshot" : "play") + "/");

		userId = System.getProperty("user.name");
		if(CookieAccess.get("userId") != null)
			userId = CookieAccess.get("userId");

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
		
		StandaloneGameKey.gamekey[0].loadConfig(Options.GUI_PROPERTIES);
		StandaloneGameKey.gamekey[1].loadConfig(Options.GUI_PROPERTIES);
		
		StandaloneResourceHolder.load();
		
		StandaloneFrame frame = new StandaloneFrame();
		
		if(PropertyConstant.is(Options.standalone().FULL_SCREEN)) {
			frame.setUndecorated(true);
			frame.setVisible(true);
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			frame.setSize(1366, 768);
			frame.setVisible(true);
		}
	}
}
