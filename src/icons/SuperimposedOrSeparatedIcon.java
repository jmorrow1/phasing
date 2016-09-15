package icons;

import phasing.PhasesPApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class SuperimposedOrSeparatedIcon implements Icon {
    private final int mode;
  
    public SuperimposedOrSeparatedIcon(int mode) {
        this.mode = mode;
    }
    
    @Override
    public void draw(float cenx, float ceny, float radius, PhasesPApplet pa) {
    	float rectWidth = radius * 0.25f;
    	float rectHeight = radius;
        pa.noStroke();
        pa.fill(0);
        pa.rectMode(pa.CENTER);
        if (mode == SUPERIMPOSED) {
        	pa.rect(cenx, ceny, rectWidth, rectHeight);
        }
        else if (mode == SEPARATED) {
        	pa.rect(cenx - rectWidth, ceny, rectWidth, rectHeight);
        	pa.rect(cenx + rectWidth, ceny, rectWidth, rectHeight);
        }
    }
    
    public static int numTypes() {
    	return numWaysOfBeingSuperimposedOrSeparated;
    }
}