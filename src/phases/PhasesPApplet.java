package phases;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import processing.core.PApplet;
import processing.core.PFont;
import processing.data.JSONObject;

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
	public Scale scale;
	private float bpm1 = 60;
	private float bpms1 = bpm1 / 60000f;
	private float bpm2 = 65;
	private float bpms2 = bpm2 / 60000f;
	
	//all screens
	private Presenter presenter;
	private Editor editor;
	
	//active screen
	private Screen currentScreen;
	
	//visual variables
	private static int color1, color2;
	public static PFont pfont12, pfont18;
	
	//gui
	private ControlP5 cp5;
	private Button changeScreenButton;
	
	/**
	 * Sets up the size of the canvas/window
	 */
	public void settings() {
		size(800, 600);
	}
	
	/**
	 * Loads JSON scale data
	 * Creates a default phrase.
	 * Creates an instance of an Editor screen and an instance of a Presenter screen.
	 * Sets the current screen.
	 */
	public void setup() {
		//init colors
		color1 = color(255, 100, 100);
		color2 = color(100, 100, 255);
		
		//init font variables
		pfont12 = loadFont("DejaVuSans-12.vlw");
		pfont18 = loadFont("DejaVuSans-18.vlw");
		
		//init controlp5
	    cp5 = new ControlP5(this);
		
		//init change screen button
		changeScreenButton = cp5.addButton("changeScreen")
							    .setPosition(10, 5)
							    .setSize(125, 40)
							    ;
		changeScreenButton.getCaptionLabel().toUpperCase(false);
		changeScreenButton.getCaptionLabel().setFont(pfont18);
		colorController(changeScreenButton);
		
		//load scales
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
		
		//create default phrase
		int n = Phrase.NOTE_START;
		phrase = new Phrase(new float[] {64, 66, 71, 73, 74, 66, 64, 73, 71, 66, 74, 73},
				            new float[] {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50},
				            new int[] {n, n, n, n, n, n, n, n, n, n, n, n});
		
		scale = this.getScale("C", "Chromatic"); //default scale
		
		//create screens
		presenter = new Presenter(this);
		editor = new Editor(this);
		
		//setup current screen
		currentScreen = editor;
		currentScreen.onEnter();
		
		if (currentScreen == editor) {
			changeScreenButton.setCaptionLabel("Perform");
		}
		else if (currentScreen == presenter) {
			changeScreenButton.setCaptionLabel("Compose");
		}
	}
	
	private void colorController(Controller c) {
		c.setColorCaptionLabel(color(255));
	    c.setColorValueLabel(color(255));
		c.setColorBackground(color(PhasesPApplet.getColor1()));
		c.setColorActive(getColor2());
		c.setColorForeground(0);
	}
	
	private void testGetScale() {
		for (int i=0; i<roots.length; i++) {
			for (int j=0; j<scaleTypes.size(); j++) {
				Scale s = getScale(roots[i], scaleTypes.get(j));
				if (s == null) {
					println("error: scale is null");
				}
			}
		}
	}
	
	/**
	 * Callback from ControlP5
	 * Controls changing the screen from Presentation screen to Editor screen and vice versa
	 * @param e
	 */
	public void changeScreen(ControlEvent e) {
		currentScreen.onExit();
		if (currentScreen == editor) {
			currentScreen = presenter;
			changeScreenButton.setCaptionLabel("Compose");
		}
		else if (currentScreen == presenter) {
			currentScreen = editor;
			changeScreenButton.setCaptionLabel("Perform");
		}
		currentScreen.onEnter();
	}
	
	/**
	 * Lets the current screen draw itself.
	 */
	public void draw() {
		currentScreen.draw();
	}
	
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
	
	public Scale getScale(String root, String scaleName) {
		for (String name : scaleTypes) {
			if (name.equals(scaleName)) {
				ScaleSet ss = scaleSets.get(name);
				for (int i=0; i<ss.numScales(); i++) {
					String scaleRootName = ss.getScale(i).getNoteName(0);
					
					if (noteNamesAreEquivalent(root, scaleRootName)) {
						return ss.getScale(i);
					}
				}
				break;
			}
		}
		return null;
	}
	
	private boolean noteNamesAreEquivalent(String root, String scaleRootName) {
		return (scaleRootName.length() > 1 && root.contains(scaleRootName)) || root.equals(scaleRootName);
	}
	
	/**
	 * 
	 * @return color 1 of the program-wide color scheme
	 */
	public static int getColor1() {
		return color1;
	}
	
	/**
	 * 
	 * @return color2 of the program-wide color scheme
	 */
	public static int getColor2() {
		return color2;
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
		else return denom - ((-num) % denom);
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
}