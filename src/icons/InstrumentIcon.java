package icons;

import phases.PhasesPApplet;

public class InstrumentIcon implements Icon {
	private int instrument;

	public InstrumentIcon(int instrument) {
		this.instrument = instrument;
	}
	
	@Override
	public void draw(float x, float y, float radius, PhasesPApplet pa) {
		if (instrument == PIANO) {
			
		}
	}
}