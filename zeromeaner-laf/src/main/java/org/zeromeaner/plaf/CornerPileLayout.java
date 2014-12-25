package org.zeromeaner.plaf;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingConstants;

public class CornerPileLayout implements LayoutManager2 {
	
	public static final String NORTH_WEST = "north_west";
	
	public static final String SOUTH_EAST = "south_east";

	protected List<Component> nw;
	protected List<Component> se;
	protected List<Component> pending; 
	protected BufferedImage buf;
	
	public CornerPileLayout() {
		nw = new ArrayList<>();
		se = new ArrayList<>();
		pending = new ArrayList<>();
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		nw.remove(comp);
		se.remove(comp);
		buf = null;
		pending.addAll(nw);
		pending.addAll(se);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(400, 400);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(400, 400);
	}

	@Override
	public void layoutContainer(Container parent) {
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		if(NORTH_WEST.equals(constraints))
			nw.add(comp);
		else if(SOUTH_EAST.equals(constraints))
			se.add(comp);
		else
			throw new IllegalArgumentException();
		pending.add(comp);
		
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return null;
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0.5f;		
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0.5f;
	}

	@Override
	public void invalidateLayout(Container target) {
		buf = null;
		pending.addAll(nw);
		pending.addAll(se);
	}

}
