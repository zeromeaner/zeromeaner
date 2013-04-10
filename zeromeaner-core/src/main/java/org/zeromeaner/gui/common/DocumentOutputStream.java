package org.zeromeaner.gui.common;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class DocumentOutputStream extends OutputStream {
	private Document doc;
	
	public DocumentOutputStream(Document doc) {
		this.doc = doc;
	}
	
	@Override
	public void write(int b) throws IOException {
		try {
			doc.insertString(doc.getLength(), String.valueOf((char) (b & 0xff)), null);
		} catch(BadLocationException ble) {
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		StringBuilder sb = new StringBuilder();
		for(int i : b) {
			sb.append((char)(i & 0xff));
		}
		try {
			doc.insertString(doc.getLength(), sb.toString(), null);
		} catch(BadLocationException ble) {
		}
	}
}
