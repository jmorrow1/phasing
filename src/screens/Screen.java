package screens;

import phasing.PhasesPApplet;
import processing.event.MouseEvent;

/**
 * An abstraction that draws things to the canvas and receives input events,
 * when the PhasesPApplet lets it do those actions.
 * 
 * @author James Morrow
 *
 */
public abstract class Screen {
	protected PhasesPApplet pa;
	
	/**
	 * 
	 * @param pa The PhasesPApplet on which to draw
	 */
	public Screen(PhasesPApplet pa) {
		this.pa = pa;		
	}
	
	/**
	 * Method that responds to mouse pressed events.
	 */
	public void mousePressed() {}
	/**
	 * Method that responds to mouse released events.
	 */
	public void mouseReleased() {}
	/**
	 * Method that responds to mouse dragged events.
	 */
	public void mouseDragged() {}
	/**
	 * Method that responds to mouse moved events.
	 */
	public void mouseMoved() {}
	/**
	 * Method that responds to key pressed events.
	 */
	public void keyPressed() {}
	/**
	 * Method that responds to key released events.
	 */
	public void keyReleased() {}
	/**
	 * Method that responds to scroll wheel events.
	 */
	public void mouseWheel(MouseEvent event) {}
	/**
	 * Method that responds to the event of this screen becoming active.
	 */
	public abstract void onEnter();
	/**
	 * Method that responds to the event of this screen becoming inactive.
	 */
	public abstract void onExit();
	/**
	 * Method that draws things to the PhasesPApplet.
	 */
	public abstract void draw();
}
