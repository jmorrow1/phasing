package views;

import java.lang.reflect.Method;
import java.util.ArrayList;

import geom.Rect;
import phasing.PhasesPApplet;
import phasing.PhraseReader;
import geom.CurvedRect;
import processing.core.PApplet;
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
	
	//phrase readers:
	private PhraseReader readerA, readerB;
	private final int ONE_ID = 1, TWO_ID = 2;
	
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
	 * @param rect The area in which to draw (usually just the entirety of the window).
	 * @param opacity The opacity of notes.
	 * @param pa The PhasesPApplet instance.
	 */
	public PhaseShifter(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect, opacity, pa);
		this.pa = pa;
		
		initBounds();
		
		//TODO: incorporate the number of notes into this calculation (the more notes the larger the radius):
		maxRadius = pa.min(pa.lerp(getHeight(), getWidth(), 0.2f) * 0.3f, pa.height/2f - FONT_SIZE/2f);
		minRadius = maxRadius / 2;
		
		initPhraseReaders();
		
		initData();
	}
	
	private void initBounds() {
		width = this.getWidth();
		height = this.getHeight()*0.5f;
		halfWidth = width*0.5f;
		halfHeight = height*0.5f;
	}
	
	private void initData() {
		dataPoints.clear();
		for (int i=0; i<=pa.currentPhrase.getNumNotes(); i++) {
			if (pa.currentPhrase.getNumNotes() > 0 && 
					pa.currentPhrase.getSCDynamic(i % pa.currentPhrase.getNumNotes()) > 0) {
				dataPoints.add(new DataPoint(i));
			}
		}
		
		dataConnections.clear();
		for (int i=0; i<dataPoints.size()-1; i++) {
			dataConnections.add(new DataConnection(dataPoints.get(i), dataPoints.get(i+1)));
		}
	}
	
	private void initPhraseReaders() {
		try {
			Method callback = PhaseShifter.class.getMethod("changeActiveNote", PhraseReader.class);
			readerA = new PhraseReader(pa.currentPhrase, ONE_ID, this, callback);
			readerB = new PhraseReader(pa.currentPhrase, TWO_ID, this, callback);

		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**************************
	 ***** Event Handling *****
	 **************************/
	
	@Override
	protected void resized() {
		initBounds();
	}
	
	@Override
	public void wakeUp(float notept1, float notept2) {
		readerA.wakeUp(notept1);
		readerB.wakeUp(notept2);
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
	
	/******************
	 ***** Update *****
	 ******************/

	@Override
	public void update(int dt, float dNotept1, float dNotept2) {
		if (pa.currentPhrase.getNumNotes() > 0) {
			readerA.update(dNotept1);
			readerB.update(dNotept2);
			
			pa.pushMatrix();
			
			pa.translate(this.getCenx(), this.getCeny());
			updateNormalTransforms(dNotept1, dNotept2);
		
			drawPhraseGraphic(activeNote1, 1);
			drawPhraseGraphic(activeNote2, 2);
			
			pa.popMatrix();
		}
	}
	
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

	private void transform(int playerNum) {
		float normalAcc = (playerNum == 2) ? normalTransform1 : normalTransform2;
		switch(transformation.toInt()) {
			case TRANSLATE: pa.translate(-normalAcc*width, 0); break;
			case ROTATE: pa.rotate(-normalAcc*pa.TWO_PI); break;
		}
	}
	
	private void drawPhraseGraphic(int activeNote, int playerNum) {
		//set non-active and active colors
		int nonActiveColor = pa.color(0, opacity);
		int activeColor = pa.color(0);
		if (colorScheme.toInt() == DIACHROMATIC) {
			nonActiveColor = (playerNum == 1) ? pa.getColor1() : pa.getColor2();
			nonActiveColor = pa.color(nonActiveColor, opacity);
			activeColor = (playerNum == 1) ? pa.getColor1VeryBold() : pa.getColor2VeryBold();
			activeColor = pa.color(activeColor, opacity);
		}
		
		//draw non-sine wave graphics
		if (noteGraphic.toInt() != SINE_WAVE) {
			pa.pushMatrix();
			transform(playerNum);
			styleNoteGraphics(nonActiveColor);
			
			int i=0; //loops through notes in phrase
			int j=0; //loops through data points
			while (i < pa.currentPhrase.getNumNotes()) {
				if (pa.currentPhrase.getSCDynamic(i) > 0) {
					if ( (activeNoteMode.toInt() == SHOW_ACTIVE_NOTE || activeNoteMode.toInt() == ONLY_SHOW_ACTIVE_NOTE) && i == activeNote) {
						styleNoteGraphics(activeColor);
						drawNoteGraphic(dataPoints.get(j), dataPoints.get(j+1));
						styleNoteGraphics(nonActiveColor);
					}
					else if (activeNoteMode.toInt() != ONLY_SHOW_ACTIVE_NOTE) {
						drawNoteGraphic(dataPoints.get(j), dataPoints.get(j+1));
					}
					j++;
				}
				i++;
			}
			
			//draw connections between dots
			if (noteGraphic.toInt() == CONNECTED_DOTS) {
				if (activeNoteMode.toInt() != ONLY_SHOW_ACTIVE_NOTE) {
					for (int k=0; k<dataConnections.size(); k++) {
						DataConnection c = dataConnections.get(k);
						if (k == activeNote && activeNoteMode.toInt() != DONT_SHOW_ACTIVE_NOTE) {
							pa.stroke(activeColor);
						}
						else {
							pa.stroke(nonActiveColor);
						}
				
						c.drawLine();
					}
				}
				else {
					pa.stroke(activeColor);
					DataConnection c = dataConnections.get(activeNote);
					c.drawLine();
				}
			}
			pa.popMatrix();
		}
		//draw sine wave graphics
		else if (noteGraphic.toInt() == SINE_WAVE) {
			pa.stroke(nonActiveColor);
			if (transformation.toInt() == TRANSLATE) {
				if (plotPitchMode.toInt() == PLOT_PITCH) {
					drawSineWave((playerNum == 1) ? normalTransform2 : normalTransform1);
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
	
	private void styleNoteGraphics(int color) {
		switch (noteGraphic.toInt()) {
			case SYMBOLS:
			case DOTS1:
			case CONNECTED_DOTS:
			case RECTS_OR_SECTORS:
				pa.noStroke();
				pa.fill(color);
				break;
		}
	}
	
	private void drawSymbol(String s, float x, float y) {
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
	
	private void drawNoteGraphic(DataPoint d, DataPoint e) {
		if (noteGraphic.toInt()== SYMBOLS) {
			pa.pushMatrix();
				pa.translate(d.x(), d.y());
				if (transformation.toInt() == ROTATE) {
					pa.rotate(d.theta1);
				}
				drawSymbol(d.pitchName, 0, 0);
				if (transformation.toInt() == TRANSLATE) {
					drawSymbol(d.pitchName, width, 0);
					drawSymbol(d.pitchName, -width, 0);
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
		
		private DataConnection(DataPoint d, DataPoint e) {
			float lineDist = pa.dist(d.tx, d.ty, e.tx, e.ty);
			float amt = (DOT_RADIUS / lineDist);
			
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
			else {
				if (plotPitchMode.toInt() == PLOT_PITCH) {
					pa.line(rx1, ry1, rx2, ry2);
					pa.line(rx1 - width, ry1, rx2 - width, ry2);
					pa.line(rx1 + width, ry1, rx2 + width, ry2);
				}
				else {
					pa.line(rx1Alt, ry1Alt, rx2Alt, ry2Alt);
					pa.line(rx1Alt - width, ry1Alt, rx2Alt - width, ry2Alt);
					pa.line(rx1Alt + width, ry1Alt, rx2Alt + width, ry2Alt);
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
			float normalStart = (i == pa.currentPhrase.getNumNotes()) ? 1 : pa.currentPhrase.getPercentDurationOfSCIndex(i);
			i %= pa.currentPhrase.getNumNotes();
			float normalWidth = pa.currentPhrase.getSCDuration(i) / pa.currentPhrase.getTotalDuration();
			tx = pa.map(normalStart, 0, 1, -halfWidth, halfWidth);
			ty = pa.map(pa.currentPhrase.getSCPitch(i), pa.currentPhrase.minPitch(), pa.currentPhrase.maxPitch(), halfHeight, -halfHeight);
			twidth = normalWidth * width;
			theta1 = pa.map(normalStart, 0, 1, 0, pa.TWO_PI);
			theta2 = pa.map(normalWidth, 0, 1, 0, pa.TWO_PI) + theta1;
			pitchName = pa.currentScale.getNoteNameByPitchValue(pa.currentPhrase.getSCPitch(i));
			radius = pa.map(pa.currentPhrase.getSCPitch(i), pa.currentPhrase.minPitch(), pa.currentPhrase.maxPitch(), minRadius, maxRadius);
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