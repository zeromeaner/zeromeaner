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
	
	@Override
	public void write(final byte[] b, final int off, final int len) {
		if(EventQueue.isDispatchThread()) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < len; i++)
				sb.append((char) (0xff & b[off + i]));
			text.setText(text.getText() + sb);
		} else
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					write(b, off, len);
				}
			});
	}
	

}
