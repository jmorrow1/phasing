package phasing;

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
	
	PhraseReaderListener listener;
	
	/**
	 * 
	 * @param phrase The phrase to read
	 * @param id An integer identifying the phrase reader
	 * @param callee The object on which to invoke the callback method
	 * @param callback The callback method
	 */
	public PhraseReader(Phrase phrase, int id, PhraseReaderListener listener) {
		this.phrase = phrase;
		this.id = id;
		this.listener = listener;
		
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
			listener.noteEvent(this);	
		}
	}
	
	/**
	 * After a period of neglect (in which update has not been invoked) this method informs the PhraseReader of where to start reading again.
	 * @param notept The place at which to start reading again.
	 */
	public void wakeUp(float notept) {
		noteIndex = -1;
		noteTimeTillNextNote = -notept;
		
		if (phrase.getNumNotes() > 0) {
			while (noteTimeTillNextNote <= 0) {
				noteIndex = (noteIndex+1) % phrase.getNumNotes();
				noteTimeTillNextNote += phrase.getSCDuration(noteIndex);
			}
		}
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
	
	/**
	 * 
	 * @author James Morrow
	 *
	 */
	public static interface PhraseReaderListener {
		/**
		 * Responds to a note event.
		 * 
		 * @param reader The PhraseReader sending the note events.
		 */
		public void noteEvent(PhraseReader reader);
	}

}
