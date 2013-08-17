package org.zeromeaner.mq;

public interface ObjectListener {
	public void objectReceived(Message message, Object value);
}
