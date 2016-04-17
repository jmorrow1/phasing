package screens;

import java.lang.reflect.Field;
import java.util.ArrayList;

import controlP5.Button;
import controlP5.ControlP5;
import controlp5.TriangleButtonView;
import geom.Rect;
import icons.CameraIcon;
import icons.ColorSchemeIcon;
import icons.DefaultIcon;
import icons.Icon;
import icons.InstrumentIcon;
import icons.NoteSetOneIcon;
import icons.NoteSetTwoIcon;
import icons.OrientationIcon;
import icons.PlotPitchIcon;
import icons.ScoreModeIcon;
import icons.ShowActiveNoteIcon;
import icons.SineWaveIcon;
import icons.SuperimposedOrSeparatedIcon;
import icons.TransformIcon;
import icons.ViewTypeIcon;
import phasing.PhasesPApplet;
import phasing.PhraseReader;
import phasing.PhraseReader.PhraseReaderListener;
import phasing.PlayerInfo;
import processing.core.PApplet;
import soundcipher.SCScorePlus;
import util.ModInt;
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
public class Presenter extends Screen implements ViewVariableInfo, PhraseReaderListener {
	// real time
	private int dt, prev_t; //milliseconds
	
	// unlock sequences (in terms of minutes to unlock thing 1, minutes to unlock thing 2, etc.)
	private final float[] musicianUnlockSeq = {0f, 0.5f, 6};
	private final float[] phaseShifterUnlockSeq = {1, 3, 5, 8, 11, 14, 17, 20};
	private final float[] liveScorerUnlockSeq = {4, 8, 12};

	// playback
	private SCScorePlus player1 = new SCScorePlus();
	private SCScorePlus player2 = new SCScorePlus();
	private int instrument = 0;
	private boolean playing;

	// musical time
	private final boolean PRINT_OUT_ANIMATION_ACCURACY = false;
	private float prev_notept1, prev_notept2;
	private float accountBalance1 = 0, accountBalance2 = 0;
	private float totalNotept1, totalNotept2;
	private float target_dNotept1, target_dNotept2;
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
	private float iconStartX1 = 0.75f * iconRadius;
	private float iconStartY1() {
		return pa.height - 2.75f * iconRadius;
	}
	
	// controlp5
	private ControlP5 cp5;
	private Button upButton, downButton;
	
	// phrase
	//private Phrase reversedPhrase;
	//private boolean phraseIsReversed;
	
	// phrase reading
	private PhraseReader reader1, reader2;
	public static final int READER_ONE_ID = 1, READER_TWO_ID = 2;

	/**************************
	 ***** Initialization *****
	 **************************/

	/**
	 * 
	 * @param pa The PhasesPApplet on which to draw views
	 * 
	 */
	public Presenter(PhasesPApplet pa) {
		super(pa);
		initPhraseReaders();
		initViews(pa.playerInfo);	
		initCP5Objects();
		cp5.hide();
	}
	
	/**
	 * Initializes reader1 and reader2.
	 */
	private void initPhraseReaders() {
		reader1 = new PhraseReader(pa.currentPhrase, READER_ONE_ID, this);
		reader2 = new PhraseReader(pa.currentPhrase, READER_TWO_ID, this);
	}
	
	/**
	 * Initializes an instance of each view type.
	 * @param playerInfo The PlayerInfo object to initialize each view with.
	 */
	private void initViews(PlayerInfo playerInfo) {
		Rect area = new Rect(viewCenx(), viewCeny(), pa.width, viewHeight(), pa.CENTER);
		musicianView = (musicianView == null) ? new Musician(area, 150, playerInfo, pa)
				                              : new Musician(musicianView, area, 150, pa);
		phaseShifterView = (phaseShifterView == null) ? new PhaseShifter(area, 150, playerInfo, pa)
				                                      : new PhaseShifter(phaseShifterView, area, 150, pa);
		liveScorerView = (liveScorerView == null) ? new LiveScorer(area, 150, playerInfo, pa) :
			                                        new LiveScorer(liveScorerView, area, 150, pa);
		
		switch(viewType.toInt()) {
			case MUSICIAN : view = musicianView; break;
			case PHASE_SHIFTER : view = phaseShifterView; break;
			case LIVE_SCORER : view = liveScorerView; break;
		}
	}
	
