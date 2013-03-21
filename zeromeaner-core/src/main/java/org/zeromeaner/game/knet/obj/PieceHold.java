package org.zeromeaner.game.knet.obj;

import org.zeromeaner.game.component.Piece;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class PieceHold implements KryoSerializable {
	private Piece piece;
	private boolean disableHold;
	
	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObjectOrNull(output, piece, Piece.class);
		output.writeBoolean(disableHold);
	}
	
	@Override
	public void read(Kryo kryo, Input input) {
		piece = kryo.readObjectOrNull(input, Piece.class);
		disableHold = input.readBoolean();
	}
	
	public Piece getPiece() {
		return piece;
	}
	public void setPiece(Piece piece) {
		this.piece = piece;
	}
	public boolean isDisableHold() {
		return disableHold;
	}
	public void setDisableHold(boolean disableHold) {
		this.disableHold = disableHold;
	}
	
	
}
