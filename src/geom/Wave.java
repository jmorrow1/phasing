package geom;

import phases.PhasesPApplet;

public abstract class Wave {
	public abstract void display(PhasesPApplet pa, int color, int opacity);
	public abstract void translate(double dx);
}