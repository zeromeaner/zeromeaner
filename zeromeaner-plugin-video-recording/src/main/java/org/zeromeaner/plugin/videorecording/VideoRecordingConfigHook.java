package org.zeromeaner.plugin.videorecording;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.zeromeaner.gui.reskin.StandaloneGeneralConfigPanel.Hook;
import org.zeromeaner.util.Localization;

public class VideoRecordingConfigHook implements Hook {

	private static final Localization l = Localization.lz();
	
	protected JCheckBox enable = new JCheckBox(l.s("enable"));
	protected JRadioButton fps30 = new JRadioButton(l.s("fps_30"));
	protected JRadioButton fps60 = new JRadioButton(l.s("fps_60"));
	
	public VideoRecordingConfigHook() {
		ButtonGroup g = new ButtonGroup();
		g.add(fps30);
		g.add(fps60);
		
		enable.setHorizontalAlignment(SwingConstants.CENTER);
		fps30.setHorizontalAlignment(SwingConstants.CENTER);
		fps60.setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public void createTabs(JTabbedPane tabs) {
		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(enable);
		panel.add(fps30);
		panel.add(fps60);
		tabs.addTab(l.s("recording_tab_name"), panel);
	}

	@Override
	public void saveConfiguration() {
		VideoRecordingOptions.get().ENABLED.set(enable.isSelected());
		VideoRecordingOptions.get().FPS.set(fps30.isSelected() ? 30 : 60);
	}

	@Override
	public void loadConfiguration() {
		enable.setSelected(VideoRecordingOptions.get().ENABLED.value());
		(VideoRecordingOptions.get().FPS.value() == 30 ? fps30 : fps60).setSelected(true);
	}

}
