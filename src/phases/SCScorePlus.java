package phases;

import arb.soundcipher.SCScore;

public class SCScorePlus extends SCScore {
	public long getTickPosition() {
		return sequencer.getTickPosition();
	}
	
	public long getTickLength() {
		return sequencer.getTickLength();
	}
}
