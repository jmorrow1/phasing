package geom;

import processing.core.PApplet;

/**
 * Represents a curved rectangle, which is two circle arcs, joined by line
 * segments.
 * 
 * @author James Morrow
 *
 */
public class CurvedRect {
    private final static int minPointsPerTwoPi = 10;
    private final static int numPointsPerTwoPi = 50;
    private final int numPointsPerArc;
    private Point[] pts;

    /**
     * 
     * @param radius
     * @param thickness
     * @param theta1
     * @param theta2
     */
    public CurvedRect(float radius, float thickness, float theta1, float theta2) {
        numPointsPerArc = PApplet.max(minPointsPerTwoPi,
                (int) PApplet.map(theta2 - theta1, 0, PApplet.TWO_PI, 0, numPointsPerTwoPi));
        pts = new Point[2 * numPointsPerArc];
        int i = 0;

        // arc 1
        float theta = theta1;
        float dTheta = (theta2 - theta1) / numPointsPerArc;
        float smallRadius = radius - thickness / 2f;
        for (int j = 0; j < numPointsPerArc - 1; j++) {
            pts[i++] = new Point(PApplet.cos(theta) * smallRadius, PApplet.sin(theta) * smallRadius);
            theta += dTheta;
        }

        theta = theta2;
        pts[i++] = new Point(PApplet.cos(theta) * smallRadius, PApplet.sin(theta) * smallRadius);

        // arc 2
        float largeRadius = radius + thickness / 2f;
        for (int j = 0; j < numPointsPerArc - 1; j++) {
            pts[i++] = new Point(PApplet.cos(theta) * largeRadius, PApplet.sin(theta) * largeRadius);
            theta -= dTheta;
        }

        theta = theta1;
        pts[i++] = new Point(PApplet.cos(theta) * largeRadius, PApplet.sin(theta) * largeRadius);
    }

    /**
     * Copy constructor.
     * 
     * @param curvedRect
     *            The CurvedRect to copy
     */
    public CurvedRect(CurvedRect curvedRect) {
        this.numPointsPerArc = curvedRect.numPointsPerArc;
        this.pts = new Point[curvedRect.pts.length];
        for (int i = 0; i < this.pts.length; i++) {
            this.pts[i] = new Point(curvedRect.pts[i]);
        }
    }

    /**
     * Draws the shape to the given PApplet instance.
     * 
     * @param pa
     */
    public void display(PApplet pa) {
        pa.beginShape();
        for (int i = 0; i < pts.length; i++) {
            pa.vertex(pts[i].x, pts[i].y);
        }
        pa.endShape(pa.CLOSE);
    }
}