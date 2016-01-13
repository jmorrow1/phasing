package views;

import geom.Rect;
import phases.Instrument;
import phases.PhasesPApplet;
import phases.PhraseReader;
import phases.Piano;
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
	private final int SUPERIMPOSED=0, SEPARATED=1;
	private int superimposedOrSeparated = SUPERIMPOSED;
	
	private final int MONOCHROME=0, DIACHROME=1;
	private int colorScheme = MONOCHROME;
	
	private final int numInstruments = 1;
	private final int PIANO=0;
	private int instrument = PIANO;
	
	@Override
	public int numOptions() {
		return 3;
	}
	
	@Override
	public int getValue(int index) {
		switch(index) {
			case 0: return superimposedOrSeparated;
			case 1: return colorScheme;
			case 2: return instrument;
			default: return -1;
		}
	}
	
	@Override
	public int numValues(int index) {
		switch(index) {
			case 0: return 2;
			case 1: return 2;
			case 2: return numInstruments;
			default: return -1;
		}
	}
	
	private void updateState() {
		initInstrumentPlayers();
		readerA.setCallee(playerA);
		readerB.setCallee(playerB);
	}

	@Override
	public String showOption(int index) {
		String s = "";
		switch (index) {
			case 0: 
				return "superimposed or separated? " 
						+ ((superimposedOrSeparated == SUPERIMPOSED) ? "SUPERIMPOSED" : "SEPARATED");
			case 1:
				return "color scheme type: " + ((colorScheme == MONOCHROME) ? "MONOCHROME" : "DIACHROME");
			case 2:
				s += "instrument: ";
				switch (instrument) {
					case PIANO: s += "PIANO"; break;
					default: s += instrument; break;
				}
				return s;
			default: return s;
		}
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
		
		if (superimposedOrSeparated == SUPERIMPOSED) {
			instrumentAB.display(pa);
		}
		else {
			instrumentA.display(pa);
			instrumentB.display(pa);
		}
		
		pa.noStroke();
		if (colorScheme == DIACHROME) {
			pa.fill(pa.getColor1(), opacity);
		}
		else {
			pa.fill(0, opacity);
		}
		playerA.display(pa);
		if (colorScheme == DIACHROME) {
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
		if (superimposedOrSeparated == SUPERIMPOSED) {
			playerA = new InstrumentPlayer(instrumentAB, pa.phrase, firstPitch);
			playerB = new InstrumentPlayer(instrumentAB, pa.phrase, firstPitch);
		}
		else {
			playerA = new InstrumentPlayer(instrumentA, pa.phrase, firstPitch);
			playerB = new InstrumentPlayer(instrumentB, pa.phrase, firstPitch);
		}
	}
	
	private void reinitInstrumentPlayers() {
		if (superimposedOrSeparated == SUPERIMPOSED) {
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