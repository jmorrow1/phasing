package instrument_graphics;

import geom.Point;
import geom.Polygon;
import geom.Rect;
import geom.Shape;
import phasing.PhasesPApplet;
import processing.core.PApplet;

/**
 * A graphical representation of a piano.
 * 
 * @author James Morrow
 *
 */
public class Piano extends Rect implements Instrument {
    private final int numOctaves;
    private boolean faceDown;

    // keys
    private Polygon[] whiteKeys;
    private Rect[] blackKeys;
    private Shape[] keys;
    private int blackKeyColor;

    /**************************
     ***** Initialization *****
     **************************/

    /**
     * 
     * @param numOctaves
     *            The number of octaves the piano should span, starting on C.
     * @param area
     *            Defines the area the piano should cover.
     * @param faceDown
     *            If true, the piano faces down. If false, the piano faces up.
     * @param blackKeyColor
     *            The color to fill the black keys.
     */
    public Piano(int numOctaves, Rect area, boolean faceDown, int blackKeyColor) {
        super(area);

        this.numOctaves = numOctaves;
        this.faceDown = faceDown;

        if (numOctaves >= 0) {
            initArrays();
        }
        if (numOctaves > 0) {
            initKeys();
        }

        this.blackKeyColor = blackKeyColor;
    }

    /**
     * Sets up the arrays that store the keys (shapes) of the piano.
     */
    private void initArrays() {
        int numKeys = numOctaves * 12;
        int numWhiteKeys = numOctaves * 7;
        int numBlackKeys = numOctaves * 5;
        keys = new Shape[numKeys];
        whiteKeys = new Polygon[numWhiteKeys];
        blackKeys = new Rect[numBlackKeys];
    }

    /**
     * Constructs a polygonal white key and returns it.
     * 
     * @param pitch
     *            The pitch of the key.
     * @param x1
     *            The leftmost x-coordinate of the key.
     * @param y1
     *            The uppermost y-coordinate of the key.
     * @param whiteKeyWidth
     *            The width of white keys in a piano.
     * @param whiteKeyHeight
     *            The height of white keys in a piano.
     * @param blackKeyWidth
     *            The width of black keys in a piano.
     * @param blackKeyHeight
     *            The height of black keys in a piano.
     * @return A polygon the shape of the white key.
     */
    private static Polygon initWhiteKey(int pitch, float x1, float y1, float whiteKeyWidth, float whiteKeyHeight,
            float blackKeyWidth, float blackKeyHeight) {
        boolean leftNeighborIsBlack = Instrument.isBlackKey(pitch - 1);
        boolean rightNeighborIsBlack = Instrument.isBlackKey(pitch + 1);

        float x2 = x1 + blackKeyWidth / 2f;
        float x3 = x1 + whiteKeyWidth - blackKeyWidth / 2f;
        float x4 = x1 + whiteKeyWidth;
        float y2 = y1 + blackKeyHeight;
        float y3 = y1 + whiteKeyHeight;

        if (leftNeighborIsBlack && !rightNeighborIsBlack) {
            return new Polygon(new float[] { x2, y1, x4, y1, x4, y3, x1, y3, x1, y2, x2, y2 });
        } else if (!leftNeighborIsBlack && rightNeighborIsBlack) {
            return new Polygon(new float[] { x1, y1, x3, y1, x3, y2, x4, y2, x4, y3, x1, y3 });
        } else if (leftNeighborIsBlack && rightNeighborIsBlack) {
            return new Polygon(new float[] { x2, y1, x3, y1, x3, y2, x4, y2, x4, y3, x1, y3, x1, y2, x2, y2 });
        } else {
            return null;
        }
    }

    /**
     * Initializes the shapes representing piano keys.
     */
    private void initKeys() {
        // dependent parameters
        int numKeys = numOctaves * 12;
        float divisor = numOctaves * 7f;
        float whiteKeyWidth = (getWidth() > getHeight()) ? (getWidth() - 1) / divisor : getWidth();

        float whiteKeyHeight = (getHeight() > getWidth()) ? (getHeight() - 1) / divisor : getHeight();
        float blackKeyWidth = whiteKeyWidth * 0.625f;
        float blackKeyHeight = whiteKeyHeight * 0.625f;

        // init keys
        float x1 = this.getX1();
        float y1 = this.getY1();
        int j = 0; // looping variable for whiteKeys
        int k = 0; // looping variable for blackKeys
        for (int i = 0; i < numKeys; i++) { // looping variable for all keys
            // init white keys
            whiteKeys[j++] = initWhiteKey(i, x1, y1, whiteKeyWidth, whiteKeyHeight, blackKeyWidth, blackKeyHeight);
            keys[i] = whiteKeys[j - 1];
            // init black keys
            if (i % 12 != 4 && i % 12 != 11) {
                if (getWidth() > getHeight()) {
                    blackKeys[k++] = new Rect(x1 + whiteKeyWidth - blackKeyWidth / 2f, y1, blackKeyWidth,
                            blackKeyHeight, PApplet.CORNER);
                    if (!faceDown) {
                        blackKeys[k - 1].translate(0, whiteKeyHeight - blackKeyHeight);
                    }
                } else {
                    blackKeys[k++] = new Rect(x1, y1 + whiteKeyHeight - blackKeyHeight / 2f, blackKeyWidth,
                            blackKeyHeight, PApplet.CORNER);
                    if (!faceDown) {
                        blackKeys[k - 1].translate(whiteKeyWidth - blackKeyWidth, 0);
                    }
                }
                i++;
                keys[i] = blackKeys[k - 1];
            }
            // increment key position
            if (getWidth() > getHeight()) {
                x1 += whiteKeyWidth;
            } else {
                y1 += whiteKeyHeight;
            }
        }
    }

