package com.github.dumpram.mesh.runner;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

public class LegendComponent extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int x = 10;
	int y = 10;
	int width = 150;
	int height = 300;
	
	@Override
	protected void paintComponent(Graphics g) {
		g.drawRect(x, y, width, height);
	}
	
	
	@Override
	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
	
}
