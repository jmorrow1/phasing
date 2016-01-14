package phases;

public class Option {
	public final String optionType;
	public final int numValues;
	private final String[] valueNames;
	public final static Option instance = new Option("", new String[] {});
	
	public Option(String optionType, String[] valueNames) {
		this.optionType = optionType;
		this.numValues = valueNames.length;
		this.valueNames = valueNames;
	}
	
	public static Camera camera() {return instance.new Camera();}
	
	public class Camera extends Option {
		public static final int FIXED=0, RELATIVE_TO_1=1, RELATIVE_TO_2=2;
		public Camera() {
			super("Camera Icon", new String[] {"FIXED", "RELATIVE TO 1", "RELATIVE TO 2"});
		}
	}
	
	public static ColorScheme colorScheme() {return instance.new ColorScheme();}
	
	public class ColorScheme extends Option {
		public static final int MONOCHROME=0, DIACHROME=1;
		public ColorScheme() {
			super("Color Scheme", new String[] {"MONOCHROME", "DIACHROME"});
		}
	}
	
	public static Instrument instrument() {return instance.new Instrument();}
	
	public class Instrument extends Option {
		public static final int PIANO=0;
		public Instrument() {
			super("Instrument", new String[] {"PIANO"});
		}
	}
	
	public static NoteGraphic noteGraphic() {return instance.new NoteGraphic();}
	
	public class NoteGraphic extends Option {
		public static final int SYMBOLS=0, DOTS=1, CONNECTED_DOTS=2, RECTS_OR_SECTORS=3;
		public NoteGraphic() {
			super("Note Graphic", new String[] {"SYMBOLS", "DOTS", "CONNECTED DOTS", "RECTS"});
		}
	}
	
	public static PlotPitch plotPitch() {return instance.new PlotPitch();}
	
	public class PlotPitch extends Option {
		public static final int PLOT_PITCH=0, DONT_PLOT_PITCH=1;
		public PlotPitch() {
			super("Plot Pitch", new String[] {"PLOT PITCH", "DON'T PLOT PITCH"});
		}
	}
	
	public static ActiveNote activeNote() {return instance.new ActiveNote();}
	
	public class ActiveNote extends Option {
		public static final int SHOW_ACTIVE_NOTE=0, ONLY_SHOW_ACTIVE_NOTE=1, DONT_SHOW_ACTIVE_NOTE=2, SHOW_LINE_AT_ACTIVE_NOTE=3;
		public ActiveNote() {
			super("Active Note", new String[] {"SHOW ACTIVE NOTE", "ONLY SHOW ACTIVE NOTE", "DON'T SHOW ACTIVE NOTE", "SHOW LINE AT ACTIVE NOTE"});
		}
	}
	
	public static PianoAxis pianoAxis() {return instance.new PianoAxis();}
	
	public class PianoAxis extends Option {
		public static final int SHOW_PIANO=0, DONT_SHOW_PIANO=1;
		public PianoAxis() {
			super("Piano Axis", new String[] {"SHOW PIANO", "DON'T SHOW PIANO"});
		}
	}
	
	public static Superimpose superimpose() {return instance.new Superimpose();}
	
	public class Superimpose extends Option {
		public static final int SUPERIMPOSED=0, SEPARATED=1;
		public Superimpose() {
			super("Superimpose", new String[] {"SUPERIMPOSED", "SEPARATED"});
		}
	}
	
	public static TimeScale timeScale() {return instance.new TimeScale();}
	
	public class TimeScale extends Option {
		public static final int PHRASE_TIME_SCALE=0, CYCLE_TIME_SCALE=1;
		public TimeScale() {
			super("Time scale", new String[] {"PHRASE TIME SCALE", "CYCLE TIME SCALE"});
		}
	}
	
	public static Transform transform() {return instance.new Transform();}
	
	public class Transform extends Option {
		public static final int TRANSLATE=0, ROTATE=1, ROTATE_Z=2;
		public Transform() {
			super("Transform", new String[] {"TRANSLATE", "ROTATE", "ROTATE_Z"});
		}
	}
}