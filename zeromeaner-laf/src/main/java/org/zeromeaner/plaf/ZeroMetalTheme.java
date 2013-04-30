package org.zeromeaner.plaf;

import java.awt.Color;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;

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
		return new ColorUIResource(0, 0, 128);
	}

	@Override
	protected ColorUIResource getSecondary2() {
		return new ColorUIResource(0, 0, 255);
	}

	@Override
	protected ColorUIResource getSecondary3() {
		return new ColorUIResource(0, 0, 192);
	}

	@Override
	protected ColorUIResource getBlack() {
		return new ColorUIResource(255, 255, 255);
	}
}
