package org.zeromeaner.gui.reskin;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.PropertyConfigurator;
import org.zeromeaner.gui.applet.AppletMain;
import org.zeromeaner.plaf.ZeroMetalTheme;
import org.zeromeaner.util.EQInvoker;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.ResourceInputStream;

import static org.zeromeaner.gui.applet.AppletMain.url;
import static org.zeromeaner.gui.applet.AppletMain.userId;

public class StandaloneApplet extends Applet {
	public static StandaloneApplet instance;
	
	@Override
	public void init() {
		if(EQInvoker.reinvoke(false, this))
			return;
		
		instance = this;
		
		CookieAccess.setInstance(new CookieAccess() {
			@Override
			protected Map<String, String> get() {
				return get(StandaloneApplet.this);
			}
		});
		
		// FIXME: Satisfy some old applet code
		AppletMain.instance = new AppletMain();
		
		url = getDocumentBase();
		if(System.getProperty("zero_url") != null) {
			try {
				url = new URL(System.getProperty("zero_url"));
			} catch(Exception ex) {
			}
		}

		if(userId == null)
			userId = CookieAccess.get("userId");
		if(userId == null)
			userId = getParameter("userId");
	
		while(userId == null || "default".equals(userId)) {
			userId = "none";
			int create = JOptionPane.showConfirmDialog(this, "To save user configuration, such as custom keys, you must create a user id.\nThere is no need to remember a password.\nIf you choose not to create a user ID the default settings will be used.\n\nCreate a user ID now?", "Create User ID?", JOptionPane.YES_NO_OPTION);
			if(create == JOptionPane.YES_OPTION) {
				userId = (String) JOptionPane.showInputDialog(this, "Enter Config ID", "Enter Config ID", JOptionPane.QUESTION_MESSAGE, null, null, "");
				if(userId != null)
					CookieAccess.put("userId", userId);
				else
					userId = "default";
			}
		}
	
		try {
			try {
				PropertyConfigurator.configure(new ResourceInputStream("config/etc/log_applet.cfg"));
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
			MetalLookAndFeel.setCurrentTheme(new ZeroMetalTheme());
			UIManager.setLookAndFeel(new MetalLookAndFeel());
			
			StandaloneMain.loadGlobalConfig();
			
			StandaloneMain.modeManager = ModeList.getModes();
			
			StandaloneGameKey.initGlobalGameKeySwing();
			StandaloneGameKey.gamekey[0].loadDefaultKeymap();
			StandaloneGameKey.gamekey[1].loadDefaultKeymap();
			
			StandaloneResourceHolder.load();
			
			StandaloneFrame frame = new StandaloneFrame();
			frame.setUndecorated(false);
			
			setLayout(new BorderLayout());
			add(frame.getRootPane(), BorderLayout.CENTER);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void destroy() {
		StandaloneMain.saveConfig();
	}
}
