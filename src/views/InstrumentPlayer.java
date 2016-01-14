package views;

import geom.Shape;
import instrument_graphics.Instrument;
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
			keyCopies[i] = instrument.getKeyCopy(phrase.getSCPitch(i) - firstPitch);
		}
		
		this.phrase = phrase;
		
		activeKey = keyCopies[0];
	}
	
	public void display(PApplet pa) {
		activeKey.display(pa);
	}
	
	//callback:
	public void setActiveKey(PhraseReader reader) {
		activeKey = keyCopies[reader.getNoteIndex()];
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