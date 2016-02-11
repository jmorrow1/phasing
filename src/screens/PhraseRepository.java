package screens;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;

public class PhraseRepository extends Screen {
	private ArrayList<Cell> cells = new ArrayList<Cell>();
			
	public PhraseRepository(PhasesPApplet pa) {
		super(pa);
		
		switch (pa.screenSizeMode) {
			case PhasesPApplet._800x600 :
				constructCells(new Rect(50, 75, 725, 550, pa.CORNERS), 4, 3);
				break;
		}
		
		populateCellsWithRandomPhrases();
	}
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	private void constructCells(Rect box, int rowSize, int colSize) {
		float cellSize = pa.min(box.getWidth() / rowSize, box.getHeight() / colSize);
		
		float y1 = box.getCeny() - cellSize*0.5f*colSize;
		for (int j=0; j<colSize; j++) {
			float x1 = box.getCenx() - cellSize*0.5f*rowSize;
			for (int i=0; i<rowSize; i++) {
				cells.add(new Cell(new Rect(x1, y1, cellSize, cellSize, pa.CORNER)));
				x1 += cellSize;
			}
			y1 += cellSize;
		}
	}
	
	private void populateCellsWithRandomPhrases() {
		for (Cell c : cells) {
			c.phrase = pa.generateReichLikePhrase();
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
		
		for (Cell c : cells) {
			c.draw();
		}
	}
	
	private class Cell {
		Rect rect;
		Phrase phrase;
		float blendAmt;
		
		Cell(Rect rect) {
			this.rect = rect;
			blendAmt = pa.random(1);
		}
		
		void draw() {
			pa.noFill();
			pa.strokeWeight(1);
			pa.stroke(150);
			rect.display(pa);
			
			if (phrase != null) {
				drawPhrase(blendAmt);
			}
		}
	
		private void drawPhrase(float blendAmt) {
			//draw notes
			pa.ellipseMode(pa.RADIUS);
			pa.noStroke();
			pa.fill(pa.getBlendedColor(blendAmt));
			float dotRadius = 0.025f * rect.getHeight();
			iterateNotes((x, y) -> pa.ellipse(x, y, dotRadius, dotRadius));
			
			//draw lines between notes	
			pa.strokeWeight(1);
			pa.stroke(pa.getBlendedColor(blendAmt));
			pa.noFill();
			pa.beginShape();
			iterateNotes((x, y) -> pa.vertex(x, y));
			pa.endShape();
		}
		
		private void iterateNotes(NoteDraw nd) {
			float x1 = pa.lerp(rect.getX1(), rect.getX2(), 0.1f);
			float x2 = pa.lerp(rect.getX2(), rect.getX1(), 0.1f);
			float x = x1;
			float dx = (x2 - x1) / phrase.getNumNotes();
					
			float y2 = pa.lerp(rect.getY2(), rect.getY1(), 0.1f);
			float y1 = pa.lerp(rect.getY1(), rect.getY2(), 0.1f);
			
			for (int i=0; i<phrase.getNumNotes(); i++) {
				int pitch = phrase.getSCPitch(i);
				float y = pa.map(pitch, phrase.minPitch(), phrase.maxPitch(), y2, y1);
				nd.draw(x, y);
				x += dx;
			}
		}
	}
	
	interface NoteDraw {
		void draw(float x, float y);
	}
}