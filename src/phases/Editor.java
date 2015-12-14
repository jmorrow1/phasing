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
	private boolean pressStartedWithMouseTouchingGrid;
	
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
			pressStartedWithMouseTouchingGrid = true;
			drawPitch();
		}
	}
	
	public void mouseReleased() {
		pressStartedWithMouseTouchingGrid = false;
	}
	
	public void mouseDragged() {
		if (pressStartedWithMouseTouchingGrid && mouseIntersectsGrid()) {
			drawPitch();
		}
	}
	
	private boolean mouseIntersectsGrid() {
		return (gridFrame.intersects(pa.mouseX, pa.mouseY) && gridFrame.getX1() + cellWidth < pa.mouseX);
	}
	
	private void drawPitch() {
		int index = (int)pa.map(pa.mouseX, 
				                gridFrame.getX1() + cellWidth, gridFrame.getX2(),
				                0, rowSize);
		int pitch = (int)pa.map(pa.mouseY,
				                gridFrame.getY2(), gridFrame.getY1(),
				                minPitch, maxPitch);
		pa.phrase.setPitch(index, pitch);
		redraw();	
	}
	
	@Override
	public void draw() {}
	
	private void redraw() {
		pa.background(255);
		
		drawGrid();
		drawPhrase();
	}
	
	private void drawPhrase() {
		pa.noStroke();
		pa.fill(Presenter.color1);
		pa.rectMode(pa.CORNER);
		float x = gridFrame.getX1() + cellWidth;
		for (int i=0; i<pa.phrase.getNumNotes(); i++) {
			int pitch = pa.phrase.getPitch(i);
			float y = pa.map(pitch+1, minPitch, maxPitch, gridFrame.getY2(), gridFrame.getY1());
			pa.rect(x, y, cellWidth, cellHeight);
			x += cellWidth;
		}
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
}