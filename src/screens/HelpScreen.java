package screens;

import controlP5.Button;
import controlP5.ControlP5;
import controlP5.Slider;
import controlP5.Toggle;
import controlp5.DropdownListPlus;
import controlp5.Scrollbar;
import geom.Rect;
import phasing.PhasesPApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class HelpScreen extends Screen {
	//grid shape
	Rect gridShape;
	
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
		gridShape = Editor.copyGridFrame(pa.width, pa.height);
		initControlP5Objects(gridShape);
	}
	
	private void initControlP5Objects(Rect gridShape) {
		cp5 = new ControlP5(pa);
		
		hScrollbar = Editor.copyHScrollbar(cp5, this, 8, 10, pa.height, gridShape);
		addNoteButton = Editor.copyAddNoteButton(cp5, this, hScrollbar);
		subNoteButton = Editor.copySubNoteButton(cp5, this, hScrollbar);
		rootMenu = Editor.copyRootMenu(cp5, this);
		scaleMenu = Editor.copyScaleMenu(rootMenu, cp5, this);
		bpmSlider = Editor.copyBPMSlider(cp5, this, scaleMenu, pa.width, pa.height);
		bpmDifferenceSlider = Editor.copyBPMDifferenceSlider(cp5, this, bpmSlider, pa.width, pa.height);
	
		playToggle = Editor.copyPlayToggle(bpmDifferenceSlider, cp5, this);	
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