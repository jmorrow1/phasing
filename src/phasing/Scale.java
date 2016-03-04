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
	 * Constructs a Scale from a JSONObject containing a name, an array of note names, and an array of note values.
	 * @param json The JSONObject
	 */
	public Scale(JSONObject json) {
		this (json, json.getString("className", "?"));
	}
	
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
	 * 
	 * @param i The note index
	 * @return The name of the ith note in the scale
	 */
	public String getNoteNameByIndex(int i) {
		return noteNames[i];
	}
	
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
	 * Does the opposite of getNoteValue(i).
	 * Where that method takes an index and returns the pitch at that index in the scale,
	 * this method takes a pitch and returns the index where that pitch is located in the scale
	 * @param midiPitchValue
	 * @return
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