	/**
	 * Returns what the center x-coordinate of a view should be.
	 * @return What the center x-coordinate of a view should be.
	 */
	private float viewCenx() {
		return pa.width/2f;
	}
	
	/**
	 * Returns what the center y-coordinate of a view should be.
	 * @return What the center y-coordinate of a view should be.
	 */
	private float viewCeny() {
		return (upButtonY1() + pa.topToolbarY2()) / 2f;
	}
	
	/**
	 * Returns what the height of a view should be.
	 * @return What the height of a view should be.
	 */
	private float viewHeight() {
		return PApplet.max(1, upButtonY1() - pa.topToolbarY2());
	}
	
	/**
	 * Initializes the Presenter's cp5 object and controllers.
	 */
	private void initCP5Objects() {
		if (cp5 != null) {
			cp5.dispose();
		}
		cp5 = new ControlP5(pa);
		cp5.setAutoDraw(false);
		initDirectionalButtons();
	}
	
	/**
	 * Initializes the directional button variables.
	 */
	private void initDirectionalButtons() {
		float buttonX1 = directionalButtonX1(0);
		
		int buttonRadius = (int)(iconRadius/2f);
		upButton = consDirectionalButton(buttonX1, upButtonY1(), buttonRadius, -pa.HALF_PI, "iconUp");
		downButton = consDirectionalButton(buttonX1, downButtonY1(), buttonRadius, pa.HALF_PI, "iconDown");
	}
	
	/**
	 * Constructs a directional button and returns it.
	 * 
	 * @param x1 The leftmost x-coordinate of the button.
	 * @param y1 The uppermost y-coordinate of the button.
	 * @param radius The radius of the button.
	 * @param headAngle The angle (in radians) of the heading.
	 * @param name The name of the button (which ControlP5 will use for callbacks).
	 * @return
	 */
	private Button consDirectionalButton(float x1, float y1, int radius, float headAngle, String name) {
		Button b = cp5.addButton(name)
			          .setPosition(x1, y1)
			          .setSize(2*radius, 2*radius)
			          .setView(new TriangleButtonView(headAngle, 0.75f*pa.TWO_PI))
			          .plugTo(this)
			          ;
		pa.colorControllerHideLabel(b);
		return b;
	}
	
	/***********************************
	 ***** Navigation Menu Control *****
	 ***********************************/
	
	/**
	 * Gives where the x-coordinate of a directional button that's associated with an icon at a given index 
	 * (whether or not there's actually an icon there) would be.
	 * @param index The index of the icon.
	 * @return The x-coordinate of a directional button associated with an icon.
	 */
	private float directionalButtonX1(int index) {
		return iconCenx(index) - iconRadius/2f;
	}
	
	/**
	 * Gives the upper y-coordinate of up-pointing directional buttons.
	 * @return The upper y-coordinate of up-pointing directional buttons.
	 */
	private float upButtonY1() {
		return iconCenY() - iconRadius - 4;
	}
	
	/**
	 * Gives the upper y-coordinate of down-pointing directional buttons.
	 * @return The upper y-coordinate of down-pointing directional buttons.
	 */
	private float downButtonY1() {
		return iconCenY() + iconRadius + 4;
	}
	
	/**
	 * 
	 * @return The center y-coordinate of icons.
	 */
	private float iconCenY() {
		return iconStartY1() + iconRadius/2f;
	}
	
	/**
	 * Repositions the up and down buttons according to the activeIconIndex.
	 */
	private void repositionDirectionalButtons() {
		upButton.setPosition(directionalButtonX1(activeIconIndex), upButtonY1());
		downButton.setPosition(directionalButtonX1(activeIconIndex), downButtonY1());
	}
	
