package screens;

import java.util.ArrayList;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlp5.TriangleButtonView;
import geom.Rect;
import phasing.PhasesPApplet;
import phasing.Phrase;
import phasing.PhrasePicture;

/**
 * A screen for saving and loading Phrases.
 * 
 * @author James Morrow
 *
 */
public class PhraseRepository extends Screen implements CellEventHandler {
	//cells
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	
	//current page number
	private int currPageNum;
	
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
		updatePageButtonStates();
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
		populateCellList(new Rect(pa.width/2, pa.height/2, pa.width - 125, pa.height - 125, pa.CENTER), 150);
	}
	
	/**
	 * Populates the list of cells with cells.
	 * 
	 * @param box The size of a cell.
	 * @param rowSize The number of cells in a row.
	 * @param colSize The number of cells in a column.
	 */
	private void populateCellList(Rect box, float cellSize) {
		cells.clear();
		
		int rowSize = (int)(box.getWidth() / cellSize);
		int colSize = (int)(box.getHeight() / cellSize);
		
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
	 ***** ControlP5 Callbacks *****
	 *******************************/
	
	/**
	 * Callback for when pageLeftButton receives an event.
	 * @param e
	 */
	public void pageLeft(ControlEvent e) {
		currPageNum--;
		updatePageButtonStates();
	}
	
	/**
	 * Callback for when pageRightButton receives an event.
	 * @param e
	 */
	public void pageRight(ControlEvent e) {
		currPageNum++;
		updatePageButtonStates();
	}
	
	/****************************
	 ***** CellEventHandler *****
	 ****************************/
	
	@Override
	public void copy(Cell cell) {
		int i = cells.indexOf(cell) + currPageNum*cells.size();
		if (i < pa.phrasePictures.size()) {
			PhrasePicture p = pa.phrasePictures.get(i);
			addPhrasePicture(i+1, new PhrasePicture(p));
		}
	}

	/*@Override
	public void load(Cell cell) {
		int i = cells.indexOf(cell) + currPageNum*cells.size();
		if (i < pa.phrasePictures.size()) {
			pa.currentPhrasePicture = pa.phrasePictures.get(i);
			pa.currentPhrase = pa.currentPhrasePicture.getPhrase();
		}
	}*/
	
	@Override
	public void delete(Cell cell) {
		int i = cells.indexOf(cell);
		if (i != -1) {
			removePhrasePicture(i + currPageNum*cells.size());
		}
	}
	
	@Override
	public void newPhrase() {
		pa.currentPhrase = new Phrase();
		pa.currentPhrasePicture = new PhrasePicture(pa.currentPhrase, pa);
		addPhrasePicture(pa.currentPhrasePicture);
	}
	
	@Override
	public void generatePhrase() {
		pa.currentPhrase = pa.generateReichLikePhrase();
		pa.currentPhrasePicture = new PhrasePicture(pa.currentPhrase, pa);
		addPhrasePicture(pa.currentPhrasePicture);
	}
	
	/**
	 * Removes a PhrasePicture from the list and sets whether the page buttons are hidden or shown.
	 * @param i
	 */
	private void removePhrasePicture(int i) {
		pa.phrasePictures.remove(i);
		updatePageButtonStates();
	}
	
	/**
	 * Adds a PhrasePicture to the end of the list and sets whether the page buttons are hidden or shown.
	 * @param p
	 */
	private void addPhrasePicture(PhrasePicture p) {
		addPhrasePicture(pa.phrasePictures.size(), p);
	}
	
	/**
	 * Adds a PhrasePicture to the list and sets whether the page buttons are hidden or shown.
	 * @param i
	 * @param p
	 */
	private void addPhrasePicture(int i, PhrasePicture p) {
		pa.phrasePictures.add(i, p);
		updatePageButtonStates();
	}
	
	/*********************************
	 ***** Screen Event Handling *****
	 *********************************/
	
	@Override
	public void windowResized() {
		cp5.dispose();
		cp5 = new ControlP5(pa);
		initCells();
		initDirectionalButtons();
	}

	@Override
	public void onEnter() {
		cp5.show();
		currPageNum = 0;
		updatePageButtonStates();
	}

	@Override
	public void onExit() {
		cp5.hide();
		pa.savePlayerInfo();
	}
	
	@Override
	public void onPause() {}
	
	@Override
	public void onResume() {}
	
	/********************************
	 ***** Input Event Handling *****
	 ********************************/
	
	@Override
	public void mousePressed() {
		Cell c = cellTouching(pa.mouseX, pa.mouseY);
		if (c != null) {
			load(c);
		}
	}
	
	private void load(Cell cell) {
		int i = cells.indexOf(cell) + currPageNum*cells.size();
		if (i < pa.phrasePictures.size()) {
			pa.currentPhrasePicture = pa.phrasePictures.get(i);
			pa.currentPhrase = pa.currentPhrasePicture.getPhrase();
		}
	}
	
	/**
	 * Gives the Cell that touches the given (x,y), or null if no such Cell exists.
	 * @param x
	 * @param y
	 * @return The Cell that touches (x,y) or null.
	 */
	private Cell cellTouching(int x, int y) {
		for (Cell c : cells) {
			if (c.touches(x, y)) {
				return c;
			}
		}
		return null;
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/
	
	/**
	 * Sets whether the left and right page buttons are hidden or shown.
	 */
	private void updatePageButtonStates() {
		if (currPageNum > 0) {
			pageLeftButton.show();
		}
		else {
			pageLeftButton.hide();
		}
		
		if (pa.phrasePictures.size() - currPageNum*cells.size() >= this.cells.size()) {
			pageRightButton.show();
		}
		else {
			pageRightButton.hide();
		}
	}
	
	@Override
	public void drawWhilePaused() {
		
	}
	
	@Override
	public void draw() {
		pa.background(255);
		int i = 0;
		int j = currPageNum*cells.size();
		while (i < cells.size()) {
			Cell c = cells.get(i);
			if (j < pa.phrasePictures.size()) {
				c.draw(pa.phrasePictures.get(j), pa);
			}
			else if (j == pa.phrasePictures.size()) {
				c.draw(true, pa);
			}
			else {
				c.draw(false, pa);
			}
			i++;
			j++;
		}
	}
}