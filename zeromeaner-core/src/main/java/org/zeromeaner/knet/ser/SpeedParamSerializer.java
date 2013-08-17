package org.zeromeaner.knet.ser;

import org.zeromeaner.game.component.SpeedParam;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.Serializer;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

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
