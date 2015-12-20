package views;

import geom.Rect;
import geom.Sector;
import phases.Phrase;
import processing.core.PApplet;

public class CircularView extends View {
	private Sector[] sectors1, sectors2;
	private float radiansPerNoteTime;

	public CircularView(Rect rect, Phrase phrase, int color1, int color2, int opacity, PApplet pa, float bpm1, float bpm2) {
		super(rect, phrase, color1, color2, opacity, 0, pa);
		
		float r1 = 0.4f * PApplet.min(this.getWidth(), this.getHeight());
		float r2 = 0.4f * PApplet.min(this.getWidth(), this.getHeight());
		float sectorThickness = 0.05f * PApplet.min(this.getWidth(), this.getHeight());
		
		sectors1 = new Sector[phrase.getNumNotes()];
		sectors2 = new Sector[phrase.getNumNotes()];
		
		float notept = 0;
		for (int i=0; i<phrase.getNumNotes(); i++) {
			float a1 = PApplet.map(notept, 0, phrase.getTotalDuration(), 0, PApplet.TWO_PI);
			notept += phrase.getSCDuration(i);
			float a2 = PApplet.map(notept, 0, phrase.getTotalDuration(), 0, PApplet.TWO_PI);
			sectors1[i] = new Sector(this.getCenx(), this.getCeny(), r1, r1 + sectorThickness, a1, a2);
			sectors2[i] = new Sector(this.getCenx(), this.getCeny(), r2, r2 + sectorThickness, a1, a2);
		}
		
		radiansPerNoteTime = PApplet.TWO_PI / phrase.getTotalDuration();
	}

	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
		//rotate sectors
		rotate(sectors2, radiansPerNoteTime * (dNotept2 - dNotept1));
		
		//display sectors
		pa.noFill();
		pa.strokeWeight(3);
		pa.stroke(color1, opacity);
		display(sectors1);
		pa.stroke(color2, opacity);
		display(sectors2);
	}
	
	private void display(Sector[] sectors) {
		for (int i=0; i<sectors.length; i++) {
			sectors[i].display(pa);
		}
	}
	
	private void rotate(Sector[] sectors, float dAngle) {
		for (int i=0; i<sectors.length; i++) {
			sectors[i].rotate(dAngle);
		}
	}

	@Override
	public int numPresets() {
		return 1;
	}

	@Override
	public void loadPreset(int preset) {
		
	}
}
