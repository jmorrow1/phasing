package phases;

import processing.data.JSONObject;

public class PlayerInfo {
	public float minutesSpentWithMusician, minutesSpentWithPhaseShifter, minutesSpentWithLiveScorer;
	public int nextMusicianUnlockIndex, nextPhaseShifterUnlockIndex, nextLiveScorerUnlockIndex;
	//TODO: Save the state of view variables and icons (could do this by saving the names of them)
	
	public PlayerInfo(boolean everythingUnlocked) {
		this((everythingUnlocked) ? 10000f : 0, (everythingUnlocked) ? 10000f : 0, (everythingUnlocked) ? 10000f : 0,
				(everythingUnlocked) ? 1000 : 0, (everythingUnlocked) ? 1000 : 0, (everythingUnlocked) ? 1000 : 0);
	}
	
	public PlayerInfo(float minutesSpentWithMusician, float minutesSpentWithPhaseShifter, float minutesSpentWithLiveScorer, int nextMusicianUnlockIndex,
			int nextPhaseShifterUnlockIndex, int nextLiveScorerUnlockIndex) {
		this.minutesSpentWithMusician = minutesSpentWithMusician;
		this.minutesSpentWithPhaseShifter = minutesSpentWithPhaseShifter;
		this.minutesSpentWithLiveScorer = minutesSpentWithLiveScorer;
		this.nextMusicianUnlockIndex = nextMusicianUnlockIndex;
		this.nextPhaseShifterUnlockIndex = nextPhaseShifterUnlockIndex;
		this.nextLiveScorerUnlockIndex = nextLiveScorerUnlockIndex;
	}
	
	public PlayerInfo(JSONObject json) {
		this(json.getFloat("minutesSpentWithMusician", 0),
		     json.getFloat("minutesSpentWithPhaseShifter", 0),
		     json.getFloat("minutesSpentWithLiveScorer", 0),
		     json.getInt("nextMusicianUnlockIndex", 0),
		     json.getInt("nextPhaseShifterUnlockIndex", 0),
		     json.getInt("nextLiveScorerUnlockIndex", 0));
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.setFloat("minutesSpentWithMusician", minutesSpentWithMusician);
		json.setFloat("minutesSpentWithPhaseShifter", minutesSpentWithPhaseShifter);
		json.setFloat("minutesSpentWithLiveScorer", minutesSpentWithLiveScorer);
		json.setInt("nextMusicianUnlockIndex", nextMusicianUnlockIndex);
		json.setInt("nextPhaseShifterUnlockIndex", nextPhaseShifterUnlockIndex);
		json.setInt("nextLiveScorerUnlockIndex", nextLiveScorerUnlockIndex);
		return json;
	}
}