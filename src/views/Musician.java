package views;

import geom.Rect;
import instrument_graphics.Instrument;
import instrument_graphics.InstrumentPlayer;
import instrument_graphics.Marimba;
import instrument_graphics.Piano;
import phasing.PhasesPApplet;
import phasing.PhraseReader;
import phasing.PlayerInfo;
import processing.core.PApplet;
import screens.Presenter;
import util.ModInt;

/**
 * The Musician View type. It animates an Instrument graphic so it looks like
 * the instrument is being played.
 * 
 * @author James Morrow
 *
 */
public class Musician extends View {
    // phrase readers:
    // private PhraseReader readerA, readerB;

    // instruments:
    private Piano pianoA, pianoB, pianoAB;
    private Marimba marimbaA, marimbaB, marimbaAB;
    private Instrument instrumentA, instrumentB, instrumentAB;

    // players:
    private InstrumentPlayer playerA, playerB;

    // options:
    public ModInt superimposedOrSeparated = new ModInt(0, numWaysOfBeingSuperimposedOrSeparated,
            superimposedOrSeparatedName);
    public ModInt colorScheme = new ModInt(0, numColorSchemes, colorSchemeName);
    public ModInt instrument = new ModInt(0, numInstruments, instrumentName);

    // style:
    private float strokeWeight;

    /**************************
     ***** Initialization *****
     **************************/

    /**
     * 
     * @param viewBox
     *            The area in which to draw.
     * @param opacity
     *            The opacity of notes.
     * @param playerInfo
     *            Contains information (potentially) about how to initialize the
     *            view's settings.
     * @param pa
     *            The PhasesPApplet instance.
     */
    public Musician(Rect viewBox, int opacity, PlayerInfo playerInfo, PhasesPApplet pa) {
        super(viewBox, opacity, pa);
        init();
        loadSettings(playerInfo);
    }

    /**
     * Constructs a Musician whose option values are taken from the another
     * Musician.
     * 
     * @param m
     *            The Musician this one derives its option values from.
     * @param viewBox
     *            The area in which to draw.
     * @param opacity
     *            The opacity of notes.
     * @param pa
     *            The PhasesPApplet instance.
     */
    public Musician(Musician m, Rect viewBox, int opacity, PhasesPApplet pa) {
        super(viewBox, opacity, pa);
        copyOptionValues(m);
        init();
    }

    /**
     * Copies the given Musician object's option values into this Musician
     * object's option variables.
     * 
     * @param m
     *            The given Musician.
     */
    private void copyOptionValues(Musician m) {
        this.superimposedOrSeparated.setValue(m.superimposedOrSeparated.toInt());
        this.colorScheme.setValue(m.colorScheme.toInt());
        this.instrument.setValue(m.instrument.toInt());
    }

    /**
     * Initializes the Musician.
     */
    private void init() {
        initInstruments();
        assignInstruments();
        initInstrumentPlayers();
        setStrokeWeight();
    }

    /**
     * Initializes all instruments.
     */
    private void initInstruments() {
        initPianos(0.75f * this.getWidth(), 0.075f * this.getWidth());
        initMarimbas(0.75f * this.getWidth(), 0.175f * this.getWidth());
    }

    /**
     * Resets all instruments according to the current width and height of this
     * Musician.
     */
    private void resetInstruments() {
        resetPianos(0.75f * this.getWidth(), 0.075f * this.getWidth());
        resetMarimbas(0.75f * this.getWidth(), 0.175f * this.getWidth());
    }

    /**
     * Initializes all marimbas.
     * 
     * @param width
     *            The width for each marimba.
     * @param height
     *            The height for each marimba.
     */
    private void initMarimbas(float width, float height) {
        marimbaAB = new Marimba(3, new Rect(this.getCenx(), this.getCeny(), width, height, PApplet.CENTER));
        marimbaA = new Marimba(3,
                new Rect(this.getCenx(), this.getCeny() - height * 0.66f, width, height, PApplet.CENTER));
        marimbaB = new Marimba(3,
                new Rect(this.getCenx(), this.getCeny() + height * 0.66f, width, height, PApplet.CENTER));
    }

    /**
     * Resets the widths and heights of each marimba.
     * 
     * @param width
     *            The width for each marimba.
     * @param height
     *            The height for each marimba.
     */
    private void resetMarimbas(float width, float height) {
        marimbaAB.init(3, new Rect(this.getCenx(), this.getCeny(), width, height, PApplet.CENTER));
        marimbaA.init(3, new Rect(this.getCenx(), this.getCeny() - height * 0.66f, width, height, PApplet.CENTER));
        marimbaB.init(3, new Rect(this.getCenx(), this.getCeny() + height * 0.66f, width, height, PApplet.CENTER));
    }

