package views;

import java.util.Arrays;

import geom.Rect;
import phases.PhasesPApplet;

/**
 * 
 * @author James Morrow
 *
 */
public abstract class View extends Rect implements ViewVariableInfo {
	protected PhasesPApplet pa;
	protected int opacity;
	
	/**
	 * 
	 * @param rect The area in which the view is drawn.
	 * @param opacity The opacity of things the view draws.
	 * @param pa The PApplet the view draws to.
	 */
	public View(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect);
		this.opacity = opacity;
		this.pa = pa;
	}
	
	/**
	 * After a period of neglect (in which update has not been invoked), this method informs the view of the state of the phasing process, so the view can get in sync with it.
	 * This comes down to telling the view what part of the phrase player 1 is reading and what part of the phrase player 2 is reading.
	 * 
	 * @param notept1 The part of the phrase player 1 is currently reading.
	 * @param notept2 The part of the phrase player 2 is currently reading.
	 */
	public abstract void wakeUp(float notept1, float notept2);
	
	/**
	 * Updates the view.
	 * @param dNotept1 The amount of time passed since the last update, in terms of how much music player 1 played.
	 * @param dNotept2 The amount of time passed since the last update, in terms of how much music player 2 played.
	 */
	public abstract void update(float dNotept1, float dNotept2);
	
	public void respondToChangeInSettings() {}
}
