package icons;

import java.lang.reflect.TypeVariable;

import phases.PhasesPApplet;
import views.Option.ActiveNote;
import views.OptionValue;

public abstract class Icon {
	public abstract void draw(float x, float y, float radius, PhasesPApplet pa);
	public static Icon init(OptionValue optionValue) {
		TypeVariable typeVar = optionValue.getClass().getTypeParameters()[0];
		if (typeVar.getClass().equals(ActiveNote.class)) {
		
		}

		return new DefaultIcon(optionValue.intValue());
	}
}