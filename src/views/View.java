package views;

import geom.Rect;
import phases.PhasesPApplet;

public abstract class View extends Rect {
	protected PhasesPApplet pa;
	protected int opacity;
	
	public View(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect);
		this.opacity = opacity;
		this.pa = pa;
	}
	
	public abstract void update(float dNotept1, float dNotept2, int sign);
	
	public abstract String showOptionValue(int index);
	public String getOptionConfigurationID() {
		String s = "";
		for (int i=0; i<numOptions(); i++) {
			s += showOptionValue(i);
		}
		return s;
	}
	public abstract int numOptions();
	public abstract void incrementOption(int index);
	public abstract void decrementOption(int index);
	public abstract String showOption(int index);
	public String showCurrentSettings() {
		String s = "[";
		for (int i=0; i<numOptions(); i++) {
			s += showOption(i);
			if (i != numOptions()-1) {
				s += ", ";
			}
		}
		s += "]";
		return s;
	}
}
