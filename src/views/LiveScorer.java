package views;

import java.lang.reflect.Method;
import java.util.ArrayList;

import geom.Rect;
import phases.PhasesPApplet;
import phases.PhraseReader;
import processing.core.PApplet;

public class LiveScorer extends View {
	//phrase readers:
	private PhraseReader readerA, readerB;
	
	//where to spawn new points
	private float x;
	private float y;
	
	//labels of plot's y-axis:
	private float[] ys;
	private float minY, maxY;
	
	//plot data container:
	private ArrayList<DataPoint> dataPts1 = new ArrayList<DataPoint>();
	private ArrayList<DataPoint> dataPts2 = new ArrayList<DataPoint>();
	
	//other:
	private float pixelsPerWholeNote;
	private float radiansPerWholeNote;
	private float durationAcc1, durationAcc2;
	private final int ONE_ID = 1, TWO_ID = 2;
	private int startingPitch = 0;
	
	//options:
	private boolean sineWave = true;
	
	private final int SCROLLS=0, FADES=1;
	private int scrollsOrFades=SCROLLS;
	
	private final int DOTS=0, SYMBOLS=1, CONNECTED_DOTS=2, RECTS=3;
	private int noteType = DOTS;
	
	private final int MONOCHROME=0, DIACHROME=1;
	private int colorSchemeType = DIACHROME;
	
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
		x = this.getCenx();
		y = this.getCeny();
		
		//labels of y-axis
		ys = new float[pa.phrase.getNumNotes()];	
		minY = this.getY1() + this.getHeight()/3f;
		maxY = this.getY2() - this.getHeight()/3f;
		float minPitch = pa.phrase.minPitch();
		float maxPitch = pa.phrase.maxPitch();
		for (int i=0; i<ys.length; i++) {
			ys[i] = PApplet.map(pa.phrase.getSCPitch(i), minPitch, maxPitch, minY, maxY);
		}
		
		//pixels to musical time conversion
		pixelsPerWholeNote = 60;
		radiansPerWholeNote = pa.TWO_PI / pa.getBPM1();
		
		onEnter();
	}
	
	public void onEnter() {
		pa.textAlign(pa.CENTER, pa.CENTER);
		pa.textSize(24);
	}
	
	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
		readerA.update(dNotept1);
		readerB.update(dNotept2);
		
		float dx = -dNotept1 * pixelsPerWholeNote;
		
		scroll(dx, dataPts1, (colorSchemeType == MONOCHROME) ? 0 : pa.getColor1());
		scroll(dx, dataPts2, (colorSchemeType == MONOCHROME) ? 0 : pa.getColor2());

	}
	
	private void scroll(float dx, ArrayList<DataPoint> dataPts, int color) {
		//translate data points	
		for (DataPoint pt : dataPts) {
			pt.translate(dx);
		}
		
		//draw data points
		for (int i=0; i<dataPts.size(); i++) {
					
			DataPoint pt = dataPts.get(i);
			
			if (i != dataPts.size()-1) {
				pt.display(color, false);
			}
			else {
				pt.display(color, true);
			}
			
		}
		
		//get rid of any data points that are out of bounds
		while (dataPts.size() > 0 && dataPts.get(0).startX < this.getX1()) {
			dataPts.remove(0);
		}
	}

	//callback:
	public void plotNote(PhraseReader reader) {
		int noteIndex = reader.getNoteIndex();
		ArrayList<DataPoint> dataPts = (reader.getId() == ONE_ID) ? dataPts1 : dataPts2;
		
		float y1 = 0;
		float y2 = 0;
		
		if (sineWave) {
			float durationAcc = 0;
		
			if (reader.getId() == ONE_ID) {
				durationAcc1 = (noteIndex == 0) ? 0 : (durationAcc1 + pa.phrase.getSCDuration(noteIndex-1));
				durationAcc = durationAcc1;
			}
			else {
				durationAcc2 = (noteIndex == 0) ? 0 : (durationAcc2 + pa.phrase.getSCDuration(noteIndex-1));
				durationAcc = durationAcc2;
			}
				
			float angle1 = PApplet.map(durationAcc, 0, pa.phrase.getTotalDuration(), 0, PApplet.TWO_PI);
			float angle2 = PApplet.map(durationAcc+pa.phrase.getSCDuration(noteIndex),
					0, pa.phrase.getTotalDuration(), 0, PApplet.TWO_PI);
			
			y1 = y + PApplet.sin(angle1)*200;
			y2 = y + PApplet.sin(angle2)*200;
		}
		else {
			y1 = ys[noteIndex];
			y2 = ys[(noteIndex+1) % ys.length];
		}
		
		dataPts.add(new DataPoint(x, y1,
                x + pixelsPerWholeNote*pa.phrase.getSCDuration(noteIndex), y2,
                noteIndex));
	}
	
	class DataPoint {
		float startX, startY, endX, endY;
		int noteIndex;
		
		DataPoint(float startX, float startY, float endX, float endY, int noteIndex) {
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
			this.noteIndex = noteIndex;
		}
		
		void translate(float dx) {
			startX += dx;
			endX += dx;
		}
		
		void display(int color, boolean rightmostPoint) {
			pa.stroke(color, opacity);
			pa.strokeWeight(2);
			pa.fill(color, opacity);
			if (noteType == SYMBOLS) {
				int pitch = (int) (pa.phrase.getSCPitch(noteIndex) % 12);
				String symbol = pa.chromaticScales.getScale(startingPitch).getNoteName(pitch);
				pa.text(symbol, startX, startY);
			}
			else if (noteType == DOTS) {
				pa.ellipse(startX, startY, 20, 20);
			}
			else if (noteType == CONNECTED_DOTS) {
				pa.ellipse(startX, startY, 20, 20);
				if (!rightmostPoint) {
					pa.line(startX, startY, endX, endY);
				}
			}
			else if (noteType == RECTS) {
				pa.rectMode(pa.CORNERS);
				pa.rect(startX, startY, endX, startY + 20);
			}
		}
	}
}