package icons;

import geom.Polygon;
import phasing.PhasesPApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class DefaultIcon implements Icon {
	private final int num;
	
	public DefaultIcon(int num) {
		this.num = num;
	}
	
	@Override
	public void draw(float x, float y, float radius, PhasesPApplet pa) {
		pa.strokeWeight(radius/20f);
		pa.stroke(0);
		pa.noFill();
		radius /= 2f;
		Polygon.drawPolygon(x, y, radius, radius, num+3, 0, pa);
	}
}