package org.zeromeaner.util;

import java.io.ByteArrayOutputStream;

import org.zeromeaner.knet.KNetKryo;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

public class KryoCopy {
	public static <E> void overwrite(E src, final E dest) {
		Kryo sk = new Kryo();
		KNetKryo.configure(sk);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Output o = new Output(bout);
		sk.writeClassAndObject(o, src);
		o.close();
		
		Kryo dk = new Kryo() {
			private E target = dest;
			@Override
			public <T> T newInstance(Class<T> type) {
				if(target != null) {
					T ret = type.cast(target);
					target = null;
					return ret;
				}
				return super.newInstance(type);
			}
		};
		KNetKryo.configure(dk);
		dk.readClassAndObject(new Input(bout.toByteArray()));
	}
}
