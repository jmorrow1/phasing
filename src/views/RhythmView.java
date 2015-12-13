package views;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;

import geom.Rect;
import phases.ColoredDot;
import phases.Phrase;
import phases.PhraseReader;
import processing.core.PApplet;

public class RhythmView extends View {
	private float x1, x2, y1, y2, width;
	private float noteX, noteY, dNoteY;
	private float pixelsPerWholeNote;
 	private Queue<ColoredDot> dots = new LinkedList<ColoredDot>();
 	private final int DOT_DIAM = 12;
 	private PhraseReader readerA, readerB;
 	
 	private final int ONE_ID = 0, TWO_ID = 1;
	
	public RhythmView(Rect rect, Phrase phrase, int color1, int color2, int opacity, PApplet pa) {
		super(rect, phrase, color1, color2, opacity, 0, pa);
		
		x1 = this.getX1() + 10;
		x2 = this.getX2() - 10;
		width = x2 - x1;
		y1 = this.getY1() + 10;
		y2 = this.getY2() - 10;
		dNoteY = DOT_DIAM * 1.5f;
		pixelsPerWholeNote = 100;
		
		noteX = x1;
		noteY = y1;
		
		try {
			Method callback = RhythmView.class.getMethod("writeNote", PhraseReader.class);
			readerA = new PhraseReader(phrase, ONE_ID, this, callback);
			readerB = new PhraseReader(phrase, TWO_ID, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
		noteX += pixelsPerWholeNote*dNotept1;
		if (noteX >= x2) {
			noteX = x1;
			noteY += dNoteY;
			if (noteY >= y2) {
				noteX = x1;
				noteY = y1;
			}
		}
		
		readerA.update(dNotept1);
		readerB.update(dNotept2);
		
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
	public void writeNote(PhraseReader reader) {
		dots.add(new ColoredDot(noteX, noteY, DOT_DIAM,
				(reader.getId() == ONE_ID) ? color1 : color2, opacity, reader.getId()));
	}
	
	@Override
	public int numPresets() {
		return 1;
	}

	@Override
	public void loadPreset(int preset) {
		//RhythmView has only the default preset at the moment
	}
}