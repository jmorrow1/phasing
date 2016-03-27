package phasing;

import java.util.Arrays;

import arb.soundcipher.SCScore;
import processing.core.PApplet;
import processing.data.JSONObject;
import util.JSONable;

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
public class Phrase implements JSONable {
	//note types (for grid notation version of phrase data)
	public static final int NOTE_START = 0, NOTE_SUSTAIN = 1, REST = 2;
	
	//parameters
	protected float unitDuration = 0.25f;
	protected float defaultArt, defaultPan;
	
	//for managing state relatively efficiently
	protected boolean scArraysUpToDate = true;
	protected float minPitch, maxPitch;
	
	//phrase data, in grid notation
	protected float[] gridPitches, gridDynamics, gridArts, gridPans;
	protected int[] cellTypes;
	
	//phrase data, in soundcipher notation
	protected float[] scPitches, scDynamics, scDurations, scArts, scPans;
	
	//other
	protected float[] scIndexToPercentDuration;
	
	//meta data
	private String scaleClassName, scaleRootName;
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	/**
	 * Constructs an empty phrase.
	 */
	public Phrase(String scaleClassName, String scaleRootName) {
		gridPitches = new float[] {};
		gridDynamics = new float[] {};
		gridArts = new float[] {};
		gridPans = new float[] {};
		cellTypes = new int[] {};
		scArraysUpToDate = false;
		this.scaleClassName = scaleClassName;
		this.scaleRootName = scaleRootName;
	}
	
	/**
	 * Constructs a phrase with the given grid values.
	 * @param gridPitches The sequence of MIDI pitch values
	 * @param gridDynamics The sequence of dynamic values
	 * @param cellTypes The sequence of cell types
	 */
	public Phrase(float[] gridPitches, float[] gridDynamics, int[] cellTypes,
			String scaleClassName, String scaleRootName) {
		this(gridPitches, gridDynamics, cellTypes, 0.5f, 63, scaleClassName, scaleRootName);
	}
	
