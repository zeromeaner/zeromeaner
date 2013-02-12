package org.zeromeaner.game.knet.obj;

import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.play.GameEngine;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KNetGameInfo implements KryoSerializable {
	public static enum TSpinEnableType {
		DISABLE,
		ENABLE,
		ENABLE_WITH_BONUSES,
	}
	
	private int gravity;
	private int denominator;
	private int are;
	private int areLine;
	private int lineDelay;
	private int lockDelay;
	private int das;
	private boolean b2bEnable;
	private int comboType;
	private TSpinEnableType tspinEnableType;
	private boolean synchronousPlay;
	private Field map;
	
	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(gravity, true);
		output.writeInt(denominator, true);
		output.writeInt(are, true);
		output.writeInt(areLine, true);
		output.writeInt(lineDelay, true);
		output.writeInt(lockDelay, true);
		output.writeInt(das, true);
		output.writeBoolean(b2bEnable);
		output.writeInt(comboType, true);
		output.writeInt(tspinEnableType.ordinal(), true);
		output.writeBoolean(synchronousPlay);
		kryo.writeObjectOrNull(output, map, Field.class);
	}
	
	@Override
	public void read(Kryo kryo, Input input) {
		gravity = input.readInt(true);
		denominator = input.readInt(true);
		are = input.readInt(true);
		areLine = input.readInt(true);
		lineDelay = input.readInt(true);
		lockDelay = input.readInt(true);
		das = input.readInt(true);
		b2bEnable = input.readBoolean();
		comboType = input.readInt(true);
		tspinEnableType = TSpinEnableType.values()[input.readInt(true)];
		synchronousPlay = input.readBoolean();
		map = kryo.readObjectOrNull(input, Field.class);
	}
	
	public int getGravity() {
		return gravity;
	}
	public void setGravity(int gravity) {
		this.gravity = gravity;
	}
	public int getAre() {
		return are;
	}
	public void setAre(int are) {
		this.are = are;
	}
	public int getAreLine() {
		return areLine;
	}
	public void setAreLine(int areLine) {
		this.areLine = areLine;
	}
	public int getLineDelay() {
		return lineDelay;
	}
	public void setLineDelay(int lineDelay) {
		this.lineDelay = lineDelay;
	}
	public int getLockDelay() {
		return lockDelay;
	}
	public void setLockDelay(int lockDelay) {
		this.lockDelay = lockDelay;
	}
	public int getDas() {
		return das;
	}
	public void setDas(int das) {
		this.das = das;
	}
	public boolean isB2bEnable() {
		return b2bEnable;
	}
	public void setB2bEnable(boolean b2bEnable) {
		this.b2bEnable = b2bEnable;
	}
	public int getComboType() {
		return comboType;
	}
	public void setComboType(int comboType) {
		this.comboType = comboType;
	}
	public TSpinEnableType getTspinEnableType() {
		return tspinEnableType;
	}
	public void setTspinEnableType(TSpinEnableType tspinEnableType) {
		this.tspinEnableType = tspinEnableType;
	}
	public boolean isSynchronousPlay() {
		return synchronousPlay;
	}
	public void setSynchronousPlay(boolean synchronousPlay) {
		this.synchronousPlay = synchronousPlay;
	}

	public int getDenominator() {
		return denominator;
	}

	public void setDenominator(int denominator) {
		this.denominator = denominator;
	}

	public Field getMap() {
		return map;
	}

	public void setMap(Field map) {
		this.map = map;
	}
	
}
