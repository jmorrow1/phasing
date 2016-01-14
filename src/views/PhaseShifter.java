package views;

import java.lang.reflect.Method;

import geom.Point;
import geom.Rect;
import phases.Option;
import phases.OptionValue;
import phases.PhasesPApplet;
import phases.PhraseReader;
import phases.Option.ActiveNote;
import phases.Option.Camera;
import phases.Option.ColorScheme;
import phases.Option.NoteGraphic;
import phases.Option.PlotPitch;
import phases.Option.Transform;
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
	
	//options
	public OptionValue<ActiveNote> activeNote = new OptionValue<ActiveNote>(Option.activeNote(), 1);
	public OptionValue<Transform> transform = new OptionValue(Option.transform(), 0);
	public OptionValue<Camera> camera = new OptionValue(Option.camera(), 1);
	public OptionValue<NoteGraphic> noteGraphic = new OptionValue(Option.noteGraphic(), 0);
	public OptionValue<PlotPitch> plotPitch = new OptionValue(Option.plotPitch(), 0);
	public OptionValue<ColorScheme> colorScheme = new OptionValue(Option.colorScheme(), 0);
	
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
		
		if (camera.equals(Camera.RELATIVE_TO_1) || camera.equals(Camera.RELATIVE_TO_2)) {
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
			drawPhraseGraphic(activeNote1, (colorScheme.equals(ColorScheme.DIACHROME)) ? pa.getColor1() : 0, pa.getBPM1());
		pa.popMatrix();
		
		//draw graphics for player 2
		pa.pushMatrix();
			transform(2);
			drawPhraseGraphic(activeNote2, (colorScheme.equals(ColorScheme.DIACHROME)) ? pa.getColor2() : 0, pa.getBPM2());
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
		switch(transform.intValue()) {
			case Transform.TRANSLATE: pa.translate(translateAmt, 0); break;
			case Transform.ROTATE: pa.rotate(rotateAmt); break;
		}
	}
	
	private void styleNoteGraphics(int color, boolean activeStyle) {
		switch (noteGraphic.intValue()) {
			case NoteGraphic.SYMBOLS:
				pa.noStroke();
				if (activeStyle) {
					pa.fill(color);
				}
				else {
					pa.fill(color, opacity);
				}
				break;
			case NoteGraphic.DOTS:
				pa.noStroke();
				if (activeStyle) {
					pa.fill(color);
				}
				else {
					pa.fill(color, opacity);
				}
				break;
			case NoteGraphic.CONNECTED_DOTS:
				if (activeStyle) {
					pa.fill(color);
					pa.stroke(color);
				}
				else {
					pa.fill(color, opacity);
					pa.stroke(color, opacity);
				}
				break;
			case NoteGraphic.RECTS_OR_SECTORS:
				if (transform.equals(Transform.TRANSLATE)) {
					if (activeStyle) {
						pa.stroke(0);
						pa.fill(color);
					}
					else {
						pa.stroke(0, opacity);
						pa.fill(color, opacity);
					}
				}
				else if (transform.equals(Transform.ROTATE)) {
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
		if (plotPitch.equals(PlotPitch.PLOT_PITCH) && pa.phrase.minPitch() != pa.phrase.maxPitch()) {
			return PApplet.map(pa.phrase.getSCPitch(i),
                               pa.phrase.minPitch(), pa.phrase.maxPitch(),
                               newMin, newMax);
		}
		else {
			//TODO: Is this right? :
			return (int)pa.lerp(newMin, newMax, 0.5f);
		}
	}
	
	private void drawPhraseGraphic(int activeNoteIndex, int color, float bpm) {
		/*if (phraseGraphicType == SINE_WAVE) {
			pa.noFill();
			pa.stroke(color);
			if (movementType == SCROLLS) {
				drawSineWave();
			}
			else if (movementType == ROTATES) {	
				int radius = (int)pa.map(bpm, pa.MIN_BPM, pa.MAX_BPM, 0, halfWidth);
				pa.ellipseMode(pa.RADIUS);
				pa.ellipse(0, 0, radius, radius);
			}
		}
		else {*/
			styleNoteGraphics(color, false);
			for (int i=0; i<pa.phrase.getNumNotes(); i++) {
				if (activeNote.equals(ActiveNote.SHOW_ACTIVE_NOTE) && i == activeNoteIndex) {
					styleNoteGraphics(color, true);
					drawNoteGraphic(i);
					styleNoteGraphics(color, false);
				}
				else {
					drawNoteGraphic(i);
				}
				
			}
		//}
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
		if (transform.equals(Transform.TRANSLATE)) {
			float x = pa.map(pa.phrase.getPercentDurationOfSCIndex(noteIndex),
						0, 1, -halfWidth, halfWidth);
			float y = mapPitch(noteIndex, halfHeight, -halfHeight);
			return new Point(x, y);
		}
		else {
			float theta = pa.phrase.getPercentDurationOfSCIndex(noteIndex) * pa.TWO_PI - pa.HALF_PI;
			return new Point(PApplet.cos(theta)*mapPitch(noteIndex, minRadius, maxRadius),
					         PApplet.sin(theta)*mapPitch(noteIndex, minRadius, maxRadius));
		}
	}
	
	private void drawNoteGraphic(int index) {
		if (noteGraphic.equals(NoteGraphic.SYMBOLS)) {
			Point a = getNoteGraphicPoint(index);
			pa.pushMatrix();
				pa.translate(a.x, a.y);
				if (transform.equals(Transform.ROTATE)) {
					float theta = pa.phrase.getPercentDurationOfSCIndex(index) * pa.TWO_PI;
					pa.rotate(theta);
				}
				int pitch = (int) (pa.phrase.getSCPitch(index) % 12);
				String symbol = pa.scale.getNoteNameByPitchValue(pitch);
				pa.text(symbol, 0, 0);
				if (transform.equals(Transform.TRANSLATE)) {
					pa.text(symbol, width, 0);
					pa.text(symbol, -width, 0);
				}
			pa.popMatrix();
		}
		else if (noteGraphic.equals(NoteGraphic.DOTS)) {
			Point a = getNoteGraphicPoint(index);
			pa.ellipseMode(pa.CENTER);
			pa.ellipse(a.x, a.y, 20, 20);
			if (transform.equals(Transform.TRANSLATE)) {
				pa.ellipse(a.x - width, a.y, 20, 20);
				pa.ellipse(a.x + width, a.y, 20, 20);
			}
		}
		else if (noteGraphic.equals(NoteGraphic.CONNECTED_DOTS)) {
			Point a = getNoteGraphicPoint(index);
			Point b = getNoteGraphicPoint(index+1);
			pa.ellipseMode(pa.CENTER);
			pa.ellipse(a.x, a.y, 20, 20);
			pa.line(a.x, a.y, b.x, b.y);
			if (transform.equals(Transform.TRANSLATE)) {
				pa.ellipse(a.x - width, a.y, 20, 20);
				pa.line(a.x - width, a.y, b.x - width, b.y);
				pa.ellipse(a.x + width, a.y, 20, 20);
				pa.line(a.x + width, a.y, b.x + width, b.y);
			}
		}
		else if (noteGraphic.equals(NoteGraphic.RECTS_OR_SECTORS)) {
			if (transform.equals(Transform.TRANSLATE)) {
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