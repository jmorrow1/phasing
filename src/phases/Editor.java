package phases;

import geom.Rect;

/**
 * Provides an editor in which the user can create and edit musical phrases for the Presenter screen.
 * @author James Morrow
 *
 */
public class Editor extends Screen {
	//piano
	private int minPitch = 60;
	private int numKeys = 24;
	private int maxPitch = minPitch + numKeys;
	private final static int W=0xfffffff, B=Presenter.color2;
	private final static int[] keyColors = new int[] {W, B, W, B, W, W, B, W, B, W, B, W};
	//grid
	private Rect gridFrame;
	private int rowSize = 12;
	private int columnSize = numKeys;
	private float cellWidth, cellHeight;
	//interaction
	private boolean userIsDrawingNote = false;
	private int startIndexOfUserDrawnNote = -1;
	private int indexMousePressed=-1, pitchMousePressed=-1;
	
	/**
	 * 
	 * @param pa The PApplet to draw to
	 */
	public Editor(PhasesPApplet pa) {
		super(pa);
		
		gridFrame = new Rect(25, 50, pa.width - 25, pa.height - 50, pa.CORNERS);
		cellWidth = gridFrame.getWidth() / (rowSize+1);
		cellHeight = gridFrame.getHeight() / columnSize;
	}

	@Override
	public void onEnter() {
		redraw();
	}
	
	@Override
	public void onExit() {}
	
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
				redraw();
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
					redraw();
				}
				else if (newIndex < indexMousePressed && newIndex < startIndexOfUserDrawnNote) {
					pa.phrase.setCell(newIndex, pitchMousePressed, defaultDynamic(), Phrase.NOTE_START);
					if (newIndex+1 < pa.phrase.getGridRowSize()) {
						pa.phrase.setCell(newIndex+1, pitchMousePressed, defaultDynamic(), Phrase.NOTE_SUSTAIN);
						indexMousePressed++;
					}
					startIndexOfUserDrawnNote = newIndex;
					redraw();
				}
			}
			else {
				mousePressed();
			}
		}
		/*else {
			userIsDrawingNote = false;
		}*/
		redraw();
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
	public void draw() {}
	
	
	/**
	 * Redraws every visible thing onto the screen.
	 */
	private void redraw() {
		pa.background(255);
		
		drawGrid();
		drawPhrase();
	}
	
	/**
	 * Interprets the Phrase data and draws it to the grid.
	 */
	private void drawPhrase() {
		pa.strokeWeight(1.5f);
		pa.stroke(0);
		pa.fill(Presenter.color1);
		pa.rectMode(pa.CORNER);
		float x = gridFrame.getX1() + cellWidth;
		for (int i=0; i<pa.phrase.getNumNotes(); i++) {
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
		float y = gridFrame.getY2();
		pa.rectMode(pa.CORNER);
		pa.stroke(Presenter.color2);
		pa.line(gridFrame.getX1() + cellWidth, y, gridFrame.getX2(), y);
		y -= cellHeight;
		for (int i=0; i<numKeys; i++) {
			
			//y-axis (piano)
			pa.fill(keyColors[i % 12]);
			pa.rect(gridFrame.getX1(), y, cellWidth, cellHeight);
			
			//horizontal lines
			pa.fill(keyColors[i % 12], 50);
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
	 * 
	 * @return The default dynamic for notes created in the Editor
	 */
	private float defaultDynamic() {
		return 50 + pa.random(-5, 5);
	}
}