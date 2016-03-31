package screens;

import java.util.Arrays;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.ControllerView;
import controlP5.DropdownList;
import controlP5.Slider;
import controlP5.Toggle;
import controlp5.DropdownListPlus;
import controlp5.PlusMinusButtonView;
import controlp5.Scrollbar;
import controlp5.SliderPlus;
import controlp5.Util;
import geom.Rect;
import phasing.PhasesPApplet;
import phasing.Phrase;
import phasing.Scale;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;
import soundcipher.SoundCipherPlus;
import soundcipher.SoundCipherPlus.SoundCipherPlusListener;
import util.FloatFormatter;

/**
 * Provides an editor in which the user can create and edit musical phrases.
 * @author James Morrow
 *
 */
public class Editor extends Screen implements SoundCipherPlusListener {
	//time
	private int prev_t;
	private float[] unlockTimes = new float[] {0, 0, 1, 2, 3, 4};
	
	//playback
	private SoundCipherPlus livePlayer;
	private float notept = 0;
	
	//animation
	private final int NOT_APPLICABLE = -1;
	private int activeNoteIndex = NOT_APPLICABLE;
	
	//piano
	private boolean labelPianoKeys = true;
	private int minOctave = 4;
	private final static int W=0xffffffff, B=PhasesPApplet.getColor2();
	private final static int[] keyColors = new int[] {W, B, W, B, W, W, B, W, B, W, B, W};
	
	//grid
	private Rect gridFrame;
	private final static int cellHeight = 20;
	private final static int cellWidth = 60;
	
	//interaction w/ grid
	private final int NOT_DRAWING=-1, DRAWING_NOTE=0, DRAWING_REST=1;
	private int drawState = NOT_DRAWING;
	private int startIndexOfUserDrawnNote = -1;
	private int indexMousePressed=-1, pitchMousePressed=-1;
	private boolean shiftPressed;
	
	//cp5
	private final static int BPM_1 = 1, BPM_DIFFERENCE = 2;
	private static int MAX_DIFFERENCE = 5;
	private ControlP5 cp5;
	private Toggle playToggle;
	private Slider bpmSlider, bpmDifferenceSlider;
	private DropdownListPlus rootMenu, scaleMenu;
	private Scrollbar hScrollbar;
	private Button subNoteButton, addNoteButton;
	private String rootLabel, scaleLabel;
	private boolean rootMenuOpen, scaleMenuOpen;
	
	//controller layout
	private final static int margin_btwn_top_toolbar_y2_and_controllers = 10;
	
	//cursor
	private boolean cursorIsX;
	private float xCursorRadius = 4;
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	/**
	 * 
	 * @param pa The PApplet to draw to
	 */
	public Editor(PhasesPApplet pa) {
		super(pa);
		initMusicPlayer();
		initGridFrame();
		initCP5Objects(gridFrame);
		hideAllControllers();
		cp5.hide();
	}
	
	//TODO Maybe find a naming convention that signals which of the following methods
	//are to be used just as variables and which of them are to be used to update the Editor state.
	
	private int columnSize() {
		return (int)(gridFrame.getHeight() / cellHeight);
	}
	
	private int numKeys() {
		return columnSize();
	}
	
	/**
	 * Gives the proper value for the uppermost y-coordinate of the bottom tollbar,
	 * which is dependent on the height of the window.
	 * @return The proper uppermost y-coordinate of the bottom toolbar.
	 */
	private float botToolbarY1() {
		return botToolbarY1(pa.height);
	}
	
	/**
	 * Gives the proper value for the uppermost y-coordinate of the bottom tollbar,
	 * which is dependent on the height of the window.
	 * @param windowHeight The height of the window.
	 * @return The proper uppermost y-coordinate of the bottom toolbar.
	 */
	private static float botToolbarY1(float windowHeight) {
		return windowHeight - 40;
	}
	
	/**
	 * Gives the proper value for the width of the hScrollbar,
	 * which is dependent on the width of the grid.
	 * @return The proper width of the hScrollbar.
	 */
	private int hScrollbarWidth() {
		return hScrollbarWidth(gridFrame);
	}
	
	/**
	 * Gives the proper value for the width of the hScrollbar,
	 * which is dependent on the width of the grid.
	 * @param gridFrame The grid frame which this controller is sized in relation to.
	 * @return The proper width of the hScrollbar.
	 */
	private static int hScrollbarWidth(Rect gridFrame) {
		return (int)(gridFrame.getWidth() - 80);
	}
	
	/**
	 * Gives the number of rows after the piano in the grid.
	 * @return The number of rows.
	 */
	private int rowSize() {
		return (int)(gridFrame.getWidth() / cellWidth) - 1;
	}
	
	/**
	 * Initializes the gridFrame.
	 */
	private void initGridFrame() {
		gridFrame = consGridFrame(pa.width, pa.height);
	}
	
	private static Rect consGridFrame(int screenWidth, int screenHeight) {
		float gridHeight = cellHeight * (int) ((botToolbarY1(screenHeight) - PhasesPApplet.topToolbarY2()) / cellHeight);
		return new Rect(10, PhasesPApplet.topToolbarY2(), screenWidth-10,
				PhasesPApplet.topToolbarY2() + gridHeight, PApplet.CORNERS);
	}
	
	/**
	 * Initializes the object that plays music in the editor.
	 */
	private void initMusicPlayer() {
		livePlayer = new SoundCipherPlus(pa, pa.currentPhrase, this);
	}
	
	/**
	 * Initalizes the ControlP5 object and controllers, in addition to adding the controllers to the CP5 object.
	 * @gridFrame Used to position the controllers.
	 */
	private void initCP5Objects(Rect gridFrame) {
		cp5 = new ControlP5(pa);
		cp5.setAutoDraw(false);
		initScaleMenus();
		initBPMSliders(scaleMenu);
		initPlayStopToggle(bpmDifferenceSlider);
		initHorizontalScrollbar(gridFrame);
	    initSubNoteButton(hScrollbar);
	    initAddNoteButton(hScrollbar);
	}
	
	/**
	 * Initializes the button that decreases phrase length note-by-note.
	 * Adds the button to the CP5 object.
	 * @param hScrollbar This is here to make explicit that this method relies on hScrollbar being already initialized.
	 */
	private void initSubNoteButton(Scrollbar hScrollbar) {
		subNoteButton = consSubNoteButton(cp5, this, hScrollbar);
	}
	
