package phases;

import geom.Rect;
import processing.core.PApplet;
import views.GHView;
import views.KeyboardsView;
import views.SymbolicView;
import views.View;
import views.WavesView;

/**
 * 
 * @author James Morrow
 *
 */
public class PhasesPApplet extends PApplet {
	public Phrase phrase;
	public SCScorePlus player1 = new SCScorePlus();
	public SCScorePlus player2 = new SCScorePlus();
	
	public int bpm1 = 90;
	public float bpms1 = bpm1 / 60000f;
	public int bpm2 = 80;
	public float bpms2 = bpm2 / 60000f;
	
	private Presenter presenter;
	private Editor editor;
	private Screen currentScreen;
	
	/**
	 * Sets up the size of the canvas/window
	 */
	public void settings() {
		size(800, 600);
	}
	
	/**
	 * Creates a default phrase.
	 * Creates an instance of an Editor screen and an instance of a Presenter screen.
	 * Sets the current screen.
	 */
	public void setup() {
		int n = Phrase.NOTE_START;
		phrase = new Phrase(new float[] {64, 66, 71, 73, 74, 66, 64, 73, 71, 66, 74, 73},
				            new float[] {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50},
				            new int[] {n, n, n, n, n, n, n, n, n, n, n, n});
		
		phrase.addToScore(player1, 0, 0, 0);
		phrase.addToScore(player2, 0, 0, 0);
		player1.tempo(bpm1);
		player2.tempo(bpm2);
		player1.repeat(-1);
		player2.repeat(-1);
		
		
		presenter = new Presenter(this);
		editor = new Editor(this);
		currentScreen = editor;
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
}