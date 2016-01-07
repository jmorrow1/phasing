package phases;

import java.util.ArrayList;

import controlP5.Button;
import controlP5.ControlP5;
import geom.Circle;
import geom.Rect;
import processing.core.PApplet;
import views.PhaseShifter;
import views.View;

/**
 * The screen that displays views, ways of visualizing the music.
 * 
 * @author James Morrow
 *
 */
public class Presenter extends Screen {	
	//time
	private float prev_notept1, prev_notept2;
	//playback
	private SCScorePlus player1 = new SCScorePlus();
	private SCScorePlus player2 = new SCScorePlus();
	private boolean playing;
	private int sign;
	//views
	View view;
	//view graph
	private float nodeRadius;
	private Circle planet;
	private ArrayList<Circle> satellites = new ArrayList<Circle>();
	
	/**
	 * 
	 * @param pa The PhasesPApplet on which to draw views
	 */
	public Presenter(PhasesPApplet pa) {
		super(pa);
		
		view = new PhaseShifter(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		//view = new Musician(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		//view = new LiveScorer(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
	}
	
	
	
	private void setupViewGraph(float cenx, float ceny, float maxDist, int numSatellites) {
		planet = new Circle(cenx, ceny, nodeRadius);
		
		float theta = 0;
		float dTheta = PApplet.TWO_PI / numSatellites;
		for (int i=0; i<numSatellites; i++) {
			float dist = (float)Math.random() * maxDist;
			float angle = theta + PApplet.map((float)Math.random(), -1, 1, -dTheta*0.125f, dTheta*0.125f);
			satellites.add(new Circle(cenx + dist*PApplet.cos(angle),
					                  ceny + dist*PApplet.sin(angle), nodeRadius));
			theta += dTheta;
		}
	}
	
	private void drawViewGraph() {
		pa.stroke(0);
		for (Circle c : satellites) {
			pa.line(c.getX(), c.getY(), planet.getX(), planet.getY());
		}
		
		pa.fill(255);
		planet.display(pa);
		for (Circle c : satellites) {
			c.display(pa);
		}
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

		view.update(dNotept1, dNotept2, sign);
	}
	
	@Override
	public void mousePressed() {
	}
}
