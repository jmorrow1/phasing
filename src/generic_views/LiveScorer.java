package generic_views;

import geom.Rect;

public class LiveScorer extends View {
	
	//options:
	private final int SCROLLS=0, FADES=1;
	private int scrollsOrFades=SCROLLS;
	
	private boolean doPlotPitch=false;
	
	private final int DOTS=0, SYMBOLS=1, CONNECTED_DOTS=2, RECTS=3, SINE_WAVE_DOTS=4;
	private int noteType = DOTS;
	
	private final int MONOCHROME=0, DIACHROME=1;
	private int colorSchemeType=0;
	
	public LiveScorer(Rect rect, int opacity) {
		super(rect, opacity);
	}
	
	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
		
	}
}
