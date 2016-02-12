package screens;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;
import processing.core.PApplet;

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
		
		/*File file = new File(pa.saveFolderPath + "phrases" + ".ser");
		if (file.exists()) {
			ArrayList<PhrasePicture> phrasePictures = readPhrases(file);
			assignPhrasesToCells(phrasePictures);
		}*/
		
		//writeToFile(Cell.toPhraseList(cells), "phrases");
	}
	
	/****************************
	 ***** Saving / Loading *****
	 ****************************/
	
	/**
	 * Takes an Arraylist of PhrasePictures, serializes it, and writes it to a file with the directory given
	 * by the PhasesPApplet field, saveFolderPath.
	 * 
	 * @param phrases The ArrayList of phrases.
	 * @param name The name of the file.
	 */
	private void writeToFile(ArrayList<PhrasePicture> phrases, String name) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pa.saveFolderPath + name + ".ser")));
			oos.writeObject(phrases);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Takes a file and tries to deserialize it and construct an ArrayList<PhrasePicture> from it.
	 * If it fails, it will return an empty ArrayList<PhrasePicture>.
	 * 
	 * @param file The file.
	 * @return An ArrayList<PhrasePicture>.
	 */
	private ArrayList<PhrasePicture> readPhrases(File file) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			ArrayList<PhrasePicture> phrases = (ArrayList<PhrasePicture>) ois.readObject();
			ois.close();
			return phrases;
		} catch (IOException | ClassNotFoundException | ClassCastException e) {
			e.printStackTrace();
			return new ArrayList<PhrasePicture>();
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