package phasing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import processing.data.JSONArray;
import processing.data.JSONObject;

/**
 * A data container of information related to the player. More specifically:
 * --> data that determines what the player has and hasn't unlocked.
 * --> how many times the player has visited the editor.
 * --> the index of the phrase the current phrase derives from.
 * --> the last saved state of view options
 * --> the last saved window size
 * 
 * @author James Morrow
 *
 */
public class PlayerInfo {
	public float minutesSpentWithMusician, minutesSpentWithPhaseShifter, minutesSpentWithLiveScorer;
	public int nextMusicianUnlockIndex, nextPhaseShifterUnlockIndex, nextLiveScorerUnlockIndex;
	public int numEditorVisits;
	public HashMap<String, Integer> viewOptionValueMap = new HashMap<String, Integer>();
	
	private boolean windowSizeInitialized = false;
	private int windowWidth, windowHeight;
	
	public PlayerInfo(boolean everythingUnlocked) {
		this((everythingUnlocked) ? 10000f : 0, (everythingUnlocked) ? 10000f : 0, (everythingUnlocked) ? 10000f : 0,
				(everythingUnlocked) ? 1000 : 0, (everythingUnlocked) ? 1000 : 0, (everythingUnlocked) ? 1000 : 0,
				(everythingUnlocked) ? 1000 : 0);
	}
	
	public PlayerInfo(float minutesSpentWithMusician, float minutesSpentWithPhaseShifter, float minutesSpentWithLiveScorer,
			int nextMusicianUnlockIndex, int nextPhaseShifterUnlockIndex, int nextLiveScorerUnlockIndex,
			int numEditorVisits) {
		this.minutesSpentWithMusician = minutesSpentWithMusician;
		this.minutesSpentWithPhaseShifter = minutesSpentWithPhaseShifter;
		this.minutesSpentWithLiveScorer = minutesSpentWithLiveScorer;
		this.nextMusicianUnlockIndex = nextMusicianUnlockIndex;
		this.nextPhaseShifterUnlockIndex = nextPhaseShifterUnlockIndex;
		this.nextLiveScorerUnlockIndex = nextLiveScorerUnlockIndex;
		this.numEditorVisits = numEditorVisits;
	}
	
	public PlayerInfo(JSONObject json) {
		this(json.getFloat("minutesSpentWithMusician", 0),
		     json.getFloat("minutesSpentWithPhaseShifter", 0),
		     json.getFloat("minutesSpentWithLiveScorer", 0),
		     json.getInt("nextMusicianUnlockIndex", 0),
		     json.getInt("nextPhaseShifterUnlockIndex", 0),
		     json.getInt("nextLiveScorerUnlockIndex", 0),
		     json.getInt("numEditorVisits", 0));
		
		if (json.hasKey("viewOptionValueMap")) {
			JSONArray optionValuePairs = json.getJSONArray("viewOptionValueMap");
			for (int i=0; i<optionValuePairs.size(); i++) {
				JSONObject pair = optionValuePairs.getJSONObject(i, new JSONObject());
				String optionName = pair.getString("optionName", "");
				Integer optionValue = pair.getInt("optionValue", -1);
				viewOptionValueMap.put(optionName, optionValue);
			}
		}
		
		if (json.hasKey("windowWidth") && json.hasKey("windowHeight")) {
			windowWidth = json.getInt("windowWidth", -1);
			windowHeight = json.getInt("windowHeight", -1);
			if (windowWidth != -1 && windowHeight != -1) {
				windowSizeInitialized = true;
			}
		}
	}
	
	public void setSize(int width, int height) {
		this.windowWidth = width;
		this.windowHeight = height;
		windowSizeInitialized = true;
	}
	
	public boolean isWindowSizeInitialized() {
		return windowSizeInitialized;
	}
	
	public int getWindowWidth() {
		return windowSizeInitialized ? windowWidth : -1;
	}
	
	public int getWindowHeight() {
		return windowSizeInitialized ? windowHeight : -1;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.setFloat("minutesSpentWithMusician", minutesSpentWithMusician);
		json.setFloat("minutesSpentWithPhaseShifter", minutesSpentWithPhaseShifter);
		json.setFloat("minutesSpentWithLiveScorer", minutesSpentWithLiveScorer);
		json.setInt("nextMusicianUnlockIndex", nextMusicianUnlockIndex);
		json.setInt("nextPhaseShifterUnlockIndex", nextPhaseShifterUnlockIndex);
		json.setInt("nextLiveScorerUnlockIndex", nextLiveScorerUnlockIndex);
		json.setInt("numEditorVisits", numEditorVisits);
		
		JSONArray jsonMap = new JSONArray();
		json.setJSONArray("viewOptionValueMap", jsonMap);
		Set<Map.Entry<String, Integer>> entrySet = viewOptionValueMap.entrySet();
		int i=0;
		for (Map.Entry<String, Integer> pair : entrySet) {
			JSONObject jsonPair = new JSONObject();
			jsonPair.setString("optionName", pair.getKey());
			jsonPair.setInt("optionValue", pair.getValue());
			jsonMap.setJSONObject(i, jsonPair);
			i++;
		}
		
		if (windowSizeInitialized && windowWidth > 200 && windowHeight > 200) {
			json.setInt("windowWidth", windowWidth);
			json.setInt("windowHeight", windowHeight);
		}
		
		return json;
	}
}