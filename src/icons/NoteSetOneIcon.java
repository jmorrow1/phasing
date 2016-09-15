package icons;

import phasing.PhasesPApplet;
import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class NoteSetOneIcon implements Icon {
    private final int noteType;
    
    public NoteSetOneIcon(int noteType) {
        this.noteType = noteType;
    }
  
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        switch(noteType) {
	        case SYMBOLS:
	            pa.fill(0);
	            pa.textFont(pa.pfont24);
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
	        case LINE_SEGMENTS:
	        	pa.strokeWeight(radius/10f);
	        	pa.stroke(0);
	            pa.line(x - radius*0.5f, y, x + radius*0.5f, y);
	            break;
	        case RECTS1:
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
    
    public static int numTypes() {
    	return numNoteGraphicSet1s;
    }
}