    /**
     * Initializes all pianos.
     * 
     * @param width
     *            The width for each piano.
     * @param height
     *            The height for each piano.
     */
    private void initPianos(float width, float height) {
        pianoAB = new Piano(4, new Rect(this.getCenx(), this.getCeny(), width, height, PApplet.CENTER), true,
                pa.color(255));
        pianoA = new Piano(4, new Rect(this.getCenx(), this.getCeny() - height, width, height, PApplet.CENTER), true,
                pa.color(255));
        pianoB = new Piano(4, new Rect(this.getCenx(), this.getCeny() + height, width, height, PApplet.CENTER), true,
                pa.color(255));
    }

    /**
     * Resets the widths and heights of each piano.
     * 
     * @param width
     *            The width for each piano.
     * @param height
     *            The height for each piano.
     */
    private void resetPianos(float width, float height) {
        float cenx = getCenx();
        float ceny = getCeny();
        pianoA.setCenter(cenx, ceny - height);
        pianoB.setCenter(cenx, ceny + height);
        pianoAB.setCenter(cenx, ceny);

        pianoA.setSize(width, height);
        pianoB.setSize(width, height);
        pianoAB.setSize(width, height);
    }

    /**
     * Initializes the InstrumentPlayers, the things that animate the
     * instruments.
     */
    private void initInstrumentPlayers() {
        if (superimposedOrSeparated.toInt() == SUPERIMPOSED) {
            playerA = new InstrumentPlayer(instrumentAB, pa.currentPhrase);
            playerB = new InstrumentPlayer(instrumentAB, pa.currentPhrase);
        } else {
            playerA = new InstrumentPlayer(instrumentA, pa.currentPhrase);
            playerB = new InstrumentPlayer(instrumentB, pa.currentPhrase);
        }
    }

    /**
     * Resets the InstrumentPlayers according to the current values of
     * InstrumentA, InstrumentB, and InstrumentAB.
     */
    private void resetInstrumentPlayers() {
        if (superimposedOrSeparated.toInt() == SUPERIMPOSED) {
            playerA.setInstrument(instrumentAB);
            playerB.setInstrument(instrumentAB);
        } else {
            playerA.setInstrument(instrumentA);
            playerB.setInstrument(instrumentB);
        }
    }

    /**
     * Assigns instruments to variables accordinate to what the "instrument"
     * option is set to.
     */
    private void assignInstruments() {
        switch (instrument.toInt()) {
        case PIANO:
            instrumentA = pianoA;
            instrumentB = pianoB;
            instrumentAB = pianoAB;
            break;
        case MARIMBA:
            instrumentA = marimbaA;
            instrumentB = marimbaB;
            instrumentAB = marimbaAB;
            break;
        }
    }

    /**************************
     ***** Event Handling *****
     **************************/

    @Override
    protected void resized(float prevWidth, float prevHeight) {
        resetInstruments();
        resetInstrumentPlayers();
        setStrokeWeight();
    }

    @Override
    public void settingsChanged() {
        assignInstruments();
        resetInstrumentPlayers();
        setStrokeWeight();
    }

    @Override
    public void wakeUp(float notept1, float notept2) {
    }

    @Override
    public void noteEvent(PhraseReader reader) {
        if (reader.getId() == Presenter.READER_ONE_ID) {
            playerA.setActiveKey(reader);
        } else if (reader.getId() == Presenter.READER_TWO_ID) {
            playerB.setActiveKey(reader);
        }
    }

    /******************
     ***** Update *****
     ******************/

    @Override
    public void update(int dt, float dNotept1, float dNotept2) {
        if (pa.currentPhrase.getNumNotes() > 0) {
            pa.strokeWeight(strokeWeight);
            if (superimposedOrSeparated.toInt() == SUPERIMPOSED) {
                instrumentAB.display(pa);
            } else {
                instrumentA.display(pa);
                instrumentB.display(pa);
            }

            pa.noStroke();
            if (colorScheme.toInt() == DIACHROMATIC) {
                pa.fill(pa.getColor1(), opacity);
            } else {
                pa.fill(0, opacity);
            }
            playerA.draw(pa);
            if (colorScheme.toInt() == DIACHROMATIC) {
                pa.fill(pa.getColor2(), opacity);
            } else {
                pa.fill(0, opacity);
            }
            playerB.draw(pa);
        }
    }

    /**
     * Sets the stroke weight with which instruments are drawn based on the size
     * of the view area.
     */
    private void setStrokeWeight() {
        strokeWeight = PApplet.map(this.getArea(), 30000, 300000, 1, 1.5f);
    }

    /***************************************
     ***** Saving and Loading Settings *****
     ***************************************/

    @Override
    public void saveSettings(PlayerInfo playerInfo) {
        save(superimposedOrSeparated, "superimposedOrSeparated", playerInfo);
        save(colorScheme, "colorScheme", playerInfo);
        save(instrument, "instrument", playerInfo);
    }

    @Override
    protected void loadSettings(PlayerInfo playerInfo) {
        tryToSet(superimposedOrSeparated, "superimposedOrSeparated", playerInfo);
        tryToSet(colorScheme, "colorScheme", playerInfo);
        tryToSet(instrument, "instrument", playerInfo);
        settingsChanged();
    }
}