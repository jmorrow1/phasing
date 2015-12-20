package phases;

import java.util.ArrayList;

import arb.soundcipher.SCScore;
import processing.core.PApplet;

/**
 * Manages two representations of a musical phrase.
 * 
 * The first representation models a musical phrase as a sequence of notes.
 * In this case a note is a pitch, a dynamic, a duration, an articulation, and a pan value.
 * The class uses 5 parallel arrays to do this: one for pitches, one for dynamics, etc.
 * This representation's purpose is to directly interface with the soundcipher library.
 * From the perspective of the outside world, this representation's data is read-only.
 * 
 * The second representation models a musical phrase as a matrix with 5 rows with an arbitrary number of columns.
 * Again there are a similar set of 5 parameters (pitch, dynamic, etc.), and again parallel arrays are used.
 * The difference is what each column represents.
 * In the 1st representation, each column represented a note.
 * In this representation, each column represents a more abstract thing: a cell.
 * Owing to this difference, instead of an array of durations there is an array of "cell types".
 * A cell type can be one of 3 things: a "note start", a "note sustain", or a "rest".
 * A "note sustain" cell allows notes to exist over several consecutive cells.
 * This representation's purpose is to make it simple for the Editor to interface with a Phrase
 * (this representation models a musical phrase as a grid and so does the Editor).
 * From the perspective of the outside world, this representation's data is readable and writable.
 * 
 * @author James Morrow
 *
 */
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
	private int[] cellTypes;
	
	//phrase data, in soundcipher notation
	private float[] scPitches, scDynamics, scDurations, scArts, scPans;	
	
	/**
	 * Constructs an empty phrase.
	 */
	public Phrase() {
		gridPitches = new float[] {};
		gridDynamics = new float[] {};
		gridArts = new float[] {};
		gridPans = new float[] {};
		cellTypes = new int[] {};
		scArraysUpToDate = false;
	}
	
	/**
	 * Constructs a phrase with the given grid values.
	 * @param gridPitches The sequence of MIDI pitch values
	 * @param gridDynamics The sequence of dynamic values
	 * @param cellTypes The sequence of cell types
	 */
	public Phrase(float[] gridPitches, float[] gridDynamics, int[] cellTypes) {
		this(gridPitches, gridDynamics, cellTypes, 0.5f, 63);
	}
	
	/**
	 * Constructs a phrase with the given grid values.
	 * @param gridPitches The sequence of MIDI pitch values
	 * @param gridDynamics The sequence of dynamic values
	 * @param cellTypes The sequence of cell types
	 * @param defaultArt The articulation value to give each note
	 * @param defaultPan The pan value to give each note
	 */
	public Phrase(float[] gridPitches, float[] gridDynamics, int[] cellTypes, float defaultArt, float defaultPan) {
		if (gridPitches.length == gridDynamics.length && gridDynamics.length == cellTypes.length) {
			this.gridPitches = gridPitches;
			this.gridDynamics = gridDynamics;
			this.cellTypes = cellTypes;
			this.defaultArt = defaultArt;
			this.defaultPan = defaultPan;
			int n = gridPitches.length;
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
	
	/**
	 * Constructs a phrase with the given grid values.
	 * @param gridPitches The sequence of MIDI pitch values
	 * @param gridDynamics The sequence of dynamic values
	 * @param cellTypes The sequence of cell types
	 * @param gridArts The sequence of articulation values
	 * @param gridPans The sequence of pan values
	 */
	public Phrase(float[] gridPitches, float[] gridDynamics, int[] cellTypes, float[] gridArts, float[] gridPans) {
		if (gridPitches.length == gridDynamics.length && gridDynamics.length == cellTypes.length &&
				cellTypes.length == gridArts.length && gridArts.length == gridPans.length) {
			this.gridPitches = gridPitches;
			this.gridDynamics = gridDynamics;
			this.cellTypes = cellTypes;
			this.gridArts = gridArts;
			this.gridPans = gridPans;
			scArraysUpToDate = false;
		}
		else {
			System.err.println("Cannot construct phrase.");
		}
	}
	
	/**
	 * Adds a new cell to the grid, which is a rest by default.
	 */
	public void addCell() {
		gridPitches = PApplet.append(gridPitches, 0);
		gridDynamics = PApplet.append(gridDynamics, 0);
		cellTypes = PApplet.append(cellTypes, REST);
		gridArts = PApplet.append(gridArts, defaultArt);
		gridPans = PApplet.append(gridPans, defaultPan);
		scArraysUpToDate = false;
	}
	
	
	/**
	 * Sets a cell with the given grid data.
	 * @param i
	 * @param pitch
	 * @param dynamic
	 * @param noteType
	 * @return
	 */
	public boolean setCell(int i, float pitch, float dynamic, int noteType) {
		return setCell(i, pitch, dynamic, noteType, defaultArt, defaultPan);
	}
	
	/**
	 * Sets a cell with the given grid data.
	 * @param i
	 * @param pitch
	 * @param dynamic
	 * @param noteType
	 * @param art
	 * @param pan
	 * @return
	 */
	public boolean setCell(int i, float pitch, float dynamic, int noteType, float art, float pan) {
		if (0 <= i && i < getGridRowSize()) {
			boolean success = setNoteType(i, noteType);
			if (!success) {
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
			System.err.println("Index out of bounds in method setNote(" + i + ") in Phrase");
			return false;
		}
	}
	
	/**
	 * Applies a pan value to every cell and sets the new default pan value.
	 * @param pan The pan value
	 */
	public void panPhrase(float pan) {
		defaultPan = pan;
		for (int i=0; i<gridPans.length; i++) {
			gridPans[i] = pan;
		}
		scArraysUpToDate = false;
	}
	
	/**
	 * Adds this object's phrase information to an SCScore object (a soundcipher utility).
	 * @param score The SCScore object
	 * @param startBeat The beat on which to start
	 * @param channel The MIDI channel
	 * @param instrument The MIDI instrument
	 */
	public void addToScore(SCScore score, float startBeat, float channel, float instrument) {
		updateSCValues();
		score.empty();
		score.addPhrase(startBeat, channel, instrument, scPitches, scDynamics, scDurations, scArts, scPans);
	}
	
	/**
	 * Updates the data fields associated with the phrase's soundcipher representation
	 * to put them in agreement with the current state of the phrase.
	 * For efficiency reasons, often the phrase's soundcipher representation will be out of date.
	 * In these cases, a variable called "scArraysUpToDate" will be set to false.
	 * This indicates that if some other object requests soundcipher data or forces phrase to use soundcipher data,
	 * then the data fields associated with the soundcipher data will have to be updated, which is what this helper method is for.
	 */
	private void updateSCValues() {
		//count the number of notes
		int n=0;
		for (int i=0; i<cellTypes.length; i++) {
			if (cellTypes[i] == NOTE_START) {
				n++;
			}
			else if (cellTypes[i] == REST && (i == 0 || cellTypes[i-1] != REST)) {
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
		while (i < cellTypes.length) {
			//if new note
			if (cellTypes[i] == NOTE_START) {
				if (i != 0) j++;
				scPitches[j] = this.gridPitches[i];
				scDynamics[j] = this.gridDynamics[i];
				scDurations[j] = unitDuration;
				scArts[j] = this.gridArts[i];
				scPans[j] = this.gridPans[i];
			}
			//if continued note
			else if (cellTypes[i] == NOTE_SUSTAIN) {
				scDurations[j] += unitDuration;
			}
			//if rest
			else if (cellTypes[i] == REST) {
				//if new rest
				if (i == 0 || cellTypes[i-1] != REST) {
					if (i != 0) j++;
					scDynamics[j] = 0;
					scDurations[j] = unitDuration;
				}
				//if continued rest
				else if (i != 0 && cellTypes[i-1] == REST) {
					scDurations[j] += unitDuration;
				}
			}
			i++;
		}
		
		scArraysUpToDate = true;
	}
	
	/**
	 * Searches the phrase and returns the lowest value MIDI pitch in the phrase.
	 * @return The lowest value MIDI pitch
	 */
	public float minPitch() {
		float minPitch = Float.MAX_VALUE;
		for (int i=0; i<this.getNumNotes(); i++) {
			if (this.getSCPitch(i) < minPitch) {
				minPitch = this.getSCPitch(i);
			}
		}
		return minPitch;
	}
	
	/**
	 * Searches the phrase and returns the greatest value MIDI pitch in the phrase.
	 * @return The greatest value MIDI pitch
	 */
	public float maxPitch() {
		float maxPitch = Float.MIN_VALUE;
		for (int i=0; i<this.getGridRowSize(); i++) {
			if (this.getSCPitch(i) > maxPitch) {
				maxPitch = this.getSCPitch(i);
			}
		}
		return maxPitch;
	}
	
	/**
	 * 
	 * @return The number of cells in the grid.
	 */
	public int getGridRowSize() {
		return gridPitches.length;
	}
	
	/**
	 * 
	 * @return The number of notes in the phrase.
	 */
	public int getNumNotes() {
		if (!scArraysUpToDate) updateSCValues();
		return scPitches.length;
	}
	
	/**
	 * 
	 * @param i The index to the phrase's soundcipher representation
	 * @return The pitch [0-127] at the given index
	 */
	public int getSCPitch(int i) {
		if (!scArraysUpToDate) updateSCValues();
		return (int)scPitches[i];
	}
	
	/**
	 * 
	 * @param i The index to the phrase's soundcipher representation
	 * @return The duration value at the given index
	 */
	public float getSCDuration(int i) {
		if (!scArraysUpToDate) updateSCValues();
		return scDurations[i];
	}
	
	/**
	 * 
	 * @param i The index to the phrase's soundcipher representation
	 * @return The dynamic value [0-127] at the given index
	 */
	public float getSCDynamic(int i) {
		if (!scArraysUpToDate) updateSCValues();
		return scDynamics[i];
	}
	
	/**
	 * 
	 * @param i The index to the phrase's soundcipher representation
	 * @return The articulation value [a multiplier to duration] at the given index
	 */
	public float getSCArticulation(int i) {
		if (!scArraysUpToDate) updateSCValues();
		return scArts[i];
	}
	
	/**
	 * 
	 * @param i The index to the phrase's soundcipher representation
	 * @return The pan value [0-127] at the given index
	 */
	public float getSCPan(int i) {
		if (!scArraysUpToDate) updateSCValues();
		return scPans[i];
	}
	
	/**
	 * 
	 * @param i The index to the phrase's grid representation
	 * @param pitch The pitch value [0-127]
	 */
	public void setGridPitch(int i, float pitch) {
		gridPitches[i] = PApplet.constrain(pitch, 0, 127);
		scArraysUpToDate = false;
	}
	
	/**
	 * 
	 * @param i The index to the phrase's grid representation
	 * @return The pitch value [0-127] at the given index
	 */
	public float getGridPitch(int i) {
		return gridPitches[i];
	}
	
	/**
	 * 
	 * @param i The index to the phrase's grid representation
	 * @param dynamic The dynamic value [0-127]
	 */
	public void setGridDynamic(int i, float dynamic) {
		gridDynamics[i] = PApplet.constrain(dynamic, 0, 127);
		scArraysUpToDate = false;
	}
	
	/**
	 * 
	 * @param i The index to the phrase's grid representation
	 * @return The dynamic value [0-127] at the given index
	 */
	public float getGridDynamic(int i) {
		return gridDynamics[i];
	}
	
	/**
	 * 
	 * @param i The index to the phrase's grid representation
	 * @return The value of the type of cell at the given index
	 */
	public int getNoteType(int i) {
		return cellTypes[i];
	}
	
	/**
	 * Sets the cell type value. If successful, it returns true.
	 * For some inputs, the method will not rewrite the cell type value and will return false.
	 * For example, it does not make sense to put a note sustain after a rest. A note cannot be sustained if it hasn't been started.
	 * Or if the value given for the cellType is not valid, the method will return false.
	 * 
	 * @param i The index to the phrase's grid representation
	 * @param cellType The value of the type of cell
	 * @return True if the method was successful, false otherwise.
	 */
	public boolean setNoteType(int i, int cellType) {
		switch(cellType) {
			case NOTE_START:
				cellTypes[i] = cellType;					
				break;
			case REST:
				cellTypes[i] = cellType;
				break;
			case NOTE_SUSTAIN:
				if (i == 0) {
					System.err.println("Can't put a note sustain at the beginning of a phrase.");
					return false;
				}
				else if (cellTypes[i-1] == REST) {
					System.err.println("Can't put a note sustain after a rest.");
					return false;
				}
				else {
					cellTypes[i] = NOTE_SUSTAIN;
					break;
				}
			default:
				System.err.println("Invalid noteType code given to Phrase.setNoteType()");
				return false;
		}
		scArraysUpToDate = false;
		return true;
	}
	
	/**
	 * 
	 * @param i The index to the phrase's grid representation
	 * @return The pan value [0-127] at the given index
	 */
	public float getGridPan(int i) {
		return gridPans[i];
	}
	
	/**
	 * 
	 * @param i The index to the phrase's grid representation
	 * @param pan The pan value [0-127]
	 */
	public void setGridPan(int i, float pan) {
		gridPans[i] = pan;
		scArraysUpToDate = false;
	}
	
	/**
	 * 
	 * @param i The index to the phrase's grid representation
	 * @return The articulation value [0-127] at the given index
	 */
	public float getGridArt(int i) {
		return gridArts[i];
	}
	
	/**
	 * 
	 * @param i The index to the phrase's grid representation
	 * @param art The articulation value [0-127]
	 */
	public void setGridArt(int i, float art) {
		gridArts[i] = art;
		scArraysUpToDate = false;
	}
	
	/**
	 * 
	 * @return The total duration of the phrase
	 */
	public float getTotalDuration() {
		return getGridRowSize() * unitDuration;
	}
	
	/**
	 * 
	 * @return The duration of any one cell or columnn
	 */
	public float getUnitDuration() {
		return unitDuration;
	}
	
	public String toString() {
		return "{pitches: " + scPitches.toString() + ", dynamics: " + scDynamics.toString()
			+ ", durations: " + scDurations.toString();
	}
	
	/**
	 * Given a MIDI pitch value, returns a string representation of that note.
	 * For example returns the string "C" given 60 as a MIDI pitch value.
	 * @param code The MIDI pitch value
	 * @param useSharps Whether to use sharps or flats in the string output
	 * @return The string representation
	 */
	public static String convertPitch(float code, boolean useSharps) {
	    return convertPitch((int)code, useSharps);
	}

	/**
	 * Given a MIDI pitch value, returns a string representation of that note.
	 * For example returns the string "C" given 60 as a MIDI pitch value.
	 * @param code The MIDI pitch value
	 * @param useSharps Whether to use sharps or flats in the string output
	 * @return The string representation
	 */
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
