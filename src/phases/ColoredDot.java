package phases;

import processing.core.PApplet;

public class ColoredDot {
	public float x, y;
	public float diam;
	public int color;
	public float opacity;
	
	public ColoredDot(float x, float y, float diam, int color, int opacity) {
		this.x = x;
		this.y = y;
		this.diam = diam;
		this.color = color;
		this.opacity = opacity;
	}
	
	public void display(PApplet pa) {
		pa.fill(color, opacity);
		pa.ellipse(x, y, diam, diam);
	}
	
	public static void style(PApplet pa) {
		pa.ellipseMode(pa.CENTER);
		pa.noStroke();
	}
}
