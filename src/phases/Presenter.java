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
	private int activeIconIndex = 0;
	private ArrayList<Icon[]> iconLists = new ArrayList<Icon[]>();
	private ArrayList<ModInt> variables = new ArrayList<ModInt>();

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
		iconLists.clear();
		Field[] fields = view.getClass().getDeclaredFields();
		try {
			for (Field f : fields) {
				if (f.getType().equals(ModInt.class)) {
					ModInt x = (ModInt)f.get(view);
					String name = x.getName();
					Icon[] iconList = null;
					switch(name) {
						case activeNoteModeName: 
							iconList = new Icon[x.getDivisor()];
							for (int i=0; i<iconList.length; i++) {
								iconList[i] = new ShowActiveNoteIcon(i);
							}
							break;
						case transformationName:
							iconList = new Icon[x.getDivisor()];
							for (int i=0; i<iconList.length; i++) {
								iconList[i] = new TransformIcon(i);
							}
							break;
						case cameraModeName:
							iconList = new Icon[x.getDivisor()];
							for (int i=0; i<iconList.length; i++) {
								iconList[i] = new CameraIcon(i); 
							}
							break;
						case noteGraphicName:
							iconList = new Icon[x.getDivisor()];
							for (int i=0; i<iconList.length; i++) {
								iconList[i] = new NoteIcon(i); 
							}
							break;
						case plotPitchModeName: 
							iconList = new Icon[x.getDivisor()];
							for (int i=0; i<iconList.length; i++) {
								iconList[i] = new PlotPitchIcon(i);
							}
							break;
						case colorSchemeName:
							iconList = new Icon[x.getDivisor()];
							for (int i=0; i<iconList.length; i++) {
								iconList[i] = new ColorSchemeIcon(i); 
							}
							break;
						case superimposedOrSeparatedName: 
							iconList = new Icon[x.getDivisor()];
							for (int i=0; i<iconList.length; i++) {
								iconList[i] = new SuperimposedOrSeparatedIcon(i);
							}
							break;
						case instrumentName :
							iconList = new Icon[x.getDivisor()];
							for (int i=0; i<iconList.length; i++) {
								iconList[i] = new InstrumentIcon(i);
							}
							break;
						case scoreModeName :
							iconList = new Icon[x.getDivisor()];
							for (int i=0; i<iconList.length; i++) {
								iconList[i] = new ScoreModeIcon(i);
							}
							break;
						default: 
							iconList = new Icon[x.getDivisor()];
							for (int i=0; i<iconList.length; i++) {
								iconList[i] = new DefaultIcon(i);
							}
							System.out.println("Don't know that view variable: " + name); 
							break;
					}
					iconLists.add(iconList);
					variables.add(x);
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
		float startX = 1.25f*radius;
		float startY = pa.height - 1.25f*radius;
		float x = startX;
		float y = startY;
		
		for (int i=0; i<iconLists.size(); i++) {
			int j = variables.get(i).toInt();
			Icon[] iconList = iconLists.get(i);
			Icon icon = iconList[j];
			icon.draw(x, y, radius, pa);
			x += dx;
		}

		//draw box around active icon
		pa.noFill();
		pa.strokeWeight(2);
		pa.stroke(pa.getBrightColor2());
		pa.rectMode(pa.RADIUS);
		pa.rect(startX + activeIconIndex*dx, startY, radius, radius);
		
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
	public void keyPressed() {
		if (pa.key == pa.CODED) {
			if (pa.keyCode == pa.LEFT) {
				activeIconIndex = PhasesPApplet.remainder(activeIconIndex-1, iconLists.size());
			}
			else if (pa.keyCode == pa.RIGHT) {
				activeIconIndex = (activeIconIndex+1) % iconLists.size();
			}
			else if (pa.keyCode == pa.UP) {
				variables.get(activeIconIndex).decrement();
				view.updateState();
			}
			else if (pa.keyCode == pa.DOWN) {
				variables.get(activeIconIndex).increment();
				view.updateState();
			}
		}
	}
}