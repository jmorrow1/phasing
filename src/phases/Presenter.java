package phases;

import java.util.ArrayList;

import geom.Circle;
import geom.Line;
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
	private Circle planet;
	private ArrayList<Line> edges = new ArrayList<Line>();
	private ArrayList<Circle> satellites = new ArrayList<Circle>();
	
	/**
	 * 
	 * @param pa The PhasesPApplet on which to draw views
	 */
	public Presenter(PhasesPApplet pa) {
		super(pa);
	}
	
	private void setupViewGraph(float cenx, float ceny, float minDist, float maxDist, int numSatellites, float nodeRadius) {
		planet = new Circle(cenx, ceny, nodeRadius);
		
		float theta = 0;
		float dTheta = PApplet.TWO_PI / numSatellites;
		for (int i=0; i<numSatellites; i++) {
			float dist = pa.random(minDist, maxDist);
			float angle = theta + PApplet.map((float)Math.random(), -1, 1, -dTheta*0.125f, dTheta*0.125f);
			
			Circle sat = new Circle(cenx + dist*PApplet.cos(angle),
	                  		ceny + dist*PApplet.sin(angle), nodeRadius);
			
			satellites.add(sat);
			
			float a1 = pa.atan2(planet.getY() - sat.getY(), planet.getX() - sat.getX());
			float a2 = a1 + pa.PI;
			edges.add(new Line(sat.getX() + pa.cos(a1) * sat.getRadius(), sat.getY() + pa.sin(a1) * sat.getRadius(),
						planet.getX() + pa.cos(a2) * planet.getRadius(), planet.getY() + pa.sin(a2) * planet.getRadius()));
			
			theta += dTheta;
		}
	}
	
	@Override
	public void onEnter() {
		view = new PhaseShifter(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		//view = new Musician(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		//view = new LiveScorer(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		
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
		
		setupViewGraph(pa.width - 85, pa.height - 85, 50, 70, 5, 14);
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
		
		drawViewGraph();
	}
	
	private void drawViewGraph() {
		pa.strokeWeight(2);
		pa.stroke(0, 150);
		pa.noFill();
		planet.display(pa);
		for (Circle c : satellites) {
			c.display(pa);
		}

		for (Line e : edges) {
			e.display(pa);
		}
	}
	
	@Override
	public void mousePressed() {
	}
}
