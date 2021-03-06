package screens;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlp5.Textfield2;
import geom.Rect;
import phasing.PhasesPApplet;
import phasing.PhrasePicture;

/**
 * 
 * @author James Morrow
 *
 */
class Cell {
    // class-scope
    private static int nextId = (int) 'a';
    private final static int TITLE = -1, GENERATE = -2, COPY = -3, NEW = -4, DELETE = -5;

    // outside world
    private CellEventHandler eventHandler;

    // dimensions
    private Rect rect;

    // buttons
    private Button copyButton;
    private Button deleteButton;
    private Button generateButton;
    private Button newPhraseButton;
    private Textfield2 titleForm;

    /**************************
     ***** Initialization *****
     **************************/

    /**
     * 
     * @param rect
     *            The area in which to draw the cell.
     * @param cp5
     *            The ControlP5 instance to which to add controllers.
     * @param phraseRepo
     *            The object to send events.
     */
    protected Cell(Rect rect, ControlP5 cp5, PhraseRepository phraseRepo) {
        this.rect = rect;
        initTitleForm(cp5);
        initCopyButton(cp5);
        initGenerateButton(cp5);
        initNewPhraseButton(cp5);
        initDeleteButton(cp5);
        nextId++;
        this.eventHandler = phraseRepo;
    }

    /**
     * Initializes the form used to edit the title of the phrase picture.
     * 
     * @param cp5
     *            The ControlP5 instance to add the button to.
     * @parm name The name of the title
     */
    private void initTitleForm(ControlP5 cp5) {
        int width = (int) rect.getWidth() - 2;
        int height = (int) (rect.getHeight() * 0.1f);

        titleForm = new Textfield2(cp5, (char) nextId + " title").setPosition(rect.getX1() + 1, rect.getY1())
                .setSize(width, height).setId(TITLE).setAutoClear(false).plugTo(this);

        titleForm.setColorBackground(0xffffffff);
        titleForm.setColorActive(PhasesPApplet.getColor1Bold());
        titleForm.setColorValue(0);
        titleForm.setColorCursor(PhasesPApplet.getColor1Bold());
        titleForm.getValueLabel().setFont(PhasesPApplet.pfont12);
        titleForm.getCaptionLabel().setVisible(false);
    }

    /**
     * Initalizes the "Copy" button. This is intended to copy a the
     * PhrasePicture associated with a cell.
     * 
     * @param cp5
     *            The ControlP5 instance to add the button to.
     */
    private void initCopyButton(ControlP5 cp5) {
        float x1 = rect.getX1();
        float y1 = rect.getY1() + 0.9f * rect.getHeight();
        float width = 0.4f * rect.getWidth();
        float height = 0.1f * rect.getHeight();
        this.copyButton = cp5.addButton((char) nextId + " copy").setLabel("Copy").setPosition(x1, y1)
                .setSize((int) width, (int) height).setId(COPY).plugTo(this);
        ;
        copyButton.getCaptionLabel().setFont(PhasesPApplet.pfont12);
        copyButton.getCaptionLabel().toUpperCase(false);
        PhasesPApplet.colorControllerShowingLabel(copyButton);
    }

    /**
     * Initializes a "Delete" button. This is intended to delete a phrase
     * picture.
     * 
     * @param cp5
     *            The ControlP5 instance to add the button to.
     */
    private void initDeleteButton(ControlP5 cp5) {
        float width = 0.4f * rect.getWidth();
        float height = 0.1f * rect.getHeight();
        float x1 = rect.getX2() - width - 1;
        float y1 = rect.getY1() + 0.9f * rect.getHeight();
        this.deleteButton = cp5.addButton("Delete " + (char) nextId).setLabel("Delete").setPosition(x1, y1)
                .setSize((int) width, (int) height).setId(DELETE).plugTo(this);
        deleteButton.getCaptionLabel().setFont(PhasesPApplet.pfont12);
        deleteButton.getCaptionLabel().toUpperCase(false);
        PhasesPApplet.colorControllerShowingLabel(deleteButton);
    }

    /**
     * Initializes the "Generate Phrase" button. This is intended to randomly
     * create a new phrase.
     * 
     * @param cp5
     *            The ControlP5 instance to add the button to.
     */
    private void initGenerateButton(ControlP5 cp5) {
        float width = 0.75f * rect.getWidth();
        float height = 0.25f * rect.getHeight();
        float x1 = rect.getCenx() - width / 2f;
        float y1 = rect.getY2() - height - 0.2f * rect.getHeight();
        this.generateButton = cp5.addButton("Generate Phrase " + (char) nextId).setLabel("Generate Phrase")
                .setPosition((int) x1, (int) y1).setSize((int) width, (int) height).setId(GENERATE).plugTo(this);
        generateButton.getCaptionLabel().setFont(PhasesPApplet.pfont12);
        generateButton.getCaptionLabel().toUpperCase(false);
        PhasesPApplet.colorControllerShowingLabel(generateButton);
    }

