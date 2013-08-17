package org.zeromeaner.knet.ser;

import org.zeromeaner.game.component.Statistics;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.Serializer;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

public class StatisticsSerializer extends Serializer<Statistics> {

	@Override
	public void write(Kryo kryo, Output output, Statistics object) {
		output.writeInt(object.level, true);
		output.writeInt(object.levelDispAdd, true);
		output.writeInt(object.lines, true);
		output.writeInt(object.maxChain, true);
		output.writeInt(object.maxCombo, true);
		output.writeInt(object.rollclear, true);
		output.writeInt(object.score, true);
		output.writeInt(object.scoreFromHardDrop, true);
		output.writeInt(object.scoreFromLineClear, true);
		output.writeInt(object.scoreFromOtherBonus, true);
		output.writeInt(object.scoreFromSoftDrop, true);
		output.writeInt(object.time, true);
		output.writeInt(object.totalB2BFour, true);
		output.writeInt(object.totalB2BTSpin, true);
		output.writeInt(object.totalDouble, true);
		output.writeInt(object.totalFour, true);
		output.writeInt(object.totalHoldUsed, true);
		output.writeInt(object.totalPieceActiveTime, true);
		output.writeInt(object.totalPieceLocked, true);
		output.writeInt(object.totalPieceMove, true);
		output.writeInt(object.totalPieceRotate, true);
		output.writeInt(object.totalSingle, true);
		output.writeInt(object.totalTriple, true);
		output.writeInt(object.totalTSpinDouble, true);
		output.writeInt(object.totalTSpinDoubleMini, true);
		output.writeInt(object.totalTSpinSingle, true);
		output.writeInt(object.totalTSpinSingleMini, true);
		output.writeInt(object.totalTSpinTriple, true);
		output.writeInt(object.totalTSpinZero, true);
		output.writeInt(object.totalTSpinZeroMini, true);
		output.writeFloat(object.gamerate);
		output.writeFloat(object.lpm);
		output.writeFloat(object.lps);
		output.writeFloat(object.ppm);
		output.writeDouble(object.spl);
		output.writeDouble(object.spm);
		output.writeDouble(object.sps);
	}

	@Override
	public Statistics read(Kryo kryo, Input input, Class<Statistics> type) {
		Statistics object = kryo.newInstance(type);
		
		object.level = input.readInt(true);
		object.levelDispAdd = input.readInt(true);
		object.lines = input.readInt(true);
		object.maxChain = input.readInt(true);
		object.maxCombo = input.readInt(true);
		object.rollclear = input.readInt(true);
		object.score = input.readInt(true);
		object.scoreFromHardDrop = input.readInt(true);
		object.scoreFromLineClear = input.readInt(true);
		object.scoreFromOtherBonus = input.readInt(true);
		object.scoreFromSoftDrop = input.readInt(true);
		object.time = input.readInt(true);
		object.totalB2BFour = input.readInt(true);
		object.totalB2BTSpin = input.readInt(true);
		object.totalDouble = input.readInt(true);
		object.totalFour = input.readInt(true);
		object.totalHoldUsed = input.readInt(true);
		object.totalPieceActiveTime = input.readInt(true);
		object.totalPieceLocked = input.readInt(true);
		object.totalPieceMove = input.readInt(true);
		object.totalPieceRotate = input.readInt(true);
		object.totalSingle = input.readInt(true);
		object.totalTriple = input.readInt(true);
		object.totalTSpinDouble = input.readInt(true);
		object.totalTSpinDoubleMini = input.readInt(true);
		object.totalTSpinSingle = input.readInt(true);
		object.totalTSpinSingleMini = input.readInt(true);
		object.totalTSpinTriple = input.readInt(true);
		object.totalTSpinZero = input.readInt(true);
		object.totalTSpinZeroMini = input.readInt(true);
		
		object.gamerate = input.readFloat();
		object.lpm = input.readFloat();
		object.lps = input.readFloat();
		object.ppm = input.readFloat();
		object.spl = input.readDouble();
		object.spm = input.readDouble();
		object.sps = input.readDouble();
		
		return object;
	}

}
