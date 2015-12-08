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
	private Wave c, d;
	private float phraseRate, quarterNoteRate;
	private boolean movementRelativeToPathA;
	public final static int LINEAR_PLOT = 0, SINE_WAVE = 1;
	
	public WavesView(Rect rect, Phrase phrase, int color1, int color2, int opacity, 
			float amp1Amt, float amp2Amt, boolean movementRelativeToPathA, int waveType, PApplet pa) {
		super(rect, phrase, color1, color2, opacity, pa);
		
		this.movementRelativeToPathA = movementRelativeToPathA;
		
		float amp1 = this.getY1() + PApplet.constrain(amp1Amt, 0, 0.5f) * this.getHeight();
		float amp2 = this.getY1() + PApplet.constrain(amp2Amt, 0, 0.5f) * this.getHeight();
		
		switch(waveType) {
			case LINEAR_PLOT: 
				a = makePlot(phrase, amp1);
				b = new LinearPlot((LinearPlot)a);
				//c = makePlot(phrase, amp2);
				//d = new LinearPlot((LinearPlot)c);
				break;
			case SINE_WAVE:
				a = new SineWave(this.getX1(), this.getX2(), this.getCeny(), this.getHeight()*0.45f);
				b = new SineWave((SineWave)a);
				//c = new SineWave(this.getX1(), this.getX2(), this.getCeny(), this.getHeight()*0.25f);
				//d = new SineWave(this.getX1(), this.getX2(), this.getCeny(), this.getHeight()*0.25f);
				break;
		}
		
		phraseRate = this.getWidth() / phrase.getTotalDuration();
		quarterNoteRate = this.getWidth() / 0.25f;
	}

	@Override
	public void update(float dNotept1, float dNotept2) {
		pa.strokeWeight(3);
		pa.noFill();
		
		if (!movementRelativeToPathA) {
			update(a, dNotept1 * phraseRate, color1, opacity);
			if (c != null) update(c, dNotept1 * quarterNoteRate, color1, opacity);
			update(b, dNotept2 * phraseRate, color2, opacity);
			if (d != null) update(d, dNotept2 * quarterNoteRate, color2, opacity);
			
			
		}
		else {
			a.display(pa, color1, opacity);
			if (c != null) c.display(pa, color1, opacity);
			pa.stroke(color2, opacity);
			update(b, (dNotept2 - dNotept1)*phraseRate, color2, opacity);
			if (d != null) update(d, (dNotept2 - dNotept1)*quarterNoteRate, color2, opacity);
		}
	}
	
	private void update(Wave w, float dx, int color, int opacity) {
		w.translate(dx, 0);
		w.display(pa, color, opacity);
	}
	
	private LinearPlot makePlot(Phrase phrase, float amp) {
		//determine min and max pitch values of phrase
		float minPitch = phrase.minPitch();
		float maxPitch = phrase.maxPitch();
		
		//convert each pitch value to a point
		float ycen = this.getCeny();
		float[] ys = new float[phrase.getNumNotes()];
		for (int i=0; i<ys.length; i++) {
			ys[i] = PApplet.map(phrase.getPitch(i), minPitch, maxPitch, ycen + amp, ycen - amp);
		}
		
		return new LinearPlot(ys, this.getX1(), this.getX2());
	}
}
