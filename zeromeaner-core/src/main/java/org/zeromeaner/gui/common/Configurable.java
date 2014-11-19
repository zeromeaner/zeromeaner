package org.zeromeaner.gui.common;

import javax.swing.JComponent;

import org.zeromeaner.util.CustomProperties;

public interface Configurable {
	public interface Configurator {
		public JComponent getConfigurationComponent();
		
		public void applyConfiguration(CustomProperties p);
		
		public void reloadConfiguration(CustomProperties p);
	}
	
	public Configurator getConfigurator();
}
