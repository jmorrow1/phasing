package icons;

import phases.PhasesPApplet;

public class ShowActiveNoteIcon extends Icon {
    final int SHOW_ACTIVE_NOTE=0, ONLY_SHOW_ACTIVE_NOTE=1, DONT_SHOW_ACTIVE_NOTE=2, SHOW_LINE_AT_NOTE=3;
    private int showState;
  
    public ShowActiveNoteIcon(int showState) {
        this.showState = showState;
    }
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) { 
        int quarterNoteSize = (int)radius;
        
        if (showState == ONLY_SHOW_ACTIVE_NOTE || showState == SHOW_ACTIVE_NOTE) {
            pa.fill(PhasesPApplet.getColor2());
        }
        else {
            pa.fill(0);
        }
        pa.quarterNote(x, y, quarterNoteSize);
        
        if (showState == ONLY_SHOW_ACTIVE_NOTE) {
            pa.fill(0, 50);
        }
        else {
            pa.fill(0);
        }
        pa.quarterNote(x - radius*0.65f, y, quarterNoteSize);
        pa.quarterNote(x + radius*0.65f, y, quarterNoteSize);
        
        if (showState == SHOW_LINE_AT_NOTE) {
            pa.strokeWeight(4);
            pa.stroke(PhasesPApplet.getColor2());
            pa.line(x, y - radius/2f, x, y + radius);
        }
    }
}