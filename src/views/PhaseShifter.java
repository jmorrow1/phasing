package views;

import java.util.ArrayList;

import geom.CurvedRect;
import geom.Rect;
import phasing.PhasesPApplet;
import phasing.PhraseReader;
import phasing.PlayerInfo;
import processing.core.PApplet;
import screens.Presenter;
import util.ModInt;

/**
 * The PhaseShifter View type. It show two identical plots of notes that are phase-shifted with respect to one another.
 * 
 * @author James Morrow
 *
 */
public class PhaseShifter extends View {
	//starting pitch:
	private int startingPitch=0;
	
	//bounds:
	private float width, height, halfWidth, halfHeight;
	private float minRadius, maxRadius;
	
	//movement:
	private float normalTransform1, normalTransform2;
	
	//active note:
	private int activeNote1, activeNote2;
	
	//geometrical data:
	private final int FONT_SIZE = 42;
	private final static int DOT_RADIUS = 10;
	private ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
	private ArrayList<DataConnection> dataConnections = new ArrayList<DataConnection>();
	
	//options:
	public ModInt activeNoteMode = new ModInt(0, numActiveNoteModes, activeNoteModeName);
	public ModInt transformation = new ModInt(0, numTransformations, transformationName);
	public ModInt cameraMode = new ModInt(0, numCameraModes, cameraModeName);
	public ModInt noteGraphic = new ModInt(0, numNoteGraphicSet1s, noteGraphicSet1Name);
	public ModInt plotPitchMode = new ModInt(0, numWaysOfPlottingPitchOrNot, plotPitchModeName);
	public ModInt colorScheme = new ModInt(1, numColorSchemes, colorSchemeName);

	/**************************
	 ***** Initialization *****
	 **************************/
	
	/**
	 * 
	 * @param viewBox The area in which to draw.
	 * @param opacity The opacity of notes.
	 * @param playerInfo Contains information (potentially) about how to initialize the view's settings.
	 * @param pa The PhasesPApplet instance.
	 */
	public PhaseShifter(Rect viewBox, int opacity, PlayerInfo playerInfo, PhasesPApplet pa) {
		super(viewBox, opacity, pa);
		init();
	}
	
	/**
	 * Constructs a PhaseShifter whose option values are taken from the another PhaseShifter.
	 * 
	 * @param ps The PhaseShifter this one derives its option values from.
	 * @param viewBox The area in which to draw.
	 * @param opacity The opacity of notes.
	 * @param playerInfo Contains information (potentially) about how to initialize the view's settings.
	 * @param pa The PhasesPApplet instance.
	 */
	public PhaseShifter(PhaseShifter ps, Rect viewBox, int opacity, PhasesPApplet pa) {
		super(viewBox, opacity, pa);
		copyOptionValues(ps);
		init();
	}
	
	/**
	 * Copies the given PhaseShifter object's option values into this PhaseShifter object's option variables.
	 * @param ps The given PhaseShifter.
	 */
	private void copyOptionValues(PhaseShifter ps) {
		this.activeNoteMode.setValue(ps.activeNoteMode.toInt());
		this.transformation.setValue(ps.transformation.toInt());
		this.cameraMode.setValue(ps.cameraMode.toInt());
		this.noteGraphic.setValue(ps.noteGraphic.toInt());
		this.plotPitchMode.setValue(ps.plotPitchMode.toInt());
		this.colorScheme.setValue(ps.colorScheme.toInt());
	}
	
	/**
	 * Initializes the PhaseShifter object.
	 */
	private void init() {
		initBounds();	
		initData();
	}
	
	/**
	 * Initializes the variables that have to do with the boundaries within which things are drawn.
	 */
	private void initBounds() {
		width = this.getWidth();
		height = this.getHeight()*0.5f;
		halfWidth = width*0.5f;
		halfHeight = height*0.5f;
		
		maxRadius = pa.min(pa.lerp(getHeight(), getWidth(), 0.2f) * 0.35f, pa.height/2f - FONT_SIZE/2f);
		minRadius = maxRadius * 0.45f;
	}
	
