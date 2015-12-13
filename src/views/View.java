package views;

import geom.Rect;
import phases.Phrase;
import processing.core.PApplet;

public abstract class View extends Rect {
	protected PApplet pa;
	protected Phrase phrase;
	protected int color1, color2, opacity;
	private int preset;
	
	public View(Rect rect, Phrase phrase, int color1, int color2, int opacity, int preset, PApplet pa) {
		super(rect);
		this.phrase = phrase;
		this.color1 = color1;
		this.color2 = color2;
		this.opacity = opacity;
		this.pa = pa;
		init();
		loadPreset(preset);
	}
	
	protected void init() {}

	public abstract void update(float dNotept1, float dNotept2, int sign);
	
	public abstract int numPresets();
	public abstract void loadPreset(int preset);
	
	public void incrementPreset() {
		preset = (preset+1) % numPresets();
		loadPreset(preset);
	}
	
	public void mousePressedInArea() {
		incrementPreset();
	}
}