	/**
	 * Constructs a sub-note button, associates it with the given ControlP5 and returns it.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the Button.
	 * @param hScrollbar The horizontal scrollbar that this controllers is positioned in relation to.
	 * @return The button.
	 */
	private static Button consSubNoteButton(ControlP5 cp5, Object listener, Scrollbar hScrollbar) {
		int sideLength = 24;
		
		Button b = cp5.addButton("decreasePhraseLength")
				      .setPosition(Util.getX1(hScrollbar) - sideLength - 10,
                                   Util.getY2(hScrollbar) - hScrollbar.getHeight()/2f - sideLength/2f)
			          .setSize(sideLength, sideLength)
			          .setView(new PlusMinusButtonView(false))
			          .plugTo(listener)
			          ;
		
		colorController(b);
		
		return b;
	}
	
	/**
	 * Initializes the button that increases phrase length note-by-note.
	 * Adds the button to the CP5 object.
	 * @param hScrollbar This is here to make explicit that this method relies on hScrollbar being already initialized.
	 */
	private void initAddNoteButton(Scrollbar hScrollbar) {
		addNoteButton = consAddNoteButton(cp5, this, hScrollbar);
		
	}
	
	/**
	 * Constructs an add-note button, associates it with the given ControlP5 and returns it.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the Button.
	 * @param hScrollbar The horizontal scrollbar that this controllers is positioned in relation to.
	 * @return The button.
	 */
	private static Button consAddNoteButton(ControlP5 cp5, Object listener, Scrollbar hScrollbar) {
		int sideLength = 24;
		
		Button b = cp5.addButton("increasePhraseLength")
				      .setSize(sideLength, sideLength)
				      .setPosition(Util.getX2(hScrollbar) + 10,
                                   Util.getY2(hScrollbar) - hScrollbar.getHeight()/2f - sideLength/2f)
		              .setView(new PlusMinusButtonView(true))
	    	          .plugTo(listener)
				      ;

		colorController(b);
		
		return b;
	}
	
	/**
	 * Initializes the scrollbar that controls which sub-section of the phrase is displayed.
	 * Adds the scrollbar to the CP5 object.
	 * @param gridFrame The shape of the grid this controller is positioned in relation to.
	 */
	private void initHorizontalScrollbar(Rect gridFrame) {
		hScrollbar = consHorizontalScrollbar(cp5, this,
				PApplet.min(rowSize(), pa.currentPhrase.getGridRowSize()), pa.currentPhrase.getGridRowSize(),
				pa.height, gridFrame);
	}
	
	/**
	 * Constructs a horizontal scrollbar, associates it with the given ControlP5 and returns it.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the Scrollbar.
	 * @param ticksPerScroller The number of ticks in the scroller (see Scrollbar documentation).
	 * @param ticksPerTrack The number of ticks in the track (see Scrollbar documentation).
	 * @param windowHeight The height of the window.
	 * @param gridFrame The shape of the grid this controller is positioned in relation to.
	 * @return The scrollbar.
	 */
	private static Scrollbar consHorizontalScrollbar(ControlP5 cp5, Object listener,
			int ticksPerScroller, int ticksPerTrack, int windowHeight, Rect gridFrame) {
		
		Scrollbar s = new Scrollbar(cp5, "hScrollbar", ticksPerScroller, ticksPerTrack);
	    
		s.setPosition(gridFrame.getX1() + 40, windowHeight - 25f)
	     .setSize(hScrollbarWidth(gridFrame), 15)
		 .plugTo(listener)
	     ;
	    colorController(s);
	    
	    return s;
	}
	
	/**
	 * Initalizes the toggle that controls whether or not music plays.
	 * Adds the toggle to the CP5 object.
	 * @param bpmDifferenceSlider This is here to make explicit that this method relies on bpmDifferenceSlider being already initialized.
	 */
	private void initPlayStopToggle(Slider bpmDifferenceSlider) {
		playToggle = consPlayToggle(bpmDifferenceSlider, cp5, this);
	}
	
	/**
	 * Constructs and returns a play toggle that is connected with the given ControlP5 instance.
	 * 
	 * @param bpmDifferenceSlider The slider that this controller is positioned in relation to.
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the Toggle.
	 * @return The toggle.
	 */
	private static Toggle consPlayToggle(Slider bpmDifferenceSlider, ControlP5 cp5, Object listener) {
		int sideLength = 35;
		Toggle t = cp5.addToggle("play")
					  .setSize(sideLength, sideLength)
					  .setPosition(Util.getX2(bpmDifferenceSlider) + PhasesPApplet.CONTROLLER_DX,
		  		                   PhasesPApplet.topToolbarY2() - margin_btwn_top_toolbar_y2_and_controllers - sideLength)
					  .plugTo(listener)
					  .setView(new ControllerView<Toggle>() {
						    @Override
							public void display(PGraphics pg, Toggle t) {
						    	if (t.getValue() == 0) {
						    		//draw white rect under play button
									pg.rectMode(pg.CORNER);
									pg.fill(255);
									pg.noStroke();
									pg.rect(0, 0, t.getWidth(), t.getHeight());
						    	}
						    	
						    	pg.noStroke();
								if (t.isMouseOver()) {
									pg.fill(t.getColor().getForeground());
								}
								else {
									pg.fill(t.getColor().getBackground());
								}
								
								if (t.getValue() == 0) {
									//draw play button
									pg.triangle(0, 0, 0, t.getHeight(), t.getWidth(), t.getHeight()/2f);
								}
								else {
									//draw pause button
									pg.rectMode(pg.CORNER);
									pg.rect(0, 0, t.getWidth(), t.getHeight());
								}
							}				   
					   })
					   ;
		colorController(t);
		
		return t;
	}
	
