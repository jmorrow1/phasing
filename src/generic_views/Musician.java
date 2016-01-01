package generic_views;

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
	private final int SUPERIMPOSED=0, SEPARATE=1;
	private int superimposedOrSeparated = SUPERIMPOSED;
	
	private final int MONOCHROME=0, DIACHROME=1;
	private int colorSchemeType = MONOCHROME;
	
	private final int PIANO=0;
	private int instrument = PIANO;
	
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
		if (colorSchemeType == DIACHROME) {
			pa.fill(pa.getColor1(), opacity);
		}
		else {
			pa.fill(0, opacity);
		}
		playerA.display(pa);
		if (colorSchemeType == DIACHROME) {
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
		if (superimposedOrSeparated == SUPERIMPOSED) {
			pianoAB = new Piano(4, new Rect(this.getCenx(), this.getCeny(),
					            0.75f*this.getWidth(), size, PApplet.CENTER), true, false, pa.color(255));
		}
		else {
			pianoA = new Piano(4, new Rect(this.getCenx(), this.getCeny() - size,
							   0.75f*this.getWidth(), size, PApplet.CENTER), true, false, pa.color(255));
			
			pianoB = new Piano(4, new Rect(this.getCenx(), this.getCeny() + size,
							   0.75f*this.getWidth(), size, PApplet.CENTER), true, false, pa.color(255));
		}
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
