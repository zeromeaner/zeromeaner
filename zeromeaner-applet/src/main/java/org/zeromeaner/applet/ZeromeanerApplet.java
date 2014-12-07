package org.zeromeaner.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.PropertyConfigurator;
import org.zeromeaner.gui.reskin.StandaloneFrame;
import org.zeromeaner.gui.reskin.StandaloneGameKey;
import org.zeromeaner.gui.reskin.StandaloneMain;
import org.zeromeaner.gui.reskin.StandaloneResourceHolder;
import org.zeromeaner.plaf.ZeroMetalTheme;
import org.zeromeaner.util.EQInvoker;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.PropertyStore;
import org.zeromeaner.util.ResourceInputStream;
import org.zeromeaner.util.Session;

public class ZeromeanerApplet extends JApplet {
	private static ZeromeanerApplet instance;
	public static URL url;

	public static boolean isApplet() {
		return instance != null;
	}
	
	public static ZeromeanerApplet getInstance() {
		return instance;
	}

	private JPanel panel;
	private JPanel cpanel;

	public ZeromeanerApplet() {
	}
	
	@Override
	public void init() {

		Runnable laf = new Runnable() {
			@Override
			public void run() {
				try {
					MetalLookAndFeel.setCurrentTheme(new ZeroMetalTheme());
					UIManager.setLookAndFeel(new MetalLookAndFeel());
				} catch(UnsupportedLookAndFeelException e) {
				}
			}
		};
		
		laf.run();

		
		if(EQInvoker.reinvoke(false, this)) {
			validate();
			repaint();
			return;
		}

		
		setLayout(new BorderLayout());
		panel = new JPanel(new GridBagLayout());
		panel.setOpaque(true);
		cpanel = new JPanel(new BorderLayout());
		panel.add(cpanel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(panel, BorderLayout.CENTER);
		
		instance = this;

		new Thread(new Runnable() {
			@Override
			public void run() {
				startup();
			}
		}).start();;
	}
	
	private void startup() {
		
		url = getDocumentBase();
		if(System.getProperty("zero_url") != null) {
			try {
				url = new URL(System.getProperty("zero_url"));
			} catch(Exception ex) {
			}
		}

		if(Session.ANONYMOUS_USER.equals(Session.getUser()))
			Session.setUser(PropertyStore.get().get("userId"));
		if(Session.getUser() == null)
			Session.setUser(getParameter("userId"));

		if(Session.getUser() == null || Session.ANONYMOUS_USER.equals(Session.getUser())) {
			Session.setUser("none");
			final JOptionPane opt = new JOptionPane("To save user configuration, such as custom keys, you must create a user id.\nThere is no need to remember a password.\nIf you choose not to create a user ID the default settings will be used.\n\nCreate a user ID now?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
			opt.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					cpanel.removeAll();
					if(evt.getNewValue().equals(JOptionPane.YES_OPTION)) {
						final JTextField tf = new JTextField("");
						JPanel p = new JPanel(new GridLayout(0, 1));
						p.add(new JLabel("Enter Config ID"));
						p.add(tf);
						JOptionPane opt2 = new JOptionPane(p, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
						opt2.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY, new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								cpanel.removeAll();
								if(evt.getNewValue().equals(JOptionPane.OK_OPTION)) {
									Session.setUser(tf.getText());
									load();
								} else {
									cpanel.add(opt, BorderLayout.CENTER);
									cpanel.revalidate();
								}
							}
						});
						cpanel.add(opt2, BorderLayout.CENTER);
						cpanel.revalidate();
					} else
						load();
				}
			});
			cpanel.add(opt, BorderLayout.CENTER);
			cpanel.revalidate();
		} else
			load();
	}
	
	private void load() {
		
		if(Session.getUser() == null || "none".equals(Session.getUser()))
			Session.setUser(Session.ANONYMOUS_USER);
	
		panel.removeAll();
		panel.setLayout(new BorderLayout());
		JLabel l = new JLabel("Loading Zeromeaner");
		l.setForeground(Color.WHITE);
		l.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(l, BorderLayout.CENTER);
		panel.revalidate();
		repaint();
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {

					try {
						PropertyConfigurator.configure(new ResourceInputStream("config/etc/log_applet.cfg"));
					} catch(IOException ioe) {
					}

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

					panel.removeAll();
					panel.add(frame.getContentPane(), BorderLayout.CENTER);
					panel.revalidate();

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
		});


	}

	@Override
	public void stop() {
	}

	@Override
	public void destroy() {
		StandaloneMain.saveConfig();
		System.exit(0);
	}
}
