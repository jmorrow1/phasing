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
	private float perimeter;
	private float[] lengths;
	private float[] breaks;
	private float width, height;
	
	public LinearPlot(Point[] pts) {
		this.pts = pts;
		breaks = new float[pts.length];
		lengths = new float[pts.length];
		recompute();
	}
	
	public LinearPlot(LinearPlot lp) {
		pts = new Point[lp.pts.length];
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(lp.pts[i]);
		}
		perimeter = lp.perimeter;
		breaks = new float[pts.length];
		lengths = new float[pts.length];
		for (int i=0; i<pts.length; i++) {
			breaks[i] = lp.breaks[i];
			lengths[i] = lp.lengths[i];
		}
	}
	
	private void recompute() {
		//compute perimeter
		perimeter = 0;		
		for (int i=1; i<pts.length; i++) {
			lengths[i] = PApplet.dist(pts[i-1].x, pts[i-1].y, pts[i].x, pts[i].y);
			perimeter += lengths[i];
		}
		lengths[0] = PApplet.dist(pts[pts.length-1].x, pts[pts.length-1].y, pts[0].x, pts[0].y);
		perimeter += lengths[0];
		//compute break points
		float sum = 0;
		for (int i=0; i<breaks.length-1; i++) {
			sum += lengths[i];
			breaks[i] = sum / perimeter;
		}
		breaks[breaks.length-1] = 1;
		recomputeCenterWidthAndHeight();
	}
	
	private void recomputeCenterWidthAndHeight() {
		float minX = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE;
		
		for (Point pt : pts) {
			if (pt.x < minX) minX = pt.x;
			if (pt.x > maxX) maxX = pt.x;
			if (pt.y < minY) minY = pt.y;
			if (pt.y > maxY) maxY = pt.y;
		}
		
		width = maxX - minX;
		height = maxY - minY;
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
	
	public float getPerimeter() {
		return perimeter;
	}
	
	public float width() {
		return width;
	}

	public float height() {
		return height;
	}

	@Override
	public void interpolate(float amt) {
		// TODO Auto-generated method stub
		
	}
}
