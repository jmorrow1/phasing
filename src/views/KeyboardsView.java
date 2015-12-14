package views;

import geom.Rect;
import phases.Phrase;
import phases.PhraseReader;
import phases.Piano;
import processing.core.PApplet;

public class KeyboardsView extends View {
	//music
	private PhraseReader readerA, readerB;
	//piano
	private boolean superimposeKeyboards;
	private Piano keyboardA, keyboardB;
	private Piano keyboardAB;
	private final int PIANO_SIZE = 50;
	//piano players
	private PianoPlayer pianoPlayerA, pianoPlayerB;
	//conversion
	private int firstPitchOfPiano;
	
	protected void init() {
		firstPitchOfPiano = 60;
		initPianos();
		initPianoPlayers();
		initPhraseReaders();
	}
	
	public KeyboardsView(Rect rect, Phrase phrase, int color1, int color2, int opacity, PApplet pa) {
		super(rect, phrase, color1, color2, opacity, 0, pa);
	}
	
	private void initPianos() {
		if (superimposeKeyboards) {
			keyboardAB = new Piano(2, new Rect(this.getCenx(), this.getCeny(),
					0.75f*this.getWidth(), PIANO_SIZE, PApplet.CENTER), true, pa.color(255));
		}
		else {
			keyboardA = new Piano(2, new Rect(this.getCenx(), this.getCeny() - PIANO_SIZE,
							0.75f*this.getWidth(), PIANO_SIZE, PApplet.CENTER), true, pa.color(255));
			
			keyboardB = new Piano(2, new Rect(this.getCenx(), this.getCeny() + PIANO_SIZE,
							0.75f*this.getWidth(), PIANO_SIZE, PApplet.CENTER), true, pa.color(255));
		}
	}
	
	private void initPianoPlayers() {
		if (superimposeKeyboards) {
			pianoPlayerA = new PianoPlayer(color1, keyboardAB, phrase);
			pianoPlayerB = new PianoPlayer(color2, keyboardAB, phrase);
		}
		else {
			pianoPlayerA = new PianoPlayer(color1, keyboardA, phrase);
			pianoPlayerB = new PianoPlayer(color2, keyboardB, phrase);
		}
	}
	
	private void initPhraseReaders() {
		//init phrase readers
		try {
			readerA = new PhraseReader(phrase, -1, pianoPlayerA,
					PianoPlayer.class.getMethod("setActiveKey", PhraseReader.class));
			readerB = new PhraseReader(phrase, -1, pianoPlayerB,
					PianoPlayer.class.getMethod("setActiveKey", PhraseReader.class));

		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
		readerA.update(dNotept1);
		readerB.update(dNotept2);
		
		if (superimposeKeyboards) {
			keyboardAB.drawWhiteKeys(pa);
			pianoPlayerA.displayIfWhite(pa);
			pianoPlayerB.displayIfWhite(pa);
			keyboardAB.drawBlackKeys(pa);
			pianoPlayerA.displayIfBlack(pa);
			pianoPlayerB.displayIfBlack(pa);
		}
		else {
			keyboardA.drawWhiteKeys(pa);
			pianoPlayerA.displayIfWhite(pa);
			keyboardA.drawBlackKeys(pa);
			pianoPlayerA.displayIfBlack(pa);

			keyboardB.drawWhiteKeys(pa);
			pianoPlayerB.displayIfWhite(pa);
			keyboardB.drawBlackKeys(pa);
			pianoPlayerB.displayIfBlack(pa);
		}
	}
	
	public class PianoPlayer {
		private Phrase phrase;
		private Rect[] keyCopies;
		private boolean keyIsWhite;
		private Rect activeKey;
		private int color;
		
		private PianoPlayer(int color, Piano piano, Phrase phrase) {
			this.color = color;
			keyCopies = new Rect[phrase.getNumNotes()];
			for (int i=0; i<keyCopies.length; i++) {
				keyCopies[i] = piano.getKeyCopy(phrase.getPitch(i) - firstPitchOfPiano);
			}
			
			this.phrase = phrase;
		}
		
		public void displayIfWhite(PApplet pa) {
			if (keyIsWhite) {
				pa.fill(color, opacity);
				activeKey.display(pa);
			}
		}
		
		public void displayIfBlack(PApplet pa) {
			if (!keyIsWhite) {
				pa.fill(color, opacity);
				activeKey.display(pa);
			}
		}
		
		//callback:
		public void setActiveKey(PhraseReader reader) {
			activeKey = keyCopies[reader.getNoteIndex()];
			keyIsWhite = Piano.isWhiteKey(phrase.getPitch(reader.getNoteIndex()));
		}
	}
	
	/*Settings*/
	
	private void superimposeKeyboards(boolean superimposeKeyboards) {
		this.superimposeKeyboards = superimposeKeyboards;
		initPianos();
		initPianoPlayers();
		initPhraseReaders();
	}
	
	public void loadPreset(int preset) {
		switch (preset) {
			case 0 :
				superimposeKeyboards(true);
				break;
			case 1 :
				superimposeKeyboards(false);
				break;
		}
	}
	
	public int numPresets() {
		return 2;
	}
}