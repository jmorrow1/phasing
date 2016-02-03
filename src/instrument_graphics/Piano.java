package instrument_graphics;

import geom.Point;
import geom.Polygon;
import geom.Rect;
import geom.Shape;
import phases.PhasesPApplet;
import processing.core.PApplet;

/**
 * A graphical representation of a piano.
 * 
 * @author James Morrow
 *
 */
public class Piano extends Rect implements Instrument {
	//independent parameters
	private final int numOctaves;
	private boolean facePositive;
	//keys
	private Polygon[] whiteKeys;
	private Rect[] blackKeys;
	private Shape[] keys;
	private int blackKeyColor;
	
	public Piano(int numOctaves, Rect rect, boolean facePositive, int blackKeyColor) {
		super(rect);
		
		this.numOctaves = numOctaves;
		this.facePositive = facePositive;
		
		if (numOctaves >= 0) {
			initArrays();
		}
		if (numOctaves > 0) {
			initKeys();
		}
		
		this.blackKeyColor = blackKeyColor;
	}
	
	private void initArrays() {
		int numKeys = numOctaves * 12;
		int numWhiteKeys = numOctaves * 7;
		int numBlackKeys = numOctaves * 5;
		keys = new Shape[numKeys];
		whiteKeys = new Polygon[numWhiteKeys];
		blackKeys = new Rect[numBlackKeys];
	}
	
	private Polygon initWhiteKey(int keyIndex, float x1, float y1, float whiteKeyWidth, float whiteKeyHeight, float blackKeyWidth, float blackKeyHeight) {
		boolean leftNeighborIsBlack = Instrument.isBlackKey(keyIndex-1);
		boolean rightNeighborIsBlack = Instrument.isBlackKey(keyIndex+1);
		
		float x2 = x1 + blackKeyWidth/2f;
		float x3 = x1 + whiteKeyWidth - blackKeyWidth/2f;
		float x4 = x1 + whiteKeyWidth;
		float y2 = y1 + blackKeyHeight;
		float y3 = y1 + whiteKeyHeight;	
		
		if (leftNeighborIsBlack && !rightNeighborIsBlack) {
			return new Polygon(new float[] {x2, y1, x4, y1, x4, y3, x1, y3, x1, y2, x2, y2});
		}
		else if (!leftNeighborIsBlack && rightNeighborIsBlack) {
			return new Polygon(new float[] {x1, y1, x3, y1, x3, y2, x4, y2, x4, y3, x1, y3});	
		}
		else if (leftNeighborIsBlack && rightNeighborIsBlack) {
			return new Polygon(new float[] {x2, y1, x3, y1, x3, y2, x4, y2, x4, y3, x1, y3, x1, y2, x2, y2});
		}
		else {
			return null;
		}	
	}
	
	private void initKeys() {
		//dependent parameters
		int numKeys = numOctaves * 12;
		float divisor = numOctaves*7f;
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
			whiteKeys[j++] = initWhiteKey(i, x1, y1, whiteKeyWidth, whiteKeyHeight, blackKeyWidth, blackKeyHeight);
			keys[i] = whiteKeys[j-1];
			//init black keys
			if (i % 12 != 4 && i % 12 != 11) {
				if (getWidth() > getHeight()) {
					blackKeys[k++] = new Rect(x1 + whiteKeyWidth - blackKeyWidth/2f, y1, 
							                  blackKeyWidth, blackKeyHeight, PApplet.CORNER);	
					if (!facePositive) {
						blackKeys[k-1].translate(0, whiteKeyHeight-blackKeyHeight);
					}
				}
				else {
					blackKeys[k++] = new Rect(x1, y1 + whiteKeyHeight - blackKeyHeight/2f, 
			                                  blackKeyWidth, blackKeyHeight, PApplet.CORNER);
					if (!facePositive) {
						blackKeys[k-1].translate(whiteKeyWidth-blackKeyWidth, 0);
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
	
	private void drawWhiteKeys(PApplet pa) {
		pa.strokeWeight(1);
		pa.stroke(100);
		pa.fill(255);
		for (int i=0; i<whiteKeys.length; i++) {
			whiteKeys[i].display(pa);
		}
	}
	
	private void drawBlackKeys(PApplet pa) {
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
			initKeys();
		}
	}
	
	public void setHeight(float height) {
		super.setHeight(height);
		if (numOctaves > 0) {
			initKeys();
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
	
	public boolean getFacePositive() {
		return facePositive;
	}
	
	public void setFacePositive(boolean value) {
		if (facePositive != value) {
			facePositive = value;
			initKeys();
		}
	}
	
	/**
	 * Returns a rectangle copy of the key indexed by i.
	 * @param i
	 * @return
	 */
	public Shape getShapeAtNoteIndex(int i) {
		if (0 <= i && i < keys.length) {
			return keys[i].clone();
		}
		else {
			System.err.println("index out of range in call to Piano.getKeyCopy(" + i + ")");
			return null;
		}
	}
	
	/**
	 * Returns a rectangle the same size as the white keys in this piano.
	 * The rectangle will be positioned with its center at (0, 0).
	 * @return
	 */
	public Polygon getWhiteKeyCopy() {
		if (whiteKeys.length > 0) {
			Polygon copy = new Polygon(whiteKeys[0]);
			copy.setCenter(0, 0);
			return copy;
		}
		else {
			return null;
		}
	}
	
	/**
	 * Returns a rectangle the same size as the black keys in this piano.
     * The rectangle will be positioned with its center at (0, 0).
	 * @return
	 */
	public Rect getBlackKeyCopy() {
		if (blackKeys.length > 0) {
			Rect copy = new Rect(blackKeys[0]);
			copy.setCenter(0, 0);
			return copy;
		}
		else {
			return null;
		}
	}
}
