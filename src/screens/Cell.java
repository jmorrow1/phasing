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
	private static int nextId = (int)'a';
	private final static int LOAD = -1, GENERATE_PHRASE = -2;
	
	//outside world
	private PhasesPApplet pa;
	private PhraseRepository phraseRepo;
	
	//dimensions
	private Rect rect;
	
	//phrase picture (optional)
	private PhrasePicture phrasePicture;
	
	//other data
	private Button loadButton;
	private Button generateButton;
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	protected Cell(boolean isLoader, Rect rect, ControlP5 cp5, PhraseRepository phraseRepo, PhasesPApplet pa) {
		this.rect = rect;
		if (isLoader) {
			initLoadButton(cp5);
		}
		initGenerateButton(cp5);
		nextId++;
		this.phraseRepo = phraseRepo;
		this.pa = pa;
	}
	
	private void initLoadButton(ControlP5 cp5) {
		float x1 = rect.getX1();
		float y1 = rect.getY1() + 0.9f * rect.getHeight();
		float width = 0.4f * rect.getWidth();
		float height = 0.1f * rect.getHeight();
		this.loadButton = cp5.addButton((char)nextId + " load")
						     .setLabel("Load")
						     .setPosition(x1, y1)
						     .setSize((int)width, (int)height)
						     .setId(LOAD)
						     .plugTo(this);
						     ;	     
		//TODO: Fix font blurriness.
		loadButton.getCaptionLabel().setFont(pa.pfont12);
		loadButton.getCaptionLabel().toUpperCase(false);
		PhasesPApplet.colorButtonShowLabel(loadButton);
		
	}
	
	private void initGenerateButton(ControlP5 cp5) {
		float width = 0.75f * rect.getWidth();
		float height = 0.25f * rect.getHeight();
		float x1 = rect.getCenx() - width/2f;
		float y1 = rect.getCeny() - height/2f;
		this.generateButton = cp5.addButton("Generate Phrase " + (char)nextId)
				                 .setLabel("Generate Phrase")
				                 .setPosition(x1, y1)
				                 .setSize((int)width, (int)height)
				                 .setId(GENERATE_PHRASE)
				                 .plugTo(this)
				                 ;
		generateButton.getCaptionLabel().setFont(pa.pfont12);
		generateButton.getCaptionLabel().toUpperCase(false);
		PhasesPApplet.colorButtonShowLabel(generateButton);
	}
	
	/*******************************
	 ***** ControlP5 Callbacks *****
	 *******************************/
	
	public void controlEvent(ControlEvent e) {
		switch (e.getId()) {
			case LOAD :
				if (phrasePicture != null) {
					phraseRepo.load(this.phrasePicture);
				}
				break;
			case GENERATE_PHRASE :
				phrasePicture = new PhrasePicture(pa.generateReichLikePhrase(), pa);
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
			if (generateButton != null) generateButton.hide();
		}
		else {
			if (loadButton != null) loadButton.hide();
			if (generateButton != null) generateButton.show();
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
