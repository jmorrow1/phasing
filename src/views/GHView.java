package views;

import geom.Rect;
import phases.Phrase;
import processing.core.PApplet;

public class GHView extends View {
	//piano
	Piano piano;
	//conversion
	int firstPitchOfPiano = 60;
	float rectHeightOfWholeNote = 100;
	//rects
	Rect[] set1, set2;
	int pointer1=0, pointer2=0; //points to rectangle closest to edge
	
	public GHView(Rect rect, Phrase phrase, PApplet pa) {
		super(rect, phrase, pa);
		piano = new Piano(3, new Rect(getX1(), getY2() - 50, getWidth(), 50, PApplet.CORNER), true);
		//piano = new Piano(3, new Rect(getX1(), getY1(), 50, getHeight(), PApplet.CORNER), true);
		set1 = initSet(phrase);
		set2 = initSet(phrase);
	}
	
	public void update(float dBeatpt1, float dBeatpt2) {
		piano.display(pa);
		pa.fill(255, 100, 100, 100);
		pointer1 = update(set1, dBeatpt1, pointer1);
		pa.fill(100, 100, 255, 100);
		pointer2 = update(set2, dBeatpt2, pointer2);
	}
	
	private int update(Rect[] set, float dBeatpt, int pointer) {
		float dy = dBeatpt * rectHeightOfWholeNote;
		
		for (int i=0; i<set.length; i++) {
			set[i].display(pa);
			set[i].translate(0, dy);
		}
		
		return wrap(set, pointer);
	}
	
	private int wrap(Rect[] set, int pointer) {
		//handle wrapping
		pa.rectMode(pa.CORNER);
		pa.rect(set[pointer].getX1(), set[pointer].getY1() - this.getHeight(),
				set[pointer].getWidth(), set[pointer].getHeight());
		
		//if rect is entirely out of bounds, translate it back and modfiy pointer
		if (set[pointer].getY1() > this.getY2()) {
			set[pointer].translate(0, -this.getHeight());
			pointer = (pointer+1) % set.length;
		}
		
		return pointer;
	}
	
	private Rect[] initSet(Phrase phrase) {
		Rect[] set = new Rect[phrase.getNumNotes()];
		float dy = 0;
		set[0] = noteToRect(phrase, 0);
		for (int i=1; i<phrase.getNumNotes(); i++) {
			set[i] = noteToRect(phrase, i);
			dy -= set[i-1].getHeight()/2f + set[i].getHeight()/2f;
			set[i].translate(0, dy);
		}
		
		return set;
	}
	
	private Rect noteToRect(Phrase phrase, int noteIndex) {
		Rect rect = piano.getKeyCopy(phrase.getPitch(noteIndex) - firstPitchOfPiano);
		
		rect.setHeight(rectHeightOfWholeNote * phrase.getDuration(noteIndex));
		rect.setY1(piano.getY2() - rect.getHeight());

		return rect;
	}
}