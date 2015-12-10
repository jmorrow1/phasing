package geom;

public class Point {
	public double x, y;
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(Point pt) {
		this.x = pt.x;
		this.y = pt.y;
	}
}