	/**
	 * Initializes the sliders that control the tempos (in beats per minute) of the two players.
	 * Adds the sliders to the CP5 object.
	 * @param scaleMenu This is here to make explicit that this method relies on scaleMenu being already initialized.
	 */
	private void initBPMSliders(DropdownList scaleMenu) {
		bpmSlider = consBPMSlider(pa.width, pa.height, pa.getBPM1(), scaleMenu, cp5, this);
		
		bpmDifferenceSlider = consBPMDifferenceSlider(pa.width, pa.height,
				                                      pa.getBPM2() - pa.getBPM1(),
				                                      bpmSlider, cp5, this);
	}
	
	/**
	 * Constructs the slider that controls the first player's tempo (in beats per minute)
	 * at a given location and with a given size.
	 * 
	 * @param windowWidth The width of the window in which this slider will be displayed.
	 * @param windowHeight THe height of the window in which this slider will be displayed.
	 * @param value The initial bpm value.
	 * @param scaleMenu The scale menu that this controller is positioned in relation to.
	 * @param cp5 The ControlP5 instance to attach the controller to.
	 * @param listener The object that should recieve events from the controller.
	 * @return The slider.
	 */
	private static Slider consBPMSlider(int windowWidth, int windowHeight, float value,
			DropdownList scaleMenu, ControlP5 cp5, Object listener) {
		int sliderWidth = getSliderWidth(windowWidth);
		int sliderHeight = getSliderHeight();
		return consBPMSlider("beatsPerMinute", "Tempo 1", BPM_1,
						  	 Util.getX2(scaleMenu) + PhasesPApplet.CONTROLLER_DX, 
			                 PhasesPApplet.topToolbarY2() - margin_btwn_top_toolbar_y2_and_controllers - sliderHeight,
			                 (int)(1.1f * sliderWidth),
			                 sliderHeight,
			                 value,
			                 11, 100,
			                 1,
			                 (floatingPoint) -> "" + PApplet.round(floatingPoint),
			                 cp5, listener);
	}
	
	/**
	 * Constructs the slider that controls the difference between the 1st player's tempo and the 2nd player's tempo (in beats per minute)
	 * at a given location and with a given size.
	 * 
     * @param windowWidth The width of the window in which this slider will be displayed.
	 * @param windowHeight THe height of the window in which this slider will be displayed.
	 * @param value The initial value of the slider.
	 * @param bpmSlider The bpm slider which this slider is defined in relation to.
	 * @param cp5 The ControlP5 instance to attach the controller to.
	 * @param listener The object that should recieve events from the controller.
	 * @return The slider.
	 */
	private static Slider consBPMDifferenceSlider(int windowWidth, int windowHeight, float value,
				Slider bpmSlider, ControlP5 cp5, Object listener) {
		int sliderWidth = getSliderWidth(windowWidth);
		int sliderHeight = getSliderHeight();
		return consBPMSlider("bpmDifference", "Tempo Difference ", BPM_DIFFERENCE,
				             Util.getX2(bpmSlider) + PhasesPApplet.CONTROLLER_DX,
                             PhasesPApplet.topToolbarY2() - margin_btwn_top_toolbar_y2_and_controllers - sliderHeight,
                             (int)(0.9f * sliderWidth), 
                             sliderHeight,
			                 value,
			                 -MAX_DIFFERENCE, MAX_DIFFERENCE,
			                 4, 
			                 (floatingPoint) -> String.format("%.2f", floatingPoint),
			                 cp5, listener);
	}
	
	/**
	 * 
	 * Constructs a slider with a bunch of properties.
	 * 
	 * The reason this method exists is mostly just to save keystrokes.
	 * 
	 * @param name A String identification.
	 * @param label The String for the slider to display.
	 * @param id An integer identification.
	 * @param x The leftmost x-coordinate of the slider.
	 * @param y The uppermost y-coordinate of the slider.
	 * @param w The width of the slider.
	 * @param h The height of the slider.
	 * @param value The starting value the slider has.
	 * @param minValue The minimum value the slider can have.
	 * @param maxValue The maximum value the slider can have.
	 * @param ticksPerWholeNumber 
	 * @param f The function that converts floating points to Strings.
	 * @param cp5 The ControlP5 to attach the controller to.
	 * @param listener The object that should receive events from the controller.
	 * @return The slider.
	 */
	private static Slider consBPMSlider(String name, String label, int id, float x, float y, int w, int h,
			  float value, int minValue, int maxValue, int ticksPerWholeNumber, FloatFormatter f,
			  ControlP5 cp5, Object listener) {
		Slider s = new SliderPlus(cp5, name, PhasesPApplet.pfont12, PhasesPApplet.pfont18, f);
        s.setId(id);
        s.setCaptionLabel(label);
        s.setDecimalPrecision(0);
        s.setRange(minValue, maxValue);
        s.setPosition(x, y);
        s.setSize(w, h);
        s.setValue(value);
        s.setLabelVisible(false);
        s.setNumberOfTickMarks((maxValue-minValue) * ticksPerWholeNumber + 1);
        s.plugTo(listener);
		colorController(s);
		return s;
	}
	
	/**
	 * Initializes the drop-down menus that control what scale is active.
	 * Adds the menus to the CP5 object.
	 */
	private void initScaleMenus() {
		scaleMenu = consScaleMenu(cp5, this);
		scaleMenu.addItems(pa.scaleTypes);
		scaleMenu.setSize(scaleMenu.getWidth(), scaleMenu.getHeight()*(pa.scaleTypes.size()+1));
		scaleMenu.setLabel(pa.currentScale.getClassName());
		formatLabel(scaleMenu);
		scaleLabel = scaleMenu.getLabel();
		
		rootMenu = consRootMenu(cp5, this);
		rootMenu.addItems(pa.roots);
		rootMenu.setSize(rootMenu.getWidth(), rootMenu.getHeight()*(pa.roots.length+1));
		rootMenu.setLabel(pa.currentScale.getName());
		formatLabel(rootMenu);
		rootLabel = rootMenu.getLabel();
	}
	
	/**
	 * Constructs and returns a label-less, empty scale menu that is connected with the given ControlP5 instance.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the controller.
	 * @return The scale menu.
	 */
	private static DropdownListPlus consScaleMenu(ControlP5 cp5, Object listener) {
		int menuItemHeight = 22;
		
		DropdownListPlus d = new DropdownListPlus(cp5, "Scale Type");
		d.setPosition(PhasesPApplet.getHelpButtonX2() + PhasesPApplet.CONTROLLER_DX,
				      PhasesPApplet.topToolbarY2() - margin_btwn_top_toolbar_y2_and_controllers - menuItemHeight)
		 .setSize(130, menuItemHeight)
	     .setItemHeight(menuItemHeight)
		 .setBarHeight(menuItemHeight)
		 .close()
		 ;
		
		colorController(d);
		
		return d;
	}
	
