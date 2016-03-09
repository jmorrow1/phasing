package views;

import java.lang.reflect.Method;
import java.util.ArrayList;

import geom.Rect;
import phasing.PhasesPApplet;
import phasing.PhraseReader;
import processing.core.PApplet;
import util.ModInt;

/**
 * The LiveScorer View type. It listens for note events and every time it receives one, it adds a new note to a plot of pitch/time.
 * 
 * @author James Morrow
 *
 */
public class LiveScorer extends View {
	//phrase readers:
	private PhraseReader readerA, readerB;
	
	//where to spawn new points:
	private float x;
	private float y;
	
	//used in calculating where to spawn new points when scoreMode is MOVE_SPAWN_POINT:
	private float spawnX1, spawnY1, spawnX2, spawnY2;
	
	//labels of plot's y-axis:
	private float[] ys;
	private float halfHeight;
	private float halfWidth;
	
	//plot data container:
	private ArrayList<DataPoint> dataPts1 = new ArrayList<DataPoint>();
	private ArrayList<DataPoint> dataPts2 = new ArrayList<DataPoint>();
	
	//musical time bookkeeping:
	private float durationAcc1, durationAcc2;
	
	//other
	private float pixelsPerWholeNote;
	private final int ONE_ID = 1, TWO_ID = 2;
	private int startingPitch = 0;
	private final float NOTE_SIZE;
	private float roundStrokeCapSurplus;
	
	//options:
	public ModInt sineWave = new ModInt(1, numWaysOfBeingASineWaveOrNot, sineWaveName);
	public ModInt scoreMode = new ModInt(0, numScoreModes, scoreModeName);
	public ModInt noteGraphic = new ModInt(0, numNoteGraphicSet2s, noteGraphicSet2Name);
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
	public LiveScorer(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect, opacity, pa);
		
		//TODO: Make dependent on screen size
		NOTE_SIZE = 20;
		
