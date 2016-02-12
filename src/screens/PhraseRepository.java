package screens;

import java.io.File;
import java.util.ArrayList;

import geom.Rect;
import phases.JSONable.Util;
import phases.PhasesPApplet;
import processing.core.PApplet;
import processing.data.JSONArray;

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
		
		//ArrayList<PhrasePicture> phrasePictures = readPhrasePictures(new File(pa.saveFolderPath + "phrases" + ".json"));
		//assignPhrasesToCells(phrasePictures);
		
		writePhrasePictures(Cell.toPhraseList(cells), "phrases");
	}
	
	/****************************
	 ***** Saving / Loading *****
	 ****************************/
	
	/**
	 * Takes an Arraylist of PhrasePictures, jsonifies it, and writes it to a file with the directory given
	 * by the PhasesPApplet field, saveFolderPath.
	 * 
	 * @param phrases The ArrayList of phrases.
	 * @param name The name of the file.
	 */
	private void writePhrasePictures(ArrayList<PhrasePicture> phrasePictures, String name) {
		JSONArray json = Util.jsonify(phrasePictures);
		pa.saveJSONArray(json, pa.saveFolderPath + name + ".json");
	}
	
	/**
	 * Takes a file, tries to read it as a json array, and tries to construct an ArrayList<PhrasePicture> from it.
	 * If it fails, it will return an empty ArrayList<PhrasePicture>.
	 * 
	 * @param file The file.
	 * @return An ArrayList<PhrasePicture>.
	 */
	private ArrayList<PhrasePicture> readPhrasePictures(File file) {
		ArrayList<PhrasePicture> phrasePictures = new ArrayList<PhrasePicture>();
		
		if (file.exists()) {
			JSONArray json = pa.loadJSONArray(file);
			for (int i=0; i<json.size(); i++) {
				phrasePictures.add(new PhrasePicture(json.getJSONObject(i)));
			}
		}
		
		return phrasePictures;
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
				cells.add(new Cell(new Rect(x1, y1, cellSize, cellSize, pa.CORNER)));
				x1 += cellSize;
			}
			y1 += cellSize;
		}
	}
	
	private void populateCellsWithRandomPhrases() {
		for (Cell c : cells) {
			c.setPhrasePicture(new PhrasePicture(pa.generateReichLikePhrase(pa.scale), pa));
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
			c.draw(pa);
		}
	}
}