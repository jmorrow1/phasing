package geom;

import processing.core.PApplet;

public class Circle extends Shape {
	private float x, y, radius, sqRadius;
	
	public Circle(Circle circle) {
		this.x = circle.x;
		this.y = circle.y;
		this.radius = circle.radius;
		this.sqRadius = circle.sqRadius;
	}
	
	public Circle(float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.sqRadius = radius*radius;
	}
	
	public boolean intersects(float x, float y) {
		return (x-this.x)*(x-this.x) + (y-this.y)*(y-this.y) < sqRadius;
	}
	
	@Override
	public void display(PApplet pa) {
		pa.ellipseMode(pa.RADIUS);
		pa.ellipse(x, y, radius, radius);
	}
	
	@Override
	public void translate(float dx, float dy) {
		x += dx;
		y += dy;
	}
	
	@Override
	public Point getCenter() {
		return new Point(x, y);
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
	
	@Override
	public Circle clone() {
		return new Circle(this);
	}
}
