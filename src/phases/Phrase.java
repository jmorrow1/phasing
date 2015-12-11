package phases;

import arb.soundcipher.SCScore;
import processing.core.PApplet;

public class Phrase {
	private float defaultArt, defaultPan;
	private float[] pitches, dynamics, durations, arts, pans;
	
	public Phrase() {}
	
	public Phrase(float[] pitches, float[] dynamics, float[] durations) {
		this(pitches, dynamics, durations, 0.5f, 63);
	}
	
	public Phrase(float[] pitches, float[] dynamics, float[] durations, float defaultArt, float defaultPan) {
		if (pitches.length == dynamics.length && dynamics.length == durations.length) {
			this.pitches = pitches;
			this.dynamics = dynamics;
			this.durations = durations;
			this.defaultArt = defaultArt;
			this.defaultPan = defaultPan;
			int n = pitches.length;
			arts = new float[n];
			pans = new float[n];
			for (int i=0; i<n; i++) {
				arts[i] = defaultArt;
				pans[i] = defaultPan;
			}
		}
	}
	
	public Phrase(float[] pitches, float[] dynamics, float[] durations, float[] arts, float[] pans) {
		if (pitches.length == dynamics.length && dynamics.length == durations.length &&
				durations.length == arts.length && arts.length == pans.length) {
			this.pitches = pitches;
			this.dynamics = dynamics;
			this.durations = durations;
			this.arts = arts;
			this.pans = pans;
		}
	}
	
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
			System.err.println("Index out of bounds in method setPitch(" + i + ") in Phrase");
		}
	}
	
	public void setDynamic(int i, float dynamic) {
		if (i <= 0 && i < dynamics.length) {
			dynamics[i] = dynamic;
		}
		else {
			System.err.println("Index out of bounds in method setDynamic(" + i + ") in Phrase");
		}
	}
	
	public void setDuration(int i, float duration) {
		if (i <= 0 && i < durations.length) {
			durations[i] = duration;
		}
		else {
			System.err.println("Index out of bounds in method setDuration(" + i + ") in Phrase");
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
	
	public float minPitch() {
		float minPitch = Float.MAX_VALUE;
		for (int i=0; i<this.getNumNotes(); i++) {
			if (this.getPitch(i) < minPitch) {
				minPitch = this.getPitch(i);
			}
		}
		return minPitch;
	}
	
	public float maxPitch() {
		float maxPitch = Float.MIN_VALUE;
		for (int i=0; i<this.getNumNotes(); i++) {
			if (this.getPitch(i) > maxPitch) {
				maxPitch = this.getPitch(i);
			}
		}
		return maxPitch;
	}
	
	public int getNumNotes() {
		return pitches.length;
	}
	
	public int getPitch(int i) {
		return (int)pitches[i];
	}
	
	public float getDuration(int i) {
		return durations[i];
	}
	
	public float getDynamic(int i) {
		return dynamics[i];
	}
	
	public float getArticulation(int i) {
		return arts[i];
	}
	
	public float getPan(int i) {
		return pans[i];
	}
	
	public float getTotalDuration() {
		float sum = 0;
		for (int i=0; i<durations.length; i++) {
			sum += durations[i];
		}
		return sum;
	}
	
	public String toString() {
		return "{pitches: " + pitches.toString() + ", dynamics: " + dynamics.toString()
		+ ", durations: " + durations.toString() + ", pan: " + defaultPan + "}";
	}
	
	public static String convertPitch(float code, boolean useSharps) {
	    return convertPitch((int)code, useSharps);
	}

	public static String convertPitch(int code, boolean useSharps) {
	    if (21 <= code && code <= 108) {
	        String s = "";
	        code -= 21;
	        int octave = code / 12 + 1;
	        int note = code % 12;
	        switch(note) {
	            case 0 : s += "A"; break;
	            case 1 : 
	                if (useSharps) s += "A#";
	                else s += "Bb";
	                break;
	            case 2 : s += "B"; break;
	            case 3 : s += "C"; break;
	            case 4 :
	                if (useSharps) s += "C#";
	                else s += "Db";
	                break;
	            case 5 : s += "D"; break;
	            case 6 : 
	                if (useSharps) s += "D#";
	                else s += "Eb";
	                break;
	            case 7 : s += "E"; break;
	            case 8 : s += "F"; break;
	            case 9 : 
	                if (useSharps) s += "F#";
	                else s += "Gb";
	                break;
	            case 10 : s += "G"; break;
	            case 11 :
	                if (useSharps) s += "G#"; 
	                else s += "Ab";
	                break;
	        }
	        //s += octave;
	        return s;
	    }
	    else {
	        return "";
	    }
	}
}
