package phases;

import java.lang.reflect.Field;
import java.util.ArrayList;

import geom.Rect;
import icons.CameraIcon;
import icons.ColorSchemeIcon;
import icons.DefaultIcon;
import icons.Icon;
import icons.InstrumentIcon;
import icons.NoteSetOneIcon;
import icons.NoteSetTwoIcon;
import icons.PlotPitchIcon;
import icons.ScoreModeIcon;
import icons.ShowActiveNoteIcon;
import icons.SineWaveIcon;
import icons.SuperimposedOrSeparatedIcon;
import icons.TransformIcon;
import icons.ViewTypeIcon;
import processing.core.PApplet;
import soundcipher.SCScorePlus;
import views.LiveScorer;
import views.Musician;
import views.PhaseShifter;
import views.View;
import views.ViewVariableInfo;

/**
 * The screen that displays views, ways of visualizing the music.
 * 
 * @author James Morrow
 *
 */
public class Presenter extends Screen implements ViewVariableInfo {
	// real time
	private int prev_t; //milliseconds
	private final float minutesPerMillisecond = 1f / 60000f;
	
	// unlock sequences (in terms of minutes to unlock thing 1, minutes to unlock thing 2, etc.)
	private float minutesSpentWithMusician = 60f, minutesSpentWithPhaseShifter=60f, minutesSpentWithLiveScorer=60f;
	private final float[] musicianUnlockSeq = {0.5f, 1, 4};
	private final float[] phaseShifterUnlockSeq = {1, 3, 5, 8, 11, 14, 17, 20, 25, 30};
	private final float[] liveScorerUnlockSeq = {4, 8};
	private int nextMusicianUnlockIndex, nextPhaseShifterUnlockIndex, nextLiveScorerUnlockIndex;
	
	// musical time
	private float prev_notept1, prev_notept2;

	// playback
	private SCScorePlus player1 = new SCScorePlus();
	private SCScorePlus player2 = new SCScorePlus();
	private boolean playing;
	private int sign;

	// to smooth animation
	private float accountBalance1 = 0, accountBalance2 = 0;
	private float totalNotept1, totalNotept2;
	private float avg_dNotept1, avg_dNotept2;
	private int dataPts = 0;
	private float acceptableAccountSize = 0.05f;

	// views
	private Musician musicianView;
	private PhaseShifter phaseShifterView;
	private LiveScorer liveScorerView;
	private View view;

	// icons
	private int activeIconIndex = 0;
	private ArrayList<Icon[]> iconLists = new ArrayList<Icon[]>();
	private ArrayList<ModInt> variables = new ArrayList<ModInt>();
	private Icon[] viewTypeIcons;
	private ModInt viewType = new ModInt(0, numViewTypes, viewTypeName);
	
	// icon parameters
	private float iconRadius = 25;
	private float icon_dx = iconRadius * 2.25f;
	private float iconStartX = 0.25f * iconRadius;
	private float iconStartY = pa.height - 2.25f * iconRadius;

	/**
	 * 
	 * @param pa The PhasesPApplet on which to draw views
	 * 
	 */
	public Presenter(PhasesPApplet pa) {
		super(pa);
	}
	
	/*************************************
	 ***** Enter/Exit Event Handling *****
	 *************************************/
	
