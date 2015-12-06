package views;

import phases.Rect;
import processing.core.PApplet;

public class Piano extends Rect {
	//independent parameter
	private int numOctaves;
	private boolean facePositive;
	//keys
	private Rect[] whiteKeys, blackKeys;

	public Piano(int numOctaves, Rect rect, boolean facePositive) {
		super(rect);
		
		this.numOctaves = numOctaves;
		this.facePositive = facePositive;
		
		initArrays();
		initRects();
	}
	
	private void initArrays() {
		int numWhiteKeys = numOctaves * 7;
		int numBlackKeys = numOctaves * 5;
		whiteKeys = new Rect[numWhiteKeys];
		blackKeys = new Rect[numBlackKeys];
	}
	
	private void initRects() {
		//dependent parameters
		int numKeys = numOctaves * 12;
		float whiteKeyWidth = (getWidth() > getHeight()) ? (getWidth()-1) / (numOctaves*7f) : 
			                                                getWidth();
		
		float whiteKeyHeight = (getHeight() > getWidth()) ? (getHeight()-1) / (numOctaves*7f) :
			                                                 getHeight();
		float blackKeyWidth = whiteKeyWidth * 0.625f;
		float blackKeyHeight = whiteKeyHeight * 0.625f;
		
		float x1 = this.getX1();
		float y1 = this.getY1();
		int j=0; //looping variable for whiteKeys
		int k=0; //looping variable for blackKeys
		for (int i=0; i<numKeys; i++) { //looping variable for all keys
			//init white keys
			whiteKeys[j++] = new Rect(x1, y1, whiteKeyWidth, whiteKeyHeight, PApplet.CORNER);
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
		pa.strokeWeight(1);
		pa.stroke(100);
		
		//draw white keys
		pa.noFill();
		for (int i=0; i<whiteKeys.length; i++) {
			whiteKeys[i].display(pa);
		}
			
		//draw black keys
		pa.fill(100);
		for (int i=0; i<blackKeys.length; i++) {
			blackKeys[i].display(pa);
		}
	}
	
	public void setWidth(float width) {
		super.setWidth(width);
		initRects();
	}
	
	public void setHeight(float height) {
		super.setHeight(height);
		initRects();
	}
	
	public int getNumOctaves() {
		return numOctaves;
	}
	
	public void setNumOctaves(int numOctaves) {
		this.numOctaves = numOctaves;
		initArrays();
		initRects();
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
}
