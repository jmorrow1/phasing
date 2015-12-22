package phases;

import java.util.Arrays;

import processing.data.JSONArray;
import processing.data.JSONObject;

public class Scale {
	private String name;
	private String[] noteNames;
	private int[] noteValues;
	
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

	public String getName() {
		return name;
	}

	public String[] getNoteNames() {
		return noteNames;
	}

	public int[] getNoteValues() {
		return noteValues;
	}

	@Override
	public String toString() {
		return "Scale [name=" + name + ", noteNames=" + Arrays.toString(noteNames) + ", noteValues="
				+ Arrays.toString(noteValues) + "]";
	}
}