    /********************************
     ***** Instrument Interface *****
     ********************************/

    @Override
    public void display(PApplet pa) {
        displayWhiteKeys(pa);
        drawBlackKeys(pa);
    }

    /**
     * Draws all the white keys.
     * 
     * @param pa
     *            The PApplet to draw to.
     */
    private void displayWhiteKeys(PApplet pa) {
        pa.stroke(100);
        pa.fill(255);
        for (int i = 0; i < whiteKeys.length; i++) {
            whiteKeys[i].display(pa);
        }
    }

    /**
     * Draws all the black keys.
     * 
     * @param pa
     *            The PApplet to draw to.
     */
    private void drawBlackKeys(PApplet pa) {
        pa.stroke(100);
        pa.fill(blackKeyColor);
        for (int i = 0; i < blackKeys.length; i++) {
            blackKeys[i].display(pa);
        }
    }

    /**
     * Returns a rectangle copy of the key indexed by i.
     * 
     * @param i
     * @return
     */
    public Shape pitchToShape(int i) {
        i = PhasesPApplet.remainder(i, keys.length);
        return keys[i].clone();
    }

    /*******************************
     ***** Getters and Setters *****
     *******************************/

    /**
     * Sets the width of the piano's area (which is a rectangle).
     */
    public void setWidth(float width) {
        super.setWidth(width);
        if (numOctaves > 0) {
            initKeys();
        }
    }

    /**
     * Sets the height of the piano's area (which is a rectangle).
     */
    public void setHeight(float height) {
        super.setHeight(height);
        if (numOctaves > 0) {
            initKeys();
        }
    }

    /**
     * Sets the width and height of the piano's area.
     * 
     * @param width
     * @param height
     */
    public void setSize(float width, float height) {
        super.setWidth(width);
        super.setHeight(height);
        if (numOctaves > 0) {
            initKeys();
        }
    }

    @Override
    public int getNumOctaves() {
        return numOctaves;
    }

    /**
     * 
     * @return The number of keys in this particular piano.
     */
    public int numKeys() {
        return keys.length;
    }

    /**
     * 
     * @return The width of white keys in this piano.
     */
    public float getWhiteKeyWidth() {
        return whiteKeys[0].getWidth();
    }

    /**
     * 
     * @return The height of white keys in this piano.
     */
    public float getWhiteKeyHeight() {
        return whiteKeys[0].getHeight();
    }

    /**
     * 
     * @return The width of black keys in this piano.
     */
    public float getBlackKeyWidth() {
        return blackKeys[0].getWidth();
    }

    /**
     * 
     * @return The height of black keys in this piano.
     */
    public float getBlackKeyHeight() {
        return blackKeys[0].getHeight();
    }

    /**
     * 
     * @return True, if this piano faces down. False, if this piano faces up.
     */
    public boolean isFaceDown() {
        return faceDown;
    }

    /**
     * Sets whether or not the piano faces down.
     * 
     * @param value
     *            If true, the piano faces down. If false, the piano faces up.
     */
    public void setToFaceDown(boolean value) {
        if (faceDown != value) {
            faceDown = value;
            initKeys();
        }
    }

    /**
     * Returns a rectangle the same size as the white keys in this piano. The
     * rectangle will be positioned with its center at (0, 0).
     * 
     * @return
     */
    public Polygon getWhiteKeyCopy() {
        if (whiteKeys.length > 0) {
            Polygon copy = new Polygon(whiteKeys[0]);
            copy.setCenter(0, 0);
            return copy;
        } else {
            return null;
        }
    }

    /**
     * Returns a rectangle the same size as the black keys in this piano. The
     * rectangle will be positioned with its center at (0, 0).
     * 
     * @return
     */
    public Rect getBlackKeyCopy() {
        if (blackKeys.length > 0) {
            Rect copy = new Rect(blackKeys[0]);
            return copy;
        } else {
            return null;
        }
    }
}