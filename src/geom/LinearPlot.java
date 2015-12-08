package geom;

import processing.core.PApplet;

/**
 * A series of line segments.
 * 
 * @author James Morrow
 *
 */
public class LinearPlot extends Wave {
	private Point[] pts;
	
	public LinearPlot(Point[] pts) {
		this.pts = pts;
	}
	
	public LinearPlot(LinearPlot lp) {
		pts = new Point[lp.pts.length];
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(lp.pts[i]);
		}
	}

	public void display(PApplet pa, float x1, float x2) {
		pa.beginShape();
		for (int i=0; i<pts.length; i++) {
			pa.vertex(pts[i].x, pts[i].y);
		}
		pa.endShape();
	}
	
	public void translate(float dx, float dy) {
		for (int i=0; i<pts.length; i++) {
			pts[i].x += dx;
			pts[i].y += dy;
		}
	}
}