	/**
	 * Constructs a phrase with the given grid values.
	 * @param gridPitches The sequence of MIDI pitch values
	 * @param gridDynamics The sequence of dynamic values
	 * @param cellTypes The sequence of cell types
	 * @param defaultArt The articulation value to give each note
	 * @param defaultPan The pan value to give each note
	 * @param scaleClassName
	 * @param scaleRootName
	 */
	public Phrase(float[] gridPitches, float[] gridDynamics, int[] cellTypes, float defaultArt, float defaultPan,
			String scaleClassName, String scaleRootName) {
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
			this.scaleClassName = scaleClassName;
			this.scaleRootName = scaleRootName;
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
	 * @param gridPans The sequence of pan value
	 * @param defaultArt
	 * @param defaultPan
	 * @param scaleClassName
	 * @param scaleRootName
	 */
	public Phrase(float[] gridPitches, float[] gridDynamics, int[] cellTypes, float[] gridArts, float[] gridPans,
			float defaultArt, float defaultPan, String scaleClassName, String scaleRootName) {
		if (gridPitches.length == gridDynamics.length && gridDynamics.length == cellTypes.length &&
				cellTypes.length == gridArts.length && gridArts.length == gridPans.length) {
			this.gridPitches = gridPitches;
			this.gridDynamics = gridDynamics;
			this.cellTypes = cellTypes;
			this.gridArts = gridArts;
			this.gridPans = gridPans;
			this.defaultArt = defaultArt;
			this.defaultPan = defaultPan;
			scArraysUpToDate = false;
			this.scaleClassName = scaleClassName;
			this.scaleRootName = scaleRootName;
		}
		else {
			System.err.println("Cannot construct phrase.");
		}
	}
	
	/**
	 * Copy constructor. This does a deep copy.
	 * @param phrase The phrase to copy.
	 */
	public Phrase(Phrase phrase) {
		if (phrase != null) {
			this.unitDuration = phrase.unitDuration;
			
			this.defaultArt = phrase.defaultArt;
			this.defaultPan = phrase.defaultPan;
			
			this.gridPitches = Arrays.copyOf(phrase.gridPitches, phrase.gridPitches.length);
			this.gridDynamics = Arrays.copyOf(phrase.gridDynamics, phrase.gridDynamics.length);
			this.gridArts = Arrays.copyOf(phrase.gridArts, phrase.gridArts.length);
			this.gridPans = Arrays.copyOf(phrase.gridPans, phrase.gridPans.length);
			this.cellTypes = Arrays.copyOf(phrase.cellTypes, phrase.cellTypes.length);
	
			this.scaleClassName = new String(phrase.scaleClassName);
			this.scaleRootName = new String(phrase.scaleRootName);
			
			scArraysUpToDate = false;
		}
		else {
			gridPitches = new float[] {};
			gridDynamics = new float[] {};
			gridArts = new float[] {};
			gridPans = new float[] {};
			cellTypes = new int[] {};
			scArraysUpToDate = false;
			
			this.scaleClassName = "Chromatic";
			this.scaleRootName = "C";
		}
	}
	
	/**
	 * Constructs a phrase from a JSONObject.
	 * @param json A JSONObject representing the phrase.
	 */
	public Phrase(JSONObject json) {
		if (json.hasKey("gridPitches") && json.hasKey("gridDynamics") && json.hasKey("gridArts") && 
				json.hasKey("gridPans") && json.hasKey("cellTypes")) {
			gridPitches = Util.toFloatArray(json.getJSONArray("gridPitches"));
			gridDynamics = Util.toFloatArray(json.getJSONArray("gridDynamics"));
			gridArts = Util.toFloatArray(json.getJSONArray("gridArts"));
			gridPans = Util.toFloatArray(json.getJSONArray("gridPans"));
			cellTypes = Util.toIntArray(json.getJSONArray("cellTypes"));
			defaultPan = json.getFloat("defaultPan", defaultPan);
			defaultArt = json.getFloat("defaultArt", defaultArt);
			scArraysUpToDate = false;
			
			this.scaleClassName = json.getString("scaleClassName", "Chromatic");
			this.scaleRootName = json.getString("scaleRootName", "C");
		}
		else {
			gridPitches = new float[] {};
			gridDynamics = new float[] {};
			gridArts = new float[] {};
			gridPans = new float[] {};
			cellTypes = new int[] {};
			scArraysUpToDate = false;
			
			this.scaleClassName = "Chromatic";
			this.scaleRootName = "C";
		}
		
		unitDuration = json.getFloat("unitDuration", unitDuration);
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.setJSONArray("gridPitches", Util.jsonify(gridPitches));
		json.setJSONArray("gridDynamics", Util.jsonify(gridDynamics));
		json.setJSONArray("gridArts", Util.jsonify(gridArts));
		json.setJSONArray("gridPans", Util.jsonify(gridPans));
		json.setJSONArray("cellTypes", Util.jsonify(cellTypes));
		json.setFloat("defaultPan", defaultPan);
		json.setFloat("defaultArt", defaultArt);
		json.setFloat("unitDuration", unitDuration);
		json.setString("scaleClassName", scaleClassName);
		json.setString("scaleRootName", scaleRootName);
		return json;
	}
	
	/**
	 * Performs a deep copy of the give phrase's fields to this phrase's fields.
	 * @param phrase The phrase to copy from.
	 */
	public void set(Phrase phrase) {
		this.defaultArt = phrase.defaultArt;
		this.defaultPan = phrase.defaultPan;
		this.gridPitches = Arrays.copyOf(phrase.gridPitches, phrase.gridPitches.length);
		this.gridDynamics = Arrays.copyOf(phrase.gridDynamics, phrase.gridDynamics.length);
		this.gridArts = Arrays.copyOf(phrase.gridArts, phrase.gridArts.length);
		this.gridPans = Arrays.copyOf(phrase.gridPans, phrase.gridPans.length);
		this.cellTypes = Arrays.copyOf(phrase.cellTypes, phrase.cellTypes.length);
		this.unitDuration = phrase.unitDuration;
		this.scaleClassName = new String(phrase.scaleClassName);
		this.scaleRootName = new String(phrase.scaleRootName);
		scArraysUpToDate = false;
	}
	
	/*****************************
	 ***** Grid Manipulation *****
	 *****************************/
	
	/**
	 * Adds a new cell to the tail end of the grid, which is a rest by default.
	 */
	public void appendCell() {
		gridPitches = PApplet.append(gridPitches, 0);
		gridDynamics = PApplet.append(gridDynamics, 0);
		cellTypes = PApplet.append(cellTypes, REST);
		gridArts = PApplet.append(gridArts, defaultArt);
		gridPans = PApplet.append(gridPans, defaultPan);
		scArraysUpToDate = false;
	}
	
	/**
	 * Removes a cell from the tail end of the grid.
	 */
	public void removeLastCell() {
		if (getGridRowSize() > 0) {
			gridPitches = PApplet.shorten(gridPitches);
			gridDynamics = PApplet.shorten(gridDynamics);
			cellTypes = PApplet.shorten(cellTypes);
			gridArts = PApplet.shorten(gridArts);
			gridPans = PApplet.shorten(gridPans);
			scArraysUpToDate = false;
		}
	}
	
	/**************************
	 ***** To SoundCipher *****
	 **************************/
	
	/**
	 * Adds this object's phrase information to an SCScore object (a soundcipher utility).
	 * @param score The SCScore object
	 * @param startBeat The beat on which to start
	 * @param channel The MIDI channel
	 * @param instrument The MIDI instrument
	 */
	public void addToScore(SCScore score, float startBeat, float channel, float instrument) {
		if (!scArraysUpToDate) {
			updateSCValues();
		}
		score.empty();
		score.addPhrase(startBeat, channel, instrument, scPitches, scDynamics, scDurations, scArts, scPans);
	}

	/****************************
	 ***** State Management *****
	 ****************************/
	
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
		
		//compute sc array values
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
		
		//compute scIndexToPercentDuration
		scIndexToPercentDuration = new float[n];
		float durationAcc = 0;
		for (int k = 0; k < scPitches.length; k++) {
			scIndexToPercentDuration[k] = durationAcc / getTotalDuration();
			durationAcc += scDurations[k];
		}
		
		//compute minimimum pitch
		minPitch = Float.MAX_VALUE;
		for (int k = 0; k < scPitches.length; k++) {
			if (scDynamics[k] > 0 && scPitches[k] < minPitch) {
				minPitch = scPitches[k];
			}
		}
			
		//compute maximum pitch
		maxPitch = Float.MIN_VALUE;
		for (int k = 0; k < scPitches.length; k++) {
			if (scDynamics[k] > 0 && scPitches[k] > maxPitch) {
				maxPitch = scPitches[k];
			}
		}
		
		scArraysUpToDate = true;
	}
	
