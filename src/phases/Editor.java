package phases;

import java.lang.reflect.Method;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.ControllerView;
import controlP5.DropdownList;
import controlP5.Slider;
import controlP5.Toggle;
import controlp5.ArrowButtonView;
import controlp5.DropdownListPlus;
import controlp5.Scrollbar;
import controlp5.SliderPlus;
import geom.Rect;
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
	//playback
	private SoundCipherPlus livePlayer;
	private long prev_t;
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
	private int rowSize = 12;
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
	private Toggle playStop;
	private Slider bpmSlider, bpmDifferenceSlider;
	private DropdownList rootMenu, scaleMenu;
	private Scrollbar scrollbar;
	private String rootLabel, scaleLabel;
	private boolean rootMenuOpen, scaleMenuOpen;
	
	/**
	 * 
	 * @param pa The PApplet to draw to
	 */
	public Editor(PhasesPApplet pa) {
		super(pa);
		
		//init playback variables
		try {
			Method callback = Editor.class.getMethod("animate", SoundCipherPlus.class);
			livePlayer = new SoundCipherPlus(pa, pa.phrase, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		//init grid variables
		gridFrame = new Rect(10, 50, pa.width-10, pa.height-40, pa.CORNERS);
		cellWidth = gridFrame.getWidth() / (rowSize+1);
		cellHeight = gridFrame.getHeight() / columnSize;
		
		//init cp5
		cp5 = new ControlP5(pa);
		cp5.setAutoDraw(false);
		
		//play stop toggle
		playStop = cp5.addToggle("play")
				      .setPosition(750, 5)
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
									pg.fill(pa.getBrightColor1());
								}
								else {
									pg.stroke(0);
									pg.fill(pa.getColor1());
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
		
		//bpm sliders
		bpmSlider = addBPMSlider(BPM_1);
		bpmDifferenceSlider = addBPMSlider(BPM_DIFFERENCE);
		
		//scale menus
		rootMenu = new DropdownListPlus(cp5, "root");
		rootMenu.setPosition(155, 19)
			    .setSize(90, 22*(PhasesPApplet.roots.length+1))
			    .addItems(PhasesPApplet.roots)
			    .setItemHeight(22)
			    .setBarHeight(22)
			    .close()
			    ;
		rootMenu.setLabel("C");
		colorController(rootMenu);
		formatLabel(rootMenu);
		rootLabel = rootMenu.getLabel();
		
		scaleMenu = new DropdownListPlus(cp5, "Scale");
		scaleMenu.setPosition(260, 19)
		         .setSize(130, 22*(PhasesPApplet.scaleTypes.size()+1))
				 .addItems(PhasesPApplet.scaleTypes)
				 .setItemHeight(22)
				 .setBarHeight(22)
				 .close()
				 ;
		scaleMenu.setLabel("Chromatic");
		colorController(scaleMenu);
		formatLabel(scaleMenu);
		scaleLabel = scaleMenu.getLabel();
		
		//add scrollbar
		scrollbar = new Scrollbar(cp5, "scrollbar", 12, 14);
	    scrollbar.setPosition(60, 570)
			     .setSize(680, 25)
			     .setColorBackground(pa.getColor1())
			     .setColorForeground(pa.getBrightColor1())
			     ;
	
		//add buttons that flank the scrollbar and control the adding and removing of notes from the phrase
	    cp5.addButton("decreasePhraseLength")
	       .setPosition(10, 570)
	       .setSize(40, 25)
	       .setView(new ArrowButtonView(false))
	       .setColorBackground(pa.color(255))
	       .setColorForeground(pa.getColor1())
	       .setColorActive(pa.getBrightColor1())
	       ;
		cp5.addButton("increasePhraseLength")
	       .setPosition(750, 570)
	       .setSize(40, 25)
	       .setView(new ArrowButtonView(true))
	       .setColorBackground(pa.color(255))
	       .setColorForeground(pa.getColor1())
	       .setColorActive(pa.getBrightColor1())
	       ;
	    
		//hide cp5
		cp5.hide();
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
		if (c instanceof DropdownList || c instanceof Button) {
			c.setColorBackground(pa.getColor1());
			c.setColorActive(pa.getBrightColor1());
			c.setColorForeground(pa.getBrightColor1());
		}
		else if (c instanceof Slider) {
			c.setColorBackground(pa.lerpColor(pa.getColor1(), pa.color(255), 0.3f));
		    c.setColorActive(pa.getColor1());
		    c.setColorForeground(pa.getColor1());
		}
	}
	
	private Slider addBPMSlider(int id) {
		switch(id) {
			case BPM_1 :
				return addBPMSlider("beatsPerMinute", "Beats Per Minute", id, 405, 18, pa.getBPM1(), 1, 100, 1,
						(x) -> "" + PApplet.round(x));
			case BPM_DIFFERENCE :
				return addBPMSlider("bpmDifference", "Difference", id, 575, 18, pa.getBPM2() - pa.getBPM1(),
						-maxPhaseDifferenceAmplitude, maxPhaseDifferenceAmplitude, 4,
						(x) -> String.format("%.2f", x));
			default :
				return null;
		}
	}
	
	private Slider addBPMSlider(String name, String label, int id, int x, int y, 
		float bpm, int minValue, int maxValue, int ticksPerWholeNumber, FloatFormatter f) {
		Slider s = new SliderPlus(cp5, name, pa.pfont12, pa.pfont18, f);
        s.setId(id);
        s.setCaptionLabel(label);
        s.setDecimalPrecision(0);
        s.setRange(minValue, maxValue);
        s.setPosition(x, y);
        s.setSize(150, 23);
        s.setValue(bpm);
        s.setLabelVisible(false);
        s.setNumberOfTickMarks((maxValue-minValue) * ticksPerWholeNumber + 1);
        s.plugTo(this);
		colorController(s);
		return s;
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
			prev_t = System.currentTimeMillis();
			livePlayer.tempo(pa.getBPM1());
			activeNoteIndex = NOT_APPLICABLE;
		}
	}
	
	/**
	 * Callback from livePlayer
	 * @param livePlayer
	 */
	public void animate(SoundCipherPlus livePlayer) {
		activeNoteIndex = livePlayer.getNoteIndex();
	}

	@Override
	public void onEnter() {
		cp5.show();
		drawToolbar();
		drawBody();
	}
	
	@Override
	public void onExit() {
		cp5.hide();
	}
	
	private boolean shiftClick() {
		return pa.keyPressed && pa.key == pa.CODED && pa.keyCode == pa.SHIFT && pa.mousePressed && (pa.mouseButton == pa.LEFT || pa.mouseButton == pa.RIGHT);
	}
	
	public void mouseWheel(MouseEvent event) {
		scrollbar.myOnScroll(event.getCount());
	}
	
	public void mousePressed() {
		//for drawing a note to the grid:
		if (mouseIntersectsGrid()) {
			indexMousePressed = mouseToIndex();
			pitchMousePressed = mouseToPitch();
			startIndexOfUserDrawnNote = indexMousePressed;
			if (!rootMenu.isInside() && !scaleMenu.isInside()) {
				if (pa.mouseButton == pa.LEFT && !shiftClick()) {
					boolean success = pa.phrase.setCell(indexMousePressed, pitchMousePressed, defaultDynamic(), Phrase.NOTE_START);
					if (success && 
							indexMousePressed+1 < pa.phrase.getGridRowSize() &&
							pa.phrase.getNoteType(indexMousePressed+1) == Phrase.NOTE_SUSTAIN) {
						pa.phrase.setNoteType(indexMousePressed+1, Phrase.NOTE_START);
					}
					if (success) {
						drawState = DRAWING_NOTE;
						drawBody();
					}
				}
				else if ( (pa.mouseButton == pa.RIGHT || shiftClick())) {
					drawState = DRAWING_REST;
					if (pitchMousePressed == pa.phrase.getGridPitch(indexMousePressed)) {
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
		boolean success = pa.phrase.setCell(index, pitch, defaultDynamic(), Phrase.REST);
		pa.phrase.setNoteType(index, Phrase.REST);
		if (success) {
			if (index+1 < pa.phrase.getGridRowSize() && 
					pa.phrase.getNoteType(index+1) == Phrase.NOTE_SUSTAIN) {
				pa.phrase.setNoteType(index+1, Phrase.NOTE_START);
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
					if (newIndex+1 < pa.phrase.getGridRowSize() &&
							(pa.phrase.getNoteType(newIndex) == Phrase.NOTE_SUSTAIN ||
							 pa.phrase.getNoteType(newIndex) == Phrase.NOTE_START) &&
							pa.phrase.getNoteType(newIndex+1) == Phrase.NOTE_SUSTAIN) {
						pa.phrase.setNoteType(newIndex+1, Phrase.NOTE_START);
						indexMousePressed++;
					}
					pa.phrase.setCell(newIndex, pitchMousePressed, defaultDynamic(), Phrase.NOTE_SUSTAIN);
					drawBody();
				}
				else if (newIndex < indexMousePressed && newIndex < startIndexOfUserDrawnNote) {
					pa.phrase.setCell(newIndex, pitchMousePressed, defaultDynamic(), Phrase.NOTE_START);
					if (newIndex+1 < pa.phrase.getGridRowSize()) {
						pa.phrase.setCell(newIndex+1, pitchMousePressed, defaultDynamic(), Phrase.NOTE_SUSTAIN);
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
			if (0 <= index && index < pa.phrase.getGridRowSize() && pitch == pa.phrase.getGridPitch(index)) {
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
		return (gridFrame.intersects(pa.mouseX, pa.mouseY) && gridFrame.getX1() + cellWidth < pa.mouseX);
	}
	
	/**
	 * Looks at the variable pa.mouseX and its position in relation to the grid, mapping that to the index of a note in the phrase.
	 * @return The index of the note to which pa.mouseX cooresponds
	 */
	private int mouseToIndex() {
		return (int)pa.map(pa.mouseX, 
			               gridFrame.getX1() + cellWidth, gridFrame.getX2(),
			               0, rowSize);
	}
	
	/**
	 * Looks at the variable pa.mouseY and its position in relation to the grid, mapping that to a pitch in the phrase.
	 * @return The pitch of the note to which pa.mouseY cooresponds
	 */
	private int mouseToPitch() {
		int pitchIndex = (int)pa.map(pa.mouseY, gridFrame.getY2(), gridFrame.getY1(), 0, numKeys);
		return pa.scale.getNoteValue(pitchIndex) + minOctave*12;
	}

	private int yToPitch(float y) {
		int pitchIndex = (int)pa.map(y, gridFrame.getY2(), gridFrame.getY1(), 0, numKeys) - 1;
		return pa.scale.getNoteValue(pitchIndex) + minOctave*12;
	}
	
	private float pitchToY(int pitch) {
		int pitchIndex = pa.scale.getIndexOfNoteValue(pitch - minOctave*12) + 1;
		return pa.map(pitchIndex, 0, numKeys, gridFrame.getY2(), gridFrame.getY1());
	}
	
	@Override
	public void draw() {
		if (playStop.getValue() != 0) {
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
		pa.rect(0, 0, pa.width, 50);
		
		//bottom toolbar background
		pa.rectMode(pa.CORNERS);
		pa.rect(0, gridFrame.getY2() + 1, pa.width, pa.height);
		
		//fill in margins
		pa.rectMode(pa.CORNER);
		pa.rect(0, gridFrame.getY2(), 10, 1);
		pa.rect(gridFrame.getX2(), gridFrame.getY2(), 10, 1);
		
		//controllers
		cp5.draw();
		updateMenus();
	}
	
	private void drawBody() {
		pa.noStroke();
		pa.fill(255);
		pa.rectMode(pa.CORNERS);
		pa.rect(0, gridFrame.getY1(), pa.width, gridFrame.getY2());
		
		//draw ghost image of grid
		float ghostCellWidth = cellWidth * pa.getBPM2() / pa.getBPM1();
		drawGrid(pa.lerpColor(PhasesPApplet.getColor2(), pa.color(255), 0.8f), ghostCellWidth);
		drawPhrase(pa.lerpColor(PhasesPApplet.getColor1(), pa.color(255), 0.8f),
				pa.lerpColor(PhasesPApplet.getColor2(), pa.color(255), 0.8f), pa.color(255), ghostCellWidth);
		
		//draw main image of grid
		drawPiano();
		drawGrid(PhasesPApplet.getColor2(), cellWidth);
		drawPhrase(PhasesPApplet.getColor1(), PhasesPApplet.getColor2(), 0, cellWidth);
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
		for (int i=0; i<pa.phrase.getNumNotes(); i++) {
			float numCellsWide = pa.phrase.getSCDuration(i) / pa.phrase.getUnitDuration();
			if (pa.phrase.getSCDynamic(i) != 0) {
				if (i == activeNoteIndex) {
					pa.fill(activeColor);
				}
				else {
					pa.fill(inactiveColor);
				}
				
				int pitch = pa.phrase.getSCPitch(i);
				float y = pitchToY(pitch);
				pa.rect(x, y, cellWidth*numCellsWide, cellHeight);
			}
			
			x += (cellWidth*numCellsWide);
		}
		pa.strokeWeight(1);
	}
	
	/**
	 * Draws the empty grid.
	 * @param color
	 */
	private void drawGrid(int color, float cellWidth) {
		//line color
		pa.stroke(color);
		
		//horizontal lines
		float y = gridFrame.getY2();
		pa.line(gridFrame.getX1() + cellWidth, y, gridFrame.getX2(), y);
		y -= cellHeight;
		for (int i=0; i<numKeys; i++) {
			pa.line(gridFrame.getX1() + cellWidth, y, gridFrame.getX2(), y);		
			y -= cellHeight;
		}
		
		//vertical lines
		float x = gridFrame.getX1() + cellWidth;
		while (x < gridFrame.getX2()) {
			pa.line(x, gridFrame.getY1(), x, gridFrame.getY2());
			x += cellWidth;
		}
		pa.line(x, gridFrame.getY1(), x, gridFrame.getY2());
	}
	
	private void updateGrid(Scale newScale) {
		float[] ys = new float[pa.phrase.getGridRowSize()];
		for (int i=0; i<pa.phrase.getGridRowSize(); i++) {
			int pitch = (int)pa.phrase.getGridPitch(i);
			ys[i] = pitchToY(pitch);
		}

		pa.scale = newScale;
		
		for (int i=0; i<pa.phrase.getGridRowSize(); i++) {
			int pitch = yToPitch(ys[i]);
			pa.phrase.setGridPitch(i, pitch);
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
		for (int i=0; i<numKeys; i++) {
			int iModScaleSize = i % pa.scale.size();
			int noteValueMod12 = pa.scale.getNoteValue(iModScaleSize) % 12;
			int keyColor = keyColors[noteValueMod12];
			pa.fill(keyColor);
			pa.rect(gridFrame.getX1(), y, cellWidth, cellHeight);
			
			if (labelPianoKeys) {
				String noteName = pa.scale.getNoteNameByIndex(iModScaleSize);
				int inverseKeyColor = (keyColor == W) ? B : W;
				pa.fill(inverseKeyColor);
				pa.text(noteName, gridFrame.getX1(), y, cellWidth, cellHeight);
			}
			
			y -= cellHeight;
		}
	}
	
	/**
	 * 
	 * @return The default dynamic for notes created in the Editor
	 */
	private float defaultDynamic() {
		return 50 + pa.random(-5, 5);
	}
}