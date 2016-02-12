package views;

import geom.Rect;
import instrument_graphics.Instrument;
import instrument_graphics.InstrumentPlayer;
import instrument_graphics.Piano;
import instrument_graphics.Xylophone;
import phases.ModInt;
import phases.PhasesPApplet;
import phases.PhraseReader;
import processing.core.PApplet;

public class Musician extends View {
	//phrase readers:
	private PhraseReader readerA, readerB;
	
	//instruments:
	private Piano pianoA, pianoB, pianoAB;
	private Xylophone xylophoneA, xylophoneB, xylophoneAB;
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
	public void update(float dNotept1, float dNotept2) {
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
	
	private void initInstruments() {
		initPianos(0.75f*this.getWidth(), 60);
		initXylophones(0.75f*this.getWidth(), 150);
	}
	
	private void assignInstruments() {
		switch (instrument.toInt()) {
			case PIANO:
				instrumentA = pianoA;
				instrumentB = pianoB;
				instrumentAB = pianoAB;
				break;
			case XYLOPHONE:
				instrumentA = xylophoneA;
				instrumentB = xylophoneB;
				instrumentAB = xylophoneAB;
				break;
		}
	}
	
	private void initXylophones(float width, float height) {
		xylophoneAB = new Xylophone(3, new Rect(this.getCenx(), this.getCeny(), width, height, PApplet.CENTER));
		xylophoneA = new Xylophone(3, new Rect(this.getCenx(), this.getCeny() - height, width, height, PApplet.CENTER));
		xylophoneB = new Xylophone(3, new Rect(this.getCenx(), this.getCeny() + height, width, height, PApplet.CENTER));
	}
	
	private void initPianos(float width, float height) {
		pianoAB = new Piano(4, new Rect(this.getCenx(), this.getCeny(), width, height, PApplet.CENTER), true, pa.color(255));
		pianoA = new Piano(4, new Rect(this.getCenx(), this.getCeny() - height, width, height, PApplet.CENTER), true, pa.color(255));	
		pianoB = new Piano(4, new Rect(this.getCenx(), this.getCeny() + height, width, height, PApplet.CENTER), true, pa.color(255));
	}

	private void initInstrumentPlayers() {	
		if (superimposedOrSeparated.toInt() == SUPERIMPOSED) {
			playerA = new InstrumentPlayer(instrumentAB, pa.phrase);
			playerB = new InstrumentPlayer(instrumentAB, pa.phrase);
		}
		else {
			playerA = new InstrumentPlayer(instrumentA, pa.phrase);
			playerB = new InstrumentPlayer(instrumentB, pa.phrase);
		}
	}
	
	private void initPhraseReaders() {
		try {
			readerA = new PhraseReader(pa.phrase, -1, playerA,
					                   InstrumentPlayer.class.getMethod("setActiveKey", PhraseReader.class));
			readerB = new PhraseReader(pa.phrase, -1, playerB,
					                   InstrumentPlayer.class.getMethod("setActiveKey", PhraseReader.class));

		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
}