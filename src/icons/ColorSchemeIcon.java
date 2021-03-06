package icons;

import phasing.PhasesPApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class ColorSchemeIcon implements Icon {
    private final int colorScheme;

    public ColorSchemeIcon(int colorScheme) {
        this.colorScheme = colorScheme;
    }

    @Override
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        pa.noStroke();
        pa.ellipseMode(pa.CENTER);

        if (colorScheme == MONOCHROMATIC) {
            pa.fill(0, 100);
        } else {
            pa.fill(PhasesPApplet.getColor2(), 150);
        }

        pa.ellipse(x - radius / 3f, y, radius, radius);

        if (colorScheme == MONOCHROMATIC) {
            pa.fill(0, 100);
        } else {
            pa.fill(PhasesPApplet.getColor1(), 150);
        }

        pa.ellipse(x + radius / 3f, y, radius, radius);
    }

    public static int numTypes() {
        return numColorSchemes;
    }
}