	@Override
	public void onEnter() {
		musicianView = new Musician(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		phaseShifterView = new PhaseShifter(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		liveScorerView = new LiveScorer(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		switch(viewType.toInt()) {
			case MUSICIAN : view = musicianView; break;
			case PHASE_SHIFTER : view = phaseShifterView; break;
			case LIVE_SCORER : view = liveScorerView; break;
		}

		pa.phrase.addToScore(player1, 0, 0, 0);
		pa.phrase.addToScore(player2, 0, 0, 0);
		player1.tempo(pa.getBPM1());
		player2.tempo(pa.getBPM2());
		player1.repeat(-1);
		player2.repeat(-1);
		player1.play();
		player2.play();

		playing = true;

		if (pa.getBPM1() < pa.getBPM2()) {
			sign = 1;
		} else if (pa.getBPM1() > pa.getBPM2()) {
			sign = -1;
		} else {
			sign = 0;
		}

		prev_notept1 = 0;
		prev_notept2 = 0;

		totalNotept1 = 0;
		totalNotept2 = 0;
		avg_dNotept1 = 0;
		avg_dNotept2 = 0;
		dataPts = 0;

		setupIconLists();
		
		activeIconIndex = 0;

		view.onEnter();
	}
	
	@Override
	public void onExit() {
		player1.stop();
		player2.stop();
	}
	
	/******************************
	 ***** Drawing and Update *****
	 ******************************/

	@Override
	public void draw() {
		updateTime();
		checkUnlocks();
		pa.background(255);
		animateView();
		drawNavigationMenu();
		
	}
	
	/**
	 * Updates variables related to tracking the amount of time the program has been executing under certain conditions.
	 */
	private void updateTime() {
		int t = pa.millis();
		int dt = t - prev_t;
		prev_t = t;
		switch (viewType.toInt()) {
			case MUSICIAN : minutesSpentWithMusician += dt * minutesPerMillisecond; break;
			case PHASE_SHIFTER : minutesSpentWithPhaseShifter += dt * minutesPerMillisecond; break;
			case LIVE_SCORER : minutesSpentWithLiveScorer += dt * minutesPerMillisecond; break;
		}
	}

	/**
	 * Draws the icon navigation menu.
	 */
	private void drawNavigationMenu() {
		if (iconLists.size() > 0) {
			float x = iconStartX;
			float y = iconStartY;
			
			//draw icons
			for (int i = 0; i < iconLists.size(); i++) {
				int j = variables.get(i).toInt();
				Icon[] iconList = iconLists.get(i);
				Icon icon = iconList[j];
				icon.draw(x + iconRadius, y + iconRadius, iconRadius, pa);
				x += icon_dx;
			}
			
			// draw box around active icon
			pa.noFill();
			pa.strokeWeight(3);
			pa.stroke(pa.getColor1());
			pa.rectMode(pa.CORNER);
			pa.rect(iconStartX + activeIconIndex * icon_dx, iconStartY, 2*iconRadius, 2*iconRadius);
		}	
	}
	
	/**
	 * If the given (x,y) position lies within the area in which an icon is being displayed, the index of that icon is returned.
	 * If the given (x,y) position does not lie within the area in which an icon is being displayed, -1 is returned.
	 * 
	 * @param x The x-coordinate in pixels.
	 * @param y The y-coordinate in pixels.
	 * @return
	 */
	private int iconIndexTouches(int x, int y) {
		float iconEndX = iconStartX + icon_dx*iconLists.size() + 2*iconRadius;
		if (iconStartX-iconRadius <= x && x <= iconEndX && iconStartY <= y && y <= iconStartY + 2*iconRadius) {
			return (int)((x - iconStartX) / icon_dx);
		}
		else {
			return -1;
		}
	}

	/**
	 * 
	 * @return The position of player1 along the duration of the phrase it is playing.
	 */
	private float computeNotept1() {
		return PApplet.map(player1.getTickPosition(), 0, player1.getTickLength(), 0, pa.phrase.getTotalDuration());
	}

	/**
	 * 
	 * @return The position of player2 along the duration of the phrase it is playing.
	 */
	private float computeNotept2() {
		return PApplet.map(player2.getTickPosition(), 0, player2.getTickLength(), 0, pa.phrase.getTotalDuration());
	}

	/**
	 * First, it computes information relating to musical timing, such as the duration a player has traveled since last frame.
	 * Then it sends that information to the active view and tells the view to update and draw itself.
	 */
	private void animateView() {
		float notept1 = computeNotept1();
		float notept2 = computeNotept2();

		float dNotept1 = notept1 - prev_notept1;
		float dNotept2 = notept2 - prev_notept2;

		if (dNotept1 < 0) {
			dNotept1 += pa.phrase.getTotalDuration();
		}

		if (dNotept2 < 0) {
			dNotept2 += pa.phrase.getTotalDuration();
		}

		// smoothing:
		totalNotept1 += dNotept1;
		totalNotept2 += dNotept2;
		dataPts++;
		avg_dNotept1 = totalNotept1 / dataPts;
		avg_dNotept2 = totalNotept2 / dataPts;
		accountBalance1 += (dNotept1 - avg_dNotept1);
		accountBalance2 += (dNotept2 - avg_dNotept2);
		dNotept1 = avg_dNotept1;
		dNotept2 = avg_dNotept2;
		if (pa.abs(accountBalance1) > acceptableAccountSize) {
			accountBalance1 = 0;
			dNotept1 += accountBalance1;
		}
		if (pa.abs(accountBalance2) > acceptableAccountSize) {
			accountBalance2 = 0;
			dNotept2 += accountBalance2;
		}

		prev_notept1 = notept1;
		prev_notept2 = notept2;

		view.update(dNotept1, dNotept2, sign);
		if (view != phaseShifterView) {
			phaseShifterView.updateNormalTransforms(dNotept1, dNotept2);
		}
	}
	
	/********************************
	 ***** Input Event Handling *****
	 ********************************/

	@Override
	public void keyPressed() {
		if (pa.key == pa.CODED) {
			if (pa.keyCode == pa.LEFT) {
				activeIconIndex = PhasesPApplet.remainder(activeIconIndex - 1, iconLists.size());
			} else if (pa.keyCode == pa.RIGHT) {
				activeIconIndex = (activeIconIndex + 1) % iconLists.size();
			} else if (pa.keyCode == pa.UP) {
				ModInt activeVar = variables.get(activeIconIndex);
				int availability = this.getIconAvailability(activeVar.getName());
				int newValue = PhasesPApplet.remainder(activeVar.toInt() - 1, availability);
				activeVar.setValue(newValue);
				view.updateState();
			} else if (pa.keyCode == pa.DOWN) {
				ModInt activeVar = variables.get(activeIconIndex);
				int availability = this.getIconAvailability(activeVar.getName());
				int newValue = PhasesPApplet.remainder(activeVar.toInt() + 1, availability);
				activeVar.setValue(newValue);
				view.updateState();
			}

			View newView = null;
			switch (viewType.toInt()) {
			case MUSICIAN:
				newView = musicianView;
				break;
			case PHASE_SHIFTER:
				newView = phaseShifterView;
				break;
			case LIVE_SCORER:
				newView = liveScorerView;
				break;
			}
			if (view != newView) {
				view = newView;
				view.recalibrate(computeNotept1(), computeNotept2());
				setupIconLists();
				view.onEnter();
			}
		}
	}

	@Override
	public void mousePressed() {
		int iconIndex = iconIndexTouches(pa.mouseX, pa.mouseY);
		if (iconIndex != -1) {
			activeIconIndex = iconIndex;
		}
	}
	
	/*******************************
	 ***** Icon Initialization *****
	 *******************************/
	
	/**
	 * First, it checks if new icons have been unlocked for the active view type.
	 * If so, it calls setupIconLists(), which refreshes the state of the icon lists.
	 */
	private void checkUnlocks() {
		if (viewType.toInt() == MUSICIAN && 
				nextMusicianUnlockIndex < musicianUnlockSeq.length &&
				musicianUnlockSeq[nextMusicianUnlockIndex] <= minutesSpentWithMusician) {
			setupIconLists();
			nextMusicianUnlockIndex++;
		}
		else if (viewType.toInt() == PHASE_SHIFTER &&
				nextPhaseShifterUnlockIndex < phaseShifterUnlockSeq.length &&
				phaseShifterUnlockSeq[nextPhaseShifterUnlockIndex] <= minutesSpentWithPhaseShifter) {
			setupIconLists();
			nextPhaseShifterUnlockIndex++;
		}
		else if (viewType.toInt() == LIVE_SCORER && 
			nextLiveScorerUnlockIndex < liveScorerUnlockSeq.length &&
			liveScorerUnlockSeq[nextLiveScorerUnlockIndex] <= minutesSpentWithLiveScorer) {
			setupIconLists();
			nextLiveScorerUnlockIndex++;
		}
		
	}
	
	/**
	 * Adds all the unlocked view type icons to the container called "iconLists", which stores all the icons that are currently available to the player.
	 */
	private void setupViewTypeIcons() {
		int availability = getIconAvailability(viewTypeName);
		if (availability > 0) {
			viewTypeIcons = new Icon[availability];
			for (int i = 0; i < availability; i++) {
				viewTypeIcons[i] = new ViewTypeIcon(i);
			}
			iconLists.add(viewTypeIcons);
			variables.add(viewType);
		}
	}

	/**
	 * Adds all the unlocked icons relating to the current view type + all the unlocked view type icons.
	 */
	private void setupIconLists() {
		iconLists.clear();
		variables.clear();
		setupViewTypeIcons();
		Field[] fields = view.getClass().getDeclaredFields();
		try {
			for (Field f : fields) {
				if (f.getType().equals(ModInt.class)) {
					ModInt x = (ModInt) f.get(view);
					String name = x.getName();
					Icon[] iconList = null;
					int availability = getIconAvailability(name);
					if (availability > 0) {
						switch (name) {
						case activeNoteModeName:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new ShowActiveNoteIcon(i);
							}
							break;
						case transformationName:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new TransformIcon(i);
							}
							break;
						case cameraModeName:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new CameraIcon(i);
							}
							break;
						case noteGraphicSet1Name:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new NoteSetOneIcon(i);
							}
							break;
						case noteGraphicSet2Name:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new NoteSetTwoIcon(i);
							}
							break;
						case plotPitchModeName:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new PlotPitchIcon(i);
							}
							break;
						case colorSchemeName:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new ColorSchemeIcon(i);
							}
							break;
						case superimposedOrSeparatedName:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new SuperimposedOrSeparatedIcon(i);
							}
							break;
						case instrumentName:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new InstrumentIcon(i);
							}
							break;
						case scoreModeName:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new ScoreModeIcon(i);
							}
							break;
						case sineWaveName:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new SineWaveIcon(i, pa);
							}
							break;
						default:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new DefaultIcon(i);
							}
							System.out.println("Don't know that view variable: " + name);
							break;
						}
						iconLists.add(iconList);
						variables.add(x);
					}
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param name The String identifier for a type of icon related to the current view type. All of these kinds of names are given in views.ViewVariableInfo.java.
	 * @return The number of icons that are available (unlocked) of the icon type associated with the given identifier.
	 */
	private int getIconAvailability(String name) {
		switch (name) {
			case viewTypeName:
				if (minutesSpentWithMusician + minutesSpentWithPhaseShifter > 25f) {
					return 3;
				}
				else if (minutesSpentWithMusician > 2f) {
					return 2;
				}
				else {
					return 0;
				}
			default : 
				switch (viewType.toInt()) {
					case MUSICIAN: return getMusicianIconAvailability(name);
					case PHASE_SHIFTER: return getPhaseShifterIconAvailability(name);
					case LIVE_SCORER: return getLiveScorerIconAvailability(name);
					default: return 0;
				}
		}	
	}
	
	/**
	 * 
	 * @param name The String identifier for a type of icon related to the view type called Musician. All of these kinds of names are given in views.ViewVariableInfo.java.
	 * @return The number of icons that are available (unlocked) of the icon type associated with the given identifier.
	 */
	private int getMusicianIconAvailability(String name) {
		switch (name) {
			case colorSchemeName:
				if (minutesSpentWithMusician > musicianUnlockSeq[0]) {
					return 2;
				}
				else {
					return 0;
				}
			case superimposedOrSeparatedName:
				if (minutesSpentWithMusician > musicianUnlockSeq[1]) {
					return 2;
				}
				else {
					return 0;
				}
			case instrumentName:
				if (minutesSpentWithMusician > musicianUnlockSeq[2]) {
					return 2;
				}
				else {
					return 0;
				}
			default: 
				return 0;
		}
	}
	
	/**
	 * 
	 * @param name The String identifier for a type of icon related to the view type called PhaseShifter. All of these kinds of names are given in views.ViewVariableInfo.java.
	 * @return The number of icons that are available (unlocked) of the icon type associated with the given identifier.
	 */
	private int getPhaseShifterIconAvailability(String name) {
		switch (name) {
			case colorSchemeName: 
				return 2;
			case activeNoteModeName:
				if (minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[3]) {
					return 3;
				}
				else if (minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[0]) {
					return 2;
				}
				else {
					return 0;
				}
			case noteGraphicSet1Name:
				if (minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[8]) {
					return 5;
				}
				else if (minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[6]) {
					return 4;
				}
				else if (minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[2]) {
					return 3;
				}
				else if (minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[1]) {
					return 2;
				}
				else {
					return 0;
				}
			case cameraModeName:
				if (minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[9]) {
					return 3;
				}
				if (minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[4]) {
					return 2;
				}
				else {
					return 0;
				}
			case plotPitchModeName:
				if (minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[5]) {
					return 2;
				}
				else {
					return 0;
				}
			case transformationName:
				if (minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[7]) {
					return 2;
				}
				else {
					return 0;
				}
			default:
				return 0;
		}
	}
	
	/**
	 * 
	 * @param name The String identifier for a type of icon related to the view type called LiveScorer. All of these kinds of names are given in views.ViewVariableInfo.java.
	 * @return The number of icons that are available (unlocked) of the icon type associated with the given identifier.
	 */
	private int getLiveScorerIconAvailability(String name) {
		switch (name) {
			case colorSchemeName:
				return numColorSchemes;
			case scoreModeName:
				return numScoreModes;
			case noteGraphicSet2Name:
				if (minutesSpentWithLiveScorer > liveScorerUnlockSeq[0]) {
					return 2;
				}
				else {
					return 0;
				}
			case sineWaveName:
				if (minutesSpentWithLiveScorer > liveScorerUnlockSeq[1]) {
					return 2;
				}
				else {
					return 0;
				}
			default:
				return 0;
		}
	}
}