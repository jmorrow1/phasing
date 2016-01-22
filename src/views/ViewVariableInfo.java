package views;

public interface ViewVariableInfo {
	//possible values of view variables
	public final String transformationName = "Transformation";
	public final int numTransformations = 2;
	public final int TRANSLATE=0, ROTATE=1;
	
	public final String cameraModeName = "Camera Mode";
	public final int numCameraModes = 3;
	public final int RELATIVE_TO_1=0, RELATIVE_TO_2=1, FIXED=2;
	
	public final String noteGraphicName = "Note Graphic";
	public final int numNoteGraphics=5;
	public final int SYMBOLS=0, DOTS=1, CONNECTED_DOTS=2, RECTS_OR_SECTORS=3, SINE_WAVE=4;
	
	public final String colorSchemeName = "Color Scheme";
	public final int numColorSchemes=2;
	public final int MONOCHROMATIC=0, DIACHROMATIC=1;
	
	public final String superimposedOrSeparatedName = "Superimposed or Separated";
	public final int numWaysOfBeingSuperimposedOrSeparated=2;
	public final int SUPERIMPOSED=0, SEPARATED=1;
	
	public final String instrumentName = "Instrument";
	public final int numInstruments=1;
	public final int PIANO=0;
	
	public final String scoreModeName = "Score Mode";
	public final int numScoreModes=2;
	public final int SCROLLS=0, FADES=1;
	
	public final String sineWaveName = "Sine Wave";
	public final int numWaysOfBeingASineWaveOrNot=2;
	public final int IS_SINE_WAVE=0, IS_NOT_SINE_WAVE=1;
	
	public final String activeNoteModeName = "Active Note Mode";
	public final int numActiveNoteModes = 3;
	public final int ONLY_SHOW_ACTIVE_NOTE=0, SHOW_ACTIVE_NOTE=1, DONT_SHOW_ACTIVE_NOTE=2;
	
	public final String plotPitchModeName = "Plot Pitch Mode";
	public final int numWaysOfPlottingPitchOrNot = 2;
	public final int PLOT_PITCH=0, DONT_PLOT_PITCH=1;
}