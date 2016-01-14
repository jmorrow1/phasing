package icons;

import phases.Option.Superimpose;
import phases.PhasesPApplet;

public class SuperimposedOrSeparatedIcon extends Icon {
    public SuperimposedOrSeparatedIcon(int value) {
        super(value);
    }
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        pa.textAlign(pa.CENTER, pa.CENTER);
        pa.fill(0);
        pa.textSize(radius);
        pa.text((value == Superimpose.SUPERIMPOSED) ? "1" : "2", x, y);
    }
}