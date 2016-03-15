package controlp5;

import controlP5.Controller;

/**
 * Place to put simple useful static functions that operate on ControlP5 objects.
 * 
 * @author James Morrow
 *
 */
public class Util {
	private Util() {}
	
	/**
	 * Changes the given Controller's lower y-coordinate value.
	 * @param c The Controller.
	 * @param y2 The new value for the Controller's lower y-coordinate.
	 */
	public static void setY2(Controller c, float y2) {
		c.setPosition(c.getPosition()[0], y2 - c.getHeight());
	}
	
	/**
	 * Changes the given controller's upper y-coordinate value.
	 * @param c The Controller.
	 * @param y1 The new value for the Controller's upper y-coordinate.
	 */
	public static void setY1(Controller c, float y1) {
		c.setPosition(c.getPosition()[0], y1);
	}
	
	/**
	 * Gives the leftmost x-coordinate of the given Controller.
	 * @param c The Controller.
	 * @return The leftmost x-coordinate.
	 */
	public static float getX1(Controller c) {
		return c.getPosition()[0];
	}
	
	/**
	 * Gives the uppermost y-coordinate of the given Controller.
	 * @param c The Controller.
	 * @return The uppermost y-coordinate.
	 */
	public static float getY1(Controller c) {
		return c.getPosition()[1];
	}
	
	/**
	 * Gives the rightmost x-coordinate of the given Controller.
	 * @param c The Controller.
	 * @return The rightmost x-coordinate.
	 */
	public static float getX2(Controller c) {
		return c.getPosition()[0] + c.getWidth();
	}
	
	/**
	 * Gives the lowermost y-coordinate of the given Controller.
	 * @param c The Controller.
	 * @return The lowermost y-coordinate.
	 */
	public static float getY2(Controller c) {
		return c.getPosition()[1] + c.getHeight();
	}
}