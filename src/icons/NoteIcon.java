package icons;

import phases.PhasesPApplet;

public class NoteIcon implements Icon {
    final int SYMBOLS=0, DOTS=1, CONNECTED_DOTS=2, RECTS_OR_SECTORS=3;  
    private int noteType;
    
    public NoteIcon(int noteType) {
        this.noteType = noteType;
    }
  
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        if (noteType == SYMBOLS) {
            pa.fill(0);
            pa.textSize(radius);
            pa.textAlign(pa.CENTER, pa.CENTER);
            pa.text("A", x, y);
        }
        else if (noteType == DOTS) {
            pa.noStroke();
            pa.fill(0);
            pa.ellipseMode(pa.CENTER);
            pa.ellipse(x, y, radius*0.8f, radius*0.8f);
        }
        else if (noteType == CONNECTED_DOTS) {
            pa.noStroke();
            pa.fill(0);
            pa.ellipseMode(pa.CENTER);
            pa.ellipse(x - radius*0.4f, y, radius*0.4f, radius*0.4f);
            pa.ellipse(x + radius*0.4f, y, radius*0.4f, radius*0.4f);
            pa.strokeWeight(2);
            pa.stroke(0);
            pa.line(x - radius*0.5f, y, x + radius*0.5f, y);
        }
        else if (noteType == RECTS_OR_SECTORS) {
            pa.fill(0);
            pa.rectMode(pa.CENTER);
            pa.rect(x, y, radius*0.9f, radius*0.35f);
        }
    }
}