package org.zeromeaner.knet.ser;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Piece;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class PieceSerializer extends Serializer<Piece> {

	@Override
	public void write(Kryo kryo, Output output, Piece p) {
		kryo.writeObject(output, p.dataX);
		kryo.writeObject(output, p.dataY);
		kryo.writeObject(output, p.block);
		output.writeInt(p.id, true);
		output.writeInt(p.direction, true);
		output.writeBoolean(p.big);
		output.writeBoolean(p.offsetApplied);
		kryo.writeObject(output, p.dataOffsetX);
		kryo.writeObject(output, p.dataOffsetY);
		output.writeBoolean(p.connectBlocks);
	}

	@Override
	public Piece read(Kryo kryo, Input input, Class<Piece> type) {
		Piece p = new Piece();
		
		p.dataX = kryo.readObject(input, int[][].class);
		p.dataY = kryo.readObject(input, int[][].class);
		p.block = kryo.readObject(input, Block[].class);
		p.id = input.readInt(true);
		p.direction = input.readInt(true);
		p.big = input.readBoolean();
		p.offsetApplied = input.readBoolean();
		p.dataOffsetX = kryo.readObject(input, int[].class);
		p.dataOffsetY = kryo.readObject(input, int[].class);
		p.connectBlocks = input.readBoolean();
		
		return p;
	}

}
