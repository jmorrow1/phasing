package geom;

import processing.core.PApplet;

public class Circle {
	private float x, y, radius, sqRadius;
	
	public Circle(float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.sqRadius = radius*radius;
	}
	
	public boolean intersects(float x, float y) {
		return (x-this.x)*(x-this.x) + (y-this.y)*(y-this.y) < radius;
	}
	
	public void display(PApplet pa) {
		pa.ellipseMode(pa.RADIUS);
		pa.ellipse(x, y, radius, radius);
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
		sqRadius = radius*radius;
	}
}
