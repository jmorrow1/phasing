package phases;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reads a phrase at the rate that its update method is invoked.
 * Invokes the given callback whenever it reads a new note, 
 * passing itself as an argument to the callback.
 * 
 * Should be able to cope and continue to read the phrase even while the phrase changes state.
 * 
 * @author James Morrow
 *
 */
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
	
	/**
	 * 
	 * @param phrase The phrase to read
	 * @param id An integer identifying the phrase reader
	 * @param callee The object on which to invoke the callback method
	 * @param callback The callback method
	 */
	public PhraseReader(Phrase phrase, int id, Object callee, Method callback) {
		this.phrase = phrase;
		this.id = id;
		
		this.callee = callee;
		this.callback = callback;
		
		noteIndex = -1;
		noteTimeTillNextNote = 0;
	}
	
	/**
	 * Reads some more of the phrase. How much is read is determined by the dNotept argument.
	 * Calls the callback if it reads a new note.
	 * 
	 * @param dNotept The amount to read, in terms of musical time.
	 */
	public void update(float dNotept) {
		noteTimeTillNextNote -= dNotept;
		
		if (noteTimeTillNextNote <= 0 && phrase.getNumNotes() > 0) {
			noteIndex = (noteIndex+1) % phrase.getNumNotes();
			noteTimeTillNextNote = noteTimeTillNextNote + phrase.getSCDuration(noteIndex);
			try {
				callback.invoke(callee, this);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * After a period of neglect (in which update has not been invoked) this method informs the PhraseReader of where to start reading again.
	 * @param notept The place at which to start reading again.
	 */
	public void wakeUp(float notept) {
		noteIndex = -1;
		noteTimeTillNextNote = -notept;
		
		while (noteTimeTillNextNote <= 0) {
			noteIndex = (noteIndex+1) % phrase.getNumNotes();
			noteTimeTillNextNote += phrase.getSCDuration(noteIndex);
		}		
	}
	
	/**
	 * Sets the callback.
	 * @param callee The object to call.
	 * @param callback The method to call.
	 */
	public void setCallback(Object callee, Method callback) {
		this.callee = callee;
		this.callback = callback;
	}
	
	/**
	 * Sets the object on which callbacks are called.
	 * @param callee The object to call.
	 */
	public void setCallee(Object callee) {
		this.callee = callee;
	}
	
	/**
	 * Sets the method to call.
	 * @param callback The method to call.
	 */
	public void setCallback(Method callback) {
		this.callback = callback;
	}
	
	/**
	 * 
	 * @return The integer identifier of this PhraseReader.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * 
	 * @return The index of the note the phrase reader is currently reading
	 */
	public int getNoteIndex() {
		return noteIndex;
	}
}
