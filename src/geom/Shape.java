package geom;

import processing.core.PApplet;

public abstract class Shape {
	public abstract void display(PApplet pa);
	public abstract void translate(float dx, float dy);
}
