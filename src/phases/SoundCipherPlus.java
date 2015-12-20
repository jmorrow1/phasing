package phases;

import java.lang.reflect.Method;

import arb.soundcipher.SoundCipher;
import processing.core.PApplet;

public class SoundCipherPlus extends SoundCipher {
	private PhraseReader phraseReader;
	private Phrase phrase;
	
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
	
	public void update(Phrase phrase, float dNotept) {
		phraseReader.update(dNotept);
	}
	
	public void playNote(PhraseReader phraseReader) {
		int i = phraseReader.getNoteIndex();
		super.playNote(phrase.getSCPitch(i), phrase.getSCDynamic(i), phrase.getSCDuration(i));
	}
}
