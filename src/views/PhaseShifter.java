package views;

import java.lang.reflect.Method;
import java.util.ArrayList;

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
	
	//movement:
	private float normalTransform1, normalTransform2;
	
	//phrase readers:
	private PhraseReader readerA, readerB;
	private final int ONE_ID = 1, TWO_ID = 2;
	
	//active note:
	private int activeNote1, activeNote2;
	
	//geometrical data:
	private ArrayList<DataPoint> data = new ArrayList<DataPoint>();
	
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
		
		initPhraseReaders();
		onEnter();
	}
	
	public void onEnter() {
		initData();
	}
	
	private void initData() {
		data.clear();
		int i = 0;
		while (i <= pa.phrase.getNumNotes()) {
			if (pa.phrase.getSCDynamic(i % pa.phrase.getNumNotes()) > 0) {
				data.add(new DataPoint(i));
			}
			i++;
		}
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
			dNotept2 = (dNotept2 - dNotept1);
			dNotept1 = 0;
		}
		else if (cameraMode.toInt() == RELATIVE_TO_2) {
			dNotept1 = (dNotept1 - dNotept2);
			dNotept2 = 0;
		}
		
		pa.pushMatrix();
		
		pa.translate(this.getCenx(), this.getCeny());
		
		updateAccumulators(dNotept2, dNotept1);
		
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
	
	private void updateAccumulators(float dNotept1, float dNotept2) {
		normalTransform1 += PApplet.map(dNotept1, 0, pa.phrase.getTotalDuration(), 0, 1);
		normalTransform2 += PApplet.map(dNotept2, 0, pa.phrase.getTotalDuration(), 0, 1);
		normalTransform1 %= 1;
		normalTransform2 %= 1;
	}

	private void transform(int playerNum) {
		float normalAcc = (playerNum == 1) ? normalTransform1 : normalTransform2;
		switch(transformation.toInt()) {
			case TRANSLATE: pa.translate(normalAcc*width, 0); break;
			case ROTATE: pa.rotate(normalAcc*pa.PI); break;
		}
	}
	
	private void drawPhraseGraphic(int activeNote, int color, float bpm) {
		styleNoteGraphics(color, false);
		for (int i=0; i<data.size()-1; i++) {
			if ( (activeNoteMode.toInt() == SHOW_ACTIVE_NOTE || activeNoteMode.toInt() == ONLY_SHOW_ACTIVE_NOTE) && i == activeNote) {
				styleNoteGraphics(color, true);
				drawNoteGraphic(data.get(i), data.get(i+1));
				styleNoteGraphics(color, false);
			}
			else {
				drawNoteGraphic(data.get(i), data.get(i+1));
			}
		}
	}
	
	class DataPoint {
		float tx, ty, twidth;
		float rx, ry, theta1, theta2, radius;
		float rxAlt, ryAlt;
		String pitchName;
		
		DataPoint(int i) {
			float normalStart = (i == pa.phrase.getNumNotes()) ? 1 : pa.phrase.getPercentDurationOfSCIndex(i);
			i %= pa.phrase.getNumNotes();
			float normalWidth = pa.phrase.getSCDuration(i) / pa.phrase.getTotalDuration();
			tx = pa.map(normalStart, 0, 1, -halfWidth, halfWidth);
			ty = pa.map(pa.phrase.getSCPitch(i), pa.phrase.minPitch(), pa.phrase.maxPitch(), halfHeight, -halfHeight);
			twidth = normalWidth * width;
			theta1 = pa.map(normalStart, 0, 1, 0, pa.TWO_PI);
			theta2 = pa.map(normalWidth, 0, 1, 0, pa.TWO_PI) + theta1;
			pitchName = pa.scale.getNoteNameByPitchValue(pa.phrase.getSCPitch(i));
			radius = pa.map(pa.phrase.getSCPitch(i), pa.phrase.minPitch(), pa.phrase.maxPitch(), minRadius, maxRadius);
			rx = pa.cos(theta1 - pa.HALF_PI) * radius;
			ry = pa.sin(theta1 - pa.HALF_PI) * radius;
			rxAlt = pa.cos(theta1 - pa.HALF_PI)*pa.lerp(minRadius, maxRadius, 0.5f);
			ryAlt = pa.sin(theta1 - pa.HALF_PI)*pa.lerp(minRadius, maxRadius, 0.5f);
		}
		
		float x() {
			if (transformation.toInt() == TRANSLATE) {
				return tx;
			}
			else {
				return (plotPitchMode.toInt() == PLOT_PITCH) ? rx : rxAlt;
			}
		}
	
		float y() {
			if (transformation.toInt() == TRANSLATE) {
				return (plotPitchMode.toInt() == PLOT_PITCH) ? ty : 0;
			}
			else {
				return (plotPitchMode.toInt() == PLOT_PITCH) ? ry : ryAlt;
			}
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
	
	private void drawNoteGraphic(DataPoint d, DataPoint e) {
		if (noteGraphic.toInt()== SYMBOLS) {
			pa.pushMatrix();
				pa.translate(d.x(), d.y());
				if (transformation.toInt() == ROTATE) {
					pa.rotate(d.theta1);
				}
				pa.text(d.pitchName, 0, 0);
				if (transformation.toInt() == TRANSLATE) {
					pa.text(d.pitchName, width, 0);
					pa.text(d.pitchName, -width, 0);
				}
			pa.popMatrix();
		}
		else if (noteGraphic.toInt()== DOTS) {
			float d_x = d.x();
			float d_y = d.y();
					
			pa.ellipseMode(pa.CENTER);
			pa.ellipse(d_x, d_y, 20, 20);
			if (transformation.toInt() == TRANSLATE) {
				pa.ellipse(d_x - width, d_y, 20, 20);
				pa.ellipse(d_x + width, d_y, 20, 20);
			}
		}
		else if (noteGraphic.toInt() == CONNECTED_DOTS) {
			float d_x = d.x();
			float d_y = d.y();
			
			pa.ellipseMode(pa.CENTER);
			pa.ellipse(d_x, d_y, 20, 20);
			pa.line(d_x, d_y, e.x(), e.y());
			if (transformation.toInt() == TRANSLATE) {
				pa.ellipse(d_x - width, d_y, 20, 20);
				pa.line(d_x - width, d_y, e.x() - width, e.y());
				pa.ellipse(d_x + width, d_y, 20, 20);
				pa.line(d_x + width, d_y, e.x() + width, e.y());
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
				pa.ellipseMode(pa.RADIUS);
				pa.arc(0, 0, d.radius-10, d.radius-10, d.theta1, d.theta2);
				pa.line(pa.cos(d.theta1)*(d.radius-10), pa.sin(d.theta1)*(d.radius-10),
						pa.cos(d.theta1)*(d.radius+10), pa.sin(d.theta1)*(d.radius+10));
				pa.line(pa.cos(d.theta2)*(d.radius-10), pa.sin(d.theta2)*(d.radius-10),
						pa.cos(d.theta2)*(d.radius+10), pa.sin(d.theta2)*(d.radius+10));
				pa.arc(0, 0, d.radius+10, d.radius+10, d.theta1, d.theta2);
			}
		}
	}
}