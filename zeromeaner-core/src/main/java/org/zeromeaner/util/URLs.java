package org.zeromeaner.util;

import java.awt.Desktop;
import java.net.URL;

public class URLs {

	public static void open(URL url) {
		try {
			if(Desktop.isDesktopSupported())
				Desktop.getDesktop().browse(url.toURI());
			else
				Runtime.getRuntime().exec("xdg-open " + url);
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}
	
	private URLs() {
		// TODO Auto-generated constructor stub
	}

}
