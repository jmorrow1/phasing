package phases;

import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class ColoredDot {
	public int id;
	public float x, y;
	public float diam;
	public int color;
	public float opacity;
	
	/**
	 * 
	 * @param x The center x-coordinate of the colored dot
	 * @param y The center y-coordinate of the colored dot
	 * @param diam The diameter of the colored dot
	 * @param color The color of the dot
	 * @param opacity The opacity of the dot
	 * @param id A (presumably) unique identifier
	 */
	public ColoredDot(float x, float y, float diam, int color, int opacity, int id) {
		this.x = x;
		this.y = y;
		this.diam = diam;
		this.color = color;
		this.opacity = opacity;
		this.id = id;
	}
	
	/**
	 * Displays the colored dot.
	 * @param pa The PApplet to draw to
	 */
	public void display(PApplet pa) {
		pa.fill(color, opacity);
		pa.ellipse(x, y, diam, diam);
	}
	
	/**
	 * Invokes Processing styling functions that pertain to any arbitrary colored dot.
	 * @param pa The PApplet to draw to
	 */
	public static void style(PApplet pa) {
		pa.ellipseMode(pa.CENTER);
		pa.noStroke();
	}
	
	/**
	 * 
	 * @return The object's identifier
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the dot's color to the new color.
	 * @param color The new color
	 */
	public void setColor(int color) {
		this.color = color;
	}
}
