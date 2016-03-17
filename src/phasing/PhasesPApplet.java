package phasing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arb.soundcipher.SCScore;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import controlp5.Util;
import geom.Polygon;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.data.JSONObject;
import processing.event.MouseEvent;
import screens.Editor;
import screens.PhraseRepository;
import screens.Presenter;
import screens.Screen;
import util.NameGenerator;

/**
 * 
 * @author James Morrow
 *
 */
public class PhasesPApplet extends PApplet {
	//phrase picture name generator
	public static NameGenerator phrasePictureNameGenerator;
	
	//music parameters
	public static final float MIN_BPM = 1, MAX_BPM = 100;
	
	//all music variables
	public final static String[] roots = new String[] {"A", "A#/Bb", "B", "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab"};
	public final static ArrayList<String> scaleTypes = new ArrayList<String>();
	private static Map<String, ScaleSet> scaleSets = new HashMap<String, ScaleSet>();
	
	//active music variables
	public Phrase currentPhrase;
	public PhrasePicture currentPhrasePicture;
	public Scale currentScale;
	public ArrayList<PhrasePicture> phrasePictures;
	private float bpm1 = 60;
	private float bpms1 = bpm1 / 60000f;
	private float bpm2 = 60.5f;
	private float bpms2 = bpm2 / 60000f;
	
	//screens
	private Presenter presenter;
	private Editor editor;
	private PhraseRepository phraseRepo;
	private boolean presenterEntered = false;
	private Screen currentScreen;
	
	//visual variables
	private static ColorScheme colorScheme;
	public static PFont pfont12, pfont18, pfont42, musicFont;
	
	//controlp5
	private ControlP5 cp5;
	private Button changeScreenButton;
	private Button helpToggle;
	private Button phraseRepoButton;
	private boolean helpOn;
	
	//screen size
	private int prevWidth, prevHeight;
	private static boolean initialWindowSizeGiven = false;
	private static int initialWidthSize = 800, initialHeightSize = 600; //default window size if given no command line args
	
	//save folder location
	public String saveFolderPath; //TODO: Phase this variable out
	
	//player
	public PlayerInfo playerInfo;

	/*****************
	 ***** Setup *****
	 *****************/
	
	/**
	 * Sets the initial width and height of the window.
     * But PhasesPApplet reverts to its defaults if the given
     * width is less than 800 or the given height is less than 600.
	 */
	protected static void setInitialScreenSize(int width, int height) {
		if (width >= 800 && height >= 600) {
			initialWidthSize = width;
			initialHeightSize = height;
			initialWindowSizeGiven = true;
		}
	}
	
	/**
	 * Sets up the size of the canvas/window
	 */
	public void settings() {
		size(initialWidthSize, initialHeightSize);
		prevWidth = width;
		prevHeight = height;
	}
	
	/**
	 * Initializes all of the class's static variables.
	 */
	private void initStaticVariables() {
		//init font variables
		pfont12 = loadFont("DejaVuSans-12.vlw");
		pfont18 = loadFont("DejaVuSans-18.vlw");
		pfont42 = loadFont("DejaVuSans-42.vlw");
		musicFont = loadFont("MaestroWide-48.vlw");
		
		//init colors
		initColorScheme();
		//initSimpleColorScheme();
		
		//init save folder path
		saveFolderPath = sketchPath() + "\\sav\\";
		
		initPhrasePictures();
		
		//init name generator, excluding names given by phrasePictures
		phrasePictureNameGenerator = new NameGenerator(PhrasePicture.getNames(phrasePictures));
	}
	
	/**
	 * Does the initial setup.
	 */
	public void setup() {
		initStaticVariables();
		initPlayerInfo();
		surface.setResizable(true);
		initCurrentScale();				
		initCurrentPhrase();
		initScreens();
		currentScreen = presenter;
		initCP5Objects(currentScreen);
		manipulateControllers();
		currentScreen.onEnter();
		if (!initialWindowSizeGiven && playerInfo.isWindowSizeInitialized()) {
			this.resize(playerInfo.getWindowWidth(), playerInfo.getWindowHeight());
		}
	}
	
	/**
	 * Initializes the ControlP5 object and its controllers.
	 * @param currentScreen Indicates that the method relies on currentScreen being already initialized.
	 */
	private void initCP5Objects(Screen currentScreen) {
		if (cp5 != null) {
			cp5.dispose();
		}
		cp5 = new ControlP5(this);
		cp5.setAutoDraw(false);
		initChangeScreenButton(currentScreen);
		initHelpToggle(changeScreenButton);
		initPhraseRepoButton(helpToggle);	
	}
	
	/**
	 * Loads the phrase pictures.
	 */
	private void initPhrasePictures() {
		boolean phrasePicturesLoaded = loadPhrasePictures();
		if (!phrasePicturesLoaded) {
			phrasePictures = new ArrayList<PhrasePicture>();
		}
	}
	
	/**
	 * Initializes the screens.
	 */
	private void initScreens() {
		presenter = new Presenter(this);
		editor = new Editor(this);
		phraseRepo = new PhraseRepository(this);
	}
	
	/**
	 * Initializes the playerInfo.
	 */
	private void initPlayerInfo() {
		boolean playerInfoLoaded = loadPlayerInfo();
		if (!playerInfoLoaded) {
			playerInfo = new PlayerInfo(true);
			savePlayerInfo();
		}
	}
	
