package generic_views;

import geom.Rect;

public abstract class View extends Rect {
	protected int opacity;
	
	public View(Rect rect, int opacity) {
		super(rect);
		this.opacity = opacity;
	}
	
	public abstract void update(float dNotept1, float dNotept2, int sign);
}
