package phases;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PhraseReader {
	//data
	private Phrase phrase;
	private int color;
	private float phraseDuration;
	//bookkeeping
	private float[] notepts;
	private int noteIndex, nextNoteIndex;
	private boolean wait;
	private float lastNotePt;
	//callback
	private Object callee;
	private Method callback;
	
	public PhraseReader(Phrase phrase, int color, Object callee, Method callback) {
		this.phrase = phrase;
		this.color = color;
		phraseDuration = phrase.getTotalDuration();
		
		notepts = new float[phrase.getNumNotes()];
		for (int i=1; i<notepts.length; i++) {
			notepts[i] = notepts[i-1] + phrase.getDuration(i-1);
		}
		noteIndex = 0;
		nextNoteIndex = (noteIndex+1) % phrase.getNumNotes();
		wait = false;
		
		this.callee = callee;
		this.callback = callback;
	}
	
	public void update2(float normalpt) {
		update3(normalpt * phraseDuration);
	}
	
	public void update3(float notept) {
		if (wait) {
			if (notept < lastNotePt) wait = false;
		}
		
		if (notept > notepts[nextNoteIndex] && !wait) {
			noteIndex = nextNoteIndex;
			nextNoteIndex = (nextNoteIndex+1) % phrase.getNumNotes();
			
			if (nextNoteIndex == 0) {
				lastNotePt = notept;
				wait = true;
			}
			
			try {
				callback.invoke(callee, noteIndex, this);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getColor() {
		return color;
	}
}