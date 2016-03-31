package geom;

import processing.core.PApplet;

/**
 * Vector graphic computer mouse image, defined in terms of Java/Processing code.
 * 
 * @author James Morrow
 *
 */
public class Mouse {
    private float cenx, ceny, w, h, x1, y1, x2, y2, upperCurveHeight, lowerCurveHeight, ay, by, cy;
    
    public Mouse(float cenx, float ceny, float w, float h) {
        set(cenx, ceny, w, h);
    }
    
    public static void draw(float cenx, float ceny, float w, float h, int strokeColor, PApplet pa) {
    	//corners
        float x1 = cenx - w/2f;
        float y1 = ceny - h/2f;
        float x2 = cenx + w/2f;
        float y2 = ceny + h/2f;
        
        //curve heights
        float upperCurveHeight = h * 0.2f;
        float lowerCurveHeight = h * 0.3f;
        
        //vertical lines
        float ay = y1 + upperCurveHeight;
        float cy = y2 - lowerCurveHeight;
        
        //buttons
        float by = y1 + 0.35f * h;
    	
    	pa.stroke(strokeColor);
    	pa.noFill();
    	
        //vertical pa.lines
        pa.line(x1, ay, x1, cy);
        pa.line(x2, ay, x2, cy);
        
        //curves
        pa.ellipseMode(pa.RADIUS);
        pa.arc(cenx, ay, w/2f, upperCurveHeight, pa.PI, pa.TWO_PI);
        pa.arc(cenx, cy, w/2f, lowerCurveHeight, 0, pa.PI);
        
        //buttons
        pa.line(x1, by, x2, by);
        pa.line(cenx, y1, cenx, by);
    }
    
    public void draw(int strokeColor, PApplet pa) {
    	pa.stroke(strokeColor);
    	pa.noFill();
    	
        //vertical pa.lines
        pa.line(x1, ay, x1, cy);
        pa.line(x2, ay, x2, cy);
        
        //curves
        pa.ellipseMode(pa.RADIUS);
        pa.arc(cenx, ay, w/2f, upperCurveHeight, pa.PI, pa.TWO_PI);
        pa.arc(cenx, cy, w/2f, lowerCurveHeight, 0, pa.PI);
        
        //buttons
        pa.line(x1, by, x2, by);
        pa.line(cenx, y1, cenx, by);
    }
    
    public static void displayLeftButton(float cenx, float ceny, float w, float h, PApplet pa) {
    	//corners
        float x1 = cenx - w/2f;
        float y1 = ceny - h/2f;
        float x2 = cenx + w/2f;
        float y2 = ceny + h/2f;
        
        //curve heights
        float upperCurveHeight = h * 0.2f;
        float lowerCurveHeight = h * 0.3f;
        
        //vertical lines
        float ay = y1 + upperCurveHeight;
        float cy = y2 - lowerCurveHeight;
        
        //buttons
        float by = y1 + 0.35f * h;
    	
    	pa.beginShape();
        pa.vertex(cenx, y1);
        pa.vertex(cenx, by);
        pa.vertex(x1, by);
        pa.vertex(x1, ay); 
        
        int numIts = (int)w/2;
        float theta = pa.PI;
        float dTheta = pa.HALF_PI / numIts;
        for (int i=0; i<numIts; i++) {
            pa.vertex(cenx + w/2*pa.cos(theta), ay + upperCurveHeight*pa.sin(theta));
            theta += dTheta;
        }
        
        pa.endShape(pa.CLOSE);
    }
    
    public void displayLeftButton(PApplet pa) {
        pa.beginShape();
        pa.vertex(cenx, y1);
        pa.vertex(cenx, by);
        pa.vertex(x1, by);
        pa.vertex(x1, ay); 
        
        int numIts = (int)w/2;
        float theta = pa.PI;
        float dTheta = pa.HALF_PI / numIts;
        for (int i=0; i<numIts; i++) {
            pa.vertex(cenx + w/2*pa.cos(theta), ay + upperCurveHeight*pa.sin(theta));
            theta += dTheta;
        }
        
        pa.endShape(pa.CLOSE);
    }
    
    public static void displayRightButton(float cenx, float ceny, float w, float h, PApplet pa) {
    	//corners
        float x1 = cenx - w/2f;
        float y1 = ceny - h/2f;
        float x2 = cenx + w/2f;
        float y2 = ceny + h/2f;
        
        //curve heights
        float upperCurveHeight = h * 0.2f;
        float lowerCurveHeight = h * 0.3f;
        
        //vertical lines
        float ay = y1 + upperCurveHeight;
        float cy = y2 - lowerCurveHeight;
        
        //buttons
        float by = y1 + 0.35f * h;
        
        pa.beginShape();
        pa.vertex(cenx, by);
        pa.vertex(cenx, ay);
        
        int numIts = (int)w/2;
        float theta = 1.5f * pa.PI;
        float dTheta = pa.HALF_PI / numIts;
        for (int i=0; i<numIts; i++) {
            pa.vertex(cenx + w/2*pa.cos(theta), ay + upperCurveHeight*pa.sin(theta));
            theta += dTheta;
        }
   
        pa.vertex(x2, ay); 
        pa.vertex(x2, by);
        
        pa.endShape(pa.CLOSE);
    }
    
    public void displayRightButton(PApplet pa) {
        pa.beginShape();
        pa.vertex(cenx, by);
        pa.vertex(cenx, ay);
        
        int numIts = (int)w/2;
        float theta = 1.5f * pa.PI;
        float dTheta = pa.HALF_PI / numIts;
        for (int i=0; i<numIts; i++) {
            pa.vertex(cenx + w/2*pa.cos(theta), ay + upperCurveHeight*pa.sin(theta));
            theta += dTheta;
        }
   
        pa.vertex(x2, ay); 
        pa.vertex(x2, by);
        
        pa.endShape(pa.CLOSE);
    }
    
    public void set(float cenx, float ceny, float w, float h) {
        this.cenx = cenx;
        this.ceny = ceny;
        this.w = w;
        this.h = h;
        
        //corners
        x1 = cenx - w/2f;
        y1 = ceny - h/2f;
        x2 = cenx + w/2f;
        y2 = ceny + h/2f;
        
        //curve heights
        upperCurveHeight = h * 0.2f;
        lowerCurveHeight = h * 0.3f;
        
        //vertical lines
        ay = y1 + upperCurveHeight;
        cy = y2 - lowerCurveHeight;
        
        //buttons
        by = y1 + 0.35f * h;
    }
    
    public void setCen(float cenx, float ceny) {
        set(cenx, ceny, w, h);
    }
    
    public void setWidth(float w) {
        set(cenx, ceny, w, h);
    }
    
    public void setHeight(float h) {
        set(cenx, ceny, w, h);
    }
}