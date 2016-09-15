package icons;

import phasing.PhasesPApplet;
import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class InstrumentIcon implements Icon {
	private final int instrument;

	public InstrumentIcon(int instrument) {
		this.instrument = instrument;
	}
	
	@Override
	public void draw(float cenx, float ceny, float radius, PhasesPApplet pa) {
		if (instrument == PIANO) {
			float w = 1.25f*radius;
	        float h = radius;
            float y1 = ceny - h/2f;
            float y2 = ceny + h/2f;
            
            pa.strokeWeight(radius/20f);
            pa.rectMode(pa.CENTER);
            
            //white keys
            pa.fill(255);
            pa.stroke(0);
            pa.rect(cenx, ceny, w, h);
            pa.line(cenx - w/6f, y1, cenx - w/6f, y2);
            pa.line(cenx + w/6f, y1, cenx + w/6f, y2);
            
            //black keys
            pa.noStroke();
            pa.fill(0);
            pa.rectMode(pa.CORNER);
            float blackKeyWidth = 2*w/9f;
            pa.rect(cenx - w/6f - blackKeyWidth/2f, ceny - h/2f, blackKeyWidth, h*0.66f);
            pa.rect(cenx + w/6f - blackKeyWidth/2f, ceny - h/2f, blackKeyWidth, h*0.66f); 
		}
		else if (instrument == MARIMBA) {
			pa.strokeWeight(2);
			pa.stroke(0);
			float halfW = 0.6f*radius;
			float halfH = 0.7f*radius;
			float angle = 0.3f * pa.PI;
			drawMallet(cenx, ceny, halfW, halfH, angle, pa);
			drawMallet(cenx, ceny, halfW, halfH, pa.PI - angle, pa);
		}
	}
	
	//Draws a mallet circumscribed in an ellipse.
	private static void drawMallet(float cenx, float ceny, float halfW, float halfH, float angle, PApplet pa) {
		pa.pushMatrix();
		pa.translate(cenx, ceny);
		float x1 = halfW*pa.cos(angle);
		float y1 = halfH*pa.sin(angle);
		float x2 = halfW*pa.cos(pa.PI + angle);
		float y2 = halfH*pa.sin(pa.PI + angle);
		pa.line(x1, y1, x2, y2);
		pa.fill(255);
		pa.translate(x2, y2);
		pa.rotate(angle + pa.HALF_PI);
		pa.ellipseMode(pa.RADIUS);
		pa.ellipse(0, 0, halfW*0.2f, halfH*0.2f);
		pa.popMatrix();
	}
	
	public static int numTypes() {
		return numInstruments;
	}
}