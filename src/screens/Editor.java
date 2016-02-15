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
import geom.Rect;
import phasing.FloatFormatter;
import phasing.PhasesPApplet;
import phasing.Phrase;
import phasing.Scale;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;
import soundcipher.SoundCipherPlus;

/**
 * Provides an editor in which the user can create and edit musical phrases for the Presenter screen.
 * @author James Morrow
 *
 */
public class Editor extends Screen {
	//time
	private long prev_t;
	private long timeEntered;
	
	//playback
	private SoundCipherPlus livePlayer;
	private float notept = 0;
	
	//animation
	private final int NOT_APPLICABLE = -1;
	private int activeNoteIndex = NOT_APPLICABLE;
	
	//piano
	private boolean labelPianoKeys = true;
	private int minOctave = 5;
	private int numKeys = 24;
	private final static int W=0xffffffff, B=PhasesPApplet.getColor2();
	private final static int[] keyColors = new int[] {W, B, W, B, W, W, B, W, B, W, B, W};
	
	//grid
	private Rect gridFrame;
	private int rowSize;
	private int columnSize = numKeys;
	private float cellWidth, cellHeight;
	
	//interaction w/ grid
	private final int NOT_DRAWING=-1, DRAWING_NOTE=0, DRAWING_REST=1;
	private int drawState = NOT_DRAWING;
	private int startIndexOfUserDrawnNote = -1;
	private int indexMousePressed=-1, pitchMousePressed=-1;
	
	//controllers
	private final static int BPM_1 = 1, BPM_DIFFERENCE = 2;
	private int maxPhaseDifferenceAmplitude = 10;
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
	
