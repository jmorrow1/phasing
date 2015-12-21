package phases;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import arb.soundcipher.SoundCipher;
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
public class SoundCipherPlus extends SoundCipher {
	private PhraseReader phraseReader;
	private Phrase phrase;
	
	private Object callee;
	private Method callback;
	
	/**
	 * 
	 * @param pa The PApplet that its parent binds to a variable
	 * @param phrase The phrase to read and play
	 * @param callee The object on which to invoke the callback
	 * @param callback The method to call when playNote is called
	 */
	public SoundCipherPlus(PApplet pa, Phrase phrase, Object callee, Method callback) {
		super(pa);
		this.phrase = phrase;
		try {
			Method scCall = SoundCipherPlus.class.getMethod("playNote", PhraseReader.class);
			phraseReader = new PhraseReader(phrase, 0, this, scCall);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		this.callee = callee;
		this.callback = callback;
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
		try {
			callback.invoke(callee, this);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return The index of the note currently being played
	 */
	public int getNoteIndex() {
		return phraseReader.getNoteIndex();
	}
}
