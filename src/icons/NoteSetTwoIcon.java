package icons;

import phasing.PhasesPApplet;
import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class NoteSetTwoIcon implements Icon {
	private final int noteType;
    
    public NoteSetTwoIcon(int noteType) {
        this.noteType = noteType;
    }
  
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        switch(noteType) {
	        case DOTS2:
	            pa.noStroke();
	            pa.fill(0);
	            pa.ellipseMode(pa.CENTER);
	            pa.ellipse(x, y, radius*0.5f, radius*0.5f);
	            break;
	        case RECTS2:
	        	pa.noStroke();
	            pa.fill(0);
	            pa.rectMode(pa.CENTER);
	            pa.rect(x, y, radius*0.9f, radius*0.35f);
	            break;
        }
        
    }
}