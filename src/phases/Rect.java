package phases;

import processing.core.PApplet;

public class Rect {
    private float cenx, ceny, width, height;
    
    public Rect(Rect rect) {
    	this.cenx = rect.cenx;
    	this.ceny = rect.ceny;
    	this.width = rect.width;
    	this.height = rect.height;
    }
    
    public Rect(float a, float b, float c, float d, int rectMode) {
        if (rectMode == PApplet.CENTER) {
            this.cenx = a;
            this.ceny = b;
            this.width = c;
            this.height = d;
        }
        else if (rectMode == PApplet.RADIUS) {
            this.cenx = a;
            this.ceny = b;
            this.width = 2*c;
            this.height = 2*d;
        }
        else if (rectMode == PApplet.CORNER) {
            this.width = c;
            this.height = d;
            this.cenx = a + 0.5f*width;
            this.ceny = b + 0.5f*height;
        }
        else if (rectMode == PApplet.CORNERS) {
            this.width = c - a;
            this.height = d - b;
            this.cenx = a + 0.5f*width;
            this.ceny = b + 0.5f*height;
        }
    }
    
    public void display(PApplet pa) {
        pa.rectMode(pa.CENTER);
        pa.rect(cenx, ceny, width, height);
    }
    
    public boolean intersects(float x, float y) {
        return (cenx - width/2 <= x && x <= cenx + width/2 &&
                ceny - height/2 <= y && y <= ceny + height/2);
    }
    
    public float getX1() {
        return cenx - width/2;
    }
    
    public float getY1() {
        return ceny - height/2;
    }
    
    public float getX2() {
        return cenx + width/2;
    }
    
    public float getY2() {
        return ceny + height/2;
    }

	public float getCenx() {
		return cenx;
	}

	public void setCenx(float cenx) {
		this.cenx = cenx;
	}

	public float getCeny() {
		return ceny;
	}

	public void setCeny(float ceny) {
		this.ceny = ceny;
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
}