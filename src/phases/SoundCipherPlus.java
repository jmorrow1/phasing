package phases;

import java.lang.reflect.Method;

import arb.soundcipher.SoundCipher;
import processing.core.PApplet;

/**
 * Extends the functionality of a SoundCipher (a player of musical data) with
 * functionality that allow it to read and play a Phrase object.
 * 
 * @author James Morrow
 *
 */
public class SoundCipherPlus extends SoundCipher {
	private PhraseReader phraseReader;
	private Phrase phrase;
	
	/**
	 * 
	 * @param pa The PApplet that its parent binds to a variable
	 * @param phrase The phrase to read and play
	 */
	public SoundCipherPlus(PApplet pa, Phrase phrase) {
		super(pa);
		this.phrase = phrase;
		try {
			Method callback = SoundCipherPlus.class.getMethod("playNote", PhraseReader.class);
			phraseReader = new PhraseReader(phrase, 0, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Plays the phrase at the rate determined by the dNotept variable.
	 * @param phrase
	 * @param dNotept
	 */
	public void update(float dNotept) {
		phraseReader.update(dNotept);
	}
	
	/**
	 * Callback for the PhraseReader.
	 * Plays the note that the PhraseReader is currently reading.
	 */
	public void playNote(PhraseReader phraseReader) {
		int i = phraseReader.getNoteIndex();
		super.playNote(phrase.getSCPitch(i), phrase.getSCDynamic(i), phrase.getSCDuration(i));
	}
}
