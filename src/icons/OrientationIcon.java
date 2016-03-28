package icons;

import phasing.PhasesPApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class OrientationIcon implements Icon {
	private int orientationMode;
	
	public OrientationIcon(int orientationMode) {
		this.orientationMode = orientationMode;
	}
	
	@Override
	public void draw(float cenx, float ceny, float radius, PhasesPApplet pa) {
		pa.strokeWeight(radius/15f);
		pa.stroke(0);
		pa.strokeCap(pa.ROUND);
		
		float arrowHeadSize = radius/4f;
		
		float x1 = cenx - 0.5f*radius;
		float x2 = cenx + 0.5f*radius;
		float y1 = ceny - 0.3f*radius;
		float y2 = ceny + 0.3f*radius;
		
		pa.line(x1, y1, x2, y1);
		pa.drawArrowHead(x2, y1, arrowHeadSize, 0, 0.8f*pa.PI);
		pa.line(x1, y2, x2, y2);
		if (orientationMode == NON_REVERSED) {
			pa.drawArrowHead(x2, y2, arrowHeadSize, 0, 0.8f*pa.PI);
		}
		else if (orientationMode == REVERSED) {
			pa.drawArrowHead(x1, y2, arrowHeadSize, pa.PI, 0.8f*pa.PI);
		}
	}
}