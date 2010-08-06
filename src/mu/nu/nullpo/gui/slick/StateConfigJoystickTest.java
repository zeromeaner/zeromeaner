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
package mu.nu.nullpo.gui.slick;

import org.lwjgl.input.Controller;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 * ジョイスティックテスト画面のステート
 */
public class StateConfigJoystickTest extends BasicGameState {
	/** このステートのID */
	public static final int ID = 13;

	/** キー入力を受付可能になるまでのフレーム数 */
	public static final int KEYACCEPTFRAME = 20;

	/** プレイヤー number */
	public int player = 0;

	/** スクリーンショット撮影フラグ */
	protected boolean ssflag = false;

	/** 使用するジョイスティックの number */
	protected int joyNumber;

	/** 最後に押されたボタン */
	protected int lastPressButton;

	/** 経過フレーム数 */
	protected int frame;

	/** ボタン数 */
	protected int buttonCount;

	/** StateBasedGame */
	protected StateBasedGame gameObj;

	/*
	 * このステートのIDを取得
	 */
	@Override
	public int getID() {
		return ID;
	}

	/*
	 * ステートのInitialization
	 */
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		gameObj = game;
	}

	/**
	 * いろいろリセット
	 */
	protected void reset() {
		joyNumber = ControllerManager.controllerID[player];
		lastPressButton = -1;
		frame = 0;
		buttonCount = 0;

		if(joyNumber >= 0) {
			buttonCount = ControllerManager.controllers.get(joyNumber).getButtonCount();
		}
	}

	/*
	 * 画面描画
	 */
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		ResourceHolder.imgMenu.draw(0, 0);

		NormalFont.printFontGrid(1, 1, "JOYSTICK INPUT TEST (" + (player + 1) + "P)", NormalFont.COLOR_ORANGE);

		if(joyNumber < 0) {
			NormalFont.printFontGrid(1, 3, "NO JOYSTICK", NormalFont.COLOR_RED);
		} else if(frame >= KEYACCEPTFRAME) {
			NormalFont.printFontGrid(1, 3, "JOYSTICK NUMBER:" + joyNumber, NormalFont.COLOR_RED);

			NormalFont.printFontGrid(1, 5, "LAST PRESSED BUTTON:" + ((lastPressButton == -1) ? "NONE" : String.valueOf(lastPressButton)));

			Controller controller = ControllerManager.controllers.get(joyNumber);

			NormalFont.printFontGrid(1, 7, "AXIS X:" + controller.getXAxisValue());
			NormalFont.printFontGrid(1, 8, "AXIS Y:" + controller.getYAxisValue());

			NormalFont.printFontGrid(1, 10, "POV X:" + controller.getPovX());
			NormalFont.printFontGrid(1, 11, "POV Y:" + controller.getPovY());
		}

		if(frame >= KEYACCEPTFRAME) {
			NormalFont.printFontGrid(1, 23, "ENTER/BACKSPACE: EXIT", NormalFont.COLOR_GREEN);
		}

		if(!NullpoMinoSlick.alternateFPSTiming) NullpoMinoSlick.alternateFPSSleep();
	}

	/*
	 * ゲーム状態の更新
	 */
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		frame++;

		// ジョイスティックボタン判定
		if(frame >= KEYACCEPTFRAME) {
			for(int i = 0; i < buttonCount; i++) {
				try {
					if(ControllerManager.isControllerButton(player, container.getInput(), i)) {
						ResourceHolder.soundManager.play("change");
						lastPressButton = i;
					}
				} catch (Throwable e) {}
			}
		}

		if(NullpoMinoSlick.alternateFPSTiming) NullpoMinoSlick.alternateFPSSleep();
	}

	/*
	 * キーを押したときの処理
	 */
	@Override
	public void keyPressed(int key, char c) {
		if(frame >= KEYACCEPTFRAME) {
			// Backspace & Enter/Return
			if((key == Input.KEY_BACK) || (key == Input.KEY_RETURN)) {
				gameObj.enterState(StateConfigJoystickMain.ID);
			}
		}
	}

	/*
	 * このステートに入ったときの処理
	 */
	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		reset();
	}

	/*
	 * このステートを去るときの処理
	 */
	@Override
	public void leave(GameContainer container, StateBasedGame game) throws SlickException {
		reset();
	}
}