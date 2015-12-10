package views;

import java.util.LinkedList;
import java.util.Queue;

import geom.Rect;
import phases.ColoredDot;
import phases.PhasesPApplet;
import phases.Phrase;

public class RhythmView extends View {
	private double x1, x2, y1, y2, width;
	private double noteX, noteY, dNoteY;
	private double pixelsPerWholeNote;
 	private Queue<ColoredDot> dots = new LinkedList<ColoredDot>();
 	private final int DOT_DIAM = 12;
 	
 	private PhraseReader a, b;
	
	public RhythmView(Rect rect, Phrase phrase, int color1, int color2, int opacity, PhasesPApplet pa) {
		super(rect, color1, color2, opacity, pa);
		
		x1 = this.getX1() + 10;
		x2 = this.getX2() - 10;
		width = x2 - x1;
		y1 = this.getY1() + 10;
		y2 = this.getY2() - 10;
		dNoteY = DOT_DIAM * 1.5f;
		pixelsPerWholeNote = 100;
		
		noteX = x1;
		noteY = y1;
		
		a = new PhraseReader(phrase, color1);
		b = new PhraseReader(phrase, color2);
		
		writeNote(color1);
		writeNote(color2);
	}

	@Override
	public void update(double dNotept1, double dNotept2) {
		noteX += pixelsPerWholeNote*dNotept1;
		if (noteX >= x2) {
			noteX = x1;
			noteY += dNoteY;
			if (noteY >= y2) {
				noteX = x1;
				noteY = y1;
			}
		}
		
		a.update(dNotept1);
		b.update(dNotept2);
		
		ColoredDot.style(pa);
		for (ColoredDot d : dots) {
			d.display(pa);
			d.opacity -= 0.1f;
		}
		
		if (dots.size() > 0 && dots.peek().opacity <= 0) {
			dots.remove();
		}
	}
	
	//callback:
	private void writeNote(int color) {
		dots.add(new ColoredDot(noteX, noteY, DOT_DIAM, color, opacity));
	}
	
	private class PhraseReader {
		Phrase phrase;
		int noteIndex;
		double noteTimeTillNextNote;
		int color;
		
		PhraseReader(Phrase phrase, int color) {
			this.phrase = phrase;
			noteIndex = 0;
			noteTimeTillNextNote = phrase.getDuration(noteIndex);
			this.color = color;
		}
		
		void update(double dNotept) {
			noteTimeTillNextNote -= dNotept;
			
			if (noteTimeTillNextNote <= 0) {
				noteIndex = (noteIndex+1) % phrase.getNumNotes();
				noteTimeTillNextNote = noteTimeTillNextNote + phrase.getDuration(noteIndex);
				writeNote(color); //<-- callback
			}
		}
	}
}