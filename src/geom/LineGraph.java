package geom;

import phases.Phrase;
import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class LineGraph extends Wave {
	private Point leftEdgePoint, rightEdgePoint;
	private Point[] pts;
	private float x1, x2, dx, width;
	private boolean drawVertices;
	
	/**
	 * 
	 * @param ys The y-values of the graph
	 * @param x1 The leftmost coordinate on the x-axis
	 * @param x2 The rightmost coordinate on the x-axis
	 */
	public LineGraph(float[] ys, float x1, float x2) {
		this(ys, x1, x2, true);
	}
	
	/**
	 * 
	 * @param ys The y-values of the graph
	 * @param x1 The leftmost coordinate on the x-axis
	 * @param x2 The rightmost coordinate on the x-axis
	 * @param drawVertices Whether or not to draw the vertices as dots
	 */
	public LineGraph(float[] ys, float x1, float x2, boolean drawVertices) {
		pts = new Point[ys.length];
		float x = x1;
		float dx = (x2-x1) / ys.length;
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(x, ys[i]);
			x += dx;
		}
		if (ys.length > 0) {
			leftEdgePoint = new Point(pts[0]);
			rightEdgePoint = new Point(x2, pts[0].y);
		}
		this.x1 = x1;
		this.x2 = x2;
		this.width = x2-x1;
		this.dx = dx;
		this.drawVertices = drawVertices;
	}
	
	/**
	 * Copy constructor
	 * @param lp The LineGraph to copy
	 */
	public LineGraph(LineGraph lp) {
		pts = new Point[lp.pts.length];
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(lp.pts[i]);
		}
		if (lp.leftEdgePoint != null && lp.rightEdgePoint != null) {
			this.leftEdgePoint = new Point(lp.leftEdgePoint);
			this.rightEdgePoint = new Point(lp.rightEdgePoint);
		}
		this.x1 = lp.x1;
		this.x2 = lp.x2;
		this.width = lp.width;
		this.dx = lp.dx;
		this.drawVertices = lp.drawVertices;
	}
	
	public void display(PApplet pa, int color, int opacity) {
		pa.stroke(color, opacity);
		pa.noFill();
		drawLines(pa);
		pa.noStroke();
		pa.fill(color, opacity);
		drawVertices(pa);
	}
	
	/**
	 * Draws the line segments of the graph
	 * @param pa The PApplet to draw on
	 */
	private void drawLines(PApplet pa) {
		pa.beginShape();
			pa.vertex(leftEdgePoint.x, leftEdgePoint.y);
			for (int i=0; i<pts.length; i++) {
				pa.vertex(pts[i].x, pts[i].y);
			}
			pa.vertex(rightEdgePoint.x, rightEdgePoint.y);
		pa.endShape();
	}
	
	/**
	 * Draws the vertices of the graph as dots
	 * @param pa The PApplet to draw on
	 */
	private void drawVertices(PApplet pa) {
		for (int i=0; i<pts.length; i++) {
			pa.ellipse(pts[i].x, pts[i].y, 10, 10);
		}
	}
	
	public void translate(float dx) {
		//translate
		for (int i=0; i<pts.length; i++) {
			pts[i].x += dx;
		}
		
		//screen wrap
		if (dx < 0) {
			while (pts[0].x < x1) {
				pts[0].x += width;
				Point.leftShift(pts);
			}
			float lerpAmt = (pts[0].x - x1) / this.dx;
			leftEdgePoint.y = PApplet.lerp(pts[0].y, pts[pts.length-1].y, lerpAmt);
			rightEdgePoint.y = PApplet.lerp(pts[0].y, pts[pts.length-1].y, lerpAmt);
		}
		else if (dx > 0) {
			while (pts[pts.length-1].x > x2) {
				pts[pts.length-1].x -= width;
				Point.rightShift(pts);
			}
			float lerpAmt = (pts[0].x - x1) / this.dx;
			leftEdgePoint.y = PApplet.lerp(pts[0].y, pts[pts.length-1].y, lerpAmt);
			rightEdgePoint.y = PApplet.lerp(pts[0].y, pts[pts.length-1].y, lerpAmt);
		}
	}
	
	/**
	 * If true, sets the vertices to be drawn as dots.
	 * If false, sets the vertices not to be drawn.
	 * @param value Whether or not to draw the vertices as dots
	 */
	public void drawVertices(boolean value) {
		drawVertices = value;
	}
}
