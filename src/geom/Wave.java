package geom;

import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public abstract class Wave {
	/**
	 * Displays the wave with the specified color and opacity
	 * @param pa The PApplet to draw to
	 * @param color The color to draw the wave
	 * @param opacity The opacity of the wave
	 */
	public abstract void display(PApplet pa, int color, int opacity);
	/**
	 * Shifts the wave in the x-direction
	 * @param dx How much to displace the wave, in pixels
	 */
	public abstract void translate(float dx);
}