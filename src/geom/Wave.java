package geom;

import processing.core.PApplet;

public abstract class Wave {
	public abstract void display(PApplet pa);
	public abstract void translate(float dx, float dy);
	public static LinearPlot add(LinearPlot lp, float offset) {
		return null;
	}
	public static SineWave add(SineWave lp, float offset) {
		return null;
	}
}