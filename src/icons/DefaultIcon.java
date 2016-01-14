package icons;

import geom.Polygon;
import phases.PhasesPApplet;

public class DefaultIcon implements Icon {
	private int num;
	
	public DefaultIcon(int num) {
		this.num = num;
	}
	
	@Override
	public void draw(float x, float y, float radius, PhasesPApplet pa) {
		pa.stroke(0);
		pa.noFill();
		radius /= 2f;
		Polygon.drawRegularPolygon(x, y, radius, radius, num, 0, pa);
	}
}