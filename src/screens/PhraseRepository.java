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

public class PhraseRepository extends Screen {
	//variable for testing
	private boolean populateCellsWithRandomPhrases;
	
	//cells
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	
	//controlp5
	private ControlP5 cp5;
	private Button pageLeftButton, pageRightButton;
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	public PhraseRepository(PhasesPApplet pa) {
		super(pa);
		
		cp5 = new ControlP5(pa);
		initCells();
		initDirectionalButtons();
		cp5.hide();
	}
	
	private void initDirectionalButtons() {
		int size = 40;
		pageLeftButton = initDirectionalButton("pageLeft", 10, pa.height - size - 5, size, pa.PI);
		pageRightButton = initDirectionalButton("pageRight", pa.width - size - 10, pa.height - size - 5, size, 0);
		pageLeftButton.hide();
		pageRightButton.hide();
	}
	
	private Button initDirectionalButton(String name, int x1, int y1, int size, float angle) {
		Button b = cp5.addButton(name)
			          .setPosition(x1, y1)
			          .setSize(size, size)
			          .setView(new TriangleButtonView(angle, 0.7f*pa.PI))
			          .plugTo(this)
	                  ;  
		pa.colorButtonHideLabel(b);
		return b;
	}
	
	private void initCells() {
		switch (pa.screenSizeMode) {
			case PhasesPApplet._800x600 :
				constructCells(new Rect(50, 75, 725, 550, pa.CORNERS), 4, 3);
				break;
		}
		
		if (populateCellsWithRandomPhrases) {
			generateRandomPhrasePictures(cells.size()-2);
		}
		
		//assignPhrasesToCells(pa.phrasePictures);
	}
	
	private void generateRandomPhrasePictures(int n) {
		pa.phrasePictures.clear();
		for (int i=0; i<n; i++) {
			PhrasePicture p = new PhrasePicture(pa.generateReichLikePhrase(), pa);
			pa.phrasePictures.add(p);
		}
		pa.savePhrasePictures();
	}
	
	/*private void assignPhrasesToCells(ArrayList<PhrasePicture> phrasePictures) {
		int end = PApplet.min(cells.size(), phrasePictures.size());
		for (int i=0; i<end; i++) {
			cells.get(i).setPhrasePicture(phrasePictures.get(i));
		}
	}*/
	
	private void constructCells(Rect box, int rowSize, int colSize) {
		float cellSize = pa.min(box.getWidth() / rowSize, box.getHeight() / colSize);
		
		float y1 = box.getCeny() - cellSize*0.5f*colSize;
		for (int j=0; j<colSize; j++) {
			float x1 = box.getCenx() - cellSize*0.5f*rowSize;
			for (int i=0; i<rowSize; i++) {
				cells.add(new Cell(new Rect(x1, y1, cellSize, cellSize, pa.CORNER), cp5, this, pa));
				x1 += cellSize;
			}
			y1 += cellSize;
		}
	}
	
	/*******************************
	 ***** Interface with Cell *****
	 *******************************/
	
	protected void copy(Cell cell) {
		int i = cells.indexOf(cell);
		if (i < pa.phrasePictures.size()) {
			PhrasePicture p = pa.phrasePictures.get(i);
			pa.phrasePictures.add(i+1, new PhrasePicture(p));
		}
	}
	
	protected void generate() {
		pa.currentPhrase = pa.generateReichLikePhrase();
		pa.currentPhrasePicture = new PhrasePicture(pa.currentPhrase, pa);
		pa.phrasePictures.add(pa.currentPhrasePicture);
	}
	
	protected void load(Cell cell) {
		int i = cells.indexOf(cell);
		if (i < pa.phrasePictures.size()) {
			pa.currentPhrasePicture = pa.phrasePictures.get(i);
			pa.currentPhrase = pa.currentPhrasePicture.getPhrase();
		}
	}
	
	/*************************************
	 ***** Enter/Exit Event Handling *****
	 *************************************/

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