package views;

import geom.Rect;
import phasing.PhasesPApplet;
import phasing.PlayerInfo;
import util.ModInt;

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
	 * @param viewBox The area in which the view is drawn.
	 * @param opacity The opacity of things the view draws.
	 * @param playerInfo Contains information (potentially) about how to initialize the view's settings.
	 * @param pa The PApplet the view draws to.
	 */
	public View(Rect viewBox, int opacity, PlayerInfo playerInfo, PhasesPApplet pa) {
		super(viewBox);
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
	 * @param dt The number of milliseconds that have passed since the last update() invocation.
	 * @param dNotept1 The amount of time passed since the last update, in terms of how much music player 1 played.
	 * @param dNotept2 The amount of time passed since the last update, in terms of how much music player 2 played.
	 */
	public abstract void update(int dt, float dNotept1, float dNotept2);
	
	/**
	 * Allows the view to change its state in response to a change in one of its option variables.
	 */
	public void settingsChanged() {}
	
	/**
	 * Handles the event of this view being resized.
	 * This method is called AFTER the view has been resized.
	 * @param prevWidth The width of the View before the resizing occured.
	 * @param prevHeight The height of the View before the resizing occured.
	 */
	protected abstract void resized(float prevWidth, float prevHeight);
	
	/**
	 * Sets the width and height of this View.
	 * @param width
	 * @param height
	 * @param cenx
	 * @param ceny
	 */
	public void setSize(float cenx, float ceny, float width, float height) {
		float prevWidth = getWidth();
		float prevHeight = getHeight();
		super.setWidth(width);
		super.setHeight(height);
		super.setCenter(cenx, ceny);
		resized(prevWidth, prevHeight);
	}
	
	@Override
	public void setWidth(float width) {
		float prevWidth = getWidth();
		super.setWidth(width);
		resized(prevWidth, getHeight());
	}
	
	@Override
	public void setHeight(float height) {
		float prevHeight = getHeight();
		super.setHeight(height);
		resized(getWidth(), prevHeight);
	}
	
	/**
	 * Saves the settings of the view to the given playerInfo object.
	 * @param playerInfo
	 */
	public abstract void saveSettings(PlayerInfo playerInfo);
	
	/**
	 * Saves the option's value to the given playerInfo object under the given name.
	 * @param var The option.
	 * @param name The name.
	 * @param playerInfo The playerInfo object.
	 */
	protected void save(ModInt var, String name, PlayerInfo playerInfo) {
		playerInfo.viewOptionValueMap.put(name, var.toInt());
	}
	
	/**
	 * Changes the settings of the view to values given by the playerInfo object.
	 * @param playerInfo
	 */
	protected abstract void loadSettings(PlayerInfo playerInfo);
	
	/**
	 * Trys to set the given ModInt's value to a value in the playerInfo object.
	 * If the playerInfo object doesn't contain the desired value, then nothing happens.
	 * 
	 * @param var The ModInt.
	 * @param key The name of the value, used to look up the value in the playerInfo object.
	 * @param playerInfo The playerInfo object.
	 */
	protected void tryToSet(ModInt var, String key, PlayerInfo playerInfo) {
		if (playerInfo.viewOptionValueMap.containsKey(key)) {
			var.setValue(playerInfo.viewOptionValueMap.get(key));
		}
	}
}
