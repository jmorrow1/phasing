package phases;

import java.util.Arrays;

import processing.data.JSONArray;
import processing.data.JSONObject;

/**
 * Data container for a musical scale.
 * 
 * @author James Morrow
 *
 */
public class Scale {
	private String name;
	private String[] noteNames;
	private int[] noteValues;
	
	/**
	 * Constructs a Scale from a JSONObject containing a name, an array of note names, and an array of note values.
	 * @param json The JSONObject
	 */
	public Scale(JSONObject json) {
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
	}
	
	/**
	 * 
	 * @return The number of notes in the scale
	 */
	public int getSize() {
		return noteNames.length;
	}

	/**
	 * 
	 * @return The name of the scale
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @param i The note index
	 * @return The name of the ith note in the scale
	 */
	public String getNoteName(int i) {
		return noteNames[i];
	}
	
	/**
	 * 
	 * @param i The note index
	 * @return The MIDI pitch value of the ith note in the scale
	 */
	public int getNoteValue(int i) {
		return noteValues[i];
	}

	@Override
	public String toString() {
		return "Scale [name=" + name + ", noteNames=" + Arrays.toString(noteNames) + ", noteValues="
				+ Arrays.toString(noteValues) + "]";
	}
}
