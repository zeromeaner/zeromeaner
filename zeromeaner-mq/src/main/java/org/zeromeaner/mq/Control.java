package org.zeromeaner.mq;

public class Control {
	public static enum Command {
		SUBSCRIBE,
		UNSUBSCRIBE,
		PERSONAL_TOPIC,
		PRIVILEGED_SUBSCRIBE,
		PRIVILEGED_SET_ORIGIN,
	}
	
	public Command command;
	public String topic;
	
	public Control() {}
	
	public Control(Command command, String topic) {
		this.command = command;
		this.topic = topic;
	}
}