    /**
     * Initializes a "New Blank Phrase" button. This is intended to create a new
     * blank phrase.
     * 
     * @param cp5
     *            The ControlP5 instance to add the button to.
     */
    private void initNewPhraseButton(ControlP5 cp5) {
        float width = 0.75f * rect.getWidth();
        float height = 0.25f * rect.getHeight();
        float x1 = rect.getCenx() - width / 2f;
        float y1 = rect.getY1() + 0.2f * rect.getHeight();
        this.newPhraseButton = cp5.addButton("New Blank Phrase " + (char) nextId).setLabel("New Blank Phrase")
                .setPosition(x1, y1).setSize((int) width, (int) height).setId(NEW).plugTo(this);
        newPhraseButton.getCaptionLabel().setFont(PhasesPApplet.pfont12);
        newPhraseButton.getCaptionLabel().toUpperCase(false);
        PhasesPApplet.colorControllerShowingLabel(newPhraseButton);
    }

    /*******************************
     ***** ControlP5 Callbacks *****
     *******************************/

    /**
     * Handles events sent from ControlP5.
     * 
     * @param e
     */
    public void controlEvent(ControlEvent e) {
        switch (e.getId()) {
        case TITLE:
            System.out.println(e);
            break;
        case COPY:
            eventHandler.copy(this);
            break;
        case NEW:
            eventHandler.newPhrase();
            break;
        case GENERATE:
            eventHandler.generatePhrase();
            break;
        case DELETE:
            eventHandler.delete(this);
            break;
        }
    }

    /*******************
     ***** Drawing *****
     *******************/

    /**
     * Draws the background of the cell. Draws this differently depending on
     * whether this cell is selected or not.
     * 
     * @param selected
     *            Whether or not the cell is selected.
     * @param pa
     *            The PhasesPApplet.
     */
    private void drawRect(boolean selected, PhasesPApplet pa) {
        pa.noFill();
        if (selected)
            pa.strokeWeight(2);
        else
            pa.strokeWeight(1);
        pa.stroke(selected ? pa.getColor1Bold() : 150);
        pa.rectMode(pa.CORNER);
        pa.rect(rect.getX1(), rect.getY1(), rect.getWidth() - 1, rect.getHeight() - 1);
    }

    /**
     * Draws the cell when there is no associated PhrasePicture.
     * 
     * @param showNewPhraseButtons
     *            Whether or not to show the "Generate Phrase" button and the
     *            "New Blank Phrase" buttons.
     * @param pa
     *            The PhasesPApplet.
     */
    protected void draw(boolean showNewPhraseButtons, PhasesPApplet pa) {
        drawRect(false, pa);
        deleteButton.hide();
        copyButton.hide();
        titleForm.hide();
        if (showNewPhraseButtons) {
            generateButton.show();
            newPhraseButton.show();
        } else {
            generateButton.hide();
            newPhraseButton.hide();
        }
    }

    /**
     * Draws the cell with an associated PhrasePicture.
     * 
     * @param phrasePicture
     *            The PhrasePicture.
     * @param pa
     *            The PhasesPApplet.
     */
    protected void draw(PhrasePicture phrasePicture, PhasesPApplet pa) {
        boolean isCurrentPhrase = phrasePicture == pa.currentPhrasePicture;
        drawRect(isCurrentPhrase, pa);

        phrasePicture.draw(rect, pa);

        if (isCurrentPhrase) {
            titleForm.show();
            titleForm.setFocus(true);
        } else {
            titleForm.setFocus(false);
            titleForm.hide();
            String name = phrasePicture.getName();
            pa.fill(0);
            pa.textAlign(pa.CENTER, pa.TOP);
            pa.textSize(12);
            pa.rectMode(pa.CORNER);
            pa.text(name, rect.getX1(), rect.getY1(), rect.getWidth(), 0.1f * rect.getHeight());
        }

        deleteButton.show();
        copyButton.show();
        generateButton.hide();
        newPhraseButton.hide();
    }

    /****************
     ***** Misc *****
     ****************/

    public void dispose(ControlP5 cp5) {
        removeControllers(cp5);
        rect = null;
        eventHandler = null;
        titleForm.nullifyBuffer();
    }

    /**
     * Sets the title that is displayed on the text form.
     * 
     * @param title
     */
    protected void setTitle(String title) {
        titleForm.setValue(title);
    }

    /**
     * Gives the title that is displayed on the text form.
     * 
     * @return The title from the text form.
     */
    protected String getTitle() {
        return titleForm.getText();
    }

    /**
     * Removes the controllers this Cell generated from the given ControlP5
     * object.
     */
    protected void removeControllers(ControlP5 cp5) {
        if (cp5 != null) {
            cp5.remove(copyButton.getName());
            cp5.remove(deleteButton.getName());
            cp5.remove(generateButton.getName());
            cp5.remove(newPhraseButton.getName());
            cp5.remove(titleForm.getName());
            titleForm.nullifyBuffer();
        }
    }

    /**
     * 
     * @param x
     * @param y
     * @return True if the (x,y) is inside the cell, false otherwise.
     */
    protected boolean touches(int x, int y) {
        return rect.touches(x, y);
    }
}