package geom;

/**
 * 
 * @author James Morrow
 *
 */
public class Point {
	public float x, y;
	
	/**
	 * 
	 * @param x The x-coordinate of the point
	 * @param y The y-coordinate of the point
	 */
	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Copy constructor
	 * @param pt The point to copy
	 */
	public Point(Point pt) {
		this.x = pt.x;
		this.y = pt.y;
	}
}
