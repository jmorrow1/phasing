package views;

import java.lang.reflect.Method;
import java.util.ArrayList;

import geom.Rect;
import phasing.PhasesPApplet;
import phasing.PhraseReader;
import phasing.PlayerInfo;
import processing.core.PApplet;
import util.ModInt;

/**
 * The LiveScorer View type. It listens for note events and every time it receives one,
 * it adds a new note to a plot of pitch/time.
 * 
 * @author James Morrow
 *
 */
public class LiveScorer extends View {
	//phrase readers:
	private PhraseReader readerA, readerB;
	
	//where to spawn new points:
	private float spawnX;
	private float spawnY;
	
	//used in calculating where to spawn new points when scoreMode is MOVE_SPAWN_POINT:
	private float startSpawnX, startSpawnY, endSpawnX, endSpawnY;
	
	//labels of plot's y-axis:
	private float[] ys;
	private float halfHeight;
	private float halfWidth;
	
	//plot data container:
	private ArrayList<DataPoint> dataPts1 = new ArrayList<DataPoint>();
	private ArrayList<DataPoint> dataPts2 = new ArrayList<DataPoint>();
	
	//musical time bookkeeping:
	private float durationAcc1, durationAcc2;
	
	//pixels to musical time conversion
	private final int PIXELS_PER_WHOLE_NOTE = 60;
	
	//this helps make it such that a quarter note is drawn as a circle when the stroke cap is round:
	private final float ROUND_STROKE_CAP_SURPLUS = PIXELS_PER_WHOLE_NOTE/8f;
	
	//other
	private float fadeRate;
	private final int ONE_ID = 1, TWO_ID = 2;
	private int startingPitch = 0;
	private float noteSize;
	
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
	 * @param viewBox The area in which to draw.
	 * @param opacity The opacity of notes.
	 * @param playerInfo Contains information (potentially) about how to initialize the view's settings.
	 * @param pa The PhasesPApplet instance.
	 */
	public LiveScorer(Rect viewBox, int opacity, PlayerInfo playerInfo, PhasesPApplet pa) {
		super(viewBox, opacity, playerInfo, pa);	
		init();
		loadSettings(playerInfo);
	}
	
	/**
	 * Constructs a LiveScorer whose option values are taken from the another LiveScorer.
	 * 
	 * @param ls The LiveScorer this one derives its option values from.
	 * @param viewBox The area in which to draw.
	 * @param opacity The opacity of notes.
	 * @param playerInfo Contains information (potentially) about how to initialize the view's settings.
	 * @param pa The PhasesPApplet instance.
	 */
	public LiveScorer(LiveScorer ls, Rect viewBox, int opacity, PlayerInfo playerInfo, PhasesPApplet pa) {
		super(viewBox, opacity, playerInfo, pa);
		copyOptionValues(ls);
		init();
		loadSettings(playerInfo);
	}
	

	/**
	 * Copies the given LiveScorer object's option values into this LiveScorer object's option variables.
	 * @param ls The given LiveScorer.
	 */
	private void copyOptionValues(LiveScorer ls) {
		this.sineWave.setValue(ls.sineWave.toInt());
		this.scoreMode.setValue(ls.scoreMode.toInt());
		this.noteGraphic.setValue(ls.noteGraphic.toInt());
		this.colorScheme.setValue(ls.colorScheme.toInt());
	}
	
	/**
	 * Initializes the LiveScorer object.
	 */
	private void init() {
		initFadeRate();
		initNoteSize();
		initPhraseReaders();
		initSpawnVariables();
		initPossibleYValuesForMoveNotesMode();
	}
	
	/**
	 * Initializes a number that determines the rate of note fading.
	 */
	private void initFadeRate() {
		//The fadeRate increases as the width decreases
		fadeRate = 1.69139f * PApplet.pow(10f, -14f) * PApplet.pow(pa.width, 4f)
					- 9.13027f * PApplet.pow(10f, -11f) * PApplet.pow(pa.width, 3f)
					+ 1.81626f * PApplet.pow(10f, -7f) * PApplet.pow(pa.width, 2f)
					- 0.000163378f * pa.width + 0.0642531f;
	}
	