	/**
	 * Constructs and returns a label-less, empty root menu that is connected with the given ControlP5 instance.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the controller.
	 * @return The root menu.
	 */
	private static DropdownListPlus consRootMenu(ControlP5 cp5, Object listener) {
		int menuItemHeight = 22;
		
		DropdownListPlus d = new DropdownListPlus(cp5, "Root");
		d.setPosition(PhasesPApplet.getPresentButtonX2() + PhasesPApplet.CONTROLLER_DX,
				      PhasesPApplet.topToolbarY2() - margin_btwn_top_toolbar_y2_and_controllers - menuItemHeight)
	     .setSize(90, menuItemHeight)  
	     .setItemHeight(menuItemHeight)
	     .setBarHeight(menuItemHeight)
	     .close()
	     ;
		
		colorController(d);
		
		return d;
	}
	
	/**
	 * Changes the style information of the given controller's label to reformat it.
	 * @param x The given controller, a DropdownList.
	 */
	private static void formatLabel(DropdownList x) {
		x.getCaptionLabel().toUpperCase(false);
		x.getValueLabel().toUpperCase(false);
		x.getCaptionLabel().setFont(PhasesPApplet.pfont18);
		x.getValueLabel().setFont(PhasesPApplet.pfont18);
		x.getCaptionLabel().getStyle().paddingTop += 5;
		x.getValueLabel().getStyle().paddingTop += 5;
	}
	
	/**
	 * Makes all the Editor's controllers invisible.
	 */
	private void hideAllControllers() {
		hScrollbar.hide();
		addNoteButton.hide();
		subNoteButton.hide();
		bpmDifferenceSlider.hide();
		bpmSlider.hide();
		scaleMenu.hide();	
		rootMenu.hide();
		playToggle.hide();
	}
	
	/**
	 * Makes all the controllers that, according to the pa.playerInfo object, the player has unlocked visible.
	 */
	private void showUnlockedControllers() {
		if (pa.playerInfo.nextEditorUnlockIndex > 0) {
			playToggle.show();
			if (pa.playerInfo.nextEditorUnlockIndex > 1) {
				bpmDifferenceSlider.show();		
				if (pa.playerInfo.nextEditorUnlockIndex > 2) {
					bpmSlider.show();
					if (pa.playerInfo.nextEditorUnlockIndex > 3) {
						hScrollbar.show();
						addNoteButton.show();
						subNoteButton.show();
						if (pa.playerInfo.nextEditorUnlockIndex > 4) {
							rootMenu.show();
							if (pa.playerInfo.nextEditorUnlockIndex > 5) {
								scaleMenu.show();
							}
						}
					}
				}
			}
		}
	}

	private int getSliderWidth() {
		return getSliderWidth(pa.width);
	}
	
	private static int getSliderWidth(int windowWidth) {
		return (int)PApplet.constrain(PApplet.map(windowWidth, 800, 1366, 160, 442), 160, 442);
	}
	
	private static int getSliderHeight() {
		return 23;
	}
	
	/**
	 * Changes the way the given controller is colored.
	 * The way it does that is based on the controller's subtype.
	 * @param c The given controller.
	 */
	private static void colorController(Controller c) {
		c.setColorCaptionLabel(0xffffffff);
	    c.setColorValueLabel(0xffffffff);
		if (c instanceof DropdownList) {
			c.setColorBackground(PhasesPApplet.getColor1());
			c.setColorActive(PhasesPApplet.getColor1Bold());
			c.setColorForeground(PhasesPApplet.getColor1Bold());
		}
		else if (c instanceof Slider || c instanceof Scrollbar) {
			c.setColorBackground(PApplet.lerpColor(PhasesPApplet.getColor1(), 0xffffffff, 0.3f, PApplet.RGB));
		    c.setColorActive(PhasesPApplet.getColor1Bold());
		    c.setColorForeground(PhasesPApplet.getColor1());
		}
		else if (c instanceof Button) {
			c.setColorBackground(0xffffffff);
		    c.setColorForeground(PhasesPApplet.getColor1());
		    c.setColorActive(PhasesPApplet.getColor1Bold());
		}
		else if (c instanceof Toggle) {
			c.setColorBackground(PhasesPApplet.getColor1());
			c.setColorForeground(PhasesPApplet.getColor1Bold());
		}
	}

	/********************************
	 ***** Controller Callbacks *****
	 ********************************/
	
	/**
	 * Decreases the phrase length note-by-note.
	 * @param e
	 */
	public void decreasePhraseLength(ControlEvent e) {
		pa.currentPhrase.removeLastCell();
		if (pa.currentPhrase.getGridRowSize() <= rowSize()) {
			hScrollbar.setNumTickMarks(pa.currentPhrase.getGridRowSize());
			hScrollbar.setTicksPerScroller(pa.currentPhrase.getGridRowSize());
		}
		else {
			hScrollbar.setNumTickMarks(pa.currentPhrase.getGridRowSize());
			hScrollbar.setTicksPerScroller(rowSize());
		}
	}
	
	/**
	 * Increases the phrase length note-by-note.
	 * @param e
	 */
	public void increasePhraseLength(ControlEvent e) {
		pa.currentPhrase.appendCell();
		if (pa.currentPhrase.getGridRowSize() <= rowSize()) {
			hScrollbar.setNumTickMarks(pa.currentPhrase.getGridRowSize());
			hScrollbar.setTicksPerScroller(pa.currentPhrase.getGridRowSize());
		}
		else {
			hScrollbar.setNumTickMarks(pa.currentPhrase.getGridRowSize());
			hScrollbar.setTicksPerScroller(rowSize());
		}
	}
	
