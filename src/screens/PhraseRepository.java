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
import util.NameGenerator;

/**
 * A screen for saving and loading Phrases.
 * 
 * @author James Morrow
 *
 */
public class PhraseRepository extends Screen implements CellEventHandler {
	//cells
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	private Cell selectedCell;
	
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
		updateDirectionalButtonStates();
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
		updateDirectionalButtonStates();
	}
	
	/**
	 * Callback for when pageRightButton receives an event.
	 * @param e
	 */
	public void pageRight(ControlEvent e) {
		currPageNum++;
		updateDirectionalButtonStates();
	}
	
	/****************************
	 ***** CellEventHandler *****
	 ****************************/
	
	@Override
	public void copy(Cell cell) {
		int i = cells.indexOf(cell) + currPageNum*cells.size();
		if (i < pa.getNumPhrasePictures()) {
			PhrasePicture p = pa.getPhrasePicture(i);
			addPhrasePicture(i+1, new PhrasePicture(p));
		}
	}
	
	@Override
	public void delete(Cell cell) {
		int i = cells.indexOf(cell);
		if (i != -1) {
			removePhrasePicture(i + currPageNum*cells.size());
		}
	}
	
	@Override
	public void newPhrase() {
		pa.currentPhrase = new Phrase("Major", "C");
		pa.currentPhrasePicture = new PhrasePicture(pa.currentPhrase, pa);
		addPhrasePicture(pa.currentPhrasePicture);
		pa.setCurrentScale(pa.currentPhrase.getScaleClassName(), pa.currentPhrase.getScaleRootName());
	}
	
	@Override
	public void generatePhrase() {
		pa.currentPhrase = pa.generateReichLikePhrase();
		pa.currentPhrasePicture = new PhrasePicture(pa.currentPhrase, pa);
		addPhrasePicture(pa.currentPhrasePicture);
		pa.setCurrentScale(pa.currentPhrase.getScaleClassName(), pa.currentPhrase.getScaleRootName());
		setSelectedCellTitle();
	}
	
	/**
	 * Removes a PhrasePicture from the list and sets whether the page buttons are hidden or shown.
	 * @param i
	 */
	private void removePhrasePicture(int i) {
		String name = pa.getPhrasePicture(i).getName();
		pa.removePhrasePicture(i);
		updateDirectionalButtonStates();
	}
	
	/**
	 * Adds a PhrasePicture to the end of the list and sets whether the page buttons are hidden or shown.
	 * @param p The PhrasePicture
	 */
	private void addPhrasePicture(PhrasePicture p) {
		addPhrasePicture(pa.getNumPhrasePictures(), p);
	}
	
	/**
	 * Adds a PhrasePicture to the list and sets whether the page buttons are hidden or shown.
	 * @param i The index.
	 * @param p The PhrasePicture.
	 */
	private void addPhrasePicture(int i, PhrasePicture p) {
		pa.addPhrasePicture(i, p);
		updateDirectionalButtonStates();
	}
	
	/*********************************
	 ***** Screen Event Handling *****
	 *********************************/
	
	@Override
	public void windowResized() {
		cp5.dispose();
		cp5 = new ControlP5(pa);
		initCells();
		while (currPageNum*cells.size() > pa.getNumPhrasePictures()) {
			currPageNum--;
		}
		initDirectionalButtons();
	}

	@Override
	public void onEnter() {
		cp5.dispose();
		cp5 = new ControlP5(pa);
		initCells();
		cp5.show();
		currPageNum = pa.indexOfCurrentPhrasePicture() != -1 ? pa.indexOfCurrentPhrasePicture() / cells.size() : 0;
		initDirectionalButtons();
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
		Cell prevSelectedCell = selectedCell;
		selectedCell = cellTouching(pa.mouseX, pa.mouseY);

		if (selectedCell != null) {
			
			//deal with previously selected cell (whose PhrasePicture may have been renamed)
			if (prevSelectedCell != null) {
				String name = prevSelectedCell.getTitle();
				
				int i = pa.indexOfCurrentPhrasePicture();
				
				if (i != -1) {
					pa.removePhrasePicture(pa.currentPhrasePicture);
					
					if (pa.hasAnotherPhrasePictureWithName(pa.currentPhrasePicture, name)) {
						pa.currentPhrasePicture.setName(pa.phrasePictureNameGenerator.getUniqueNameFrom(name));
					}
					else {
						pa.currentPhrasePicture.setName(name);
					}
					
					pa.addPhrasePicture(i, pa.currentPhrasePicture);
				}
			}
			
			//deal with newly selected cell
			load(selectedCell);
			setSelectedCellTitle();
		}
	}
	
	/**
	 * Sets the selectedCell's title based on the name of the currentPhrasePicture.
	 */
	private void setSelectedCellTitle() {
		int index = cells.indexOf(selectedCell) + currPageNum*cells.size();
		if (index < pa.getNumPhrasePictures()) {
			pa.currentPhrasePicture = pa.getPhrasePicture(index);
			pa.currentPhrase = pa.currentPhrasePicture.getPhrase();
			selectedCell.setTitle(pa.currentPhrasePicture.getName());
		}
	}
	
	/**
	 * Loads the PhrasePicture at the given cell's location.
	 * @param cell
	 */
	private void load(Cell cell) {
		int i = cells.indexOf(cell) + currPageNum*cells.size();
		if (i < pa.getNumPhrasePictures()) {
			pa.currentPhrasePicture = pa.getPhrasePicture(i);
			pa.currentPhrase = pa.currentPhrasePicture.getPhrase();
			pa.setCurrentScale(pa.currentPhrase.getScaleClassName(), pa.currentPhrase.getScaleRootName());
		}
	}
	
	/**
	 * Gives the Cell that touches the given (x,y), or null if no such Cell exists.
	 * @param x
	 * @param y
	 * @return The Cell that touches (x,y) or null.
	 */
	private Cell cellTouching(int x, int y) {
		for (int i=0; i<cells.size(); i++) {
			Cell c = cells.get(i);
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
	private void updateDirectionalButtonStates() {
		if (currPageNum > 0) {
			pageLeftButton.show();
		}
		else {
			pageLeftButton.hide();
		}
		
		if (pa.getNumPhrasePictures() - currPageNum*cells.size() >= this.cells.size()) {
			pageRightButton.show();
		}
		else {
			pageRightButton.hide();
		}
	}
	
	@Override
	public void drawWhilePaused() {}
	
	@Override
	public void draw() {
		pa.background(255);
		pa.drawControlP5();
		int i = 0;
		int j = currPageNum*cells.size();
		while (i < cells.size()) {
			Cell c = cells.get(i);
			if (j < pa.getNumPhrasePictures()) {
				c.draw(pa.getPhrasePicture(j), pa);
			}
			else if (j == pa.getNumPhrasePictures()) {
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