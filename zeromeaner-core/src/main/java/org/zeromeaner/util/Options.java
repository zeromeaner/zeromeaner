package org.zeromeaner.util;

import java.util.Properties;
import java.util.regex.Pattern;

import static org.zeromeaner.util.PropertyConstant.*;

public final class Options {
	public static final CustomProperties ROOT_PROPERTIES = new CustomProperties();
	public static final CustomProperties GLOBAL_PROPERTIES = new CustomProperties(ROOT_PROPERTIES, "0mino");
	public static final CustomProperties GUI_PROPERTIES = new CustomProperties(GLOBAL_PROPERTIES, "gui");
	public static final CustomProperties RUNTIME_PROPERTIES = new CustomProperties(GUI_PROPERTIES, "runtime");
	
	
	public static final Constant<Boolean> BGM_ENABLE = GUI_PROPERTIES.create(BOOLEAN, "0mino.bgm.enable", true);
	public static final Constant<String> BGM_SELECTION = GUI_PROPERTIES.create(STRING, "0mino.bgm.selection");
	public static final Constant<Boolean> FULL_SCREEN = RUNTIME_PROPERTIES.create(BOOLEAN, "0mino.standalone.full-screen", false);
	
	private Options() {}
}
