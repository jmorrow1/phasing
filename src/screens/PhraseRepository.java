package screens;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import controlP5.ControlP5;
import geom.Rect;
import phases.PhasesPApplet;
import processing.core.PApplet;
import processing.data.JSONObject;

public class PhraseRepository extends Screen {
	//variable for testing
	private boolean populateCellsWithRandomPhrases = false;
	
	//cells
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	
	//controlp5
	private ControlP5 cp5;
	
	public PhraseRepository(PhasesPApplet pa) {
		super(pa);
		
		cp5 = new ControlP5(pa);
		
		switch (pa.screenSizeMode) {
			case PhasesPApplet._800x600 :
				constructCells(new Rect(50, 75, 725, 550, pa.CORNERS), 4, 3);
				break;
		}
		
		//create new phrase pictures:
		if (populateCellsWithRandomPhrases) {
			populateCellsWithRandomPhrases();
			writePhrasePictures(Cell.toPhraseList(cells));
		}
		//load phrase pictures:
		else {
			try {
				ArrayList<PhrasePicture> phrasePictures = new ArrayList<PhrasePicture>();
				Files.walk(Paths.get(pa.saveFolderPath + "phrases\\")).forEach(filePath -> {
					if (filePath.toString().endsWith(".json")) {
						JSONObject json = pa.loadJSONObject(filePath.toString());
						phrasePictures.add(new PhrasePicture(json));
					}
				});
				assignPhrasesToCells(phrasePictures);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		cp5.hide();
	}
	
	/******************
	 ***** Saving *****
	 ******************/
	
	private void writePhrasePictures(ArrayList<PhrasePicture> phrasePictures) {
		for (int i=0; i<phrasePictures.size(); i++) {
			PhrasePicture p = phrasePictures.get(i);
			pa.saveJSONObject(p.toJSON(), pa.saveFolderPath + "phrases\\" + p.getName() + ".json");
		}
	}
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	private void assignPhrasesToCells(ArrayList<PhrasePicture> phrasePictures) {
		int end = PApplet.min(cells.size(), phrasePictures.size());
		for (int i=0; i<end; i++) {
			cells.get(i).setPhrasePicture(phrasePictures.get(i));
		}
	}
	
	private void constructCells(Rect box, int rowSize, int colSize) {
		float cellSize = pa.min(box.getWidth() / rowSize, box.getHeight() / colSize);
		
		float y1 = box.getCeny() - cellSize*0.5f*colSize;
		for (int j=0; j<colSize; j++) {
			float x1 = box.getCenx() - cellSize*0.5f*rowSize;
			for (int i=0; i<rowSize; i++) {
				cells.add(new Cell(new Rect(x1, y1, cellSize, cellSize, pa.CORNER), cp5));
				x1 += cellSize;
			}
			y1 += cellSize;
		}
	}
	
	private void populateCellsWithRandomPhrases() {
		int x = (int)'a';
		for (Cell c : cells) {
			c.setPhrasePicture(new PhrasePicture(pa.generateReichLikePhrase(pa.scale), "" + (char)x, pa));
			x++;
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