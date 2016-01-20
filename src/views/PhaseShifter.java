package views;

import java.lang.reflect.Method;

import geom.Point;
import geom.Rect;
import phases.ModInt;
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
	private float translateAcc1, translateAcc2, rotateAcc1, rotateAcc2, dNoteptAcc=0;
	private float pixelsPerNoteTime, radiansPerNoteTime;
	
	//phrase readers:
	private PhraseReader readerA, readerB;
	private final int ONE_ID = 1, TWO_ID = 2;
	
	//active note:
	private int activeNote1, activeNote2;
	
	//options:
	public ModInt activeNoteMode = new ModInt(0, numActiveNoteModes, activeNoteModeName);
	public ModInt transformation = new ModInt(0, numTransformations, transformationName);
	public ModInt cameraMode = new ModInt(0, numCameraModes, cameraModeName);
	public ModInt noteGraphic = new ModInt(0, numNoteGraphics, noteGraphicName);
	public ModInt plotPitchMode = new ModInt(0, numWaysOfPlottingPitchOrNot, plotPitchModeName);
	public ModInt colorScheme = new ModInt(1, numColorSchemes, colorSchemeName);
	
	@Override
	public int numOptions() {
		return 6;
	}

	public PhaseShifter(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect, opacity, pa);
		this.pa = pa;
		
		width = this.getWidth();
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
		
		if (cameraMode.toInt() == RELATIVE_TO_1) {
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
		
		pa.pushMatrix();
		
		pa.translate(this.getCenx(), this.getCeny());
		
		updateTransformAccumulators(dNotept2, dNotept1);
		
		//draw graphics for player 1
		pa.pushMatrix();
			transform(1);
			drawPhraseGraphic(activeNote1, (colorScheme.toInt() == DIACHROMATIC) ? pa.getColor1() : 0, pa.getBPM1());
		pa.popMatrix();
		
		//draw graphics for player 2
		pa.pushMatrix();
			transform(2);
			drawPhraseGraphic(activeNote2, (colorScheme.toInt() == DIACHROMATIC) ? pa.getColor2() : 0, pa.getBPM2());
		pa.popMatrix();
		
		pa.popMatrix();
	}
	
	private void updateTransformAccumulators(float dNotept1, float dNotept2) {
		translateAcc1 = PhasesPApplet.remainder(translateAcc1 + pixelsPerNoteTime * dNotept1, width);
		translateAcc2 = PhasesPApplet.remainder(translateAcc2 + pixelsPerNoteTime * dNotept2, width);
		rotateAcc1 = rotateAcc1 + radiansPerNoteTime * dNotept1;
		rotateAcc2 = rotateAcc2 + radiansPerNoteTime * dNotept2;
	}

	private void transform(int playerNum) {
		if (playerNum == 1) {
			transform(translateAcc1, rotateAcc1);
		}
		else if (playerNum == 2) {
			transform(translateAcc2, rotateAcc2);
		}
	}
	
	private void transform(float translateAmt, float rotateAmt) {
		switch(transformation.toInt()) {
			case TRANSLATE: pa.translate(translateAmt, 0); break;
			case ROTATE: pa.rotate(rotateAmt); break;
		}
	}
	
	private void styleNoteGraphics(int color, boolean activeStyle) {
		switch (noteGraphic.toInt()) {
			case SYMBOLS:
				pa.textAlign(pa.CENTER, pa.CENTER);
				pa.textSize(42);
				pa.textFont(pa.pfont42);
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
				if (transformation.toInt() == TRANSLATE) {
					if (activeStyle) {
						pa.stroke(0);
						pa.fill(color);
					}
					else {
						pa.stroke(0, opacity);
						pa.fill(color, opacity);
					}
				}
				else if (transformation.toInt() == ROTATE) {
					if (activeStyle) {
						pa.stroke(color);
					}
					else {
						pa.stroke(color, opacity);
					}
					pa.noFill();
				}
				
				break;
		}
	}
	
	private float mapPitch(int i, float newMin, float newMax) {
		i %= 12;
		if (plotPitchMode.toInt() == PLOT_PITCH && pa.phrase.minPitch() != pa.phrase.maxPitch()) {
			return PApplet.map(pa.phrase.getSCPitch(i),
                               pa.phrase.minPitch(), pa.phrase.maxPitch(),
                               newMin, newMax);
		}
		else {
			return (int)pa.lerp(newMin, newMax, 0.5f);
		}
	}
	
	private void drawPhraseGraphic(int activeNote, int color, float bpm) {
		if (noteGraphic.toInt()== SINE_WAVE) {
			pa.noFill();
			pa.stroke(color);
			if (transformation.toInt() == TRANSLATE) {
				drawSineWave();
			}
			else if (transformation.toInt() == ROTATE) {	
				int radius = (int)pa.map(bpm, pa.MIN_BPM, pa.MAX_BPM, 0, halfWidth);
				pa.ellipseMode(pa.RADIUS);
				pa.ellipse(0, 0, radius, radius);
			}
		}
		else {
			styleNoteGraphics(color, false);
			for (int i=0; i<pa.phrase.getNumNotes(); i++) {
				if ( (activeNoteMode.toInt() == SHOW_ACTIVE_NOTE || activeNoteMode.toInt() == ONLY_SHOW_ACTIVE_NOTE)
						&& i == activeNote) {
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
		float percentDuration = -1;
		
		if (noteIndex == pa.phrase.getNumNotes()) {
			noteIndex = 0;
			percentDuration = 1;
		}
		else if (noteIndex > pa.phrase.getNumNotes()) {
			noteIndex %= pa.phrase.getNumNotes();
		}
		else {
			percentDuration = pa.phrase.getPercentDurationOfSCIndex(noteIndex % pa.phrase.getNumNotes());
		}
		
		if (pa.phrase.getSCDynamic(noteIndex) <= 0) {
			return null;
		}
			
		
		if (transformation.toInt() == TRANSLATE) {
			float x = pa.map(percentDuration, 0, 1, -halfWidth, halfWidth);
			float y = mapPitch(noteIndex, halfHeight, -halfHeight);
			return new Point(x, y);
		}
		else {
			float theta = percentDuration * pa.TWO_PI - pa.HALF_PI;
			return new Point(PApplet.cos(theta)*mapPitch(noteIndex, minRadius, maxRadius),
					         PApplet.sin(theta)*mapPitch(noteIndex, minRadius, maxRadius));
		}
	}
	
	private void drawNoteGraphic(int index) {
		if (getNoteGraphicPoint(index) == null) {
		}
		else if (noteGraphic.toInt()== SYMBOLS) {
			Point a = getNoteGraphicPoint(index);
			pa.pushMatrix();
				pa.translate(a.x, a.y);
				if (transformation.toInt() == ROTATE) {
					float theta = pa.phrase.getPercentDurationOfSCIndex(index) * pa.TWO_PI;
					pa.rotate(theta);
				}
				int pitch = (int) (pa.phrase.getSCPitch(index) % 12);
				String symbol = pa.scale.getNoteNameByPitchValue(pitch);
				pa.text(symbol, 0, 0);
				if (transformation.toInt() == TRANSLATE) {
					pa.text(symbol, width, 0);
					pa.text(symbol, -width, 0);
				}
			pa.popMatrix();
		}
		else if (noteGraphic.toInt()== DOTS) {
			Point a = getNoteGraphicPoint(index);
			pa.ellipseMode(pa.CENTER);
			pa.ellipse(a.x, a.y, 20, 20);
			if (transformation.toInt() == TRANSLATE) {
				pa.ellipse(a.x - width, a.y, 20, 20);
				pa.ellipse(a.x + width, a.y, 20, 20);
			}
		}
		else if (noteGraphic.toInt()== CONNECTED_DOTS) {
			Point a = getNoteGraphicPoint(index);
			Point b = getNoteGraphicPoint(index+1);
			pa.ellipseMode(pa.CENTER);
			pa.ellipse(a.x, a.y, 20, 20);
			pa.line(a.x, a.y, b.x, b.y);
			if (transformation.toInt() == TRANSLATE) {
				pa.ellipse(a.x - width, a.y, 20, 20);
				pa.line(a.x - width, a.y, b.x - width, b.y);
				pa.ellipse(a.x + width, a.y, 20, 20);
				pa.line(a.x + width, a.y, b.x + width, b.y);
			}
		}
		else if (noteGraphic.toInt()== RECTS_OR_SECTORS) {
			if (transformation.toInt() == TRANSLATE) {
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