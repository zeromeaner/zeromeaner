package org.zeromeaner.knet.ser;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.Callable;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.InputChunked;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.OutputChunked;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.util.ObjectMap;

public class DiffFieldSerializer<T> extends FieldSerializer<T> {
	protected Callable<Kryo> kryoFactory;
	protected T typical;
	
	public DiffFieldSerializer(Kryo kryo, Class<T> type, T typical, Callable<Kryo> kryoFactory) {
		super(kryo, type);
		this.typical = typical;
		this.kryoFactory = kryoFactory;
	}

	@Override
	public void write(Kryo kryo, Output output, T object) {
		CachedField[] fields = getFields();
		ObjectMap context = kryo.getContext();
		byte[][] typicalFields;
		
		if(!context.containsKey(this)) {
			typicalFields = new byte[fields.length][];
			for(int i = 0; i < fields.length; i++) {
				try {
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					Output kout = new Output(bout, 1024);
					kryoFactory.call().writeClassAndObject(kout, fields[i].getField().get(typical));
					kout.flush();
					typicalFields[i] = bout.toByteArray();
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			context.put(this, typicalFields);
		} else
			typicalFields = (byte[][]) context.get(this);
		
		for(int i = 0; i < fields.length; i++) {
			Field f = fields[i].getField();
			byte[] objf = null;
			try {
				Object v = f.get(object);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				Output kout = new Output(bout, 1024);
				kryoFactory.call().writeClassAndObject(kout, v);
				kout.flush();
				objf = bout.toByteArray();
			} catch(Exception ex) {
			}
			if(!Arrays.equals(typicalFields[i], objf)) {
				output.writeInt(i, true);
				fields[i].write(output, object);
			}
		}
		output.writeInt(-1, true);
	}

	@Override
	public T read(Kryo kryo, Input input, Class<T> type) {
		CachedField[] fields = getFields();
		
		T object = kryo.newInstance(type);
		
		for(int i = 0; i < fields.length; i++) {
			fields[i].copy(typical, object);
		}
		
		for(int i = input.readInt(true); i != -1; i = input.readInt(true)) {
			CachedField f = fields[i];
			f.read(input, object);
		}
		return object;
	}

}
