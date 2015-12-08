package geom;

import processing.core.PApplet;

public class SineWave extends Wave {
	private float x1, x2, ycen, amplitude;
	
	public SineWave(float x1, float x2, float ycen, float amplitude) {
		this.x1 = x1;
		this.x2 = x2;
		this.ycen = ycen;
		this.amplitude = amplitude;
	}
	
	public SineWave(SineWave sw) {
		this.x1 = sw.x1;
		this.x2 = sw.x2;
		this.ycen = sw.ycen;
		this.amplitude = sw.amplitude;
	}
	
	public void display(PApplet pa, float x1, float x2, int color, int opacity) {
		pa.noFill();
		pa.stroke(color, opacity);
		pa.beginShape();
			float x = x1;
			float dx = 2;
			float angle = 0;
			float dAngle = pa.TWO_PI / ((x2-x1) / dx);
			while (x < x2) {
				pa.vertex(x, ycen + amplitude * PApplet.sin(angle));
				x += dx;
				angle += dAngle;
			}
			pa.vertex(x2, ycen + amplitude * PApplet.sin(pa.TWO_PI));
		
		pa.endShape();
	}

	@Override
	public void translate(float dx, float dy) {
		x1 += dx;
		x2 += dx;
		ycen += dy;
	}
}