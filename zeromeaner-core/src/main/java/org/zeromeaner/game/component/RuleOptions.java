/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package org.zeromeaner.game.component;

import java.io.Serializable;

import org.zeromeaner.util.CustomProperties;


/**
 * Setting the rules of the game data
 */
public class RuleOptions implements Serializable {
	/** Serial version ID */
	private static final long serialVersionUID = 5781310758989780350L;

	/** Lateral motion counterOrrotation counterExceeded the fixed timeTo disable the reset */
	public static final int LOCKRESET_LIMIT_OVER_NORESET = 0;

	/** Lateral motion counterOrrotation counterI fixed the excess is immediately */
	public static final int LOCKRESET_LIMIT_OVER_INSTANT = 1;

	/** Lateral motion counterOrrotation counterI exceeded theWallkickDisable */
	public static final int LOCKRESET_LIMIT_OVER_NOWALLKICK = 2;

	public String resourceName;
	
	/** Of this ruleName */
	public String strRuleName;

	/** UseWallkickThe class name of the algorithm (If an empty stringWallkickNot) */
	public String strWallkick;

	/** The class name of the order of appearance correction algorithm to be used (If an empty string completely random) */
	public String strRandomizer;

	/** Game Style */
	public int style;

	/** BlockOf PeacerotationPatternX-coordinateCorrection (11Peace ×4Direction) */
	public int[][] pieceOffsetX;

	/** BlockOf PeacerotationPatternY-coordinateCorrection (11Peace ×4Direction) */
	public int[][] pieceOffsetY;

	/** BlockAppearance of the pieceX-coordinateCorrection (11Peace ×4Direction) */
	public int[][] pieceSpawnX;

	/** BlockAppearance of the pieceY-coordinateCorrection (11Peace ×4Direction) */
	public int[][] pieceSpawnY;

	/** BlockOf PeaceBigAppear whenX-coordinateCorrection (11Peace ×4Direction) */
	public int[][] pieceSpawnXBig;

	/** BlockOf PeaceBigAppear whenY-coordinateCorrection (11Peace ×4Direction) */
	public int[][] pieceSpawnYBig;

	/** BlockPeace color */
	public int[] pieceColor;

	/** BlockThe initial pieceDirection */
	public int[] pieceDefaultDirection;

	/** fieldEmerge from the above */
	public boolean pieceEnterAboveField;

	/** When the planned site appearance is buriedY-coordinateSlide on theMaximum count */
	public int pieceEnterMaxDistanceY;

	/** fieldThe width of the */
	public int fieldWidth;

	/** Field height */
	public int fieldHeight;

	/** fieldThe height of the invisible part of the above */
	public int fieldHiddenHeight;

	/** fieldPresence or absence of a ceiling of */
	public boolean fieldCeiling;

	/** fieldWhether you die did not put in the frame */
	public boolean fieldLockoutDeath;

	/** fieldWhether you die alone protruding into Attempts off target */
	public boolean fieldPartialLockoutDeath;

	/** NEXTOfcount */
	public int nextDisplay;

	/** Availability Hold */
	public boolean holdEnable;

	/** Hold preceding */
	public boolean holdInitial;

	/** Can not hold prior continuous use */
	public boolean holdInitialLimit;

	/** When using the holdBlockThe orientation of the piece back to its initial state */
	public boolean holdResetDirection;

	/** You can hold count (-1:Limitless) */
	public int holdLimit;

	/** Hard dropAvailability */
	public boolean harddropEnable;

	/** Hard dropImmediately fixed */
	public boolean harddropLock;

	/** Hard dropNot continuous use */
	public boolean harddropLimit;

	/** Soft dropAvailability */
	public boolean softdropEnable;

	/** Soft dropImmediately fixed */
	public boolean softdropLock;

	/** Soft dropNot continuous use */
	public boolean softdropLimit;

	/** In the ground stateSoft dropThen immediately fixed */
	public boolean softdropSurfaceLock;

	/** Soft dropSpeed (1.0f=1G, 0.5f=0.5G) */
	public float softdropSpeed;

	/** Soft dropSpeedCurrent × normal speednTo double */
	public boolean softdropMultiplyNativeSpeed;

	/** Use new soft drop codes */
	public boolean softdropGravitySpeedLimit;

	/** Precedingrotation */
	public boolean rotateInitial;

	/** PrecedingrotationNot continuous use */
	public boolean rotateInitialLimit;

	/** Wallkick */
	public boolean rotateWallkick;

	/** PrecedingrotationButWallkickMake */
	public boolean rotateInitialWallkick;

	/** TopDirectionToWallkickYou count (-1:Infinite) */
	public int rotateMaxUpwardWallkick;

	/** falseLeft is positive ifrotation, When true,Right is positiverotation */
	public boolean rotateButtonDefaultRight;

	/** ReverserotationAllow (falseIf positiverotationThe same as the) */
	public boolean rotateButtonAllowReverse;

	/** 180-degree rotationAllow (falseIf positiverotationThe same as the) */
	public boolean rotateButtonAllowDouble;

	/** In the fall fixing timeReset */
	public boolean lockresetFall;

	/** Move fixed timeReset */
	public boolean lockresetMove;

