package phases;

import java.lang.reflect.Method;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.DropdownList;
import controlP5.Slider;
import controlP5.Toggle;
import geom.Rect;

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
	private Scale currentScale;
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
	private final static int BPM_1 = 1, BPM_2 = 2;
	private ControlP5 cp5;
	private Toggle playStop;
	
	/**
	 * 
	 * @param pa The PApplet to draw to
	 */
	public Editor(PhasesPApplet pa) {
		super(pa);
		
		currentScale = pa.getScale("C", "Chromatic");

		try {
			Method callback = Editor.class.getMethod("animate", SoundCipherPlus.class);
			livePlayer = new SoundCipherPlus(pa, pa.phrase, this, callback);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		gridFrame = new Rect(150, 50, 775, 575, pa.CORNERS);
		cellWidth = gridFrame.getWidth() / (rowSize+1);
		cellHeight = gridFrame.getHeight() / columnSize;
		
		cp5 = new ControlP5(pa);
		/*playStop = cp5.addToggle("play")
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
					   ;*/
		
		//addBPMSlider(BPM_1);
		//addBPMSlider(BPM_2);
		
		DropdownListPlus rootMenu = new DropdownListPlus(cp5, "ROOT");
		rootMenu.setPosition(5, 20)
			    .setWidth(45)
			    .addItems(PhasesPApplet.roots)
			    //.close()
			    ;
		rootMenu.getCaptionLabel().toUpperCase(false);
		rootMenu.getValueLabel().toUpperCase(false);
		colorController(rootMenu);
		
		DropdownListPlus scaleMenu = new DropdownListPlus(cp5, "Scale");
		scaleMenu.setPosition(55, 20)
				 .setWidth(90)
				 .addItems(PhasesPApplet.scaleTypes)
				 //.close()
				 ;
		colorController(scaleMenu);
		
		cp5.hide();
	}
	
	private void colorController(Controller c) {
		c.setColorCaptionLabel(pa.color(255));
		c.setColorValueLabel(pa.color(255));
		c.setColorBackground(pa.color(PhasesPApplet.getColor1()));
		c.setColorActive(pa.getColor2());
		c.setColorForeground(pa.getColor2());
	}
	
	private Slider addBPMSlider(int id) {
		switch(id) {
			case BPM_1 :
				return addBPMSlider("bpm 1", id, 100, 5, pa.getBPM1());
			case BPM_2 :
				return addBPMSlider("bpm 2", id, 100, 25, pa.getBPM2());
			default :
				return null;
		}
	}
	
	private Slider addBPMSlider(String name, int id, int x, int y, float bpm) {
		Slider s = cp5.addSlider(name)
			          .setId(id)
					  .setDecimalPrecision(1)
					  .setRange(pa.MIN_BPM, pa.MAX_BPM)
					  .setNumberOfTickMarks(2*(int)(pa.MAX_BPM - pa.MIN_BPM) + 1)
					  .setPosition(x, y)
					  .setSize(600, 15)
					  .setValue(bpm)
					  .plugTo(this)
					  ;
		colorController(s);
		return s;
	}
	
	/**
	 * Callback for ControlP5
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
				break;
			case BPM_2 :
				pa.setBPM2(e.getValue());
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
		/*if (playStop.getValue() != 0) {
			long dt = System.currentTimeMillis() - prev_t;
			prev_t = System.currentTimeMillis();
			livePlayer.update(dt * pa.getBPMS1());
		}*/
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
		
		for (int i=0; i<numKeys; i++) {
			int iModScaleSize = i % currentScale.size();
			
			int keyColor = keyColors[currentScale.getNoteValue(iModScaleSize)];
			pa.fill(keyColor);		
			pa.rect(gridFrame.getX1(), y, cellWidth, cellHeight);
			
			if (labelPianoKeys) {
				String noteName = currentScale.getNoteName(iModScaleSize);
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