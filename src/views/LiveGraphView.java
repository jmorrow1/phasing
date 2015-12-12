package views;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;

import geom.Rect;
import phases.ColoredDot;
import phases.Phrase;
import phases.PhraseReader;
import processing.core.PApplet;

public class LiveGraphView extends View {
	private Queue<ColoredDot> dots;
	private final float DOT_DIAM = 8;
	private float x;
	private float[] ys; //maps to notes in the phrase
	private float pixelsPerWholeNote;
	private PhraseReader readerA, readerB;
	//settings:
	private boolean drawLines, drawDots;
	//etc:
	private final int ONE_ID = 0, TWO_ID = 1;

	public LiveGraphView(Rect rect, Phrase phrase, int color1, int color2,
			int opacity, boolean drawLines, boolean drawDots, PApplet pa) {
		super(rect, color1, color2, opacity, pa);
		
		pixelsPerWholeNote = 50;
		
		dots = new LinkedList<ColoredDot>();
		
		x = this.getCenx();
		
		ys = new float[phrase.getNumNotes()];	
		float y1 = this.getY1() + this.getHeight()/3f;
		float y2 = this.getY2() - this.getHeight()/3f;
		float minPitch = phrase.minPitch();
		float maxPitch = phrase.maxPitch();
		for (int i=0; i<ys.length; i++) {
			ys[i] = PApplet.map(phrase.getPitch(i), minPitch, maxPitch, y2, y1);
		}
		
		try {
			Method callback = LiveGraphView.class.getMethod("plotNote", PhraseReader.class);
			readerA = new PhraseReader(phrase, ONE_ID, this, callback);
			readerB = new PhraseReader(phrase, TWO_ID, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		this.drawLines = drawLines;
		this.drawDots = drawDots;
	}

	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
		float dx = -dNotept1 * pixelsPerWholeNote;
		
		readerA.update(dNotept1);
		readerB.update(dNotept2);
		
		moveQueue();
		updateGraph(dx);
	}
	
	private void updateGraph(float dx) {
		for (ColoredDot d : dots) {
			d.x += dx;
		}
		
		if (drawLines) {
			pa.stroke(100);
			pa.noFill();
			pa.beginShape();
			for (ColoredDot d : dots) {
				pa.vertex(d.x, d.y);
			}
			pa.endShape();
		}
		
		if (drawDots) {
			ColoredDot.style(pa);
			for (ColoredDot d : dots) {
				d.display(pa);
			}
		}
	}
	
	private void moveQueue() {
		while (dots.size() > 0 && dots.peek().x < this.getX1()) {
			dots.remove();
		}
	}
	
	//callback:
	public void plotNote(PhraseReader reader) {
		dots.add(new ColoredDot(x, ys[reader.getNoteIndex()], DOT_DIAM,
				(reader.getId() == ONE_ID) ? color1 : color2, opacity, reader.getId()));
	}
	
	/*Settings*/
	
	public void drawLines(boolean drawLines) {
		this.drawLines = drawLines;
	}
	
	public void drawDots(boolean drawDots) {
		this.drawDots = drawDots;
	}
	
	public void setColor1(int color1) {
		this.color1 = color1;
		for (ColoredDot d : dots) {
			if (d.getId() == ONE_ID) {
				d.setColor(color1);
			}
		}
	}
	
	public void setColor2(int color2) {
		this.color2 = color2;
		for (ColoredDot d : dots) {
			if (d.getId() == TWO_ID) {
				d.setColor(color2);
			}
		}
	}
}
