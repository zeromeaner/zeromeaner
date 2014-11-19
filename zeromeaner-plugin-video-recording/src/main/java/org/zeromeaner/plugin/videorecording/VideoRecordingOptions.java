package org.zeromeaner.plugin.videorecording;

import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.PropertyConstant;
import org.zeromeaner.util.PropertyConstant.Constant;

public class VideoRecordingOptions {
	
	private static final VideoRecordingOptions instance = new VideoRecordingOptions();
	public static VideoRecordingOptions get() {
		return instance;
	}
	
	public final Constant<Boolean> ENABLED;
	public final Constant<Integer> FPS;
	
	private VideoRecordingOptions() {
		CustomProperties p = Options.GUI_PROPERTIES.subProperties(".videorecording.");
		ENABLED = p.create(PropertyConstant.BOOLEAN, "enabled", false);
		FPS = p.create(PropertyConstant.INTEGER, "fps", 30);
	}

}
