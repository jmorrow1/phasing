package phases;

import java.lang.reflect.Field;
import java.util.ArrayList;

import geom.Rect;
import icons.CameraIcon;
import icons.ColorSchemeIcon;
import icons.DefaultIcon;
import icons.Icon;
import icons.InstrumentIcon;
import icons.NoteIcon;
import icons.PlotPitchIcon;
import icons.ScoreModeIcon;
import icons.ShowActiveNoteIcon;
import icons.SuperimposedOrSeparatedIcon;
import icons.TransformIcon;
import processing.core.PApplet;
import soundcipher.SCScorePlus;
import views.PhaseShifter;
import views.View;
import views.ViewVariableInfo;

/**
 * The screen that displays views, ways of visualizing the music.
 * 
 * @author James Morrow
 *
 */
public class Presenter extends Screen implements ViewVariableInfo{	
	//time
	private float prev_notept1, prev_notept2;
	
	//playback
	private SCScorePlus player1 = new SCScorePlus();
	private SCScorePlus player2 = new SCScorePlus();
	private boolean playing;
	private int sign;
	
	//view
	private View view;
	
	//icons
	private ArrayList<Icon> icons = new ArrayList<Icon>();


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
	
	public void setupIcons() {
		icons.clear();
		Field[] fields = view.getClass().getDeclaredFields();
		try {
			for (Field f : fields) {
				if (f.getType().equals(ModInt.class)) {
					ModInt x = (ModInt)f.get(view);
					String name = x.getName();
					switch(name) {
						case activeNoteModeName: icons.add(new ShowActiveNoteIcon(x.toInt())); break;
						case transformationName: icons.add(new TransformIcon(x.toInt())); break;
						case cameraModeName: icons.add(new CameraIcon(x.toInt())); break;
						case noteGraphicName: icons.add(new NoteIcon(x.toInt())); break;
						case plotPitchModeName: icons.add(new PlotPitchIcon(x.toInt())); break;
						case colorSchemeName: icons.add(new ColorSchemeIcon(x.toInt())); break;
						case superimposedOrSeparatedName: icons.add(new SuperimposedOrSeparatedIcon(x.toInt())); break;
						case instrumentName : icons.add(new InstrumentIcon(x.toInt())); break;
						case scoreModeName : icons.add(new ScoreModeIcon(x.toInt())); break;
						default: 
							icons.add(new DefaultIcon(x.toInt()));
							System.out.println("Don't know that view variable: " + name); 
							break;
					}
				}
			}
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public void drawIcons() {
		float radius = 25;
		float dx = radius*2.25f;
		float x = radius;
		float y = pa.height - radius;
		
		for (Icon icon : icons) {
			icon.draw(x, y, radius, pa);
			x += dx;
		}
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
}
