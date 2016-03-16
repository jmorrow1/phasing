package phasing;

import java.util.Arrays;

import processing.data.JSONArray;
import processing.data.JSONObject;
import util.JSONable;

/**
 * Data container for a musical scale.
 * 
 * @author James Morrow
 *
 */
public class Scale implements JSONable {
	private final String name;
	private final String className;
	private final String[] noteNames;
	private final int[] noteValues;
	private final JSONObject json;
	
	/**
	 * Constructs a Scale from a JSONObject containing a name, an array of note names, and an array of note values.
	 * @param json The JSONObject
	 * @param className The name of the class this scale belongs to ("Major", for instance).
	 */
	public Scale(JSONObject json, String className) {
		name = json.getString("name");
		JSONArray jNoteNames = json.getJSONArray("noteNames");
		noteNames = new String[jNoteNames.size()];
		for (int i=0; i<noteNames.length; i++) {
			noteNames[i] = jNoteNames.getString(i);
		}
		JSONArray jNoteValues = json.getJSONArray("noteValues");
		noteValues = new int[jNoteValues.size()];
		for (int i=0; i<noteValues.length; i++) {
			noteValues[i] = jNoteValues.getInt(i);
		}
		this.className = className;
		this.json = json;
	}
	
	/**
	 * Constructs a Scale from a JSONObject containing a name, a class name, an array of note names, and an array of note values.
	 * @param json The JSONObject
	 */
	public Scale(JSONObject json) {
		this (json, json.getString("className", "?"));
	}
	
	@Override
	public JSONObject toJSON() {
		json.setString("className", className);
		return json;
	}
	
	/**
	 * 
	 * @return The number of notes in the scale
	 */
	public int size() {
		return noteNames.length;
	}

	/**
	 * 
	 * @return The name of the scale. "A", for example.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return The class name of the scale. "Major Pentatonic", for example.
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Gives the name of the note at the ith index, with 0 <= i && i < this.size().
	 * 
	 * Throws an array out of bounds exception if i is out of range.
	 * @param i The note index
	 * @return The name of the ith note in the scale
	 */
	public String getNoteNameByIndex(int i) throws ArrayIndexOutOfBoundsException {
		return noteNames[i];
	}
	
	/**
	 * Gives the name for the given pitchValue that applies in the context of this scale.
	 * The given pitchValue should be a pitch value within this scale.
	 * If it isn't, the method returns "?".
	 * 
	 * Won't throw an array out of bounds exception.
	 * 
	 * @param pitchValue The MIDI value of the pitch.
	 * @return The name of the note in the context of this scale, or "?" if the note doesn't exist in this scale.
	 */
	public String getNoteNameByPitchValue(int pitchValue) {
		int i = this.getIndexOfNoteValue(pitchValue);
		if (i != -1) {
			i %= this.size();
			return noteNames[i];
		}
		else {
			return "?";
		}
	}
	
	/**
	 * Gives the value of the note at the index i.
	 * if i > this.size(), that value is extrapolated, so large values for i are perfectly acceptable.
	 * 
	 * Will throw an array out of bounds exception if and only if (i < 0).
	 * 
	 * @param i The note index
	 * @return The MIDI pitch value of the ith note in the scale
	 */
	public int getNoteValue(int i) {
		if (0 <= i && i < noteValues.length) {
			return noteValues[i];
		}
		else {
			return noteValues[i % noteValues.length] + (i / noteValues.length) * 12;
		}
	}
	
	/**
	 * Gives the index of the given note in the context of this scale.
	 * If the given note does not exist in this scale, then the method will return -1.
	 * If i > this.size(), the index is extrapolated, so large values for i are perfectly acceptable.
	 * Same for small values for i, though this property is less likely to be useful.
	 * 
	 * @param midiPitchValue The value of the note.
	 * @return The index in this scale of the given note, or -1 if the given note does not exist in this scale.
	 */
	public int getIndexOfNoteValue(int midiPitchValue) {
		int minPitch = noteValues[0];
		int maxPitch = minPitch + 12;
		int value = PhasesPApplet.remainder(midiPitchValue, minPitch, maxPitch);
		
		for (int i=0; i<noteValues.length; i++) {
			if (value == noteValues[i]) {
				return i + ( (midiPitchValue - minPitch) / 12) * noteValues.length;
			}
		}
		
		return -1;
	}

	@Override
	public String toString() {
		return "Scale [name=" + name + ", noteNames=" + Arrays.toString(noteNames) + ", noteValues="
				+ Arrays.toString(noteValues) + "]";
	}
}