	/** rotationFixed at timeReset */
	public boolean lockresetRotate;

	/** Lock delay reset on wallkick */
	public boolean lockresetWallkick;

	/** Lateral motion countLimit (-1:Infinite) */
	public int lockresetLimitMove;

	/** rotation countLimit (-1:Infinite) */
	public int lockresetLimitRotate;

	/** Lateral motion counterAndrotation counterShare (Lateral motion counterI use only) */
	public boolean lockresetLimitShareCount;

	/** Lateral motion counterOrrotation counterHappens when you exceed the (LOCKRESET_LIMIT_OVER_Begins with a constantcountI use) */
	public int lockresetLimitOver;

	/** Shining moment fixed frame count */
	public int lockflash;

	/** BlockDedicated shines frame Put */
	public boolean lockflashOnlyFrame;

	/** Line clearBeforeBlockShine frame Put */
	public boolean lockflashBeforeLineClear;

	/** ARE cancel on move */
	public boolean areCancelMove;

	/** ARE cancel on rotate*/
	public boolean areCancelRotate;

	/** ARE cancel on hold*/
	public boolean areCancelHold;

	/** Minimum/MaximumARE (-1:Unspecified) */
	public int minARE, maxARE;

	/** Minimum/MaximumARE after line clear (-1:Unspecified) */
	public int minARELine, maxARELine;

	/** Minimum/MaximumLine clear time (-1:Unspecified) */
	public int minLineDelay, maxLineDelay;

	/** Minimum/MaximumFixation time (-1:Unspecified) */
	public int minLockDelay, maxLockDelay;

	/** Minimum/MaximumHorizontal reservoir time (-1:Unspecified) */
	public int minDAS, maxDAS;

	/** Lateral movement interval */
	public int dasDelay;

	public boolean shiftLockEnable;

	/** ReadyCan accumulate on the screen next to */
	public boolean dasInReady;

	/** First frame Can accumulate in the horizontal */
	public boolean dasInMoveFirstFrame;

	/** BlockPossible reservoir beside the moment it shines */
	public boolean dasInLockFlash;

	/** Line clearCan I accumulate in horizontal */
	public boolean dasInLineClear;

	/** ARECan I accumulate in horizontal */
	public boolean dasInARE;

	/** AREAt the end of the frame Can accumulate in the horizontal */
	public boolean dasInARELastFrame;

	/** EndingCan accumulate on the screen next to the inrush */
	public boolean dasInEndingStart;

	/** Charge DAS on blocked move */
	public boolean dasChargeOnBlockedMove;

	/** Leave DAS charge alone when left/right are not held -- useful with dasRedirectInDelay **/
   public boolean dasStoreChargeOnNeutral;

   /** Allow direction changes during delays without zeroing DAS charge **/
   public boolean dasRedirectInDelay;

	/** First frame Can be moved in */
	public boolean moveFirstFrame;

	/** Diagonal movement */
	public boolean moveDiagonal;

	/** Permit simultaneous push up and down */
	public boolean moveUpAndDown;

	/** Simultaneously pressing the left and right permit */
	public boolean moveLeftAndRightAllow;

	/** Before when I press the left and right simultaneously frame Of input DirectionGive priority to (Preferred to ignore the left and right while holding down the left and press the right) */
	public boolean moveLeftAndRightUsePreviousInput;

	/** Line clearOn afterBlockThe1View the animation step by step fall */
	public boolean lineFallAnim;

	/** Line delay cancel on move */
	public boolean lineCancelMove;

	/** Line delay cancel on rotate */
	public boolean lineCancelRotate;

	/** Line delay cancel on hold */
	public boolean lineCancelHold;

	/** BlockPicture of */
	public int skin;

	/** ghost Presence or absence of (falseIfMode At theghost A is enabledI hide, even if you) */
	public boolean ghost;

	/**
	 * Constructor
	 */
	public RuleOptions() {
		reset();
	}

	/**
	 * Copy constructor
	 * @param r Copy source
	 */
	public RuleOptions(RuleOptions r) {
		copy(r);
	}

	/**
	 * Initialization
	 */
	public void reset() {
		strRuleName = "";
		strWallkick = "";
		strRandomizer = "";

		style = 0;

		pieceOffsetX = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceOffsetY = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceSpawnX = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceSpawnY = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceSpawnXBig = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceSpawnYBig = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];

		pieceColor = new int[Piece.PIECE_COUNT];
		pieceColor[Piece.PIECE_I] = Block.BLOCK_COLOR_GRAY;
		pieceColor[Piece.PIECE_L] = Block.BLOCK_COLOR_GRAY;
		pieceColor[Piece.PIECE_O] = Block.BLOCK_COLOR_GRAY;
		pieceColor[Piece.PIECE_Z] = Block.BLOCK_COLOR_GRAY;
		pieceColor[Piece.PIECE_T] = Block.BLOCK_COLOR_GRAY;
		pieceColor[Piece.PIECE_J] = Block.BLOCK_COLOR_GRAY;
		pieceColor[Piece.PIECE_S] = Block.BLOCK_COLOR_GRAY;
		pieceColor[Piece.PIECE_I1] = Block.BLOCK_COLOR_PURPLE;
		pieceColor[Piece.PIECE_I2] = Block.BLOCK_COLOR_BLUE;
		pieceColor[Piece.PIECE_I3] = Block.BLOCK_COLOR_GREEN;
		pieceColor[Piece.PIECE_L3] = Block.BLOCK_COLOR_ORANGE;

