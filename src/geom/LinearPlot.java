package geom;

import phases.PhasesPApplet;

public class LinearPlot extends Wave {
	private Point leftEdgePoint, rightEdgePoint;
	private Point[] pts;
	private double x1, x2, dx, width;
	private boolean drawVertices;
	
	public LinearPlot(double[] ys, double x1, double x2) {
		this(ys, x1, x2, true);
	}
	
	public LinearPlot(double[] ys, double x1, double x2, boolean drawVertices) {
		pts = new Point[ys.length];
		double x = x1;
		double dx = (x2-x1) / ys.length;
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
	
	public LinearPlot(LinearPlot lp) {
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

	public void display(PhasesPApplet pa, int color, int opacity) {
		//draw lines
		pa.noFill();
		pa.stroke(color, opacity);
		pa.beginShape();
		pa.vertex(leftEdgePoint.x, leftEdgePoint.y);
		for (int i=0; i<pts.length; i++) {
			pa.vertex(pts[i].x, pts[i].y);
		}
		pa.vertex(rightEdgePoint.x, rightEdgePoint.y);
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
	
	public void translate(double dx) {
		//translate
		for (int i=0; i<pts.length; i++) {
			pts[i].x += dx;
		}
		
		//screen wrap
		if (dx < 0) {
			while (pts[0].x < x1) {
				pts[0].x += width;
				leftShift(pts);
			}
			double lerpAmt = (pts[0].x - x1) / this.dx;
			leftEdgePoint.y = PhasesPApplet.lerp(pts[0].y, pts[pts.length-1].y, lerpAmt);
			rightEdgePoint.y = PhasesPApplet.lerp(pts[0].y, pts[pts.length-1].y, lerpAmt);
		}
		else if (dx > 0) {
			while (pts[pts.length-1].x > x2) {
				pts[pts.length-1].x -= width;
				rightShift(pts);
			}
			double lerpAmt = (pts[0].x - x1) / this.dx;
			leftEdgePoint.y = PhasesPApplet.lerp(pts[0].y, pts[pts.length-1].y, lerpAmt);
			rightEdgePoint.y = PhasesPApplet.lerp(pts[0].y, pts[pts.length-1].y, lerpAmt);
		}
	}
	
	public void drawVertices(boolean value) {
		drawVertices = value;
	}
	
	private static void leftShift(Point[] xs) {
	    if (xs.length > 0) {
	        int i = xs.length-1;
	        Point next = xs[i];
	        xs[i] = xs[0];
	        i--;
	        while (i >= 0) {
	            Point temp = xs[i];
	            xs[i] = next;
	            next = temp;
	            i--;
	        }
	    }
	}

	private static void rightShift(Point[] xs) {
	    if (xs.length > 0) {
	        int i=0;
	        Point prev = xs[i];
	        xs[i] = xs[xs.length-1];
	        i++;
	        while (i < xs.length) {
	        	Point temp = xs[i];
	            xs[i] = prev;
	            prev = temp;
	            i++;
	        }
	    }
	}
}
