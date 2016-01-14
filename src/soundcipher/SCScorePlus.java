package soundcipher;

import arb.soundcipher.SCScore;

/**
 * Extends the functionality of an SCScore (a container of musical score data)
 * with methods that expose the position of the playhead while the score is being played.
 * 
 * @author James Morrow
 *
 */
public class SCScorePlus extends SCScore {
	public long getTickPosition() {
		return sequencer.getTickPosition();
	}
	
	public long getTickLength() {
		return sequencer.getTickLength();
	}
}
