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
	//editor
	Editor editor;
	
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
	
	//copy
	private String activeText = "";
	private static final String presentButtonText = "Where you can watch your compositions unfold.";
	private static final String composeButtonText = "Where you can compose music.";
	private static final String loadButtonText = "Where you can load previously created music and generate new music.";
	private static final String rootMenuText = "Change the starting pitch of the scale.";
	private static final String scaleMenuText = "Change the type of scale. \n\nThe pentatonic scales are good for making highly harmonious music. \n\nWith the chromatic scales, it is possible to make highly disharmonious music. \n\nThe major and minor scales give more color than the pentatonic scales while still avoiding some of the possible disharmonies of the chromatic scales.";
	private static final String tempoSliderText = "Change the speed (in beats per minute) of the music. The larger the tempo, the faster the music.";
	private static final String tempoDifferenceSliderText = "Change the difference in speed between the two players. \n\nA tempo difference of 0 means the two players will play at exactly the same speed. \n\nThings become interesting when you make the tempo difference a non-zero value.";
	private static final String playToggleText = "Play back melodies in the editor before trying them out in the presenter.";
	private static final String hScrollbarText = "Browse a melody that is longer than what can be contained on a single screen.";
	private static final String subNoteButtonText = "Make the melody one note shorter.";
	private static final String addNoteButtonText = "Make the melody one note longer.";
	
	//grid info toggle
	private boolean drawGridInfo;
	
	//style
	private final int gridColor;
	
	public HelpScreen(Editor editor, PhasesPApplet pa) {
		super(pa);
		this.editor = editor;
		gridColor = pa.lerpColor(pa.color(0), pa.getColor1(), 0.25f);
	}
	
	private void initControlP5Objects(Rect gridShape) {
		if (cp5 != null) {
			cp5.dispose();
		}
		cp5 = new ControlP5(pa);
		
		hScrollbar = editor.copyHScrollbar(cp5, this, 8, 10, pa.height, gridShape);
		addNoteButton = editor.copyAddNoteButton(cp5, this, hScrollbar);
		subNoteButton = editor.copySubNoteButton(cp5, this, hScrollbar);
		scaleMenu = editor.copyScaleMenu(cp5, this);
		rootMenu = editor.copyRootMenu(cp5, this);	
		bpmSlider = editor.copyBPMSlider(cp5, this, scaleMenu, pa.width, pa.height);
		bpmDifferenceSlider = editor.copyBPMDifferenceSlider(cp5, this, bpmSlider, pa.width, pa.height);
		playToggle = editor.copyPlayToggle(bpmDifferenceSlider, cp5, this);	
	}

	@Override
	public void windowResized() {
		gridShape = editor.copyGridFrame(pa.width, pa.height);
		initControlP5Objects(gridShape);
	}

	@Override
	public void onEnter() {
		gridShape = editor.copyGridFrame(pa.width, pa.height);
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
		pa.fill(255);
		pa.strokeWeight(1);
		pa.stroke(gridColor);
		gridShape.display(pa);
		drawInfo();
		pa.drawControlP5();
	}
	
	private void drawInfo() {
		if (drawGridInfo) {
			
		}
		else {
			pa.textFont(pa.pfont18);
			pa.textAlign(pa.LEFT, pa.TOP);
			pa.rectMode(pa.CORNERS);
			pa.fill(gridColor);
			pa.text(activeText, 
					gridShape.getX1() + 15, gridShape.getY1() + 15,
					gridShape.getX2() - 15, gridShape.getY2() - 15);
		}
	}
	
	@Override
	public void mouseMoved() {
		if (gridShape.touches(pa.mouseX, pa.mouseY)) {
			drawGridInfo = true;
			activeText = "";
		}
		else {
			drawGridInfo = false;
			if (pa.isMouseOverPresentButton()) {
				activeText = presentButtonText;
			}
			else if (pa.isMouseOverComposeButton()) {
				activeText = composeButtonText;
			}
			else if (pa.isMouseOverLoadButton()) {
				activeText = loadButtonText;
			}
			else if (rootMenu.isMouseOver()) {
				activeText = rootMenuText;
			}
			else if (scaleMenu.isMouseOver()) {
				activeText = scaleMenuText;
			}
			else if (bpmSlider.isMouseOver()) {
				activeText = tempoSliderText;
			}
			else if (bpmDifferenceSlider.isMouseOver()) {
				activeText = tempoDifferenceSliderText;
			}
			else if (playToggle.isMouseOver()) {
				activeText = playToggleText;
			}
			else if (subNoteButton.isMouseOver()) {
				activeText = subNoteButtonText;
			}
			else if (hScrollbar.isMouseOver()) {
				activeText = hScrollbarText;
			}
			else if (addNoteButton.isMouseOver()) {
				activeText = addNoteButtonText;
			}
			else {
				activeText = "";
			}
		}
	}
}