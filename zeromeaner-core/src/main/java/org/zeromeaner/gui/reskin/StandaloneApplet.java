package org.zeromeaner.gui.reskin;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.PropertyConfigurator;
import org.zeromeaner.gui.common.JTextComponentOutputStream;
import org.zeromeaner.plaf.ZeroMetalTheme;
import org.zeromeaner.util.EQInvoker;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.ResourceInputStream;

import static org.zeromeaner.gui.reskin.StandaloneMain.userId;

public class StandaloneApplet extends Applet {
	public static StandaloneApplet instance;
	public static URL url;

	public static boolean isApplet() {
		return instance != null;
	}

	private JTextArea loading = new JTextArea();

	@Override
	public void init() {
		if(EQInvoker.reinvoke(false, this)) {
			JTextComponentOutputStream out = new JTextComponentOutputStream(loading);
			System.setOut(new PrintStream(out));
			return;
		}

		loading.setLineWrap(true);
		loading.setWrapStyleWord(true);
		
		setLayout(new BorderLayout());
		add(new JScrollPane(loading), BorderLayout.CENTER);
		loading.revalidate();
		validate();
		repaint();


		instance = this;

		new Thread(new Runnable() {
			@Override
			public void run() {
				startup();
			}
		}).start();;
	}
	
	private void startup() {
		
		CookieAccess.setInstance(new CookieAccess() {
			@Override
			public Map<String, String> get() {
				return get(StandaloneApplet.this);
			}
		});

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
			}

			MetalLookAndFeel.setCurrentTheme(new ZeroMetalTheme());
			UIManager.setLookAndFeel(new MetalLookAndFeel());

			StandaloneMain.loadGlobalConfig();

			StandaloneMain.modeManager = ModeList.getModes();

			StandaloneGameKey.initGlobalGameKeySwing();
			StandaloneGameKey.gamekey[0].loadDefaultKeymap();
			StandaloneGameKey.gamekey[1].loadDefaultKeymap();

			StandaloneGameKey.gamekey[0].loadConfig(Options.GUI_PROPERTIES);
			StandaloneGameKey.gamekey[1].loadConfig(Options.GUI_PROPERTIES);

			StandaloneResourceHolder.load();

			final StandaloneFrame frame = new StandaloneFrame();
			frame.setUndecorated(false);

			removeAll();
			add(frame.getRootPane(), BorderLayout.CENTER);
			validate();

			if(url.getQuery() != null) {
				String[] qf = url.getQuery().split("&");
				for(String qpp : qf) {
					final String[] qp = qpp.split("=", 2);
					if("replay".equals(qp[0]) && qp.length > 1) {
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								frame.startReplayGame(qp[1]);
							}
						});
						break;
					}

				}
			}

		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void stop() {
		StandaloneMain.saveConfig();
		System.exit(0);
	}

	@Override
	public void destroy() {
		StandaloneMain.saveConfig();
		System.exit(0);
	}
}
