package icons;

import phases.PhasesPApplet;

public class ShowActiveNoteIcon implements Icon {
    private final int showState;
  
    public ShowActiveNoteIcon(int showState) {
        this.showState = showState;
    }
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) { 
        int quarterNoteSize = (int)(radius*1.5f);
        
        if (showState == ONLY_SHOW_ACTIVE_NOTE || showState == SHOW_ACTIVE_NOTE) {
            pa.fill(PhasesPApplet.getColor1Bold());
        }
        else {
            pa.fill(0);
        }
        pa.drawQuarterNoteSymbol(x, y, quarterNoteSize);
        
        if (showState != ONLY_SHOW_ACTIVE_NOTE) {
        	pa.fill(0);
        	pa.drawQuarterNoteSymbol(x - radius*0.65f, y, quarterNoteSize);
            pa.drawQuarterNoteSymbol(x + radius*0.65f, y, quarterNoteSize);
        }
    }
}