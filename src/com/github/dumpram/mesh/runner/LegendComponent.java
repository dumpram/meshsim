package com.github.dumpram.mesh.runner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LegendComponent extends JComponent {
	
	private static final long serialVersionUID = 1L;

	int x = 10;
	int y = 10;
	int width = 200;
	int height = 150;
	
	private JViewport viewport;
	
	public LegendComponent(JViewport viewport) {
		this.viewport = viewport;
		viewport.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setBounds(getBounds());
			}
			
		});
	
	}
	
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
		
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		Font f = g.getFont();
		Font f2 = new Font(f.getFontName(), Font.BOLD, f.getSize() + 1);
		g.setFont(f2);
		
		int offx = 0;
		int offy = 0;
		
		//setBounds(getBounds());
		
		g.drawRect(offx + 0, offy + 0, width-10, height-10);
		g.drawString("Legend:", offx + 10, offy + 20);
		g.drawRect(offx + 10, offy + 30, 10, 10);
		g.setColor(Color.GREEN);
		g.fillRect(offx + 11, offy + 31, 9, 9);
		g.setColor(Color.BLACK);
		g.drawString("Level 1", offx + 25, offy + 40);
		
		g.drawRect(offx + 10, offy + 50, 10, 10);
		g.setColor(Color.MAGENTA);
		g.fillRect(offx + 11, offy + 51, 9, 9);
		g.setColor(Color.BLACK);
		g.drawString("Level 2", offx + 25, offy + 60);
		
		g.drawRect(offx + 10, offy + 70, 10, 10);
		g.setColor(Color.ORANGE);
		g.fillRect(offx + 11, offy + 71, 9, 9);
		g.setColor(Color.BLACK);
		g.drawString("Gateway", offx + 25, offy + 80);
	}
	
	
	@Override
	public Rectangle getBounds() {
		int offx = viewport.getViewPosition().x;
		int offy = viewport.getViewPosition().y;
		return new Rectangle(offx + x, offy + y, width, height);
	}
	
}
