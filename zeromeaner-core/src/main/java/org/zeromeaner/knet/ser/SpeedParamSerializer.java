package org.zeromeaner.knet.ser;

import org.zeromeaner.game.component.SpeedParam;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SpeedParamSerializer extends Serializer<SpeedParam> {

	@Override
	public void write(Kryo kryo, Output output, SpeedParam object) {
		output.writeInt(object.are, true);
		output.writeInt(object.areLine, true);
		output.writeInt(object.das, true);
		output.writeInt(object.denominator, true);
		output.writeInt(object.gravity, true);
		output.writeInt(object.lineDelay, true);
		output.writeInt(object.lockDelay, true);
	}

	@Override
	public SpeedParam read(Kryo kryo, Input input, Class<SpeedParam> type) {
		SpeedParam sp = kryo.newInstance(type);
		sp.are = input.readInt(true);
		sp.areLine = input.readInt(true);
		sp.das = input.readInt(true);
		sp.denominator = input.readInt(true);
		sp.gravity = input.readInt(true);
		sp.lineDelay = input.readInt(true);
		sp.lockDelay = input.readInt(true);
		return sp;
	}

}
