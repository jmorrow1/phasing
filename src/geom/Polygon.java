package geom;

import processing.core.PApplet;

public class Polygon extends Shape {
	private Point[] pts;
	private Point cen;
	private float width, height;
	
	/**
	 * Constructs a regular polygon inscribed in a circle.
	 * @param c
	 * @param numPoints
	 * @param startAngle
	 */
	public Polygon(Circle c, int numPoints, float startAngle) {
		this(c.getX(), c.getY(), c.getRadius(), c.getRadius(), numPoints, startAngle);
	}

	/**
	 * Constructs a regular polygon inscribed in a circle.
	 * @param cenx 
	 * @param ceny 
	 * @param radius
	 * @param numPoints
	 * @param startAngle
	 */
	public Polygon(float cenx, float ceny, float radius, int numPoints, float startAngle) {
		this(cenx, ceny, radius, radius, numPoints, startAngle);
	}
	
	/**
	 * Constructs a polygon inscribed in an ellipse.
	 * @param cenx
	 * @param ceny
	 * @param half_width
	 * @param half_height
	 * @param numPoints
	 * @param startAngle
	 */
	public Polygon(float cenx, float ceny, float half_width, float half_height, int numPoints, float startAngle) {
		cen = new Point(cenx, ceny);
		pts = new Point[numPoints];		
		float angle=startAngle;
		float changeInAngle = PApplet.TWO_PI / numPoints;
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(cenx + half_width*PApplet.cos(angle),
							   ceny + half_height*PApplet.sin(angle));
			angle += changeInAngle;
		}
		computeDimensions();
	}
	
	/**
	 * Constructs a polygon inscribed in an ellipse.
	 * @param cenx
	 * @param ceny
	 * @param half_width
	 * @param half_height
	 * @param angles
	 */
	public Polygon(float cenx, float ceny, float half_width, float half_height, float[] angles) {
		cen = new Point(cenx, ceny);
		int n = angles.length;
		pts = new Point[n];
		for (int i=0; i<n; i++) {
			pts[i] = new Point(cenx + half_width*PApplet.cos(angles[i]),
							   ceny + half_height*PApplet.sin(angles[i]));
		}
		computeDimensions();
	}
	
	public Polygon(float[] coords) {
		pts = new Point[coords.length/2];
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(coords[i*2], coords[i*2+1]);
		}
		computeDimensions();
	}
	
	/**
	 * Constructs a polygon from an array of points
	 * @param pts
	 */
	public Polygon(Point[] pts) {
		this.pts = pts;
		computeDimensions();
	}
	
	/**
	 * Copy constructor
	 * @param poly Polygon to copy
	 */
	public Polygon(Polygon poly) {
		pts = new Point[poly.pts.length];
		for (int i=0; i<poly.pts.length; i++) {
			pts[i] = new Point(poly.pts[i]);
		}
		this.width = poly.width;
		this.height = poly.height;
	}
	
	private void computeDimensions() {
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
		
		this.width = maxX - minX;
		this.height = maxY - minY;
		this.cen = new Point((maxX - minX) / 2f, (maxY - minY) / 2f);
	}
	
	@Override
	public void display(PApplet pa) {
		pa.beginShape();
		for (int i=0; i<pts.length; i++) {
			pa.vertex(pts[i].x, pts[i].y);
		}
		pa.endShape(pa.CLOSE);
	}
	
	@Override
	public void translate(float dx, float dy) {
		for (Point pt : pts) {
			pt.x += dx;
			pt.y += dy;
		}
	}
	
	@Override
	public Point getCenter() {
		return new Point(cen);
	}
	
	public void setCenter(float x, float y) {
		translate(x - cen.x, y - cen.y);
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	@Override
	public Polygon clone() {
		return new Polygon(this);
	}
}
