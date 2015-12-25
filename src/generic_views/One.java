package generic_views;

import geom.Point;
import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;
import processing.core.PApplet;

public class One extends Rect {
	private PhasesPApplet pa;
	
	//bounds:
	private float width, height, halfWidth, halfHeight;
	private float minRadius, maxRadius;
	
	//color data:
	private int opacity;
	
	//scrolling or rotating movement:
	private float movementAcc1=0, movementAcc2=0, dNoteptAcc=0;
	private float pixelsPerNoteTime, radiansPerNoteTime;
	
	//options:
	private final int SCROLLS=0, ROTATES=1;
	private int movementType = SCROLLS;
	
	private final int RELATIVE=0, FIXED=1;
	private int cameraType = RELATIVE;
	
	private final int SYMBOLS=0, DOTS=1, CONNECTED_DOTS=2, RECTS_OR_SECTORS=3, SINE_WAVE=4;
	private int phraseGraphicType = RECTS_OR_SECTORS;
	
	private boolean doPlotPitch = true;
	
	private final int MONOCHROME=0, DIACHROME=1;
	private int colorSchemeType;

	public One(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect);
		this.opacity = opacity;
		this.pa = pa;
		
		width = this.getWidth()*0.9f;
		height = this.getHeight()*0.5f;
		halfWidth = width*0.5f;
		halfHeight = height*0.5f;
		minRadius = 100;
		maxRadius = 200;
	
		pixelsPerNoteTime = this.getWidth() / pa.phrase.getTotalDuration();
		radiansPerNoteTime = PApplet.TWO_PI / pa.phrase.getTotalDuration();
		
		onEnter();
	}
	
	public void onEnter() {
		pa.textAlign(pa.CENTER, pa.CENTER);
		pa.textSize(42);
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
			styleNoteGraphics(pa.getColor1());
			drawPhraseGraphic(pa.getBPM1());
		pa.popMatrix();
		
		//draw graphics for player 2
		pa.pushMatrix();
			movementAcc2 += changeInMovement(dNotept2);
			transform(movementAcc2);
			styleNoteGraphics(pa.getColor2());
			drawPhraseGraphic(pa.getBPM2());
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
	
	private void styleNoteGraphics(int color) {
		switch (phraseGraphicType) {
			case SYMBOLS:
				pa.noStroke();
				pa.fill(color, opacity);
				break;
			case DOTS:
				pa.noStroke();
				pa.fill(color, opacity);
				break;
			case CONNECTED_DOTS:
				pa.stroke(color, opacity);
				pa.fill(color, opacity);
				break;
			case RECTS_OR_SECTORS:
				if (movementType == SCROLLS) {
					pa.stroke(0, opacity);
					pa.fill(color, opacity);
				}
				else if (movementType == ROTATES) {
					pa.stroke(color, opacity);
					pa.noFill();
				}
				break;
			
		}
	}
	
	private float mapPitch(int i, float newMin, float newMax) {
		if (doPlotPitch) {
			return PApplet.map(pa.phrase.getSCPitch(i),
                               pa.phrase.minPitch(), pa.phrase.maxPitch(),
                               newMin, newMax);
		}
		else {
			return 1;
		}
	}
	
	private void drawPhraseGraphic(float bpm) {
		for (int i=0; i<pa.phrase.getNumNotes(); i++) {
			drawNoteGraphic(i);
		}
	}
	
	private void drawNoteGraphic(int noteIndex) {
		if (movementType == SCROLLS) {
			float x1 = PApplet.map(noteIndex, 0, pa.phrase.getNumNotes(), -halfWidth, halfWidth);
			float y = mapPitch(noteIndex, halfHeight, -halfHeight);
			float x2 = PApplet.map(noteIndex+1, 0, pa.phrase.getNumNotes(), -halfWidth, halfWidth);
			
			drawNoteGraphic(noteIndex, x1, y, x2);
		}
		else if (movementType == ROTATES) {
			float alpha = pa.map(noteIndex, 0, pa.phrase.getNumNotes(), 0, pa.TWO_PI) - pa.HALF_PI;
			float beta = alpha + pa.TWO_PI/pa.phrase.getNumNotes();
			
			float x1 = PApplet.cos(alpha)*mapPitch(noteIndex, minRadius, maxRadius);
			float y = PApplet.sin(alpha)*mapPitch(noteIndex, minRadius, maxRadius);
			float x2 = PApplet.cos(beta)*mapPitch(noteIndex, minRadius, maxRadius);
			
			drawNoteGraphic(noteIndex, x1, y, x2);
		}
	}
	
	private void drawNoteGraphic(int index, float x1, float y1, float x2) {
		switch (phraseGraphicType) {
			case SYMBOLS:
				pa.pushMatrix();
					pa.translate(x1, y1);
					float theta = pa.map(index, 0, pa.phrase.getGridRowSize(), 0, pa.TWO_PI);
					pa.rotate(theta);
					int pitch = (int) (pa.phrase.getGridPitch(index) % 12);
					String symbol = pa.scale.getNoteName(pitch);
					pa.text(symbol, 0, 0);
				pa.popMatrix();
				
				break;
			case DOTS:
				pa.ellipse(x1, y1, 20, 20);
				break;
			case CONNECTED_DOTS:
				break;
			case RECTS_OR_SECTORS:
				if (movementType == SCROLLS) {
					pa.rectMode(pa.CORNERS);
					pa.rect(x1, y1, x2, y1 + 20);
				}
				else {
					float alpha = pa.map(index, 0, pa.phrase.getGridRowSize(), 0, pa.TWO_PI);
					float beta = pa.map(index+1, 0, pa.phrase.getGridRowSize(), 0, pa.TWO_PI);
					float radius = mapPitch(index, minRadius, maxRadius);
					pa.ellipseMode(pa.RADIUS);
					pa.arc(0, 0, radius-10, radius-10, alpha, beta);
					pa.line(pa.cos(alpha)*(radius-10), pa.sin(alpha)*(radius-10),
							pa.cos(alpha)*(radius+10), pa.sin(alpha)*(radius+10));
					pa.line(pa.cos(beta)*(radius-10), pa.sin(beta)*(radius-10),
							pa.cos(beta)*(radius+10), pa.sin(beta)*(radius+10));
					pa.arc(0, 0, radius+10, radius+10, alpha, beta);
				}
				break;
		}
	}
}