		pieceDefaultDirection = new int[Piece.PIECE_COUNT];
		pieceEnterAboveField = true;
		pieceEnterMaxDistanceY = 0;

		fieldWidth = Field.DEFAULT_WIDTH;
		fieldHeight = Field.DEFAULT_HEIGHT;
		fieldHiddenHeight = Field.DEFAULT_HIDDEN_HEIGHT;
		fieldCeiling = false;
		fieldLockoutDeath = true;
		fieldPartialLockoutDeath = false;

		nextDisplay = 3;

		holdEnable = true;
		holdInitial = true;
		holdInitialLimit = false;
		holdResetDirection = true;
		holdLimit = -1;

		harddropEnable = true;
		harddropLock = true;
		harddropLimit = true;

		softdropEnable = true;
		softdropLock = false;
		softdropLimit = false;
		softdropSurfaceLock = false;
		softdropSpeed = 0.5f;
		softdropMultiplyNativeSpeed = false;
		softdropGravitySpeedLimit = false;

		rotateInitial = true;
		rotateInitialLimit = false;
		rotateWallkick = true;
		rotateInitialWallkick = true;
		rotateMaxUpwardWallkick = -1;
		rotateButtonDefaultRight = true;
		rotateButtonAllowReverse = true;
		rotateButtonAllowDouble = true;

		lockresetFall = true;
		lockresetMove = true;
		lockresetRotate = true;
		lockresetWallkick = false;
		lockresetLimitMove = 15;
		lockresetLimitRotate = 15;
		lockresetLimitShareCount = true;
		lockresetLimitOver = LOCKRESET_LIMIT_OVER_INSTANT;

		lockflash = 2;
		lockflashOnlyFrame = true;
		lockflashBeforeLineClear = false;
		areCancelMove = false;
		areCancelRotate = false;
		areCancelHold = false;

		minARE = -1;
		maxARE = -1;
		minARELine = -1;
		maxARELine = -1;
		minLineDelay = -1;
		maxLineDelay = -1;
		minLockDelay = -1;
		maxLockDelay = -1;
		minDAS = -1;
		maxDAS = -1;

		dasDelay = 0;

		shiftLockEnable = false;

		dasInReady = true;
		dasInMoveFirstFrame = true;
		dasInLockFlash = true;
		dasInLineClear = true;
		dasInARE = true;
		dasInARELastFrame = true;
		dasInEndingStart = true;
		dasChargeOnBlockedMove = false;
	   dasStoreChargeOnNeutral = false;
	   dasRedirectInDelay = false;

		moveFirstFrame = true;
		moveDiagonal = true;
		moveUpAndDown = true;
		moveLeftAndRightAllow = true;
		moveLeftAndRightUsePreviousInput = false;

		lineFallAnim = true;
		lineCancelMove = false;
		lineCancelRotate = false;
		lineCancelHold = false;

