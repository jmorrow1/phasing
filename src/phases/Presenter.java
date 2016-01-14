package phases;

import java.lang.reflect.Field;

import geom.Rect;
import icons.CameraIcon;
import icons.ColorSchemeIcon;
import icons.InstrumentIcon;
import icons.NoteIcon;
import icons.PlotPitchIcon;
import icons.ScoreModeIcon;
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
	}
	
	public void setupIcons() {
		Field[] fields = view.getClass().getFields();
		try {
			for (Field f : fields) {
				if (f.isAccessible() && f.getType().equals(ModInt.class)) {
					ModInt x = (ModInt)f.get(view);
					String name = x.getName();
					switch(name) {
						//case "showActiveNote": new ShowActiveNoteIcon(f.getBoolean(view)); break;
						case transformationName: new TransformIcon(f.getInt(view)); break;
						case cameraModeName: new CameraIcon(f.getInt(view)); break;
						case noteGraphicName: new NoteIcon(f.getInt(view)); break;
						case plotPitchModeName: new PlotPitchIcon(f.getBoolean(view)); break;
						case colorSchemeName: new ColorSchemeIcon(f.getInt(view)); break;
						case superimposedOrSeparatedName: new SuperimposedOrSeparatedIcon(f.getInt(view)); break;
						case instrumentName : new InstrumentIcon(f.getInt(view)); break;
						case scoreModeName : new ScoreModeIcon(f.getInt(view)); break;
						default: System.out.println("Don't know that view field name " + f.getName()); break;
					}
				}
			}
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
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
