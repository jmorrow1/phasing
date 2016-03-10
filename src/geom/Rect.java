package geom;

import processing.core.PApplet;

/**
 * Represents a rectangle.
 * 
 * @author James Morrow
 *
 */
public class Rect extends Shape {
    private float x, y, width, height;
    private float area;
    
    /**
     * Copy constructor
     * @param rect The rect to copy
     */
    public Rect(Rect rect) {
    	this.x = rect.x;
    	this.y = rect.y;
    	this.width = rect.width;
    	this.height = rect.height;
    	area = width * height;
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
            this.x = a;
            this.y = b;
            
        }
        else if (rectMode == PApplet.RADIUS) {
        	this.width = 2*c;
            this.height = 2*d;
            this.x = a;
            this.y = b;
            
        }
        else if (rectMode == PApplet.CORNER) {
            this.width = c;
            this.height = d;  
            this.x = a + width/2;
            this.y = b + height/2;
        }
        else if (rectMode == PApplet.CORNERS) {
            this.width = c - a;
            this.height = d - b;
            this.x = a + width/2;
            this.y = b + height/2;
        }
        area = width * height;
    }
    
    /**
     * Displays the rect.
     * @param pa The PApplet to draw to
     */
    public void display(PApplet pa) {
        pa.rectMode(pa.CENTER);
        pa.rect(x, y, width, height);
    }
    
    /**
     *
     * @param x The x-coordinate of the point
     * @param y The y-coordinate of the point
     * @return True if the point (x, y) is wihtin the rect, false otherwise
     */
    public boolean touches(float x, float y) {
        return (getX1() <= x && x <= getX2() &&
                getY1() <= y && y <= getY2());
    }
    
    /**
     * 
     * @return the leftmost x-coordinate of the rect
     */
    public float getX1() {
        return x - width/2;
    }
    
    /**
     * 
     * @return the uppermost y-coordinate of the rect
     */
    public float getY1() {
        return y - height/2;
    }
    
    /**
     * 
     * @return the rightmost x-coordinate of the rect
     */
    public float getX2() {
        return x + width/2;
    }
    
    /**
     * 
     * @return the bottommost y-coordinate of the rect
     */
    public float getY2() {
        return y + height/2;
    }

    /**
     * 
     * @return the center x-coordinate of the rect
     */
	public float getCenx() {
		return x;
	}
	
	/**
	 * 
	 * @return The center of this rectangle.
	 */
	public Point getCenter() {
		return new Point(x, y);
	}
	
	/**
	 * 
	 * @param x The new center x-coordinate for this rectangle.
	 * @param y The new center y-coordinate for this rectangle.
	 */
	public void setCenter(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 
	 * @param x1 the new leftmost x-coordinate for the rect
	 */
	public void setX1(float x1) {
		x = x1 + width/2;
	}

	/**
	 * 
	 * @return the center y-coordinate of the rect
	 */
	public float getCeny() {
		return y;
	}

	/**
	 * 
	 * @param y1 the new uppermost y-coordinate for the rect
	 */
	public void setY1(float y1) {
		y = y1 + height/2;
	}

	/**
	 * 
	 * @return the width of the rect
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Sets the width, keeping the center of the Rect in place.
	 * @param the new width for the rect
	 */
	public void setWidth(float width) {
		this.width = width;
		area = width * height;
	}

	/**
	 * Sets the height, keeping the center of the Rect in place.
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
		area = width * height;
	}
	
	/**
	 * 
	 * @return The area of the rect
	 */
	public float getArea() {
		return area;
	}
	
	@Override
	public void translate(float dx, float dy) {
		x += dx;
		y += dy;
	}
	
	@Override
	public Rect clone() {
		return new Rect(this);
	}
}