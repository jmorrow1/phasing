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
	private final static int LOAD = -1, GENERATE = -2, COPY = -3, NEW = -4;
	
	//outside world
	private CellEventHandler eventHandler;
	
	//dimensions
	private Rect rect;

	//buttons
	private Button copyButton;
	private Button loadButton;
	private Button generateButton;
	private Button newPhraseButton;
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	/**
	 * 
	 * @param rect The area in which to draw the cell.
	 * @param cp5 The ControlP5 instance to which to add controllers.
	 * @param phraseRepo The object to send events.
	 */
	protected Cell(Rect rect, ControlP5 cp5, PhraseRepository phraseRepo) {
		this.rect = rect;
		initLoadButton(cp5);
		initCopyButton(cp5);
		initGenerateButton(cp5);
		initNewPhraseButton(cp5);
		nextId++;
		this.eventHandler = phraseRepo;
	}
	
	/**
	 * Initalizes the "Copy" button. This is intended to copy 
	 * a the PhrasePicture associated with a cell.
	 * 
	 * @param cp5 The ControlP5 instance to add the button to.
	 */
	private void initCopyButton(ControlP5 cp5) {
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
        copyButton.getCaptionLabel().setFont(PhasesPApplet.pfont12);
        copyButton.getCaptionLabel().toUpperCase(false);
  		PhasesPApplet.colorControllerShowingLabel(copyButton);
	}
	
	/**
	 * Initializes the "Load" button. This is intended to make
	 * the PhrasePicture associated with this cell the current phrase.
	 * 
	 * @param cp5 The ControlP5 instance to add the button to.
	 */
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
		loadButton.getCaptionLabel().setFont(PhasesPApplet.pfont12);
		loadButton.getCaptionLabel().toUpperCase(false);
		PhasesPApplet.colorControllerShowingLabel(loadButton);
		
	}
	
	/**
	 * Initializes the "Generate Phrase" button. This is intended to randomly
	 * create a new phrase.
	 * 
	 * @param cp5 The ControlP5 instance to add the button to.
	 */
	private void initGenerateButton(ControlP5 cp5) {
		float width = 0.75f * rect.getWidth();
		float height = 0.25f * rect.getHeight();
		float x1 = rect.getCenx() - width/2f;
		float y1 = rect.getY2() - height - 0.2f * rect.getHeight();	
		this.generateButton = cp5.addButton("Generate Phrase " + (char)nextId)
				                 .setLabel("Generate Phrase")
				                 .setPosition(x1, y1)
				                 .setSize((int)width, (int)height)
				                 .setId(GENERATE)
				                 .plugTo(this)
				                 ;
		generateButton.getCaptionLabel().setFont(PhasesPApplet.pfont12);
		generateButton.getCaptionLabel().toUpperCase(false);
		PhasesPApplet.colorControllerShowingLabel(generateButton);
	}
	
	/**
	 * Initializes a "New Blank Phrase" button. This is intended to create a new blank phrase.
	 * 
	 * @param cp5 The ControlP5 instance to add the button to.
	 */
	private void initNewPhraseButton(ControlP5 cp5) {
		float width = 0.75f * rect.getWidth();
		float height = 0.25f * rect.getHeight();
		float x1 = rect.getCenx() - width/2f;
		float y1 = rect.getY1() + 0.2f * rect.getHeight();
		this.newPhraseButton = cp5.addButton("New Blank Phrase " + (char)nextId)
				                  .setLabel("New Blank Phrase")
				                  .setPosition(x1, y1)
				                  .setSize((int)width, (int)height)
				                  .setId(NEW)
				                  .plugTo(this)
				                  ;
		newPhraseButton.getCaptionLabel().setFont(PhasesPApplet.pfont12);
		newPhraseButton.getCaptionLabel().toUpperCase(false);
		PhasesPApplet.colorControllerShowingLabel(newPhraseButton);
	}
	
	/*******************************
	 ***** ControlP5 Callbacks *****
	 *******************************/
	
	public void controlEvent(ControlEvent e) {
		switch (e.getId()) {
			case COPY :
				eventHandler.copy(this);
				break;
			case LOAD :
				eventHandler.load(this);
				break;
			case NEW :
				eventHandler.newPhrase();
				break;
			case GENERATE :
				eventHandler.generatePhrase();
				break;
		}
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/
	
	/**
	 * Draws a border around the cell.
	 * A highlighted border, if the cell is associated with the current phrase.
	 * 
	 * @param isCurrentPhrase Whether or not the cell is associated with the current phrase.
	 * @param pa The PhasesPApplet.
	 */
	private void drawBorder(boolean isCurrentPhrase, PhasesPApplet pa) {
		pa.noFill();
		pa.strokeWeight(1);
		pa.stroke(isCurrentPhrase ? 0 : 150);
		pa.rect(rect.getX1(), rect.getY1(), rect.getWidth() - 1, rect.getHeight() - 1);
	}
	
	/**
	 * Draws the cell when there is no associated PhrasePicture.
	 * 
	 * @param showNewPhraseButtons Whether or not to show the "Generate Phrase" button and the "New Blank Phrase" buttons.
	 * @param pa The PhasesPApplet.
	 */
	protected void draw(boolean showNewPhraseButtons, PhasesPApplet pa) {
		drawBorder(false, pa);
		copyButton.hide();
		loadButton.hide();
		if (showNewPhraseButtons) {
			generateButton.show();
			newPhraseButton.show();
		}
		else {
			generateButton.hide();
			newPhraseButton.hide();
		}
	}
	
	/**
	 * Draws the cell with an associated PhrasePicture.
	 * 
	 * @param phrasePicture The PhrasePicture.
	 * @param pa The PhasesPApplet.
	 */
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
		newPhraseButton.hide();
	}
}