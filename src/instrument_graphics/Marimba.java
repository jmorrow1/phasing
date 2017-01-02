package instrument_graphics;

import geom.Polygon;
import geom.Rect;
import geom.Shape;
import phasing.PhasesPApplet;
import processing.core.PApplet;

/**
 * A graphical representation of a marimba.
 * 
 * @author James Morrow
 *
 */
public class Marimba implements Instrument {
    private int numOctaves;
    private int numBars;
    private Shape[] bars;

    /**************************
     ***** Initialization *****
     **************************/

    /**
     * 
     * @param numOctaves
     *            The number of octaves the marimba spans.
     * @param r
     *            The area in which the marimba is situated.
     */
    public Marimba(int numOctaves, Rect r) {
        init(numOctaves, r);
    }

    /**
     * Initializes (or re-initializes) the marimba's fields.
     * 
     * @param numOctaves
     *            The number of octaves the marimba spans.
     * @param r
     *            The area in which the marimba is situated.
     */
    public void init(int numOctaves, Rect r) {
        this.numOctaves = numOctaves;
        this.numBars = numOctaves * 12;
        int numWhiteBars = numOctaves * 7;
        bars = new Shape[numBars];
        int dx = (int) (r.getWidth() / numWhiteBars);
        int barWidth = PApplet.ceil(0.75f * dx);
        int widthBetweenBars = PApplet.floor(0.25f * dx);
        float unitBarHeight = r.getHeight() / 2f;
        initBars(r.getX1(), r.getY1() + 0.4f * r.getHeight(), barWidth, unitBarHeight, widthBetweenBars);
    }

    /**
     * Initializes the bars variable.
     * 
     * @param x1
     *            The leftmost x-coordinate of the area in which to initialize
     *            bars.
     * @param y1
     *            The uppermost y-coordinate of the area in which to initalize
     *            bars.
     * @param barWidth
     *            The width of any bar.
     * @param unitBarHeight
     *            The height of the unit bar.
     * @param widthBetweenBars
     *            The number of pixels between bars.
     */
    private void initBars(float x1, float y1, float barWidth, float unitBarHeight, float widthBetweenBars) {
        float dx = barWidth + widthBetweenBars;

        float prevBarHeight = 0;
        float barHeight = 0;
        float nextBarHeight = (int) barHeight(0, unitBarHeight);
        int i = 0;
        while (i < numBars) {
            barHeight = nextBarHeight;
            nextBarHeight = barHeight(i + 1, unitBarHeight);
            if (Instrument.isWhiteKey(i)) {
                bars[i] = consWhiteBar(i, x1, y1, barWidth, barHeight, widthBetweenBars, -prevBarHeight * 0.2f,
                        -nextBarHeight * 0.2f);
                i++;
                x1 += dx;
            } else {
                Rect r = new Rect(x1 - dx / 2f, y1 - (0.8f * barHeight), barWidth, barHeight, PApplet.CORNER);
                bars[i++] = r;
            }
            prevBarHeight = barHeight;
        }
    }

    /**
     * Constructs a Polygon for a white bar.
     * 
     * @param barIndex
     *            The index of the bar.
     * @param x1
     *            The leftmost x-coordinate of the bar.
     * @param y1
     *            The uppermost y-coordinate of the bar.
     * @param barWidth
     *            The width of any bar.
     * @param barHeight
     *            The height of the bar.
     * @param widthBetweenBars
     *            The number of pixels between bars.
     * @param prevBarHeight
     *            The height of the bar at the previous index.
     * @param nextBarHeight
     *            The height of the bar at the next index.
     * @return The polygon.
     */
    private Shape consWhiteBar(int barIndex, float x1, float y1, float barWidth, float barHeight,
            float widthBetweenBars, float prevBarHeight, float nextBarHeight) {
        boolean leftNeighborIsBlack = Instrument.isBlackKey(barIndex - 1);
        boolean rightNeighborIsBlack = Instrument.isBlackKey(barIndex + 1);

        float x2 = x1 + barWidth / 2f - widthBetweenBars / 2f;
        float x3 = x1 + barWidth / 2f + widthBetweenBars / 2f;
        float x4 = x1 + barWidth;

        float y2 = y1 - prevBarHeight;
        float y3 = y1 - nextBarHeight;
        float y4 = y1 + barHeight;

        if (!leftNeighborIsBlack && rightNeighborIsBlack) {
            return new Polygon(new float[] { x1, y1, x3, y1, x3, y3, x4, y3, x4, y4, x1, y4, x1, y3 });
        } else if (leftNeighborIsBlack && rightNeighborIsBlack) {
            return new Polygon(new float[] { x1, y2, x2, y2, x2, y1, x3, y1, x3, y3, x4, y3, x4, y4, x1, y4 });
        } else if (leftNeighborIsBlack && !rightNeighborIsBlack) {
            return new Polygon(new float[] { x1, y2, x2, y2, x2, y1, x3, y1, x4, y1, x4, y4, x1, y4 });
        }

        return null;
    }

    /********************************
     ***** Instrument Interface *****
     ********************************/

    @Override
    public Shape pitchToShape(int index) {
        index = PhasesPApplet.remainder(index, bars.length);
        return bars[index].clone();
    }

    @Override
    public void display(PApplet pa) {
        pa.stroke(100);
        pa.noFill();
        for (Shape s : bars) {
            s.display(pa);
        }
    }

    /**********************************
     ***** Static Utility Methods *****
     **********************************/

    private static int cachedUpTo = 0; // exclusive bound
    private static float[] normalBarHeightCache = new float[100];

    /**
     * Computes the height of the nth bar in a mrimba with a given unit bar
     * height. Caches up to the first 100 normal bar heights to avoid
     * recomputation.
     * 
     * @param n
     *            The index of the bar in a marimba.
     * @param unitBarHeight
     *            The unit bar height.
     * @return The bar height of the nth bar.
     */
    private static float barHeight(int n, float unitBarHeight) {
        if (cachedUpTo > n) {
            return normalBarHeightCache[n] * unitBarHeight;
        } else if (n < normalBarHeightCache.length) {
            for (int i = cachedUpTo; i < n; i++) {
                normalBarHeightCache[i] = barHeightHelper(i, 1);
            }

            return normalBarHeightCache[n] * unitBarHeight;
        } else {
            return barHeightHelper(n, unitBarHeight);
        }
    }

    /**
     * Computes the height of the nth bar in a marimba with a given unit bar
     * height.
     * 
     * @param n
     *            The index of the bar in a marimba.
     * @param unitBarHeight
     *            The unit bar height.
     * @return The bar height of the nth bar.
     */
    private static float barHeightHelper(int n, float unitBarHeight) {
        float ratio = 1 / (float) Math.sqrt(Math.pow(nroot(2, 12), n));
        return ratio * unitBarHeight;
    }

    /**
     * Computes the nth root of x.
     * 
     * @param x
     * @param n
     * @return The nth root of x.
     */
    private static float nroot(float x, float n) {
        return (float) Math.pow(x, 1 / n);
    }

    /*******************************
     ***** Getters and Setters *****
     *******************************/

    @Override
    public int getNumOctaves() {
        return numOctaves;
    }
}