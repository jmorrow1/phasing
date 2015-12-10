package views;

import geom.LinearPlot;
import geom.Rect;
import geom.SineWave;
import geom.Wave;
import phases.PhasesPApplet;
import phases.Phrase;
import processing.core.PApplet;

public class WavesView extends View {
	private Wave a, b;
	private Wave c, d;
	private double phraseRate, quarterNoteRate;
	private boolean movementRelativeToPathA;
	public final static int LINEAR_PLOT = 0, SINE_WAVE = 1;
	
	public WavesView(Rect rect, Phrase phrase, int color1, int color2, int opacity, 
			double amp1Amt, double amp2Amt, boolean movementRelativeToPathA, int waveType, PhasesPApplet pa) {
		super(rect, color1, color2, opacity, pa);
		
		this.movementRelativeToPathA = movementRelativeToPathA;
		
		double amp1 = this.getY1() + PhasesPApplet.constrain(amp1Amt, 0, 0.5f) * this.getHeight();
		double amp2 = this.getY1() + PhasesPApplet.constrain(amp2Amt, 0, 0.5f) * this.getHeight();
		
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
	public void update(double dNotept1, double dNotept2) {
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
	
	private void update(Wave w, double dx, int color, int opacity) {
		w.translate(dx);
		w.display(pa, color, opacity);
	}
	
	private LinearPlot makePlot(Phrase phrase, double amp) {
		//determine min and max pitch values of phrase
		double minPitch = phrase.minPitch();
		double maxPitch = phrase.maxPitch();
		
		//convert each pitch value to a point
		double ycen = this.getCeny();
		double[] ys = new double[phrase.getNumNotes()];
		for (int i=0; i<ys.length; i++) {
			ys[i] = PhasesPApplet.map(phrase.getPitch(i), minPitch, maxPitch, ycen + amp, ycen - amp);
		}
		
		return new LinearPlot(ys, this.getX1(), this.getX2());
	}
}
