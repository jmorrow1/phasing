package geom;

import processing.core.PApplet;

public class Sector {
	private final static int minPointsPerTwoPi = 10;
    private final static int numPointsPerTwoPi = 50;
    private final int numPointsPerArc;
    private Point[] pts;
    
    public Sector(float radius, float thickness, float theta1, float theta2) {
        numPointsPerArc = PApplet.max(minPointsPerTwoPi, (int)PApplet.map(theta2 - theta1, 0, PApplet.TWO_PI, 0, numPointsPerTwoPi));
        pts = new Point[2*numPointsPerArc];
        int i=0;
      
        //arc 1
        float theta = theta1;
        float dTheta = (theta2 - theta1) / numPointsPerArc;
        float smallRadius = radius - thickness/2f;    
        for (int j=0; j<numPointsPerArc-1; j++) {
            pts[i++] = new Point(PApplet.cos(theta)*smallRadius, PApplet.sin(theta)*smallRadius);
            theta += dTheta;
        }
        
        theta = theta2;
        pts[i++] = new Point(PApplet.cos(theta)*smallRadius, PApplet.sin(theta)*smallRadius);
  
        //arc 2
        float largeRadius = radius + thickness/2f;
        for (int j=0; j<numPointsPerArc-1; j++) {
            pts[i++] = new Point(PApplet.cos(theta)*largeRadius, PApplet.sin(theta)*largeRadius);
            theta -= dTheta;
        }
        
        theta = theta1;
        pts[i++] = new Point(PApplet.cos(theta)*largeRadius, PApplet.sin(theta)*largeRadius);
    }
    
    public Sector(Sector sector) {
    	this.numPointsPerArc = sector.numPointsPerArc;
    	this.pts = new Point[sector.pts.length];
    	for (int i=0; i<this.pts.length; i++) {
    		this.pts[i] = new Point(sector.pts[i]);
    	}
    }

    public void display(PApplet pa) {
        pa.beginShape();
        for (int i=0; i<pts.length; i++) {
            pa.vertex(pts[i].x, pts[i].y);
        }
        pa.endShape(pa.CLOSE);
    }
}