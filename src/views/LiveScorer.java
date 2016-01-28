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
	
	//where to spawn new points
	private float x;
	private float y;
	
	//used in calculating where to spawn new points when scoreMode is MOVE_SPAWN_POINT
	private float spawnX1, spawnY1, spawnX2, spawnY2;
	
	//labels of plot's y-axis:
	private float[] ys;
	private float halfHeight;
	private float halfWidth;
	
	//plot data container:
	private ArrayList<DataPoint> dataPts1 = new ArrayList<DataPoint>();
	private ArrayList<DataPoint> dataPts2 = new ArrayList<DataPoint>();
	
	//other:
	private float pixelsPerWholeNote;
	private float radiansPerWholeNote;
	private final int ONE_ID = 1, TWO_ID = 2;
	private int startingPitch = 0;
	private float fadeRate = 0.25f;
	
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
		spawnX1 = -halfWidth;
		spawnY1 = -halfHeight;
		spawnX2 = halfWidth;
		spawnY2 = halfHeight;
		
		//pixels to musical time conversion
		pixelsPerWholeNote = 60;
		radiansPerWholeNote = pa.TWO_PI / pa.getBPM1();
		
		onEnter();
	}
	
	public void updateState() {
		if (scoreMode.toInt() == MOVE_NOTES) {
			x = this.getCenx();
			y = this.getCeny();
		}
		else if (scoreMode.toInt() == MOVE_SPAWN_POINT) {
			
		}
	}
	
	public void onEnter() {
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
			//fade(dataPts1);
			//fade(dataPts2);
		}
		else if (scoreMode.toInt() == MOVE_SPAWN_POINT) {
			pa.println("dx = " + (-dx) + ", x = " + x + ", y = " + y);
			moveSpawnPoint(-dx);
		}
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
	
	private void moveSpawnPoint(float dx) {
		if (x < spawnX2) {
			x += dx;
		}
		else {
			x = spawnX1;
			if (y < spawnY2) {
				y += dx;
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
		
		float y1 = 0;
		float y2 = 0;
		
		if (sineWave.toInt() == IS_SINE_WAVE) {
			float noteDuration = (noteIndex == 0) ? 0 : pa.phrase.getSCDuration(noteIndex-1);

			float angle1 = PApplet.map(noteDuration,
					                   0, pa.phrase.getTotalDuration(),
					                   0, PApplet.TWO_PI);
			float angle2 = PApplet.map(noteDuration+pa.phrase.getSCDuration(noteIndex),
									   0, pa.phrase.getTotalDuration(),
									   0, PApplet.TWO_PI);
			
			y1 = PApplet.sin(angle1)*halfHeight;
			y2 = PApplet.sin(angle2)*halfHeight;
		}
		else {
			y1 = ys[noteIndex];
			y2 = ys[(noteIndex+1) % ys.length];
		}
		
		dataPts.add(new DataPoint(0, y1,
                	pixelsPerWholeNote*pa.phrase.getSCDuration(noteIndex), y2,
                	noteIndex, opacity));
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
			pa.strokeWeight(2);
			pa.fill(color, opacity);
			if (noteGraphic.toInt() == DOTS) {
				pa.ellipseMode(pa.CENTER);
				pa.ellipse(startX, startY, 20, 20);
			}
			else if (noteGraphic.toInt() == RECTS) {
				pa.rectMode(pa.CORNERS);
				pa.rect(startX, startY, endX, startY + 20);
			}
		}
	}
}