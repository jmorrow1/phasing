package geom;

import phases.Phrase;
import processing.core.PApplet;

public class LinearPlot extends Wave {
	private Point[] pts;
	private float x1, x2, dx;
	private boolean drawVertices;
	
	public LinearPlot(float[] ys, float x1, float x2) {
		this(ys, x1, x2, true);
	}
	
	public LinearPlot(float[] ys, float x1, float x2, boolean drawVertices) {
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
		this.drawVertices = drawVertices;
	}
	
	public LinearPlot(LinearPlot lp) {
		pts = new Point[lp.pts.length];
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(lp.pts[i]);
		}
		this.x1 = lp.x1;
		this.x2 = lp.x2;
		this.dx = lp.dx;
		this.drawVertices = lp.drawVertices;
	}

	public void display(PApplet pa, float x1, float x2, int color, int opacity) {
		//draw lines
		pa.noFill();
		pa.stroke(color, opacity);
		pa.beginShape();
		for (int i=0; i<pts.length; i++) {
			pa.vertex(pts[i].x, pts[i].y);
		}
		pa.endShape();
		
		if (drawVertices) {
			//draw vertices
			pa.noStroke();
			pa.fill(color, opacity);
			pa.ellipseMode(pa.CENTER);
			for (int i=0; i<pts.length; i++) {
				pa.ellipse(pts[i].x, pts[i].y, 10, 10);
			}
		}
	}
	
	public void translate(float dx, float dy) {
		for (int i=0; i<pts.length; i++) {
			pts[i].x += dx;
			pts[i].y += dy;
		}
		x1 += dx;
		x2 += dx;
	}
	
	public void drawVertices(boolean value) {
		drawVertices = value;
	}
}
