package org.zeromeaner.gui.knet;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.lang.reflect.Field;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.zeromeaner.game.knet.obj.KNetGameInfo;

public class KNetGameInfoEditor extends JTabbedPane {
	private static JSpinner numberSpinner(int dv, int low, int high, int step) {
		return new JSpinner(new SpinnerNumberModel(dv, low, high, step));
	}
	
	private JSpinner gravity = numberSpinner(1, 1, 20, 1);
	private JSpinner denominator = new JSpinner(new SpinnerNumberModel());
	private JSpinner are = numberSpinner(0, 0, Integer.MAX_VALUE, 1);
	private JSpinner areLine = numberSpinner(0, 0, Integer.MAX_VALUE, 1);
	private JSpinner lineDelay = numberSpinner(0, 0, Integer.MAX_VALUE, 1);
	private JSpinner lockDelay = numberSpinner(0, 0, Integer.MAX_VALUE, 1);
	private JSpinner das = numberSpinner(0, 0, Integer.MAX_VALUE, 1);
	private JCheckBox b2bEnable = new JCheckBox();
	private JSpinner comboType = numberSpinner(0, 0, 2, 1);
	private JComboBox tspinEnableType = new JComboBox(KNetGameInfo.TSpinEnableType.values());
	private JCheckBox synchronousPlay = new JCheckBox();
	private JCheckBox reduceLineSend = new JCheckBox();
	private JCheckBox useFractionalGarbage = new JCheckBox();
	private JCheckBox targettedGarbage = new JCheckBox();
	private JCheckBox rensaBlock = new JCheckBox();
	private JSpinner garbagePercent = numberSpinner(100, 0, Integer.MAX_VALUE, 1);
	private JCheckBox divideChangeRateByPlayers = new JCheckBox();
	private JCheckBox garbageChangePerAttack = new JCheckBox();
	private JSpinner hurryupSeconds = numberSpinner(0, 0, Integer.MAX_VALUE, 1);
	private JSpinner hurryupInterval = numberSpinner(0, 0, Integer.MAX_VALUE, 1);
	private JSpinner targetTimer = numberSpinner(0, 0, Integer.MAX_VALUE, 1);
	private JCheckBox b2bChunk = new JCheckBox();
	private JCheckBox counterGarbage = new JCheckBox();
	
	public KNetGameInfoEditor() {
		super(JTabbedPane.LEFT);
		
		JPanel p = new JPanel(new GridLayout(0, 2));
		try {
			for(Field f : KNetGameInfoEditor.class.getDeclaredFields()) {
				if(!JComponent.class.isAssignableFrom(f.getType()))
					continue;
				p.add(new JLabel(f.getName()));
				p.add((JComponent) f.get(this));
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
		
		addTab("JUNK", p);
	}
	
	public void load(KNetGameInfo g) {
		try {
			for(Field f : KNetGameInfoEditor.class.getDeclaredFields()) {
				if(!JComponent.class.isAssignableFrom(f.getType()))
					continue;
				Field gf = KNetGameInfo.class.getDeclaredField(f.getName());
				gf.setAccessible(true);
				if(gf.getType() == int.class)
					((JSpinner) f.get(this)).setValue(gf.get(g));
				else if(gf.getType() == boolean.class)
					((JCheckBox) f.get(this)).setSelected((Boolean) gf.get(g));
				else if(Object.class.isAssignableFrom(gf.getType()))
					((JComboBox) f.get(this)).setSelectedItem(gf.get(g));
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void store(KNetGameInfo g) {
		try {
			for(Field f : KNetGameInfoEditor.class.getDeclaredFields()) {
				if(!JComponent.class.isAssignableFrom(f.getType()))
					continue;
				Field gf = KNetGameInfo.class.getDeclaredField(f.getName());
				gf.setAccessible(true);
				if(gf.getType() == int.class)
					gf.set(g, ((JSpinner) f.get(this)).getValue());
				else if(gf.getType() == boolean.class)
					gf.set(g, ((JCheckBox) f.get(this)).isSelected());
				else if(Object.class.isAssignableFrom(gf.getType()))
					gf.set(g, ((JComboBox) f.get(this)).getSelectedItem());
			}
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