	/**
	 * Initializes note graphic positioning data and stores it so it doesn't have to be constantly recomputed.
	 */
	private void initData() {
		boolean phraseContainsNonRests = false;
		boolean phraseContainsMultipleNonRests = false;
		
		dataPoints.clear();
		for (int i=0; i<pa.currentPhrase.getNumNotes(); i++) {
			dataPoints.add(new DataPoint(i));
			if (!pa.currentPhrase.isRest(i)) {
				if (phraseContainsNonRests) {
					phraseContainsMultipleNonRests = true;
				}
				phraseContainsNonRests = true;
			}
		}
		
		dataConnections.clear();
		
		if (phraseContainsMultipleNonRests) {
			DataPoint startPt = null;
			for (int i=0; i<dataPoints.size(); i++) {
				if (!pa.currentPhrase.isRest(i)) {
					if (startPt == null) {
						startPt = dataPoints.get(i);
					}
					else {
						dataConnections.add(new DataConnection(startPt, dataPoints.get(i)));
						startPt = dataPoints.get(i);
					}
				}
			}
			
			DataPoint lastPt = null;
			for (int i=0; i<pa.currentPhrase.getNumNotes(); i++) {
				if (!pa.currentPhrase.isRest(i)) {
					lastPt = new DataPoint(i + pa.currentPhrase.getNumNotes());
					break;
				}
			}
		
			dataConnections.add(new DataConnection(startPt, lastPt));
		}
	}
	
	/**************************
	 ***** Event Handling *****
	 **************************/
	
	@Override
	protected void resized(float prevWidth, float prevHeight) {
		initBounds();
		initData();
	}
	
	@Override
	public void wakeUp(float notept1, float notept2) {}
	
	@Override
	public void noteEvent(PhraseReader reader) {
		changeActiveNote(reader);
	}
	
	/**
	 * Callback from PhraseReaders, for when a new note (or rest) is read.
	 * @param reader The callee.
	 */
	private void changeActiveNote(PhraseReader reader) {
		if (reader.getId() == Presenter.READER_ONE_ID) {
			activeNote1 = reader.getNoteIndex();
		}
		else if (reader.getId() == Presenter.READER_TWO_ID) {
			activeNote2 = reader.getNoteIndex();
		}
	}
	
	/******************
	 ***** Update *****
	 ******************/

	@Override
	public void update(int dt, float dNotept1, float dNotept2) {
		if (pa.currentPhrase.getNumNotes() > 0) {
			pa.pushMatrix();
			
			pa.translate(this.getCenx(), this.getCeny());
			updateNormalTransforms(dNotept1, dNotept2);
		
			drawWave(activeNote1, 1);
			drawWave(activeNote2, 2);
			
			pa.popMatrix();
		}
	}
	
	/**
	 * Updates the transformation variables, which can later be translated into a rotation amount or a translation amount.
	 * @param dNotept1 The amount of time passed since the last update, in terms of how much music player 1 played.
	 * @param dNotept2 The amount of time passed since the last update, in terms of how much music player 2 played.
	 */
	public void updateNormalTransforms(float dNotept1, float dNotept2) {
		if (pa.currentPhrase.getTotalDuration() != 0) {
			if (cameraMode.toInt() == RELATIVE_TO_1) {
				dNotept2 = (dNotept2 - dNotept1);
				dNotept1 = 0;
			}
			else if (cameraMode.toInt() == RELATIVE_TO_2) {
				dNotept1 = (dNotept1 - dNotept2);
				dNotept2 = 0;
			}
			
			normalTransform1 += PApplet.map(dNotept2, 0, pa.currentPhrase.getTotalDuration(), 0, 1);
			normalTransform2 += PApplet.map(dNotept1, 0, pa.currentPhrase.getTotalDuration(), 0, 1);
			normalTransform1 %= 1;
			normalTransform2 %= 1;
		}
	}

	/**
	 * Applies a transformation to the animator with the given waveNum (1 or 2).
	 * Whether that's a translation or a rotation depends on the transformation mode.
	 * 
	 * @param waveNum Specifies which wave (graph) is to be transformed.
	 */
	private void transform(int waveNum) {
		float normalAcc = (waveNum == 2) ? normalTransform1 : normalTransform2;
		switch(transformation.toInt()) {
			case TRANSLATE: pa.translate(-normalAcc*width, 0); break;
			case ROTATE: pa.rotate(-normalAcc*pa.TWO_PI); break;
		}
	}
	
	/**
	 * Tells whether or not active notes should be shown.
	 * @return True if active notes should be shown, false otherwise.
	 */
	private boolean showActiveNote() {
		return (activeNoteMode.toInt() == SHOW_ACTIVE_NOTE || 
				activeNoteMode.toInt() == ONLY_SHOW_ACTIVE_NOTE);
	}
	
