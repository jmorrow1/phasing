package phasing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import arb.soundcipher.SCScore;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
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

/**
 * 
 * @author James Morrow
 *
 */
public class PhasesPApplet extends PApplet {
	//music parameters
	public static final float MIN_BPM = 1, MAX_BPM = 100;
	
	//all music variables
	public final static String[] roots = new String[] {"A", "A#/Bb", "B", "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab"};
	public static ArrayList<String> scaleTypes = new ArrayList<String>();
	private static Map<String, ScaleSet> scaleSets = new HashMap<String, ScaleSet>();
	
	//active music variables
	public Phrase phrase;
	public PhrasePicture phrasePicture;
	public Scale scale;
	private float bpm1 = 60;
	private float bpms1 = bpm1 / 60000f;
	private float bpm2 = 60.5f;
	private float bpms2 = bpm2 / 60000f;
	
	//all screens
	private Presenter presenter;
	private Editor editor;
	private PhraseRepository phraseRepo;
	
	//active screen
	private Screen currentScreen;
	
	//visual variables
	private static ColorScheme colorScheme;
	public static PFont pfont12, pfont18, pfont42, musicFont;
	public int changeScreenButtonY2 = 50;
	public int changeScreenButtonX2 = 135;
	
	//gui
	private ControlP5 cp5;
	private Button changeScreenButton;
	
	//screen size
	public static final int _800x600 = 0, _1366x768 = 1, _1920x1080 = 2, _1024x768 = 3, _1280x800 = 4, _1280x1024 = 5;
	public int screenSizeMode = _800x600;
	
	//save folder location
	public String saveFolderPath;
	
	//player
	public PlayerInfo playerInfo;
	
	/****************
	***** Setup *****
	*****************/
	
	/**
	 * Sets up the size of the canvas/window
	 */
	public void settings() {
		setSize(screenSizeMode);
	}
	
	/**
	 * Sets the size of the canvas/window according to a given integer code.
	 * 
	 * @param screenSizeMode An integer code that represents a screen size.
	 */
	private void setSize(int screenSizeMode) {
		switch (screenSizeMode) {
			case _800x600 : size(800, 600); break;
			case _1366x768 : size(1366, 768); break;
			case _1920x1080 : size(1920, 1080); break;
			case _1024x768 : size(1024, 768); break;
			case _1280x800 : size(1280, 800); break;
			case _1280x1024 : size(1280, 1024); break;
		}
	}
	
	/**
	 * Loads JSON scale data
	 * Creates a default phrase.
	 * Creates an instance of an Editor screen and an instance of a Presenter screen.
	 * Sets the current screen.
	 */
	public void setup() {
		saveFolderPath = sketchPath() + "\\save\\";
		//init colors
		initColorScheme();
		//initSimpleColorScheme();
		
		//init font variables
		pfont12 = loadFont("DejaVuSans-12.vlw");
		pfont18 = loadFont("DejaVuSans-18.vlw");
		pfont42 = loadFont("DejaVuSans-42.vlw");
		musicFont = loadFont("MaestroWide-48.vlw");
		
		//init controlp5
	    cp5 = new ControlP5(this);
		
		//init change screen button
		changeScreenButton = cp5.addButton("changeScreen")
							    .setPosition(changeScreenButtonX2 - 125, changeScreenButtonY2 - 40)
							    .setSize(125, 40)
							    ;
		changeScreenButton.getCaptionLabel().toUpperCase(false);
		changeScreenButton.getCaptionLabel().setFont(pfont18);
		
		//load scales
		loadScales();
		
		//load player info
		initPlayerInfo(false);
		
		//create phrase
		scale = getRandomScale();
		phrase = generateReichLikePhrase(scale);
		phrasePicture = new PhrasePicture(phrase, "Current Phrase", this);
	
		//create screens
		presenter = new Presenter(this);
		editor = new Editor(this);
		phraseRepo = new PhraseRepository(this);
		
		//setup current screen
		currentScreen = phraseRepo;
		
		if (currentScreen == editor) {
			changeScreenButton.setCaptionLabel("Rehearse");
		}
		else if (currentScreen == presenter) {
			changeScreenButton.setCaptionLabel("Compose");
		}
		else if (currentScreen == phraseRepo) {
			changeScreenButton.setCaptionLabel("Go Back");
		}
		
		colorButtonShowLabel(changeScreenButton);
		
		currentScreen.onEnter();
	}
	
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
	
	private void initPlayerInfo(boolean loadFile) {
		String playerInfoFileName = saveFolderPath + "playerInfo.json";
		File playerInfoFile = new File(playerInfoFileName);
		if (loadFile && playerInfoFile.exists()) {
			JSONObject json = loadJSONObject(playerInfoFileName);
			playerInfo = new PlayerInfo(json);
		}
		else {
			playerInfo = new PlayerInfo(true);
			saveJSONObject(playerInfo.toJSON(), playerInfoFileName);
		}
	}
	
	/**
	 * Saves the player info to a file in the save folder.
	 */
	public void savePlayerInfo() {
		JSONObject json = playerInfo.toJSON();
		try {
			saveJSONObject(json, saveFolderPath);
		} catch (RuntimeException e) {
			print(e.toString());
		}
	}
	
