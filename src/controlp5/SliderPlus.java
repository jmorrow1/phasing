package controlp5;

import controlP5.ControlP5;
import controlP5.Slider;
import phasing.PhasesPApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import util.FloatFormatter;

/**
 * A subclass of ControlP5's Slider that overrides Slider's draw method in order to draw text on top of and above the slider.
 * @author James Morrow
 *
 */
public class SliderPlus extends Slider {
	private PFont captionFont, valueFont;
	private FloatFormatter f;
	
	/**
	 * 
	 * @param cp5 The cp5 instance
	 * @param name The controller's name
	 * @param captionFont The font used to draw the controller's name just above the slider
	 * @param valueFont The font used to draw the controller's value on top of the slider
	 * @param f A function from float to String, which is used to format the controller's value
	 */
	public SliderPlus(ControlP5 cp5, String name, PFont captionFont, PFont valueFont, FloatFormatter f) {
		super(cp5, name);
		this.captionFont = captionFont;
		this.valueFont = valueFont;
		this.f = f;
	}

	public void draw(final PGraphics pg) {
		pg.pushMatrix();
		pg.translate(x(position) , y(position));
		_myControllerView.display(pg, this);

		//draw white rect under caption label
		pg.fill(255);
		pg.noStroke();
		pg.rectMode(pg.CORNERS);
		pg.rect(0, -20, getWidth(), 0);
		
		//draw caption label
		pg.textFont(captionFont);
		pg.fill(0);
		pg.textAlign(pg.LEFT, pg.BOTTOM);
		pg.text(getLabel(), 0, 0);
		
		//draw value label
		pg.textFont(valueFont);
		pg.fill(255);
		String s = f.format(getValue());
		pg.textAlign(pg.CENTER, pg.CENTER);
		pg.text(s, 0, 0, getWidth(), getHeight());
		
		pg.popMatrix();
	}
}