	/*******************************
	 ***** Getters and Setters ***** 
	 *******************************/
	
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
	 * Searches the phrase and returns the lowest value MIDI pitch in the phrase.
	 * Rests don't count as having a pitch, so they're excluded from the search.
	 * @return The lowest value MIDI pitch
	 */
	public float minPitch() {
		if (!scArraysUpToDate) {
			updateSCValues();
		}
		return minPitch;
	}
	
	/**
	 * Searches the phrase and returns the greatest value MIDI pitch in the phrase.
	 * Rests don't count as having a pitch, so they're excluded from the search.
	 * @return The greatest value MIDI pitch
	 */
	public float maxPitch() {
		if (!scArraysUpToDate) {
			updateSCValues();
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
	 * Tells whether or not the note at the given index is a rest.
	 * @param scIndex Index to the soundcipher pitches.
	 * @return True if there is a rest at the given index, false otherwise.
	 */
	public boolean isRest(int scIndex) {
		if (0 <= scIndex && scIndex < getNumNotes()) {
			return getSCDynamic(scIndex) == 0;
		}
		else {
			return false;
		}
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
	 * Copies the scPitches array and returns it.
	 * @return A copy of the Phrase's pitch values.
	 */
	public float[] getSCPitches() {
		return Arrays.copyOf(scPitches, scPitches.length);
	}
	
	/**
	 * Copies the scDynamics array and returns it.
	 * @return A copy of the Phrase's dynamic values.
	 */
	public float[] getSCDynamics() {
		return Arrays.copyOf(scDynamics, scDynamics.length);
	}
	
	/**
	 * Copies the scDurations array and returns it.
	 * @return A copy of the Phrase's duration values.
	 */
	public float[] getSCDurations() {
		return Arrays.copyOf(scDurations, scDurations.length);
	}
	
	/**
	 * Copies the scArts array and returns it.
	 * @return A copy of the Phrase's articulation values.
	 */
	public float[] getSCArticulations() {
		return Arrays.copyOf(scArts, scArts.length);
	}
	
	/**
	 * Copies the scPans array and returns it.
	 * @return A copy of the Phrase's pan values.
	 */
	public float[] getSCPans() {
		return Arrays.copyOf(scPans, scPans.length);
	}
	
	/**
	 * It looks up the note at the given scIndex. Then it looks up the start time of that note.
	 * It compares that start time to the total duration of the phrase and returns that ratio.
	 * 
	 * @param scIndex The index of the desired note.
	 * @return The ratio between the note's start time and the total duration of the phrase.
	 */
	public float getPercentDurationOfSCIndex(int scIndex) {
		if (!scArraysUpToDate) updateSCValues();
		return scIndexToPercentDuration[scIndex];
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
	
	/**
	 * Sets the name of the scale class.
	 * @param scaleClassName The name of the scale class.
	 */
	public void setScaleClassName(String scaleClassName) {
		this.scaleClassName = scaleClassName;
	}
	
	/**
	 * Gives the name of the scale class.
	 * @return The name of the scale class.
	 */
	public String getScaleClassName() {
		return scaleClassName;
	}
	
	/**
	 * Sets the name of the scale root.
	 * @param scaleRootName The name of the scale root.
	 */
	public void setScaleRootName(String scaleRootName) {
		this.scaleRootName = scaleRootName;
	}
	
	/**
	 * Gives the name of the scale root.
	 * @return The name of the scale root.
	 */
	public String getScaleRootName() {
		return scaleRootName;
	}
	
	/*********************
	 ***** To String *****
	 *********************/

	/**
	 *
	 * @return A string representation of the cellTypes array.
	 */
	public String cellTypesToString() {
		String s = "";
		for (int i=0; i<cellTypes.length; i++) {
			switch (cellTypes[i]) {
				case NOTE_START: s += "START   "; break;
				case NOTE_SUSTAIN: s += "SUSTAIN "; break;
				case REST: s += "REST    "; break;
				default: s += "UNKNOWN "; break;
			}
		}
		return s;
	}
	
	@Override
	public String toString() {
		if (!scArraysUpToDate) updateSCValues();
		return "{pitches: " + Arrays.toString(scPitches) + ", dynamics: " + Arrays.toString(scDynamics)
			+ ", durations: " + Arrays.toString(scDurations);
	}
	
	/****************************
	 ***** Static Functions *****
	 ****************************/
	
	/**
	 * Gives a new Phrase that is the given Phrase played backwards.
	 * 
	 * @param phrase The given Phrase.
	 * @return The new Phrase.
	 */
	public static Phrase reverse(Phrase phrase) {
		float[] gridPitches = reverse(phrase.gridPitches);
		float[] gridDynamics = reverse(phrase.gridDynamics);
		int[] cellTypes = readCellTypesBackwards(phrase.cellTypes);
		float[] gridArts = reverse(phrase.gridArts);
		float[] gridPans = reverse(phrase.gridPans);
		float defaultArt = phrase.defaultArt;
		float defaultPan = phrase.defaultPan;
		String scaleClassName = phrase.scaleClassName;
		String scaleRootName = phrase.scaleRootName;	
		
		return new Phrase(gridPitches, gridDynamics, cellTypes, gridArts, gridPans,
				defaultArt, defaultPan, scaleClassName, scaleRootName);
	}
	
	/**
	 * Takes an array of cell types and reads it in backwards order, returning a new array of cell types.
	 * 
	 * @param in The array of cell types.
	 * @return The result of reading the array of cell types in backwards order.
	 */
	private static int[] readCellTypesBackwards(int[] in) {
		int[] out = new int[in.length];
		
		//initiate a state machine-esque computation:
		if (in.length != 0) {
			int cell = in[in.length - 1];
			switch (cell) {
				case REST : sA(in, out, 0); break;
				case NOTE_START : sB(in, out, 0); break;
				case NOTE_SUSTAIN : sC1(in, out, 0); break;
			}
		}
		
		return out;
	}
	
	//read a rest.
	private static void sA(int[] src, int[] dest, int i) {
		dest[i] = REST;
		i++;
		if (i < src.length) {
			int nextCell = src[src.length - 1 - i];
			switch (nextCell) {
				case REST : sA(src, dest, i); break;
				case NOTE_START : sB(src, dest, i); break;
				case NOTE_SUSTAIN : sC1(src, dest, i); break;
			}
		}
	}
	
	//read a one-cell note.
	private static void sB(int[] src, int[] dest, int i) {
		dest[i] = NOTE_START;
		i++;
		if (i < src.length) {
			int nextCell = src[src.length - 1 - i];
			switch (nextCell) {
				case REST : sA(src, dest, i); break;
				case NOTE_START : sB(src, dest, i); break;
				case NOTE_SUSTAIN : sC1(src, dest, i); break;
			}
		}
	}
	
	//start reading a sustained note backwards
	private static void sC1(int[] src, int[] dest, int i) {
		dest[i] = NOTE_START;
		i++;
		if (i < src.length) {
			int nextCell = src[src.length - 1 - i];
			switch (nextCell) {
				case NOTE_START : sC3(src, dest, i); break;
				case NOTE_SUSTAIN : sC2(src, dest, i); break;
			}
		}
	}
	
	//continue to read a sustained note backwards
	private static void sC2(int[] src, int[] dest, int i) {
		dest[i] = NOTE_SUSTAIN;
		i++;
		if (i < src.length) {
			int nextCell = src[src.length - 1 - i];
			switch (nextCell) {
				case NOTE_START : sC3(src, dest, i); break;
				case NOTE_SUSTAIN : sC2(src, dest, i); break;
			}
		}
	}
	
	//finish reading a sustained note backwards
	private static void sC3(int[] src, int[] dest, int i) {
		dest[i] = NOTE_SUSTAIN;
		i++;
		if (i < src.length) {
			int nextCell = src[src.length - 1 - i];
			switch (nextCell) {
				case REST : sA(src, dest, i); break;
				case NOTE_START : sB(src, dest, i); break;
				case NOTE_SUSTAIN : sC1(src, dest, i); break;
			}
		}
	}
	
	/**
	 * Returns a new array that is the input array with its values reversed.
	 * 
	 * @param xs The input array.
	 * @return The new array.
	 */
	private static float[] reverse(float[] xs) {
		float[] ys = new float[xs.length];
		int i = 0;
		int j = ys.length - 1;
		while (i < ys.length) {
			ys[i] = xs[j];
			i++;
			j--;
		}
		return ys;
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
	        return s;
	    }
	    else {
	        return "";
	    }
	}
}