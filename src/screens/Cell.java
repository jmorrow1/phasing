package screens;

import java.io.Serializable;
import java.util.ArrayList;

import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;
import processing.core.PApplet;

class Cell {
	private Rect rect;
	private PictureStyle pictureStyle;
	private Phrase phrase;
	
	protected Cell(Rect rect) {
		this.rect = rect;
	}
	
	protected void draw(PhasesPApplet pa) {
		pa.noFill();
		pa.strokeWeight(1);
		pa.stroke(150);
		rect.display(pa);
		
		if (hasPhrase()) {
			pictureStyle.draw(phrase, rect, pa);
		}
	}
	
	/**
	 * Takes an ArrayList<Cell>, extracts its DrawablePhrases, and puts those in an ArrayList.
	 * Then it returns the ArrayList of DrawablePhrases.
	 * 
	 * @param cells The ArrayList<Cell>.
	 * @return The ArrayList<Phrase>.
	 */
	public static ArrayList<Phrase> toPhraseList(ArrayList<Cell> cells) {
		ArrayList<Phrase> phraseList = new ArrayList<Phrase>();
		for (Cell c : cells) {
			if (c.hasPhrase()) {
				phraseList.add(c.getPhrase());
			}
		}
		return phraseList;
	}

	/*******************************
	 ***** Getters and Setters *****
	 *******************************/
	
	protected boolean hasPhrase() {
		return phrase != null;
	}
	
	protected void addPhrase(Phrase phrase, PApplet pa) {
		this.phrase = phrase;
		this.pictureStyle = new PictureStyle(pa);
	}
	
	protected Phrase getPhrase() {
		return phrase;
	}
}
