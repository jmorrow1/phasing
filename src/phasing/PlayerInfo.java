package phasing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import processing.data.JSONArray;
import processing.data.JSONObject;

/**
 * A data container of information related to the player. More specifically:
 * --> data that determines what the player has and hasn't unlocked.
 * --> the index of the phrase the current phrase derives from.
 * --> the last saved bpm1 value
 * --> the last saved phase difference value
 * 
 * @author James Morrow
 *
 */
public class PlayerInfo {
	public float minutesSpentWithMusician, minutesSpentWithPhaseShifter, minutesSpentWithLiveScorer;
	public int nextMusicianUnlockIndex, nextPhaseShifterUnlockIndex, nextLiveScorerUnlockIndex;
	public float minutesSpentWithEditor;
	public int nextEditorUnlockIndex;

	public float bpm1 = PhasesPApplet.DEFAULT_BPM_1;
	public float bpmDifference = PhasesPApplet.DEFAULT_BPM_DIFFERENCE;
	
	public PlayerInfo(boolean everythingUnlocked) {
		this((everythingUnlocked) ? 10000f : 0, (everythingUnlocked) ? 10000f : 0, (everythingUnlocked) ? 10000f : 0,
				(everythingUnlocked) ? 1000 : 0, (everythingUnlocked) ? 1000 : 0, (everythingUnlocked) ? 1000 : 0,
				(everythingUnlocked) ? 1000 : 0, (everythingUnlocked) ? 1000 : 0);
	}
	
	public PlayerInfo(float minutesSpentWithMusician, float minutesSpentWithPhaseShifter, float minutesSpentWithLiveScorer,
			int nextMusicianUnlockIndex, int nextPhaseShifterUnlockIndex, int nextLiveScorerUnlockIndex,
			float minutesSpentWithEditor, int nextEditorUnlockIndex) {
		this.minutesSpentWithMusician = minutesSpentWithMusician;
		this.minutesSpentWithPhaseShifter = minutesSpentWithPhaseShifter;
		this.minutesSpentWithLiveScorer = minutesSpentWithLiveScorer;
		this.nextMusicianUnlockIndex = nextMusicianUnlockIndex;
		this.nextPhaseShifterUnlockIndex = nextPhaseShifterUnlockIndex;
		this.nextLiveScorerUnlockIndex = nextLiveScorerUnlockIndex;
		this.minutesSpentWithEditor = minutesSpentWithEditor;
		this.nextEditorUnlockIndex = nextEditorUnlockIndex;
	}
	
	public PlayerInfo(JSONObject json) {
		this(json.getFloat("minutesSpentWithMusician", 0),
		     json.getFloat("minutesSpentWithPhaseShifter", 0),
		     json.getFloat("minutesSpentWithLiveScorer", 0),
		     json.getInt("nextMusicianUnlockIndex", 0),
		     json.getInt("nextPhaseShifterUnlockIndex", 0),
		     json.getInt("nextLiveScorerUnlockIndex", 0),
		     json.getFloat("minutesSpentWithEditor", 0),
		     json.getInt("nextEditorUnlockIndex", 0));
		
		bpm1 = json.getFloat("bpm1", PhasesPApplet.DEFAULT_BPM_1);
		bpmDifference = json.getFloat("phaseDifference", PhasesPApplet.DEFAULT_BPM_DIFFERENCE);
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.setFloat("minutesSpentWithMusician", minutesSpentWithMusician);
		json.setFloat("minutesSpentWithPhaseShifter", minutesSpentWithPhaseShifter);
		json.setFloat("minutesSpentWithLiveScorer", minutesSpentWithLiveScorer);
		json.setInt("nextMusicianUnlockIndex", nextMusicianUnlockIndex);
		json.setInt("nextPhaseShifterUnlockIndex", nextPhaseShifterUnlockIndex);
		json.setInt("nextLiveScorerUnlockIndex", nextLiveScorerUnlockIndex);
		json.setFloat("minutesSpentWithEditor", minutesSpentWithEditor);
		json.setInt("nextEditorUnlockIndex", nextEditorUnlockIndex);

		json.setFloat("bpm1", bpm1);
		json.setFloat("phaseDifference", bpmDifference);
		
		return json;
	}
}