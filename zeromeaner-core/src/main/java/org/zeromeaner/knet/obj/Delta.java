package org.zeromeaner.knet.obj;

import java.io.ByteArrayOutputStream;

import org.badiff.ByteArrayDiffs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Delta<T> implements KryoSerializable {
	protected byte[] diff;
	
	public Delta() {}
	
	public Delta(Kryo kryo, T previous, T current) {
		ByteArrayOutputStream pbytes = new ByteArrayOutputStream();
		ByteArrayOutputStream cbytes = new ByteArrayOutputStream();
		
		Output output = new Output(pbytes);
		kryo.writeClassAndObject(output, previous);
		output.flush();
		
		output = new Output(cbytes);
		kryo.writeClassAndObject(output, current);
		output.flush();
		
		diff = ByteArrayDiffs.udiff(pbytes.toByteArray(), cbytes.toByteArray());
	}
	
	public T getCurrent(Kryo kryo, T previous) {
		ByteArrayOutputStream pbytes = new ByteArrayOutputStream();
		
		Output output = new Output(pbytes);
		kryo.writeClassAndObject(output, previous);
		output.flush();
		
		Input input = new Input(ByteArrayDiffs.apply(pbytes.toByteArray(), diff));
		return (T) kryo.readClassAndObject(input);
	}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, diff);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		diff = kryo.readObject(input, byte[].class);
	}
}
