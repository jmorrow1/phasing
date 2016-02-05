package phases;

import java.util.Arrays;

import processing.data.JSONArray;
import processing.data.JSONObject;

public class ScaleSet {
	private String name;
	private Scale[] scales;
	
	public ScaleSet(JSONObject json) {
		this.name = json.getString("name");
		JSONArray jscales = json.getJSONArray("scales");
		scales = new Scale[jscales.size()];
		for (int i=0; i<jscales.size(); i++) {
			scales[i] = new Scale(jscales.getJSONObject(i), name);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public int numScales() {
		return scales.length;
	}
	
	public Scale getScale(int i) {
		if (0 <= i && i < scales.length) {
			return scales[i];
		}
		return null;
	}
	
	public String getScaleName(int i) {
		if (0 <= i && i < scales.length) {
			return scales[i].getName();
		}
		return null;
	}

	@Override
	public String toString() {
		return name;
	}
}
