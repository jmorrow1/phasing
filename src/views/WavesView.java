package views;

import geom.LinearPath;
import geom.Point;
import geom.Rect;
import phases.Phrase;
import processing.core.PApplet;

public class WavesView extends View {
	private LinearPath a, b;
	private float pixelsPerWholeNote;
	private boolean movementRelativeToPathA;
	
	public WavesView(Rect rect, Phrase phrase, int color1, int color2, int opacity, 
			float lowYAmt, float highYAmt, boolean movementRelativeToPathA, PApplet pa) {
		super(rect, phrase, color1, color2, opacity, pa);
		
		this.movementRelativeToPathA = movementRelativeToPathA;
		
		float lowY = this.getY1() + PApplet.constrain(lowYAmt, 0, 1) * this.getHeight();
		float highY = this.getY1() + PApplet.constrain(highYAmt, 0, 1) * this.getHeight();
		a = makePlot(phrase, lowY, highY);
		b = new LinearPath(a);
		pixelsPerWholeNote = this.getWidth() / phrase.getTotalDuration();
	}

	@Override
	public void update(float dNotept1, float dNotept2) {
		pa.noFill();
		
		if (!movementRelativeToPathA) {
			pa.stroke(color1);
			update(a, dNotept1);
			pa.stroke(color2);
			update(b, dNotept2);
		}
		else {
			pa.stroke(color1);
			a.display(pa);
			pa.stroke(color2);
			update(b, dNotept2 - dNotept1);
		}
	}
	
	private void update(LinearPath lp, float dNotept) {
		float dx = dNotept * pixelsPerWholeNote;
		lp.translate(dx, 0);
		lp.display(pa);
	}
	
	private LinearPath makePlot(Phrase phrase, float lowY, float highY) {
		//determine min and max pitch values of phrase
		float minPitch = Float.MAX_VALUE;
		float maxPitch = Float.MIN_VALUE;
		for (int i=0; i<phrase.getNumNotes(); i++) {
			if (phrase.getPitch(i) < minPitch) {
				minPitch = phrase.getPitch(i);
			}
			if (phrase.getPitch(i) > maxPitch) {
				maxPitch = phrase.getPitch(i);
			}
		}
		
		//convert each pitch value to a point
		float lowX = this.getX1();
		float highX = this.getX2();
		Point[] pts = new Point[phrase.getNumNotes()];
		float x = lowX;
		float dx = (highX-lowX) / phrase.getNumNotes();
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(x, PApplet.map(phrase.getPitch(i), minPitch, maxPitch, lowY, highY));
			x += dx;
		}
		
		return new LinearPath(pts);
	}
}
