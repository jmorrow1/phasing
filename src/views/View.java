package views;

import java.util.Arrays;

import geom.Rect;
import phases.PhasesPApplet;

public abstract class View extends Rect implements ViewVariableInfo {
	protected PhasesPApplet pa;
	protected int opacity;
	
	public View(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect);
		this.opacity = opacity;
		this.pa = pa;
	}
	public abstract void recalibrate(float notept1, float notept2);
	public abstract void onEnter();
	public abstract void update(float dNotept1, float dNotept2, int sign);
	public abstract int numOptions();
	public void updateState() {}
}
