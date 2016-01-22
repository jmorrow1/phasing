package icons;

import phases.PhasesPApplet;

public class ShowActiveNoteIcon implements Icon {
    private int showState;
  
    public ShowActiveNoteIcon(int showState) {
        this.showState = showState;
    }
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) { 
        int quarterNoteSize = (int)(radius*1.5f);
        
        if (showState == ONLY_SHOW_ACTIVE_NOTE || showState == SHOW_ACTIVE_NOTE) {
            pa.fill(PhasesPApplet.getBrightColor1());
        }
        else {
            pa.fill(0);
        }
        pa.quarterNote(x, y, quarterNoteSize);
        
        if (showState != ONLY_SHOW_ACTIVE_NOTE) {
        	pa.fill(0);
        	pa.quarterNote(x - radius*0.65f, y, quarterNoteSize);
            pa.quarterNote(x + radius*0.65f, y, quarterNoteSize);
        }
    }
}