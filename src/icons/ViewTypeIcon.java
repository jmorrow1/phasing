package icons;

import phasing.PhasesPApplet;
import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class ViewTypeIcon implements Icon {
	private final int viewType;
	
	public ViewTypeIcon(int viewType) {
		this.viewType = viewType;
	}
	
	@Override
	public void draw(float x, float y, float radius, PhasesPApplet pa) {
		//clock outline
		pa.strokeWeight(radius / 10f);
		pa.stroke(0);
		pa.noFill();
		pa.ellipseMode(pa.RADIUS);
		pa.ellipse(x, y, 0.8f*radius, 0.8f*radius);
		
		//clock center
		pa.noStroke();
		pa.fill(0);
		pa.ellipse(x, y, radius / 7.5f, radius / 7.5f);
		
		//time markers
		float theta = 0;
		float dTheta = pa.TWO_PI / 12f;
		float markerRadius = 0.7f * radius;
		float dotRadius = radius / 15f;
		pa.fill(100);
		for (int i=0; i<12; i++) {
			pa.ellipse(x + PApplet.cos(theta)*markerRadius, y + PApplet.sin(theta)*markerRadius, dotRadius, dotRadius);
			theta += dTheta;
		}
		
		//clock hand
		pa.stroke(0);
		float handLength = 0.55f*radius;
		switch (viewType) {
			case MUSICIAN :
				present(x, y, handLength, pa);
				break;
			case PHASE_SHIFTER :
				past(x, y, handLength, pa);
				present(x, y, handLength, pa);
				future(x, y, handLength, pa);
				/*pa.stroke(pa.getColor2());
				pa.drawSineWave(x, y, 1.5f*radius, 0.5f*radius, 0);
				pa.stroke(pa.getColor1());
				pa.drawSineWave(x, y, 1.5f*radius, 0.5f*radius, pa.HALF_PI);*/
				break;
			case LIVE_SCORER :
				past(x, y, handLength, pa);
				present(x, y, handLength, pa);
				break;
		}
	}
	
	private void past(float x, float y, float handLength, PApplet pa) {
		hand(x, y, handLength, -pa.TWO_PI/6f - pa.HALF_PI, pa);
	}
	
	private void present(float x, float y, float handLength, PApplet pa) {
		pa.line(x, y, x, y - handLength);
	}
	
	private void future(float x, float y, float handLength, PApplet pa) {
		hand(x, y, handLength, pa.TWO_PI/6f - pa.HALF_PI, pa);
	}
	
	private void hand(float x, float y, float length, float angle, PApplet pa) {
		pa.line(x, y, x + length*PApplet.cos(angle), y + length*PApplet.sin(angle));
	}
	
	public static int numTypes() {
		return numViewTypes;
	}
}