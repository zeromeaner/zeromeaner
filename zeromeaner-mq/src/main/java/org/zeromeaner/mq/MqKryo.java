package org.zeromeaner.mq;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

public class MqKryo extends Kryo {
	public MqKryo() {
		setReferences(false);
		setAutoReset(true);
		setRegistrationRequired(true);
		register(byte[].class);
		register(Message.class, new FieldSerializer<>(this, Message.class));
		register(Control.class, new FieldSerializer<>(this, Control.class));
		register(Control.Command.class);
	}
}
