package geom;

import processing.core.PApplet;

public class SineWave extends Wave {
	private float x1, x2, width, ycen, amplitude;
	private float startAngle;
	
	public SineWave(float x1, float x2, float ycen, float amplitude) {
		this.x1 = x1;
		this.x2 = x2;
		width = x2-x1;
		this.ycen = ycen;
		this.amplitude = amplitude;
	}
	
	public SineWave(SineWave sw) {
		this.x1 = sw.x1;
		this.x2 = sw.x2;
		this.width = sw.width;
		this.ycen = sw.ycen;
		this.amplitude = sw.amplitude;
	}
	
	public void display(PApplet pa, int color, int opacity) {
		pa.noFill();
		pa.stroke(color, opacity);
		pa.beginShape();
			float x = x1;
			float dx = 2;
			float angle = startAngle;
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
	public void translate(float dx) {
		startAngle -= dx/width * PApplet.TWO_PI;
		//startAngle -= PApplet.map(dx, 0, width, 0, PApplet.TWO_PI);
	}
}