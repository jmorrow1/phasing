package views;

import java.lang.reflect.Method;

import geom.Point;
import geom.Rect;
import phases.PhasesPApplet;
import phases.PhraseReader;
import processing.core.PApplet;

public class PhaseShifter extends View {
	//starting pitch:
	private int startingPitch=0;
	
	//bounds:
	private float width, height, halfWidth, halfHeight;
	private float minRadius, maxRadius;
	
	//scrolling or rotating movement:
	private float movementAcc1=0, movementAcc2=0, dNoteptAcc=0;
	private float pixelsPerNoteTime, radiansPerNoteTime;
	
	//phrase readers:
	PhraseReader readerA, readerB;
	private final int ONE_ID = 1, TWO_ID = 2;
	
	//active note:
	int activeNote1, activeNote2;
	
	//options:
	private boolean showActiveNote = true;
	
	private final int SCROLLS=0, ROTATES=1;
	private int movementType = ROTATES;
	
	private final int RELATIVE=0, FIXED=1;
	private int cameraType = RELATIVE;
	
	private final int SYMBOLS=0, DOTS=1, CONNECTED_DOTS=2, RECTS_OR_SECTORS=3, SINE_WAVE=4;
	private int phraseGraphicType = DOTS;
	
	private boolean doPlotPitch = true;
	
	private final int MONOCHROMATIC=0, DIACHROMATIC=1;
	private int colorSchemeType = DIACHROMATIC;

	public PhaseShifter(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect, opacity, pa);
		this.pa = pa;
		
		width = this.getWidth()*0.9f;
		height = this.getHeight()*0.5f;
		halfWidth = width*0.5f;
		halfHeight = height*0.5f;
		minRadius = 100;
		maxRadius = 200;
	
		pixelsPerNoteTime = width / pa.phrase.getTotalDuration();
		radiansPerNoteTime = PApplet.TWO_PI / pa.phrase.getTotalDuration();
		
		initPhraseReaders();
		
