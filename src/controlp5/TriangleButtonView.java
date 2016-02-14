package controlp5;

import controlP5.Button;
import controlP5.ControllerView;
import geom.Polygon;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * 
 * Draws a ControlP5 Button as an isosceles triangle inscribed in an ellipse (the ellipse isn't actually drawn).
 * The ellipse's width and height are defined by the width and height of the Button.
 * 
 * @author James Morrow
 *
 */
public class TriangleButtonView implements ControllerView<Button> {
	private float headAngle, deviationAngle;
	
	/**
	 * 
	 * @param headAngle The angle specifying where the triangle head is pointed, in radians.
	 * @param deviationAngle How much the angles of the other two triangle vertices deviate from the triangle head, in radians.
	 */
	public TriangleButtonView(float headAngle, float deviationAngle) {
		this.headAngle = headAngle;
		this.deviationAngle = deviationAngle;
	}
	
	@Override
	public void display(PGraphics pg, Button b) {
		pg.noStroke();

		int c = b.isMouseOver() ? b.getColor().getActive() : b.getColor().getForeground();
		pg.fill(c);
		
		float cenx = b.getWidth()/2f;
		float ceny = b.getHeight()/2f;
		float halfWidth = b.getWidth()/2f;
		float halfHeight = b.getHeight()/2f;
		
		float a1 = headAngle;
		float a2 = headAngle - deviationAngle;
		float a3 = headAngle + deviationAngle;
		
		pg.triangle(cenx + halfWidth*PApplet.cos(a1), ceny + halfHeight*PApplet.sin(a1),
				    cenx + halfWidth*PApplet.cos(a2), ceny + halfHeight*PApplet.sin(a2),
				    cenx + halfWidth*PApplet.cos(a3), ceny + halfHeight*PApplet.sin(a3));
	}
}