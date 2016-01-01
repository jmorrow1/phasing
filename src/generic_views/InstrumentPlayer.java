package generic_views;

import geom.Shape;
import phases.Instrument;
import phases.Phrase;
import phases.PhraseReader;
import processing.core.PApplet;

public class InstrumentPlayer {
	private Phrase phrase;
	private Shape[] keyCopies;
	private Shape activeKey;
	
	public InstrumentPlayer(Instrument instrument, Phrase phrase, int firstPitch) {
		keyCopies = new Shape[phrase.getNumNotes()];
		
		for (int i=0; i<keyCopies.length; i++) {
			keyCopies[i] = instrument.getKeyCopy(phrase.getSCPitch(i) - firstPitch);
		}
		
		this.phrase = phrase;
	}
	
	public void display(PApplet pa) {
		activeKey.display(pa);
	}
	
	//callback:
	public void setActiveKey(PhraseReader reader) {
		activeKey = keyCopies[reader.getNoteIndex()];
	}
}
