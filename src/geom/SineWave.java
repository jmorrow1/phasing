package geom;

import phases.PhasesPApplet;
import processing.core.PApplet;

public class SineWave extends Wave {
	private double x1, x2, width, ycen, amplitude;
	private double startAngle;
	
	public SineWave(double x1, double x2, double ycen, double amplitude) {
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
	
	public void display(PhasesPApplet pa, int color, int opacity) {
		pa.noFill();
		pa.stroke(color, opacity);
		pa.beginShape();
			double x = x1;
			double dx = 2;
			double angle = startAngle;
			double dAngle = pa.TWO_PI / ((x2-x1) / dx);
			while (x < x2) {
				pa.vertex(x, ycen + amplitude * Math.sin(angle));
				x += dx;
				angle += dAngle;
			}
			pa.vertex(x2, ycen + amplitude * Math.sin(pa.TWO_PI));
		
		pa.endShape();
	}

	@Override
	public void translate(double dx) {
		startAngle -= dx/width * PApplet.TWO_PI;
		//startAngle -= PApplet.map(dx, 0, width, 0, PApplet.TWO_PI);
	}
}