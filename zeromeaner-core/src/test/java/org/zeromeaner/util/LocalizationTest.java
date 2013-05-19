package org.zeromeaner.util;

import org.junit.Assert;
import org.junit.Test;

import static org.zeromeaner.util.Localization.*;

public class LocalizationTest {
	private static Localization lz = new Localization();
	
	@Test
	public void testCallerClass() {
		Assert.assertEquals(LocalizationTest.class, lz.getBase());
		Assert.assertEquals(LocalizationTest.class, lz().getBase());
	}
	
	@Test
	public void testLocalized() {
		Assert.assertEquals("«unlocalized»", lz.s("unlocalized"));
		Assert.assertEquals("«unlocalized»", lz("unlocalized"));
		Assert.assertEquals("howdy", lz.s("localized"));
		Assert.assertEquals("howdy", lz("localized"));
	}
}