		skin = 0;
		ghost = true;
	}

	/**
	 * OtherRuleParamCopy the contents of the
	 * @param r Copy sourceOfRuleParam
	 */
	public void copy(RuleOptions r) {
		strRuleName = r.strRuleName;
		strWallkick = r.strWallkick;
		strRandomizer = r.strRandomizer;

		style = r.style;

		pieceOffsetX = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceOffsetY = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceSpawnX = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceSpawnY = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceSpawnXBig = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceSpawnYBig = new int[Piece.PIECE_COUNT][Piece.DIRECTION_COUNT];
		pieceColor = new int[Piece.PIECE_COUNT];
		pieceDefaultDirection = new int[Piece.PIECE_COUNT];
		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				pieceOffsetX[i][j] = r.pieceOffsetX[i][j];
				pieceOffsetY[i][j] = r.pieceOffsetY[i][j];
				pieceSpawnX[i][j] = r.pieceSpawnX[i][j];
				pieceSpawnY[i][j] = r.pieceSpawnY[i][j];
				pieceSpawnXBig[i][j] = r.pieceSpawnXBig[i][j];
				pieceSpawnYBig[i][j] = r.pieceSpawnYBig[i][j];
			}
			pieceColor[i] = r.pieceColor[i];
			pieceDefaultDirection[i] = r.pieceDefaultDirection[i];
		}
		pieceEnterAboveField = r.pieceEnterAboveField;
		pieceEnterMaxDistanceY = r.pieceEnterMaxDistanceY;

		fieldWidth = r.fieldWidth;
		fieldHeight = r.fieldHeight;
		fieldHiddenHeight = r.fieldHiddenHeight;
		fieldCeiling = r.fieldCeiling;
		fieldLockoutDeath = r.fieldLockoutDeath;
		fieldPartialLockoutDeath = r.fieldPartialLockoutDeath;

		nextDisplay = r.nextDisplay;

		holdEnable = r.holdEnable;
		holdInitial = r.holdInitial;
		holdInitialLimit = r.holdInitialLimit;
		holdResetDirection = r.holdResetDirection;
		holdLimit = r.holdLimit;

		harddropEnable = r.harddropEnable;
		harddropLock = r.harddropLock;
		harddropLimit = r.harddropLimit;

		softdropEnable = r.softdropEnable;
		softdropLock = r.softdropLock;
		softdropLimit = r.softdropLimit;
		softdropSurfaceLock = r.softdropSurfaceLock;
		softdropSpeed = r.softdropSpeed;
		softdropMultiplyNativeSpeed = r.softdropMultiplyNativeSpeed;
		softdropGravitySpeedLimit = r.softdropGravitySpeedLimit;

		rotateInitial = r.rotateInitial;
		rotateInitialLimit = r.rotateInitialLimit;
		rotateWallkick = r.rotateWallkick;
		rotateInitialWallkick = r.rotateInitialWallkick;
		rotateMaxUpwardWallkick = r.rotateMaxUpwardWallkick;
		rotateButtonDefaultRight = r.rotateButtonDefaultRight;
		rotateButtonAllowReverse = r.rotateButtonAllowReverse;
		rotateButtonAllowDouble = r.rotateButtonAllowDouble;

		lockresetFall = r.lockresetFall;
		lockresetMove = r.lockresetMove;
		lockresetRotate = r.lockresetRotate;
		lockresetWallkick = r.lockresetWallkick;
		lockresetLimitMove = r.lockresetLimitMove;
		lockresetLimitRotate = r.lockresetLimitRotate;
		lockresetLimitShareCount = r.lockresetLimitShareCount;
		lockresetLimitOver = r.lockresetLimitOver;

		lockflash = r.lockflash;
		lockflashOnlyFrame = r.lockflashOnlyFrame;
		lockflashBeforeLineClear = r.lockflashBeforeLineClear;
		areCancelMove = r.areCancelMove;
		areCancelRotate = r.areCancelRotate;
		areCancelHold = r.areCancelHold;

		minARE = r.minARE;
		maxARE = r.maxARE;
		minARELine = r.minARELine;
		maxARELine = r.maxARELine;
		minLineDelay = r.minLineDelay;
		maxLineDelay = r.maxLineDelay;
		minLockDelay = r.minLockDelay;
		maxLockDelay = r.maxLockDelay;
		minDAS = r.minDAS;
		maxDAS = r.maxDAS;

		dasDelay = r.dasDelay;

		shiftLockEnable = r.shiftLockEnable;

		dasInReady = r.dasInReady;
		dasInMoveFirstFrame = r.dasInMoveFirstFrame;
		dasInLockFlash = r.dasInLockFlash;
		dasInLineClear = r.dasInLineClear;
		dasInARE = r.dasInARE;
		dasInARELastFrame = r.dasInARELastFrame;
		dasInEndingStart = r.dasInEndingStart;
		dasChargeOnBlockedMove = r.dasChargeOnBlockedMove;
		dasStoreChargeOnNeutral = r.dasStoreChargeOnNeutral;
      dasRedirectInDelay = r.dasRedirectInDelay;

		moveFirstFrame = r.moveFirstFrame;
		moveDiagonal = r.moveDiagonal;
		moveUpAndDown = r.moveUpAndDown;
		moveLeftAndRightAllow = r.moveLeftAndRightAllow;
		moveLeftAndRightUsePreviousInput = r.moveLeftAndRightUsePreviousInput;

		lineFallAnim = r.lineFallAnim;
		lineCancelMove = r.lineCancelMove;
		lineCancelRotate = r.lineCancelRotate;
		lineCancelHold = r.lineCancelHold;

		skin = r.skin;
		ghost = r.ghost;
	}

	/**
	 * Compared with other rules, If the sametrueReturns
	 * @param r Rules to compare
	 * @param ignoreGraphicsSetting trueIgnore the settings that do not affect the game itself and to
	 * @return If compared to the same rulestrue
	 */
	public boolean compare(RuleOptions r, boolean ignoreGraphicsSetting) {
		if((!ignoreGraphicsSetting) && (strRuleName != r.strRuleName)) return false;
		if(strWallkick != r.strWallkick) return false;
		if(strRandomizer != r.strRandomizer) return false;

		if(style != r.style) return false;

		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				if(pieceOffsetX[i][j] != r.pieceOffsetX[i][j]) return false;
				if(pieceOffsetY[i][j] != r.pieceOffsetY[i][j]) return false;
				if(pieceSpawnX[i][j] != r.pieceSpawnX[i][j]) return false;
				if(pieceSpawnY[i][j] != r.pieceSpawnY[i][j]) return false;
				if(pieceSpawnXBig[i][j] != r.pieceSpawnXBig[i][j]) return false;
				if(pieceSpawnYBig[i][j] != r.pieceSpawnYBig[i][j]) return false;
			}
			if((!ignoreGraphicsSetting) && (pieceColor[i] != r.pieceColor[i])) return false;
			if(pieceDefaultDirection[i] != r.pieceDefaultDirection[i]) return false;
		}
		if(pieceEnterAboveField != r.pieceEnterAboveField) return false;
		if(pieceEnterMaxDistanceY != r.pieceEnterMaxDistanceY) return false;

		if(fieldWidth != r.fieldWidth) return false;
		if(fieldHeight != r.fieldHeight) return false;
		if(fieldHiddenHeight != r.fieldHiddenHeight) return false;
		if(fieldCeiling != r.fieldCeiling) return false;
		if(fieldLockoutDeath != r.fieldLockoutDeath) return false;
		if(fieldPartialLockoutDeath != r.fieldPartialLockoutDeath) return false;

		if(nextDisplay != r.nextDisplay) return false;

		if(holdEnable != r.holdEnable) return false;
		if(holdInitial != r.holdInitial) return false;
		if(holdInitialLimit != r.holdInitialLimit) return false;
		if(holdResetDirection != r.holdResetDirection) return false;
		if(holdLimit != r.holdLimit) return false;

		if(harddropEnable != r.harddropEnable) return false;
		if(harddropLock != r.harddropLock) return false;
		if(harddropLimit != r.harddropLimit) return false;

		if(softdropEnable != r.softdropEnable) return false;
		if(softdropLock != r.softdropLock) return false;
		if(softdropLimit != r.softdropLimit) return false;
		if(softdropSurfaceLock != r.softdropSurfaceLock) return false;
		if(softdropSpeed != r.softdropSpeed) return false;
		if(softdropMultiplyNativeSpeed != r.softdropMultiplyNativeSpeed) return false;
		if(softdropGravitySpeedLimit != r.softdropGravitySpeedLimit) return false;

		if(rotateInitial != r.rotateInitial) return false;
		if(rotateInitialLimit != r.rotateInitialLimit) return false;
		if(rotateWallkick != r.rotateWallkick) return false;
		if(rotateInitialWallkick != r.rotateInitialWallkick) return false;
		if(rotateMaxUpwardWallkick != r.rotateMaxUpwardWallkick) return false;
		if(rotateButtonDefaultRight != r.rotateButtonDefaultRight) return false;
		if(rotateButtonAllowReverse != r.rotateButtonAllowReverse) return false;
		if(rotateButtonAllowDouble != r.rotateButtonAllowDouble) return false;

		if(lockresetFall != r.lockresetFall) return false;
		if(lockresetMove != r.lockresetMove) return false;
		if(lockresetRotate != r.lockresetRotate) return false;
		if(lockresetWallkick != r.lockresetWallkick) return false;
		if(lockresetLimitMove != r.lockresetLimitMove) return false;
		if(lockresetLimitRotate != r.lockresetLimitRotate) return false;
		if(lockresetLimitShareCount != r.lockresetLimitShareCount) return false;
		if(lockresetLimitOver != r.lockresetLimitOver) return false;

		if(lockflash != r.lockflash) return false;
		if(lockflashOnlyFrame != r.lockflashOnlyFrame) return false;
		if(lockflashBeforeLineClear != r.lockflashBeforeLineClear) return false;
		if(areCancelMove != r.areCancelMove) return false;
		if(areCancelRotate != r.areCancelRotate) return false;
		if(areCancelHold != r.areCancelHold) return false;

		if(minARE != r.minARE) return false;
		if(maxARE != r.maxARE) return false;
		if(minARELine != r.minARELine) return false;
		if(maxARELine != r.maxARELine) return false;
		if(minLineDelay != r.minLineDelay) return false;
		if(maxLineDelay != r.maxLineDelay) return false;
		if(minLockDelay != r.minLockDelay) return false;
		if(maxLockDelay != r.maxLockDelay) return false;
		if(minDAS != r.minDAS) return false;
		if(maxDAS != r.maxDAS) return false;

		if(dasDelay != r.dasDelay) return false;

		if(shiftLockEnable != r.shiftLockEnable) return false;

		if(dasInReady != r.dasInReady) return false;
		if(dasInMoveFirstFrame != r.dasInMoveFirstFrame) return false;
		if(dasInLockFlash != r.dasInLockFlash) return false;
		if(dasInLineClear != r.dasInLineClear) return false;
		if(dasInARE != r.dasInARE) return false;
		if(dasInARELastFrame != r.dasInARELastFrame) return false;
		if(dasInEndingStart != r.dasInEndingStart) return false;
		if(dasChargeOnBlockedMove != r.dasChargeOnBlockedMove) return false;
		if(dasStoreChargeOnNeutral != r.dasStoreChargeOnNeutral) return false;
		if(dasRedirectInDelay != r.dasRedirectInDelay) return false;

		if(moveFirstFrame != r.moveFirstFrame) return false;
		if(moveDiagonal != r.moveDiagonal) return false;
		if(moveUpAndDown != r.moveUpAndDown) return false;
		if(moveLeftAndRightAllow != r.moveLeftAndRightAllow) return false;
		if(moveLeftAndRightUsePreviousInput != r.moveLeftAndRightUsePreviousInput) return false;

		if((ignoreGraphicsSetting) && (lineFallAnim != r.lineFallAnim)) return false;
		if(lineCancelMove != r.lineCancelMove) return false;
		if(lineCancelRotate != r.lineCancelRotate) return false;
		if(lineCancelHold != r.lineCancelHold) return false;

		if((ignoreGraphicsSetting) && (skin != r.skin)) return false;
		if(ghost != r.ghost) return false;

		return true;
	}

	/**
	 * Stored in the property set
	 * @param p Property Set
	 * @param id Player IDOrPresetID
	 */
	public void writeProperty(CustomProperties p, int id) {
		p = p.subProperties(id + ".ruleopt.");
		p.setProperty("strRuleName", strRuleName);
		p.setProperty("strWallkick", strWallkick);
		p.setProperty("strRandomizer", strRandomizer);

		p.setProperty("style", style);

		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				p.setProperty("pieceOffsetX." + i + "." + j, pieceOffsetX[i][j]);
				p.setProperty("pieceOffsetY." + i + "." + j, pieceOffsetY[i][j]);
				p.setProperty("pieceSpawnX." + i + "." + j, pieceSpawnX[i][j]);
				p.setProperty("pieceSpawnY." + i + "." + j, pieceSpawnY[i][j]);
				p.setProperty("pieceSpawnXBig." + i + "." + j, pieceSpawnXBig[i][j]);
				p.setProperty("pieceSpawnYBig." + i + "." + j, pieceSpawnYBig[i][j]);
			}
			p.setProperty("pieceColor." + i, pieceColor[i]);
			p.setProperty("pieceDefaultDirection." + i, pieceDefaultDirection[i]);
		}
		p.setProperty("pieceEnterAboveField", pieceEnterAboveField);
		p.setProperty("pieceEnterMaxDistanceY", pieceEnterMaxDistanceY);

		p.setProperty("fieldWidth", fieldWidth);
		p.setProperty("fieldHeight", fieldHeight);
		p.setProperty("fieldHiddenHeight", fieldHiddenHeight);
		p.setProperty("fieldCeiling", fieldCeiling);
		p.setProperty("fieldLockoutDeath", fieldLockoutDeath);
		p.setProperty("fieldPartialLockoutDeath", fieldPartialLockoutDeath);

		p.setProperty("nextDisplay", nextDisplay);

		p.setProperty("holdEnable", holdEnable);
		p.setProperty("holdInitial", holdInitial);
		p.setProperty("holdInitialLimit", holdInitialLimit);
		p.setProperty("holdResetDirection", holdResetDirection);
		p.setProperty("holdLimit", holdLimit);

		p.setProperty("harddropEnable", harddropEnable);
		p.setProperty("harddropLock", harddropLock);
		p.setProperty("harddropLimit", harddropLimit);

		p.setProperty("softdropEnable", softdropEnable);
		p.setProperty("softdropLock", softdropLock);
		p.setProperty("softdropLimit", softdropLimit);
		p.setProperty("softdropSurfaceLock", softdropSurfaceLock);
		p.setProperty("softdropSpeed", softdropSpeed);
		p.setProperty("softdropMultiplyNativeSpeed", softdropMultiplyNativeSpeed);
		p.setProperty("softdropGravitySpeedLimit", softdropGravitySpeedLimit);

		p.setProperty("rotateInitial", rotateInitial);
		p.setProperty("rotateInitialLimit", rotateInitialLimit);
		p.setProperty("rotateWallkick", rotateWallkick);
		p.setProperty("rotateInitialWallkick", rotateInitialWallkick);
		p.setProperty("rotateMaxUpwardWallkick", rotateMaxUpwardWallkick);
		p.setProperty("rotateButtonDefaultRight", rotateButtonDefaultRight);
		p.setProperty("rotateButtonAllowReverse", rotateButtonAllowReverse);
		p.setProperty("rotateButtonAllowDouble", rotateButtonAllowDouble);

		p.setProperty("lockresetFall", lockresetFall);
		p.setProperty("lockresetMove", lockresetMove);
		p.setProperty("lockresetRotate", lockresetRotate);
		p.setProperty("lockresetWallkick", lockresetWallkick);
		p.setProperty("lockresetLimitMove", lockresetLimitMove);
		p.setProperty("lockresetLimitRotate", lockresetLimitRotate);
		p.setProperty("lockresetLimitShareCount", lockresetLimitShareCount);
		p.setProperty("lockresetLimitOver", lockresetLimitOver);

		p.setProperty("lockflash", lockflash);
		p.setProperty("lockflashOnlyFrame", lockflashOnlyFrame);
		p.setProperty("lockflashBeforeLineClear", lockflashBeforeLineClear);
		p.setProperty("areCancelMove", areCancelMove);
		p.setProperty("areCancelRotate", areCancelRotate);
		p.setProperty("areCancelHold", areCancelHold);

		p.setProperty("minARE", minARE);
		p.setProperty("maxARE", maxARE);
		p.setProperty("minARELine", minARELine);
		p.setProperty("maxARELine", maxARELine);
		p.setProperty("minLineDelay", minLineDelay);
		p.setProperty("maxLineDelay", maxLineDelay);
		p.setProperty("minLockDelay", minLockDelay);
		p.setProperty("maxLockDelay", maxLockDelay);
		p.setProperty("minDAS", minDAS);
		p.setProperty("maxDAS", maxDAS);

		p.setProperty("dasDelay", dasDelay);

		p.setProperty("shiftLockEnable", shiftLockEnable);

		p.setProperty("dasInReady", dasInReady);
		p.setProperty("dasInMoveFirstFrame", dasInMoveFirstFrame);
		p.setProperty("dasInLockFlash", dasInLockFlash);
		p.setProperty("dasInLineClear", dasInLineClear);
		p.setProperty("dasInARE", dasInARE);
		p.setProperty("dasInARELastFrame", dasInARELastFrame);
		p.setProperty("dasInEndingStart", dasInEndingStart);
		p.setProperty("dasOnBlockedMove", dasChargeOnBlockedMove);
		p.setProperty("dasStoreChargeOnNeutral", dasStoreChargeOnNeutral);
		p.setProperty("dasRedirectInARE", dasRedirectInDelay);

		p.setProperty("moveFirstFrame", moveFirstFrame);
		p.setProperty("moveDiagonal", moveDiagonal);
		p.setProperty("moveUpAndDown", moveUpAndDown);
		p.setProperty("moveLeftAndRightAllow", moveLeftAndRightAllow);
		p.setProperty("moveLeftAndRightUsePreviousInput", moveLeftAndRightUsePreviousInput);

		p.setProperty("lineFallAnim", lineFallAnim);
		p.setProperty("lineCancelMove", lineCancelMove);
		p.setProperty("lineCancelRotate", lineCancelRotate);
		p.setProperty("lineCancelHold", lineCancelHold);

		p.setProperty("skin", skin);
		p.setProperty("ghost", ghost);
	}

	/**
	 * Read from the property set
	 * @param p Property Set
	 * @param id Player IDOrPresetID
	 */
	public void readProperty(CustomProperties p, int id) {
		p = p.subProperties(id + ".ruleopt.");
		strRuleName = p.getProperty("strRuleName", strRuleName);
		strWallkick = p.getProperty("strWallkick", strWallkick);
		strRandomizer = p.getProperty("strRandomizer", strRandomizer);

		style = p.getProperty("style", 0);

		for(int i = 0; i < Piece.PIECE_COUNT; i++) {
			for(int j = 0; j < Piece.DIRECTION_COUNT; j++) {
				pieceOffsetX[i][j] = p.getProperty("pieceOffsetX." + i + "." + j, pieceOffsetX[i][j]);
				pieceOffsetY[i][j] = p.getProperty("pieceOffsetY." + i + "." + j, pieceOffsetY[i][j]);
				pieceSpawnX[i][j] = p.getProperty("pieceSpawnX." + i + "." + j, pieceSpawnX[i][j]);
				pieceSpawnY[i][j] = p.getProperty("pieceSpawnY." + i + "." + j, pieceSpawnY[i][j]);
				pieceSpawnXBig[i][j] = p.getProperty("pieceSpawnXBig." + i + "." + j, pieceSpawnXBig[i][j]);
				pieceSpawnYBig[i][j] = p.getProperty("pieceSpawnYBig." + i + "." + j, pieceSpawnYBig[i][j]);
			}
			pieceColor[i] = p.getProperty("pieceColor." + i, pieceColor[i]);
			pieceDefaultDirection[i] = p.getProperty("pieceDefaultDirection." + i, pieceDefaultDirection[i]);
		}
		pieceEnterAboveField = p.getProperty("pieceEnterAboveField", pieceEnterAboveField);
		pieceEnterMaxDistanceY = p.getProperty("pieceEnterMaxDistanceY", pieceEnterMaxDistanceY);

		fieldWidth = p.getProperty("fieldWidth", fieldWidth);
		fieldHeight = p.getProperty("fieldHeight", fieldHeight);
		fieldHiddenHeight = p.getProperty("fieldHiddenHeight", fieldHiddenHeight);
		fieldCeiling = p.getProperty("fieldCeiling", fieldCeiling);
		fieldLockoutDeath = p.getProperty("fieldLockoutDeath", fieldLockoutDeath);
		fieldPartialLockoutDeath = p.getProperty("fieldPartialLockoutDeath", fieldPartialLockoutDeath);

		nextDisplay = p.getProperty("nextDisplay", nextDisplay);

		holdEnable = p.getProperty("holdEnable", holdEnable);
		holdInitial = p.getProperty("holdInitial", holdInitial);
		holdInitialLimit = p.getProperty("holdInitialLimit", holdInitialLimit);
		holdResetDirection = p.getProperty("holdResetDirection", holdResetDirection);
		holdLimit = p.getProperty("holdLimit", holdLimit);

		harddropEnable = p.getProperty("harddropEnable", harddropEnable);
		harddropLock = p.getProperty("harddropLock", harddropLock);
		harddropLimit = p.getProperty("harddropLimit", harddropLimit);

		softdropEnable = p.getProperty("softdropEnable", softdropEnable);
		softdropLock = p.getProperty("softdropLock", softdropLock);
		softdropLimit = p.getProperty("softdropLimit", softdropLimit);
		softdropSurfaceLock = p.getProperty("softdropSurfaceLock", softdropSurfaceLock);
		softdropSpeed = p.getProperty("softdropSpeed", softdropSpeed);
		softdropMultiplyNativeSpeed = p.getProperty("softdropMultiplyNativeSpeed", softdropMultiplyNativeSpeed);
		softdropGravitySpeedLimit = p.getProperty("softdropGravitySpeedLimit", softdropGravitySpeedLimit);

		rotateInitial = p.getProperty("rotateInitial", rotateInitial);
		rotateInitialLimit = p.getProperty("rotateInitialLimit", rotateInitialLimit);
		rotateWallkick = p.getProperty("rotateWallkick", rotateWallkick);
		rotateInitialWallkick = p.getProperty("rotateInitialWallkick", rotateInitialWallkick);
		rotateMaxUpwardWallkick = p.getProperty("rotateMaxUpwardWallkick", rotateMaxUpwardWallkick);
		rotateButtonDefaultRight = p.getProperty("rotateButtonDefaultRight", rotateButtonDefaultRight);
		rotateButtonAllowReverse = p.getProperty("rotateButtonAllowReverse", rotateButtonAllowReverse);
		rotateButtonAllowDouble = p.getProperty("rotateButtonAllowDouble", rotateButtonAllowDouble);

		lockresetFall = p.getProperty("lockresetFall", lockresetFall);
		lockresetMove = p.getProperty("lockresetMove", lockresetMove);
		lockresetRotate = p.getProperty("lockresetRotate", lockresetRotate);
		lockresetWallkick = p.getProperty("lockresetWallkick", lockresetWallkick);
		lockresetLimitMove = p.getProperty("lockresetLimitMove", lockresetLimitMove);
		lockresetLimitRotate = p.getProperty("lockresetLimitRotate", lockresetLimitRotate);
		lockresetLimitShareCount = p.getProperty("lockresetLimitShareCount", lockresetLimitShareCount);
		lockresetLimitOver = p.getProperty("lockresetLimitOver", lockresetLimitOver);

		lockflash = p.getProperty("lockflash", lockflash);
		lockflashOnlyFrame = p.getProperty("lockflashOnlyFrame", lockflashOnlyFrame);
		lockflashBeforeLineClear = p.getProperty("lockflashBeforeLineClear", lockflashBeforeLineClear);
		areCancelMove = p.getProperty("areCancelMove", areCancelMove);
		areCancelRotate = p.getProperty("areCancelRotate", areCancelRotate);
		areCancelHold = p.getProperty("areCancelHold", areCancelHold);

		minARE = p.getProperty("minARE", minARE);
		maxARE = p.getProperty("maxARE", maxARE);
		minARELine = p.getProperty("minARELine", minARELine);
		maxARELine = p.getProperty("maxARELine", maxARELine);
		minLineDelay = p.getProperty("minLineDelay", minLineDelay);
		maxLineDelay = p.getProperty("maxLineDelay", maxLineDelay);
		minLockDelay = p.getProperty("minLockDelay", minLockDelay);
		maxLockDelay = p.getProperty("maxLockDelay", maxLockDelay);
		minDAS = p.getProperty("minDAS", minDAS);
		maxDAS = p.getProperty("maxDAS", maxDAS);

		dasDelay = p.getProperty("dasDelay", dasDelay);
		shiftLockEnable = p.getProperty("shiftLockEnable", shiftLockEnable);

		dasInReady = p.getProperty("dasInReady", dasInReady);
		dasInMoveFirstFrame = p.getProperty("dasInMoveFirstFrame", dasInMoveFirstFrame);
		dasInLockFlash = p.getProperty("dasInLockFlash", dasInLockFlash);
		dasInLineClear = p.getProperty("dasInLineClear", dasInLineClear);
		dasInARE = p.getProperty("dasInARE", dasInARE);
		dasInARELastFrame = p.getProperty("dasInARELastFrame", dasInARELastFrame);
		dasInEndingStart = p.getProperty("dasInEndingStart", dasInEndingStart);
		dasChargeOnBlockedMove = p.getProperty("dasOnBlockedMove", dasChargeOnBlockedMove);
		dasStoreChargeOnNeutral = p.getProperty("dasStoreChargeOnNeutral", dasStoreChargeOnNeutral);
		dasRedirectInDelay = p.getProperty("dasRedirectInARE", dasRedirectInDelay);

		moveFirstFrame = p.getProperty("moveFirstFrame", moveFirstFrame);
		moveDiagonal = p.getProperty("moveDiagonal", moveDiagonal);
		moveUpAndDown = p.getProperty("moveUpAndDown", moveUpAndDown);
		moveLeftAndRightAllow = p.getProperty("moveLeftAndRightAllow", moveLeftAndRightAllow);
		moveLeftAndRightUsePreviousInput = p.getProperty("moveLeftAndRightUsePreviousInput", moveLeftAndRightUsePreviousInput);

		lineFallAnim = p.getProperty("lineFallAnim", lineFallAnim);
		lineCancelMove = p.getProperty("lineCancelMove", lineCancelMove);
		lineCancelRotate = p.getProperty("lineCancelRotate", lineCancelRotate);
		lineCancelHold = p.getProperty("lineCancelHold", lineCancelHold);

		skin = p.getProperty("skin", skin);
		ghost = p.getProperty("ghost", ghost);
	}
}