	/**
	 * Initializes the variable that determines the (stroke) size of notes.
	 */
	private void initNoteSize() {
		float h = getHeight();
		noteSize = (0 < h && h < 800) ? PApplet.map(h, 0, 800, 12, 24) : 24;
	}
	
	/**
	 * Initializes the PhraseReaders, which read the Phrase and send events to the LiveScorer object whenever they encounter a new note.
	 * When a LiveScorer object receives a note event, it draws it.
	 */
	private void initPhraseReaders() {
		try {
			Method callback = LiveScorer.class.getMethod("plotNote", PhraseReader.class);
			readerA = new PhraseReader(pa.currentPhrase, ONE_ID, this, callback);
			readerB = new PhraseReader(pa.currentPhrase, TWO_ID, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initializes an array of possible y-values for notes, so they don't have to be constantly recomputed.
	 * These values are used when (scoreMode.toInt() == MOVE_NOTES) but not when (scoreMode.toInt() == MOVE_SPAWN_POINT).
	 */
	private void initPossibleYValuesForMoveNotesMode() {
		ys = new float[pa.currentPhrase.getNumNotes()];
		
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
	}
	
	/**
	 * Initializes variables for that define the boundaries of the note spawn point, which depends on the scoreMode.
	 * Then initializes the spawn point.
	 */
	private void initSpawnVariables() {	
		startSpawnX = -getWidth() * 0.5f;
		startSpawnY = -getHeight() * 0.15f - noteSize/2f;
		endSpawnX = getWidth() * 0.5f;
		endSpawnY = getHeight() * 0.15f + noteSize/2f;
		
		halfWidth = (scoreMode.toInt() == MOVE_NOTES) ? (getWidth() * 0.5f) : (getWidth() * 0.25f);
		halfHeight = (scoreMode.toInt() == MOVE_NOTES) ? (getHeight() * 0.3f) : (getHeight() * 0.15f);
		
		spawnX = (scoreMode.toInt() == MOVE_SPAWN_POINT) ? startSpawnX : 0 + getWidth()/6;
		spawnY = (scoreMode.toInt() == MOVE_SPAWN_POINT) ? startSpawnY : 0;
	}
	
	/**************************
	 ***** Event Handling *****
	 **************************/
	
	@Override
	protected void resized(float prevWidth, float prevHeight) {
		float prevHalfHeight = halfHeight;
		initNoteSize();
		initSpawnVariables();
		for (int i=0; i<ys.length; i++) {
			ys[i] = PApplet.map(ys[i], prevHalfHeight, -prevHalfHeight, halfHeight, -halfHeight);
		}
		for (DataPoint pt : dataPts1) {
			pt.startY = PApplet.map(pt.startY, prevHalfHeight, -prevHalfHeight, halfHeight, -halfHeight);
		}
		for (DataPoint pt : dataPts2) {
			pt.startY = PApplet.map(pt.startY, prevHalfHeight, -prevHalfHeight, halfHeight, -halfHeight);
		}
		initFadeRate();
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
		boolean scoreModeChanged = ((scoreMode.toInt() == MOVE_NOTES && spawnY != 0) || 
				                    (scoreMode.toInt() == MOVE_SPAWN_POINT && spawnY == 0));
		
		if (scoreModeChanged) {
			initSpawnVariables();
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
			
			float dx = -dNotept1 * PIXELS_PER_WHOLE_NOTE;
			
			drawDataPoints(dataPts1, (colorScheme.toInt() == MONOCHROMATIC) ? 0 : pa.getColor1());
			drawDataPoints(dataPts2, (colorScheme.toInt() == MONOCHROMATIC) ? 0 : pa.getColor2());
			
			pa.popMatrix();
			
			if (scoreMode.toInt() == MOVE_NOTES) {
				scroll(dx, dataPts1);
				scroll(dx, dataPts2);
			}
			else if (scoreMode.toInt() == MOVE_SPAWN_POINT) {
				moveSpawnPoint(-dx, getHeight() * 0.3f + noteSize);
			}
			fade(dataPts1, dt);
			fade(dataPts2, dt);
		}
	}
	
	/**
	 * Draws the given list of data points with the given color.
	 * 
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
	 * 
	 * @param dataPts The list of data points.
	 * @param dt The number of milliseconds since the last update() invocation.
	 */
	private void fade(ArrayList<DataPoint> dataPts, int dt) {
		for (DataPoint pt : dataPts) {
			pt.opacity -= fadeAmt(dt);
		}
	}
	
	/**
	 * Gives the fade amount, given some delta time in milliseconds.
	 * 
	 * @param dt The number of milliseconds since the last update() invocation.
	 * @return The amount to decrease opacity by given that dt milliseconds have passed.
	 */
	private float fadeAmt(int dt) {
		if (scoreMode.toInt() == MOVE_SPAWN_POINT) {
			float amt = 0.5f * dt * fadeRate;
			return amt;
		}
		else {
			float amt = dt * fadeRate;
			return amt;
		}
	}
	
	/**
	 * Translates the note spawn point by (dx,dy).
	 * 
	 * @param dx The number of pixels to move the spawn point horizontally.
	 * @param dy The number of pixels to move the spawn point vertically.
	 */
	private void moveSpawnPoint(float dx, float dy) {
		if (spawnX < endSpawnX) {
			spawnX += dx;
		}
		else {
			spawnX = startSpawnX;
			if (spawnY < endSpawnY) {
				spawnY += dy;
			}
			else {
				spawnY = startSpawnY;
			}
		}
	}

	/**
	 * Callback from the PhraseReader. When the PhraseReader reads a new note it calls this method to plot that note.
	 * @param reader The PhraseReader invoking the callback.
	 */
	public void plotNote(PhraseReader reader) {
		int noteIndex = reader.getNoteIndex();
		
		if (!pa.currentPhrase.isRest(noteIndex)) {
			ArrayList<DataPoint> dataPts = (reader.getId() == ONE_ID) ? dataPts1 : dataPts2;
			
			float y1 = -1;
			float y2 = -1;
			
			if (sineWave.toInt() == IS_SINE_WAVE &&
					!pa.currentPhrase.isRest(reader.getNoteIndex())) {
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
				
				y1 = spawnY + PApplet.sin(angle1)*halfHeight;
				y2 = spawnY + PApplet.sin(angle2)*halfHeight;
			}
			else {
				y1 = noteIndexToY(noteIndex);
				y2 = noteIndexToY((noteIndex+1) % ys.length);
			}
			
			dataPts.add(new DataPoint(spawnX, y1,
	                	spawnX + PIXELS_PER_WHOLE_NOTE*pa.currentPhrase.getSCDuration(noteIndex),
	                	opacity));
		}
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
			return spawnY + pa.map(pa.currentPhrase.getSCPitch(noteIndex), minPitch, maxPitch, halfHeight, -halfHeight);
		}
		else {
			return spawnY + pa.lerp(minPitch, maxPitch, 0.5f);
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
			pa.strokeWeight(noteSize);
			pa.fill(color, opacity);
			if (noteGraphic.toInt() == DOTS2) {
				pa.strokeCap(pa.ROUND);
				float x1 = startX + ROUND_STROKE_CAP_SURPLUS;
				float x2 = endX - ROUND_STROKE_CAP_SURPLUS;
				pa.line(x1, startY, x2, startY);
			}
			else if (noteGraphic.toInt() == RECTS) {
				pa.strokeCap(pa.SQUARE);
				pa.line(startX, startY, endX, startY);
			}
			pa.strokeCap(pa.ROUND); //back to default stroke cap
		}
	}
	
	/***************************************
	 ***** Saving and Loading Settings *****
	 ***************************************/
	
	@Override
	public void saveSettings(PlayerInfo playerInfo) {
		save(sineWave, "sineWave", playerInfo);
		save(scoreMode, "scoreMode", playerInfo);
		save(noteGraphic, "noteGraphic2", playerInfo);
		save(colorScheme, "colorScheme", playerInfo);
	}
	
	@Override
	protected void loadSettings(PlayerInfo playerInfo) {
		tryToSet(sineWave, "sineWave", playerInfo);
		tryToSet(scoreMode, "scoreMode", playerInfo);
		tryToSet(noteGraphic, "noteGraphic2", playerInfo);
		tryToSet(colorScheme, "colorScheme", playerInfo);
		settingsChanged();
	}
}