package phases;

import geom.Rect;

public class PhraseRepository extends Screen {
	private Rect[][] cells;
			
	public PhraseRepository(PhasesPApplet pa) {
		super(pa);
		
		switch (pa.screenSizeMode) {
			case PhasesPApplet._800x600 :
				constructCells(new Rect(50, 75, 725, 550, pa.CORNERS), 4, 3);
				break;
		}
	}
	
	private void constructCells(Rect box, int rowSize, int colSize) {
		float cellSize = pa.min(box.getWidth() / rowSize, box.getHeight() / colSize);
		
		cells = new Rect[rowSize][colSize];	
		
		float y1 = box.getCeny() - cellSize*0.5f*colSize;
		for (int j=0; j<colSize; j++) {
			float x1 = box.getCenx() - cellSize*0.5f*rowSize;
			for (int i=0; i<rowSize; i++) {
				cells[i][j] = new Rect(x1, y1, cellSize, cellSize, pa.CORNER);
				x1 += cellSize;
			}
			y1 += cellSize;
		}
	}
	
	/*************************************
	 ***** Enter/Exit Event Handling *****
	 *************************************/

	@Override
	public void onEnter() {
	}

	@Override
	public void onExit() {
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/

	@Override
	public void draw() {
		pa.background(255);
		
		//test:
		pa.noFill();
		pa.stroke(0);
		for (Rect[] rs : cells) {
			for (Rect r : rs) {
				r.display(pa);
			}
		}
	}
}