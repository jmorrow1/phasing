package views;

import phases.Phrase;
import phases.Rect;
import processing.core.PApplet;

public abstract class View extends Rect {
	protected PApplet pa;
	protected Phrase phrase;
	
	public View(Rect rect, Phrase phrase, PApplet pa) {
		super(rect);
		this.phrase = phrase;
		this.pa = pa;
	}
	
	public abstract void update(float dBeatpt1, float dBeatpt2);
}
