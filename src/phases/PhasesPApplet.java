package phases;

import arb.soundcipher.SCScore;
import geom.Rect;
import processing.core.PApplet;
import views.KeyboardsView;
import views.SymbolicView;
import views.View;
import views.WavesView;

public class PhasesPApplet extends PApplet {
	boolean playing;
	//time
	long prev_t;
	//music
	Phrase phrase;
	int bpm1 = 80;
	float bpms1 = bpm1 / 60000f;
	int bpm2 = 86;
	float bpms2 = bpm2 / 60000f;
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
		phrase = new Phrase(new float[] {64, 66, 71, 73, 74, 66, 64, 73, 71, 66, 74, 73},
				            new float[] {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50},
				            new float[] {0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f});
		
		viewFrames = new Rect[] {
				new Rect(0, 0, width/2f, height/2f, PApplet.CORNER),
				new Rect(width/2f, 0, width/2f, height/2f, PApplet.CORNER),
				new Rect(0, height/2f, width/2f, height/2f, PApplet.CORNER),
				new Rect(width/2f, height/2f, width/2f, height/2f, PApplet.CORNER)
		};
		
		int color1 = color(255, 100, 100);
		int color2 = color(100, 100, 255);
		
		//views[0] = new GHView(viewFrames[0], phrase, GHView.DOWN, false, false, color1, color2, 100, this);
		
		views[1] = new WavesView(viewFrames[1], phrase, color1, color2, 150, 0.45f, 0.25f, true, WavesView.LINEAR_PLOT, this);
		
		views[2] = new KeyboardsView(viewFrames[2], phrase, color1, color2, 100, false, this);
		
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
		float dBeatpt1 = (playing) ? dt * bpms1 : 0;
		float dBeatpt2 = (playing) ? dt * bpms2 : 0;
	
		//drawing
		background(255);
		strokeWeight(1);
		stroke(0);
		line(width/2f, 0, width/2f, height);
		line(0, height/2f, width, height/2f);
		
		for (int i=0; i<views.length; i++) {
			if (views[i] != null) {
				views[i].update(dBeatpt1, dBeatpt2);
			}
		}
	}
	
	public static int remainder(int num, int denom) {
		if (0 <= num && num < denom) return num;
		else if (num > 0) return num % denom;
		else return denom - ((-num) % denom);
	}
}