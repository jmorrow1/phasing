package controlp5;

import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.DropdownList;
import processing.core.PGraphics;

public class DropdownListPlus extends DropdownList {

	public DropdownListPlus(ControlP5 theControlP5, String theName) {
		super(theControlP5, theName);
	}

	protected DropdownListPlus(ControlP5 theControlP5, ControllerGroup<?> theGroup, String theName, int theX, int theY,
			int theW, int theH) {
		super(theControlP5, theGroup, theName, theX, theY, theW, theH);
	}
	
	public void draw(final PGraphics pg) {
		pg.pushMatrix();
		pg.translate(x(position) , y(position));
		
		//draw white rectangle
		pg.noStroke();
		pg.fill(255);
		pg.rectMode(pg.CORNER);
		pg.rect(0, 0, this.getWidth(), this.getHeight());
		
		_myControllerView.display( pg , this );
		pg.popMatrix();
	}
	
	public void onRelease() {
		try {
			super.onRelease();
		}
		catch (IndexOutOfBoundsException e) {
		}
	}

}