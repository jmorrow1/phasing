package views;

import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;
import processing.core.PApplet;

public class GHView extends View {
	//piano
	private boolean displayPiano;
	private Piano piano;
	private final float PIANO_SIZE = 50;
	//conversion
	private int firstPitchOfPiano = 60;
	private float pixelsPerWholeNote;
	
	//note views
	private Rect[] list1, list2;
	private int pointer1=0, pointer2=0; //points to rectangle closest to edge
	public final static int RIGHT=2, DOWN=1, LEFT=-2, UP=-1;
	private int noteMovement;
	private boolean movementRelativeToList1;
	private boolean list1IsReversed = false;
	
	public GHView(Rect rect, Phrase phrase, int noteMovement, boolean displayPiano, boolean movementRelativeToList1,
			int color1, int color2, int opacity, PApplet pa) {
		super(rect, color1, color2, opacity, pa);
		
		this.noteMovement = noteMovement;
		this.displayPiano = displayPiano;
		this.movementRelativeToList1 = movementRelativeToList1;
		
		switch (noteMovement) {
			case RIGHT:
				piano = new Piano(3, new Rect(getX2() - PIANO_SIZE, getY1(), PIANO_SIZE, getHeight(), PApplet.CORNER), false);
				pixelsPerWholeNote = getWidth() / phrase.getTotalDuration();
				break;
			case DOWN:
				piano = new Piano(3, new Rect(getX1(), getY2() - PIANO_SIZE, getWidth(), PIANO_SIZE, PApplet.CORNER), true);
				pixelsPerWholeNote = getHeight() / phrase.getTotalDuration();
				break;
			case LEFT:
				piano = new Piano(3, new Rect(getX1(), getY1(), PIANO_SIZE, getHeight(), PApplet.CORNER), true);
				pixelsPerWholeNote = getWidth() / phrase.getTotalDuration();
				break;
			case UP:
				piano = new Piano(3, new Rect(getX1(), getY1(), getWidth(), PIANO_SIZE, PApplet.CORNER), true);
				pixelsPerWholeNote = getHeight() / phrase.getTotalDuration();
				break;
		}
		
		list1 = initList(phrase);
		list2 = initList(phrase);
	}
	
	public void update(float dBeatpt1, float dBeatpt2) {
		if (displayPiano) piano.display(pa);
		
		pa.strokeWeight(1);
		
		//list1
		pa.fill(color1, opacity);
		if (movementRelativeToList1) {
			for (int i=0; i<list1.length; i++) {
				list1[i].display(pa);
			}
		}
		else {
			pointer1 = update(list1, dBeatpt1, pointer1);
		}
		
		//list2
		pa.fill(color2, opacity);
		if (movementRelativeToList1) {		
			if ((!list1IsReversed && dBeatpt2 - dBeatpt1 < 0) || (list1IsReversed && dBeatpt2 - dBeatpt1 > 0)) {
				reverse(list2);
				list1IsReversed = !list1IsReversed;
			}
			pointer2 = update(list2, dBeatpt2 - dBeatpt1, pointer2);
		}
		else {
			pointer2 = update(list2, dBeatpt2, pointer2);
		}
	}
	
	private int update(Rect[] list, float dBeatpt, int pointer) {
		float dpos = dBeatpt * pixelsPerWholeNote;

		for (int i=0; i<list.length; i++) {
			list[i].display(pa);
			
			switch(noteMovement) {
				case RIGHT:
					list[i].translate(dpos, 0);
					break;
				case DOWN:
					list[i].translate(0, dpos);
					break;
				case LEFT:
					list[i].translate(-dpos, 0);
					break;
				case UP:
					list[i].translate(0, -dpos);
					break;
			}
		}
	
		pointer = wrap(list, pointer, dBeatpt < 0);
		return pointer;
	}

	private int wrap(Rect[] list, int pointer, boolean movingInReverse) {
		//handle wrapping
		pa.rectMode(pa.CORNER);
		
		int directionOfMovement = noteMovement;
		if (movingInReverse) directionOfMovement *= -1;
		
		boolean movePointer = false;
		
		switch(directionOfMovement) {
			case RIGHT:
				pa.rect(list[pointer].getX1() - this.getWidth(), list[pointer].getY1(),
						list[pointer].getWidth(), list[pointer].getHeight());
				
				if (list[pointer].getX1() > this.getX2()) {
					list[pointer].translate(-this.getWidth(), 0);
					pointer = PhasesPApplet.remainder(pointer+1, list.length);
				}
				break;
			case DOWN:
				pa.rect(list[pointer].getX1(), list[pointer].getY1() - this.getHeight(),
						list[pointer].getWidth(), list[pointer].getHeight());
				
				if (list[pointer].getY1() > this.getY2()) {
					list[pointer].translate(0, -this.getHeight());
					pointer = PhasesPApplet.remainder(pointer+1, list.length);
				}
				break;
			case LEFT:
				pa.rect(list[pointer].getX1() + this.getWidth(), list[pointer].getY1(),
						list[pointer].getWidth(), list[pointer].getHeight());
				
				if (list[pointer].getX2() < this.getX1()) {
					list[pointer].translate(this.getWidth(), 0);
					pointer = PhasesPApplet.remainder(pointer+1, list.length);
				}
				break;
			case UP:
				pa.rect(list[pointer].getX1(), list[pointer].getY1() + this.getHeight(),
						list[pointer].getWidth(), list[pointer].getHeight());
				
				if (list[pointer].getY2() < this.getY1()) {
					list[pointer].translate(0, this.getHeight());
					pointer = PhasesPApplet.remainder(pointer+1, list.length);
				}
				break;
		}
		
		return pointer;
	}
	
	private Rect[] initList(Phrase phrase) {
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
				rect.setWidth(pixelsPerWholeNote * phrase.getDuration(noteIndex));
				rect.setX1(piano.getX2() - rect.getWidth());
				break;
			case DOWN:
				rect.setHeight(pixelsPerWholeNote * phrase.getDuration(noteIndex));
				rect.setY1(piano.getY2() - rect.getHeight());
				break;
			case LEFT:
				rect.setWidth(pixelsPerWholeNote * phrase.getDuration(noteIndex));
				rect.setX1(piano.getX1());
				break;
			case UP:
				rect.setHeight(pixelsPerWholeNote * phrase.getDuration(noteIndex));
				rect.setY1(piano.getY1());
				break;
		}
		

		return rect;
	}
	
	private static void reverse(Rect[] xs) {
	    int i=0;
	    int j=xs.length-1;
	    while (i < j) {
	    	Rect temp = xs[i];
	    	xs[i] = xs[j];
	    	xs[j] = temp;
	    	i++;
	    	j--;
	    }
	}
}