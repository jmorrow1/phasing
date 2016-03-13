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
	public static void setControllerY2(Controller c, float y2) {
		c.setPosition(c.getPosition()[0], y2 - c.getHeight());
	}
	
	/**
	 * Changes the given controller's upper y-coordinate value.
	 * @param c The Controller.
	 * @param y1 The new value for the Controller's upper y-coordinate.
	 */
	public static void setControllerY1(Controller c, float y1) {
		c.setPosition(c.getPosition()[0], y1);
	}
}