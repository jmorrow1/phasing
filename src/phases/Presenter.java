package phases;

import java.lang.reflect.Field;
import java.util.ArrayList;

import geom.Rect;
import icons.Icon;
import processing.core.PApplet;
import soundcipher.SCScorePlus;
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
	
	//view
	private View view;

	ArrayList<OptionValueController> ovcs = new ArrayList<OptionValueController>();
	OptionValueController active_ovc;

	/**
	 * 
	 * @param pa The PhasesPApplet on which to draw views
	 */
	public Presenter(PhasesPApplet pa) {
		super(pa);
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
		
		setupIcons();
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
		drawIcons();
	}
	
	private void setupIcons() {
		float x = 110;
		float y = 550;
		float radius = 50;
		float dx = radius * 2.1f;
		
		Field[] fields = view.getClass().getFields();
		for (Field f : fields) {
			if(f.getType().equals(OptionValue.class)) {
				try {
					OptionValue value = (OptionValue)f.get(view);
					ovcs.add(new OptionValueController(x, y, radius, value));
					x += dx;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
		}
		
		if (ovcs.size() > 0) {
			active_ovc = ovcs.get(0);
		}
	}
	
	private void drawIcons() {
		for (OptionValueController ovc : ovcs) {
			ovc.display();
		}
		
		pa.noFill();
		pa.stroke(PhasesPApplet.getColor1());
		pa.rectMode(pa.RADIUS);
		pa.rect(active_ovc.x, active_ovc.y, active_ovc.radius, active_ovc.radius);
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
	public void mouseMoved() {
	}
	
	class OptionValueController {
		float x, y, radius;
		OptionValue value;
		Icon icon;
		
		OptionValueController(float x, float y, float radius, OptionValue value) {
			this.x = x;
			this.y = y;
			this.radius = radius;
			this.value = value;
			this.icon = Icon.init(value);
		}
		
		void display() {
			icon.draw(x, y, radius, pa);
		}
		
		void prev() {
			value.set(value.prev());
			icon.setValue(value.intValue());
		}
		
		void next() {
			value.set(value.next());
			icon.setValue(value.intValue());
		}
	}
}
