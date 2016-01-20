package views;

import geom.Rect;
import instrument_graphics.Instrument;
import instrument_graphics.Piano;
import phases.ModInt;
import phases.PhasesPApplet;
import phases.PhraseReader;
import processing.core.PApplet;

public class Musician extends View {
	//phrase readers:
	private PhraseReader readerA, readerB;
	
	//instruments:
	private Piano pianoA, pianoB, pianoAB;
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
	public int numOptions() {
		return 3;
	}
	
	@Override
	public void updateState() {
		initInstrumentPlayers();
		readerA.setCallee(playerA);
		readerB.setCallee(playerB);
	}
	
	public Musician(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect, opacity, pa);
		initInstruments();
		initInstrumentPlayers();
		initPhraseReaders();
	}
	
	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
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
		playerA.display(pa);
		if (colorScheme.toInt() == DIACHROMATIC) {
			pa.fill(pa.getColor2(), opacity);
		}
		else {
			pa.fill(0, opacity);
		}
		playerB.display(pa);
	}
	
	private void initInstruments() {
		initPianos(60);
		instrumentA = pianoA;
		instrumentB = pianoB;
		instrumentAB = pianoAB;
	}
	
	private void initPianos(int size) {
		pianoAB = new Piano(4, new Rect(this.getCenx(), this.getCeny(),
				            0.75f*this.getWidth(), size, PApplet.CENTER), true, pa.color(255));
		pianoA = new Piano(4, new Rect(this.getCenx(), this.getCeny() - size,
						   0.75f*this.getWidth(), size, PApplet.CENTER), true, pa.color(255));	
		pianoB = new Piano(4, new Rect(this.getCenx(), this.getCeny() + size,
						   0.75f*this.getWidth(), size, PApplet.CENTER), true, pa.color(255));
	}

	private void initInstrumentPlayers() {	
		if (superimposedOrSeparated.toInt() == SUPERIMPOSED) {
			playerA = new InstrumentPlayer(instrumentAB, pa.phrase, firstPitch);
			playerB = new InstrumentPlayer(instrumentAB, pa.phrase, firstPitch);
		}
		else {
			playerA = new InstrumentPlayer(instrumentA, pa.phrase, firstPitch);
			playerB = new InstrumentPlayer(instrumentB, pa.phrase, firstPitch);
		}
	}
	
	private void reinitInstrumentPlayers() {
		if (superimposedOrSeparated.toInt() == SUPERIMPOSED) {
			playerA = new InstrumentPlayer(instrumentAB, playerA);
			playerB = new InstrumentPlayer(instrumentAB, playerB);
		}
		else {
			playerA = new InstrumentPlayer(instrumentA, playerA);
			playerB = new InstrumentPlayer(instrumentB, playerB);
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