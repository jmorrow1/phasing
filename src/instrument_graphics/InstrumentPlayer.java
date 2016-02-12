package instrument_graphics;

import geom.Shape;
import phases.Phrase;
import phases.PhraseReader;
import processing.core.PApplet;

public class InstrumentPlayer {
	private Phrase phrase;
	private Shape[] keyCopies;
	private Shape activeKey;
	private int firstPitch;
	
	public InstrumentPlayer(Instrument instrument, InstrumentPlayer instrumentPlayer) {
		this(instrument, instrumentPlayer.phrase, instrumentPlayer.firstPitch);
		int activeKeyIndex = indexOf(instrumentPlayer.activeKey, instrumentPlayer.keyCopies);
		activeKey = keyCopies[activeKeyIndex];
	}
	
	public InstrumentPlayer(Instrument instrument, Phrase phrase, int firstPitch) {
		keyCopies = new Shape[phrase.getNumNotes()];
		
		this.firstPitch = firstPitch;
		
		for (int i=0; i<keyCopies.length; i++) {
			keyCopies[i] = instrument.getShapeAtNoteIndex(phrase.getSCPitch(i) - firstPitch);
		}
		
		this.phrase = phrase;
		
		activeKey = null;
	}
	
	public void display(PApplet pa) {
		if (activeKey != null) {
			activeKey.display(pa);
		}
	}
	
	//callback:
	public void setActiveKey(PhraseReader reader) {
		int i = reader.getNoteIndex();
		activeKey = (phrase.getSCDynamic(i) > 0) ? keyCopies[reader.getNoteIndex()] : null;
	}
	
	private static int indexOf(Shape x, Shape[] xs) {
		for (int i=0; i<xs.length; i++) {
			if (xs[i] == x) {
				return i;
			}
		}
		return -1;
	}
}