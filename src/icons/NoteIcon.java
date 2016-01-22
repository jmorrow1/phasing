package icons;

import phases.PhasesPApplet;
import processing.core.PApplet;

public class NoteIcon implements Icon {
    private int noteType;
    
    //parameters for sine wave graphic:
    private static final int NUM_PTS = 100;
    private static final float dTheta = PApplet.TWO_PI / NUM_PTS;
    
    public NoteIcon(int noteType) {
        this.noteType = noteType;
    }
  
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        switch(noteType) {
	        case SYMBOLS:
	            pa.fill(0);
	            pa.textSize(radius);
	            pa.textAlign(pa.CENTER, pa.CENTER);
	            pa.text("A", x, y);
	            break;
	        case DOTS:
	            pa.noStroke();
	            pa.fill(0);
	            pa.ellipseMode(pa.CENTER);
	            pa.ellipse(x, y, radius*0.5f, radius*0.5f);
	            break;
	        case CONNECTED_DOTS: 
	            pa.noStroke();
	            pa.fill(0);
	            pa.ellipseMode(pa.CENTER);
	            pa.ellipse(x - radius*0.4f, y, radius*0.4f, radius*0.4f);
	            pa.ellipse(x + radius*0.4f, y, radius*0.4f, radius*0.4f);
	            pa.strokeWeight(radius/20f);
	            pa.stroke(0);
	            pa.line(x - radius*0.5f, y, x + radius*0.5f, y);
	            break;
	        case RECTS_OR_SECTORS:
	        	pa.noStroke();
	            pa.fill(0);
	            pa.rectMode(pa.CENTER);
	            pa.rect(x, y, radius*0.9f, radius*0.35f);
	            break;
	        case SINE_WAVE:
	        	pa.strokeWeight(radius/6f);
	        	pa.stroke(0);
        		float theta = 0;
        		float ptx = x - radius*0.75f;
        		float dx = (1.5f*radius) / NUM_PTS;
        		float amp = radius*0.5f;
        		for (int i=0; i<NUM_PTS; i++) {
        			pa.point(ptx, y + amp*pa.sin(theta));
        			ptx += dx;
        			theta += dTheta;
        		}	
	        	break;
        }
        
    }
}