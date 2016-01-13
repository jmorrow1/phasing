package icons;

import phases.PhasesPApplet;

public class SuperimposedOrSeparatedIcon implements Icon {
    private boolean superimposed;
    private String text;
  
    public SuperimposedOrSeparatedIcon(boolean superimposed) {
        this.superimposed = superimposed;
        text = (superimposed) ? "1" : "2";
    }
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        pa.textAlign(pa.CENTER, pa.CENTER);
        pa.fill(0);
        pa.textSize(radius);
        pa.text(text, x, y);
    }
}