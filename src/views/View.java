package views;

import geom.Rect;
import phases.Phrase;
import processing.core.PApplet;

public abstract class View extends Rect {
	protected PApplet pa;
	protected Phrase phrase;
	protected int color1, color2, opacity;
	
	public View(Rect rect, Phrase phrase, int color1, int color2, int opacity, PApplet pa) {
		super(rect);
		this.phrase = phrase;
		this.color1 = color1;
		this.color2 = color2;
		this.opacity = opacity;
		this.pa = pa;
	}
	
	public abstract void update(float dBeatpt1, float dBeatpt2);
}
