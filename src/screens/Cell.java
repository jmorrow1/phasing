package screens;

import java.util.ArrayList;

import controlP5.Button;
import controlP5.ControlP5;
import geom.Rect;
import phasing.PhasesPApplet;
import phasing.Phrase;
import phasing.PhrasePicture;

/**
 * 
 * @author James Morrow
 *
 */
class Cell {
	private Rect rect;
	private PhrasePicture phrasePicture;
	private Button loadButton;
	private static int nextID = (int)'a';
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	protected Cell(Rect rect, ControlP5 cp5) {
		this.rect = rect;
		
		initLoadButton(cp5);
	}
	
	private void initLoadButton(ControlP5 cp5) {
		float x1 = rect.getX1();
		float y1 = rect.getY1() + 0.9f * rect.getHeight();
		float width = 0.4f * rect.getWidth();
		float height = 0.1f * rect.getHeight();
		this.loadButton = cp5.addButton((char)nextID + " load")
						     .setLabel("Load")
						     .setPosition(x1, y1)
						     .setSize((int)width, (int)height)
						     .setId(-1)
						     .plugTo(this);
						     ;
		//TODO: Make label be displayed as upper and lower case characters.
		//TODO: Fix font blurriness.
		PhasesPApplet.colorButtonShowLabel(loadButton);
		nextID++;
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/
	
	protected void draw(PhasesPApplet pa) {
		pa.noFill();
		pa.strokeWeight(1);
		pa.stroke(150);
		rect.display(pa);
		
		if (hasPhrase()) {
			phrasePicture.draw(rect, pa);
		}
	}
	
	/*******************
	 ***** Utility *****
	 *******************/
	
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