	/**
	 * Manipulates the tempo (in beats per minute) of the first player. 
	 * @param e
	 */
	public void beatsPerMinute(ControlEvent e) {
		pa.setBPM1(e.getValue());
		livePlayer.tempo(pa.getBPM1());
		pa.setBPM2(e.getValue() + bpmDifferenceSlider.getValue());
		pa.playerInfo.bpm1 = pa.getBPM1();
		pa.playerInfo.bpmDifference = pa.getBPM2() - pa.getBPM1();
	}
	
	/**
	 * Manipulates the difference between the tempos (in beats per minute) of the two players.
	 * @param e
	 */
	public void bpmDifference(ControlEvent e) {
		pa.setBPM2(pa.getBPM1() + e.getValue());
		pa.playerInfo.bpmDifference = pa.getBPM2() - pa.getBPM1();
	}
	
	/**
	 * Callback for ControlP5
	 * When playStop controller is toggled
	 * @param e
	 */
	public void play(ControlEvent e) {
		if (e.getValue() == 0) {
			livePlayer.stop();
		}
		else {
			livePlayer.tempo(pa.getBPM1());
			activeNoteIndex = NOT_APPLICABLE;
		}
	}
	
	/**************************
	 ***** Music Callback *****
	 **************************/
	
	@Override
	public void noteEvent(SoundCipherPlus livePlayer) {
		activeNoteIndex = livePlayer.getNoteIndex();
	}
	
	/*********************************
	 ***** Screen Event Handling *****
	 *********************************/
	
	@Override
	public void windowResized() {
		initGridFrame();
		cp5.dispose();
		initCP5Objects(gridFrame);
		hideAllControllers();
		showUnlockedControllers();
	}

	@Override
	public void onEnter() {
		cp5.show();
		showUnlockedControllers();
		drawToolbar();
		prev_t = pa.millis();
		playToggle.setValue(false);
		
		if (pa.width < 800 || pa.height < 600) {
			pa.resize(PApplet.max(800, pa.width), PApplet.max(600, pa.height));
		}
		else {
			windowResized();
		}
		
		initMusicPlayer();
		scaleMenu.setLabel(pa.currentScale.getClassName());
		rootMenu.setLabel(pa.currentScale.getName());
	}
	
	@Override
	public void onExit() {
		cp5.hide();
		pa.savePlayerInfo(); //TODO Is this line necessary?
		pa.saveCurrentPhrasePicture(); //TODO Is this line necessary?
	}
	
	@Override
	public void onPause() {
		//NOT IMPLEMENTED
	}
	
	@Override
	public void onResume() {
		//NOT IMPLEMENTED
	}
	
	/********************************
	 ***** Input Event Handling *****
	 ********************************/
	
	@Override
	public void keyPressed() {
		if (pa.key == ' ') {
			playToggle.setState(!playToggle.getState());
		}
		else if (pa.key == pa.CODED && pa.keyCode == pa.SHIFT) {
			shiftPressed = true;
			if (shiftClick()) {
				xCursor();
			}
		}
	}
	
	@Override
	public void keyReleased() {
		if (pa.key == pa.CODED && pa.keyCode == pa.SHIFT) {
			shiftPressed = false;
			if (!pa.mousePressed || pa.mouseButton != pa.RIGHT) {
				defaultCursor();
			}
		}
	}
	
	/**
	 * 
	 * @return True if user is simultaneously holding shift and pressing the left or right mouse button, false otherwise.
	 */
	private boolean shiftClick() {
		return pa.keyPressed && shiftPressed && pa.mousePressed && (pa.mouseButton == pa.LEFT || pa.mouseButton == pa.RIGHT);
	}
	
	@Override
	public void mouseWheel(MouseEvent event) {
		hScrollbar.myOnScroll(event.getCount());
	}
	
	@Override
	public void mousePressed() {
		boolean shiftClick = shiftClick();
		if (pa.mouseButton == pa.LEFT && !shiftClick) {
			defaultCursor();
		}
		else if (pa.mouseButton == pa.RIGHT || shiftClick) {
			xCursor();
		}
		
		if (mouseInGrid()) {
			mousePressedOnGrid();
		}
		
		if (!rootMenu.isInside()) {
			rootMenu.close();
		}
		
		if (!scaleMenu.isInside()) {
			scaleMenu.close();
		}
	}
	
	/**
	 * Allows notes and rests to be drawn to the grid.
	 */
	private void mousePressedOnGrid() {
		indexMousePressed = mouseToIndex();
		pitchMousePressed = mouseToPitch();
		startIndexOfUserDrawnNote = indexMousePressed;
		
		if (!rootMenu.isInside() && !scaleMenu.isInside()) {
			if (pa.mouseButton == pa.LEFT && !shiftClick()) {
				writeNote(indexMousePressed, pitchMousePressed);
			}
			else if ( (pa.mouseButton == pa.RIGHT || shiftClick())) {
				drawState = DRAWING_REST;
				if (pitchMousePressed == pa.currentPhrase.getGridPitch(indexMousePressed)) {
					writeRest(indexMousePressed, pitchMousePressed);
				}
			}
		}
	}
	
	/**
	 * Tries to write a pitch to the current phrase at some index.
	 * If it succeeds, it also edits the note at the subsequent index when appropriate.
	 * 
	 * @param index The index at which to add the pitch.
	 * @param pitch The pitch.
	 */
	private void writeNote(int index, int pitch) {
		boolean success = pa.currentPhrase.setCell(index, pitch, defaultDynamic(), Phrase.NOTE_START);
		if (success) {
			boolean editSubsequentNote = index+1 < pa.currentPhrase.getGridRowSize() &&
											pa.currentPhrase.getNoteType(index+1) == Phrase.NOTE_SUSTAIN;
			if (editSubsequentNote) {
				pa.currentPhrase.setNoteType(index+1, Phrase.NOTE_START);
			}
			
			drawState = DRAWING_NOTE;
		}
	}
	
	/**
	 * Tries to write a rest to the current phrase at some index.
	 * If it succeeds, it also edits the note at the subsequent index when appropriate.
	 * 
	 * @param index The index at which to add the rest.
	 * @param pitch The pitch.
	 */
	private void writeRest(int index, int pitch) {
		boolean success = pa.currentPhrase.setCell(index, pitch, defaultDynamic(), Phrase.REST);
		pa.currentPhrase.setNoteType(index, Phrase.REST);
		if (success) {
			boolean editSubsequentNote = index+1 < pa.currentPhrase.getGridRowSize() && 
											pa.currentPhrase.getNoteType(index+1) == Phrase.NOTE_SUSTAIN;
			if (editSubsequentNote) {
				pa.currentPhrase.setNoteType(index+1, Phrase.NOTE_START);
			}
		}	
	}
	
