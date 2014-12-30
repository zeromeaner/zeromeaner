package org.zeromeaner.plaf;

import java.awt.Color;
import java.awt.Font;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
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
		return new ColorUIResource(0xcc, 0xdd, 0xff);
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
		return new ColorUIResource(0xcc, 0xdd, 0xff);
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
	
	private static FontUIResource zeroFont(Font base) {
		Font f = Font.decode("Arial");
		if(f == null)
			f = base;
		f = f.deriveFont((float) base.getSize()).deriveFont(Font.BOLD);
		return new FontUIResource(f);
	}
	
	private FontUIResource controlTextFont = zeroFont(super.getControlTextFont());
	private FontUIResource menuTextFont = zeroFont(super.getMenuTextFont());
	private FontUIResource subTextFont = zeroFont(super.getSubTextFont());
	private FontUIResource userTextFont = zeroFont(super.getUserTextFont());
	private FontUIResource systemTextFont = zeroFont(super.getSystemTextFont());
	private FontUIResource windowTitleFont = zeroFont(super.getWindowTitleFont());
	
	@Override
	public FontUIResource getControlTextFont() {
		return controlTextFont;
	}
	
	@Override
	public FontUIResource getMenuTextFont() {
		return menuTextFont;
	}
	
	@Override
	public FontUIResource getSubTextFont() {
		return subTextFont;
	}
	
	@Override
	public FontUIResource getUserTextFont() {
		return userTextFont;
	}
	
	@Override
	public FontUIResource getSystemTextFont() {
		return systemTextFont;
	}
	
	@Override
	public FontUIResource getWindowTitleFont() {
		return windowTitleFont;
	}
}
