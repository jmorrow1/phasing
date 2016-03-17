package instrument_graphics;

import geom.Shape;
import phasing.Phrase;
import phasing.PhraseReader;
import processing.core.PApplet;

/**
 * An InstrumentPlayer graphically animates an Instrument.
 * It works by receiving note events from a PhraseReader.
 * To work properly, a PhraseReader needs to be set up to
 * send callbacks to the InstrumentPlayer.setActiveKey() method.
 * 
 * @author James Morrow
 *
 */
public class InstrumentPlayer {
	private Phrase phrase;
	private Shape[] keyCopies;
	private Shape activeKey;
	
	/**
	 * 
	 * @param instrument The instrument to animate.
	 * @param phrase The phrase to animate it with.
	 */
	public InstrumentPlayer(Instrument instrument, Phrase phrase) {
		keyCopies = new Shape[phrase.getNumNotes()];
		this.phrase = phrase;
		setInstrument(instrument);
		
		activeKey = null;
	}
	
	/**
	 * Animates its instrument.
	 * @param pa The PApplet to which the instrument is drawn.
	 */
	public void draw(PApplet pa) {
		if (activeKey != null) {
			activeKey.display(pa);
		}
	}
	
	//callback:
	public void setActiveKey(PhraseReader reader) {
		int i = reader.getNoteIndex();
		activeKey = (phrase.getSCDynamic(i) > 0) ? keyCopies[i] : null;
	}
	
	/**
	 * Returns the index of the first occurence of x in xs.
	 * @param x
	 * @param xs
	 * @return The index of the first occurence of x in xs.
	 */
	private static int indexOf(Shape x, Shape[] xs) {
		for (int i=0; i<xs.length; i++) {
			if (xs[i] == x) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Sets the instrument the InstrumentPlayer plays.
	 * @param instrument The instrument for the InstrumentPlayer to play.
	 */
	public void setInstrument(Instrument instrument) {
		for (int i=0; i<keyCopies.length; i++) {
			int minPhraseOctaveNum = (int)(phrase.minPitch() / 12);
			int phraseSpan = (int)(phrase.maxPitch() - phrase.minPitch());
			int instrumentSpan = instrument.getNumOctaves() * 12;
			int spanDifferenceInNotes = instrumentSpan - phraseSpan;
			int spanDifferenceInOctaves = spanDifferenceInNotes / 12;
			int instrumentOffset = 12 * (spanDifferenceInOctaves/2);
			if (instrumentOffset < 0) {
				instrumentOffset = 0;
			}
			int pitch = phrase.getSCPitch(i) - 12*minPhraseOctaveNum + instrumentOffset;
			keyCopies[i] = instrument.pitchToShape(pitch);
		}
	}
}