	/**
	 * This method can be triggered by ControlP5 (as a callback) and by the presenter itself.
	 * 
	 * Moves the activeVar up, if it can.
	 * Changes the icon being displayed at the activeIconIndex accordingly.
	 * Checks to see if the view type needs to be changes and changes it if it does.
	 */
	public void iconUp() {
		ModInt activeVar = variables.get(activeIconIndex);
		int availability = this.getIconAvailability(activeVar.getName());
		int newValue = PhasesPApplet.remainder(activeVar.toInt() - 1, availability);
		activeVar.setValue(newValue);
		view.settingsChanged();
		checkForInstrumentChange();
		//checkPhraseOrientation();
		checkViewType();
	}
	
	/**
	 * This method can be triggered by ControlP5 (as a callback) and by the Presenter itself.
	 * 
	 * Moves the activeVar down, if it can.
	 * Changes the icon being displayed at the activeIconIndex accordingly.
	 * Checks to see if the view type needs to be changes and changes it if it does.
	 */
	public void iconDown() {
		ModInt activeVar = variables.get(activeIconIndex);
		int availability = this.getIconAvailability(activeVar.getName());
		int newValue = PhasesPApplet.remainder(activeVar.toInt() + 1, availability);
		activeVar.setValue(newValue);
		view.settingsChanged();
		checkForInstrumentChange();
		//checkPhraseOrientation();
		checkViewType();
	}
	
	/**
	 * Moves the activeIconIndex right.
	 * Messages the directional buttons to reposition themselves according to the new activeIconIndex.
	 */
	public void iconRight() {
		activeIconIndex = (activeIconIndex + 1) % iconLists.size();
		repositionDirectionalButtons();
	}
	
	/**
	 * Moves the activeIconIndex left.
	 * Messages the directional buttons to reposition themselves according to the new activeIconIndex.
	 */
	public void iconLeft() {
		activeIconIndex = PhasesPApplet.remainder(activeIconIndex - 1, iconLists.size());
		repositionDirectionalButtons();
	}
	
