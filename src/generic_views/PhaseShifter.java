package generic_views;

import geom.Point;
import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;
import processing.core.PApplet;

public class PhaseShifter extends View {
	private PhasesPApplet pa;
	
	//starting pitch:
	private int startingPitch=0;
	
	//bounds:
	private float width, height, halfWidth, halfHeight;
	private float minRadius, maxRadius;
	
	//scrolling or rotating movement:
	private float movementAcc1=0, movementAcc2=0, dNoteptAcc=0;
	private float pixelsPerNoteTime, radiansPerNoteTime;
	
	//options:
	private final int SCROLLS=0, ROTATES=1;
	private int movementType = ROTATES;
	
	private final int RELATIVE=0, FIXED=1;
	private int cameraType = RELATIVE;
	
	private final int SYMBOLS=0, DOTS=1, CONNECTED_DOTS=2, RECTS_OR_SECTORS=3, SINE_WAVE=4;
	private int phraseGraphicType = SINE_WAVE;
	
	private boolean doPlotPitch = true;
	
	private final int MONOCHROMATIC=0, DIACHROMATIC=1;
	private int colorSchemeType = DIACHROMATIC;

	public PhaseShifter(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect, opacity);
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

	@Override
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
			styleNoteGraphics((this.colorSchemeType == DIACHROMATIC) ? pa.getColor1() : 0);
			drawPhraseGraphic(pa.getBPM1());
		pa.popMatrix();
		
		//draw graphics for player 2
		pa.pushMatrix();
			movementAcc2 += changeInMovement(dNotept2);
			transform(movementAcc2);
			styleNoteGraphics((this.colorSchemeType == DIACHROMATIC) ? pa.getColor2() : 0);
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
			case SINE_WAVE:
				pa.noFill();
				pa.stroke(color);
				break;
		}
	}
	
	private float mapPitch(int i, float newMin, float newMax) {
		i %= 12;
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
		if (phraseGraphicType == SINE_WAVE) {
			if (movementType == SCROLLS) {
				drawSineWave();
			}
			else if (movementType == ROTATES) {	
				int radius = (int)pa.map(bpm, pa.MIN_BPM, pa.MAX_BPM, 0, halfWidth);
				pa.ellipseMode(pa.RADIUS);
				pa.ellipse(0, 0, radius, radius);
			}
		}
		else {
			for (int i=0; i<pa.phrase.getNumNotes(); i++) {
				drawNoteGraphic(i);
			}
		}
	}
	
	private void drawSineWave() {
		pa.beginShape();
			float x = -halfWidth;
			float dx = 2;
			float angle = 0;
			float dAngle = pa.TWO_PI / (width / dx);
			while (x < halfWidth) {
				pa.vertex(x, halfHeight * PApplet.sin(angle));
				x += dx;
				angle += dAngle;
			}
			pa.vertex(x, halfHeight * PApplet.sin(pa.TWO_PI));
		
		pa.endShape();
	}
	
	private Point getNoteGraphicPoint(int noteIndex) {
		if (movementType == SCROLLS) {
			return new Point(PApplet.map(noteIndex, 0, pa.phrase.getNumNotes(), -halfWidth, halfWidth),
					         mapPitch(noteIndex, halfHeight, -halfHeight));
		}
		else {
			float theta = pa.map(noteIndex, 0, pa.phrase.getNumNotes(), 0, pa.TWO_PI) - pa.HALF_PI;
			return new Point(PApplet.cos(theta)*mapPitch(noteIndex, minRadius, maxRadius),
					         PApplet.sin(theta)*mapPitch(noteIndex, minRadius, maxRadius));
		}
	}
	
	private void drawNoteGraphic(int index) {
		if (phraseGraphicType == SYMBOLS) {
			Point a = getNoteGraphicPoint(index);
			pa.pushMatrix();
				pa.translate(a.x, a.y);
				float theta = pa.map(index, 0, pa.phrase.getGridRowSize(), 0, pa.TWO_PI);
				pa.rotate(theta);
				int pitch = (int) (pa.phrase.getGridPitch(index) % 12);
				String symbol = pa.chromaticScales.getScale(startingPitch).getNoteName(pitch);
				pa.text(symbol, 0, 0);
			pa.popMatrix();
		}
		else if (phraseGraphicType == DOTS) {
			Point a = getNoteGraphicPoint(index);
			pa.ellipse(a.x, a.y, 20, 20);
		}
		else if (phraseGraphicType == CONNECTED_DOTS) {
			Point a = getNoteGraphicPoint(index);
			Point b = getNoteGraphicPoint(index+1);
			pa.ellipse(a.x, a.y, 20, 20);
			pa.line(a.x, a.y, b.x, b.y);
		}
		else if (phraseGraphicType == RECTS_OR_SECTORS) {
			if (movementType == SCROLLS) {
				Point a = getNoteGraphicPoint(index);
				Point b = getNoteGraphicPoint(index+1);
				pa.rectMode(pa.CORNERS);
				pa.rect(a.x, a.y, b.x, a.y + 20);
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
		}
	}
}