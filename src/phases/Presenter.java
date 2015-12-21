package phases;

import geom.Rect;
import processing.core.PApplet;
import views.CircularView;
import views.KeyboardsView;
import views.LiveGraphView;
import views.RhythmView;
import views.View;

public class Presenter extends Screen {	
	//time
	private float prev_notept1, prev_notept2;
	//playback
	private SCScorePlus player1 = new SCScorePlus();
	private SCScorePlus player2 = new SCScorePlus();
	private boolean playing;
	private int sign;
	//views
	public static int color1, color2;
	private Rect[] viewFrames;
	private View[] views = new View[4];
	
	public Presenter(PhasesPApplet pa) {
		super(pa);
		
		viewFrames = new Rect[] {
				new Rect(0, 0, pa.width/2f, pa.height/2f, PApplet.CORNER),
				new Rect(pa.width/2f, 0, pa.width/2f, pa.height/2f, PApplet.CORNER),
				new Rect(0, pa.height/2f, pa.width/2f, pa.height/2f, PApplet.CORNER),
				new Rect(pa.width/2f, pa.height/2f, pa.width/2f, pa.height/2f, PApplet.CORNER)
		};
		
		color1 = pa.color(255, 100, 100);
		color2 = pa.color(100, 100, 255);
		
		//views[0] = new GHView(viewFrames[0], pa.phrase, color1, color2, 100, pa);
		
		views[0] = new CircularView(viewFrames[0], pa.phrase, color1, color2, 100, pa, 50, 75);
		
		views[3] = new LiveGraphView(viewFrames[3], pa.phrase, color1, color2, 175, pa);
		
		views[1] = new RhythmView(viewFrames[1], pa.phrase, color1, color2, 175, pa);
		
		//views[1] = new WavesView(viewFrames[1], pa.phrase, color1, color2, 150, pa);
		
		views[2] = new KeyboardsView(viewFrames[2], pa.phrase, color1, color2, 100, pa);
		
		//views[3] = new SymbolicView(viewFrames[3], pa.phrase, color1, color2, 175, pa);
	}
	
	@Override
	public void onEnter() {
		pa.phrase.addToScore(player1, 0, 0, 0);
		pa.phrase.addToScore(player2, 0, 0, 0);
		player1.tempo(pa.getBPM1());
		player2.tempo(pa.getBPM2());
		player1.repeat(-1);
		player2.repeat(-1);
		player1.play();
		player2.play();
		
		playing = true;
		
		if (pa.getBPM1() < pa.getBPM2()) {
			sign = 1;
		}
		else if (pa.getBPM1() > pa.getBPM2()) {
			sign = -1;
		}
		else {
			sign = 0;
		}
		
		prev_notept1 = 0;
		prev_notept2 = 0;
	}
	
	@Override
	public void onExit() {
		player1.stop();
		player2.stop();
	}
	
	@Override
	public void draw() {
		//time
		float notept1 = PApplet.map(player1.getTickPosition(),
				                    0, player1.getTickLength(),
				                    0, pa.phrase.getTotalDuration());
		float notept2 = PApplet.map(player2.getTickPosition(),
				                    0, player2.getTickLength(),
				                    0, pa.phrase.getTotalDuration());
		
		float dNotept1 = notept1 - prev_notept1;
		float dNotept2 = notept2 - prev_notept2;
		
		if (dNotept1 < 0) {
			dNotept1 += pa.phrase.getTotalDuration();
		}
		
		if (dNotept2 < 0) {
			dNotept2 += pa.phrase.getTotalDuration();
		}
		
		prev_notept1 = notept1;
		prev_notept2 = notept2;

		//drawing
		pa.background(255);

		for (int i=0; i<views.length; i++) {
			if (views[i] != null) {
				views[i].update(dNotept1, dNotept2, sign);
			}
		}
		
		pa.strokeWeight(2);
		pa.stroke(0);
		pa.line(pa.width/2f, 0, pa.width/2f, pa.height);
		pa.line(0, pa.height/2f, pa.width, pa.height/2f);
	}
	
	@Override
	public void mousePressed() {
		for (View v : views) {
			if (v != null) {
				if (v.intersects(pa.mouseX, pa.mouseY)) {
					v.mousePressedInArea();
					break;
				}
			}
		}
	}
}
