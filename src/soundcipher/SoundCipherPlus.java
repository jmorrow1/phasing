package soundcipher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import arb.soundcipher.SoundCipher;
import phasing.Phrase;
import phasing.PhraseReader;
import phasing.PhraseReader.PhraseReaderListener;
import processing.core.PApplet;

/**
 * Extends the functionality of a SoundCipher (a player of musical data) with
 * functionality that allow it to read and play a Phrase object.
 * 
 * Also has a callback that it triggers playNote() is called (every time it plays a note),
 * passing itself as an argument.
 * 
 * @author James Morrow
 *
 */
public class SoundCipherPlus extends SoundCipher implements PhraseReaderListener {
	private PhraseReader phraseReader;
	private Phrase phrase;
	private SoundCipherPlusListener listener;
	
	/**
	 * 
	 * @param pa The PApplet that its parent binds to a variable
	 * @param phrase The phrase to read and play
	 * @param callee The object on which to invoke the callback
	 * @param callback The method to call when playNote is called
	 */
	public SoundCipherPlus(PApplet pa, Phrase phrase, SoundCipherPlusListener listener) {
		super(pa);
		this.phrase = phrase;
		phraseReader = new PhraseReader(phrase, 0, this);
		this.listener = listener;
	}
	
	/**
	 * Plays the phrase at the rate determined by the dNotept variable.
	 * @param currentPhrase
	 * @param dNotept
	 */
	public void update(float dNotept) {
		phraseReader.update(dNotept);
	}
	
	@Override
	public void noteEvent(PhraseReader phraseReader) {
		int i = phraseReader.getNoteIndex();
		if (!phrase.isRest(i)) {
			super.playNote(phrase.getSCPitch(i), phrase.getSCDynamic(i), 0.9f*phrase.getSCDuration(i));
		}
		listener.noteEvent(this);
	}
	
	/**
	 * 
	 * @return The index of the note currently being played
	 */
	public int getNoteIndex() {
		return phraseReader.getNoteIndex();
	}
	
	/**
	 * 
	 * @author James Morrow
	 *
	 */
	public interface SoundCipherPlusListener {
		/**
		 * Responds to a note event.
		 * 
		 * @param scPlus The SoundCipherPlus sending the note events.
		 */
		public void noteEvent(SoundCipherPlus scPlus);
	}

}
