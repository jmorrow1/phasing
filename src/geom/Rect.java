package geom;

import processing.core.PApplet;

/**
 * Represents a rectangle.
 * 
 * @author James Morrow
 *
 */
public class Rect extends Shape {
    private float x1, y1, width, height;
    
    /**
     * Copy constructor
     * @param rect The rect to copy
     */
    public Rect(Rect rect) {
    	this.x1 = rect.x1;
    	this.y1 = rect.y1;
    	this.width = rect.width;
    	this.height = rect.height;
    }
    
    /**
     * The rectMode argument behaves analogously to Processing's rectMode method.
     * 
     * In other words, the following Java code...
     * Rect r = new Rect(a, b, c, d, rectMode);
     * r.display(pa);
     * 
     * ...is behaviorally equivalent to the Processing code:
     * rectMode(rectMode);
     * rect(a, b, c, d);
     * 
     * @param a
     * @param b
     * @param c
     * @param d
     * @param rectMode
     */
    public Rect(float a, float b, float c, float d, int rectMode) {
        if (rectMode == PApplet.CENTER) {
        	this.width = c;
            this.height = d;
            this.x1 = a - width/2f;
            this.y1 = b - height/2f;
            
        }
        else if (rectMode == PApplet.RADIUS) {
        	this.width = 2*c;
            this.height = 2*d;
            this.x1 = a - width/2f;
            this.y1 = b - height/2f;
            
        }
        else if (rectMode == PApplet.CORNER) {
        	this.x1 = a;
            this.y1 = b;
            this.width = c;
            this.height = d;        
        }
        else if (rectMode == PApplet.CORNERS) {
        	this.x1 = a;
            this.y1 = b;
            this.width = c - a;
            this.height = d - b;
        }
    }
    
    /**
     * Displays the rect.
     * @param pa The PApplet to draw to
     */
    public void display(PApplet pa) {
        pa.rectMode(pa.CORNER);
        pa.rect(x1, y1, width, height);
    }
    
    /**
     * Displays the rect, wrapping it to the specified x-range if it is partially within the x-range.
     * @param pa The PApplet to draw to
     * @param x1 The minimum x-coordinate of the range
     * @param x2 The minimum x-coordinate of the range
     */
    public void displayHorizontallyWrapped(PApplet pa, float x1, float x2) {
    	pa.rectMode(pa.CORNER);
    	if (this.x1 < x1 && this.getX2() > x1) {
    		float surplusWidth = x1 - this.x1;
    		pa.rect(x1, this.y1, this.width-surplusWidth, this.height);
    		pa.rect(x2 - surplusWidth, this.y1, surplusWidth, this.height);
    	}
    	else if (this.getX2() > x2 && this.getX1() < x2) {
    		float surplusWidth = this.getX2() - x2;
    		pa.rect(this.x1, this.y1, this.width-surplusWidth, this.height);
    		pa.rect(x1, this.y1, surplusWidth, this.height);
    	}
    	else {
    		display(pa);
    	}
    }
    
    /**
     * Displays the rect, wrapping it to the specified y-range if it is partially within the y-range.
     * @param pa The PApplet to draw to
     * @param y1 The minimum y-coordinate of the range
     * @param y2 The minimum y-coordinate of the range
     */
    public void displayVerticallyWrapped(PApplet pa, float y1, float y2) {
    	pa.rectMode(pa.CORNER);
    	if (this.y1 < y1 && this.getY2() > y1) {
    		float surplusHeight = y1 - this.y1;
    		pa.rect(this.x1, y1, this.width, this.height-surplusHeight);
    		pa.rect(this.x1, y2 - surplusHeight, this.width, surplusHeight);
    	}
    	else if (this.getY2() > y2 && this.getY1() < y2) {
    		float surplusHeight = this.getY2() - y2;
    		pa.rect(this.x1, this.y1, this.width, this.height-surplusHeight);
    		pa.rect(this.x1, y1, this.width, surplusHeight);
    	}
    	else {
    		display(pa);
    	}
    }
    
    /**
     *
     * @param x The x-coordinate of the point
     * @param y The y-coordinate of the point
     * @return True if the point (x, y) is wihtin the rect, false otherwise
     */
    public boolean touches(float x, float y) {
        return (x1 <= x && x <= x1 + width &&
                y1 <= y && y <= y1 + height);
    }
    
    /**
     * 
     * @return the leftmost x-coordinate of the rect
     */
    public float getX1() {
        return x1;
    }
    
    /**
     * 
     * @return the uppermost y-coordinate of the rect
     */
    public float getY1() {
        return y1;
    }
    
    /**
     * 
     * @return the rightmost x-coordinate of the rect
     */
    public float getX2() {
        return x1 + width;
    }
    
    /**
     * 
     * @return the bottommost y-coordinate of the rect
     */
    public float getY2() {
        return y1 + height;
    }

    /**
     * 
     * @return the center x-coordinate of the rect
     */
	public float getCenx() {
		return x1 + width/2f;
	}
	
	public Point getCenter() {
		return new Point(getCenx(), getCeny());
	}
	
	public void setCenter(float x, float y) {
		translate(x - getCenx(), y - getCeny());
	}

	/**
	 * 
	 * @param x1 the new rightmost x-coordinate for the rect
	 */
	public void setX1(float x1) {
		this.x1 = x1;
	}

	/**
	 * 
	 * @return the center y-coordinate of the rect
	 */
	public float getCeny() {
		return y1 + height/2f;
	}

	/**
	 * 
	 * @param y1 the new uppermost y-coordinate for the rect
	 */
	public void setY1(float y1) {
		this.y1 = y1;
	}

	/**
	 * 
	 * @return the width of the rect
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * 
	 * @param the new width for the rect
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * 
	 * @return the height of the rect
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * 
	 * @param height the new height for the rect
	 */
	public void setHeight(float height) {
		this.height = height;
	}
	
	@Override
	public void translate(float dx, float dy) {
		x1 += dx;
		y1 += dy;
	}
	
	@Override
	public Rect clone() {
		return new Rect(this);
	}
}