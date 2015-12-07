package phases;

import arb.soundcipher.SCScore;
import geom.Rect;
import processing.core.PApplet;
import views.GHView;
import views.View;

public class PhasesPApplet extends PApplet {
	boolean playing;
	//time
	long prev_t;
	//music
	Phrase phrase;
	int bpm1 = 80;
	float bpms1 = bpm1 / 60000f;
	int bpm2 = 82;
	float bpms2 = bpm2 / 60000f;
	//playback
	SCScore player1 = new SCScore();
	SCScore player2 = new SCScore();
	//views
	View[] views = new View[4];
	
	public void settings() {
		size(800, 600);
	}
	
	public void setup() {
		phrase = new Phrase(new float[] {64, 66, 71, 73, 74, 66, 64, 73, 71, 66, 74, 73},
				            new float[] {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50},
				            new float[] {0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f});
		
		views[0] = new GHView(new Rect(0, 0, width/2f, height/2f, PApplet.CORNER), phrase, this);

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
		line(width/2f, 0, width/2f, height);
		line(0, height/2f, width, height/2f);
		
		for (int i=0; i<views.length; i++) {
			if (views[i] != null) {
				views[i].update(dBeatpt1, dBeatpt2);
			}
		}
	}
}