package phases;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.data.JSONObject;

/**
 * 
 * @author James Morrow
 *
 */
public class PhasesPApplet extends PApplet {
	private static ArrayList<ScaleSet> scaleSets = new ArrayList<ScaleSet>();
	public static ScaleSet chromaticScales;
	
	public Phrase phrase;
	public Scale scale;

	public static final float MIN_BPM = 20, MAX_BPM = 160;
	private float bpm1 = 60;
	private float bpms1 = bpm1 / 60000f;
	private float bpm2 = 62;
	private float bpms2 = bpm2 / 60000f;
	
	private Presenter presenter;
	private Editor editor;
	private Screen currentScreen;
	
	private static int color1, color2;
	
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
		//load scales
		try {
			int i=0;
			Files.walk(Paths.get("src/data/scales")).forEach(filePath -> {
				if (filePath.toString().endsWith(".json")) {
					JSONObject json = loadJSONObject(filePath.toString());
					ScaleSet ss = new ScaleSet(json);
					scaleSets.add(ss);
					if (ss.getName().equals("Chromatic")) {
						chromaticScales = ss;
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		color1 = color(255, 100, 100);
		color2 = color(100, 100, 255);
		
		//create default phrase
		int n = Phrase.NOTE_START;
		phrase = new Phrase(new float[] {64, 66, 71, 73, 74, 66, 64, 73, 71, 66, 74, 73},
				            new float[] {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50},
				            new int[] {n, n, n, n, n, n, n, n, n, n, n, n});
		
		scale = chromaticScales.getScale(0); //default scale
		
		//create screens
		presenter = new Presenter(this);
		editor = new Editor(this);
		
		//setup current screen
		currentScreen = presenter;
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
	
	public static int getNumScaleSets() {
		return scaleSets.size();
	}
	
	public static ScaleSet getScaleSet(int i) {
		if (0 <= i && i < scaleSets.size()) {
			return scaleSets.get(i);
		}
		return null;
	}
	
	public static ScaleSet[] getScaleSets() {
		return scaleSets.toArray(new ScaleSet[] {});
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