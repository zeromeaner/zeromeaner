package org.zeromeaner.knet.ser;

import org.zeromeaner.game.component.Block;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class BlockSerializer extends Serializer<Block> {

	@Override
	public void write(Kryo kryo, Output output, Block b) {
		output.writeInt(b.color, true);
		output.writeInt(b.skin, true);
		output.writeInt(b.attribute, true);
		output.writeInt(b.elapsedFrames, true);
		output.writeFloat(b.darkness);
		output.writeFloat(b.alpha);
		output.writeInt(b.pieceNum, true);
		output.writeInt(b.item, true);
		output.writeInt(b.hard, true);
		output.writeInt(b.countdown, true);
		output.writeInt(b.secondaryColor, true);
		output.writeInt(b.bonusValue, true);
	}

	@Override
	public Block read(Kryo kryo, Input input, Class<Block> type) {
		Block b = new Block();
		
		b.color = input.readInt(true);
		b.skin = input.readInt(true);
		b.attribute = input.readInt(true);
		b.elapsedFrames = input.readInt(true);
		b.darkness = input.readFloat();
		b.alpha = input.readFloat();
		b.pieceNum = input.readInt(true);
		b.item = input.readInt(true);
		b.hard = input.readInt(true);
		b.countdown = input.readInt(true);
		b.secondaryColor = input.readInt(true);
		b.bonusValue = input.readInt(true);
		
		return b;
	}

}