	/**
	 * Initializes the currentScale.
	 */
	private void initCurrentScale() {
		loadScales();
		boolean currentScaleLoaded = loadCurrentScale();
		if (!currentScaleLoaded) {
			currentScale = getRandomScale();
			saveCurrentScale();
		}
	}
	
	/**
	 * Initializes the currentPhrase.
	 */
	private void initCurrentPhrase() {
		boolean currentPhraseLoaded = loadCurrentPhrasePicture();
		if (!currentPhraseLoaded) {
			currentPhrase = generateReichLikePhrase(currentScale);
			currentPhrasePicture = new PhrasePicture(currentPhrase, "Current Phrase", this);
			saveCurrentPhrasePicture();
		}
	}
	
	/**
	 * Gives the proper value for the lowermost y-coordinate of the top toolbar,
	 * which is dependent on the height of the window.
	 * @return The proper lowermost y-coordinate of the top toolbar.
	 */
	public int topToolbarY2() {
		return 60;
		//return (int)PApplet.constrain(PApplet.map(this.height, 0, 600, 0, 60), 60, 150);
	}
	
	/**
	 * Gives the proper height of the change screen button, depending on the height of the screen.
	 * @return The proper height of the changeScreenButton.
	 */
	private int changeScreenButtonY2() {
		return topToolbarY2() - 10;
	}
	
	/**
	 * Gives the proper height of the change screen button, depending on what the current screen is.
	 * @return The proper height of the change screen button.
	 */
	private int changeScreenButtonHeight() {
		return (currentScreen != phraseRepo) ? 40 : 33;
	}
	
	private int helpToggleWidth() {
		return (currentScreen == editor) ? 110 : changeScreenButton.getWidth();
	}
	
	/**
	 * Gives the proper width of the change screen button.
	 * @return The proper width of the changeScreenButton.
	 */
	private int changeScreenButtonX2() {
		return 135;
	}
	
	/**
	 * Initializes the changeScreenButton.
	 */
	private void initChangeScreenButton(Screen currentScreen) {
		int width = 125;
		int height = changeScreenButtonHeight();
		changeScreenButton = cp5.addButton("changeScreen")
			                    .setPosition(changeScreenButtonX2() - width, changeScreenButtonY2() - height)
			                    .setSize(width, height)
			                    ;
		changeScreenButton.getCaptionLabel().toUpperCase(false);
		changeScreenButton.getCaptionLabel().setFont(pfont18);
		colorControllerShowingLabel(changeScreenButton);
		
		changeScreenButton.setCaptionLabel(captionLabel(currentScreen));
	}
	
	/**
	 * Initializes the helpToggle;
	 * @param changeScreenButton This is here to make explicit that this method relies on changeScreenButton being already initialized.
	 */
	private void initHelpToggle(Button changeScreenButton) {
		int toggleWidth = helpToggleWidth();
		int toggleHeight = 12;
		float x1 = Util.getX2(changeScreenButton) + 15;
		float y1 = Util.getY1(changeScreenButton);
		helpToggle = cp5.addButton("toggleHelp")
				        .setLabel("Help")
				        .setPosition(x1, y1)
				        .setSize(toggleWidth, toggleHeight)
				        ;
		helpToggle.getCaptionLabel().toUpperCase(false);
		helpToggle.getCaptionLabel().setFont(pfont12);
		colorControllerShowingLabel(helpToggle);
	}
	
	/**
	 * Initializes the button that takes the program to the phraseRepo screen.
	 * @param helpToggle This is here to make explicit that this method relies on helpToggle being already initialized.

	 */
	private void initPhraseRepoButton(Button helpToggle) {
		int buttonWidth = 110;
		int buttonHeight = 12;
		float x1 = Util.getX2(helpToggle) + 15;
		float y1 = Util.getY1(helpToggle);
		phraseRepoButton = cp5.addButton("toPhraseRepo")
				              .setLabel("Phrase Repo")
				              .setPosition(x1, y1)
		                      .setSize(buttonWidth, buttonHeight)
		                      ;
		phraseRepoButton.getCaptionLabel().toUpperCase(false);
		phraseRepoButton.getCaptionLabel().setFont(pfont12);
		colorControllerShowingLabel(phraseRepoButton);
	}
	
	/**
	 * Initializes a red / blue color scheme.
	 */
	private void initColorScheme() {
		colorMode(HSB, 360, 100, 100, 100);
		int color1 = color(0, 60, 90);
		int color1Bold = color(0, 90, 90);
		int color1VeryBold = color(0, 100, 100);
		int color2 = color(240, 60, 90);
		int color2Bold = color(240, 90, 90);
		int color2VeryBold = color(240, 100, 100);
		colorMode(RGB, 255, 255, 255, 255);
		colorScheme = new ColorScheme(color1, color2, color1Bold, color2Bold, color1VeryBold, color2VeryBold);
	}
	
	/**
	 * Initializes a color scheme composed of a random color and a gray.
	 */
	private void initSimpleColorScheme() {
		int r = (int)random(255);
		int g = (int)random(255);
		int b = (int)random(255);
		
		int color1 = color(r, g, b, 150);
		int color1Bold = color(r, g, b, 255);
		int color2 = color(100);
		int color2Bold = color(0);
		
		colorScheme = new ColorScheme(color1, color2, color1Bold, color2Bold, color1Bold, color2Bold);
	}
	
