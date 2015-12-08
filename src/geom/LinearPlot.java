package geom;

import phases.Phrase;
import processing.core.PApplet;

public class LinearPlot extends Wave {
	private Point[] pts;
	private float x1, x2, dx;
	
	public LinearPlot(float[] ys, float x1, float x2) {
		pts = new Point[ys.length];
		float x = x1;
		float dx = (x2-x1) / ys.length;
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(x, ys[i]);
			x += dx;
		}
		
		this.x1 = x1;
		this.x2 = x2;
		this.dx = dx;
	}
	
	public LinearPlot(LinearPlot lp) {
		pts = new Point[lp.pts.length];
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(lp.pts[i]);
		}
		this.x1 = lp.x1;
		this.x2 = lp.x2;
		this.dx = lp.dx;
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
		x1 += dx;
		x2 += dx;
	}
}
