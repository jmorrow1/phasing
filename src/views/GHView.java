package views;

import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;
import phases.Piano;
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
	public final static int RIGHT=0, DOWN=1;
	private int noteMovement;
	private boolean cameraRelativeToMotion;
	//other
	private float dNoteptAcc;
	
	public GHView(Rect rect, Phrase phrase, int color1, int color2, int opacity, PApplet pa) {
		super(rect, color1, color2, opacity, 0, pa);
		
		switch (noteMovement) {
			case RIGHT:
				piano = new Piano(3, new Rect(getX2() - PIANO_SIZE, getY1(), PIANO_SIZE, getHeight(), PApplet.CORNER), false);
				pixelsPerWholeNote = getWidth() / phrase.getTotalDuration();
				break;
			case DOWN:
				piano = new Piano(3, new Rect(getX1(), getY2() - PIANO_SIZE, getWidth(), PIANO_SIZE, PApplet.CORNER), true);
				pixelsPerWholeNote = getHeight() / phrase.getTotalDuration();
				break;
		}
		
		list1 = initList(phrase);
		list2 = initList(phrase);
	}
	
	public void update(float dNotept1, float dNotept2, int sign) {
		if (displayPiano) piano.display(pa);
		
		pa.strokeWeight(1);
		
		//list1
		pa.fill(color1, opacity);
		if (cameraRelativeToMotion) {
			for (int i=0; i<list1.length; i++) {
				list1[i].display(pa);
			}
		}
		else {
			update(list1, dNotept1);
		}
		
		//list2
		pa.fill(color2, opacity);
		if (cameraRelativeToMotion) {
			
			dNoteptAcc += (dNotept2 - dNotept1);
			
			if ( (sign < 0 && dNoteptAcc < 0) || (sign > 0 && dNoteptAcc > 0) ) {
				update(list2, dNoteptAcc);
				dNoteptAcc = 0;
			}
			else {
				update(list2, 0);
			}
		}
		else {
			update(list2, dNotept2);
		}
	}
	
	private void update(Rect[] list, float dBeatpt) {
		float dpos = dBeatpt * pixelsPerWholeNote;
			
		//translate
		for (int i=0; i<list.length; i++) {	
			switch(noteMovement) {
				case RIGHT:
					list[i].translate(dpos, 0);
					break;
				case DOWN:
					list[i].translate(0, dpos);
					break;
			}
		}
		
		//wrap
		wrap(list);
		
		//display
		for (int i=0; i<list.length; i++) {
			if (i == 0 || i == list.length-1) {
				if (noteMovement == RIGHT) {
					list[i].displayHorizontallyWrapped(pa, this.getX1(), this.getX2());
				}
				else {
					list[i].displayVerticallyWrapped(pa, this.getY1(), this.getY2());
				}
			}
			else {
				list[i].display(pa);
			}
		}
	}

	private void wrap(Rect[] list) {
		pa.rectMode(pa.CORNER);
		if (noteMovement == RIGHT) {
			if (list[0].getX2() < this.getX1()) {
				list[0].translate(this.getWidth(), 0);
				leftShift(list);
				
			}
			else if (list[list.length-1].getX1() > this.getX2()) {
				list[list.length-1].translate(-this.getWidth(), 0);
				rightShift(list);
			}
		}
		else {
			if (list[0].getY2() < this.getY1()) {
				list[0].translate(0, this.getHeight());
				leftShift(list);
				
			}
			else if (list[list.length-1].getY1() > this.getY2()) {
				list[list.length-1].translate(0, -this.getHeight());
				rightShift(list);
			}
		}
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
					dx += set[i-1].getWidth();
					break;
				case DOWN:	
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
				rect.setX1(getX1());
				break;
			case DOWN:
				rect.setHeight(pixelsPerWholeNote * phrase.getDuration(noteIndex));
				rect.setY1(getY1());
				break;
		}
		

		return rect;
	}
	
	/*Settings*/
	
	private void displayPiano(boolean displayPiano) {
		this.displayPiano = displayPiano;
	}
	
	public int numPresets() {
		return 2;
	}
	
	public void loadPreset(int preset) {
		noteMovement = DOWN;
		cameraRelativeToMotion = true;
		
		switch(preset) {
			case 0 :
				displayPiano(false);
				break;
			case 1 :
				displayPiano(true);
				break;
		}
	}
}