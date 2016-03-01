package screens;

import java.util.ArrayList;

import controlP5.Button;
import controlP5.ControlP5;
import controlp5.TriangleButtonView;
import geom.Rect;
import phasing.PhasesPApplet;
import phasing.Phrase;
import phasing.PhrasePicture;
import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class PhraseRepository extends Screen implements CellEventHandler {
	//cells
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	
	//controlp5
	private ControlP5 cp5;
	private Button pageLeftButton, pageRightButton;
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	/**
	 * 
	 * @param pa The PApplet to draw to.
	 */
	public PhraseRepository(PhasesPApplet pa) {
		super(pa);
		
		cp5 = new ControlP5(pa);
		initCells();
		initDirectionalButtons();
		cp5.hide();
	}
	
	/**
	 * Initializes the buttons that turn the page.
	 */
	private void initDirectionalButtons() {
		int size = 40;
		pageLeftButton = consDirectionalButton("pageLeft", 10, pa.height - size - 5, size, pa.PI);
		pageRightButton = consDirectionalButton("pageRight", pa.width - size - 10, pa.height - size - 5, size, 0);
		pageLeftButton.hide();
		pageRightButton.hide();
	}
	
	/**
	 * Intended for constructing a page-left or page-right button.
	 * 
	 * @param name The button's name.
	 * @param x1 The leftmost x-coordinate of the button.
	 * @param y1 The topmost y-coordinate of the button.
	 * @param size The width and height of the button.
	 * @param angle The heading (where it's pointing) of the button, in terms of radians.
	 * @return The newly constructed button.
	 */
	private Button consDirectionalButton(String name, int x1, int y1, int size, float angle) {
		Button b = cp5.addButton(name)
			          .setPosition(x1, y1)
			          .setSize(size, size)
			          .setView(new TriangleButtonView(angle, 0.7f*pa.PI))
			          .plugTo(this)
	                  ;  
		pa.colorControllerHideLabel(b);
		return b;
	}
	
	/**
	 * Initialzes the list of cells.
	 */
	private void initCells() {
		switch (pa.screenSizeMode) {
			case PhasesPApplet._800x600 :
				initCells(new Rect(50, 75, 725, 550, pa.CORNERS), 4, 3);
				break;
		}
	}
	
	/**
	 * Initializes the list of cells.
	 * 
	 * @param box The size of a cell.
	 * @param rowSize The number of cells in a row.
	 * @param colSize The number of cells in a column.
	 */
	private void initCells(Rect box, int rowSize, int colSize) {
		float cellSize = pa.min(box.getWidth() / rowSize, box.getHeight() / colSize);
		
		float y1 = box.getCeny() - cellSize*0.5f*colSize;
		for (int j=0; j<colSize; j++) {
			float x1 = box.getCenx() - cellSize*0.5f*rowSize;
			for (int i=0; i<rowSize; i++) {
				cells.add(new Cell(new Rect(x1, y1, cellSize, cellSize, pa.CORNER), cp5, this));
				x1 += cellSize;
			}
			y1 += cellSize;
		}
	}
	
	/*******************************
	 ***** Interface with Cell *****
	 *******************************/
	
	public void copy(Cell cell) {
		int i = cells.indexOf(cell);
		if (i < pa.phrasePictures.size()) {
			PhrasePicture p = pa.phrasePictures.get(i);
			pa.phrasePictures.add(i+1, new PhrasePicture(p));
		}
	}

	public void load(Cell cell) {
		int i = cells.indexOf(cell);
		if (i < pa.phrasePictures.size()) {
			pa.currentPhrasePicture = pa.phrasePictures.get(i);
			pa.currentPhrase = pa.currentPhrasePicture.getPhrase();
		}
	}
	
	public void newPhrase() {
		pa.currentPhrase = new Phrase();
		pa.currentPhrasePicture = new PhrasePicture(pa.currentPhrase, pa);
		pa.phrasePictures.add(pa.currentPhrasePicture);
	}
	
	public void generatePhrase() {
		pa.currentPhrase = pa.generateReichLikePhrase();
		pa.currentPhrasePicture = new PhrasePicture(pa.currentPhrase, pa);
		pa.phrasePictures.add(pa.currentPhrasePicture);
	}
	
	/*********************************
	 ***** Screen Event Handling *****
	 *********************************/
	
	@Override
	public void resized() {}

	@Override
	public void onEnter() {
		cp5.show();
	}

	@Override
	public void onExit() {
		cp5.hide();
		pa.savePlayerInfo();
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/

	@Override
	public void draw() {
		pa.background(255);
		
		for (int i=0; i<cells.size(); i++) {
			Cell c = cells.get(i);
			if (i < pa.phrasePictures.size()) {
				c.draw(pa.phrasePictures.get(i), pa);
			}
			else if (i == pa.phrasePictures.size()) {
				c.draw(true, pa);
			}
			else {
				c.draw(false, pa);
			}
		}
	}
}