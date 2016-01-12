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
	
	public abstract int getValue(int index);
	public abstract int numOptions();
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
	public abstract int numValues(int optionVariableIndex);
	
	private int numPosConfigs() {
		int product = 1;
		for (int i=0; i<numOptions(); i++) {
			product *= numValues(i);
		}
		return product;
	}
}
