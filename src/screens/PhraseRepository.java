package screens;

import java.util.ArrayList;

import controlP5.Button;
import controlP5.ControlP5;
import controlp5.TriangleButtonView;
import geom.Rect;
import phasing.PhasesPApplet;
import phasing.PhrasePicture;
import processing.core.PApplet;

public class PhraseRepository extends Screen {
	//variable for testing
	private boolean populateCellsWithRandomPhrases = true;
	
	//cells
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	private PhrasePicture derivativePhrasePicture;
	
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
		
		assignPhrasesToCells(pa.phrasePictures);
	}
	
	private void generateRandomPhrasePictures(int n) {
		pa.phrasePictures.clear();
		for (int i=0; i<n; i++) {
			PhrasePicture p = new PhrasePicture(pa.generateReichLikePhrase(), pa);
			pa.phrasePictures.add(p);
		}
		pa.savePhrasePictures();
	}
	
	private void assignPhrasesToCells(ArrayList<PhrasePicture> phrasePictures) {
		int end = PApplet.min(cells.size(), phrasePictures.size()+1);
		if (end > 0) {
			cells.get(0).setPhrasePicture(pa.currentPhrasePicture);
		}
		for (int i=1; i<end; i++) {
			cells.get(i).setPhrasePicture(phrasePictures.get(i-1));
		}
	}
	
	private void constructCells(Rect box, int rowSize, int colSize) {
		float cellSize = pa.min(box.getWidth() / rowSize, box.getHeight() / colSize);
		
		float y1 = box.getCeny() - cellSize*0.5f*colSize;
		for (int j=0; j<colSize; j++) {
			float x1 = box.getCenx() - cellSize*0.5f*rowSize;
			for (int i=0; i<rowSize; i++) {
				if (i == 0 && j == 0) {
					cells.add(new Cell(false, new Rect(x1, y1, cellSize, cellSize, pa.CORNER), cp5, this, pa));
				}
				else {
					cells.add(new Cell(true, new Rect(x1, y1, cellSize, cellSize, pa.CORNER), cp5, this, pa));
				}
				x1 += cellSize;
			}
			y1 += cellSize;
		}
	}
	
	/*******************************
	 ***** Interface with Cell *****
	 *******************************/
	
	protected void load(PhrasePicture phrasePicture) {
		pa.currentPhrase.set(phrasePicture.getPhrase());
		derivativePhrasePicture = phrasePicture;
		int i = indexOf(derivativePhrasePicture);
		pa.playerInfo.derivativePhraseIndex = i;
		pa.savePlayerInfo();
	}
	
	private int indexOf(PhrasePicture phrasePicture) {
		for (int i=0; i<cells.size(); i++) {
			if (cells.get(i).getPhrasePicture() == phrasePicture) {
				return i;
			}
		}
		return -1;
	}
	
	/*************************************
	 ***** Enter/Exit Event Handling *****
	 *************************************/

	@Override
	public void onEnter() {
		cp5.show();
		int i = pa.playerInfo.derivativePhraseIndex;
		if (i != -1 && cells.size() > i) {
			derivativePhrasePicture = cells.get(i).getPhrasePicture();
		}
	}

	@Override
	public void onExit() {
		cp5.hide();
		int i = indexOf(derivativePhrasePicture);
		pa.playerInfo.derivativePhraseIndex = i;
		pa.savePlayerInfo();
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/

	@Override
	public void draw() {
		pa.background(255);
		
		for (Cell c : cells) {
			c.draw(pa);
		}
	}
}