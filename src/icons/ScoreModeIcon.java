package icons;

import phases.PhasesPApplet;

public class ScoreModeIcon implements Icon {
    private int mode;
    
    public ScoreModeIcon(int mode) {
        this.mode = mode;
    }
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) { 
        int quarterNoteSize = (int)radius;
        
        pa.fill(0);
        
        if (mode == FADES) {
            pa.fill(0, 75);
        }
        pa.quarterNote(x - radius*0.4f, y - 0.5f*radius, quarterNoteSize);
       
        if (mode == FADES) {
            pa.fill(0, 150);
        }
        pa.quarterNote(x + 0.1f*radius, y - 0.5f*radius, quarterNoteSize);
        
        if (mode == FADES) {
            pa.fill(0, 255);
        }
        pa.quarterNote(x + radius*0.6f, y - 0.5f*radius, quarterNoteSize);
        
        if (mode == SCROLLS) {
        	pa.stroke(0);
        	pa.strokeWeight(radius/15f);
        	pa.line(x + radius*0.9f, y + radius*0.4f, x - radius*0.9f, y + radius*0.4f);
            float arrowHeadSize = radius*0.25f;
            pa.drawArrowHead(x - radius*0.9f, y + radius*0.4f, arrowHeadSize, pa.PI, pa.PI*0.75f);
        }
    }
}