	/**
	 * Draws the wave (graph).
	 * 
	 * @param activeNote The index of the note (or rest) that is currently being played.
	 * @param waveNum Specifies which wave (graph) is to be transformed.
	 */
	private void drawWave(int activeNote, int waveNum) {
		//set non-active and active colors
		int nonActiveColor = pa.color(0, opacity);
		int activeColor = pa.color(0);
		if (colorScheme.toInt() == DIACHROMATIC) {
			nonActiveColor = (waveNum == 1) ? pa.getColor1() : pa.getColor2();
			nonActiveColor = pa.color(nonActiveColor, opacity);
			activeColor = (waveNum == 1) ? pa.getColor1VeryBold() : pa.getColor2VeryBold();
			activeColor = pa.color(activeColor, opacity);
		}
		
		if (noteGraphic.toInt() != SINE_WAVE && noteGraphic.toInt() != LINE_SEGMENTS) {
			pa.pushMatrix();
			transform(waveNum);
			styleNoteGraphics(nonActiveColor);
			
			for (int i=0; i < pa.currentPhrase.getNumNotes(); i++) {
				if (!pa.currentPhrase.isRest(i)) {
					if (showActiveNote() && i == activeNote) {
						styleNoteGraphics(activeColor);
						drawNoteGraphic(dataPoints.get(i));
						styleNoteGraphics(nonActiveColor);
					}
					else if (activeNoteMode.toInt() != ONLY_SHOW_ACTIVE_NOTE) {
						drawNoteGraphic(dataPoints.get(i));
					}
				}
			}
			
			//draw connections between dots
			if (noteGraphic.toInt() == CONNECTED_DOTS) {
				
				int i = 0; //loops through the Phrase's notes
				int j = 0; //loops through the data connections
				while (i < pa.currentPhrase.getNumNotes()) {
					boolean increment_j = !pa.currentPhrase.isRest(i) && dataConnections.size() > 0;
					if (increment_j) {
						boolean drawConnection = (activeNoteMode.toInt() != ONLY_SHOW_ACTIVE_NOTE || i == activeNote);
						if (drawConnection) {
							DataConnection c = dataConnections.get(j);						
							if (i == activeNote && activeNoteMode.toInt() != DONT_SHOW_ACTIVE_NOTE) {
								pa.stroke(activeColor);
							}
							else {
								pa.stroke(nonActiveColor);
							}
							c.drawLine();
						}
						j++;
					}
					i++;
				}
				
			}
			
			pa.popMatrix();
		}
		else if (noteGraphic.toInt() == LINE_SEGMENTS) {
			drawLineSegments(waveNum, activeNote, nonActiveColor, activeColor);
		}
		else if (noteGraphic.toInt() == SINE_WAVE) {
			pa.stroke(nonActiveColor);
			if (transformation.toInt() == TRANSLATE) {
				if (plotPitchMode.toInt() == PLOT_PITCH) {
					drawSineWave((waveNum == 1) ? normalTransform2 : normalTransform1);
				}
				else {
					pa.line(-halfWidth, 0, halfWidth, 0);
				}
			}
			else if (transformation.toInt() == ROTATE) {
				float radius = pa.min(halfWidth, halfHeight);
				pa.noFill();
				pa.ellipseMode(pa.RADIUS);
				pa.ellipse(0, 0, radius, radius);
			}
		}	
	}
	
	private void drawLineSegments(int waveNum, int activeNote, int nonActiveColor, int activeColor) {
		pa.pushMatrix();
		transform(waveNum);
		
		if (activeNoteMode.toInt() != ONLY_SHOW_ACTIVE_NOTE) {
			pa.stroke(nonActiveColor);
			pa.beginShape();
			if (transformation.toInt() == TRANSLATE) {
				drawLineSegments(waveNum, activeNote, nonActiveColor, activeColor, -width);
			}
			drawLineSegments(waveNum, activeNote, nonActiveColor, activeColor, 0);
			if (transformation.toInt() == TRANSLATE) {
				drawLineSegments(waveNum, activeNote, nonActiveColor, activeColor, width);
			}
			pa.endShape();
		}
		
		if (showActiveNote()) {
			pa.stroke(activeColor);
			drawActiveLineSegment(waveNum, activeNote, nonActiveColor, activeColor, 0);
			if (transformation.toInt() == TRANSLATE) {
				drawActiveLineSegment(waveNum, activeNote, nonActiveColor, activeColor, -width);
				drawActiveLineSegment(waveNum, activeNote, nonActiveColor, activeColor, width);
			}
		}
		
		pa.popMatrix();
	}
	
