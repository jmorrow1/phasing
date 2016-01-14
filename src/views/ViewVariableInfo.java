package views;

public interface ViewVariableInfo {
	//possible values of view variables
	public final int numTransformations = 2;
	public final int TRANSLATE=0, ROTATE=1;
	
	public final int numCameraModes = 2;
	public final int RELATIVE=0, FIXED=1;
	
	public final int numNoteGraphics=5;
	public final int SYMBOLS=0, DOTS=1, CONNECTED_DOTS=2, RECTS_OR_SECTORS=3, SINE_WAVE=4;
	
	public final int numColorSchemes=2;
	public final int MONOCHROMATIC=0, DIACHROMATIC=1;
	
	public final int numWaysOfBeingSuperimposedOrSeparated=2;
	public final int SUPERIMPOSED=0, SEPARATED=1;
	
	public final int numInstruments=1;
	public final int PIANO=0;
	
	public final int numScoreModes=2;
	public final int SCROLLS=0, FADES=1;
}