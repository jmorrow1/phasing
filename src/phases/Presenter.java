package phases;

import geom.Rect;
import processing.core.PApplet;
import views.LiveScorer;
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
	
	//view
	private View view;

	//minimap
	private Minimap minimap;
	
	//megamap
	private Megamap megamap;
	
	/**
	 * 
	 * @param pa The PhasesPApplet on which to draw views
	 */
	public Presenter(PhasesPApplet pa) {
		super(pa);
	}
	
	@Override
	public void onEnter() {
		//view = new PhaseShifter(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		//view = new Musician(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		view = new LiveScorer(new Rect(0, 0, pa.width, pa.height, pa.CORNER), 150, pa);
		
		minimap = new Minimap(pa.width - 100, pa.height - 80, 75, 50, 85, 60, 16, view);
		//megamap = new Megamap(pa.width/2f, pa.height/2f, 16, view);
		
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
		
		//setupViewGraph();
	}
	
	@Override
	public void onExit() {
		player1.stop();
		player2.stop();
	}
	
	@Override
	public void draw() {
		pa.background(255);
		animateView();
		minimap.display(pa);
		//megamap.update();
		//megamap.display(pa);
	}
	
	private void animateView() {
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
		
		view.update(dNotept1, dNotept2, sign);
	}
	
	@Override
	public void mousePressed() {
		if (minimap.intersects(pa.mouseX, pa.mouseY)) {
			minimap.mousePressed(pa);
		}
	}
	
	@Override
	public void mouseMoved() {
	}
}
