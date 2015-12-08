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
	
	public static void displayAddedWaves(PApplet pa, LinearPlot a, LinearPlot b) {
		int i=0; //looping variable for a.pts
		int j=0; //looping variable for b.pts
		pa.beginShape();
		while (i < a.pts.length && j < b.pts.length) {
			if (i >= a.pts.length) {
				pa.vertex(b.pts[j].x, b.pts[j].y);
				j++;
			}
			else if (j >= b.pts.length) {
				pa.vertex(a.pts[i].x, a.pts[i].y);
				i++;
			}
			else if (a.pts[i].x < b.pts[i].y) {
				pa.vertex(a.pts[i].x, a.pts[i].y);
				i++;
			}
			else {
				pa.vertex(b.pts[j].x, b.pts[j].y);
				j++;
			}
		}
		pa.endShape();
	}

	public void display(PApplet pa) {
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
