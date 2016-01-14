package icons;

import phases.Option.ColorScheme;
import phases.PhasesPApplet;

public class ColorSchemeIcon extends Icon {
    public ColorSchemeIcon(int value) {
        super(value);
    }
  
    @Override
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        pa.noStroke();
        pa.ellipseMode(pa.CENTER);
        
        if (value == ColorScheme.MONOCHROME) {
            pa.fill(0, 100);
        }
        else {
            pa.fill(PhasesPApplet.getColor2(), 100);
        }
        
        pa.ellipse(x - radius/3f, y, radius, radius);
       
        if (value == ColorScheme.DIACHROME) {
            pa.fill(0, 100);
        }
        else {
            pa.fill(PhasesPApplet.getColor1(), 100);
        }

        pa.ellipse(x + radius/3f, y, radius, radius);
    }
}