	@Override
	public void mouseReleased() {
		if ( (cursorIsX && pa.mouseButton == pa.RIGHT) ||
				(pa.mouseButton == pa.LEFT && shiftPressed)) {
			defaultCursor();
		}
		
		//resets the Editor's state w/r/t the grid:
		drawState = NOT_DRAWING;
		startIndexOfUserDrawnNote = -1;
		pa.saveCurrentPhrasePicture();
	}
	
	@Override
	public void mouseDragged() {
		//for continuing to draw notes to the grid:
		if (drawState == DRAWING_NOTE && mouseInGrid()) {
			int newIndex = mouseToIndex();
			int newPitch = mouseToPitch();
			if (newPitch == pitchMousePressed) {
				if (newIndex > indexMousePressed) {
					if (newIndex+1 < pa.currentPhrase.getGridRowSize() &&
							(pa.currentPhrase.getNoteType(newIndex) == Phrase.NOTE_SUSTAIN ||
							 pa.currentPhrase.getNoteType(newIndex) == Phrase.NOTE_START) &&
							pa.currentPhrase.getNoteType(newIndex+1) == Phrase.NOTE_SUSTAIN) {
						pa.currentPhrase.setNoteType(newIndex+1, Phrase.NOTE_START);
						indexMousePressed++;
					}
					pa.currentPhrase.setCell(newIndex, pitchMousePressed, defaultDynamic(), Phrase.NOTE_SUSTAIN);
				}
				else if (newIndex < indexMousePressed && newIndex < startIndexOfUserDrawnNote) {
					pa.currentPhrase.setCell(newIndex, pitchMousePressed, defaultDynamic(), Phrase.NOTE_START);
					if (newIndex+1 < pa.currentPhrase.getGridRowSize()) {
						pa.currentPhrase.setCell(newIndex+1, pitchMousePressed, defaultDynamic(), Phrase.NOTE_SUSTAIN);
						indexMousePressed++;
					}
					startIndexOfUserDrawnNote = newIndex;
				}
			}
			else {
				mousePressed();
			}
		}
		else if (drawState == DRAWING_REST && mouseInGrid()) {
			int index = mouseToIndex();
			int pitch = mouseToPitch();
			if (0 <= index && index < pa.currentPhrase.getGridRowSize() && pitch == pa.currentPhrase.getGridPitch(index)) {
				writeRest(index, pitch);
			}
		}
	}
	
	/**
	 * 
	 * @return True, if the mouse intersects the grid (but not the piano-shaped y-axis), false otherwise
	 */
	private boolean mouseInGrid() {
		return (gridFrame.touches(pa.mouseX, pa.mouseY) && gridFrame.getX1() + cellWidth < pa.mouseX);
	}
	
	/**
	 * Maps the mouse's x-coordinate to an index of a note in the phrase.
	 * @return The index.
	 */
	private int mouseToIndex() {
		return (int)pa.map(pa.mouseX, 
			               gridFrame.getX1() + cellWidth, gridFrame.getX2(),
			               0, rowSize()) + hScrollbar.getLowTick();
	}
	
	/**
	 * Maps the mouse's x-coordinate to a pitch value.
	 * @return The pitch value.
	 */
	private int mouseToPitch() {
		int pitchIndex = (int)pa.map(pa.mouseY, gridFrame.getY2(), gridFrame.getY1(), 0, numKeys());
		int pitch = pa.currentScale.getNoteValue(pitchIndex) + minOctave*12;
		return pitch;
	}
	
	/*********************************
	 ***** Music/Grid Conversion *****
	 *********************************/

	/**
	 * Converts a y-value to a pitch value, using the coordinate system of the grid.
	 * @param y The y-value.
	 * @return The pitch value.
	 */
	private int yToPitch(float y) {
		int pitchIndex = (int)pa.map(y + cellHeight/2f, gridFrame.getY2(), gridFrame.getY1(), 0, numKeys());
		int pitch = pa.currentScale.getNoteValue(pitchIndex) + minOctave*12;
		return pitch;
	}
	
	/**
	 * Converts a pitch value to a y-value, using the coordinate system of the grid.
	 * @param pitch The pitch value.
	 * @return The y-value.
	 */
	private float pitchToY(int pitch) {
		int pitchIndex = pa.currentScale.getIndexOfNoteValue(pitch - minOctave*12) + 1;
		float y = pa.map(pitchIndex, 0, numKeys(), gridFrame.getY2(), gridFrame.getY1());
		return y;
	}
	
	/******************************
	 ***** Drawing and Update *****
	 ******************************/
	
	@Override
	public void drawWhilePaused() {
		prev_t = pa.millis();
	}
	
	@Override
	public void draw() {
		pa.background(255);
		
		int dt = pa.millis() - prev_t;
		pa.playerInfo.minutesSpentWithEditor += PhasesPApplet.millisToMinutes(dt);
		if (playToggle.getValue() != 0) {
			livePlayer.update(dt * pa.getBPMS1());
		}
		prev_t = pa.millis();
		
		checkUnlocks();
		
		drawBody();
		drawToolbar();
		pa.drawControlP5();
		
		drawCursor();
	}
	
	/**
	 * Draws the cursor when the PApplet is not already drawing the cursor.
	 */
	private void drawCursor() {
		if (cursorIsX) {
			pa.stroke(0);
			pa.strokeWeight(4);
			
			float component = 1.414f * xCursorRadius;
			pa.line(pa.mouseX - component, pa.mouseY - component,
					pa.mouseX + component, pa.mouseY + component);
			pa.line(pa.mouseX - component, pa.mouseY + component,
					pa.mouseX + component, pa.mouseY - component);
		}
	}
	
	/**
	 * Checks if any Editor-related thing should be unlocked,
	 * and performs the unlocking for anything that should be unlocked.
	 */
	private void checkUnlocks() {
		if (pa.playerInfo.nextEditorUnlockIndex < unlockTimes.length
				&& pa.playerInfo.minutesSpentWithEditor >= unlockTimes[pa.playerInfo.nextEditorUnlockIndex]) {
			pa.playerInfo.nextEditorUnlockIndex++;
			showUnlockedControllers();
		}
	}
	
