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
 * A screen that gives information about the Editor and how to use it.
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
	}
	
	private void initControlP5Objects(Rect gridShape) {
		if (cp5 != null) {
			cp5.dispose();
		}
		cp5 = new ControlP5(pa);
		
		hScrollbar = Editor.copyHScrollbar(cp5, this, 8, 10, pa.height, gridShape);
		addNoteButton = Editor.copyAddNoteButton(cp5, this, hScrollbar);
		subNoteButton = Editor.copySubNoteButton(cp5, this, hScrollbar);
		
		scaleMenu = Editor.copyScaleMenu(cp5, this);
		rootMenu = Editor.copyRootMenu(cp5, this);
		
		bpmSlider = Editor.copyBPMSlider(cp5, this, scaleMenu, pa.width, pa.height);
		bpmDifferenceSlider = Editor.copyBPMDifferenceSlider(cp5, this, bpmSlider, pa.width, pa.height);
	
		playToggle = Editor.copyPlayToggle(bpmDifferenceSlider, cp5, this);	
	}

	@Override
	public void windowResized() {
		gridShape = Editor.copyGridFrame(pa.width, pa.height);
		initControlP5Objects(gridShape);
	}

	@Override
	public void onEnter() {
		gridShape = Editor.copyGridFrame(pa.width, pa.height);
		initControlP5Objects(gridShape);
	}

	@Override
	public void onExit() {
		if (cp5 != null) {
			cp5.dispose();
		}
	}

	@Override
	public void onPause() {}

	@Override
	public void onResume() {}

	@Override
	public void draw() {
		pa.background(255);
		pa.drawControlP5();
	}
}