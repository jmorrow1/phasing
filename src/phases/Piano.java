package phases;

import geom.Point;
import geom.Rect;
import processing.core.PApplet;

/**
 * A graphical representation of a piano.
 * 
 * @author James Morrow
 *
 */
public class Piano extends Rect implements Instrument {
	//independent parameters
	private int numOctaves;
	private boolean facePositive;
	private boolean allKeysEqualSize;
	//keys
	private Rect[] whiteKeys, blackKeys, keys;
	private int blackKeyColor;
	
	public Piano(int numOctaves, Rect rect, boolean facePositive) {
		this(numOctaves, rect, facePositive, false, 0);
	}

	public Piano(int numOctaves, Rect rect, boolean facePositive, boolean allKeysEqualSize, int blackKeyColor) {
		super(rect);
		
		this.numOctaves = numOctaves;
		this.facePositive = facePositive;
		this.allKeysEqualSize = allKeysEqualSize;
		
		if (numOctaves >= 0) {
			initArrays();
		}
		if (numOctaves > 0) {
			initRects();
		}
		
		this.blackKeyColor = blackKeyColor;
	}
	
	private void initArrays() {
		int numKeys = numOctaves * 12;
		int numWhiteKeys = numOctaves * 7;
		int numBlackKeys = numOctaves * 5;
		keys = new Rect[numKeys];
		whiteKeys = new Rect[numWhiteKeys];
		blackKeys = new Rect[numBlackKeys];
	}
	
	private void initRects() {
		//dependent parameters
		int numKeys = numOctaves * 12;
		float divisor = (allKeysEqualSize) ? numKeys : numOctaves*7f;
		float whiteKeyWidth = (getWidth() > getHeight()) ? (getWidth()-1) / divisor : 
			                                                getWidth();
		
		float whiteKeyHeight = (getHeight() > getWidth()) ? (getHeight()-1) / divisor :
			                                                 getHeight();
		float blackKeyWidth = whiteKeyWidth * 0.625f;
		float blackKeyHeight = whiteKeyHeight * 0.625f;
		
		//init keys
		float x1 = this.getX1();
		float y1 = this.getY1();
		int j=0; //looping variable for whiteKeys
		int k=0; //looping variable for blackKeys
		for (int i=0; i<numKeys; i++) { //looping variable for all keys
			//init white keys
			whiteKeys[j++] = new Rect(x1, y1, whiteKeyWidth, whiteKeyHeight, PApplet.CORNER);
			keys[i] = whiteKeys[j-1];
			//init black keys
			if (i % 12 != 4 && i % 12 != 11) {
				if (getWidth() > getHeight()) {
					if (allKeysEqualSize) {
						x1 += whiteKeyWidth;
						blackKeys[k++] = new Rect(x1, y1, whiteKeyWidth, whiteKeyHeight, PApplet.CORNER);
					}
					else {
						blackKeys[k++] = new Rect(x1 + whiteKeyWidth - blackKeyWidth/2f, y1, 
								                  blackKeyWidth, blackKeyHeight, PApplet.CORNER);	
						if (!facePositive) {
							blackKeys[k-1].translate(0, whiteKeyHeight-blackKeyHeight);
						}
					}
				}
				else {
					if (allKeysEqualSize) {
						y1 += whiteKeyHeight;
						blackKeys[k++] = new Rect(x1, y1, whiteKeyWidth, whiteKeyHeight, PApplet.CORNER);
					}
					else {
						blackKeys[k++] = new Rect(x1, y1 + whiteKeyHeight - blackKeyHeight/2f, 
				                                  blackKeyWidth, blackKeyHeight, PApplet.CORNER);
						if (!facePositive) {
							blackKeys[k-1].translate(whiteKeyWidth-blackKeyWidth, 0);
						}
					}
				}
				i++;
				keys[i] = blackKeys[k-1];
			}
			//increment key position
			if (getWidth() > getHeight()) {
				x1 += whiteKeyWidth;
			}
			else {
				y1 += whiteKeyHeight;
			}
		}
	}
	