	/**
	 * Saves a list of phrase pictures to the phrases subfolder in the save folder.
	 * @param phrasePictures
	 */
	public void savePhrasePictures(ArrayList<PhrasePicture> phrasePictures) {
		for (int i=0; i<phrasePictures.size(); i++) {
			PhrasePicture p = phrasePictures.get(i);
			saveJSONObject(p.toJSON(), saveFolderPath + "phrases\\" + p.getName() + ".json");
		}
	}
	
	/**
	 * Initializes a red / blue color scheme.
	 */
	private void initColorScheme() {
		colorMode(HSB, 360, 100, 100, 100);
		int color1 = color(0, 60, 90);
		int color1Bold = color(0, 100, 90);
		int color2 = color(240, 60, 90);
		int color2Bold = color(240, 100, 90);
		colorMode(RGB, 255, 255, 255, 255);
		colorScheme = new ColorScheme(color1, color2, color1Bold, color2Bold);
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
		
		colorScheme = new ColorScheme(color1, color2, color1Bold, color2Bold);
	}
	
	/**************************
	***** Color Loading *******
	***************************/
	
	public int loadColor(JSONObject json) {
		pushStyle();
		colorMode(HSB, 360, 100, 100, 100);
		int color = color(json.getInt("hue"), json.getInt("saturation"), json.getInt("brightness"), json.getInt("opacity"));
		popStyle();
		return color;
	}
	
	/****************************
	***** Phrase Generation *****
	*****************************/	
	
	/**
	 * 
	 * @return The score from Steve Reich's Piano Phase (1967)
	 */
	public Phrase pianoPhase() {
		int n = Phrase.NOTE_START;
		return phrase = new Phrase(new float[] {64, 66, 71, 73, 74, 66, 64, 73, 71, 66, 74, 73},
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
	
	/**************************
	***** ControlP5 Style *****
	***************************/
	
	public static void colorButtonShowLabel(Controller c) {
		c.setColorCaptionLabel(0xffffffff);
	    c.setColorValueLabel(0xffffffff);
		c.setColorBackground(getColor1());
		c.setColorForeground(getColor1Bold());
		c.setColorActive(getColor1Bold());	
	}
	
	public void colorButtonHideLabel(Button b) {
		b.setColorBackground(color(255));
	    b.setColorForeground(getColor1());
	    b.setColorActive(getColor1Bold());
	}
	
	/**********************************
	***** Callback from ControlP5 *****
	***********************************/
	
	/**
	 * Callback from ControlP5
	 * Controls changing the screen from Presentation screen to Editor screen and vice versa
	 * @param e
	 */
	public void changeScreen(ControlEvent e) {
		currentScreen.onExit();
		changeScreenButton.show();
		if (currentScreen == editor) {
			currentScreen = presenter;
			changeScreenButton.setCaptionLabel("Compose");
		}
		else if (currentScreen == presenter) {
			currentScreen = editor;
			changeScreenButton.setCaptionLabel("Rehearse");
		}
		else if (currentScreen == phraseRepo) {
			changeScreenButton.setCaptionLabel("Go Back");
		}
		currentScreen.onEnter();
	}
	
	/********************
	***** Draw Loop *****
	*********************/
	
	/**
	 * Sends a message to the current screen to draw itself.
	 */
	public void draw() {
		currentScreen.draw();
	}
	
	/*******************************
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
	 * Sends key pressed events to the current screen.
	 */
	public void keyPressed() {
		currentScreen.keyPressed();
	}
	
	/**
	 * Sends key released events to the current screen.
	 */
	public void keyReleased() {
		currentScreen.keyReleased();
	}
	
	public void mouseWheel(MouseEvent event) {
		currentScreen.mouseWheel(event);
	}
	
	/***********************************************
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
	
	/****************************
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
	
	/******************************
	***** Getters and Setters *****
	******************************/
	
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
	
	private static boolean noteNamesAreEquivalent(String root, String scaleRootName) {
		return (scaleRootName.length() > 1 && root.contains(scaleRootName)) || root.equals(scaleRootName);
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
		bpm1 = constrain(bpm1, MIN_BPM, MAX_BPM);
		this.bpm1 = bpm1;
		this.bpms1 = bpm1 / 60000f;
	}
	
	/**
	 * Sets the beats per minute of the second piano player
	 * @param bpm2
	 */
	public void setBPM2(float bpm2) {
		bpm2 = constrain(bpm2, MIN_BPM, MAX_BPM);
		this.bpm2 = bpm2;
		this.bpms2 = bpm2 / 60000f;
	}
	
	/**
	 * Data container for a color scheme.
	 * 
	 * @author James Morrow
	 *
	 */
	private class ColorScheme {
		final int color1, color2, color1Bold, color2Bold, blendedColor, blendedColorBold;

		ColorScheme(int color1, int color2, int color1Bold, int color2Bold) {
			this.color1 = color1;
			this.color2 = color2;
			this.color1Bold = color1Bold;
			this.color2Bold = color2Bold;
			blendedColor = lerpColor(color1, color2, 0.5f);
			blendedColorBold = lerpColor(color1Bold, color2Bold, 0.5f);
		}
	}
}