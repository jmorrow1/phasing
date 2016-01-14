package views;

import java.util.Arrays;

import geom.Rect;
import phases.PhasesPApplet;

public abstract class View extends Rect {
	protected PhasesPApplet pa;
	protected int opacity;
	private int[][] configIds;
	
	public View(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect);
		this.opacity = opacity;
		this.pa = pa;
	}
	
	public abstract void update(float dNotept1, float dNotept2, int sign);
	
	public abstract int numOptions();
}