	private void drawActiveLineSegment(int waveNum, int activeNote, int nonActiveColor, int activeColor, float xOffset) {
		if (dataConnections.size() > 0) {
			int i = 0; //loops through the Phrase's notes
			int j = 0; //loops through the data connections
			while (i < pa.currentPhrase.getNumNotes()) {
				if (!pa.currentPhrase.isRest(i)) {
					if (i == activeNote) {
						DataConnection c = dataConnections.get(j);
						pa.line(c.d.x() + xOffset, c.d.y(), c.e.x() + xOffset, c.e.y());
					}
					j++;
				}

				i++;
			}
		}
		
	}
	
	private void drawLineSegments(int waveNum, int activeNote, int nonActiveColor, int activeColor, float xOffset) {

		int i = 0; //loops through the Phrase's notes
		int j = 0; //loops through the data connections
		while (i < pa.currentPhrase.getNumNotes()) {
			boolean drawConnection = (activeNoteMode.toInt() != ONLY_SHOW_ACTIVE_NOTE || i == activeNote)
					&& !pa.currentPhrase.isRest(i) && dataConnections.size() > 0;
			if (drawConnection) {
				DataConnection c = dataConnections.get(j);
				pa.vertex(c.d.x() + xOffset, c.d.y());
				pa.vertex(c.e.x() + xOffset, c.e.y());
				j++;
			}
			else {
				pa.endShape();
				pa.beginShape();
			}
			i++;
		}
		
	}
	
	/**
	 * Draws a sine wave within the appropriate area of the view box.
	 * 
	 * @param normalTransform A normal value (one that's between 0 and 1) that determines a transformation (a translation in this case).
	 */
	private void drawSineWave(float normalTransform) {
		int x = (int)-halfWidth;
		int dx = 4;
		float amp = halfHeight;
		
		float translation = normalTransform * width;
		float theta = pa.map(translation, 0, width, 0, pa.TWO_PI);
		float dTheta = dx / width * PApplet.TWO_PI;
		
		pa.strokeWeight(4);
		pa.beginShape();
		while (x <= halfWidth) {
			pa.vertex(x, pa.sin(theta) * amp);
			x += dx;
			theta += dTheta;
		}
		pa.endShape();
	}
	
	/**
	 * Styles the note graphics with the given color.
	 * @param color The color.
	 */
	private void styleNoteGraphics(int color) {
		switch (noteGraphic.toInt()) {
			case LINE_SEGMENTS :
				pa.stroke(color);
				pa.noFill();
				break;
			default :
				pa.noStroke();
				pa.fill(color);
				break;
		}		
	}
	
	/**
	 * Draws the String at (x,y).
	 * 
	 * @param s The string.
	 * @param x The x-coordinate.
	 * @param y The y-coordinate.
	 */
	private void drawString(String s, float x, float y) {
		pa.textSize(FONT_SIZE);
		pa.textFont(pa.pfont42);
		pa.text(s.charAt(0), x, y);
		
		x += pa.textWidth(s.charAt(0))/2f + 5;
		y += pa.textDescent();
		
		if (s.length() > 1) {
			pa.textSize(18);
			pa.textFont(pa.pfont18);
			pa.text(s.charAt(1), x, y);
		}
	}
	
	/**
	 * Draws the note graphic.
	 * 
	 * @param d The note DataPoint to draw.
	 */
	private void drawNoteGraphic(DataPoint d) {
		if (noteGraphic.toInt()== SYMBOLS) {
			pa.pushMatrix();
				pa.translate(d.x(), d.y());
				if (transformation.toInt() == ROTATE) {
					pa.rotate(d.theta1);
				}
				drawString(d.pitchName, 0, 0);
				if (transformation.toInt() == TRANSLATE) {
					drawString(d.pitchName, width, 0);
					drawString(d.pitchName, -width, 0);
				}
			pa.popMatrix();
		}
		else if (noteGraphic.toInt() == DOTS1 || noteGraphic.toInt() == CONNECTED_DOTS) {
			float d_x = d.x();
			float d_y = d.y();
					
			pa.ellipseMode(pa.CENTER);
			pa.ellipse(d_x, d_y, 20, 20);
			if (transformation.toInt() == TRANSLATE) {
				pa.ellipse(d_x - width, d_y, 20, 20);
				pa.ellipse(d_x + width, d_y, 20, 20);
			}
		}
		else if (noteGraphic.toInt() == RECTS_OR_SECTORS) {
			if (transformation.toInt() == TRANSLATE) {
				float d_x = d.x();
				float d_y = d.y();
				
				pa.rectMode(pa.CORNER);
				pa.rect(d_x, d_y, d.twidth, 20);
				pa.rect(d_x - width, d_y, d.twidth, 20);
				pa.rect(d_x + width, d_y, d.twidth, 20);
			}
			else {
				d.curvedRect().display(pa);
			}
		}
	}
	
