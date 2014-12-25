package org.zeromeaner.plaf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

public class CornerPileLayout implements LayoutManager2 {
	
	public static final String NORTH_WEST = "north_west";
	
	public static final String SOUTH_EAST = "south_east";
	
	public static final Comparator<Component> PREFERRED_SIZE_ORDER = new Comparator<Component>() {
		
		@Override
		public int compare(Component o1, Component o2) {
			Dimension d1 = o1.getPreferredSize();
			Dimension d2 = o2.getPreferredSize();
			return -((Integer) (d1.width * d1.height)).compareTo(d2.width * d2.height);
		}
	};
	
	public static final Comparator<Component> TEXT_ORDER = new Comparator<Component>() {
		@Override
		public int compare(Component o1, Component o2) {
			String t1 = ((AbstractButton) o1).getText();
			String t2 = ((AbstractButton) o2).getText();
			t1 = t1.replaceAll("<.*?>", "");
			t2 = t2.replaceAll("<.*?>", "");
			int c = String.CASE_INSENSITIVE_ORDER.compare(t1.substring(0, 1), t2.substring(0, 1));
			if(c != 0)
				return c;
			return PREFERRED_SIZE_ORDER.compare(o1, o2);
		}
	};

	protected Set<Component> nw;
	protected Set<Component> se;
	protected PriorityQueue<Component> pending; 
	protected BufferedImage buf;
	
	public CornerPileLayout() {
		nw = new HashSet<>();
		se = new HashSet<>();
		pending = new PriorityQueue<>(10, TEXT_ORDER);
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
		if(buf == null || buf.getWidth() != parent.getWidth() || buf.getHeight() != parent.getHeight()) {
			buf = new BufferedImage(parent.getWidth(), parent.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
			Graphics g = buf.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, buf.getWidth(), buf.getHeight());
			pending.clear();
			pending.addAll(nw);
			pending.addAll(se);
		}
		while(pending.size() > 0) {
			Component c = pending.poll();
			Rectangle r = new Rectangle(0, 0, c.getPreferredSize().width, c.getPreferredSize().height);
			if(nw.contains(c)) {
				for(int i = 0; i < buf.getWidth() * buf.getHeight(); i++) {
					if(isAvailable(r))
						break;
					r.x-=6;
					r.y+=6;
					if(r.x < 0) {
						r.x = r.y;
						r.y = 0;
					}
					if(r.y >= buf.getHeight()) {
						r.x += buf.getHeight();
						r.y = 0;
					}
				}
			} else {
				r.x = buf.getWidth() - 1;
				r.y = buf.getHeight() - 1;
				for(int i = 0; i < buf.getWidth() * buf.getHeight(); i++) {
					if(isAvailable(r))
						break;
					r.x+=6;
					r.y-=6;
					if(r.x >= buf.getWidth()) {
						r.x = buf.getWidth() - (buf.getHeight() - r.y);
						r.y = buf.getHeight() - 1;
					}
					if(r.y < 0) {
						r.x -= buf.getHeight();
						r.y = buf.getHeight() - 1;
					}
				}
			}
			c.setSize(c.getPreferredSize());
			c.setLocation(r.x, r.y);
			Graphics g = buf.getGraphics();
			g.setColor(Color.BLACK);
			((Graphics2D) g).setStroke(new BasicStroke(6f));
			g.drawRect(r.x, r.y, r.width, r.height);
			g.fillRect(r.x, r.y, r.width, r.height);
		}
	}
	
	protected boolean isAvailable(Rectangle r) {
		if(r.x + r.width >= buf.getWidth())
			return false;
		if(r.y + r.height >= buf.getHeight())
			return false;
		for(int x = r.x; x < r.x + r.width; x++) {
			for(int y = r.y; y < r.y + r.height; y++) {
				if(buf.getRGB(x, y) != Color.WHITE.getRGB())
					return false;
			}
		}
		return true;
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
