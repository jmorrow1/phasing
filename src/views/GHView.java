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
	private int firstPitchOfPiano;
	private float pixelsPerWholeNote;
	//note views
	private Rect[] list1, list2;
	public final static int RIGHT=0, DOWN=1;
	private int noteMovement;
	private boolean cameraRelativeToMotion;
	//other
	private float dNoteptAcc;
	
	protected void init() {
		firstPitchOfPiano = 60;
		noteMovement = RIGHT;
		initPiano();
		list1 = initList();
		list2 = initList();
	}
	
	public GHView(Rect rect, Phrase phrase, int color1, int color2, int opacity, PApplet pa) {
		super(rect, phrase, color1, color2, opacity, 0, pa);
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
	
	private void initPiano() {
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
	}
	
	private Rect[] initList() {
		Rect[] set = new Rect[phrase.getNumNotes()];
		float dx = 0;
		float dy = 0;
		set[0] = noteToRect(0);
		for (int i=1; i<phrase.getNumNotes(); i++) {
			set[i] = noteToRect(i);
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
	
	private Rect noteToRect(int noteIndex) {
		Rect rect = piano.getKeyCopy(phrase.getSCPitch(noteIndex) - firstPitchOfPiano);
		
		switch(noteMovement) {
			case RIGHT:
				rect.setWidth(pixelsPerWholeNote * phrase.getSCDuration(noteIndex));
				rect.setX1(getX1());
				break;
			case DOWN:
				rect.setHeight(pixelsPerWholeNote * phrase.getSCDuration(noteIndex));
				rect.setY1(getY1());
				break;
		}
		

		return rect;
	}
	
	/*Settings*/
	
	private void displayPiano(boolean displayPiano) {
		this.displayPiano = displayPiano;
	}
	
	private void setCameraRelativeToMotion(boolean cameraRelativeToMotion) {
		this.cameraRelativeToMotion = cameraRelativeToMotion;
	}
	
	private void setNoteMovement(int noteMovement) {
		if ( (this.noteMovement == RIGHT && noteMovement == DOWN) ||
				(this.noteMovement == DOWN && noteMovement == RIGHT)) {
			rotateRects(list1);
			rotateRects(list2);
		}
		this.noteMovement = noteMovement;
		initPiano();
	}
	
	private void rotateRects(Rect[] list) {
		for (int i=0; i<list.length; i++) {
			float x1 = PApplet.map(list[i].getY1(), this.getY1(), this.getY2(), this.getX1(), this.getX2());
			float y1 = PApplet.map(list[i].getX1(), this.getX1(), this.getX2(), this.getY1(), this.getY2());
			float x2 = PApplet.map(list[i].getY2(), this.getY1(), this.getY2(), this.getX1(), this.getX2());
			float y2 = PApplet.map(list[i].getX2(), this.getX1(), this.getX2(), this.getY1(), this.getY2());
			list[i] = new Rect(x1, y1, x2, y2, PApplet.CORNERS);
		}
	}
	
	public int numPresets() {
		return 6;
	}
	
	public void loadPreset(int preset) {
		cameraRelativeToMotion = true;
		
		switch(preset) {
			case 0 :
				setNoteMovement(RIGHT);
				setCameraRelativeToMotion(true);
				displayPiano(false);
				break;
			case 1 :
				setNoteMovement(RIGHT);
				setCameraRelativeToMotion(false);
				displayPiano(false);
				break;
			case 2 :
				setNoteMovement(RIGHT);
				setCameraRelativeToMotion(false);
				displayPiano(true);
				break;
			case 3 :
				setNoteMovement(DOWN);
				setCameraRelativeToMotion(true);
				displayPiano(false);
				break;
			case 4 :
				setNoteMovement(DOWN);
				setCameraRelativeToMotion(false);
				displayPiano(false);
				break;
			case 5 :
				setNoteMovement(DOWN);
				setCameraRelativeToMotion(false);
				displayPiano(true);
				break;
		}
	}
}