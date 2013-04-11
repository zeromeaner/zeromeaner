package org.zeromeaner.gui.common;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.zeromeaner.util.EQInvoker;

public class DocumentOutputStream extends OutputStream {
	private Document doc;
	
	public DocumentOutputStream(Document doc) {
		this.doc = doc;
	}
	
	@Override
	public void write(int b) throws IOException {
		if(EQInvoker.reinvoke(false, this, b))
			return;
		try {
			doc.insertString(doc.getLength(), String.valueOf((char) (b & 0xff)), null);
		} catch(BadLocationException ble) {
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if(EQInvoker.reinvoke(false, this, b, off, len))
			return;
		StringBuilder sb = new StringBuilder();
		for(int i = off; i < off + len; i++) {
			int c = b[i];
			sb.append((char)(c & 0xff));
		}
		try {
			doc.insertString(doc.getLength(), sb.toString(), null);
		} catch(BadLocationException ble) {
		}
	}
}
