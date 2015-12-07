package views;

import geom.Rect;
import phases.Phrase;
import processing.core.PApplet;

public class GHView extends View {
	//direction of note movement
	public final static int RIGHT=0, DOWN=1, LEFT=2, UP=3;
	int noteMovement;
	//piano
	Piano piano;
	//conversion
	int firstPitchOfPiano = 60;
	float rectHeightOfWholeNote;
	//rects
	Rect[] set1, set2;
	int pointer1=0, pointer2=0; //points to rectangle closest to edge
	
	public GHView(Rect rect, Phrase phrase, int noteMovement, PApplet pa) {
		super(rect, phrase, pa);
		
		this.noteMovement = noteMovement;
		switch (noteMovement) {
			case RIGHT:
				piano = new Piano(3, new Rect(getX2() - 50, getY1(), 50, getHeight(), PApplet.CORNER), false);
				rectHeightOfWholeNote = getWidth() / phrase.getTotalDuration();
				break;
			case DOWN:
				piano = new Piano(3, new Rect(getX1(), getY2() - 50, getWidth(), 50, PApplet.CORNER), true);
				rectHeightOfWholeNote = getHeight() / phrase.getTotalDuration();
				break;
			case LEFT:
				piano = new Piano(3, new Rect(getX1(), getY1(), 50, getHeight(), PApplet.CORNER), true);
				rectHeightOfWholeNote = getWidth() / phrase.getTotalDuration();
				break;
			case UP:
				piano = new Piano(3, new Rect(getX1(), getY1(), getWidth(), 50, PApplet.CORNER), true);
				rectHeightOfWholeNote = getHeight() / phrase.getTotalDuration();
				break;
		}
		
		//noteMovement
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
		float dpos = dBeatpt * rectHeightOfWholeNote;
		
		for (int i=0; i<set.length; i++) {
			set[i].display(pa);
			
			switch(noteMovement) {
				case RIGHT:
					set[i].translate(dpos, 0);
					break;
				case DOWN:
					set[i].translate(0, dpos);
					break;
				case LEFT:
					set[i].translate(-dpos, 0);
					break;
				case UP:
					set[i].translate(0, -dpos);
					break;
			}
		}
		
		return wrap(set, pointer);
	}
	
	private int wrap(Rect[] set, int pointer) {
		//handle wrapping
		pa.rectMode(pa.CORNER);
		
		switch(noteMovement) {
			case RIGHT:
				pa.rect(set[pointer].getX1() - this.getWidth(), set[pointer].getY1(),
						set[pointer].getWidth(), set[pointer].getHeight());
				
				if (set[pointer].getX1() > this.getX2()) {
					set[pointer].translate(-this.getWidth(), 0);
					pointer = (pointer+1) % set.length;
				}
				break;
			case DOWN:
				pa.rect(set[pointer].getX1(), set[pointer].getY1() - this.getHeight(),
						set[pointer].getWidth(), set[pointer].getHeight());
				
				if (set[pointer].getY1() > this.getY2()) {
					set[pointer].translate(0, -this.getHeight());
					pointer = (pointer+1) % set.length;
				}
				break;
			case LEFT:
				pa.rect(set[pointer].getX1() + this.getWidth(), set[pointer].getY1(),
						set[pointer].getWidth(), set[pointer].getHeight());
				
				if (set[pointer].getX2() < this.getX1()) {
					set[pointer].translate(this.getWidth(), 0);
					pointer = (pointer+1) % set.length;
				}
				break;
			case UP:
				pa.rect(set[pointer].getX1(), set[pointer].getY1() + this.getHeight(),
						set[pointer].getWidth(), set[pointer].getHeight());
				
				if (set[pointer].getY2() < this.getY1()) {
					set[pointer].translate(0, this.getHeight());
					pointer = (pointer+1) % set.length;
				}
				break;
		}
		
		return pointer;
	}
	
	private Rect[] initSet(Phrase phrase) {
		Rect[] set = new Rect[phrase.getNumNotes()];
		float dx = 0;
		float dy = 0;
		set[0] = noteToRect(phrase, 0);
		for (int i=1; i<phrase.getNumNotes(); i++) {
			set[i] = noteToRect(phrase, i);
			//setup translation
			switch(noteMovement) {
				case RIGHT:
					dx -= set[i-1].getWidth();
					break;
				case DOWN:	
					dy -= set[i-1].getHeight();
					break;
				case LEFT:
					dx += set[i-1].getWidth();
					break;
				case UP:
					dy += set[i-1].getHeight();
					break;
			}
			
			//do translation
			set[i].translate(dx, dy);
		}
		
		return set;
	}
	
	private Rect noteToRect(Phrase phrase, int noteIndex) {
		Rect rect = piano.getKeyCopy(phrase.getPitch(noteIndex) - firstPitchOfPiano);
		
		switch(noteMovement) {
			case RIGHT:
				rect.setWidth(rectHeightOfWholeNote * phrase.getDuration(noteIndex));
				rect.setX1(piano.getX2() - rect.getWidth());
				break;
			case DOWN:
				rect.setHeight(rectHeightOfWholeNote * phrase.getDuration(noteIndex));
				rect.setY1(piano.getY2() - rect.getHeight());
				break;
			case LEFT:
				rect.setWidth(rectHeightOfWholeNote * phrase.getDuration(noteIndex));
				rect.setX1(piano.getX1());
				break;
			case UP:
				rect.setHeight(rectHeightOfWholeNote * phrase.getDuration(noteIndex));
				rect.setY1(piano.getY1());
				break;
		}
		

		return rect;
	}
}