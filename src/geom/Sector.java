package geom;

import processing.core.PApplet;

/**
 * Represents a shape consisting of two arcs with their ends joined by line segments.
 * 
 * @author James Morrow
 *
 */
public class Sector {
	private Arc arc1, arc2;
	
	/**
	 * Constructs a Sector.
	 * 
	 * @param cenx The center-x coordinate of circles c1 and c2
	 * @param ceny The center-y coordinate of circles c1 and c2
	 * @param r1 The radius of a circle, c1
	 * @param r2 The radius of a circle, c2
	 * @param a1 The start angle of arcs arc1 and arc2
	 * @param a2 The end angle of arcs arc1 and arc2
	 */
	public Sector(float cenx, float ceny, float r1, float r2, float a1, float a2) {
		arc1 = new Arc(cenx, ceny, a1, a2, r1);
		arc2 = new Arc(cenx, ceny, a1, a2, r2);
	}
	
	/**
	 * Copy constructor.
	 * @param s The sector to copy
	 */
	public Sector(Sector s) {
		s.arc1 = new Arc(arc1);
		s.arc2 = new Arc(arc2);
	}
	
	/**
	 * Draws the shape.
	 * @param pa The PApplet on which to draw the shape
	 */
	public void display(PApplet pa) {
		arc1.display(pa);
		pa.line(arc1.endX(), arc1.endY(), arc2.endX(), arc2.endY());
		arc2.display(pa);
		pa.line(arc2.startX(), arc2.startY(), arc1.startX(), arc1.startY());		
	}

	/**
	 * Displaces the shape by (dx, dy).
	 * @param dx The number of pixels to move horizontally
	 * @param dy The number of pixels to move vertically
	 */
	public void translate(float dx, float dy) {
		arc1.translate(dx, dy);
		arc2.translate(dx, dy);
	}
	
	/**
	 * Rotates the shape by the given angle, in radians.
	 * @param dAngle The number of radians to rotate the Shape
	 */
	public void rotate(float dAngle) {
		arc1.rotate(dAngle);
		arc2.rotate(dAngle);
	}
}