	/********************************
	 ***** DataConnection class *****
	 ********************************/
	
	/**
	 * Container for positioning data regarding connections between note graphics (used when the noteGraphic is CONNECTED_DOTS).
	 * This is so the data doesn't have to be recalculated every time its used, which is every frame when the noteGraphic is CONNECTED_DOTS.
	 * 
	 * @author James Morrow
	 *
	 */
	private class DataConnection {
		//specific to connected dots:
		float tx1, ty1, tx2, ty2;
		float tx1Alt, ty1Alt, tx2Alt, ty2Alt;
		float rx1, ry1, rx2, ry2;
		float rx1Alt, ry1Alt, rx2Alt, ry2Alt;
		DataPoint d, e;
		
		private DataConnection(DataPoint d, DataPoint e) {
			float lineDist = pa.dist(d.tx, d.ty, e.tx, e.ty);
			float amt = (DOT_RADIUS / lineDist);
			
			this.d = d;
			this.e = e;
			
			tx1 = pa.lerp(d.tx, e.tx, amt);
			ty1 = pa.lerp(d.ty, e.ty, amt);
			tx2 = pa.lerp(d.tx, e.tx, 1-amt);
			ty2 = pa.lerp(d.ty, e.ty, 1-amt);
			
			lineDist = pa.dist(d.txAlt, d.tyAlt, e.txAlt, e.tyAlt);
			amt = (DOT_RADIUS / lineDist);
			
			tx1Alt = pa.lerp(d.txAlt, e.txAlt, amt);
			ty1Alt = pa.lerp(d.tyAlt, e.tyAlt, amt);
			tx2Alt = pa.lerp(d.txAlt, e.txAlt, 1-amt);
			ty2Alt = pa.lerp(d.tyAlt, e.tyAlt, 1-amt);
			
			lineDist = pa.dist(d.rx, d.ry, e.rx, e.ry);
			amt = (DOT_RADIUS / lineDist);
			
			rx1 = pa.lerp(d.rx, e.rx, amt);
			ry1 = pa.lerp(d.ry, e.ry, amt);
			rx2 = pa.lerp(d.rx, e.rx, 1-amt);
			ry2 = pa.lerp(d.ry, e.ry, 1-amt);
			
			lineDist = pa.dist(d.rxAlt, d.ryAlt, e.rxAlt, e.ryAlt);
			amt = (DOT_RADIUS / lineDist);
			
			rx1Alt = pa.lerp(d.rxAlt, e.rxAlt, amt);
			ry1Alt = pa.lerp(d.ryAlt, e.ryAlt, amt);
			rx2Alt = pa.lerp(d.rxAlt, e.rxAlt, 1-amt);
			ry2Alt = pa.lerp(d.ryAlt, e.ryAlt, 1-amt);
		}
		
		void drawLine() {
			if (noteGraphic.toInt() == CONNECTED_DOTS) {
				if (transformation.toInt() == TRANSLATE) {
					if (plotPitchMode.toInt() == PLOT_PITCH) {
						pa.line(tx1, ty1, tx2, ty2);
						pa.line(tx1 - width, ty1, tx2 - width, ty2);
						pa.line(tx1 + width, ty1, tx2 + width, ty2);
					}
					else {
						pa.line(tx1Alt, ty1Alt, tx2Alt, ty2Alt);
						pa.line(tx1Alt - width, ty1Alt, tx2Alt - width, ty2Alt);
						pa.line(tx1Alt + width, ty1Alt, tx2Alt + width, ty2Alt);
					}
				}
				else if (transformation.toInt() == ROTATE) {
					if (plotPitchMode.toInt() == PLOT_PITCH) {
						pa.line(rx1, ry1, rx2, ry2);
					}
					else {
						pa.line(rx1Alt, ry1Alt, rx2Alt, ry2Alt);
					}
				}
			}
			else if (noteGraphic.toInt() == LINE_SEGMENTS) {
				if (transformation.toInt() == TRANSLATE) {
					pa.line(d.x(), d.y(), e.x(), e.y());
					pa.line(d.x() - width, d.y(), e.x() - width, e.y());
					pa.line(d.x() + width, d.y(), e.x() + width, e.y());
				}
				else if (transformation.toInt() == ROTATE) {
					if (plotPitchMode.toInt() == PLOT_PITCH) {
						pa.line(d.x(), d.y(), e.x(), e.y());
					}
					else {
						pa.line(d.x(), d.y(), e.x(), e.y());
					}
				}
			}
		}
	}
	
