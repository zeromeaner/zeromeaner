package org.zeromeaner.util;

import javax.swing.text.JTextComponent;

public class SwingUtils {
	public static int getIntTextField(int defaultValue, JTextComponent c) {
		try {
			return Integer.parseInt(c.getText());
		} catch(RuntimeException re) {
			return defaultValue;
		}
	}
	
	public static double getDoubleTextField(double defaultValue, JTextComponent c) {
		try {
			return Double.parseDouble(c.getText());
		} catch(RuntimeException re) {
			return defaultValue;
		}
	}
}
