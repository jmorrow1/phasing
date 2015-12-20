package phases;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PhraseReader {
	//data
	private int id;
	private Phrase phrase;
	//bookkeeping
	private int noteIndex;
	private float noteTimeTillNextNote;
	//callback
	private Object callee;
	private Method callback;
	
	public PhraseReader(Phrase phrase, int id, Object callee, Method callback) {
		this.phrase = phrase;
		this.id = id;
		
		noteIndex = 0;
		noteTimeTillNextNote = phrase.getSCDuration(noteIndex);
		
		this.callee = callee;
		this.callback = callback;
		
		try {
			callback.invoke(callee, this);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void update(float dNotept) {
		noteTimeTillNextNote -= dNotept;
		
		if (noteTimeTillNextNote <= 0) {
			noteIndex = (noteIndex+1) % phrase.getNumNotes();
			noteTimeTillNextNote = noteTimeTillNextNote + phrase.getSCDuration(noteIndex);
			try {
				callback.invoke(callee, this);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getNoteIndex() {
		return noteIndex;
	}
}
