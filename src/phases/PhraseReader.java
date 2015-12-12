package phases;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PhraseReader {
	//data
	private Phrase phrase;
	private int color;
	//bookkeeping
	private int noteIndex;
	private float noteTimeTillNextNote;
	//callback
	private Object callee;
	private Method callback;
	
	public PhraseReader(Phrase phrase, int color, Object callee, Method callback) {
		this.phrase = phrase;
		this.color = color;
		
		noteIndex = 0;
		noteTimeTillNextNote = phrase.getDuration(noteIndex);
		
		this.callee = callee;
		this.callback = callback;
		
		try {
			callback.invoke(callee, this);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void update(float dNotept) {
		noteTimeTillNextNote -= dNotept;
		
		if (noteTimeTillNextNote <= 0) {
			noteIndex = (noteIndex+1) % phrase.getNumNotes();
			noteTimeTillNextNote = noteTimeTillNextNote + phrase.getDuration(noteIndex);
			try {
				callback.invoke(callee, this);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public int getColor() {
		return color;
	}
	
	public int getNoteIndex() {
		return noteIndex;
	}
}
