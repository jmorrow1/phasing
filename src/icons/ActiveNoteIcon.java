package icons;

import phases.PhasesPApplet;
import phases.Option.ActiveNote;

public class ActiveNoteIcon extends Icon {
    public ActiveNoteIcon(int value) {
        super(value);
    }
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) { 
        int quarterNoteSize = (int)radius;
        
        if (value == ActiveNote.ONLY_SHOW_ACTIVE_NOTE || value == ActiveNote.SHOW_ACTIVE_NOTE) {
            pa.fill(PhasesPApplet.getColor2());
        }
        else {
            pa.fill(0);
        }
        pa.quarterNote(x, y, quarterNoteSize);
        
        if (value == ActiveNote.ONLY_SHOW_ACTIVE_NOTE) {
            pa.fill(0, 50);
        }
        else {
            pa.fill(0);
        }
        pa.quarterNote(x - radius*0.65f, y, quarterNoteSize);
        pa.quarterNote(x + radius*0.65f, y, quarterNoteSize);
        
        if (value == ActiveNote.SHOW_LINE_AT_ACTIVE_NOTE) {
            pa.strokeWeight(4);
            pa.stroke(PhasesPApplet.getColor2());
            pa.line(x, y - radius/2f, x, y + radius);
        }
    }
}