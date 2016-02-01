package views;

import java.lang.reflect.Method;
import java.util.ArrayList;

import geom.Rect;
import phases.ModInt;
import phases.PhasesPApplet;
import phases.PhraseReader;
import processing.core.PApplet;

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
	private float fadeRate = 0.15f;
	private final float NOTE_SIZE = 20;
	private float roundStrokeCapSurplus;
	
	//options:
	public ModInt sineWave = new ModInt(1, numWaysOfBeingASineWaveOrNot, sineWaveName);
	public ModInt scoreMode = new ModInt(0, numScoreModes, scoreModeName);
	public ModInt noteGraphic = new ModInt(0, numNoteGraphicSet2s, noteGraphicSet2Name);
	public ModInt colorScheme = new ModInt(1, numColorSchemes, colorSchemeName);
	
	@Override
	public int numOptions() {
		return 4;
	}

	public LiveScorer(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect, opacity, pa);
		
		//phrase readers
		try {
			Method callback = LiveScorer.class.getMethod("plotNote", PhraseReader.class);
			readerA = new PhraseReader(pa.phrase, ONE_ID, this, callback);
			readerB = new PhraseReader(pa.phrase, TWO_ID, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		//note spawn point
		x = 0;
		y = 0;
		
		//labels of y-axis
		ys = new float[pa.phrase.getNumNotes()];
		halfWidth = this.getWidth() * 0.5f;
		halfHeight = this.getHeight() * 0.3f;
		float minPitch = pa.phrase.minPitch();
		float maxPitch = pa.phrase.maxPitch();
		for (int i=0; i<ys.length; i++) {
			if (minPitch != maxPitch) {
				ys[i] = PApplet.map(pa.phrase.getSCPitch(i), minPitch, maxPitch, halfHeight, -halfHeight);
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
		
		onEnter();
	}
	
	public void updateState() {
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
	
	public void onEnter() {
		updateState();
	}
	
	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
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
		fade(dataPts1);
		fade(dataPts2);
	}
	
	private void drawDataPoints(ArrayList<DataPoint> dataPts, int color) {
		//draw data points
		for (int i=0; i<dataPts.size(); i++) {			
			DataPoint pt = dataPts.get(i);
			pt.display(color);
		}
	}
	
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
	
	private void fade(ArrayList<DataPoint> dataPts) {
		//fade data points
		for (DataPoint pt : dataPts) {
			pt.opacity -= fadeRate;
		}
	}
	
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

	//callback:
	public void plotNote(PhraseReader reader) {
		int noteIndex = reader.getNoteIndex();
		ArrayList<DataPoint> dataPts = (reader.getId() == ONE_ID) ? dataPts1 : dataPts2;
		
		float y1 = -1;
		float y2 = -1;
		
		if (sineWave.toInt() == IS_SINE_WAVE) {
			float notept = -1;
			
			if (reader.getId() == ONE_ID) {
				durationAcc1 = (noteIndex == 0) ? 0 : durationAcc1 + pa.phrase.getSCDuration(noteIndex-1);
				notept = durationAcc1;
			}
			else if (reader.getId() == TWO_ID) {
				durationAcc2 = (noteIndex == 0) ? 0 : durationAcc2 + pa.phrase.getSCDuration(noteIndex-1);
				notept = durationAcc2;
			}
			
			float angle1 = PApplet.map(notept,
					                   0, pa.phrase.getTotalDuration(),
					                   0, PApplet.TWO_PI);
			float angle2 = PApplet.map(notept+pa.phrase.getSCDuration(noteIndex),
									   0, pa.phrase.getTotalDuration(),
									   0, PApplet.TWO_PI);
			
			y1 = y + PApplet.sin(angle1)*halfHeight;
			y2 = y + PApplet.sin(angle2)*halfHeight;
		}
		else {
			y1 = noteIndexToY(noteIndex);
			y2 = noteIndexToY((noteIndex+1) % ys.length);
		}
		
		dataPts.add(new DataPoint(x, y1,
                	x + pixelsPerWholeNote*pa.phrase.getSCDuration(noteIndex), y2,
                	noteIndex, opacity));
	}
	
	private float noteIndexToY(int noteIndex) {
		float minPitch = pa.phrase.minPitch();
		float maxPitch = pa.phrase.maxPitch();

		if (minPitch != maxPitch) {
			return y + pa.map(pa.phrase.getSCPitch(noteIndex), minPitch, maxPitch, halfHeight, -halfHeight);
		}
		else {
			return y + pa.lerp(minPitch, maxPitch, 0.5f);
		}
	}
	
	class DataPoint {
		float startX, startY, endX, endY;
		int noteIndex;
		float opacity;
		
		DataPoint(float startX, float startY, float endX, float endY, int noteIndex, float opacity) {
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
			this.noteIndex = noteIndex;
			this.opacity = opacity;
		}
		
		void translate(float dx) {
			startX += dx;
			endX += dx;
		}
		
		void display(int color) {
			pa.stroke(color, opacity);
			pa.strokeWeight(NOTE_SIZE);
			pa.fill(color, opacity);
			if (noteGraphic.toInt() == DOTS) {
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