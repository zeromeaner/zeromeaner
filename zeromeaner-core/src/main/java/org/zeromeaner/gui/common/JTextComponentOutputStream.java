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
		if(EventQueue.isDispatchThread()) {
			String t = text.getText() + (char) (0xff & b);
			if(t.indexOf('\n') != t.lastIndexOf('\n'))
				t = t.substring(t.indexOf('\n') + 1);
			text.setText(t);
			text.repaint();
		}
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
			String t = text.getText() + sb;
			if(t.indexOf('\n') != t.lastIndexOf('\n'))
				t = t.substring(t.indexOf('\n') + 1);
			text.setText(t);
			text.repaint();
		} else
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					write(b, off, len);
				}
			});
	}
	

}
