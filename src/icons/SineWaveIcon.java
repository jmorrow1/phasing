package icons;

import geom.Point;
import phasing.PhasesPApplet;
import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class SineWaveIcon implements Icon {
	private final int mode;
	private final static int maxNumPts = 12;
	
	public SineWaveIcon(int mode, PApplet pa) {
		this.mode = mode;
	}
	
	@Override
	public void draw(float x, float y, float radius, PhasesPApplet pa) {
		final float amp = radius*0.6f;
		final float halfWidth = radius*0.75f;
		final int numPts = pa.min(pa.phrase.getNumNotes(), maxNumPts);
		
		pa.stroke(0);
		pa.strokeWeight(radius/7.5f);
		
		float ptx = x - halfWidth;
		final float dx = (2*halfWidth) / numPts;
		if (mode == IS_SINE_WAVE) {
    		float theta = 0;
    		float dTheta = pa.TWO_PI / numPts;
    		for (int i=0; i<numPts; i++) {
    			pa.point(ptx, y + amp*pa.sin(theta));
    			theta += dTheta;
    			ptx += dx;
    		}
		}
		else if (mode == IS_NOT_SINE_WAVE) {
			final float minPitch = pa.phrase.minPitch();
			final float maxPitch = pa.phrase.maxPitch();
			for (int i=0; i<numPts; i++) {
				float pty = (minPitch != maxPitch) ? y + pa.map(pa.phrase.getSCPitch(i), maxPitch, minPitch, -amp, amp) : y;
				pa.point(ptx, pty);
				ptx += dx;
			}
		}
	}
}