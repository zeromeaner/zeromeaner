package org.zeromeaner.knet.obj;

import org.zeromeaner.game.component.Field;

public class KNetGameInfo {
	public static enum TSpinEnableType {
		DISABLE,
		ENABLE,
		ENABLE_WITH_BONUSES,
	}
	
	private int gravity = 4;
	private int denominator = 256;
	private int are = 0;
	private int areLine = 0;
	private int lineDelay = 0;
	private int lockDelay = 30;
	private int das = 14;
	private boolean b2bEnable;
	private int comboType;
	private TSpinEnableType tspinEnableType = TSpinEnableType.ENABLE;
	private boolean synchronousPlay;
	private Field map;
	private boolean reduceLineSend;
	private boolean useFractionalGarbage;
	private boolean targettedGarbage;
	private boolean rensaBlock;
	private int garbagePercent = 100;
	private boolean divideChangeRateByPlayers;
	private boolean garbageChangePerAttack;
	private int hurryupSeconds;
	private int hurryupInterval;
	private int targetTimer;
	private boolean b2bChunk;
	private boolean counterGarbage;
	
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

	public boolean isReduceLineSend() {
		return reduceLineSend;
	}

	public void setReduceLineSend(boolean reduceLineSend) {
		this.reduceLineSend = reduceLineSend;
	}

	public boolean isUseFractionalGarbage() {
		return useFractionalGarbage;
	}

	public void setUseFractionalGarbage(boolean useFractionalGarbage) {
		this.useFractionalGarbage = useFractionalGarbage;
	}

	public boolean isTargettedGarbage() {
		return targettedGarbage;
	}

	public void setTargettedGarbage(boolean targettedGarbage) {
		this.targettedGarbage = targettedGarbage;
	}
	public boolean isRensaBlock() {
		return rensaBlock;
	}
	public void setRensaBlock(boolean rensaBlock) {
		this.rensaBlock = rensaBlock;
	}
	public int getGarbagePercent() {
		return garbagePercent;
	}
	public void setGarbagePercent(int garbagePercent) {
		this.garbagePercent = garbagePercent;
	}
	public boolean isDivideChangeRateByPlayers() {
		return divideChangeRateByPlayers;
	}
	public void setDivideChangeRateByPlayers(boolean divideChangeRateByPlayers) {
		this.divideChangeRateByPlayers = divideChangeRateByPlayers;
	}
	public boolean isGarbageChangePerAttack() {
		return garbageChangePerAttack;
	}
	public void setGarbageChangePerAttack(boolean garbageChangePerAttack) {
		this.garbageChangePerAttack = garbageChangePerAttack;
	}
	public int getHurryupSeconds() {
		return hurryupSeconds;
	}
	public void setHurryupSeconds(int hurryupSeconds) {
		this.hurryupSeconds = hurryupSeconds;
	}
	public int getHurryupInterval() {
		return hurryupInterval;
	}
	public void setHurryupInterval(int hurryupInterval) {
		this.hurryupInterval = hurryupInterval;
	}
	public int getTargetTimer() {
		return targetTimer;
	}
	public void setTargetTimer(int targetTimer) {
		this.targetTimer = targetTimer;
	}
	public boolean isB2bChunk() {
		return b2bChunk;
	}
	public void setB2bChunk(boolean b2bChunk) {
		this.b2bChunk = b2bChunk;
	}
	public boolean isCounterGarbage() {
		return counterGarbage;
	}
	public void setCounterGarbage(boolean counterGarbage) {
		this.counterGarbage = counterGarbage;
	}
	
}
