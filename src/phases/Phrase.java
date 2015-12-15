package phases;

import java.util.ArrayList;

import arb.soundcipher.SCScore;
import processing.core.PApplet;

public class Phrase {
	//note types (for grid notation version of phrase data)
	public static final int NOTE_START = 0, NOTE_SUSTAIN = 1, REST = 2;
	
	//parameters
	private float unitDuration = 0.25f;
	private float defaultArt, defaultPan;
	
	//for managing state relatively efficiently
	private boolean scArraysUpToDate = true;
	
	//phrase data, in grid notation
	private float[] gridPitches, gridDynamics, gridArts, gridPans;
	private int[] noteTypes;
	
	//phrase data, in soundcipher notation
	private float[] scPitches, scDynamics, scDurations, scArts, scPans;	
	
	public Phrase() {
		gridPitches = new float[] {};
		gridDynamics = new float[] {};
		gridArts = new float[] {};
		gridPans = new float[] {};
		noteTypes = new int[] {};
		scArraysUpToDate = false;
	}
	
	public Phrase(float[] pitches, float[] dynamics, int[] sustains) {
		this(pitches, dynamics, sustains, 0.5f, 63);
	}
	
	public Phrase(float[] pitches, float[] dynamics, int[] noteTypes, float defaultArt, float defaultPan) {
		if (pitches.length == dynamics.length && dynamics.length == noteTypes.length) {
			this.gridPitches = pitches;
			this.gridDynamics = dynamics;
			this.noteTypes = noteTypes;
			this.defaultArt = defaultArt;
			this.defaultPan = defaultPan;
			int n = pitches.length;
			gridArts = new float[n];
			gridPans = new float[n];
			for (int i=0; i<n; i++) {
				gridArts[i] = defaultArt;
				gridPans[i] = defaultPan;
			}
			scArraysUpToDate = false;
		}
		else {
			System.err.println("Cannot construct phrase.");
		}
	}
	
	public Phrase(float[] pitches, float[] dynamics, int[] noteTypes, float[] arts, float[] pans) {
		if (pitches.length == dynamics.length && dynamics.length == noteTypes.length &&
				noteTypes.length == arts.length && arts.length == pans.length) {
			this.gridPitches = pitches;
			this.gridDynamics = dynamics;
			this.noteTypes = noteTypes;
			this.gridArts = arts;
			this.gridPans = pans;
			scArraysUpToDate = false;
		}
		else {
			System.err.println("Cannot construct phrase.");
		}
	}
	
	public void addNote() {
		gridPitches = PApplet.append(gridPitches, 0);
		gridDynamics = PApplet.append(gridDynamics, 0);
		noteTypes = PApplet.append(noteTypes, REST);
		gridArts = PApplet.append(gridArts, defaultArt);
		gridPans = PApplet.append(gridPans, defaultPan);
		scArraysUpToDate = false;
	}
	
	public boolean setNoteType(int i, float pitch, float dynamic, int noteType) {
		return setNoteType(i, pitch, dynamic, noteType, defaultArt, defaultPan);
	}
	
	public boolean setNoteType(int i, float pitch, float dynamic, int noteType, float art, float pan) {
		if (0 <= i && i < getNumNotes()) {
			switch(noteType) {
				case NOTE_START:
				case REST:
					noteTypes[i] = noteType;
					break;
				case NOTE_SUSTAIN:
					if (i == 0) {
						System.err.println("Can't put a note sustain at the beginning of a phrase.");
						return false;
					}
					else if (noteTypes[i-1] == REST) {
						System.err.println("Can't put a note sustain after a rest.");
						return false;
					}
					else {
						noteTypes[i] = NOTE_SUSTAIN;
						break;
					}
				default:
					System.err.println("Invalid noteType code given to Phrase.setNoteType()");
					return false;
			}
			gridPitches[i] = pitch;
			gridDynamics[i] = dynamic;
			gridArts[i] = art;
			gridPans[i] = pan;
			scArraysUpToDate = false;
			return true;
		}
		else {
			System.err.println("Index out of bounds in method setDuration(" + i + ") in Phrase");
			return false;
		}
	}
	
	public void panPhrase(float pan) {
		defaultPan = pan;
		for (int i=0; i<gridPans.length; i++) {
			gridPans[i] = pan;
		}
		scArraysUpToDate = false;
	}
	
	public void addToScore(SCScore score, float startBeat, float channel, float instrument) {
		initSCValues();
		score.addPhrase(startBeat, channel, instrument, scPitches, scDynamics, scDurations, scArts, scPans);
	}
	
	private void initSCValues() {
		//count the number of notes
		int n=0;
		for (int i=0; i<noteTypes.length; i++) {
			if (noteTypes[i] == NOTE_START) {
				n++;
			}
			else if (noteTypes[i] == REST && (i == 0 || noteTypes[i-1] != REST)) {
				n++;
			}
		}
		//init soundcipher arrays
		scPitches = new float[n];
		scDynamics = new float[n];
		scDurations = new float[n];
		scArts = new float[n];
		scPans = new float[n];
		int i=0; //loops through soundcipher arrays
		int j=0; //loops through grid-notation arrays
		while (i < noteTypes.length) {
			//if new note
			if (noteTypes[i] == NOTE_START) {
				if (i != 0) j++;
				scPitches[j] = this.gridPitches[i];
				scDynamics[j] = this.gridDynamics[i];
				scDurations[j] = unitDuration;
				scArts[j] = this.gridArts[i];
				scPans[j] = this.gridPans[i];
			}
			//if continued note
			else if (noteTypes[i] == NOTE_SUSTAIN) {
				scDurations[j] += unitDuration;
			}
			//if rest
			else if (noteTypes[i] == REST) {
				//if new rest
				if (i == 0 || noteTypes[i-1] != REST) {
					if (i != 0) j++;
					scDynamics[j] = 0;
					scDurations[j] = unitDuration;
				}
				//if continued rest
				else if (i != 0 && noteTypes[i-1] == REST) {
					scDurations[j] += unitDuration;
				}
			}
			i++;
		}
		
		scArraysUpToDate = true;
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
		for (int i=0; i<this.getNumElements(); i++) {
			if (this.getPitch(i) > maxPitch) {
				maxPitch = this.getPitch(i);
			}
		}
		return maxPitch;
	}
	
	private int getNumElements() {
		return gridPitches.length;
	}
	
	public int getNumNotes() {
		if (!scArraysUpToDate) initSCValues();
		return scPitches.length;
	}
	
	public int getPitch(int i) {
		if (!scArraysUpToDate) initSCValues();
		return (int)scPitches[i];
	}
	
	public float getDuration(int i) {
		if (!scArraysUpToDate) initSCValues();
		return scDurations[i];
	}
	
	public float getDynamic(int i) {
		if (!scArraysUpToDate) initSCValues();
		return scDynamics[i];
	}
	
	public float getArticulation(int i) {
		if (!scArraysUpToDate) initSCValues();
		return scArts[i];
	}
	
	public float getPan(int i) {
		if (!scArraysUpToDate) initSCValues();
		return scPans[i];
	}
	
	public float getTotalDuration() {
		return getNumNotes() * unitDuration;
	}
	
	public String toString() {
		return "{pitches: " + scPitches.toString() + ", dynamics: " + scDynamics.toString()
			+ ", durations: " + scDurations.toString();
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
