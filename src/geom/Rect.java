package geom;

import phases.PhasesPApplet;
import processing.core.PApplet;

public class Rect {
    private double x1, y1, width, height;
    
    public Rect(Rect rect) {
    	this.x1 = rect.x1;
    	this.y1 = rect.y1;
    	this.width = rect.width;
    	this.height = rect.height;
    }
    
    public Rect(double a, double b, double c, double d, int rectMode) {
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
    
    public void display(PhasesPApplet pa) {
        pa.rectMode(pa.CORNER);
        pa.rect(x1, y1, width, height);
    }
    
    public boolean intersects(double x, double y) {
        return (x1 <= x && x <= x1 + width &&
                y1 <= y && y <= y1 + height);
    }
    
    public double getX1() {
        return x1;
    }
    
    public double getY1() {
        return y1;
    }
    
    public double getX2() {
        return x1 + width;
    }
    
    public double getY2() {
        return y1 + height;
    }

	public double getCenx() {
		return x1 + width/2f;
	}

	public void setX1(double x1) {
		this.x1 = x1;
	}

	public double getCeny() {
		return y1 + height/2f;
	}

	public void setY1(double y1) {
		this.y1 = y1;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}
	
	public void translate(double dx, double dy) {
		x1 += dx;
		y1 += dy;
	}
}