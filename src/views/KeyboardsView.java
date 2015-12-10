package views;

import geom.Rect;
import phases.Phrase;
import processing.core.PApplet;

public class KeyboardsView extends View {
	//music
	private PhraseReader a, b;
	//piano
	private Piano keyboardA, keyboardB;
	private Piano keyboardAB;
	private final int PIANO_SIZE = 50;
	//conversion
	private int firstPitchOfPiano = 60;
	
	public KeyboardsView(Rect rect, Phrase phrase, int color1, int color2, int opacity,
			boolean superimposeKeyboards, PApplet pa) {
		super(rect, color1, color2, opacity, pa);

		if (superimposeKeyboards) {
			keyboardAB = new Piano(2, new Rect(this.getCenx(), this.getCeny(),
					0.75f*this.getWidth(), PIANO_SIZE, PApplet.CENTER), true, pa.color(255));
		}
		else {
			keyboardA = new Piano(2, new Rect(this.getCenx(), this.getCeny() - PIANO_SIZE,
							0.75f*this.getWidth(), PIANO_SIZE, PApplet.CENTER), true, pa.color(255));
			
			keyboardB = new Piano(2, new Rect(this.getCenx(), this.getCeny() + PIANO_SIZE,
							0.75f*this.getWidth(), PIANO_SIZE, PApplet.CENTER), true, pa.color(255));
		}
		
		Rect[] noteIndexToKeyA = new Rect[phrase.getNumNotes()];
		Rect[] noteIndexToKeyB = null;
		
		if (superimposeKeyboards) {
			for (int i=0; i<noteIndexToKeyA.length; i++) {
				noteIndexToKeyA[i] = keyboardAB.getKeyCopy(phrase.getPitch(i) - firstPitchOfPiano);
			}
			noteIndexToKeyB = noteIndexToKeyA;
		}
		else {
			noteIndexToKeyB = new Rect[phrase.getNumNotes()];
			for (int i=0; i<noteIndexToKeyA.length; i++) {
				noteIndexToKeyA[i] = keyboardA.getKeyCopy(phrase.getPitch(i) - firstPitchOfPiano);
				noteIndexToKeyB[i] = keyboardB.getKeyCopy(phrase.getPitch(i) - firstPitchOfPiano);
			}
		}
		
		a = new PhraseReader(phrase, noteIndexToKeyA, color1, opacity);
		b = new PhraseReader(phrase, noteIndexToKeyB, color2, opacity);
	}

	@Override
	public void update(float dNotept1, float dNotept2) {	
		boolean aIsOnWhiteKey = a.update(dNotept1);
		boolean bIsOnWhiteKey = b.update(dNotept2);
		
		if (keyboardAB != null) {
			keyboardAB.drawWhiteKeys(pa);
			if (aIsOnWhiteKey) a.draw();
			if (bIsOnWhiteKey) b.draw();
			keyboardAB.drawBlackKeys(pa);
			if (!aIsOnWhiteKey) a.draw();
			if (!bIsOnWhiteKey) b.draw();
		}
		if (keyboardA != null) {
			keyboardA.drawWhiteKeys(pa);
			if (aIsOnWhiteKey) a.draw();
			keyboardA.drawBlackKeys(pa);
			if (!aIsOnWhiteKey) a.draw();
		}
		if (keyboardB != null) {
			keyboardB.drawWhiteKeys(pa);
			if (bIsOnWhiteKey) b.draw();
			keyboardB.drawBlackKeys(pa);
			if (!bIsOnWhiteKey) b.draw();
		}
		
		
	}
	
	private class PhraseReader {
		int noteIndex;
		float noteTimeTillNextNote;
		Phrase phrase;
		Rect[] noteIndexToKey;
		int color;
		int opacity;
		boolean currNoteIsWhiteKey;
		
		PhraseReader(Phrase phrase, Rect[] noteIndexToKey, int color, int opacity) {
			noteIndex = 0;
			this.phrase = phrase;
			this.noteIndexToKey = noteIndexToKey;
			this.noteTimeTillNextNote = phrase.getDuration(noteIndex);
			this.color = color;
			this.opacity = opacity;
			currNoteIsWhiteKey = Piano.isWhiteKey(phrase.getPitch(noteIndex));
		}
		
		
		
		boolean update(float dNotept) {
			noteTimeTillNextNote -= dNotept;
			
			if (noteTimeTillNextNote <= 0) {
				noteIndex = (noteIndex+1) % phrase.getNumNotes();
				noteTimeTillNextNote = noteTimeTillNextNote + phrase.getDuration(noteIndex);
				currNoteIsWhiteKey = Piano.isWhiteKey(phrase.getPitch(noteIndex));
			}
			
			return currNoteIsWhiteKey;
		}
		
		void draw() {
			pa.noStroke();
			pa.fill(color, opacity);
			noteIndexToKey[noteIndex].display(pa);
		}
	}
}