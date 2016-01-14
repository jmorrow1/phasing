package icons;

import geom.Polygon;
import phases.PhasesPApplet;

public class DefaultIcon extends Icon {
	public DefaultIcon(int value) {
		super(value);
	}
	
	@Override
	public void draw(float x, float y, float radius, PhasesPApplet pa) {
		pa.strokeWeight(2);
		pa.stroke(0);
		pa.noFill();
		radius /= 2f;
		Polygon.drawRegularPolygon(x, y, radius, radius, value+3, 0, pa);
	}
}