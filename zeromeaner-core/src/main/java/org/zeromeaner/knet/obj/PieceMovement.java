package org.zeromeaner.knet.obj;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class PieceMovement implements KryoSerializable {
	private int pieceId;
	private int x;
	private int y;
	private int bottomY;
	private int direction;
	private int color;
	private int skin;
	private boolean big;
	
	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(pieceId, true);
		output.writeInt(x, true);
		output.writeInt(y, true);
		output.writeInt(bottomY, true);
		output.writeInt(direction, true);
		output.writeInt(color, true);
		output.writeInt(skin, true);
		output.writeBoolean(big);
	}
	@Override
	public void read(Kryo kryo, Input input) {
		pieceId = input.readInt(true);
		x = input.readInt(true);
		y = input.readInt(true);
		bottomY = input.readInt(true);
		direction = input.readInt(true);
		color = input.readInt(true);
		skin = input.readInt(true);
		big = input.readBoolean();
	}
	
	public int getPieceId() {
		return pieceId;
	}
	public void setPieceId(int pieceId) {
		this.pieceId = pieceId;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public int getColor() {
		return color;
	}
	public void setColor(int color) {
		this.color = color;
	}
	public int getSkin() {
		return skin;
	}
	public void setSkin(int skin) {
		this.skin = skin;
	}
	public boolean isBig() {
		return big;
	}
	public void setBig(boolean big) {
		this.big = big;
	}
	public int getBottomY() {
		return bottomY;
	}
	public void setBottomY(int bottomY) {
		this.bottomY = bottomY;
	}
}
