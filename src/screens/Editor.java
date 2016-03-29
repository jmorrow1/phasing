package screens;

import java.lang.reflect.Method;

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
import util.FloatFormatter;

/**
 * Provides an editor in which the user can create and edit musical phrases.
 * @author James Morrow
 *
 */
public class Editor extends Screen {
	//time
	private int prev_t;
	private float[] unlockTimes = new float[] {0, 1, 2, 3, 4, 5};
	
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
	private int cellHeight = 20;
	private int cellWidth = 60;
	
	//interaction w/ grid
	private final int NOT_DRAWING=-1, DRAWING_NOTE=0, DRAWING_REST=1;
	private int drawState = NOT_DRAWING;
	private int startIndexOfUserDrawnNote = -1;
	private int indexMousePressed=-1, pitchMousePressed=-1;
	
	//cp5
	private final static int BPM_1 = 1, BPM_DIFFERENCE = 2;
	private int maxPhaseDifferenceAmplitude = 5;
	private ControlP5 cp5;
	private Toggle playToggle;
	private Slider bpmSlider, bpmDifferenceSlider;
	private DropdownList rootMenu, scaleMenu;
	private Scrollbar hScrollbar;
	private Button subNoteButton, addNoteButton;
	private String rootLabel, scaleLabel;
	private boolean rootMenuOpen, scaleMenuOpen;
	
