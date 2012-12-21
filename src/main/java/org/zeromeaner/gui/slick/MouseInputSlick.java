package org.zeromeaner.gui.slick;


import org.newdawn.slick.Input;
import org.zeromeaner.gui.AbstractMouseInput;

public class MouseInputSlick extends AbstractMouseInput {
	public static MouseInputSlick mouseInput;

	private MouseInputSlick() {
		super();
	}

	public static void initializeMouseInput() {
		mouseInput = new MouseInputSlick();
	}

	public void update(Input input) {
		mouseX = input.getMouseX();
		mouseY = input.getMouseY();
		if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
			mousePressed[0]++;
		} else {
			mousePressed[0] = 0;
		}
		if (input.isMouseButtonDown(Input.MOUSE_MIDDLE_BUTTON)) {
			mousePressed[1]++;
		} else {
			mousePressed[1] = 0;
		}
		if (input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON)) {
			mousePressed[2]++;
		} else {
			mousePressed[2] = 0;
		}
	}
}
