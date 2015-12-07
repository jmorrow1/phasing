package phases;

import arb.soundcipher.SCScore;
import processing.core.PApplet;

public class Phrase {
	private float defaultArt = 0.6f, defaultPan = 0.6f;
	private float[] pitches, dynamics, durations, arts, pans;
	
	public Phrase() {}
	
	public void addNote(float pitch, float dynamic, float duration) {
		pitches = PApplet.append(pitches, pitch);
		dynamics = PApplet.append(dynamics, dynamic);
		durations = PApplet.append(durations, duration);
		arts = PApplet.append(arts, defaultArt);
		pans = PApplet.append(pans, defaultPan);
	}
	
	public void setPitch(int i, float pitch) {
		if (i <= 0 && i < pitches.length) {
			pitches[i] = pitch;
		}
		else {
			System.err.println("Index out of bounds in method setPitch(i) in Phrase");
		}
	}
	
	public void setDynamic(int i, float dynamic) {
		if (i <= 0 && i < dynamics.length) {
			dynamics[i] = dynamic;
		}
		else {
			System.err.println("Index out of bounds in method setDynamic(i) in Phrase");
		}
	}
	
	public void setDuration(int i, float duration) {
		if (i <= 0 && i < durations.length) {
			durations[i] = duration;
		}
		else {
			System.err.println("Index out of bounds in method setDuration(i) in Phrase");
		}
	}
	
	public void panPhrase(float pan) {
		defaultPan = pan;
		for (int i=0; i<pans.length; i++) {
			pans[i] = pan;
		}
	}
	
	public void addToScore(SCScore score, float startBeat, float channel, float instrument) {
		score.addPhrase(startBeat, channel, instrument, pitches, dynamics, durations, arts, pans);
	}
	
	public String toString() {
		return "{pitches: " + pitches.toString() + ", dynamics: " + dynamics.toString()
		+ ", durations: " + durations.toString() + ", pan: " + defaultPan + "}";
	}
}