	/**
	 * Draws the stuff apart from the grid.
	 */
	private void drawToolbar() {
		cp5.draw();
		checkMenus();
	}
	
	/**
	 * Draws the grid and the stuff behind the grid.
	 */
	private void drawBody() {
		//draw ghost image of grid
		float ghostCellWidth = cellWidth * pa.getBPM2() / pa.getBPM1();
		drawGrid(pa.lerpColor(PhasesPApplet.getColor2(), pa.color(255), 0.8f), ghostCellWidth);
		drawPhrase(pa.lerpColor(PhasesPApplet.getColor1(), pa.color(255), 0.8f),
				   pa.lerpColor(PhasesPApplet.getColor2(), pa.color(255), 0.8f), pa.color(255), ghostCellWidth);
			
		//draw grid
		drawGrid(PhasesPApplet.getColor2(), cellWidth);
		drawPhrase(PhasesPApplet.getColor1(), PhasesPApplet.getColor2(), 0, cellWidth);
		pa.noStroke();
		pa.fill(255);
		pa.rect(0, gridFrame.getY1(), cellWidth, gridFrame.getHeight());
		
		drawPiano();
	}
	
	/**
	 * Checks if either the rootMenu or the scaleMenu have changed state.
	 * If either has changed state, that means the current scale has changed,
	 * and the editor needs to change state accordingly.
	 */
	private void checkMenus() {
		if (rootLabel != rootMenu.getLabel() || scaleLabel != scaleMenu.getLabel()) {
			rootLabel = rootMenu.getLabel();
			scaleLabel = scaleMenu.getLabel();
			Scale newScale = pa.getScale(rootLabel, scaleLabel);
			changeScale(newScale);
		}
	}
	
	/**
	 * Changes the current scale to the new scale, w/r/t to the global
	 * currentScale variable and w/r/t to the Editor's own state.
	 * @param newScale The new scale.
	 */
	private void changeScale(Scale newScale) {
		float[] ys = new float[pa.currentPhrase.getGridRowSize()];
		for (int i=0; i<pa.currentPhrase.getGridRowSize(); i++) {
			int pitch = (int)pa.currentPhrase.getGridPitch(i);
			ys[i] = pitchToY(pitch);
		}

		pa.currentScale = newScale;
		pa.currentPhrase.setScaleClassName(newScale.getClassName());
		pa.currentPhrase.setScaleRootName(newScale.getName());
		
		for (int i=0; i<pa.currentPhrase.getGridRowSize(); i++) {
			if (pa.currentPhrase.getGridPitch(i) > 0) {
				int pitch = yToPitch(ys[i]);
				pa.currentPhrase.setGridPitch(i, pitch);
			}
		}
	}
	
	/**
	 * Interprets the Phrase data as geometry and draws it to the grid.
	 */
	private void drawPhrase(int inactiveColor, int activeColor, int strokeColor, float cellWidth) {
		pa.strokeWeight(1.5f);
		pa.stroke(strokeColor);
		pa.rectMode(pa.CORNER);
		float x = gridFrame.getX1() + cellWidth;
		
		pa.pushMatrix();
		
		pa.translate(-hScrollbar.getLowTick() * cellWidth, 0);
		
		for (int i=0; i<pa.currentPhrase.getNumNotes(); i++) {
			float numCellsWide = pa.currentPhrase.getSCDuration(i) / pa.currentPhrase.getUnitDuration();
			if (pa.currentPhrase.getSCDynamic(i) != 0) {
				if (i == activeNoteIndex) {
					pa.fill(activeColor);
				}
				else {
					pa.fill(inactiveColor);
				}
				
				int pitch = pa.currentPhrase.getSCPitch(i);
				
				float y = pitchToY(pitch);
				if (y >= gridFrame.getY1() - 1f) {
					pa.rect(x, y, cellWidth*numCellsWide, cellHeight);
				}
			}
			
			x += (cellWidth*numCellsWide);
		}
		pa.strokeWeight(1);
		
		pa.popMatrix();
	}
	
	/**
	 * Draws the empty grid.
	 * @param strokeColor
	 */
	private void drawGrid(int strokeColor, float cellWidth) {
		pa.strokeWeight(1);
		pa.stroke(strokeColor);
		
		//vertical lines
		int numQuarterNotes = (int)(pa.currentPhrase.getTotalDuration() / 0.25f) - hScrollbar.getLowTick();
		float x = gridFrame.getX1() + cellWidth;
		for (int i=0; i<numQuarterNotes; i++) {
			pa.line(x, gridFrame.getY1(), x, gridFrame.getY2());
			x += cellWidth;
		}
		
		//horizontal lines
		float y = gridFrame.getY1();
		pa.line(gridFrame.getX1() + cellWidth, y, x, y);
		y += cellHeight;
		for (int i=0; i<numKeys(); i++) {
			pa.line(gridFrame.getX1() + cellWidth, y, x, y);		
			y += cellHeight;
		}
		
		pa.line(x, gridFrame.getY1(), x, gridFrame.getY2());
	}
	
	/**
	 * Draws the piano, which serves as the y-axis of the grid.
	 */
	private void drawPiano() {
		pa.rectMode(pa.CORNER);
		pa.stroke(PhasesPApplet.getColor2());
		float y = gridFrame.getY2() - cellHeight;
		
		pa.textAlign(pa.CENTER, pa.CENTER);
		pa.textFont(pa.pfont18);
		pa.textSize(16);
		//for (int i=pitchOffset; i<numKeys+pitchOffset; i++) {
		for (int i=0; i<numKeys(); i++) {
			int iModScaleSize = i % pa.currentScale.size();
			int noteValueMod12 = pa.currentScale.getNoteValue(iModScaleSize) % 12;
			int keyColor = keyColors[noteValueMod12];
			pa.fill(keyColor);
			pa.rect(gridFrame.getX1(), y, cellWidth, cellHeight);
			
			if (labelPianoKeys) {
				String noteName = pa.currentScale.getNoteNameByIndex(iModScaleSize);
				int inverseKeyColor = (keyColor == W) ? B : W;
				pa.fill(inverseKeyColor);
				pa.text(noteName, gridFrame.getX1(), y, cellWidth, cellHeight);
			}
			
			y -= cellHeight;
		}
	}
	
