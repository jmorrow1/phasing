package views;

import java.util.LinkedList;
import java.util.Queue;

import geom.Rect;
import phases.ColoredDot;
import phases.Phrase;
import processing.core.PApplet;

public class LiveGraphView extends View {
	private Queue<ColoredDot> dots;
	private final float DOT_DIAM = 8;
	private float x;
	private float[] ys; //maps to notes in the phrase
	private float pixelsPerWholeNote;
	private PhraseReader readerA, readerB;

	public LiveGraphView(Rect rect, Phrase phrase, int color1, int color2, int opacity, PApplet pa) {
		super(rect, color1, color2, opacity, pa);
		
		pixelsPerWholeNote = 50;
		
		dots = new LinkedList<ColoredDot>();
		
		x = this.getCenx();
		
		ys = new float[phrase.getNumNotes()];	
		float y1 = this.getY1() + this.getHeight()/2.5f;
		float y2 = this.getY2() - this.getHeight()/2.5f;
		float minPitch = phrase.minPitch();
		float maxPitch = phrase.maxPitch();
		for (int i=0; i<ys.length; i++) {
			ys[i] = PApplet.map(phrase.getPitch(i), minPitch, maxPitch, y2, y1);
		}
		
		readerA = new PhraseReader(phrase, color1);
		readerB = new PhraseReader(phrase, color2);
		
		plotNote(0, color1);
		plotNote(0, color2);
	}

	@Override
	public void update(float dNotept1, float dNotept2) {
		float dx = -dNotept1 * pixelsPerWholeNote;
		
		readerA.update(dNotept1);
		readerB.update(dNotept2);
		
		moveQueue();
		updateGraph(dx);
	}
	
	private void updateGraph(float dx) {
		//draw it and translate it
		pa.stroke(100);
		pa.noFill();
		pa.beginShape();
		for (ColoredDot d : dots) {
			pa.vertex(d.x, d.y);
		}
		pa.endShape();
		
		ColoredDot.style(pa);
		for (ColoredDot d : dots) {
			d.display(pa);
			d.x += dx;
		}
	}
	
	private void moveQueue() {
		while (dots.size() > 0 && dots.peek().x < this.getX1()) {
			dots.remove();
		}
	}
	
	//callback:
	private void plotNote(int noteIndex, int color) {
		dots.add(new ColoredDot(x, ys[noteIndex], DOT_DIAM, color, opacity));
	}
	
	private class PhraseReader {
		Phrase phrase;
		int noteIndex;
		float noteTimeTillNextNote;
		int color;
		
		PhraseReader(Phrase phrase, int color) {
			this.phrase = phrase;
			noteIndex = 0;
			noteTimeTillNextNote = phrase.getDuration(noteIndex);
			this.color = color;
		}
		
		void update(float dNotept) {
			noteTimeTillNextNote -= dNotept;
			
			if (noteTimeTillNextNote <= 0) {
				noteIndex = (noteIndex+1) % phrase.getNumNotes();
				noteTimeTillNextNote = noteTimeTillNextNote + phrase.getDuration(noteIndex);
				plotNote(noteIndex, color); //<-- callback
			}
		}
	}
}