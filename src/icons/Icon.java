package icons;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import phases.Option.ActiveNote;
import phases.Option.Camera;
import phases.Option.ColorScheme;
import phases.Option.NoteGraphic;
import phases.Option.PlotPitch;
import phases.Option.Superimpose;
import phases.Option.Transform;
import phases.OptionValue;
import phases.PhasesPApplet;

public abstract class Icon {
	protected int value;
	
	public Icon(int value) {
		this.value = value;
	}
	
	public abstract void draw(float x, float y, float radius, PhasesPApplet pa);
	public static Icon init(OptionValue<?> optionValue) {
		System.out.println(optionValue.getClass().getTypeName());
		Type typeVar = ((ParameterizedType)optionValue.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		if (typeVar.getClass().equals(ActiveNote.class)) {
			return new ActiveNoteIcon(optionValue.intValue());
		}
		else if (typeVar.getClass().equals(Camera.class)) {
			return new CameraIcon(optionValue.intValue());
		}
		else if (typeVar.getClass().equals(ColorScheme.class)) {
			return new ColorSchemeIcon(optionValue.intValue());
		}
		/*else if (typeVar.getClass().equals(Instrument.class)) {
			
		}*/
		else if (typeVar.getClass().equals(NoteGraphic.class)) {
			return new NoteIcon(optionValue.intValue());
		}
		else if (typeVar.getClass().equals(PlotPitch.class)) {
			return new PlotPitchIcon(optionValue.intValue());
		}
		/*else if (typeVar.getClass().equals(PianoAxis.class)) {
			
		}*/
		else if (typeVar.getClass().equals(Superimpose.class)) {
			return new SuperimposedOrSeparatedIcon(optionValue.intValue());
		}
		/*else if (typeVar.getClass().equals(TimeScale.class)) {
			
		}*/
		else if (typeVar.getClass().equals(Transform.class)) {
			return new TransformIcon(optionValue.intValue());
		}

		return new DefaultIcon(optionValue.intValue());
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
}