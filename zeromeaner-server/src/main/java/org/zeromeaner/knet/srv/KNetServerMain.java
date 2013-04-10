package org.zeromeaner.knet.srv;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.PropertyConfigurator;

public class KNetServerMain {

	private static final Options OPTIONS;
	static {
		OPTIONS = new Options();
		OPTIONS.addOption("p", "port", true, "server port");
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
		PropertyConfigurator.configure(getClass().getClassLoader().getResourceAsStream("org/zeromeaner/config/etc/log_server.cfg"));
		
		CommandLine cli = new PosixParser().parse(OPTIONS, args);
		
		int port = Integer.parseInt(cli.getOptionValue("port", "" + KNetServer.DEFAULT_PORT));
		
		new KNetServer(port);
		new KNetCanary(port);
	}

}
