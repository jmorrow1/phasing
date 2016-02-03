package geom;

import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class Polygon extends Shape {
	private Point[] pts;
	private Point cen;
	private float width, height;
	
	/**
	 * Constructs a regular polygon inscribed in a circle.
	 * 
	 * @param c The circle
	 * @param numVertices The number of vertices in the polygon
	 * @param startAngle The angle of the first vertex
	 */
	public Polygon(Circle c, int numVertices, float startAngle) {
		this(c.getX(), c.getY(), c.getRadius(), c.getRadius(), numVertices, startAngle);
	}

	/**
	 * Constructs a regular polygon inscribed in a circle.
	 * 
	 * @param cenx The center x-coordinate of the circle
	 * @param ceny The center y-coordinate of the circle
	 * @param radius The radius of the circle
	 * @param numVertices The number of vertices in the polygon
	 * @param startAngle The angle of the first vertex
	 */
	public Polygon(float cenx, float ceny, float radius, int numVertices, float startAngle) {
		this(cenx, ceny, radius, radius, numVertices, startAngle);
	}
	
	/**
	 * Constructs a polygon inscribed in an ellipse.
	 * 
	 * @param cenx The center x-coordinate of the ellipse
	 * @param ceny The center y-coordinate of the ellipse
	 * @param half_width Half the width of the ellipse
	 * @param half_height Half the height of the ellipse
	 * @param numVertices The number of vertices in the polygon
	 * @param startAngle The angle of the first vertex
	 */
	public Polygon(float cenx, float ceny, float half_width, float half_height, int numVertices, float startAngle) {
		cen = new Point(cenx, ceny);
		pts = new Point[numVertices];		
		float angle=startAngle;
		float changeInAngle = PApplet.TWO_PI / numVertices;
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(cenx + half_width*PApplet.cos(angle),
							   ceny + half_height*PApplet.sin(angle));
			angle += changeInAngle;
		}
		computeDimensions();
	}
	
	/**
	 * Constructs a polygon inscribed in an ellipse.
	 * @param cenx The center x-coordinate of the ellipse
	 * @param ceny The center y-coordinate of the ellipse
	 * @param half_width Half the width of the ellipse
	 * @param half_height Half the height of the ellipse
	 * @param angles The sequence of angles
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
	
	/**
	 * Constructs a polygon from an array of alternating x and y coordinates.
	 * @param coords The array of x and y coordinates. Even indices should give x-coordinates and odd indices should give y-coordinates.
	 */
	public Polygon(float[] coords) {
		pts = new Point[coords.length/2];
		for (int i=0; i<pts.length; i++) {
			pts[i] = new Point(coords[i*2], coords[i*2+1]);
		}
		computeDimensions();
	}
	
	/**
	 * Constructs a polygon from an array of points
	 * @param pts The points
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
	
	/**
	 * Computes the dependent state of the Polygon.
	 * That is, the width, height, and center.
	 */
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
	
	/**
	 * Draws a polygon inscribed in an ellipse.
	 * 
	 * @param cenx The center x-coordinate of the ellipse
	 * @param ceny The center y-coordinate of the ellipse
	 * @param half_width Half the width of the ellipse
	 * @param half_height Half the height of the ellipse
	 * @param numVertices The number of vertices in the polygon
	 * @param startAngle The angle of the first vertex
	 * @param pa The PApplet instance to draw to
	 */
	public static void drawPolygon(float cenx, float ceny, float half_width, float half_height, int numVertices, float startAngle, PApplet pa) {
		float theta = startAngle;
		float dTheta = pa.TWO_PI / numVertices;
		pa.beginShape();
		for (int i=0; i<numVertices; i++) {
			pa.vertex(cenx + half_width*pa.cos(theta), ceny + half_height*pa.sin(theta));
			theta += dTheta;
		}
		pa.endShape(pa.CLOSE);
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
	
	/**
	 * 
	 * @return The center of the polygon
	 */
	public Point getCenter() {
		return new Point(cen);
	}
	
	/**
	 * Sets the center of the polygon, thereby translating the polygon.
	 * @param x
	 * @param y
	 */
	public void setCenter(float x, float y) {
		translate(x - cen.x, y - cen.y);
	}
	
	/**
	 * 
	 * @return The width of the polygon
	 */
	public float getWidth() {
		return width;
	}
	
	/**
	 * 
	 * @return The height of the polygon
	 */
	public float getHeight() {
		return height;
	}
	
	@Override
	public Polygon clone() {
		return new Polygon(this);
	}
}