		//phrase readers
		try {
			Method callback = LiveScorer.class.getMethod("plotNote", PhraseReader.class);
			readerA = new PhraseReader(pa.currentPhrase, ONE_ID, this, callback);
			readerB = new PhraseReader(pa.currentPhrase, TWO_ID, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		//note spawn point
		x = 0;
		y = 0;
		
		//labels of y-axis
		ys = new float[pa.currentPhrase.getNumNotes()];
		halfWidth = this.getWidth() * 0.5f;
		halfHeight = this.getHeight() * 0.3f;
		float minPitch = pa.currentPhrase.minPitch();
		float maxPitch = pa.currentPhrase.maxPitch();
		for (int i=0; i<ys.length; i++) {
			if (minPitch != maxPitch) {
				ys[i] = PApplet.map(pa.currentPhrase.getSCPitch(i), minPitch, maxPitch, halfHeight, -halfHeight);
			}
			else {
				ys[i] = PApplet.lerp(minPitch, maxPitch, 0.5f);
			}
		}
		
		//variables for computing note spawn point when scoreMode is MOVE_SPAWN_POINT
		spawnX1 = -getWidth() * 0.5f;
		spawnY1 = -getHeight() * 0.15f - NOTE_SIZE/2f;
		spawnX2 = getWidth() * 0.5f;
		spawnY2 = getHeight() * 0.15f + NOTE_SIZE/2f;
		
		//pixels to musical time conversion
		pixelsPerWholeNote = 60;
		
		//this helps make it such that a quarter note is drawn as a circle when the stroke cap is round:
		roundStrokeCapSurplus = pixelsPerWholeNote/8f;
		
		settingsChanged();
	}
	
	/**************************
	 ***** Event Handling *****
	 **************************/
	
	@Override
	public void screenResized() {
		
	}
	
	@Override
	public void wakeUp(float notept1, float notept2) {
		dataPts1.clear();
		dataPts2.clear();
		readerA.wakeUp(notept1);
		readerB.wakeUp(notept2);
	}
	
	@Override
	public void settingsChanged() {
		boolean scoreModeChanged = ((scoreMode.toInt() == MOVE_NOTES && x != 0 && y != 0) || 
				                    (scoreMode.toInt() == MOVE_SPAWN_POINT && x == 0 && y == 0));
		
		if (scoreModeChanged) {
			if (scoreMode.toInt() == MOVE_NOTES) {
				x = 0;
				y = 0;
				halfWidth = getWidth() * 0.5f;
				halfHeight = getHeight() * 0.3f;
			}
			else if (scoreMode.toInt() == MOVE_SPAWN_POINT) {
				x = spawnX1;
				y = spawnY1;
				halfWidth = getWidth() * 0.25f;
				halfHeight = getHeight() * 0.15f;
			}
		}
	}
	
	/******************
	 ***** Update *****
	 ******************/
	
	@Override
	public void update(int dt, float dNotept1, float dNotept2) {
		if (pa.currentPhrase.getNumNotes() > 0) {
			pa.pushMatrix();
			
			pa.translate(getCenx(), getCeny());
			
			readerA.update(dNotept1);
			readerB.update(dNotept2);
			
			float dx = -dNotept1 * pixelsPerWholeNote;
			
			drawDataPoints(dataPts1, (colorScheme.toInt() == MONOCHROMATIC) ? 0 : pa.getColor1());
			drawDataPoints(dataPts2, (colorScheme.toInt() == MONOCHROMATIC) ? 0 : pa.getColor2());
			
			pa.popMatrix();
			
			if (scoreMode.toInt() == MOVE_NOTES) {
				scroll(dx, dataPts1);
				scroll(dx, dataPts2);
			}
			else if (scoreMode.toInt() == MOVE_SPAWN_POINT) {
				moveSpawnPoint(-dx, getHeight() * 0.3f + NOTE_SIZE);
			}
			fade(dataPts1, dt);
			fade(dataPts2, dt);
		}
	}
	
	/**
	 * Draws the given list of data points with the given color.
	 * @param dataPts The list of data points.
	 * @param color The color.
	 */
	private void drawDataPoints(ArrayList<DataPoint> dataPts, int color) {
		for (DataPoint pt : dataPts) {
			pt.display(color);
		}
	}
	
	/**
	 * Translates a list of data points horizontally by the given dx.
	 * 
	 * @param dx The amount of translation.
	 * @param dataPts The list of data points to translate.
	 */
	private void scroll(float dx, ArrayList<DataPoint> dataPts) {
		//translate data points	
		for (DataPoint pt : dataPts) {
			pt.translate(dx);
		}
		
		//get rid of any data points that are out of bounds
		while (dataPts.size() > 0 && dataPts.get(0).startX < -halfWidth) {
			dataPts.remove(0);
		}
	}
	
	/**
	 * Fades away a list of data points by the fade rate, which is specified by a helper method fadeAmt(dt).
	 * @param dataPts The list of data points.
	 * @param dt The number of milliseconds since the last update() invocation.
	 */
	private void fade(ArrayList<DataPoint> dataPts, int dt) {
		for (DataPoint pt : dataPts) {
			pt.opacity -= fadeAmt(dt);
		}
	}
	
	/**
	 * @param dt The number of milliseconds since the last update() invocation.
	 * @return The amount to decrease opacity by given that dt milliseconds have passed.
	 */
	private float fadeAmt(int dt) {
		//TODO Test and make more precise if need be
		if (scoreMode.toInt() == MOVE_SPAWN_POINT) {
			return (10000 - pa.width) * 0.00000075f * dt;
		}
		else {
			return (10000 - pa.width) * 0.000002f * dt;
		}
	}
	
	/**
	 * Translates the note spawn point by (dx,dy).
	 * 
	 * @param dx The number of pixels to move the spawn point horizontally.
	 * @param dy The number of pixels to move the spawn point vertically.
	 */
	private void moveSpawnPoint(float dx, float dy) {
		if (x < spawnX2) {
			x += dx;
		}
		else {
			x = spawnX1;
			if (y < spawnY2) {
				y += dy;
			}
			else {
				y = spawnY1;
			}
		}
	}

	/**
	 * Callback from the PhraseReader. When the PhraseReader reads a new note it calls this method to plot that note.
	 * @param reader The PhraseReader invoking the callback.
	 */
	public void plotNote(PhraseReader reader) {
		int noteIndex = reader.getNoteIndex();
		ArrayList<DataPoint> dataPts = (reader.getId() == ONE_ID) ? dataPts1 : dataPts2;
		
		float y1 = -1;
		float y2 = -1;
		
		if (sineWave.toInt() == IS_SINE_WAVE) {
			float notept = -1;
			
			if (reader.getId() == ONE_ID) {
				durationAcc1 = (noteIndex == 0) ? 0 : durationAcc1 + pa.currentPhrase.getSCDuration(noteIndex-1);
				notept = durationAcc1;
			}
			else if (reader.getId() == TWO_ID) {
				durationAcc2 = (noteIndex == 0) ? 0 : durationAcc2 + pa.currentPhrase.getSCDuration(noteIndex-1);
				notept = durationAcc2;
			}
			
			float angle1 = PApplet.map(notept,
					                   0, pa.currentPhrase.getTotalDuration(),
					                   0, PApplet.TWO_PI);
			float angle2 = PApplet.map(notept+pa.currentPhrase.getSCDuration(noteIndex),
									   0, pa.currentPhrase.getTotalDuration(),
									   0, PApplet.TWO_PI);
			
			y1 = y + PApplet.sin(angle1)*halfHeight;
			y2 = y + PApplet.sin(angle2)*halfHeight;
		}
		else {
			y1 = noteIndexToY(noteIndex);
			y2 = noteIndexToY((noteIndex+1) % ys.length);
		}
		
		dataPts.add(new DataPoint(x, y1,
                	x + pixelsPerWholeNote*pa.currentPhrase.getSCDuration(noteIndex),
                	opacity));
	}
	
	/**
	 * Looks up a note given its index and maps its pitch to a y-coordinate.
	 * 
	 * @param noteIndex The index of the note to the Phrase.
	 * @return The y-coordinate.
	 */
	private float noteIndexToY(int noteIndex) {
		float minPitch = pa.currentPhrase.minPitch();
		float maxPitch = pa.currentPhrase.maxPitch();

		if (minPitch != maxPitch) {
			return y + pa.map(pa.currentPhrase.getSCPitch(noteIndex), minPitch, maxPitch, halfHeight, -halfHeight);
		}
		else {
			return y + pa.lerp(minPitch, maxPitch, 0.5f);
		}
	}
	
	/***************************
	 ***** DataPoint class *****
	 ***************************/
	
	/**
	 * A single note in the pitch/time plot of notes, with a start point, an end point, an opacity, and an index to the Phrase.
	 * 
	 * @author James Morrow
	 *
	 */
	private class DataPoint {
		float startX, startY, endX;
		float opacity;
		
		/**
		 * 
		 * @param startX The x-coordinate (an image of time) at which the note starts.
		 * @param startY The y-coordinate (an image of pitch) of the note.
		 * @param endX The x-coordinate (an image of time) at which the note ends.
		 * @param opacity
		 */
		private DataPoint(float startX, float startY, float endX, float opacity) {
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.opacity = opacity;
		}
		
		/**
		 * Translates the note by (dx,0).
		 * 
		 * @param dx The number of pixels to translate the note horizontally.
		 */
		void translate(float dx) {
			startX += dx;
			endX += dx;
		}
		
		/**
		 * Displays the note with the given color.
		 * 
		 * @param color
		 */
		void display(int color) {
			pa.stroke(color, opacity);
			pa.strokeWeight(NOTE_SIZE);
			pa.fill(color, opacity);
			if (noteGraphic.toInt() == DOTS2) {
				pa.strokeCap(pa.ROUND);
				float x1 = startX + roundStrokeCapSurplus;
				float x2 = endX - roundStrokeCapSurplus;
				pa.line(x1, startY, x2, startY);
			}
			else if (noteGraphic.toInt() == RECTS) {
				pa.strokeCap(pa.SQUARE);
				pa.line(startX, startY, endX, startY);
			}
			pa.strokeCap(pa.ROUND); //back to default stroke cap
		}
	}
}