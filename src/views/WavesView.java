package views;

import geom.LinearPlot;
import geom.Point;
import geom.Rect;
import geom.SineWave;
import geom.Wave;
import phases.Phrase;
import processing.core.PApplet;

public class WavesView extends View {
	private Wave a, b;
	private float pixelsPerWholeNote;
	private boolean movementRelativeToPathA;
	public final static int LINEAR_PLOT = 0, SINE_WAVE = 1;
	
	public WavesView(Rect rect, Phrase phrase, int color1, int color2, int opacity, 
			float lowYAmt, float highYAmt, boolean movementRelativeToPathA, int waveType, PApplet pa) {
		super(rect, phrase, color1, color2, opacity, pa);
		
		this.movementRelativeToPathA = movementRelativeToPathA;
		
		float lowY = this.getY1() + PApplet.constrain(lowYAmt, 0, 1) * this.getHeight();
		float highY = this.getY1() + PApplet.constrain(highYAmt, 0, 1) * this.getHeight();
		
		switch(waveType) {
			case LINEAR_PLOT: 
				a = makePlot(phrase, lowY, highY);
				b = new LinearPlot((LinearPlot)a);
				break;
			case SINE_WAVE:
				a = new SineWave(this.getX1(), this.getX2(), this.getCeny(), this.getHeight()*0.25f);
				b = new SineWave((SineWave)a);
				break;
		}
		
		pixelsPerWholeNote = this.getWidth() / phrase.getTotalDuration();
	}

	@Override
	public void update(float dNotept1, float dNotept2) {
		pa.strokeWeight(3);
		pa.noFill();
		
		if (!movementRelativeToPathA) {
			pa.stroke(color1, opacity);
			update(a, dNotept1);
			pa.stroke(color2, opacity);
			update(b, dNotept2);
		}
		else {
			pa.stroke(color1, opacity);
			a.display(pa);
			pa.stroke(color2, opacity);
			update(b, dNotept2 - dNotept1);
		}
	}
	
	private void update(Wave w, float dNotept) {
		float dx = dNotept * pixelsPerWholeNote;
		w.translate(dx, 0);
		w.display(pa);
	}
	
	private LinearPlot makePlot(Phrase phrase, float lowY, float highY) {
		//determine min and max pitch values of phrase
		float minPitch = minPitch(phrase);
		float maxPitch = maxPitch(phrase);
		
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
		
		return new LinearPlot(pts);
	}
	
	private float minPitch(Phrase phrase) {
		float minPitch = Float.MAX_VALUE;
		for (int i=0; i<phrase.getNumNotes(); i++) {
			if (phrase.getPitch(i) < minPitch) {
				minPitch = phrase.getPitch(i);
			}
		}
		return minPitch;
	}
	
	private float maxPitch(Phrase phrase) {
		float maxPitch = Float.MIN_VALUE;
		for (int i=0; i<phrase.getNumNotes(); i++) {
			if (phrase.getPitch(i) > maxPitch) {
				maxPitch = phrase.getPitch(i);
			}
		}
		return maxPitch;
	}
}
