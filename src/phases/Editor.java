package phases;

import geom.Rect;

public class Editor extends Screen {
	private final static int W=0xfffffff, B=0x0;
	private final static int[] keyColors = new int[] {W, B, W, B, W, W, B, W, B, W, B, W};
	private int numKeys = 48;
	private float keyWidth, keyHeight;
	
	public Editor(PhasesPApplet pa) {
		super(pa);
		keyWidth = 50;
		keyHeight = pa.height / ((float)numKeys);
	}

	@Override
	public void onEnter() {}

	@Override
	public void draw() {
		pa.background(255);
		
		float y = pa.height - keyHeight;
		pa.rectMode(pa.CORNER);
		for (int i=0; i<numKeys; i++) {
			//y-axis (piano)
			pa.stroke(0);
			pa.fill(keyColors[i % 12]);
			pa.rect(0, y, keyWidth, keyHeight);
			
			//horizontal lines
			pa.fill(keyColors[i % 12], 50);
			pa.line(keyWidth, y, pa.width, y);
			
			y -= keyHeight;
		}
		
		//vertical lines
		float x = keyWidth;
		while (x < pa.width) {
			pa.line(x, 0, x, pa.height);
			x += keyWidth;
		}
	}
}