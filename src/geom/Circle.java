package geom;

import processing.core.PApplet;

public class Circle {
	private Point cen;
	private float diam;
	private float angleOffset;
	
	public Circle(float x, float y, float radius) {
		cen = new Point(x, y);
		this.diam = radius*2f;
	}
	
	public Circle(Circle c) {
		this(c.cen.x, c.cen.y, c.getRadius());
	}
	
	public void display(PApplet pa) {
		pa.ellipseMode(pa.CENTER);
		pa.ellipse(cen.x, cen.y, diam, diam);
	}

	public void translate(float dx, float dy) {
		cen.x += dx;
		cen.y += dy;
	}
	
	public float getCenx() {
		return cen.x;
	}
	
	public float getCeny() {
		return cen.y;
	}
	
	public void setCenter(float cenx, float ceny) {
		cen.x = cenx;
		cen.y = ceny;
	}
	
	public float getDiam() {
		return diam;
	}
	
	public void setDiam(float diam) {
		this.diam = diam;
	}
	
	public float getRadius() {
		return 0.5f*diam;
	}
	
	public void setRadius(float radius) {
		this.diam = 2f * radius;
	}
}

