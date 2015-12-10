package views;

import geom.Circle;
import geom.Rect;
import phases.Phrase;
import processing.core.PApplet;

public class SectorView extends View {
	private Circle circle;
	private String[] noteNames = new String[12];
	private float[] angles = new float[12];
	private final float SIXTH_OF_PI = PApplet.TWO_PI / 12f;
	private PhraseReader readerA, readerB;
	int blendedColor;
	
	public SectorView(Rect rect, Phrase phrase, int color1, int color2, int opacity, PApplet pa) {
		super(rect, color1, color2, opacity, pa);;
		
		circle = new Circle(this.getCenx(), this.getCeny(), 0.375f*PApplet.min(this.getWidth(), this.getHeight()));
		
		float theta = 0;
		float dTheta = PApplet.TWO_PI / 12f;
		for (int i=0; i<12; i++) {
			noteNames[i] = Phrase.convertPitch(i+21+6, true);
			angles[i] = theta;
			theta += dTheta;
		}
		
		readerA = new PhraseReader(phrase);
		readerB = new PhraseReader(phrase);
		
		blendedColor = pa.lerpColor(color1, color2, 0.5f);
	}

	@Override
	public void update(float dNotept1, float dNotept2) {
		//draw circle
		pa.stroke(0);
		pa.noFill();
		circle.display(pa);
		//draw dot in center of circle
		pa.fill(0);
		pa.ellipseMode(pa.CENTER);
		pa.ellipse(circle.getCenx(), circle.getCeny(), 5, 5);
		//draw note names around circle
		drawNoteNames();
		//animate circle with arcs
		readerA.update(dNotept1);
		int indexA = readerA.currentPitchMod12();
		readerB.update(dNotept2);
		int indexB = readerB.currentPitchMod12();
		
		pa.noStroke();
		pa.fill(blendedColor);
		pa.ellipseMode(pa.CENTER);
		pa.arc(circle.getCenx(), circle.getCeny(), circle.getDiam(), circle.getDiam(),
				angles[indexA], angles[indexB]);
		
	}
	
	private void drawNoteNames() {
		pa.textAlign(pa.CENTER, pa.BOTTOM);
		pa.textSize(24);

		float angle = 0;
		float dAngle = PApplet.TWO_PI / 12;
		float radius = circle.getRadius();
		
		for (int i=0; i<12; i++) {
			pa.pushMatrix();
				pa.translate(circle.getCenx() + PApplet.cos(angle)*radius,
						     circle.getCeny() + PApplet.sin(angle)*radius);
				pa.rotate(angle + PApplet.HALF_PI);
				pa.text(noteNames[i], 0, 0);
				angle += dAngle;
			pa.popMatrix();
		}
	}
	
	private class PhraseReader {
		int noteIndex;
		float noteTimeTillNextNote;
		Phrase phrase;
		
		int currentPitchMod12;
		
		PhraseReader(Phrase phrase) {
			noteIndex = 0;
			currentPitchMod12 = phrase.getPitch(noteIndex) % 12;
			this.phrase = phrase;
			this.noteTimeTillNextNote = phrase.getDuration(noteIndex);
		}
		
		void update(float dNotept) {
			noteTimeTillNextNote -= dNotept;
			
			if (noteTimeTillNextNote <= 0) {
				noteIndex = (noteIndex+1) % phrase.getNumNotes();
				noteTimeTillNextNote = noteTimeTillNextNote + phrase.getDuration(noteIndex);
				currentPitchMod12 = phrase.getPitch(noteIndex) % 12;
			}
		}

		int currentPitchMod12() {
			return currentPitchMod12;
		}
	}
}