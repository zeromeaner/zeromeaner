package org.zeromeaner.knet.srv;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

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
			throw new RuntimeException(ex);
		}
	}
	
	protected void innerMain(String[] args) throws Exception {
		CommandLine cli = new PosixParser().parse(OPTIONS, args);
		
		int port = Integer.parseInt(cli.getOptionValue("port", "" + KNetServer.DEFAULT_PORT));
		
		KNetServer server = new KNetServer(port);
	}

}
