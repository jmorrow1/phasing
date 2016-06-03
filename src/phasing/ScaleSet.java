package phasing;

import java.util.Arrays;

import processing.data.JSONArray;
import processing.data.JSONObject;

/**
 * A data container for a set of scales. Usually this would be a meaningful class of scales, like, for instance, the class of major scales. 
 * 
 * @author James Morrow
 *
 */
public class ScaleSet {
	private String name;
	private Scale[] scales;
	
	/**
	 * Loads a scale set from a JSONObject.
	 * @param json The JSONObject.
	 */
	public ScaleSet(JSONObject json) {
		this.name = json.getString("name");
		JSONArray jscales = json.getJSONArray("scales");
		scales = new Scale[jscales.size()];
		for (int i=0; i<jscales.size(); i++) {
			scales[i] = new Scale(jscales.getJSONObject(i), name);
		}
	}
	
	/**
	 * Returns the name of the scale class.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return The number of scales in the set.
	 */
	public int numScales() {
		return scales.length;
	}
	
	/**
	 * 
	 * @param i The index.
	 * @return The scale at the given index.
	 */
	public Scale getScale(int i) {
		if (0 <= i && i < scales.length) {
			return scales[i];
		}
		return null;
	}
	
	/**
	 * 
	 * @param i The index.
	 * @return The name of the scale at the given index.
	 */
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
