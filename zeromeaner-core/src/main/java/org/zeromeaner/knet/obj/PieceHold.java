package org.zeromeaner.knet.obj;

import org.zeromeaner.game.component.Piece;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoSerializable;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

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
