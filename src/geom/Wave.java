package geom;

import processing.core.PApplet;

public abstract class Wave {
	public abstract void display(PApplet pa, int color, int opacity);
	public abstract void translate(float dx);
}