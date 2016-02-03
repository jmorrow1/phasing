package geom;

import processing.core.PApplet;

/**
 * A pretty bare bones shape interface.
 * 
 * @author James Morrow
 *
 */
public abstract class Shape {
	/**
	 * Draws the shape to the given PApplet instance.
	 * @param pa
	 */
	public abstract void display(PApplet pa);
	
	/**
	 * Shifts the shape by (dx, dy)
	 * @param dx How much to shift the x-coordinate of the shape, in pixels
	 * @param dy How much to shift the y-coordinate of the shape, in pixels
	 */
	public abstract void translate(float dx, float dy);

	public abstract Shape clone();
}
