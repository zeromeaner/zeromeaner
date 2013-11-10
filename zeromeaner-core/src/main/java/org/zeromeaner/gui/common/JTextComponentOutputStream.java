package org.zeromeaner.gui.common;

import java.awt.EventQueue;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.text.JTextComponent;

public class JTextComponentOutputStream extends OutputStream {
	private JTextComponent text;
	private boolean oneline;
	
	public JTextComponentOutputStream(JTextComponent text, boolean oneline) {
		this.text = text;
		this.oneline = oneline;
	}
	
	@Override
	public void write(final int b) {
		if(EventQueue.isDispatchThread()) {
			String t = text.getText() + (char) (0xff & b);
			if(oneline && t.indexOf('\n') != t.lastIndexOf('\n'))
				t = t.substring(t.indexOf('\n') + 1);
			text.setText(t);
			text.repaint();
		} else
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						write(b);
					}
				});
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
	}
	
	@Override
	public void write(final byte[] b, final int off, final int len) {
		if(EventQueue.isDispatchThread()) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < len; i++)
				sb.append((char) (0xff & b[off + i]));
			String t = text.getText() + sb;
			if(oneline && t.indexOf('\n') != t.lastIndexOf('\n'))
				t = t.substring(t.indexOf('\n') + 1);
			text.setText(t);
			text.repaint();
		} else
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						write(b, off, len);
					}
				});
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
	}
	

}
