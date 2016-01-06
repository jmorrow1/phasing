package phases;

import java.lang.reflect.Method;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.ControllerView;
import controlP5.DropdownList;
import controlP5.Slider;
import controlP5.Toggle;
import geom.Rect;
import processing.core.PFont;
import processing.core.PGraphics;

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
	private int minPitch = 60;
	private int numKeys = 24;
	private int maxPitch = minPitch + numKeys;
	private final static int W=0xffffffff, B=PhasesPApplet.getColor2();
	private final static int[] keyColors = new int[] {W, B, W, B, W, W, B, W, B, W, B, W};
	//grid
	private Rect gridFrame;
	private int rowSize = 12;
	private int columnSize = numKeys;
	private float cellWidth, cellHeight;
	//interaction w/ grid
	private boolean userIsDrawingNote = false;
	private int startIndexOfUserDrawnNote = -1;
	private int indexMousePressed=-1, pitchMousePressed=-1;
	//controllers
	private final static int BPM_1 = 1, PHASE_DIFFERENCE = 2;
	private ControlP5 cp5;
	private Toggle playStop;
	private Slider phaseDifferenceSlider;
	private DropdownList rootMenu, scaleMenu;
	private String rootLabel, scaleLabel;
	private PFont pfont;
	
	/**
	 * 
	 * @param pa The PApplet to draw to
	 */
	public Editor(PhasesPApplet pa) {
		super(pa);
		
		pfont = pa.loadFont("DejaVuSans-18.vlw");
		
		pa.textFont(pfont);
		
		try {
			Method callback = Editor.class.getMethod("animate", SoundCipherPlus.class);
			livePlayer = new SoundCipherPlus(pa, pa.phrase, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		gridFrame = new Rect(25, 50, 775, 575, pa.CORNERS);
		cellWidth = gridFrame.getWidth() / (rowSize+1);
		cellHeight = gridFrame.getHeight() / columnSize;
		
		cp5 = new ControlP5(pa);
		playStop = cp5.addToggle("play")
				      .setPosition(35, 10)
					  .setSize(35, 35)
					  .plugTo(this)
					  .setView(new ControllerView<Toggle>() {
			
						    @Override
							public void display(PGraphics pg, Toggle t) {
								if (t.isMouseOver()) {
									pg.stroke(0);
									pg.fill(PhasesPApplet.getColor1());
								}
								else {
									pg.stroke(0, 150);
									pg.fill(PhasesPApplet.getColor1(), 150);
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
		
		addBPMSlider(BPM_1);
		phaseDifferenceSlider = addBPMSlider(PHASE_DIFFERENCE);
		
		rootMenu = new DropdownListPlus(cp5, "Root");
		rootMenu.setPosition(150, 5)
			    .setSize(130, 300)
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
		scaleMenu.setPosition(300, 5)
				 .setSize(130, 145)
				 .addItems(PhasesPApplet.scaleTypes)
				 .setItemHeight(22)
				 .setBarHeight(22)
				 .close()
				 ;
		scaleMenu.setLabel("Chromatic");
		colorController(scaleMenu);
		formatLabel(scaleMenu);
		scaleLabel = scaleMenu.getLabel();
	
		cp5.hide();
	}
	
	private void formatLabel(DropdownList x) {
		x.getCaptionLabel().toUpperCase(false);
		x.getValueLabel().toUpperCase(false);
		x.getCaptionLabel().setFont(pfont);
		x.getValueLabel().setFont(pfont);
		x.getCaptionLabel().getStyle().paddingTop += 5;
		x.getValueLabel().getStyle().paddingTop += 5;
	}
	
	private void colorController(Controller c) {
		c.setColorCaptionLabel(pa.color(255));
	    c.setColorValueLabel(pa.color(255));
		if (c instanceof DropdownList) {
			c.setColorBackground(pa.color(PhasesPApplet.getColor1()));
			c.setColorActive(pa.getColor2());
			c.setColorForeground(pa.getColor2());
		}
		else if (c instanceof Slider) {		
		    c.setColorBackground(pa.color(pa.getColor1(), 150));
		    c.setColorActive(pa.getColor1());
		    c.setColorForeground(pa.getColor1());
		}
	}
	
	private Slider addBPMSlider(int id) {
		switch(id) {
			case BPM_1 :
				return addBPMSlider("Beats Per Minute", id, 460, 5, pa.getBPM1(), 1, 100, 1);
			case PHASE_DIFFERENCE :
				return addBPMSlider("Phase difference", id, 620, 5, pa.getBPM2() - pa.getBPM1(), -10, 10, 4);
			default :
				return null;
		}
	}
	
	private Slider addBPMSlider(String name, int id, int x, int y, 
		float bpm, int minValue, int maxValue, int ticksPerWholeNumber) {
		Slider s = cp5.addSlider(name)
			          .setId(id)
			          .setDecimalPrecision(0)
			          .setRange(minValue, maxValue)
			          .setPosition(x, y)
			          .setSize(150, 30)
			          .setValue(bpm)
			          .setLabelVisible(false)
			          .setNumberOfTickMarks((maxValue-minValue) * ticksPerWholeNumber + 1)
			          .plugTo(this)
			          ;
		colorController(s);
		return s;
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
	 * Callback for ControlP5
	 * @param e
	 */
	public void controlEvent(ControlEvent e) {
		switch (e.getId()) {
			case BPM_1 :
				pa.setBPM1(e.getValue());
				livePlayer.tempo(pa.getBPM1());
				pa.setBPM2(e.getValue() + phaseDifferenceSlider.getValue());
				break;
			case PHASE_DIFFERENCE :
				pa.setBPM2(pa.getBPM1() + e.getValue());
				break;
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
		//redraw();
	}
	
	@Override
	public void onExit() {
		cp5.hide();
	}
	
	public void mousePressed() {
		//for drawing a note to the grid:
		if (mouseIntersectsGrid()) {
			indexMousePressed = mouseToIndex();
			pitchMousePressed = mouseToPitch();
			startIndexOfUserDrawnNote = indexMousePressed;
			boolean success = pa.phrase.setCell(indexMousePressed, pitchMousePressed, defaultDynamic(), Phrase.NOTE_START);
			if (indexMousePressed+1 < pa.phrase.getGridRowSize() && pa.phrase.getNoteType(indexMousePressed+1) == Phrase.NOTE_SUSTAIN) {
				pa.phrase.setNoteType(indexMousePressed+1, Phrase.NOTE_START);
			}
			if (success) {
				userIsDrawingNote = true;
				//redraw();
			}
		}
	}
	
	public void mouseReleased() {
		//for resetting the Editor's state w/r/t the grid:
		userIsDrawingNote = false;
		startIndexOfUserDrawnNote = -1;
	}
	
	public void mouseDragged() {
		//for continuing to draw notes to the grid:
		if (userIsDrawingNote && mouseIntersectsGrid()) {
			int newIndex = mouseToIndex();
			int newPitch = mouseToPitch();
			if (newPitch == pitchMousePressed) {
				if (newIndex > indexMousePressed) {
					if (newIndex+1 < pa.phrase.getGridRowSize() &&
							(pa.phrase.getNoteType(newIndex) == Phrase.NOTE_SUSTAIN ||
							 pa.phrase.getNoteType(newIndex) == Phrase.NOTE_START)) {
						pa.phrase.setNoteType(newIndex+1, Phrase.NOTE_START);
						indexMousePressed++;
					}
					pa.phrase.setCell(newIndex, pitchMousePressed, defaultDynamic(), Phrase.NOTE_SUSTAIN);
					//redraw();
				}
				else if (newIndex < indexMousePressed && newIndex < startIndexOfUserDrawnNote) {
					pa.phrase.setCell(newIndex, pitchMousePressed, defaultDynamic(), Phrase.NOTE_START);
					if (newIndex+1 < pa.phrase.getGridRowSize()) {
						pa.phrase.setCell(newIndex+1, pitchMousePressed, defaultDynamic(), Phrase.NOTE_SUSTAIN);
						indexMousePressed++;
					}
					startIndexOfUserDrawnNote = newIndex;
					//redraw();
				}
			}
			else {
				mousePressed();
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
		return (int)pa.map(pa.mouseY,
			               gridFrame.getY2(), gridFrame.getY1(),
			               minPitch, maxPitch);
	}
	
	@Override
	public void draw() {
		if (playStop.getValue() != 0) {
			long dt = System.currentTimeMillis() - prev_t;
			prev_t = System.currentTimeMillis();
			livePlayer.update(dt * pa.getBPMS1());
		}
		redraw();
	}
	
	
	/**
	 * Redraws every visible thing onto the screen.
	 */
	private void redraw() {
		pa.background(255);
		
		drawPiano();
		drawGrid();
		drawPhrase();
		
		if (rootLabel != rootMenu.getLabel() || scaleLabel != scaleMenu.getLabel()) {
			rootLabel = rootMenu.getLabel();
			scaleLabel = scaleMenu.getLabel();
			pa.scale = pa.getScale(rootLabel, scaleLabel);
		}
	}
	
	/**
	 * Interprets the Phrase data and draws it to the grid.
	 */
	private void drawPhrase() {
		pa.strokeWeight(1.5f);
		pa.stroke(0);
		pa.rectMode(pa.CORNER);
		float x = gridFrame.getX1() + cellWidth;
		for (int i=0; i<pa.phrase.getNumNotes(); i++) {
			
			if (i == activeNoteIndex) {
				pa.fill(PhasesPApplet.getColor2());
			}
			else {
				pa.fill(PhasesPApplet.getColor1());
			}
			
			int pitch = pa.phrase.getSCPitch(i);
			float y = pa.map(pitch+1, minPitch, maxPitch, gridFrame.getY2(), gridFrame.getY1());
			float numCellsWide = pa.phrase.getSCDuration(i) / pa.phrase.getUnitDuration();
			pa.rect(x, y, cellWidth*numCellsWide, cellHeight);
			x += (cellWidth*numCellsWide);
		}
		pa.strokeWeight(1);
	}
	
	/**
	 * Draws the grid.
	 */
	private void drawGrid() {
		//line color
		pa.stroke(PhasesPApplet.getColor2());
		
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
	
	/**
	 * Draws the piano, which serves as the y-axis of the grid.
	 */
	private void drawPiano() {
		pa.rectMode(pa.CORNER);
		pa.stroke(PhasesPApplet.getColor2());
		float y = gridFrame.getY2() - cellHeight;
		
		pa.textAlign(pa.CENTER, pa.CENTER);
		pa.textSize(16);
		for (int i=0; i<numKeys; i++) {
			int iModScaleSize = i % pa.scale.size();
			int noteValueMod12 = pa.scale.getNoteValue(iModScaleSize) % 12;
			int keyColor = keyColors[noteValueMod12];
			pa.fill(keyColor);		
			pa.rect(gridFrame.getX1(), y, cellWidth, cellHeight);
			
			if (labelPianoKeys) {
				String noteName = pa.scale.getNoteName(iModScaleSize);
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