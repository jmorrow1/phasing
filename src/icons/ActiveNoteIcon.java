package icons;

import phases.PhasesPApplet;

public class ActiveNoteIcon implements Icon {
	private int value;
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) { 
        int quarterNoteSize = (int)radius;
        
        if (value == ONLY_SHOW_ACTIVE_NOTE || value == SHOW_ACTIVE_NOTE) {
            pa.fill(PhasesPApplet.getColor2());
        }
        else {
            pa.fill(0);
        }
        pa.quarterNote(x, y, quarterNoteSize);
        
        if (value == ONLY_SHOW_ACTIVE_NOTE) {
            pa.fill(0, 50);
        }
        else {
            pa.fill(0);
        }
        pa.quarterNote(x - radius*0.65f, y, quarterNoteSize);
        pa.quarterNote(x + radius*0.65f, y, quarterNoteSize);
        
        if (value == SHOW_LINE_AT_ACTIVE_NOTE) {
            pa.strokeWeight(4);
            pa.stroke(PhasesPApplet.getColor2());
            pa.line(x, y - radius/2f, x, y + radius);
        }
    }
}