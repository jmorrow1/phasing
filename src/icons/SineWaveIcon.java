package icons;

import geom.Point;
import phases.PhasesPApplet;
import processing.core.PApplet;

public class SineWaveIcon implements Icon {
	private int mode;
	
	private final static int numPts = 10;
	private final static Point[] pts = new Point[numPts];
	private final float dTheta = PApplet.TWO_PI / numPts;
	
	public SineWaveIcon(int mode, PApplet pa) {
		this.mode = mode;
		
		float x = -1;
		float dx = 2f / pts.length;
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(x, pa.random(-1, 1));
			x += dx;
		}
	}
	
	@Override
	public void draw(float x, float y, float radius, PhasesPApplet pa) {
		pa.stroke(0);
		pa.strokeWeight(radius/7.5f);
		final float amp = radius*0.6f;
		final float halfWidth = radius*0.75f;
		
		if (mode == IS_SINE_WAVE) {
    		float theta = 0;
    		for (int i=0; i<numPts; i++) {
    			pa.point(x + halfWidth*pts[i].x, y + amp*pa.sin(theta));
    			theta += dTheta;
    		}
		}
		else if (mode == IS_NOT_SINE_WAVE) {
			for (int i=0; i<numPts; i++) {
				pa.point(x + halfWidth*pts[i].x, y + amp*pts[i].y);
			}
		}
	}
}