	/**
	 * 
	 * @param pa The PApplet to draw to
	 */
	public Editor(PhasesPApplet pa) {
		super(pa);
		
		//init playback variables
		try {
			Method callback = Editor.class.getMethod("animate", SoundCipherPlus.class);
			livePlayer = new SoundCipherPlus(pa, pa.currentPhrase, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		//other things dependent on screen size
		gridFrame = new Rect(10, 60, pa.width-10, pa.height-40, pa.CORNERS);
		switch (pa.screenSizeMode) {
			case PhasesPApplet._800x600 :
				rowSize = 12;
				break;
			case PhasesPApplet._1366x768 :
				rowSize = 18;
				break;
		}
		
		cellWidth = gridFrame.getWidth() / (rowSize+1);
		cellHeight = gridFrame.getHeight() / columnSize;
		
		//cp5
		cp5 = new ControlP5(pa);
		cp5.setAutoDraw(false);
		setupControllers();
	}

	/****************************
	 ***** Controller Setup *****
	 ****************************/
	
	private void setupControllers() {
		//scale menus
		int menuItemHeight = 22;
		rootMenu = new DropdownListPlus(cp5, "root");
		rootMenu.setPosition(pa.changeScreenButtonX2 + controller_dx, pa.changeScreenButtonY2 - menuItemHeight)
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
				              pa.changeScreenButtonY2 - menuItemHeight)
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
		
		//bpm sliders
		int sliderWidth = getSliderWidth();
		int sliderHeight = 23;
		bpmSlider = addBpm1Slider(scaleMenu.getPosition()[0] + scaleMenu.getWidth() + controller_dx, 
				                  pa.changeScreenButtonY2 - sliderHeight,
				                  sliderWidth,
				                  sliderHeight);
		
		bpmDifferenceSlider = addBpmDifferenceSlider(bpmSlider.getPosition()[0] + sliderWidth + controller_dx,
				                                     pa.changeScreenButtonY2 - sliderHeight,
				                                     sliderWidth, 
				                                     sliderHeight);
		
		//play stop toggle
		playToggle = cp5.addToggle("play")
				        .setPosition(pa.width - 50, pa.changeScreenButtonY2 - 35)
					    .setSize(35, 35)
					    .plugTo(this)
					    .setView(new ControllerView<Toggle>() {
						    @Override
							public void display(PGraphics pg, Toggle t) {
						    	if (t.getValue() == 0) {
						    		//draw white rect under play button
									pg.rectMode(pg.CORNER);
									pg.fill(255);
									pg.stroke(255);
									pg.rect(0, 0, t.getWidth(), t.getHeight());
						    	}
						    	
								if (t.isMouseOver()) {
									pg.stroke(0);
									pg.fill(t.getColor().getForeground());
								}
								else {
									pg.stroke(0);
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
		
		//horizontal scrollbar
		hScrollbar = new Scrollbar(cp5, "hScrollbar", PApplet.min(rowSize, pa.currentPhrase.getGridRowSize()), pa.currentPhrase.getGridRowSize());
	    hScrollbar.setPosition(gridFrame.getX1() + 40, pa.height - 25f)
			      .setSize((int)gridFrame.getWidth() - 80, 15)
			      .plugTo(this)
			      ;
	    colorController(hScrollbar);
	    
		//add buttons that flank the scrollbar and control the adding and removing of notes from the phrase
	    subNoteButton = cp5.addButton("decreasePhraseLength")
						    .setPosition(gridFrame.getX1(), pa.height - 30f)
						    .setSize(24, 24)
						    .setView(new PlusMinusButtonView(false))
						    .plugTo(this)
						    ;
	    colorController(subNoteButton);
		addNoteButton = cp5.addButton("increasePhraseLength")
						   .setPosition(gridFrame.getX2() - 24, pa.height - 30f)
						   .setSize(24, 24)
						   .setView(new PlusMinusButtonView(true))
						   .plugTo(this)
						   ;
	    colorController(addNoteButton);
	    cp5.hide();
	    hideAllControllers();
	}
	
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
	
	private void showUnlockedControllers() {
		if (pa.playerInfo.numEditorVisits >= 1) {
			playToggle.show();
			if (pa.playerInfo.numEditorVisits >= 2) {
				rootMenu.show();
				if (pa.playerInfo.numEditorVisits >= 3) {
					scaleMenu.show();
					if (pa.playerInfo.numEditorVisits >= 4) {
						bpmSlider.show();
						if (pa.playerInfo.numEditorVisits >= 5) {
							bpmDifferenceSlider.show();
							if (pa.playerInfo.numEditorVisits >= 6) {
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
		switch(pa.screenSizeMode) {
			case PhasesPApplet._800x600 : return 160;
			case PhasesPApplet._1366x768 : return 442;
			default : return 100;
		}
	}
	
	private void formatLabel(DropdownList x) {
		x.getCaptionLabel().toUpperCase(false);
		x.getValueLabel().toUpperCase(false);
		x.getCaptionLabel().setFont(pa.pfont18);
		x.getValueLabel().setFont(pa.pfont18);
		x.getCaptionLabel().getStyle().paddingTop += 5;
		x.getValueLabel().getStyle().paddingTop += 5;
	}
	
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
		    c.setColorActive(pa.getColor1());
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
	
	private Slider addBpm1Slider(float x, float y, int w, int h) {
		return addBPMSlider("beatsPerMinute", "Beats Per Minute", BPM_1,
			                x, y, w, h,
			                pa.getBPM1(),
			                1, 100,
			                1,
			                (floatingPoint) -> "" + PApplet.round(floatingPoint));
	}
	
	private Slider addBpmDifferenceSlider(float x, float y, int w, int h) {
		return addBPMSlider("bpmDifference", "Difference", BPM_DIFFERENCE,
			                x, y, w, h,
			                pa.getBPM2() - pa.getBPM1(),
			                -maxPhaseDifferenceAmplitude, maxPhaseDifferenceAmplitude,
			                4, 
			                (floatingPoint) -> String.format("%.2f", floatingPoint));
	}
	
	private Slider addBPMSlider(String name, String label, int id, float x, float y, int w, int h,
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

	public void hScrollbar(ControlEvent e) {
		drawBody();
	}
	
	/********************************
	 ***** Controller Callbacks *****
	 ********************************/
	
	public void decreasePhraseLength(ControlEvent e) {
		pa.currentPhrase.removeLastCell();
		if (pa.currentPhrase.getGridRowSize() <= rowSize) {
			hScrollbar.setNumTickMarks(pa.currentPhrase.getGridRowSize());
			hScrollbar.setTicksPerScroller(pa.currentPhrase.getGridRowSize());
		}
		else {
			hScrollbar.setNumTickMarks(pa.currentPhrase.getGridRowSize());
			hScrollbar.setTicksPerScroller(rowSize);
		}
		drawBody();		
	}
	
	public void increasePhraseLength(ControlEvent e) {
		pa.currentPhrase.appendCell();
		if (pa.currentPhrase.getGridRowSize() <= rowSize) {
			hScrollbar.setNumTickMarks(pa.currentPhrase.getGridRowSize());
			hScrollbar.setTicksPerScroller(pa.currentPhrase.getGridRowSize());
		}
		else {
			hScrollbar.setNumTickMarks(pa.currentPhrase.getGridRowSize());
			hScrollbar.setTicksPerScroller(rowSize);
		}
		drawBody();
	}
	
	public void beatsPerMinute(ControlEvent e) {
		pa.setBPM1(e.getValue());
		livePlayer.tempo(pa.getBPM1());
		pa.setBPM2(e.getValue() + bpmDifferenceSlider.getValue());
		drawBody();
	}
	
	public void bpmDifference(ControlEvent e) {
		pa.setBPM2(pa.getBPM1() + e.getValue());
		drawBody();
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
	
	/*************************************
	 ***** Enter/Exit Event Handling *****
	 *************************************/

	@Override
	public void onEnter() {
		cp5.show();
		showUnlockedControllers();
		drawToolbar();
		drawBody();
		timeEntered = System.currentTimeMillis();
	}
	
	@Override
	public void onExit() {
		cp5.hide();
		pa.println("visit time: " + (System.currentTimeMillis() - timeEntered));
		if (System.currentTimeMillis() - timeEntered > 10000) {
			pa.playerInfo.numEditorVisits++;
			pa.savePlayerInfo();
		}
		pa.saveCurrentPhrasePicture();
		pa.saveCurrentScale();
	}
	
	/********************************
	 ***** Input Event Handling *****
	 ********************************/
	
	private boolean shiftClick() {
		return pa.keyPressed && pa.key == pa.CODED && pa.keyCode == pa.SHIFT && pa.mousePressed && (pa.mouseButton == pa.LEFT || pa.mouseButton == pa.RIGHT);
	}
	
	public void mouseWheel(MouseEvent event) {
		hScrollbar.myOnScroll(event.getCount());
	}
	
	public void mousePressed() {
		//for drawing a note to the grid:
		if (mouseIntersectsGrid()) {
			indexMousePressed = mouseToIndex();
			pitchMousePressed = mouseToPitch();
			startIndexOfUserDrawnNote = indexMousePressed;
			if (!rootMenu.isInside() && !scaleMenu.isInside()) {
				if (pa.mouseButton == pa.LEFT && !shiftClick()) {
					boolean success = pa.currentPhrase.setCell(indexMousePressed, pitchMousePressed, defaultDynamic(), Phrase.NOTE_START);
					if (success && 
							indexMousePressed+1 < pa.currentPhrase.getGridRowSize() &&
							pa.currentPhrase.getNoteType(indexMousePressed+1) == Phrase.NOTE_SUSTAIN) {
						pa.currentPhrase.setNoteType(indexMousePressed+1, Phrase.NOTE_START);
					}
					if (success) {
						drawState = DRAWING_NOTE;
						drawBody();
					}
				}
				else if ( (pa.mouseButton == pa.RIGHT || shiftClick())) {
					drawState = DRAWING_REST;
					if (pitchMousePressed == pa.currentPhrase.getGridPitch(indexMousePressed)) {
						drawRest(indexMousePressed, pitchMousePressed);
					}
				}
			}
		}
		
		//close a menu when a mouse click occurs outside it:
		if (!rootMenu.isInside()) {
			rootMenu.close();
		}
		
		if (!scaleMenu.isInside()) {
			scaleMenu.close();
		}
	}
	
	private void drawRest(int index, int pitch) {
		boolean success = pa.currentPhrase.setCell(index, pitch, defaultDynamic(), Phrase.REST);
		pa.currentPhrase.setNoteType(index, Phrase.REST);
		if (success) {
			if (index+1 < pa.currentPhrase.getGridRowSize() && 
					pa.currentPhrase.getNoteType(index+1) == Phrase.NOTE_SUSTAIN) {
				pa.currentPhrase.setNoteType(index+1, Phrase.NOTE_START);
			}
			drawBody();
			
		}	
	}
	
	public void mouseReleased() {
		//for resetting the Editor's state w/r/t the grid:
		drawState = NOT_DRAWING;
		startIndexOfUserDrawnNote = -1;
	}
	
	public void mouseDragged() {
		//for continuing to draw notes to the grid:
		if (drawState == DRAWING_NOTE && mouseIntersectsGrid()) {
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
					drawBody();
				}
				else if (newIndex < indexMousePressed && newIndex < startIndexOfUserDrawnNote) {
					pa.currentPhrase.setCell(newIndex, pitchMousePressed, defaultDynamic(), Phrase.NOTE_START);
					if (newIndex+1 < pa.currentPhrase.getGridRowSize()) {
						pa.currentPhrase.setCell(newIndex+1, pitchMousePressed, defaultDynamic(), Phrase.NOTE_SUSTAIN);
						indexMousePressed++;
					}
					startIndexOfUserDrawnNote = newIndex;
					drawBody();
				}
			}
			else {
				mousePressed();
			}
		}
		else if (drawState == DRAWING_REST && mouseIntersectsGrid()) {
			int index = mouseToIndex();
			int pitch = mouseToPitch();
			if (0 <= index && index < pa.currentPhrase.getGridRowSize() && pitch == pa.currentPhrase.getGridPitch(index)) {
				drawRest(index, pitch);
			}
		}
		/*else {
			userIsDrawingNote = false;
		}*/
	}
	
	/**
	 * 
	 * @return True, if the mouse intersects the grid (but not the piano-shaped y-axis), false otherwise
	 */
	private boolean mouseIntersectsGrid() {
		return (gridFrame.touches(pa.mouseX, pa.mouseY) && gridFrame.getX1() + cellWidth < pa.mouseX);
	}
	
	/**
	 * Looks at the variable pa.mouseX and its position in relation to the grid, mapping that to the index of a note in the phrase.
	 * @return The index of the note to which pa.mouseX cooresponds
	 */
	private int mouseToIndex() {
		return (int)pa.map(pa.mouseX, 
			               gridFrame.getX1() + cellWidth, gridFrame.getX2(),
			               0, rowSize) + hScrollbar.getLowTick();
	}
	
	/**
	 * Looks at the variable pa.mouseY and its position in relation to the grid, mapping that to a pitch in the phrase.
	 * @return The pitch of the note to which pa.mouseY cooresponds
	 */
	private int mouseToPitch() {
		int pitchIndex = (int)pa.map(pa.mouseY, gridFrame.getY2(), gridFrame.getY1(), 0, numKeys);
		return pa.currentScale.getNoteValue(pitchIndex) + minOctave*12;
	}
	
	/*********************************
	 ***** Music/Grid Conversion *****
	 *********************************/

	private int yToPitch(float y) {
		int pitchIndex = (int)pa.map(y + cellHeight/2f, gridFrame.getY2(), gridFrame.getY1(), 0, numKeys);
		return pa.currentScale.getNoteValue(pitchIndex) + minOctave*12;
	}
	
	private float pitchToY(int pitch) {
		int pitchIndex = pa.currentScale.getIndexOfNoteValue(pitch - minOctave*12) + 1;
		return pa.map(pitchIndex, 0, numKeys, gridFrame.getY2(), gridFrame.getY1());
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/
	
	@Override
	public void draw() {
		if (playToggle.getValue() != 0) {
			long dt = System.currentTimeMillis() - prev_t;
			prev_t = System.currentTimeMillis();
			livePlayer.update(dt * pa.getBPMS1());
			drawBody();
		}
		
		drawToolbar();
	}
	
	private void drawToolbar() {
		//top toolbar background
		pa.noStroke();
		pa.fill(255);
		pa.rectMode(pa.CORNER);
		pa.rect(0, 0, pa.width, gridFrame.getY1());
		
		//bottom toolbar background
		pa.rectMode(pa.CORNERS);
		pa.rect(0, gridFrame.getY2() + 1, pa.width, pa.height);
		
		//left toolbar background
		pa.rect(0, 0, gridFrame.getX1(), pa.height);
		
		//controllers
		cp5.draw();
		updateMenus();
	}
	
	private void drawBody() {
		//draw blank background behind grid
		pa.noStroke();
		pa.fill(255);
		pa.rectMode(pa.CORNERS);
		pa.rect(0, gridFrame.getY1(), pa.width, gridFrame.getY2());
		
		//draw ghost image of grid
		float ghostCellWidth = cellWidth * pa.getBPM2() / pa.getBPM1();
		drawGrid(pa.lerpColor(PhasesPApplet.getColor2(), pa.color(255), 0.8f), ghostCellWidth);
		drawPhrase(pa.lerpColor(PhasesPApplet.getColor1(), pa.color(255), 0.8f),
				pa.lerpColor(PhasesPApplet.getColor2(), pa.color(255), 0.8f), pa.color(255), ghostCellWidth);
		
		/*if (pa.phrase.getNumNotes() < rowSize) {
			//draw outline of grid frame
			pa.noStroke();
			pa.fill(pa.getColor2(), 25);
			pa.rectMode(pa.CORNERS);
			pa.rect(gridFrame.getX1() + cellWidth * (pa.phrase.getNumNotes()+1), gridFrame.getY1(),
					gridFrame.getX2(), gridFrame.getY2());
		}*/
			
		//draw grid
		drawGrid(PhasesPApplet.getColor2(), cellWidth);
		drawPhrase(PhasesPApplet.getColor1(), PhasesPApplet.getColor2(), 0, cellWidth);
		pa.noStroke();
		pa.fill(255);
		pa.rect(0, gridFrame.getY1(), cellWidth, gridFrame.getHeight());
		
		drawPiano();
	}
	
	private void updateMenus() {
		if (rootLabel != rootMenu.getLabel() || scaleLabel != scaleMenu.getLabel()) {
			rootLabel = rootMenu.getLabel();
			scaleLabel = scaleMenu.getLabel();
			Scale newScale = pa.getScale(rootLabel, scaleLabel);
			updateGrid(newScale);
			drawBody();
		}
		else if (rootMenu.isOpen() != rootMenuOpen || scaleMenu.isOpen() != scaleMenuOpen) {
			rootMenuOpen = rootMenu.isOpen();
			scaleMenuOpen = scaleMenu.isOpen();
			drawBody();
		}
	}
	
	/**
	 * Interprets the Phrase data and draws it to the grid.
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
				pa.rect(x, y, cellWidth*numCellsWide, cellHeight);
			}
			
			x += (cellWidth*numCellsWide);
		}
		pa.strokeWeight(1);
		
		pa.popMatrix();
	}
	
	/**
	 * Draws the empty grid.
	 * @param color
	 */
	private void drawGrid(int color, float cellWidth) {
		pa.strokeWeight(1);
		pa.stroke(color);
		
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
		for (int i=0; i<numKeys; i++) {
			pa.line(gridFrame.getX1() + cellWidth, y, x, y);		
			y += cellHeight;
		}
		
		pa.line(x, gridFrame.getY1(), x, gridFrame.getY2());
	}
	
	private void updateGrid(Scale newScale) {
		float[] ys = new float[pa.currentPhrase.getGridRowSize()];
		for (int i=0; i<pa.currentPhrase.getGridRowSize(); i++) {
			int pitch = (int)pa.currentPhrase.getGridPitch(i);
			ys[i] = pitchToY(pitch);
		}

		pa.currentScale = newScale;
		
		for (int i=0; i<pa.currentPhrase.getGridRowSize(); i++) {
			if (pa.currentPhrase.getGridPitch(i) > 0) {
				int pitch = yToPitch(ys[i]);
				pa.currentPhrase.setGridPitch(i, pitch);
			}
		}
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
		for (int i=0; i<numKeys; i++) {
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
	 * @return The default dynamic for notes created in the Editor
	 */
	private float defaultDynamic() {
		return 50 + pa.random(-5, 5);
	}
}