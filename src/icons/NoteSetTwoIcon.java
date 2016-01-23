package icons;

import phases.PhasesPApplet;
import processing.core.PApplet;

public class NoteSetTwoIcon implements Icon {
	private int noteType;
    
    public NoteSetTwoIcon(int noteType) {
        this.noteType = noteType;
    }
  
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        switch(noteType) {
	        case DOTS:
	            pa.noStroke();
	            pa.fill(0);
	            pa.ellipseMode(pa.CENTER);
	            pa.ellipse(x, y, radius*0.5f, radius*0.5f);
	            break;
	        case RECTS:
	        	pa.noStroke();
	            pa.fill(0);
	            pa.rectMode(pa.CENTER);
	            pa.rect(x, y, radius*0.9f, radius*0.35f);
	            break;
        }
        
    }
}