	/**
	 * Checks if the view should change type.
	 * If it should, the method changes the view type accordingly.
	 */
	public void checkViewType() {
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
			view.wakeUp(computeNotept1(), computeNotept2());
			setupIconLists();
		}
	}
	
	/**
	 * Checks if the current phrase should change orientation.
	 * If it should, the method changes it.
	 */
	/*private void checkPhraseOrientation() {
		if (phraseOrientationShouldChange()) {
			phraseIsReversed = !phraseIsReversed;
			musicianView.wakeUp(0, 0);
			liveScorerView.wakeUp(0, 0);
			phaseShifterView.wakeUp(0, 0);
			setupPlayback();
		}
	}*/
	
	/**
	 * Tells whether or not the current phrase should change orientation.
	 * @return True, if it should change, false otherwise.
	 */
	/*private boolean phraseOrientationShouldChange() {
		return (phraseIsReversed && liveScorerView.orientationMode.toInt() == NON_REVERSED) ||
				(!phraseIsReversed && liveScorerView.orientationMode.toInt() == REVERSED);
	}*/
	
	
	/**
	 * Checks if the instrument sound should change.
	 * If it should, the method changes it.
	 */
	public void checkForInstrumentChange() {
		if (instrumentShouldChange()) {
			changeInstrument();
			musicianView.wakeUp(0, 0);
			liveScorerView.wakeUp(0, 0);
			phaseShifterView.wakeUp(0, 0);
			setupPlayback();
		}
	}
	
	/**
	 * Changes the instrument variable to the appropriate up-to-date value.
	 */
	private void changeInstrument() {
		switch (musicianView.instrument.toInt()) {
			case PIANO : instrument = 0; break;
			case MARIMBA : instrument = 13; break;
		}
	}
	
	/**
	 * Tells whether or not the instrument sound should change.
	 * @return True, if it should change, false otherwise.
	 */
	private boolean instrumentShouldChange() {
		return (instrument == 13 && musicianView.instrument.toInt() != MARIMBA) ||
				(instrument == 0 && musicianView.instrument.toInt() != PIANO);
	}
	
	/*********************************
	 ***** Screen Event Handling *****
	 *********************************/
	
	@Override
	public void windowResized() {
		musicianView.setSize(viewCenx(), viewCeny(), pa.width, viewHeight());
		phaseShifterView.setSize(viewCenx(), viewCeny(), pa.width, viewHeight());
		liveScorerView.setSize(viewCenx(), viewCeny(), pa.width, viewHeight());
		initCP5Objects();
		repositionDirectionalButtons();
	}
	
	@Override
	public void onEnter() {
		//reversedPhrase = Phrase.reverse(pa.currentPhrase);
		initCP5Objects();
		initViews(pa.playerInfo);
		initPhraseReaders();
		setupIconLists();
		activeIconIndex = 0;
		repositionDirectionalButtons();
		cp5.show();	
		
		checkForInstrumentChange();
		if (pa.playerInfo.nextMusicianUnlockIndex != 0) {
			upButton.show();
			downButton.show();
		}
		else {
			upButton.hide();
			downButton.hide();
		}
		setupPlayback();
	}
	
	/**
	 * Sets up music playback.
	 */
	private void setupPlayback() {
		prev_notept1 = 0;
		prev_notept2 = 0;
		totalNotept1 = 0;
		totalNotept2 = 0;
		accountBalance1 = 0;
		accountBalance2 = 0;
		dataPts = 0;
		
		pa.currentPhrase.addToScore(player1, 0, 0, instrument);
		//if (liveScorerView.orientationMode.toInt() == NON_REVERSED) {
			pa.currentPhrase.addToScore(player2, 0, 0, instrument);
		//}
		//else {
		//	reversedPhrase.addToScore(player2, 0, 0, instrument);
		//}
			
		reader1.wakeUp(0);
		reader2.wakeUp(0);

		player1.tempo(pa.getBPM1());
		player2.tempo(pa.getBPM2());
		player1.repeat(-1);
		player2.repeat(-1);
		player1.play();
		player2.play();
		
		playing = true;
	}
	
	@Override
	public void onExit() {
		player1.stop();
		player2.stop();
		cp5.hide();
		
	}
	
	@Override
	public void onPause() {
		//NOT IMPLEMENTED
	}
	
	public void onResume() {
		//NOT IMPLEMENTED
	}
	
	/******************************
	 ***** Drawing and Update *****
	 ******************************/
	
	@Override
	public void drawWhilePaused() {
		int t = pa.millis();
		dt = t - prev_t;
		prev_t = t;
	}

	@Override
	public void draw() {
		updateTime();
		checkUnlocks();
		pa.background(255);
		pa.drawControlP5();
		cp5.draw();
		animateView();
		drawNavigationMenu();
	}
	
	/**
	 * Updates variables related to tracking the amount of time the program has been executing under certain conditions.
	 */
	private void updateTime() {
		int t = pa.millis();
		dt = t - prev_t;
		prev_t = t;
		float changeInMinutes = PhasesPApplet.millisToMinutes(dt);
		switch (viewType.toInt()) {
			case MUSICIAN : pa.playerInfo.minutesSpentWithMusician += changeInMinutes; break;
			case PHASE_SHIFTER : pa.playerInfo.minutesSpentWithPhaseShifter += changeInMinutes; break;
			case LIVE_SCORER : pa.playerInfo.minutesSpentWithLiveScorer += changeInMinutes; break;
		}
	}

	/**
	 * Draws the icon navigation menu.
	 */
	private void drawNavigationMenu() {
		if (iconLists.size() > 0) {
			float iconCeny = iconStartY1() + iconRadius;
			
			//draw icons
			for (int i = 0; i < iconLists.size(); i++) {
				int j = variables.get(i).toInt();
				Icon[] iconList = iconLists.get(i);
				Icon icon = iconList[j];
				icon.draw(iconCenx(i), iconCeny, iconRadius, pa);
			}
			
			// draw box around active icon
			pa.noFill();
			pa.strokeWeight(3);
			pa.stroke(pa.getColor1());
			pa.rectMode(pa.CORNER);
			pa.rect(iconStartX1 + activeIconIndex * icon_dx, iconStartY1(), 2*iconRadius, 2*iconRadius);
		}	
	}
	
	/**
	 * Gives the center x-coordinate of the icon at the given index (whether or not an icon actually exists there).
	 * @param index 
	 * @returnThe center x-coordinate of the icon.
	 */
	private float iconCenx(int index) {
		return iconStartX1 + index * icon_dx + iconRadius;
	}
	
	/**
	 * If the given (x,y) position lies within the area in which an icon is being displayed, the index of that icon is returned.
	 * If the given (x,y) position does not lie within the area in which an icon is being displayed, -1 is returned.
	 * 
	 * The hit box is actually a bit taller than the space the icon takes up, which is on purpose.
	 * 
	 * @param x The x-coordinate in pixels.
	 * @param y The y-coordinate in pixels.
	 * @return The index of the icon that the given (x,y) touches.
	 */
	private int iconIndexTouches(int x, int y) {
		float iconEndX = iconStartX1 + icon_dx*(iconLists.size()-1) + 2*iconRadius;
		float iconStartY1 = iconStartY1();
		if (iconStartX1-iconRadius <= x && x < iconEndX && iconStartY1 - iconRadius <= y && y <= iconStartY1 + 3f*iconRadius) {
			int result = (int)((x - iconStartX1) / icon_dx);
			return PApplet.constrain(result, 0, iconLists.size()-1);
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
		if (pa.currentPhrase.getTotalDuration() == 0) return 0;
		return PApplet.map(player1.getTickPosition(), 0, player1.getTickLength(), 0, pa.currentPhrase.getTotalDuration());
	}

	/**
	 * 
	 * @return The position of player2 along the duration of the phrase it is playing.
	 */
	private float computeNotept2() {
		if (pa.currentPhrase.getTotalDuration() == 0) return 0;
		return PApplet.map(player2.getTickPosition(), 0, player2.getTickLength(), 0, pa.currentPhrase.getTotalDuration());
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
			dNotept1 += pa.currentPhrase.getTotalDuration();
		}

		if (dNotept2 < 0) {
			dNotept2 += pa.currentPhrase.getTotalDuration();
		}

		// smoothing:
		dataPts++;
		totalNotept1 += dNotept1;
		totalNotept2 += dNotept2;
		float avg_dNotept1 = totalNotept1 / dataPts;
		float avg_dNotept2 = totalNotept2 / dataPts;
		
		float actual_dNotept1 = avg_dNotept1 + 0.05f * accountBalance1;
		float actual_dNotept2 = avg_dNotept2 + 0.05f * accountBalance2;
		
		accountBalance1 += (dNotept1 - actual_dNotept1);
		accountBalance2 += (dNotept2 - actual_dNotept2);
	
		if (PRINT_OUT_ANIMATION_ACCURACY) {
			System.out.println("accountBalance1: " + accountBalance1 + ", accountBalance2: " + accountBalance2);
		}
			
		prev_notept1 = notept1;
		prev_notept2 = notept2;

		reader1.update(actual_dNotept1);
		reader2.update(actual_dNotept2);
		view.update(dt, actual_dNotept1, actual_dNotept2);

		if (view != phaseShifterView) {
			phaseShifterView.updateNormalTransforms(actual_dNotept1, actual_dNotept2);
		}
	}
	
	/********************************
	 ***** Input Event Handling *****
	 ********************************/

	@Override
	public void keyPressed() {		
		if (pa.key == pa.CODED) {
			switch (pa.keyCode) {
				case PApplet.LEFT : iconLeft(); break;
				case PApplet.RIGHT : iconRight(); break;
				case PApplet.UP : iconUp(); break;
				case PApplet.DOWN : iconDown(); break;
			}
		}
		else {
			switch (pa.key) {
				case 'A' :
				case 'a' : iconLeft(); break;
				case 'D' :
				case 'd' : iconRight(); break;
				case 'W' :
				case 'w' : iconUp(); break;
				case 'S' :
				case 's' : iconDown(); break;
			}
		}
	}

	@Override
	public void mousePressed() {		
		int iconIndex = iconIndexTouches(pa.mouseX, pa.mouseY);
		if (iconIndex != -1) {
			activeIconIndex = iconIndex;
			repositionDirectionalButtons();
		}
	}
	
	/*******************************
	 ***** Note Event Handling *****
	 *******************************/
	
	@Override
	public void noteEvent(PhraseReader reader) {
		view.noteEvent(reader);
	}
	
	/********************************
	 ***** Icon Unlock Handling *****
	 ********************************/
	
	/**
	 * First, it checks if a new icons has been unlocked for the active view type.
	 * If there's been a new unlock, it calls setupIconLists(), which refreshes the state of the icon lists.
	 * If there's been a new unlock, it updates the player info save file.
	 */
	private void checkUnlocks() {
		boolean unlockedSomething = false;
		
		if (viewType.toInt() == MUSICIAN && 
				pa.playerInfo.nextMusicianUnlockIndex < musicianUnlockSeq.length &&
				musicianUnlockSeq[pa.playerInfo.nextMusicianUnlockIndex] <= pa.playerInfo.minutesSpentWithMusician) {
			setupIconLists();
			pa.playerInfo.nextMusicianUnlockIndex++;
			unlockedSomething = true;
		}
		else if (viewType.toInt() == PHASE_SHIFTER &&
				pa.playerInfo.nextPhaseShifterUnlockIndex < phaseShifterUnlockSeq.length &&
				phaseShifterUnlockSeq[pa.playerInfo.nextPhaseShifterUnlockIndex] <= pa.playerInfo.minutesSpentWithPhaseShifter) {
			setupIconLists();
			pa.playerInfo.nextPhaseShifterUnlockIndex++;
			unlockedSomething = true;
		}
		else if (viewType.toInt() == LIVE_SCORER && 
			pa.playerInfo.nextLiveScorerUnlockIndex < liveScorerUnlockSeq.length &&
			liveScorerUnlockSeq[pa.playerInfo.nextLiveScorerUnlockIndex] <= pa.playerInfo.minutesSpentWithLiveScorer) {
			setupIconLists();
			pa.playerInfo.nextLiveScorerUnlockIndex++;
			unlockedSomething = true;
		}
		else if ( (phaseShifterUnlocked() && viewTypeIcons.length < 2) || 
				(liveScorerUnlocked() && viewTypeIcons.length < 3)) {
			setupIconLists();
		}
		
		if (unlockedSomething) {
			pa.savePlayerInfo();
		}
		
		if (pa.playerInfo.nextMusicianUnlockIndex != 0) {
			upButton.show();
			downButton.show();
		}
		else {
			upButton.hide();
			downButton.hide();
		}
		
		if (pa.playerInfo.minutesSpentWithMusician < 1f) {
			pa.hideChangeScreenButtons();
		}
		else {
			pa.showChangeScreenButtons();
		}
	}
	
	/*******************************
	 ***** Icon Initialization *****
	 *******************************/
	
	/**
	 * Adds all the unlocked view type icons to the container called "iconLists",
	 * which stores all the icons that are currently available to the player.
	 */
	private void setupViewTypeIcons() {
		int availability = getIconAvailability(viewTypeName);
		viewTypeIcons = new Icon[availability];
		if (availability > 0) {
			
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
						case orientationModeName:
							iconList = new Icon[availability];
							for (int i = 0; i < availability; i++) {
								iconList[i] = new OrientationIcon(i);
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
	
	//TODO Pos. improvement: Implement unlock sequences by interpreting a text file that defines the unlock sequences
	
	/**
	 * 
	 * @param name The String identifier for a type of icon related to the current view type. All of these kinds of names are given in views.ViewVariableInfo.java.
	 * @return The number of icons that are available (unlocked) of the icon type associated with the given identifier.
	 */
	private int getIconAvailability(String name) {
		switch (name) {
			case viewTypeName:
				if (liveScorerUnlocked()) {
					return 3;
				}
				else if (phaseShifterUnlocked()) {
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
	 * @return True if the conditions are met for the PhaseShifter icon to be unlocked, false otherwise.
	 */
	private boolean phaseShifterUnlocked() {
		return pa.playerInfo.minutesSpentWithMusician > 3f;
	}
	
	/**
	 * 
	 * @return True if the conditions are met for the LiveScorer icon to be unlocked, false otherwise.
	 */
	private boolean liveScorerUnlocked() {
		return pa.playerInfo.minutesSpentWithMusician + pa.playerInfo.minutesSpentWithPhaseShifter > 25f;
	}
	
	/**
	 * 
	 * @param name The String identifier for a type of icon related to the view type called Musician. All of these kinds of names are given in views.ViewVariableInfo.java.
	 * @return The number of icons that are available (unlocked) of the icon type associated with the given identifier.
	 */
	private int getMusicianIconAvailability(String name) {
		switch (name) {
			case colorSchemeName:
				if (pa.playerInfo.minutesSpentWithMusician > musicianUnlockSeq[0]) {
					return 2;
				}
				else {
					return 0;
				}
			case superimposedOrSeparatedName:
				if (pa.playerInfo.minutesSpentWithMusician > musicianUnlockSeq[1]) {
					return 2;
				}
				else {
					return 0;
				}
			case instrumentName:
				if (pa.playerInfo.minutesSpentWithMusician > musicianUnlockSeq[2]) {
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
				if (pa.playerInfo.minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[3]) {
					return 3;
				}
				else if (pa.playerInfo.minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[0]) {
					return 2;
				}
				else {
					return 0;
				}
			case noteGraphicSet1Name:
				if (pa.playerInfo.minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[7]) {
					return 5;
				}
				else if (pa.playerInfo.minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[6]) {
					return 4;
				}
				else if (pa.playerInfo.minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[2]) {
					return 3;
				}
				else if (pa.playerInfo.minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[1]) {
					return 2;
				}
				else {
					return 0;
				}
			case cameraModeName:
				if (pa.playerInfo.minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[4]) {
					return 2;
				}
				else {
					return 0;
				}
			case plotPitchModeName:
				return 0; //deprecated
			case transformationName:
				if (pa.playerInfo.minutesSpentWithPhaseShifter > phaseShifterUnlockSeq[5]) {
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
				if (pa.playerInfo.minutesSpentWithLiveScorer > liveScorerUnlockSeq[0]) {
					return 2;
				}
				else {
					return 0;
				}
			case sineWaveName:
				if (pa.playerInfo.minutesSpentWithLiveScorer > liveScorerUnlockSeq[1]) {
					return 2;
				}
				else {
					return 0;
				}
			case orientationModeName:
				if (pa.playerInfo.minutesSpentWithLiveScorer > liveScorerUnlockSeq[2]) {
					return 2;
				}
				else {
					return 0;
				}
			default:
				return 0;
		}
	}
	
	/******************
	 ***** Saving *****
	 ******************/
	
	public void saveViewSettings() {
		musicianView.saveSettings(pa.playerInfo);
		phaseShifterView.saveSettings(pa.playerInfo);
		liveScorerView.saveSettings(pa.playerInfo);
	}
}