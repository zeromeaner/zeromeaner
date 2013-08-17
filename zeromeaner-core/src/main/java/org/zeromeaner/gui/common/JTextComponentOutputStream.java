package org.zeromeaner.gui.common;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.text.JTextComponent;

public class JTextComponentOutputStream extends OutputStream {
	private JTextComponent text;
	
	public JTextComponentOutputStream(JTextComponent text) {
		this.text = text;
	}
	
	@Override
	public void write(final int b) {
		if(EventQueue.isDispatchThread())
			text.setText(text.getText() + (char) (0xff & b));
		else
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					write(b);
				}
			});
	}

}