	/***************************
	 ***** DataPoint class *****
	 ***************************/
	
	/**
	 * Container for the positions of note graphics.
	 * This is so the data doesn't have to be recalculated every time its used, which is every frame.
	 *  
	 * @author James Morrow
	 *
	 */
	private class DataPoint {
		final float tx, ty, twidth;
		final float txAlt, tyAlt;
		final float rx, ry, theta1, theta2, radius;
		final float rxAlt, ryAlt;
		//specific to symbols:
		final String pitchName;
		//specific to sectors:
		final static int sectorThickness = 20;
		final CurvedRect curvedRect, sectorAlt;
		
		private DataPoint(int i) {
			float normalStart = pa.currentPhrase.getPercentDurationOfSCIndex(i % pa.currentPhrase.getNumNotes());
			if (i >= pa.currentPhrase.getNumNotes()) {
				normalStart += 1;
			}
			i %= pa.currentPhrase.getNumNotes();
			float normalWidth = pa.currentPhrase.getSCDuration(i) / pa.currentPhrase.getTotalDuration();
			tx = pa.map(normalStart, 0, 1, -halfWidth, halfWidth);
			ty = (pa.currentPhrase.minPitch() != pa.currentPhrase.maxPitch()) ? pa.map(pa.currentPhrase.getSCPitch(i),
					                                                                   pa.currentPhrase.minPitch(), pa.currentPhrase.maxPitch(),
					                                                                   halfHeight, -halfHeight)
					                                                          : 0;
			twidth = normalWidth * width;
			theta1 = pa.map(normalStart, 0, 1, 0, pa.TWO_PI);
			theta2 = pa.map(normalWidth, 0, 1, 0, pa.TWO_PI) + theta1;
			pitchName = pa.currentScale.getNoteNameByPitchValue(pa.currentPhrase.getSCPitch(i));
			radius = (pa.currentPhrase.minPitch() != pa.currentPhrase.maxPitch()) ? pa.map(pa.currentPhrase.getSCPitch(i),
						                                                                   pa.currentPhrase.minPitch(), pa.currentPhrase.maxPitch(),
						                                                                   minRadius, maxRadius)
					                                                              : pa.lerp(minRadius, maxRadius, 0.5f);
			rx = pa.cos(theta1 - pa.HALF_PI) * radius;
			ry = pa.sin(theta1 - pa.HALF_PI) * radius;
			rxAlt = pa.cos(theta1 - pa.HALF_PI)*pa.lerp(minRadius, maxRadius, 0.5f);
			ryAlt = pa.sin(theta1 - pa.HALF_PI)*pa.lerp(minRadius, maxRadius, 0.5f);
			curvedRect = new CurvedRect(radius, sectorThickness, theta1, theta2);
			sectorAlt = new CurvedRect(pa.lerp(minRadius, maxRadius, 0.5f), sectorThickness, theta1, theta2);
			
			txAlt = tx;
			tyAlt = 0;
		}
		
		CurvedRect curvedRect() {
			return (plotPitchMode.toInt() == PLOT_PITCH) ? curvedRect : sectorAlt;
		}
		
		float x() {
			if (transformation.toInt() == TRANSLATE) {
				return (plotPitchMode.toInt() == PLOT_PITCH) ? tx : txAlt;
			}
			else {
				return (plotPitchMode.toInt() == PLOT_PITCH) ? rx : rxAlt;
			}
		}
	
		float y() {
		    if (transformation.toInt() == TRANSLATE) {
				return (plotPitchMode.toInt() == PLOT_PITCH) ? ty : tyAlt;
			}
			else {
				return (plotPitchMode.toInt() == PLOT_PITCH) ? ry : ryAlt;
			}
		}
	}
}