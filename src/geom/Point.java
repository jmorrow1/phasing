package geom;

public class Point {
	public float x, y;
	
	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(Point pt) {
		this.x = pt.x;
		this.y = pt.y;
	}
}
