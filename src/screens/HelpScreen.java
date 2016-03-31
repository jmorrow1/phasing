package screens;

import controlP5.Button;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Slider;
import controlP5.Toggle;
import controlp5.DropdownListPlus;
import controlp5.Scrollbar;
import phasing.PhasesPApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class HelpScreen extends Screen {
	
	//controlp5
	private ControlP5 cp5;
	private Button addNoteButton, subNoteButton;
	private Slider bpmSlider, bpmDifferenceSlider;
	private Scrollbar hScrollbar;
	private Toggle playToggle;
	private DropdownListPlus rootMenu, scaleMenu;
	private int controller_dy = 30;

	public HelpScreen(PhasesPApplet pa) {
		super(pa);	
		initControlP5Objects();
	}
	
	private void initControlP5Objects() {
		cp5 = new ControlP5(pa);
		
		addNoteButton = Editor.copyAddNoteButton(cp5, this);
		subNoteButton = Editor.copySubNoteButton(cp5, this);
		bpmDifferenceSlider = Editor.copyBPMDifferenceSlider(cp5, this, 100, 23);
		bpmSlider = Editor.copyBPMSlider(cp5, this, 100, 23);
		hScrollbar = Editor.copyHScrollbar(cp5, this, 200, 8, 10);
		playToggle = Editor.copyPlayToggle(cp5, this);
		rootMenu = Editor.copyRootMenu(cp5, this);
		scaleMenu = Editor.copyScaleMenu(cp5, this);
		
		Controller[] cs = new Controller[] {addNoteButton,
				                            subNoteButton,
				                            bpmDifferenceSlider,
				                            bpmSlider,
				                            hScrollbar,
				                            playToggle,
				                            rootMenu,
				                            scaleMenu};
		
		int x1 = 0, y1 = 0;
		for (int i=0; i<cs.length; i++) {
			cs[i].setPosition(x1, y1);
			y1 += cs[i].getHeight() + controller_dy;
		}
	}

	@Override
	public void windowResized() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onExit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw() {
		pa.background(255);
	}
}