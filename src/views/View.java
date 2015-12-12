package views;

import geom.Rect;
import phases.Phrase;
import processing.core.PApplet;

public abstract class View extends Rect {
	protected PApplet pa;
	protected int color1, color2, opacity;
	protected int preset;
	
	public View(Rect rect, int color1, int color2, int opacity, PApplet pa) {
		super(rect);
		this.color1 = color1;
		this.color2 = color2;
		this.opacity = opacity;
		this.pa = pa;
	}

	public abstract void update(float dNotept1, float dNotept2, int sign);
	
	public abstract void incrementPreset();
}
