package icons;

import phasing.PhasesPApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class SuperimposedOrSeparatedIcon implements Icon {
    private final int superimposed;
  
    public SuperimposedOrSeparatedIcon(int superimposed) {
        this.superimposed = superimposed;
    }
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        pa.textAlign(pa.CENTER, pa.CENTER);
        pa.noStroke();
        pa.fill(0);
        pa.textSize(radius);
        pa.text((superimposed == SUPERIMPOSED) ? "1" : "2", x, y);
    }
}