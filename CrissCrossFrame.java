/**
 * Criss Cross Game
 *
 * @version 1.00 
 * @author Michael Kalinin, Alykoff Gali
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class CrissCrossFrame extends JFrame {
	private WordArea area;
	private Dimension dimension;

	public CrissCrossFrame() {
		this.area = new WordArea();
		this.calcDimension();
		this.setTitle("Задача Criss Cross");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.add(area);
		this.addMouseListener(new MouseHandler());
		this.setMinimumSize(dimension);
		this.setSize(dimension);
	}

	private void calcDimension() {
		this.dimension = new Dimension(area.getwidth(), area.getheight());
	}
	
	public static void main(String[] args) {
		new CrissCrossFrame().setVisible(true);
	}
	
	private class MouseHandler extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent event) {
			area.nextArea();
			calcDimension();
			setMinimumSize(dimension);
			setSize(dimension);
		}
	}
}