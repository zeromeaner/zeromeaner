package org.zeromeaner.util;

import java.util.Properties;

public final class PropertyConstants {
	public static final CustomProperties ROOT_PROPERTIES = new CustomProperties();
	public static final CustomProperties GLOBAL_PROPERTIES = new CustomProperties(ROOT_PROPERTIES, "0mino");
	public static final CustomProperties GUI_PROPERTIES = new CustomProperties(GLOBAL_PROPERTIES, "gui");
	public static final CustomProperties RUNTIME_PROPERTIES = new CustomProperties(GUI_PROPERTIES, "runtime");
	
	public static final String BGM_ENABLE = "0mino.bgm.enable";
	public static final String BGM_SELECTION = "0mino.bgm.selection";
	public static final String FULL_SCREEN = "0mino.full_screen";
	
	private PropertyConstants() {}
}
