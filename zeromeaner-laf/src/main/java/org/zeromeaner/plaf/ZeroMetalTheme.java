package org.zeromeaner.plaf;

import java.awt.Color;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

public class ZeroMetalTheme extends DefaultMetalTheme {

	@Override
	public String getName() {
		return "0mino";
	}

	@Override
	protected ColorUIResource getPrimary1() {
		return new ColorUIResource(Color.GRAY);
	}

	@Override
	protected ColorUIResource getPrimary2() {
		return new ColorUIResource(Color.LIGHT_GRAY);
	}

	@Override
	protected ColorUIResource getPrimary3() {
		return new ColorUIResource(Color.WHITE);
	}

	@Override
	protected ColorUIResource getSecondary1() {
		return new ColorUIResource(0, 0, 64);
	}

	@Override
	protected ColorUIResource getSecondary2() {
		return new ColorUIResource(192, 192, 255);
	}

	@Override
	protected ColorUIResource getSecondary3() {
		return new ColorUIResource(0xcc, 0xcc, 0xcc);
	}

	@Override
	protected ColorUIResource getBlack() {
		return new ColorUIResource(0x60, 0x60, 0x60);
	}
	
	@Override
	public ColorUIResource getUserTextColor() {
		return new ColorUIResource(0, 0, 0);
	}
	
	@Override
	public ColorUIResource getTextHighlightColor() {
		return new ColorUIResource(128, 128, 128);
	}
	
	@Override
	public ColorUIResource getControlDarkShadow() {
		return getPrimary1();
	}
	
}
