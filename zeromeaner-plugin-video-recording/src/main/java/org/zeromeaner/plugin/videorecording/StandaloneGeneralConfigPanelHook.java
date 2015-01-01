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

public class StandaloneGeneralConfigPanelHook implements Hook {

	private static final Localization l = Localization.lz();
	
	protected JCheckBox enable = new JCheckBox(l.s("enable"));
	
	public StandaloneGeneralConfigPanelHook() {
		enable.setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public void createTabs(JTabbedPane tabs) {
		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(enable);
		tabs.addTab(l.s("recording_tab_name"), panel);
	}

	@Override
	public void saveConfiguration() {
		VideoRecordingOptions.get().ENABLED.set(enable.isSelected());
		VideoRecordingOptions.get().FPS.set(30);
	}

	@Override
	public void loadConfiguration() {
		enable.setSelected(VideoRecordingOptions.get().ENABLED.value());
		VideoRecordingOptions.get().FPS.set(30);
	}

}