	/****************************
	 ***** Saving / Loading *****
	 ****************************/
	
	/**
	 * Looks at all the JSON files in the data/scales folder and tries to translate them into
	 * scale sets, which go into the PhasesPApplet member variable "scaleSets".
	 */
	private void loadScales() {
		try {
			int i=0;
			Files.walk(Paths.get("src/data/scales")).forEach(filePath -> {
				if (filePath.toString().endsWith(".json")) {
					JSONObject json = loadJSONObject(filePath.toString());
					ScaleSet ss = new ScaleSet(json);
					scaleSets.put(ss.getName(), ss);
					scaleTypes.add(ss.getName());
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Tries to initialize the current phrase and current phrase picture by loading the appropriate file.
	 * @return True, if it succeeds. False if it fails.
	 */
	private boolean loadCurrentPhrasePicture() {
		File file = new File(saveFolderPath + "phrases\\Current Phrase.json");
		if (file.exists()) {
			try {
				JSONObject json = loadJSONObject(file);
				currentPhrasePicture = new PhrasePicture(json);
				currentPhrase = currentPhrasePicture.getPhrase();
				return true;
			}
			catch (RuntimeException e) {
				e.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	/**
	 * Saves the phrase picture of the current phrase to the phrases subfolder in the save folder.
	 */
	public void saveCurrentPhrasePicture() {
		saveJSONObject(currentPhrasePicture.toJSON(), saveFolderPath + "phrases\\" + currentPhrasePicture.getName() + ".json");
	}
	
	/**
	 * Tries to initialize the playerInfo variable by loading the appropriate file.
	 * @return True, if it succeeds. False if it fails.
	 */
	private boolean loadPlayerInfo() {
		String playerInfoFileName = saveFolderPath + "playerInfo.json";
		File playerInfoFile = new File(playerInfoFileName);
		if (playerInfoFile.exists()) {
			try {
				JSONObject json = loadJSONObject(playerInfoFileName);
				playerInfo = new PlayerInfo(json);
				return true;
			} catch (RuntimeException e) {
				e.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	/**
	 * Saves the player info to a file in the save folder.
	 */
	public void savePlayerInfo() {
		JSONObject json = playerInfo.toJSON();
		try {
			saveJSONObject(json, saveFolderPath + "playerInfo.json");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Tries to initialize the current scale variable by loading the appropriate file.
	 * @return True, if it succeeds. False, if it fails.
	 */
	public boolean loadCurrentScale() {
		File file = new File(saveFolderPath + "\\Current Scale.json");
		if (file.exists()) {
			try {
				JSONObject json = loadJSONObject(file);
				currentScale = new Scale(json);
				return true;
			} catch (RuntimeException e) {
				e.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	/**
	 * Saves the current scale to the save folder.
	 */
	public void saveCurrentScale() {
		saveJSONObject(currentScale.toJSON(), saveFolderPath + "\\Current Scale.json");
	}
	
	/**
	 * Saves the phrase pictures to the phrases subfolder in the save folder.
	 * @param phrasePictures
	 */
	public void savePhrasePictures() {
		for (int i=0; i<phrasePictures.size(); i++) {
			PhrasePicture p = phrasePictures.get(i);
			saveJSONObject(p.toJSON(), saveFolderPath + "phrases\\" + p.getName() + ".json");
		}
	}
	
	/**
	 * Loads the phrase pictures from the phrases subfolder in the save folder,
	 * except for any file named "Current Phrase".
	 * 
	 * @return True, if it succeeds. False, if it fails. 
	 */
	public boolean loadPhrasePictures() {
		try {
			phrasePictures = new ArrayList<PhrasePicture>();
			Files.walk(Paths.get(saveFolderPath + "phrases\\")).forEach(filePath -> {
				if (filePath.toString().endsWith(".json") &&
						!filePath.toString().equals(saveFolderPath + "phrases\\Current Phrase.json")) {
					JSONObject json = loadJSONObject(filePath.toString());
					phrasePictures.add(new PhrasePicture(json));
				}
			});
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/*****************************
	 ***** Phrase Generation *****
	 *****************************/	
	
	/**
	 * 
	 * @return The score from Steve Reich's Piano Phase (1967)
	 */
	public Phrase pianoPhase() {
		int n = Phrase.NOTE_START;
		return currentPhrase = new Phrase(new float[] {64, 66, 71, 73, 74, 66, 64, 73, 71, 66, 74, 73},
	                                      new float[] {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50},
	                                      new int[] {n, n, n, n, n, n, n, n, n, n, n, n});
	}
	
	/**
	 * 
	 * @return A random scale contained by this PhasesPApplet object.
	 */
	public Scale getRandomScale() {
		String type = scaleTypes.get((int)random(scaleTypes.size()));
		ScaleSet s = scaleSets.get(type);
		return s.getScale((int)random(s.numScales()));
	}
	
	/**
	 * Generates a phrase that has the property of being like the phrase in Steve Reich's Piano Phase
	 * in this sense: every pitch in the phrase occurs periodically.
	 * 
	 * A phasing process over a phrase like this as opposed to a generic phrase results in a
	 * greater frequence of unisons (two notes of the same pitch playing at the same time).
	 * 
	 * @return The generated phrase.
	 */
	public Phrase generateReichLikePhrase() {
		return generateReichLikePhrase(getRandomScale());
	}
	
	/**
	 * Generates a phrase that has the property of being like the phrase in Steve Reich's Piano Phase
	 * in this sense: every pitch in the phrase occurs periodically.
	 * 
	 * A phasing process over a phrase like this as opposed to a generic phrase results in a
	 * greater frequence of unisons (two notes of the same pitch playing at the same time).
	 * 
	 * @param scale The set of pitches the phrase draws from.
	 * 
	 * @return The generated phrase.
	 */
	public Phrase generateReichLikePhrase(final Scale scale) {
		return generateReichLikePhrase(scale, 5, (random(1) < 0.5f) ? true : false);
	}
	
	/**
	 * Generates a phrase that has the property of being like the phrase in Steve Reich's Piano Phase
	 * in this sense: every pitch in the phrase occurs periodically.
	 * 
	 * A phasing process over a phrase like this as opposed to a generic phrase results in a
	 * greater frequence of unisons (two notes of the same pitch playing at the same time).
	 * 
	 * @param scale The set of pitches the phrase draws from.
	 * @param octave The starting octave. Pitches in the phrase may be above this octave, but not below it.
	 * @param allowRests A boolean that indicates whether or not a rest can stand in for a pitch.
	 * 
	 * 
	 * @return The generated phrase.
	 */
	public Phrase generateReichLikePhrase(final Scale scale, final int octave, final boolean allowRests) {
		return generatePhraseFromTemplates(new String[] {"ABCDAECF", "ABCDABCE", "ABCDEBADCBED", "ABCDEBADCBED", "ABCDEBADFBEDABGDEBADHBED", "ABCDAECFADCEAGCDAECHADCE"}, scale, octave, allowRests);
	}
	
	/**
	 * Generates a phrase from one of the given templates.
	 * 
	 * A template is a string of characters. Each character is like a variable. It represents some pitch.
	 * This function randomly binds every character in the template to a pitch in the scale.
	 * 
	 * @param templates The set of templates.
	 * @param scale The set of pitches the phrase draws from.
	 * @param minOctave The starting octave. Pitches in the phrase may be above this octave, but not below it.
	 * @param allowRests A boolean that indicates whether or not a rest can stand in for a pitch.
	 * @return The generated phrase.
	 */
	public Phrase generatePhraseFromTemplates(final String[] templates, final Scale scale, final int minOctave, final boolean allowRests) {
		//choose template
		final int REST = -1; 
		String template = templates[(int)random(templates.length)];
		HashMap<Character, Integer> map = new HashMap<Character, Integer>();
		
		//init set of pitch choices
		int numPitchesNeeded = numUniqueChars(template);
		int numOctavesNeeded = ceil((float)numPitchesNeeded / (float)scale.size());
		int numPitchChoices = numOctavesNeeded * scale.size();
		
		if (allowRests) numPitchChoices++;
		int[] pitchChoices = new int[numPitchChoices];
		for (int i=0; i<pitchChoices.length; i++) {
			pitchChoices[i] = scale.getNoteValue(i) + minOctave*12;
		}
		if (allowRests) pitchChoices[pitchChoices.length-1] = REST;
		
		shuffle(pitchChoices);
		
		//init phrase components
		float[] pitches = new float[template.length()];
		float[] dynamics = new float[template.length()];
		int[] cellTypes = new int[template.length()];
		
		int j=0; //loops through pitch choices
		for (int i=0; i<template.length(); i++) { //loops through template
			char c = template.charAt(i);
			if (!map.containsKey(c)) {
				int pitch = pitchChoices[j];
				j++;
				map.put(c, pitch);
			}
			int pitch = map.get(c);
			pitches[i] = pitch;
			dynamics[i] = (pitch == REST) ? 0 : 50;
			cellTypes[i] = (pitch == REST) ? Phrase.REST : Phrase.NOTE_START;
		}
		
		Phrase phrase = new Phrase(pitches, dynamics, cellTypes);
		
		return new Phrase(pitches, dynamics, cellTypes);
	}
	
	/**
	 * Counts the number of unique characters in the given string and returns the result.
	 * @param s The string.
	 * @return The number of unique characters in the given string.
	 */
	private int numUniqueChars(String s) {
		String uniqueChars = "";
		
		for (int i=0; i<s.length(); i++) {
			if (!uniqueChars.contains("" + s.charAt(i))) {
				uniqueChars += s.charAt(i);
			}
		}
		
		return uniqueChars.length();
	}
	
	/**
	 * Randomly shuffles an array of ints.
	 * @param xs The array of ints to shuffle.
	 */
	private void shuffle(int[] xs) {
		int i = xs.length - 1;
		while (i > 0) {
			int j = (int)random(0, i+1);
			swap(xs, i, j);
			i--;
		}
	}
	
	/**
	 * Swaps two ints within an int array.
	 * @param xs The int array.
	 * @param i The index of one int.
	 * @param j The index of the other int.
	 */
	private static void swap(int[] xs, int i, int j) {
		int xs_i = xs[i];
		xs[i] = xs[j];
		xs[j] = xs_i;
	}
	
	/*****************************
	 ***** ControlP5 Related *****
	 *****************************/
	
	/**
	 * Draws the PhasesPapplet's controllers.
	 */
	public void drawControlP5() {
		cp5.draw();
	}
	
	/**
	 * Makes all the PhasesPApplet's controllers invisible.
	 */
	public void hideAllControllers() {
		helpToggle.hide();
		changeScreenButton.hide();
		phraseRepoButton.hide();
	}
	
	/**
	 * Makes all the PhasesPApplet's controllers visible.
	 */
	public void showAllControllers() {
		helpToggle.show();
		changeScreenButton.show();
		phraseRepoButton.show();
	}
	
	/**
	 * Gives the default coloring for labeled buttons to the given controller.
	 * @param c The given controller.
	 */
	public static void colorControllerShowingLabel(Controller c) {
		c.setColorCaptionLabel(0xffffffff);
	    c.setColorValueLabel(0xffffffff);
		c.setColorBackground(getColor1());
		c.setColorForeground(getColor1Bold());
		c.setColorActive(getColor1Bold());
	}
	
	/**
	 * Gives the default coloring for unlabeled buttons to the given button.
	 * @param b The given button.
	 */
	public void colorControllerHideLabel(Button b) {
		b.setColorBackground(color(255));
	    b.setColorForeground(getColor1());
	    b.setColorActive(getColor1Bold());
	}
	
	/***********************************
	 ***** Callback from ControlP5 *****
	 ***********************************/
	
	/**
	 * Callback from ControlP5.
	 * Controls changing the screen from Presentation screen to Editor screen and vice versa.
	 */
	public void changeScreen() {
		if (currentScreen == editor) {
			changeScreenTo(presenter);	
		}
		else if (currentScreen == presenter) {
			changeScreenTo(editor);
		}
		else if (currentScreen == phraseRepo) {
			changeScreenTo(editor);
		}
	}
	
	/**
	 * Changes the current screen from what it currently is to the given screen.
	 * @param destination The screen to change to.
	 */
	private void changeScreenTo(Screen destination) {
		currentScreen.onExit();
		currentScreen = destination;
		manipulateControllers();
		currentScreen.onEnter();
		changeScreenButton.setCaptionLabel(captionLabel(currentScreen));
	}
	
	/**
	 * Manipulates the controllers based on the value of currentScreen.
	 */
	private void manipulateControllers() {
		if (currentScreen == editor) {
			helpToggle.show();
			phraseRepoButton.show();
			changeScreenButton.setHeight(changeScreenButtonHeight());
			Util.setY2(changeScreenButton, changeScreenButtonY2());
			helpToggle.setWidth(helpToggleWidth());
			Util.setX1(helpToggle, Util.getX2(changeScreenButton) + 15);
			Util.setY1(helpToggle, Util.getY1(changeScreenButton));
			Util.setX1(phraseRepoButton, Util.getX2(helpToggle) + 15);
			Util.setY1(phraseRepoButton, Util.getY1(helpToggle));
		}
		else if (currentScreen == presenter) {
			helpToggle.hide();
			phraseRepoButton.hide();
		}
		else if (currentScreen == phraseRepo) {
			phraseRepoButton.hide();
			helpToggle.show();
			changeScreenButton.setHeight(changeScreenButtonHeight());
			helpToggle.setWidth(helpToggleWidth());
			Util.setY2(changeScreenButton, changeScreenButtonY2());
			Util.setX1(helpToggle, Util.getX1(changeScreenButton));
			Util.setY2(helpToggle, Util.getY1(changeScreenButton) - 5);
		}
		
		
		
	}
	
	/**
	 * Gives what the caption label should be for the given screen.
	 * @param screen The screen.
	 * @return The caption label.
	 */
	private String captionLabel(Screen screen) {
		if (screen == presenter) {
			return "Compose";
		}
		else if (screen == editor) {
			return "Rehearse";
		}
		else if (screen == phraseRepo) {
			return "Compose";
		}
		else {
			return "";
		}
			
	}
	
	/**
	 * Callback from ControlP5.
	 * Controls whether or not the help information is displayed.
	 */
	public void toggleHelp() {
		helpOn = !helpOn;
		
		if (helpOn) {
			helpToggle.setColorBackground(getColor1Bold());
		}
		else {
			helpToggle.setColorBackground(getColor1());
		}
	}
	
	/**
	 * Calback from ControlP5.
	 * Sends the program to the phraseRepo screen.
	 */
	public void toPhraseRepo() {
		changeScreenTo(phraseRepo);
	}
	
	/*********************
	 ***** Draw Loop *****
	 *********************/
	
	/**
	 * Sends a message to the current screen to draw itself.
	 */
	public void draw() {
		checkForWindowResizeEvent();
		currentScreen.draw();
	}
	
	/********************************
	 ***** Input Event Handling *****
	 ********************************/
	
	/**
	 * Sends mouse pressed events to the current screen.
	 */
	public void mousePressed() {
		currentScreen.mousePressed();
	}
	
	/**
	 * Sends mouse released events to the current screen.
	 */
	public void mouseReleased() {
		currentScreen.mouseReleased();
	}
	
	/**
	 * Sends mouse dragged events to the current screen.
	 */
	public void mouseDragged() {
		currentScreen.mouseDragged();
	}
	
	/**
	 * Sends mouse moved events to the current screen.
	 */
	public void mouseMoved() {
		currentScreen.mouseMoved();
	}
	
	/**
	 * Handles pause / unpause requests.
	 * Sends key pressed events to the current screen.
	 */
	public void keyPressed() {
		if (key == 'h') {
			toggleHelp();
		}
		else if (key == ESC) {
			key = 0; //disable Processing's default behavior to close the program when ESC is pressed
		}
		else  {
			currentScreen.keyPressed();
		}
	}
	
	/**
	 * Sends key released events to the current screen.
	 */
	public void keyReleased() {
		currentScreen.keyReleased();
		
	}
	
	/**
	 * Sends mouse wheel events to the current screen.
	 */
	public void mouseWheel(MouseEvent event) {
		currentScreen.mouseWheel(event);
	}
	
	/************************
	 ***** Other Events *****
	 ************************/
	
	/**
	 * The way to resize the PhasesPApplet through code logic.
	 * 
	 * @param width The new width for the window.
	 * @param height The new height for the window.
	 */
	public void resize(int width, int height) {
		surface.setSize(width, height);
		checkForWindowResizeEvent();
	}
	
	/**
	 * Checks if the window size has changed.
	 * If so, informs the currentScreen of the size change.
	 * 
	 */
	private void checkForWindowResizeEvent() {
		if (prevWidth != width || prevHeight != height) {
			playerInfo.setSize(width, height);
			repositionControllers();
			prevWidth = width;
			prevHeight = height;
			currentScreen.windowResized();
		}
	}
	
	/**
	 * Repositions all the controllers that need to be repositioned in the case of a window resize event.
	 */
	private void repositionControllers() {
		repositionChangeScreenButton();
		repositionHelpToggle(changeScreenButton);
		repositionPhraseRepoButton(helpToggle);
	}
	
	/**
	 * Repositions the ChangeScreenButton based on the current height of the window.
	 */
	private void repositionChangeScreenButton() {
		Util.setY2(changeScreenButton, changeScreenButtonY2());
	}
	
	/**
	 * Repositions the help toggle based on the current height of the window.
	 * @param changeScreenButton Indicates that the changeScreenButton should be up to date before calling this method.
	 */
	private void repositionHelpToggle(Button changeScreenButton) {
		Util.setY2(helpToggle, Util.getY1(changeScreenButton) - 5);
	}
	
	/**
	 * Repositions the phraseREpoButton based on the current height of the window.
	 * @param changeScreenButton Indicates that the changeScreenButton should be up to date before calling this method.
	 */
	private void repositionPhraseRepoButton(Button changeScreenButton) {
		Util.setY2(phraseRepoButton, Util.getY1(changeScreenButton) - 5);
	}
	
	/************************************************
	 ***** Extended Primitive Drawing Functions *****
	 ************************************************/
	
	/**
	 * Draws an arrow head from two lines (which looks like this: >).
	 * It has its tip at (x,y).
	 * The lines have a given length.
	 * The headAngle tells the arrow head which direction to point, in radians
	 * The deviationAngle tells the lines how much to bend away from the head angle, in radians.
	 * 
	 * @param x The x-coordinate of the arrow head's tip
	 * @param y The y-coordinate of the aroow head's tip
	 * @param length The length of each of the lines
	 * @param headAngle The direction in which the arrow head points, in radians
	 * @param deviationAngle How much the lines bend away from the head angle, in radians
	 * @param pg The PGraphics instance on which to draw the arrow head
	 */
	public static void drawArrowHead(float x, float y, float leng, float headAngle, float deviationAngle, PGraphics pg) {
		 pg.line(x, y, x + leng*cos(headAngle + deviationAngle), y + leng*sin(headAngle + deviationAngle));
		 pg.line(x, y, x + leng*cos(headAngle - deviationAngle), y + leng*sin(headAngle - deviationAngle));
	}
	
	/**
	 * Draws an arrow head from two lines (which looks like this: >).
	 * It has its tip at (x,y).
	 * The lines have a given length.
	 * The headAngle tells the arrow head which direction to point, in radians
	 * The deviationAngle tells the lines how much to bend away from the head angle, in radians.
	 * 
	 * @param x The x-coordinate of the arrow head's tip
	 * @param y The y-coordinate of the aroow head's tip
	 * @param length The length of each of the lines
	 * @param headAngle The direction in which the arrow head points, in radians
	 * @param deviationAngle How much the lines bend away from the head angle, in radians
	 */
	public void drawArrowHead(float x, float y, float length, float headAngle, float deviationAngle) {
	    line(x, y, x + length*cos(headAngle + deviationAngle), y + length*sin(headAngle + deviationAngle));
	    line(x, y, x + length*cos(headAngle - deviationAngle), y + length*sin(headAngle - deviationAngle));
	}
	
	/**
	 * Draws a quarter note symbol, as in standard musical notation, centered about (cenx, ceny).
	 * @param cenx The center x-coordinate
	 * @param ceny The center y-coordinate
	 * @param textSize The size of the font that draws the symbol
	 */
	public void drawQuarterNoteSymbol(float cenx, float ceny, int textSize) {
	    pushStyle();
	    textFont(musicFont);
	    textSize(textSize);
	    textAlign(CENTER, CENTER);
	    text("q", cenx, ceny  - textSize*0.2f);
	    popStyle();
	}
	
	/**
	 * Draws a camera icon with upperleft corner (x1, y1), width w, and height h.
	 * @param x1 The leftmost x-coordinate
	 * @param y1 The uppermost y-coordinate
	 * @param w The width
	 * @param h The height
	 */
	public void drawCameraIcon(float x1, float y1, float w, float h) {
	    noStroke();
	    fill(0);
	    rectMode(CORNER);
	    rect(x1 - 0.3f*w, y1 - 0.5f*h, 0.8f*w - 5, h);
	    noStroke();
	    fill(0);
	    Polygon.drawPolygon(x1 - 0.3f*w - 0.1f*w, y1, 0.2f*w, 0.2f*w, 3, 0, this);
	}
	
	/**
	 * Draws a transverse sine wave centered about (cenx, ceny) with a given length and amplitude.
	 * @param cenx The center x-coordinate
	 * @param ceny The center y-coordinate
	 * @param length The length
	 * @param amp The amplitude
	 */
	public void drawSineWave(float cenx, float ceny, float length, float amp) {
		drawSineWave(cenx, ceny, length, amp, 0);
	}
	
	/**
	 * Draws a transverse sine wave centered about (cenx, ceny) with a given length, amplitude, and a starting angle, in radians, which displaces the wave.
	 * @param cenx The center x-coordinate
	 * @param ceny The center y-coordinate
	 * @param length The length
	 * @param amp The amplitude
	 * @param startAngle The starting angle, in radians
	 */
	public void drawSineWave(float cenx, float ceny, float length, float amp, float startAngle) {
		int numPts = 100;
		float dTheta = TWO_PI / numPts;
		float theta = startAngle;
		float radius = length / 2f;
		float x = cenx - radius;
		float dx = length / numPts;
		
		for (int i=0; i<numPts; i++) {
			point(x, ceny + amp*sin(theta));
			x += dx;
			theta += dTheta;
		}
	}
	
	/*****************************
	 ***** Utility Functions *****
	 *****************************/
	
	//TODO: implement this function
	public static void phraseToMidiFile(String location, String name) {
		SCScore score = new SCScore();
		//THE IMPORTANT PART GOES HERE
		score.writeMidiFile(location + "/" + name + ".mid");
	}
	
	/**
	 * A generalization of the other remainder function.
	 * In normal modulo arithmetic, the upper number is constrained.
	 * This is a generalization of that where the lower number can also be constrained.
	 * @param num
	 * @param min
	 * @param max
	 * @return
	 */
	public static int remainder(int num, int min, int max) {
		return remainder(num - min, max-min) + min;
	}
	
	/**
	 * Calculates the remainder of num / denom.
	 * @param num The numerator
	 * @param denom The denominator
	 * @return The remainder of num / denom
	 */
	public static int remainder(int num, int denom) {
		if (0 <= num && num < denom) return num;
	    else if (num > 0) return num % denom;
	    else return (denom - ((-num) % denom)) % denom;
	}
	
	/**
	 * Calculates the remainder of num / denom.
	 * @param num The numerator
	 * @param denom The denominator
	 * @return The remainder of num / denom
	 */
	public static float remainder(float num, float denom) {
		if (0 <= num && num < denom) return num;
		else if (num > 0) return num % denom;
		else return denom - ((-num) % denom);
	}
	
	/*******************************
	 ***** Getters and Setters *****
	 ******************************/
	
	/**
	 * Returns the scale matching the given root name and scale name, or null if no scale matches.
	 * 
	 * @param root The name of the root (e.g. "A", "Bb", ...).
	 * @param scaleName The name of the scale (e.g. "Major", "Minor Pentatonic", ...).
	 * @return The matching scale, or null if no scale matches.
	 */
	public Scale getScale(String root, String scaleName) {
		for (String name : scaleTypes) {
			if (name.equals(scaleName)) {
				ScaleSet ss = scaleSets.get(name);
				for (int i=0; i<ss.numScales(); i++) {
					String scaleRootName = ss.getScale(i).getNoteNameByIndex(0);
					
					if (noteNamesAreEquivalent(root, scaleRootName)) {
						return ss.getScale(i);
					}
				}
				break;
			}
		}
		return null;
	}
	
	/**
	 * Helper to recognize the equivalence between two strings that refer to the same pitch.
	 * 
	 * The first string, doubleName, should be one of two things. If it represents a note with an accidental--
	 * for example "A#"--then it will refer to both names i.e. "A#/Bb". If it represents a note without
	 * an accidental--"C" for example-- then it simply be "C".
	 * 
	 * The second string should only ever contain one name. So it could equal to "A#". It could equal "Bb".
	 * But it could not equal "A#/Bb".
	 * 
	 * @param doubleName 
	 * @param singleName
	 * 
	 * @return True, if the two strings represent equivalent pitches, false otherwise.
	 */
	private static boolean noteNamesAreEquivalent(String doubleName, String singleName) {
		return (singleName.length() > 1 && doubleName.contains(singleName)) || doubleName.equals(singleName);
	}
	
	/**
	 * 
	 * @return color 1 of the program-wide color scheme
	 */
	public static int getColor1() {
		return colorScheme.color1;
	}
	
	/**
	 * 
	 * @return The bold version of color 1 of the program-wide color scheme
	 */
	public static int getColor1Bold() {
		return colorScheme.color1Bold;
	}
	
	/**
	 * 
	 * @return The very bold version of color 1 of the program-wide color scheme
	 */
	public static int getColor1VeryBold() {
		return colorScheme.color1VeryBold;
	}
	
	/**
	 * 
	 * @return color 2 of the program-wide color scheme
	 */
	public static int getColor2() {
		return colorScheme.color2;
	}
	
	/**
	 * 
	 * @return The bold version of color 2 of the program-wide color scheme
	 */
	public static int getColor2Bold() {
		return colorScheme.color2Bold;
	}
	
	/**
	 * 
	 * @return The very bold version of color 2 of the program-wide color scheme
	 */
	public static int getColor2VeryBold() {
		return colorScheme.color2VeryBold;
	}
	
	/**
	 * 
	 * @return The color halfway in between color 1 and color 2 of the program-wide color scheme
	 */
	public static int getBlendedColor() {
		return colorScheme.blendedColor;
	}
	
	/**
	 * 
	 * @return The color halfway in between color of color 1 bold and color 2 bold of the program-wide color scheme
	 */
	public static int getBlendedColorBold() {
		return colorScheme.blendedColorBold;
	}
	
	/**
	 * 
	 * @param amt The lerp amt, a value between 0 and 1.
	 * @return A color in between color 1 and color 2 of the program-wide color scheme
	 */
	public static int getBlendedColor(float amt) {
		return lerpColor(colorScheme.color1, colorScheme.color2, amt, PApplet.RGB);
	}
	
	/**
	 * 
	 * @param amt The lerp amt, a value between 0 and 1.
	 * @return A color in between color 1 and color 2 of the program-wide color scheme
	 */
	public static int getBlendedColorBold(float amt) {
		return PApplet.lerpColor(colorScheme.color1Bold, colorScheme.color2Bold, amt, PApplet.RGB);
	}
	
	/**
	 * 
	 * @return The beats per minute of the first piano player
	 */
	public float getBPM1() {
		return bpm1;
	}
	
	/**
	 * 
	 * @return The beats per millisecond of the first piano player
	 */
	public float getBPMS1() {
		return bpms1;
	}
	
	/**
	 * 
	 * @return The beats per minute of the second piano player
	 */
	public float getBPM2() {
		return bpm2;
	}
	
	/**
	 * 
	 * @return The beats per millisecond of the second piano player
	 */
	public float getBPMS2() {
		return bpms2;
	}
	
	/**
	 * Sets the beats per minute of the first piano player
	 * @param bpm1
	 */
	public void setBPM1(float bpm1) {
		this.bpm1 = bpm1;
		this.bpms1 = bpm1 / 60000f;
	}
	
	/**
	 * Sets the beats per minute of the second piano player
	 * @param bpm2
	 */
	public void setBPM2(float bpm2) {
		this.bpm2 = bpm2;
		this.bpms2 = bpm2 / 60000f;
	}
	
	/**
	 * Gives the lowermost y-coordinate of the change screen button.
	 * @return The lowermost y-coordinate of the change screen button.
	 */
	public float getChangeScreenButtonY2() {
		return (changeScreenButton != null) ? Util.getY2(changeScreenButton) : changeScreenButtonY2();
	}
	
	/**
	 * Sets the lowermost y-coordinate of the change screen button.
	 * @param y2 The new value for the lowermost y-coordinate of the change screen button.
	 */
	public void setChangeScreenButtonY2(float y2) {
		float currX1 = changeScreenButton.getPosition()[0];
		float currY1 = changeScreenButton.getPosition()[1];
		float currY2 = currY1 + changeScreenButton.getHeight();
		float newY1 = currY1 + (y2 - currY2);
		changeScreenButton.setPosition(currX1, newY1);
	}
	
	/**
	 * Gives the rightmost x-coordinate of the change screen button.
	 * @return The rightmost x-coordinate of the change screen button.
	 */
	public float getChangeScreenButtonX2() {
		return (changeScreenButton != null) ? Util.getX2(changeScreenButton) : changeScreenButtonX2();
	}
	
	/**
	 * Gives the width of the change screen button.
	 * @return The width of the change screen button.
	 */
	public float getChangeScreenButtonHeight() {
		return changeScreenButton.getHeight();
	}
	
	/**
	 * Data container for a color scheme.
	 * 
	 * @author James Morrow
	 *
	 */
	//TODO: Make ColorSchemes possess a light version of each color (color 1, color 2, and the blended color)
	private class ColorScheme {
		final int color1, color2, color1Bold, color2Bold, color1VeryBold, color2VeryBold;
		final int blendedColor, blendedColorVeryBold, blendedColorBold;

		private ColorScheme(int color1, int color2, int color1Bold, int color2Bold, int color1VeryBold, int color2VeryBold) {
			this.color1 = color1;
			this.color2 = color2;
			this.color1Bold = color1Bold;
			this.color2Bold = color2Bold;
			this.color1VeryBold = color1VeryBold;
			this.color2VeryBold = color2VeryBold;
			blendedColor = lerpColor(color1, color2, 0.5f);
			blendedColorBold = lerpColor(color1Bold, color2Bold, 0.5f);
			blendedColorVeryBold = lerpColor(color1VeryBold, color2VeryBold, 0.5f);
		}
	}
}