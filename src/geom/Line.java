package geom;

import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class Line {
    public float x1, y1, x2, y2;

    /**
     * 
     * @param x1
     *            The leftmost x-coordinate
     * @param y1
     *            The uppermost y-coordinate
     * @param x2
     *            the rightmost x-coordinate
     * @param y2
     *            the bottommost y-coordinate
     */
    public Line(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Copy constructor.
     * 
     * @param line
     *            The line to copy.
     */
    public Line(Line line) {
        this.x1 = line.x1;
        this.y1 = line.y1;
        this.x2 = line.x2;
        this.y2 = line.y2;
    }

    /**
     * Draws the line to the given PApplet instance.
     * 
     * @param pa
     */
    public void display(PApplet pa) {
        pa.line(x1, y1, x2, y2);
    }
}
