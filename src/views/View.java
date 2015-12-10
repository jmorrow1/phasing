package views;

import geom.Rect;
import phases.PhasesPApplet;

public abstract class View extends Rect {
	protected PhasesPApplet pa;
	protected int color1, color2, opacity;
	
	public View(Rect rect, int color1, int color2, int opacity, PhasesPApplet pa) {
		super(rect);
		this.color1 = color1;
		this.color2 = color2;
		this.opacity = opacity;
		this.pa = pa;
	}
	
	public abstract void update(double dBeatpt1, double dBeatpt2);
}
