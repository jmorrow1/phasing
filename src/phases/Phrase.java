package phases;

import arb.soundcipher.SCScore;
import processing.core.PApplet;

public class Phrase {
	private double defaultArt, defaultPan;
	private double[] pitches, dynamics, durations, arts, pans;
	
	public Phrase() {}
	
	public Phrase(double[] pitches, double[] dynamics, double[] durations) {
		this(pitches, dynamics, durations, 0.5f, 63);
	}
	
	public Phrase(double[] pitches, double[] dynamics, double[] durations, double defaultArt, double defaultPan) {
		if (pitches.length == dynamics.length && dynamics.length == durations.length) {
			this.pitches = pitches;
			this.dynamics = dynamics;
			this.durations = durations;
			this.defaultArt = defaultArt;
			this.defaultPan = defaultPan;
			int n = pitches.length;
			arts = new double[n];
			pans = new double[n];
			for (int i=0; i<n; i++) {
				arts[i] = defaultArt;
				pans[i] = defaultPan;
			}
		}
	}
	
	public Phrase(double[] pitches, double[] dynamics, double[] durations, double[] arts, double[] pans) {
		if (pitches.length == dynamics.length && dynamics.length == durations.length &&
				durations.length == arts.length && arts.length == pans.length) {
			this.pitches = pitches;
			this.dynamics = dynamics;
			this.durations = durations;
			this.arts = arts;
			this.pans = pans;
		}
	}
	
	/*public void addNote(double pitch, double dynamic, double duration) {
		pitches = PhasesPApplet.append(pitches, pitch);
		dynamics = PhasesPApplet.append(dynamics, dynamic);
		durations = PhasesPApplet.append(durations, duration);
		arts = PhasesPApplet.append(arts, defaultArt);
		pans = PhasesPApplet.append(pans, defaultPan);
	}*/
	
	public void setPitch(int i, double pitch) {
		if (i <= 0 && i < pitches.length) {
			pitches[i] = pitch;
		}
		else {
			System.err.println("Index out of bounds in method setPitch(" + i + ") in Phrase");
		}
	}
	
	public void setDynamic(int i, double dynamic) {
		if (i <= 0 && i < dynamics.length) {
			dynamics[i] = dynamic;
		}
		else {
			System.err.println("Index out of bounds in method setDynamic(" + i + ") in Phrase");
		}
	}
	
	public void setDuration(int i, double duration) {
		if (i <= 0 && i < durations.length) {
			durations[i] = duration;
		}
		else {
			System.err.println("Index out of bounds in method setDuration(" + i + ") in Phrase");
		}
	}
	
	public void panPhrase(double pan) {
		defaultPan = pan;
		for (int i=0; i<pans.length; i++) {
			pans[i] = pan;
		}
	}
	
	public void addToScore(SCScore score, double startBeat, double channel, double instrument) {
		score.addPhrase(startBeat, channel, instrument, pitches, dynamics, durations, arts, pans);
	}
	
	public double minPitch() {
		double minPitch = Float.MAX_VALUE;
		for (int i=0; i<this.getNumNotes(); i++) {
			if (this.getPitch(i) < minPitch) {
				minPitch = this.getPitch(i);
			}
		}
		return minPitch;
	}
	
	public double maxPitch() {
		double maxPitch = Float.MIN_VALUE;
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
	
	public double getDuration(int i) {
		return durations[i];
	}
	
	public double getDynamic(int i) {
		return dynamics[i];
	}
	
	public double getArticulation(int i) {
		return arts[i];
	}
	
	public double getPan(int i) {
		return pans[i];
	}
	
	public double getTotalDuration() {
		double sum = 0;
		for (int i=0; i<durations.length; i++) {
			sum += durations[i];
		}
		return sum;
	}
	
	public String toString() {
		return "{pitches: " + pitches.toString() + ", dynamics: " + dynamics.toString()
		+ ", durations: " + durations.toString() + ", pan: " + defaultPan + "}";
	}
	
	public static String convertPitch(double code, boolean useSharps) {
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
