package screens;

import java.util.ArrayList;

import controlP5.Button;
import controlP5.ControlEvent;
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
	//class-scope
	private static int nextID = (int)'a';
	private final static int LOAD = -1;
	
	//outside world
	private PhasesPApplet pa;
	private PhraseRepository phraseRepo;
	
	//dimensions
	private Rect rect;
	
	//phrase picture (optional)
	private PhrasePicture phrasePicture;
	
	//other data
	private Button loadButton;
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	protected Cell(boolean hasLoadButton, Rect rect, ControlP5 cp5, PhraseRepository phraseRepo, PhasesPApplet pa) {
		this.rect = rect;
		if (hasLoadButton) {
			initLoadButton(cp5);
		}
		this.phraseRepo = phraseRepo;
		this.pa = pa;
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
						     .setId(LOAD)
						     .plugTo(this);
						     ;
		//TODO: Make label be displayed as upper and lower case characters.
		//TODO: Fix font blurriness.
		PhasesPApplet.colorButtonShowLabel(loadButton);
		nextID++;
	}
	
	/*******************************
	 ***** ControlP5 Callbacks *****
	 *******************************/
	
	public void controlEvent(ControlEvent e) {
		switch (e.getId()) {
			case LOAD :
				if (phrasePicture != null) pa.phrase.set(phrasePicture.getPhrase());
				break;
		}
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
			String name = phrasePicture.getName();
			pa.textAlign(pa.CENTER, pa.TOP);
			pa.fill(0);
			pa.text(name, rect.getCenx(), rect.getY1());
			if (loadButton != null) loadButton.show();
		}
		else {
			if (loadButton != null) loadButton.hide();
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