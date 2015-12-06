package phases;

import geom.Rect;
import processing.core.PApplet;
import views.GHView;
import views.View;

public class PhasesPApplet extends PApplet {
	boolean playing = true;	
	//time
	long prev_t;	
	//musical time
	int bpm1 = 80;
	float bpms1 = bpm1 / 60000;
	int bpm2 = 82;
	float bpms2 = bpm2 / 60000;
	//views
	View[] views = new View[4];
	
	public void settings() {
		size(800, 600);
		views[0] = new GHView(new Rect(0, 0, width/2f, height/2f, PApplet.CORNER), new Phrase(), this);
	}
	
	public void setup() {
		prev_t = System.currentTimeMillis();
	}
	
	public void draw() {
		//time
		long dt = System.currentTimeMillis() - prev_t;
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