	/*************************
	 ***** Miscellaneous *****
	 *************************/
	
	/**
	 * Sets the cursor to be the cursor given by PApplet.cursor().
	 */
	private void defaultCursor() {
		pa.cursor();
		cursorIsX = false;
	}
	
	/**
	 * Sets the cursor to be in the shape of an x.
	 */
	private void xCursor() {
		pa.noCursor();
		cursorIsX = true;
	}
	
	/**
	 * 
	 * @return The default dynamic value for notes created in the Editor
	 */
	private float defaultDynamic() {
		return 50 + pa.random(-5, 5);
	}
	
	/*******************************
	 ***** Getters and Setters *****
	 *******************************/
	
	/**
	 * Constructs a copy of the controller, one that looks just like the one that appears in the Editor.
	 * 
	 * @param bpmDifferenceSlider The slider this controller is positioned in relation to.
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the controller.
	 * @return The controller.
	 */
	public Toggle copyPlayToggle(Slider bpmDifferenceSlider, ControlP5 cp5, Object listener) {
		Toggle t = consPlayToggle(bpmDifferenceSlider, cp5, listener);
		if (!this.playToggle.isVisible()) {
			t.hide();
		}
		return t;
	}
	
	/**
	 * Constructs a copy of the controller, one that looks just like the one that appears in the Editor.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the controller.
	 * @param scaleMenu The scale menu that this controller is defined in relation to.
	 * @param windowWidth The width of the window in which this controller will be drawn.
	 * @param windowHeight The heigh of the window in which this controller will be drawn.
	 * @return The controller.
	 */
	public Slider copyBPMSlider(ControlP5 cp5, Object listener,
			DropdownListPlus scaleMenu, int windowWidth, int windowHeight) {
		Slider s = consBPMSlider(windowWidth, windowHeight, 60, scaleMenu, cp5, listener);
		if (!this.bpmSlider.isVisible()) {
			s.hide();
		}
		return s;
	}
	
	/**
	 * Constructs a copy of the controller, one that looks just like the one that appears in the Editor.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the controller.
	 * @param bpmSlider The bpm slider that this controller is positioned in relation to.
	 * @param windowWidth The width of the window in which this controller will be drawn.
	 * @param windowHeight The heigh of the window in which this controller will be drawn.
	 * @return The controller.
	 */	
	public Slider copyBPMDifferenceSlider(ControlP5 cp5, Object listener,
			Slider bpmSlider, int windowWidth, int windowHeight) {
		Slider s = consBPMDifferenceSlider(windowWidth, windowHeight, 0.5f, bpmSlider, cp5, listener);
		if (!this.bpmDifferenceSlider.isVisible()) {
			s.hide();
		}
		return s;
	}
	
	/**
	 * Constructs a copy of the controller, one that looks just like the one that appears in the Editor.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the controller.
	 * @return The controller.
	 */
	public DropdownListPlus copyRootMenu(ControlP5 cp5, Object listener) {
		DropdownListPlus r = consRootMenu(cp5, listener);
		formatLabel(r);
		if (!this.rootMenu.isVisible()) {
			r.hide();
		}
		return r;
	}
	
	/**
	 * Constructs a copy of the controller, one that looks just like the one that appears in the Editor.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the controller.
	 * @return The controller.
	 */
	public DropdownListPlus copyScaleMenu(ControlP5 cp5, Object listener) {
		DropdownListPlus s = consScaleMenu(cp5, listener);
		formatLabel(s);
		if (!this.scaleMenu.isVisible()) {
			s.hide();
		}
		return s;
	}
	
	/**
	 * Constructs a copy of the controller, one that looks just like the one that appears in the Editor.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the controller.
	 * @param ticksPerScroller The number of ticks in the scroller (see Scrollbar documentation).
	 * @param ticksPerTrack The number of ticks in the track (see Scrollbar documentation).
	 * @param screenHeight The height of the screen this controller is being defined in.
	 * @param gridFrame The shape of the grid this controller is positioned in relation to.
	 * @return The controller.
	 */
	public Scrollbar copyHScrollbar(ControlP5 cp5, Object listener,
			int ticksPerScroller, int ticksPerTrack, int screenHeight, Rect gridFrame) {
		Scrollbar s = consHorizontalScrollbar(cp5, listener, ticksPerScroller, ticksPerTrack, screenHeight, gridFrame);
		if (!this.hScrollbar.isVisible()) {
			s.hide();
		}
		return s;
	}
	
	/**
	 * Constructs a copy of the controller, one that looks just like the one that appears in the Editor.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the controller.
	 * @param hScrollbar The scrollbar that this controller is positioned in relation to.
	 * @return The controller.
	 */
	public Button copySubNoteButton(ControlP5 cp5, Object listener, Scrollbar hScrollbar) {
		Button b = consSubNoteButton(cp5, listener, hScrollbar);
		if (!this.subNoteButton.isVisible()) {
			b.hide();
		}
		return b;
	}
	
	/**
	 * Constructs a copy of the controller, one that looks just like the one that appears in the Editor.
	 * 
	 * @param cp5 The ControlP5 instance.
	 * @param listener The object that should receive callbacks from the controller.
	 * @param hScrollbar The scrollbar that this controller is positioned in relation to.
	 * @return The controller.
	 */
	public Button copyAddNoteButton(ControlP5 cp5, Object listener, Scrollbar hScrollbar) {
		Button b = consAddNoteButton(cp5, listener, hScrollbar);
		if (!this.addNoteButton.isVisible()) {
			b.hide();
		}
		return b;
	}
	
	/**
	 * Constructs a copy of the shape of the grid, one that is just like the shape of the Editor's grid.
	 * 
	 * @param windowWidth The width of the window in which the grid shape will be used.
	 * @param windowHeight The height of the window in which the grid shape will be used.
	 * @return The shape of the grid.
	 */
	public Rect copyGridFrame(int windowWidth, int windowHeight) {
		return consGridFrame(windowWidth, windowHeight);
	}
}