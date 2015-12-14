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
	
	/**
	 * Moves every element in the array leftward.
	 * @param xs The array to left-shift.
	 */
	public static void leftShift(Point[] xs) {
	    if (xs.length > 0) {
	        int i = xs.length-1;
	        Point next = xs[i];
	        xs[i] = xs[0];
	        i--;
	        while (i >= 0) {
	            Point temp = xs[i];
	            xs[i] = next;
	            next = temp;
	            i--;
	        }
	    }
	}

	/**
	 * Moves every element in the array rightward.
	 * @param xs The array to right-shift.
	 */
	public static void rightShift(Point[] xs) {
	    if (xs.length > 0) {
	        int i=0;
	        Point prev = xs[i];
	        xs[i] = xs[xs.length-1];
	        i++;
	        while (i < xs.length) {
	        	Point temp = xs[i];
	            xs[i] = prev;
	            prev = temp;
	            i++;
	        }
	    }
	}
}
