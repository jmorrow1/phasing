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
	private final static int LOAD = -1, GENERATE = -2, COPY = -3;
	
	//outside world
	private PhasesPApplet pa;
	private PhraseRepository phraseRepo;
	
	//dimensions
	private Rect rect;

	//buttons
	private Button copyButton;
	private Button loadButton;
	private Button generateButton;
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	protected Cell(Rect rect, ControlP5 cp5, PhraseRepository phraseRepo, PhasesPApplet pa) {
		this.rect = rect;
		initLoadButton(cp5);
		initOverwriteButton(cp5);
		initGenerateButton(cp5);
		nextId++;
		this.phraseRepo = phraseRepo;
		this.pa = pa;
	}
	
	private void initOverwriteButton(ControlP5 cp5) {
		float x1 = rect.getX1();
		float y1 = rect.getY1() + 0.9f * rect.getHeight();
		float width = 0.4f * rect.getWidth();
		float height = 0.1f * rect.getHeight();
		this.copyButton = cp5.addButton((char)nextId + " copy")
				                  .setLabel("Copy")
			                      .setPosition(x1, y1)
			                      .setSize((int)width, (int)height)
			                      .setId(COPY)
			                      .plugTo(this);
			                      ;
        copyButton.getCaptionLabel().setFont(pa.pfont12);
        copyButton.getCaptionLabel().toUpperCase(false);
  		PhasesPApplet.colorButtonShowLabel(copyButton);
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
				                 .setId(GENERATE)
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
			case COPY :
				phraseRepo.copy(this);
				break;
			case LOAD :
				phraseRepo.load(this);
				break;
			case GENERATE :
				phraseRepo.generate();
				break;
		}
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/
	
	private void drawBorder(boolean isCurrentPhrase, PhasesPApplet pa) {
		pa.noFill();
		pa.strokeWeight(1);
		pa.stroke(isCurrentPhrase ? 0 : 150);
		pa.rect(rect.getX1(), rect.getY1(), rect.getWidth() - 1, rect.getHeight() - 1);
	}
	
	protected void draw(boolean showGenerateButton, PhasesPApplet pa) {
		drawBorder(false, pa);
		copyButton.hide();
		loadButton.hide();
		if (showGenerateButton) {
			generateButton.show();
		}
		else {
			generateButton.hide();
		}
	}
	
	protected void draw(PhrasePicture phrasePicture, PhasesPApplet pa) {
		boolean isCurrentPhrase = phrasePicture == pa.currentPhrasePicture;
		drawBorder(isCurrentPhrase, pa);
		
		phrasePicture.draw(rect, pa);
		
		String name = phrasePicture.getName();
		pa.textAlign(pa.CENTER, pa.TOP);
		pa.fill(0);
		pa.text(name, rect.getCenx(), rect.getY1());
		
		if (isCurrentPhrase) {
			copyButton.show();
			loadButton.hide();
		}
		else {
			copyButton.hide();
			loadButton.show();
		}
		generateButton.hide();
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
	/*public static ArrayList<PhrasePicture> toPhraseList(ArrayList<Cell> cells) {
		ArrayList<PhrasePicture> phraseList = new ArrayList<PhrasePicture>();
		for (Cell c : cells) {
			if (c.hasPhrase()) {
				phraseList.add(c.getPhrasePicture());
			}
		}
		return phraseList;
	}*/

	/*******************************
	 ***** Getters and Setters *****
	 *******************************/
	
	/*protected boolean hasPhrase() {
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
	}*/
}