		onEnter();
	}
	
	private void initPhraseReaders() {
		try {
			Method callback = PhaseShifter.class.getMethod("changeActiveNote", PhraseReader.class);
			readerA = new PhraseReader(pa.phrase, ONE_ID, this, callback);
			readerB = new PhraseReader(pa.phrase, TWO_ID, this, callback);

		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public void onEnter() {
		pa.textAlign(pa.CENTER, pa.CENTER);
		pa.textSize(42);
	}
	
	//callback:
	public void changeActiveNote(PhraseReader reader) {
		if (reader.getId() == ONE_ID) {
			activeNote1 = reader.getNoteIndex();
		}
		else if (reader.getId() == TWO_ID) {
			activeNote2 = reader.getNoteIndex();
		}
	}

	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
		readerA.update(dNotept1);
		readerB.update(dNotept2);
		
		if (cameraType == RELATIVE) {
			dNotept2 = (dNotept1 - dNotept2) + dNoteptAcc;
			dNotept1 = 0;
			
			if ( (dNotept2 < 0 && sign < 0) || (dNotept2 > 0 && sign > 0) ) {
				dNoteptAcc = dNotept2;
				dNotept2 = 0;
			}
			else {
				dNoteptAcc = 0;
			}
		}
		
		pa.pushMatrix();
		
		pa.translate(this.getCenx(), this.getCeny());
		
		//draw graphics for player 1
		pa.pushMatrix();
			movementAcc1 = incrementMovement(movementAcc1, dNotept1);
			transform(movementAcc1);
			drawPhraseGraphic(activeNote1, (this.colorSchemeType == DIACHROMATIC) ? pa.getColor1() : 0, pa.getBPM1());
		pa.popMatrix();
		
		//draw graphics for player 2
		pa.pushMatrix();
			movementAcc2 = incrementMovement(movementAcc2, dNotept2);
			transform(movementAcc2);
			drawPhraseGraphic(activeNote2, (this.colorSchemeType == DIACHROMATIC) ? pa.getColor2() : 0, pa.getBPM2());
		pa.popMatrix();
		
		pa.popMatrix();
	}
	
	private float incrementMovement(float movementAcc, float dNotept) {
		switch(movementType) {
		case SCROLLS: return PhasesPApplet.remainder(movementAcc + pixelsPerNoteTime * dNotept, width);
		case ROTATES: return movementAcc + radiansPerNoteTime * dNotept;
		default: return -1;
		}
	}

	private void transform(float movementAcc) {
		switch(movementType) {
			case SCROLLS: pa.translate(movementAcc, 0); break;
			case ROTATES: pa.rotate(movementAcc); break;
		}
	}
	
	private void styleNoteGraphics(int color, boolean activeStyle) {
		switch (phraseGraphicType) {
			case SYMBOLS:
				pa.noStroke();
				if (activeStyle) {
					pa.fill(color);
				}
				else {
					pa.fill(color, opacity);
				}
				break;
			case DOTS:
				pa.noStroke();
				if (activeStyle) {
					pa.fill(color);
				}
				else {
					pa.fill(color, opacity);
				}
				break;
			case CONNECTED_DOTS:
				if (activeStyle) {
					pa.fill(color);
					pa.stroke(color);
				}
				else {
					pa.fill(color, opacity);
					pa.stroke(color, opacity);
				}
				break;
			case RECTS_OR_SECTORS:
				if (movementType == SCROLLS) {
					if (activeStyle) {
						pa.stroke(0);
						pa.fill(color);
					}
					else {
						pa.stroke(0, opacity);
						pa.fill(color, opacity);
					}
				}
				else if (movementType == ROTATES) {
					if (activeStyle) {
						pa.stroke(color);
					}
					else {
						pa.stroke(color, opacity);
					}
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
	
	private void drawPhraseGraphic(int activeNote, int color, float bpm) {
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
			styleNoteGraphics(color, false);
			for (int i=0; i<pa.phrase.getNumNotes(); i++) {
				if (showActiveNote && i == activeNote) {
					styleNoteGraphics(color, true);
					drawNoteGraphic(i);
					styleNoteGraphics(color, false);
				}
				else {
					drawNoteGraphic(i);
				}
				
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
			float x = PApplet.map(noteIndex, 0, pa.phrase.getNumNotes(), -halfWidth, halfWidth);
			float y = mapPitch(noteIndex, halfHeight, -halfHeight);
			return new Point(x, y);
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
				if (movementType == ROTATES) {
					float theta = pa.map(index, 0, pa.phrase.getGridRowSize(), 0, pa.TWO_PI);
					pa.rotate(theta);
				}
				int pitch = (int) (pa.phrase.getGridPitch(index) % 12);
				String symbol = pa.scale.getNoteName(pitch);
				pa.text(symbol, 0, 0);
				if (movementType == SCROLLS) {
					pa.text(symbol, width, 0);
					pa.text(symbol, -width, 0);
				}
			pa.popMatrix();
		}
		else if (phraseGraphicType == DOTS) {
			Point a = getNoteGraphicPoint(index);
			pa.ellipse(a.x, a.y, 20, 20);
			if (movementType == SCROLLS) {
				pa.ellipse(a.x - width, a.y, 20, 20);
				pa.ellipse(a.x + width, a.y, 20, 20);
			}
		}
		else if (phraseGraphicType == CONNECTED_DOTS) {
			Point a = getNoteGraphicPoint(index);
			Point b = getNoteGraphicPoint(index+1);
			pa.ellipse(a.x, a.y, 20, 20);
			pa.line(a.x, a.y, b.x, b.y);
			if (movementType == SCROLLS) {
				pa.ellipse(a.x - width, a.y, 20, 20);
				pa.line(a.x - width, a.y, b.x - width, b.y);
				pa.ellipse(a.x + width, a.y, 20, 20);
				pa.line(a.x + width, a.y, b.x + width, b.y);
			}
		}
		else if (phraseGraphicType == RECTS_OR_SECTORS) {
			if (movementType == SCROLLS) {
				Point a = getNoteGraphicPoint(index);
				Point b = getNoteGraphicPoint(index+1);
				pa.rectMode(pa.CORNERS);
				pa.rect(a.x, a.y, b.x, a.y + 20);
				pa.rect(a.x - width, a.y, b.x - width, a.y + 20);
				pa.rect(a.x + width, a.y, b.x + width, a.y + 20);
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