	//controller layout
	private int controller_dx = 15;
	private int margin_btwn_top_toolbar_y2_and_controllers = 10;
	
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
		initCP5Objects();
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
		return pa.height - 40;
	}
	
	/**
	 * Gives the proper value for the width of the hScrollbar,
	 * which is dependent on the width of the grid.
	 * @return The proper width of the hScrollbar.
	 */
	private int hScrollbarWidth() {
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
		float gridHeight = cellHeight * (int) ((botToolbarY1() - pa.topToolbarY2()) / cellHeight);
		gridFrame = new Rect(10, pa.topToolbarY2(), pa.width-10, pa.topToolbarY2() + gridHeight, pa.CORNERS);
	}
	
	/**
	 * Initializes the object that plays music in the editor.
	 */
	private void initMusicPlayer() {
		try {
			Method callback = Editor.class.getMethod("animate", SoundCipherPlus.class);
			livePlayer = new SoundCipherPlus(pa, pa.currentPhrase, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initalizes the ControlP5 object and controllers, in addition to adding the controllers to the CP5 object.
	 */
	private void initCP5Objects() {
		cp5 = new ControlP5(pa);
		cp5.setAutoDraw(false);
		initScaleMenus();
		initBPMSliders(scaleMenu);
		initPlayStopToggle(bpmDifferenceSlider);
		initHorizontalScrollbar();
	    initSubNoteButton(hScrollbar);
	    initAddNoteButton(hScrollbar);
	}
	
	/**
	 * Initializes the button that decreases phrase length note-by-note.
	 * Adds the button to the CP5 object.
	 * @param hScrollbar This is here to make explicit that this method relies on hScrollbar being already initialized.
	 */
	private void initSubNoteButton(Scrollbar hScrollbar) {
		int sideLength = 24;
		float botToolbarHeight = pa.height - botToolbarY1();
		
		subNoteButton = cp5.addButton("decreasePhraseLength")
			               .setPosition(Util.getX1(hScrollbar) - sideLength - 10,
			            		        Util.getY2(hScrollbar) - hScrollbar.getHeight()/2f - sideLength/2f)
			               .setSize(sideLength, sideLength)
			               .setView(new PlusMinusButtonView(false))
			               .plugTo(this)
			               ;
		colorController(subNoteButton);
	}
	
	/**
	 * Initializes the button that increases phrase length note-by-note.
	 * Adds the button to the CP5 object.
	 * @param hScrollbar This is here to make explicit that this method relies on hScrollbar being already initialized.
	 */
	private void initAddNoteButton(Scrollbar hScrollbar) {
		int sideLength = 24;
		
		addNoteButton = cp5.addButton("increasePhraseLength")
				           .setPosition(Util.getX2(hScrollbar) + 10,
				        		        Util.getY2(hScrollbar) - hScrollbar.getHeight()/2f - sideLength/2f)
				           .setSize(sideLength, sideLength)
				           .setView(new PlusMinusButtonView(true))
				           .plugTo(this)
				           ;
		colorController(addNoteButton);
	}
	
	/**
	 * Initializes the scrollbar that controls which sub-section of the phrase is displayed.
	 * Adds the scrollbar to the CP5 object.
	 */
	private void initHorizontalScrollbar() {
		hScrollbar = new Scrollbar(cp5, "hScrollbar", PApplet.min(rowSize(), pa.currentPhrase.getGridRowSize()), pa.currentPhrase.getGridRowSize());
	    hScrollbar.setPosition(gridFrame.getX1() + 40, pa.height - 25f)
			      .setSize(hScrollbarWidth(), 15)
			      .plugTo(this)
			      ;
	    colorController(hScrollbar);
	}
	
	/**
	 * Initalizes the toggle that controls whether or not music plays.
	 * Adds the toggle to the CP5 object.
	 * @param bpmDifferenceSlider This is here to make explicit that this method relies on bpmDifferenceSlider being already initialized.
	 */
	private void initPlayStopToggle(Slider bpmDifferenceSlider) {
		int sideLength = 35;
		playToggle = cp5.addToggle("play")
		                .setPosition(Util.getX2(bpmDifferenceSlider) + this.controller_dx,
		        		             pa.topToolbarY2() - margin_btwn_top_toolbar_y2_and_controllers - sideLength)
					    .setSize(sideLength, sideLength)
					    .plugTo(this)
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
		colorController(playToggle);
	}
	
	/**
	 * Initializes the sliders that control the tempos (in beats per minute) of the two players.
	 * Adds the sliders to the CP5 object.
	 * @param scaleMenu This is here to make explicit that this method relies on scaleMenu being already initialized.
	 */
	private void initBPMSliders(DropdownList scaleMenu) {
		int sliderWidth = getSliderWidth();
		int sliderHeight = 23;
		bpmSlider = consBPMSlider(Util.getX2(scaleMenu) + controller_dx, 
				                  pa.topToolbarY2() - margin_btwn_top_toolbar_y2_and_controllers - sliderHeight,
				                  (int)(1.1f * sliderWidth),
				                  sliderHeight);
		
		bpmDifferenceSlider = consBPMDifferenceSlider(Util.getX2(bpmSlider) + controller_dx,
				                                     pa.topToolbarY2() - margin_btwn_top_toolbar_y2_and_controllers - sliderHeight,
				                                     (int)(0.9f * sliderWidth), 
				                                     sliderHeight);
	}
	
	/**
	 * Constructs the slider that controls the first player's tempo (in beats per minute)
	 * at a given location and with a given size.
	 * 
	 * @param x The leftmost x-coordinate of the slider.
	 * @param y The uppermost y-coordinate of the slider.
	 * @param w The width of the slider.
	 * @param h The height of the slider.
	 * @return The slider.
	 */
	private Slider consBPMSlider(float x, float y, int w, int h) {
		return consBPMSlider("beatsPerMinute", "Beats Per Minute", BPM_1,
			                x, y, w, h,
			                pa.getBPM1(),
			                11, 100,
			                1,
			                (floatingPoint) -> "" + PApplet.round(floatingPoint));
	}
	
	/**
	 * Constructs the slider that controls the difference between the 1st player's tempo and the 2nd player's tempo (in beats per minute)
	 * at a given location and with a given size.
	 * 
	 * @param x The leftmost x-coordinate of the slider.
	 * @param y The uppermost y-coordinate of the slider.
	 * @param w The width of the slider.
	 * @param h The height of the slider.
	 * @return The slider.
	 */
	private Slider consBPMDifferenceSlider(float x, float y, int w, int h) {
		return consBPMSlider("bpmDifference", "Difference ", BPM_DIFFERENCE,
			                x, y, w, h,
			                pa.getBPM2() - pa.getBPM1(),
			                -maxPhaseDifferenceAmplitude, maxPhaseDifferenceAmplitude,
			                4, 
			                (floatingPoint) -> String.format("%.2f", floatingPoint));
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
	 * @return The slider.
	 */
	private Slider consBPMSlider(String name, String label, int id, float x, float y, int w, int h,
			  float value, int minValue, int maxValue, int ticksPerWholeNumber, FloatFormatter f) {
		Slider s = new SliderPlus(cp5, name, pa.pfont12, pa.pfont18, f);
        s.setId(id);
        s.setCaptionLabel(label);
        s.setDecimalPrecision(0);
        s.setRange(minValue, maxValue);
        s.setPosition(x, y);
        s.setSize(w, h);
        s.setValue(value);
        s.setLabelVisible(false);
        s.setNumberOfTickMarks((maxValue-minValue) * ticksPerWholeNumber + 1);
        s.plugTo(this);
		colorController(s);
		return s;
	}
	
	/**
	 * Initializes the drop-down menus that control what scale is active.
	 * Adds the menus to the CP5 object.
	 */
	private void initScaleMenus() {
		int menuItemHeight = 22;
		rootMenu = new DropdownListPlus(cp5, "root");
		rootMenu.setPosition(pa.getChangeScreenButtonX2() + controller_dx, 
				             pa.topToolbarY2() - margin_btwn_top_toolbar_y2_and_controllers - menuItemHeight)
			    .setSize(90, menuItemHeight*(pa.roots.length+1))
			    .addItems(pa.roots)
			    .setItemHeight(menuItemHeight)
			    .setBarHeight(menuItemHeight)
			    .close()
			    ;
		rootMenu.setLabel(pa.currentScale.getName());
		colorController(rootMenu);
		formatLabel(rootMenu);
		rootLabel = rootMenu.getLabel();
		
		scaleMenu = new DropdownListPlus(cp5, "Scale");
		scaleMenu.setPosition(rootMenu.getPosition()[0] + rootMenu.getWidth() + controller_dx,
				              pa.topToolbarY2() - margin_btwn_top_toolbar_y2_and_controllers - menuItemHeight)
		         .setSize(130, menuItemHeight*(pa.scaleTypes.size()+1))
				 .addItems(pa.scaleTypes)
				 .setItemHeight(menuItemHeight)
				 .setBarHeight(menuItemHeight)
				 .close()
				 ;
		scaleMenu.setLabel(pa.currentScale.getClassName());
		colorController(scaleMenu);
		formatLabel(scaleMenu);
		scaleLabel = scaleMenu.getLabel();
	}
	
	/**
	 * Changes the style information of the given controller's label to reformat it.
	 * @param x The given controller, a DropdownList.
	 */
	private void formatLabel(DropdownList x) {
		x.getCaptionLabel().toUpperCase(false);
		x.getValueLabel().toUpperCase(false);
		x.getCaptionLabel().setFont(pa.pfont18);
		x.getValueLabel().setFont(pa.pfont18);
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
				rootMenu.show();
				if (pa.playerInfo.nextEditorUnlockIndex > 2) {
					scaleMenu.show();
					if (pa.playerInfo.nextEditorUnlockIndex > 3) {
						bpmSlider.show();
						if (pa.playerInfo.nextEditorUnlockIndex > 4) {
							bpmDifferenceSlider.show();
							if (pa.playerInfo.nextEditorUnlockIndex > 5) {
								hScrollbar.show();
								addNoteButton.show();
								subNoteButton.show();
							}
						}
					}
				}
			}
		}	
	}

	private int getSliderWidth() {
		return (int)PApplet.constrain(PApplet.map(pa.width, 800, 1366, 160, 442), 160, 442);
	}
	
	/**
	 * Changes the way the given controller is colored.
	 * The way it does that is based on the controller's subtype.
	 * @param c The given controller.
	 */
	private void colorController(Controller c) {
		c.setColorCaptionLabel(pa.color(255));
	    c.setColorValueLabel(pa.color(255));
		if (c instanceof DropdownList) {
			c.setColorBackground(pa.getColor1());
			c.setColorActive(pa.getColor1Bold());
			c.setColorForeground(pa.getColor1Bold());
		}
		else if (c instanceof Slider || c instanceof Scrollbar) {
			c.setColorBackground(pa.lerpColor(pa.getColor1(), pa.color(255), 0.3f));
		    c.setColorActive(pa.getColor1Bold());
		    c.setColorForeground(pa.getColor1());
		}
		else if (c instanceof Button) {
			c.setColorBackground(pa.color(255));
		    c.setColorForeground(pa.getColor1());
		    c.setColorActive(pa.getColor1Bold());
		}
		else if (c instanceof Toggle) {
			c.setColorBackground(pa.getColor1());
			c.setColorForeground(pa.getColor1Bold());
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
	
	/**
	 * Callback from livePlayer
	 * @param livePlayer
	 */
	public void animate(SoundCipherPlus livePlayer) {
		activeNoteIndex = livePlayer.getNoteIndex();
	}
	
	/*********************************
	 ***** Screen Event Handling *****
	 *********************************/
	
	@Override
	public void windowResized() {
		initGridFrame();
		cp5.dispose();
		initCP5Objects();
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
	}
	
	/**
	 * 
	 * @return True if user is simultaneously holding shift and pressing the left or right mouse button, false otherwise.
	 */
	private boolean shiftClick() {
		return pa.keyPressed && pa.key == pa.CODED && pa.keyCode == pa.SHIFT && pa.mousePressed && (pa.mouseButton == pa.LEFT || pa.mouseButton == pa.RIGHT);
	}
	
	@Override
	public void mouseWheel(MouseEvent event) {
		hScrollbar.myOnScroll(event.getCount());
	}
	
	@Override
	public void mousePressed() {
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
		//resets the Editor's state w/r/t the grid:
		drawState = NOT_DRAWING;
		startIndexOfUserDrawnNote = -1;
		pa.saveCurrentPhrasePicture(); //TODO Is it a good idea to put this here?
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
	 * 
	 * @return The default dynamic value for notes created in the Editor
	 */
	private float defaultDynamic() {
		return 50 + pa.random(-5, 5);
	}
}