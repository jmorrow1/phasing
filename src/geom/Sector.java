package geom;

import processing.core.PApplet;

public class Sector {
	private Arc arc1, arc2;
	
	public Sector(float cenx, float ceny, float r1, float r2, float a1, float a2) {
		arc1 = new Arc(cenx, ceny, a1, a2, r1);
		arc2 = new Arc(cenx, ceny, a1, a2, r2);
	}
	
	public Sector(Sector s) {
		s.arc1 = new Arc(arc1);
		s.arc2 = new Arc(arc2);
	}
	
	public void display(PApplet pa) {
		arc1.display(pa);
		pa.line(arc1.endX(), arc1.endY(), arc2.endX(), arc2.endY());
		arc2.display(pa);
		pa.line(arc2.startX(), arc2.startY(), arc1.startX(), arc1.startY());		
	}

	public void translate(float dx, float dy) {
		arc1.translate(dx, dy);
		arc2.translate(dx, dy);
	}
	
	public void rotate(float dAngle) {
		arc1.rotate(dAngle);
		arc2.rotate(dAngle);
	}
}
