package geom;

import processing.core.PApplet;

public abstract class Wave {
	public abstract void display(PApplet pa);
	public abstract void translate(float dx, float dy);
	public abstract void interpolate(float amt);
	public static LinearPlot add(Wave a, Wave b) {
		return null;
	}
}