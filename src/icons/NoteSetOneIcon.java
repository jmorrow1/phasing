package icons;

import phases.PhasesPApplet;
import processing.core.PApplet;

public class NoteSetOneIcon implements Icon {
    private final int noteType;
    
    public NoteSetOneIcon(int noteType) {
        this.noteType = noteType;
    }
  
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        switch(noteType) {
	        case SYMBOLS:
	            pa.fill(0);
	            pa.textFont(pa.pfont42);
	            pa.textSize(radius);
	            pa.textAlign(pa.CENTER, pa.CENTER);
	            pa.text("A", x, y);
	            break;
	        case DOTS1:
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
        		pa.drawSineWave(x, y, 1.5f*radius, 0.5f*radius);
	        	break;
        }
        
    }
}