	public void display(PApplet pa) {
		drawWhiteKeys(pa);			
		drawBlackKeys(pa);
	}
	
	public void drawWhiteKeys(PApplet pa) {
		pa.strokeWeight(1);
		pa.stroke(100);
		pa.fill(255);
		for (int i=0; i<whiteKeys.length; i++) {
			whiteKeys[i].display(pa);
		}
	}
	
	public void drawBlackKeys(PApplet pa) {
		pa.strokeWeight(1);
		pa.stroke(100);
		pa.fill(blackKeyColor);
		for (int i=0; i<blackKeys.length; i++) {
			blackKeys[i].display(pa);
		}
	}
	
	public void setWidth(float width) {
		super.setWidth(width);
		if (numOctaves > 0) {
			initRects();
		}
	}
	
	public void setHeight(float height) {
		super.setHeight(height);
		if (numOctaves > 0) {
			initRects();
		}
	}
	
	public int getNumOctaves() {
		return numOctaves;
	}
	
	public int numKeys() {
		return keys.length;
	}
	
	public float getWhiteKeyWidth() {
		return whiteKeys[0].getWidth();
	}
	
	public float getWhiteKeyHeight() {
		return whiteKeys[0].getHeight();
	}
	
	public float getBlackKeyWidth() {
		return blackKeys[0].getWidth();
	}
	
	public float getBlackKeyHeight() {
		return blackKeys[0].getHeight();
	}
	
	public void setNumOctaves(int numOctaves) {
		this.numOctaves = numOctaves;
		if (numOctaves >= 0) {
			initArrays();
		}
		if (numOctaves > 0) {
			initRects();
		}
	}
	
	public boolean getFacePositive() {
		return facePositive;
	}
	
	public void setFacePositive(boolean value) {
		if (facePositive != value) {
			facePositive = value;
			initRects();
		}
	}
	
	/**
	 * Returns a rectangle copy of the key indexed by i.
	 * @param i
	 * @return
	 */
	public Rect getKeyCopy(int i) {
		if (0 <= i && i < keys.length) {
			return new Rect(keys[i]);
		}
		else {
			System.err.println("index out of range in call to Piano.getKeyCopy(" + i + ")");
			return null;
		}
	}
	
	/**
	 * Returns the center coordinate of the key indexed by i.
	 * @param i
	 * @return
	 */
	public Point getKeyCenter(int i) {
		if (0 <= i && i < keys.length) {
			return new Point(keys[i].getCenx(), keys[i].getCeny());
		}
		else {
			System.err.println("index out of range in call to Piano.getKeyCenter(" + i + ")");
			return null;
		}
	}
	
	/**
	 * Returns a rectangle the same size as the white keys in this piano.
	 * The rectangle will be positioned with its upper left corner at (0, 0).
	 * @return
	 */
	public Rect getWhiteKeyCopy() {
		if (whiteKeys.length > 0) {
			Rect copy = new Rect(whiteKeys[0]);
			copy.setX1(0);
			copy.setY1(0);
			return copy;
		}
		else {
			return null;
		}
	}
	
	/**
	 * Returns a rectangle the same size as the black keys in this piano.
     * The rectangle will be positioned with its upper left corner at (0, 0).
	 * @return
	 */
	public Rect getBlackKeyCopy() {
		if (blackKeys.length > 0) {
			Rect copy = new Rect(blackKeys[0]);
			copy.setX1(0);
			copy.setY1(0);
			return copy;
		}
		else {
			return null;
		}
	}
	
	/**
	 *
	 * @param midiPitch The midi pitch value
	 * @return True if the given midi pitch value cooresponds to a white piano key, false otherwise.
	 */
	public static boolean isWhiteKey(int midiPitch) {
		midiPitch %= 12;
		return !isBlackKey(midiPitch);
	}
	
	/**
	 * 
	 * @param midiPitch The midi pitch value
	 * @return True if the given midi pitch value cooresponds to a black piano key, false otherwise.
	 */
	public static boolean isBlackKey(int i) {
		i %= 12;
		return i == 1 || i == 3 || i == 6 || i == 8 || i == 10;
	}
}
