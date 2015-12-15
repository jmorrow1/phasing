package phases;

import geom.Rect;

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
		if (mouseIntersectsGrid()) {
			indexMousePressed = mouseToIndex();
			pitchMousePressed = mouseToPitch();
			startIndexOfUserDrawnNote = indexMousePressed;
			boolean success = pa.phrase.setNote(indexMousePressed, pitchMousePressed, defaultDynamic(), Phrase.NOTE_START);
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
		userIsDrawingNote = false;
		startIndexOfUserDrawnNote = -1;
	}
	
	public void mouseDragged() {
		if (userIsDrawingNote && mouseIntersectsGrid()) {
			int newIndex = mouseToIndex();
			int newPitch = mouseToPitch();
			if (newPitch == pitchMousePressed) {
				//System.out.println("newIndex = " + newIndex + ", indexMousePressed = " + indexMousePressed + ", startIndexOfUserDrawnNote = " + startIndexOfUserDrawnNote);
				if (newIndex > indexMousePressed) {
					if (newIndex+1 < pa.phrase.getGridRowSize() &&
							(pa.phrase.getNoteType(newIndex) == Phrase.NOTE_SUSTAIN ||
							pa.phrase.getNoteType(newIndex) == Phrase.NOTE_START) ) {
						pa.phrase.setNoteType(newIndex+1, Phrase.NOTE_START);
						indexMousePressed++;
					}
					pa.phrase.setNote(newIndex, pitchMousePressed, defaultDynamic(), Phrase.NOTE_SUSTAIN);
					redraw();
				}
				else if (newIndex < indexMousePressed && newIndex < startIndexOfUserDrawnNote) {
					pa.phrase.setNote(newIndex, pitchMousePressed, defaultDynamic(), Phrase.NOTE_START);
					if (newIndex+1 < pa.phrase.getGridRowSize()) {
						pa.phrase.setNote(newIndex+1, pitchMousePressed, defaultDynamic(), Phrase.NOTE_SUSTAIN);
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
		else {
			userIsDrawingNote = false;
		}
		redraw();
	}
	
	private boolean mouseIntersectsGrid() {
		return (gridFrame.intersects(pa.mouseX, pa.mouseY) && gridFrame.getX1() + cellWidth < pa.mouseX);
	}
	
	private int mouseToIndex() {
		return (int)pa.map(pa.mouseX, 
			               gridFrame.getX1() + cellWidth, gridFrame.getX2(),
			               0, rowSize);
	}
	
	private int mouseToPitch() {
		return (int)pa.map(pa.mouseY,
			               gridFrame.getY2(), gridFrame.getY1(),
			               minPitch, maxPitch);
	}
	
	@Override
	public void draw() {}
	
	private void redraw() {
		pa.background(255);
		
		drawGrid();
		drawPhrase();
	}
	
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
	
	private float defaultDynamic() {
		return 50 + pa.random(-5, 5);
	}
}