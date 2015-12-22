package generic_views;

import geom.Point;
import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;
import processing.core.PApplet;

public class One extends Rect {
	private PhasesPApplet pa;
	
	//bounds
	private float halfWidth, halfHeight;
	
	//color data
	int opacity;
	
	//scrolling or rotating movement:
	private float movementAcc1=0, movementAcc2=0, dNoteptAcc=0;
	private float pixelsPerNoteTime, radiansPerNoteTime;
	
	//options:
	private final int SCROLLS=0, ROTATES=1;
	private int movementType = ROTATES;
	
	private final int RELATIVE=0, FIXED=1;
	private int cameraType = RELATIVE;
	
	private final int SYMBOLS=0, DOTS=1, CONNECTED_DOTS=2, RECTS_OR_SECTORS=3, SINE_WAVE=4;
	private int phraseGraphicType = DOTS;
	
	private boolean doPlotPitch = false;
	
	private final int MONOCHROME=0, DIACHROME=1;
	private int colorSchemeType;

	public One(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect);
		this.opacity = opacity;
		this.pa = pa;
		
		halfWidth = this.getWidth()*0.45f;
		halfHeight = this.getHeight()*0.45f;
	
		pixelsPerNoteTime = this.getWidth() / pa.phrase.getTotalDuration();
		radiansPerNoteTime = PApplet.TWO_PI / pa.phrase.getTotalDuration();
	}

	public void update(float dNotept1, float dNotept2, int sign) {
		if (cameraType == RELATIVE) {
			dNotept2 = (dNotept2 - dNotept1) + dNoteptAcc;
			dNotept1 = 0;
			
			if ( (dNotept2 < 0 && sign > 0) || (dNotept2 > 0 && sign < 0) ) {
				dNoteptAcc = dNotept2;
				dNotept2 = 0;
			}
			else {
				dNoteptAcc = 0;
			}
		}
		
		pa.translate(this.getCenx(), this.getCeny());
		
		//draw graphics for player 1
		pa.pushMatrix();
			movementAcc1 += changeInMovement(dNotept1);
			transform(movementAcc1);
			pa.noStroke();
			pa.fill(pa.getColor1(), opacity);
			iteratePoints(pa.getBPM1());
		pa.popMatrix();
		
		//draw graphics for player 2
		pa.pushMatrix();
			movementAcc2 += changeInMovement(dNotept2);
			transform(movementAcc2);
			pa.noStroke();
			pa.fill(pa.getColor2(), opacity);
			iteratePoints(pa.getBPM2());
		pa.popMatrix();
	}
	
	private float changeInMovement(float dNotept) {
		switch(movementType) {
			case SCROLLS: return pixelsPerNoteTime * dNotept;
			case ROTATES: return radiansPerNoteTime * dNotept;
			default: return -1;
		}
	}
	
	private void transform(float movementAcc) {
		switch(movementType) {
			case SCROLLS: pa.translate(movementAcc, 0); break;
			case ROTATES: pa.rotate(movementAcc); break;
		}
	}
	
	private void iteratePoints(float bpm) {	
		if (movementType == SCROLLS) {
			float x = -halfWidth;
			float dx = this.getWidth() / pa.phrase.getGridRowSize();
			
			for (int i=0; i<pa.phrase.getGridRowSize(); i++) {
				if (pa.phrase.getNoteType(i) == Phrase.NOTE_START) {
					if (doPlotPitch) {
						float y = PApplet.map(pa.phrase.getSCPitch(i),
								              pa.phrase.minPitch(), pa.phrase.maxPitch(),
								              halfHeight, -halfHeight);
						drawNoteGraphic(x, y);
					}
					else {
						float y = 0;
						drawNoteGraphic(x, y);
					}
				}
				x += dx;
			}
		}
		else {
			float radius = 100;
			
			float theta = -PApplet.HALF_PI;
			float dTheta = PApplet.TWO_PI / pa.phrase.getNumNotes();
			
			for (int i=0; i<pa.phrase.getGridRowSize(); i++) {
				if (pa.phrase.getNoteType(i) == Phrase.NOTE_START) {
					float x = PApplet.cos(theta)*radius;
					float y = PApplet.sin(theta)*radius;
					if (doPlotPitch) {
						y *= PApplet.map(pa.phrase.getSCPitch(i),
							             pa.phrase.minPitch(), pa.phrase.maxPitch(),
							             1f, 1.5f);
					}
					drawNoteGraphic(x, y);
				}
				theta += dTheta;
			}
		}
	}
	
	private void drawNoteGraphic(float x, float y/*, float x2, float y2*/) {
		switch (phraseGraphicType) {
			case SYMBOLS:
				break;
			case DOTS:
				pa.ellipse(x, y, 20, 20);
				break;
			case CONNECTED_DOTS:
				break;
			case RECTS_OR_SECTORS:
				if (movementType == SCROLLS) {
					
				}
				else {
					
				}
				break;
		}
	}
}