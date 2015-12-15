package phases;

import geom.Rect;
import processing.core.PApplet;
import views.GHView;
import views.KeyboardsView;
import views.SymbolicView;
import views.View;
import views.WavesView;

public class PhasesPApplet extends PApplet {
	public Phrase phrase;
	public int bpm1 = 90;
	public float bpms1 = bpm1 / 60000f;
	public int bpm2 = 80;
	public float bpms2 = bpm2 / 60000f;
	
	private Presenter presenter;
	private Editor editor;
	private Screen currentScreen;
	
	public void settings() {
		size(800, 600);
	}
	
	public void setup() {
		int n = Phrase.NOTE_START;
		phrase = new Phrase(new float[] {64, 66, 71, 73, 74, 66, 64, 73, 71, 66, 74, 73},
				            new float[] {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50},
				            new int[] {n, n, n, n, n, n, n, n, n, n, n, n});
		
		presenter = new Presenter(this);
		editor = new Editor(this);
		currentScreen = editor;
		currentScreen.onEnter();
	}
	
	public void draw() {
		currentScreen.draw();
	}
	
	public void mousePressed() {
		currentScreen.mousePressed();
	}
	
	public void mouseReleased() {
		currentScreen.mouseReleased();
	}
	
	public void mouseDragged() {
		currentScreen.mouseDragged();
	}
	
	public void mouseMoved() {
		currentScreen.mouseMoved();
	}
	
	public void keyPressed() {
		currentScreen.keyPressed();
	}
	
	public void keyReleased() {
		currentScreen.keyReleased();
	}
	
	public static int remainder(int num, int denom) {
		if (0 <= num && num < denom) return num;
		else if (num > 0) return num % denom;
		else return denom - ((-num) % denom);
	}
}