package geom;

import processing.core.PApplet;

public class Rect {
    private float x1, y1, width, height;
    
    public Rect(Rect rect) {
    	this.x1 = rect.x1;
    	this.y1 = rect.y1;
    	this.width = rect.width;
    	this.height = rect.height;
    }
    
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
    
    public void display(PApplet pa) {
        pa.rectMode(pa.CORNER);
        pa.rect(x1, y1, width, height);
    }
    
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
    
    public boolean intersects(float x, float y) {
        return (x1 <= x && x <= x1 + width &&
                y1 <= y && y <= y1 + height);
    }
    
    public float getX1() {
        return x1;
    }
    
    public float getY1() {
        return y1;
    }
    
    public float getX2() {
        return x1 + width;
    }
    
    public float getY2() {
        return y1 + height;
    }

	public float getCenx() {
		return x1 + width/2f;
	}

	public void setX1(float x1) {
		this.x1 = x1;
	}

	public float getCeny() {
		return y1 + height/2f;
	}

	public void setY1(float y1) {
		this.y1 = y1;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}
	
	public void translate(float dx, float dy) {
		x1 += dx;
		y1 += dy;
	}
	
	public static void leftShift(Rect[] xs) {
	    if (xs.length > 0) {
	        int i = xs.length-1;
	        Rect next = xs[i];
	        xs[i] = xs[0];
	        i--;
	        while (i >= 0) {
	        	Rect temp = xs[i];
	            xs[i] = next;
	            next = temp;
	            i--;
	        }
	    }
	}

	public static void rightShift(Rect[] xs) {
	    if (xs.length > 0) {
	        int i=0;
	        Rect prev = xs[i];
	        xs[i] = xs[xs.length-1];
	        i++;
	        while (i < xs.length) {
	        	Rect temp = xs[i];
	            xs[i] = prev;
	            prev = temp;
	            i++;
	        }
	    }
	}
}