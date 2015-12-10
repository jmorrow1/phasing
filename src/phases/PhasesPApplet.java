package phases;

import arb.soundcipher.SCScore;
import geom.Rect;
import processing.core.PApplet;
import views.KeyboardsView;
import views.LiveGraphView;
import views.RhythmView;
import views.SymbolicView;
import views.View;

public class PhasesPApplet extends PApplet {
	boolean playing;
	//time
	long prev_t;
	//music
	Phrase phrase;
	int bpm1 = 40;
	double bpms1 = bpm1 / 60000f;
	int bpm2 = 42;
	double bpms2 = bpm2 / 60000f;
	//playback
	SCScore player1 = new SCScore();
	SCScore player2 = new SCScore();
	//views
	Rect[] viewFrames;
	View[] views = new View[4];
	
	public void settings() {
		size(800, 600);
	}
	
	public void setup() {
		phrase = new Phrase(new double[] {64, 66, 71, 73, 74, 66, 64, 73, 71, 66, 74, 73},
				            new double[] {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50},
				            new double[] {0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f});
		
		viewFrames = new Rect[] {
				new Rect(0, 0, width/2f, height/2f, PApplet.CORNER),
				new Rect(width/2f, 0, width/2f, height/2f, PApplet.CORNER),
				new Rect(0, height/2f, width/2f, height/2f, PApplet.CORNER),
				new Rect(width/2f, height/2f, width/2f, height/2f, PApplet.CORNER)
		};
		
		int color1 = color(255, 100, 100);
		int color2 = color(100, 100, 255);
		
		//views[0] = new GHView(viewFrames[0], phrase, GHView.DOWN, false, false, color1, color2, 100, this);
		
		views[0] = new LiveGraphView(viewFrames[0], phrase, color1, color2, 175, this);
		
		views[1] = new RhythmView(viewFrames[1], phrase, color1, color2, 175, this);
		
		//views[1] = new WavesView(viewFrames[1], phrase, color1, color2, 150, 0.45f, 0.25f, true, WavesView.LINEAR_PLOT, this);
		
		views[2] = new KeyboardsView(viewFrames[2], phrase, color1, color2, 100, true, this);
		
		views[3] = new SymbolicView(viewFrames[3], phrase, color1, color2, 175, true, this);

		phrase.addToScore(player1, 0, 0, 0);
		phrase.addToScore(player2, 0, 0, 0);
		player1.repeat(-1);
		player2.repeat(-1);
		player1.tempo(bpm1);
		player2.tempo(bpm2);
		player1.play();
		player2.play();
		
		playing = true;
		
		prev_t = System.currentTimeMillis();
	}
	
	public void draw() {
		//time
		long dt = System.currentTimeMillis() - prev_t;
		prev_t = System.currentTimeMillis();
		double dBeatpt1 = (playing) ? dt * bpms1 : 0;
		double dBeatpt2 = (playing) ? dt * bpms2 : 0;
	
		//drawing
		background(255);

		for (int i=0; i<views.length; i++) {
			if (views[i] != null) {
				views[i].update(dBeatpt1, dBeatpt2);
			}
		}
		
		strokeWeight(2);
		stroke(0);
		line(width/2f, 0, width/2f, height);
		line(0, height/2f, width, height/2f);
	}
	
	public static int remainder(int num, int denom) {
		if (0 <= num && num < denom) return num;
		else if (num > 0) return num % denom;
		else return denom - ((-num) % denom);
	}
	
	public static float constrain(double amt, double low, double high) {
		return constrain((float)amt, (float)low, (float)high);
	}

	public void ellipse(double a, double b, double c, double d) {
		ellipse((float)a, (float)b, (float)c, (float)d);
	}
	
	public void fill(double gray) {
		fill((int)gray);
	}
	
	public void fill(double gray, double alpha) {
		fill((int)gray, (float)alpha);
	}
	
	public void fill(double v1, double v2, double v3) {
		fill((int)v1, (int)v2, (int)v3);
	}
	
	public void fill(double v1, double v2, double v3, double alpha) {
		fill((int)v1, (int)v2, (int)v3, (float)alpha);
	}
	
	public static double lerp(double a, double b, double amt) {
		return lerp((float)a, (float)b, (float)amt);
	}
	
	public static double map(double value, double low1, double high1, double low2, double high2) {
		return map((float)value, (float)low1, (float)high1, (float)low2, (float)high2);
	}
	
	public void rect(double a, double b, double c, double d) {
		rect((float)a, (float)b, (float)c, (float)d);
	}
	
	public void text(String s, double x, double y) {
		text(s, (float)x, (float)y);
	}
	
	public void text(char c, double x, double y) {
		text(c, (float)x, (float)y);
	}
	
	public void textAlign(double a, double b) {
		textAlign((float)a, (float)b);
	}
	
	public void vertex(double x, double y) {
		vertex((float)x, (float)y);
	}
}