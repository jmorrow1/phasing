package phases;

import processing.core.PApplet;

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
	}
	
	public void setup() {
		prev_t = millis();
	}
	
	public void draw() {
		//time
		long dt = System.currentTimeMillis() - prev_t;
		float dBeatpt1 = dt * bpms1;
		float dBeatpt2 = dt * bpms2;

		//drawing
		background(255);
		line(width/2f, 0, width/2f, height);
		line(0, height/2f, width, height/2f);
	}
}