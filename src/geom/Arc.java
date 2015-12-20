package geom;

import phases.PhasesPApplet;
import processing.core.PApplet;

public class Arc {
	private float cenx, ceny, startAngle, endAngle, radius;
	
	public Arc(float cenx, float ceny, float startAngle, float endAngle, float radius) {
		this.cenx = cenx;
		this.ceny = ceny;
		this.startAngle = PhasesPApplet.remainder(startAngle, PApplet.TWO_PI);
		this.endAngle = PhasesPApplet.remainder(endAngle, PApplet.TWO_PI);
		this.radius = radius;
	}
	
	public Arc(Arc arc) {
		this(arc.cenx, arc.ceny, arc.startAngle, arc.endAngle, arc.radius);
	}

	public void display(PApplet pa) {
		pa.ellipseMode(PApplet.RADIUS);
		pa.arc(cenx, ceny, radius, radius, startAngle, endAngle);
	}

	public void translate(float dx, float dy) {
		cenx += dx;
		ceny += dy;
	}
	
	public void rotate(float dAngle) {
		startAngle += dAngle;
		endAngle += dAngle;
	}
	
	/**
	 * 
	 * @return the x-coordinate where the arc starts
	 */
	public float startX() {
		return cenx + radius*PApplet.cos(startAngle);
	}
	
	/**
	 * 
	 * @return the y-coordinate where the arc starts
	 */
	public float startY() {
		return ceny + radius*PApplet.sin(startAngle);
	}
	
	/**
	 * 
	 * @return the x-coordinate where the arc ends
	 */
	public float endX() {
		return cenx + radius*PApplet.cos(endAngle);
	}
	
	/**
	 * 
	 * @return the y-coordinate where the arc ends
	 */
	public float endY() {
		return ceny + radius*PApplet.sin(endAngle);
	}
	
	/**
	 * 
	 * @return the angle along an ellipse where arc starts
	 */
	public float startAngle() {
		return startAngle;
	}
	
	/**
	 * Set the angle along an ellipse where the arc starts.
	 * @param startAngle
	 */
	public void setStartAngle(float startAngle) {
		this.startAngle = startAngle;
	}
	
	/**
	 *
	 * @return the angle along an ellipse where the arc stops
	 */
	public float endAngle() {
		return endAngle;
	}
	
	/**
	 * Set the angle along an ellipse where the arc stops.
	 * @param endAngle the angle where the arc stops
	 */
	public void setEndAngle(float endAngle) {
		this.endAngle = endAngle;
	}
}
