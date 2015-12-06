package phases;

public abstract class View extends Rect {
	protected Phrase phrase;
	
	public View(Rect rect, Phrase phrase) {
		super(rect);
		this.phrase = phrase;
	}
	
	public abstract void update(float dBeatpt1, float dBeatpt2);
}
