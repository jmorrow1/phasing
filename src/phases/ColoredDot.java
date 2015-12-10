package phases;

import processing.core.PApplet;

public class ColoredDot {
	public double x, y;
	public double diam;
	public int color;
	public double opacity;
	
	public ColoredDot(double x, double y, double diam, int color, int opacity) {
		this.x = x;
		this.y = y;
		this.diam = diam;
		this.color = color;
		this.opacity = opacity;
	}
	
	public void display(PhasesPApplet pa) {
		pa.fill(color, opacity);
		pa.ellipse(x, y, diam, diam);
	}
	
	public static void style(PhasesPApplet pa) {
		pa.ellipseMode(pa.CENTER);
		pa.noStroke();
	}
}
