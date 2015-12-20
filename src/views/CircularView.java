package views;

import geom.Rect;
import phases.Phrase;
import processing.core.PApplet;

public class CircularView extends View {
	
	

	public CircularView(Rect rect, Phrase phrase, int color1, int color2, int opacity, int preset, PApplet pa) {
		super(rect, phrase, color1, color2, opacity, preset, pa);
	}

	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int numPresets() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void loadPreset(int preset) {
		// TODO Auto-generated method stub
		
	}

}
