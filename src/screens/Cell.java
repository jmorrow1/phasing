package screens;

import java.io.Serializable;
import java.util.ArrayList;

import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;
import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
class Cell {
	private Rect rect;
	private PhrasePicture phrasePicture;
	
	protected Cell(Rect rect) {
		this.rect = rect;
	}
	
	protected void draw(PhasesPApplet pa) {
		pa.noFill();
		pa.strokeWeight(1);
		pa.stroke(150);
		rect.display(pa);
		
		if (hasPhrase()) {
			phrasePicture.draw(rect, pa);
		}
	}
	
	/**
	 * Takes an ArrayList<Cell>, extracts its PhrasePictures, and puts those in an ArrayList.
	 * Then it returns the ArrayList of PhrasePictures.
	 * 
	 * @param cells The ArrayList<Cell>.
	 * @return The ArrayList<PhrasePicture>.
	 */
	public static ArrayList<PhrasePicture> toPhraseList(ArrayList<Cell> cells) {
		ArrayList<PhrasePicture> phraseList = new ArrayList<PhrasePicture>();
		for (Cell c : cells) {
			if (c.hasPhrase()) {
				phraseList.add(c.getPhrasePicture());
			}
		}
		return phraseList;
	}

	/*******************************
	 ***** Getters and Setters *****
	 *******************************/
	
	protected boolean hasPhrase() {
		return phrasePicture != null;
	}
	
	protected void setPhrasePicture(PhrasePicture phrasePicture) {
		this.phrasePicture = phrasePicture;
	}
	
	protected PhrasePicture getPhrasePicture() {
		return phrasePicture;
	}
	
	protected Phrase getPhrase() {
		return phrasePicture.getPhrase();
	}
}
