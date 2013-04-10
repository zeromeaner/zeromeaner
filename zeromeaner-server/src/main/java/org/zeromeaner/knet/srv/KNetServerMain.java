package org.zeromeaner.knet.srv;

import java.awt.BorderLayout;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.zeromeaner.gui.common.DocumentOutputStream;

public class KNetServerMain {

	private static final Logger log = Logger.getLogger(KNetServerMain.class);
	
	private static final Options OPTIONS;
	static {
		OPTIONS = new Options();
		OPTIONS.addOption("p", "port", true, "server port");
		OPTIONS.addOption("n", "nogui", false, "don't show a gui");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new KNetServerMain().innerMain(args);
		} catch(Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	protected void innerMain(String[] args) throws Exception {

		CommandLine cli = new PosixParser().parse(OPTIONS, args);

		int port = Integer.parseInt(cli.getOptionValue("port", "" + KNetServer.DEFAULT_PORT));

		if(!cli.hasOption("nogui")) {
			JFrame frame = new JFrame("zeromeaner server on port " + port);
			frame.setLayout(new BorderLayout());
			JTextArea ta = new JTextArea("");
			ta.setEditable(false);
			frame.add(new JScrollPane(ta), BorderLayout.CENTER);
			System.setOut(new PrintStream(new DocumentOutputStream(ta.getDocument())));
			frame.pack();
			frame.setSize(400, 400);
			frame.setVisible(true);
		}
		PropertyConfigurator.configure(getClass().getClassLoader().getResourceAsStream("org/zeromeaner/config/etc/log_server.cfg"));
		
		log.debug("Starting zeromeaner server on port " + port + "...");
		
		new KNetServer(port);
		new KNetCanary(port);
	}

}
