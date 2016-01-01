package phases;

import geom.Shape;
import processing.core.PApplet;

public interface Instrument {
	public Shape getKeyCopy(int index);
	public void display(PApplet pa);
}
