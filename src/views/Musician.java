package views;

import geom.Rect;
import instrument_graphics.Instrument;
import instrument_graphics.InstrumentPlayer;
import instrument_graphics.Piano;
import instrument_graphics.Marimba;
import phasing.PhasesPApplet;
import phasing.PhraseReader;
import processing.core.PApplet;
import util.ModInt;

/**
 * The Musician View type. It animates an Instrument graphic so it looks like the instrument is being played.
 * 
 * @author James Morrow
 *
 */
public class Musician extends View {
	//phrase readers:
	private PhraseReader readerA, readerB;
	
	//instruments:
	private Piano pianoA, pianoB, pianoAB;
	private Marimba marimbaA, marimbaB, marimbaAB;
	private Instrument instrumentA, instrumentB, instrumentAB;
	
	//players:
	private InstrumentPlayer playerA, playerB;
	
	//other
	private int firstPitch = 48;
	
	//options:
	public ModInt superimposedOrSeparated = new ModInt(0, numWaysOfBeingSuperimposedOrSeparated, superimposedOrSeparatedName);
	public ModInt colorScheme = new ModInt(0, numColorSchemes, colorSchemeName);
	public ModInt instrument = new ModInt(0, numInstruments, instrumentName);
	
	@Override
	public void respondToChangeInSettings() {
		assignInstruments();
		initInstrumentPlayers();
		readerA.setCallee(playerA);
		readerB.setCallee(playerB);
	}
	
	/**
	 * 
	 * @param rect The area in which to draw (usually just the entirety of the window).
	 * @param opacity The opacity of notes.
	 * @param pa The PhasesPApplet instance.
	 */
	public Musician(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect, opacity, pa);
		initInstruments();
		assignInstruments();
		initInstrumentPlayers();
		initPhraseReaders();
	}
	
	@Override
	public void wakeUp(float notept1, float notept2) {
		readerA.wakeUp(notept1);
		readerB.wakeUp(notept2);
	}
	
	@Override
	public void update(int dt, float dNotept1, float dNotept2) {
		if (pa.currentPhrase.getNumNotes() > 0) {
			readerA.update(dNotept1);
			readerB.update(dNotept2);	
			
			if (superimposedOrSeparated.toInt() == SUPERIMPOSED) {
				instrumentAB.display(pa);
			}
			else {
				instrumentA.display(pa);
				instrumentB.display(pa);
			}
			
			pa.noStroke();
			if (colorScheme.toInt() == DIACHROMATIC) {
				pa.fill(pa.getColor1(), opacity);
			}
			else {
				pa.fill(0, opacity);
			}
			playerA.draw(pa);
			if (colorScheme.toInt() == DIACHROMATIC) {
				pa.fill(pa.getColor2(), opacity);
			}
			else {
				pa.fill(0, opacity);
			}
			playerB.draw(pa);
		}
	}
	
	/**
	 * Initializes all instruments.
	 */
	private void initInstruments() {
		initPianos(0.75f*this.getWidth(), 0.075f*this.getWidth());
		initMarimbas(0.75f*this.getWidth(), 0.175f*this.getWidth());
	}
	
	/**
	 * Assigns instruments to variables accordinate to what the "instrument" option is set to.
	 */
	private void assignInstruments() {
		switch (instrument.toInt()) {
			case PIANO:
				instrumentA = pianoA;
				instrumentB = pianoB;
				instrumentAB = pianoAB;
				break;
			case XYLOPHONE:
				instrumentA = marimbaA;
				instrumentB = marimbaB;
				instrumentAB = marimbaAB;
				break;
		}
	}
	
	/**
	 * Initializes all marimbas.
	 * @param width The width for each marimba.
	 * @param height The height for each marimba.
	 */
	private void initMarimbas(float width, float height) {
		marimbaAB = new Marimba(3, new Rect(this.getCenx(), this.getCeny(), width, height, PApplet.CENTER));
		marimbaA = new Marimba(3, new Rect(this.getCenx(), this.getCeny() - height*0.66f, width, height, PApplet.CENTER));
		marimbaB = new Marimba(3, new Rect(this.getCenx(), this.getCeny() + height*0.66f, width, height, PApplet.CENTER));
	}
	
	/**
	 * Initializes all pianos.
	 * @param width The width for each piano.
	 * @param height The height for each piano.
	 */
	private void initPianos(float width, float height) {
		pianoAB = new Piano(4, new Rect(this.getCenx(), this.getCeny(), width, height, PApplet.CENTER), true, pa.color(255));
		pianoA = new Piano(4, new Rect(this.getCenx(), this.getCeny() - height, width, height, PApplet.CENTER), true, pa.color(255));	
		pianoB = new Piano(4, new Rect(this.getCenx(), this.getCeny() + height, width, height, PApplet.CENTER), true, pa.color(255));
	}

	/**
	 * Initializes the InstrumentPlayers, the things that animate the instruments.
	 */
	private void initInstrumentPlayers() {	
		if (superimposedOrSeparated.toInt() == SUPERIMPOSED) {
			playerA = new InstrumentPlayer(instrumentAB, pa.currentPhrase);
			playerB = new InstrumentPlayer(instrumentAB, pa.currentPhrase);
		}
		else {
			playerA = new InstrumentPlayer(instrumentA, pa.currentPhrase);
			playerB = new InstrumentPlayer(instrumentB, pa.currentPhrase);
		}
	}
	
	/**
	 * Initializes the PhraseReaders, the things that send events to the InstrumentPlayers whenever new notes are read.
	 */
	private void initPhraseReaders() {
		try {
			readerA = new PhraseReader(pa.currentPhrase, -1, playerA,
					                   InstrumentPlayer.class.getMethod("setActiveKey", PhraseReader.class));
			readerB = new PhraseReader(pa.currentPhrase, -1, playerB,
					                   InstrumentPlayer.class.getMethod("setActiveKey", PhraseReader.class));

		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
}