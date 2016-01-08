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
import geom.Rect;
import processing.core.PApplet;
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
	//phasing screen graphics
	private int[] pixelsBuffer;
	//piano
	private boolean labelPianoKeys = true;
	private int minPitch = 60;
	private int numKeys = 24;
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
	private final static int BPM_1 = 1, BPM_DIFFERENCE = 2;
	private int maxPhaseDifferenceAmplitude = 10;
	private ControlP5 cp5;
	private Toggle playStop;
	private Slider bpmSlider, bpmDifferenceSlider;
	private DropdownList rootMenu, scaleMenu;
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
		gridFrame = new Rect(10, 50, pa.width-10, pa.height-25, pa.CORNERS);
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
									pg.fill(PhasesPApplet.getColor1());
								}
								else {
									pg.stroke(0, 150);
									pg.fill(PhasesPApplet.getColor1(), 175);
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
	
		//hide cp5
		cp5.hide();
		
		//init pixels buffer
		pixelsBuffer = new int[pa.width * pa.height];
		clearBuffer();
	}
	
	private void clearBuffer() {
	    for (int i=0; i<pixelsBuffer.length; i++) {
	        pixelsBuffer[i] = 0xffffffff;
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
		if (c instanceof DropdownList || c instanceof Button) {
			c.setColorBackground(pa.color(PhasesPApplet.getColor1()));
			c.setColorActive(pa.getColor2());
			c.setColorForeground(0);
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
				drawBody();
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
		int pitchIndex = (int)pa.map(pa.mouseY, gridFrame.getY2(), gridFrame.getY1(), 0, numKeys) + 1;
		return pa.scale.getNoteValue(pitchIndex) + minPitch;
	}

	private int yToPitch(int y) {
		int pitchIndex = (int)pa.map(y, gridFrame.getY2(), gridFrame.getY1(), 0, numKeys);
		return pa.scale.getNoteValue(pitchIndex) + minPitch;
	}
	
	private float pitchToY(int pitch) {
		int pitchIndex = pa.scale.getIndexOfNoteValue(pitch - minPitch);
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
	
	private void drawToolbarBackground() {
		pa.noStroke();
		pa.fill(255);
		pa.rectMode(pa.CORNER);
		pa.rect(0, 0, pa.width, 50);
	}
	
	private void drawToolbar() {
		drawToolbarBackground();
		cp5.draw();
		updateMenus();
	}
	
	private void drawBody() {
		pa.noStroke();
		pa.fill(255);
		pa.rectMode(pa.CORNERS);
		pa.rect(0, 50, pa.width, pa.height);
		
		drawPiano();
		drawGrid();
		drawPhrase();
		
		phase(0, 50, (int)(bpmDifferenceSlider.getValue() * cellWidth / 10),
				pa.lerpColor(pa.getColor2(), pa.color(255), 0.8f), false);
	}
	
	private void phase(int startX, int startY, int numPixels, int color, boolean wrap) {
		pa.loadPixels();
	    
	    int i=startX + startY*pa.width; //loops through pixels
	    int x=startX;
	    int y=startY;
	    while (y < pa.height) {
	        while (x < pa.width) {
	            if (pa.pixels[i] != 0xffffffff) {
	            	if (wrap) {
	            		pixelsBuffer[ (x + numPixels) % pa.width + pa.width*y ] = color;
	            	}
	            	else if (0 <= x + numPixels && x + numPixels < pa.width) {
	            		pixelsBuffer[i + numPixels] = color;
	            	}
	            }
	            i++;
	            x++;
	        }
	        x = 0;
	        y++;
	    }
	    
	    i=0;
	    while (i < pa.pixels.length) {
	        if (pa.pixels[i] == 0xffffffff) {
	            pa.pixels[i] = pixelsBuffer[i];
	        }
	        pixelsBuffer[i] = 0xffffffff;
	        i++;
	    }
	    
	    pa.updatePixels();
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
			float y = pitchToY(pitch);
			float numCellsWide = pa.phrase.getSCDuration(i) / pa.phrase.getUnitDuration();
			pa.rect(x, y, cellWidth*numCellsWide, cellHeight);
			x += (cellWidth*numCellsWide);
		}
		pa.strokeWeight(1);
	}
	
	private void updateGrid(Scale newScale) {
		int[] ys = new int[pa.phrase.getGridRowSize()];
		for (int i=0; i<pa.phrase.getGridRowSize(); i++) {
			int pitch = (int)pa.phrase.getGridPitch(i);
			ys[i] = (int)pitchToY(pitch);
		}
		
		pa.scale = newScale;
		
		for (int i=0; i<pa.phrase.getGridRowSize(); i++) {
			int pitch = yToPitch(ys[i]);
			pa.phrase.setGridPitch(i, pitch);
		}
	}
	
	/**
	 * Draws the empty grid.
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