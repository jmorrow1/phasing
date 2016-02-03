package geom;

import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class Circle extends Shape {
	private float x, y, radius;
	
	/**
	 * Copy constructor.
	 * @param circle The circle to copy
	 */
	public Circle(Circle circle) {
		this.x = circle.x;
		this.y = circle.y;
		this.radius = circle.radius;
	}
	
	/**
	 * 
	 * @param x The center x-coordinate of the circle
	 * @param y The center y-coordinate of the circle
	 * @param radius The radius of the circle
	 */
	public Circle(float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
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
	
	/**
	 * 
	 * @return The center of the circle
	 */
	public Point getCenter() {
		return new Point(x, y);
	}
	
	/**
	 * 
	 * @return The center x-coordinate of the circle.
	 */
	public float getX() {
		return x;
	}

	/**
	 * Sets the center x-coordinate of the circle to the specified value.
	 * @param x
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * 
	 * @return The center y-coordinate of the circle
	 */
	public float getY() {
		return y;
	}

	/**
	 * Sets the center y-coordinate of the circle to the specified value.
	 * @param y
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * 
	 * @return The radius of the circle
	 */
	public float getRadius() {
		return radius;
	}

	/**
	 * Sets the radius of the circle to the specified value.
	 * @param radius
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	@Override
	public Circle clone() {
		return new Circle(this);
	}
}
