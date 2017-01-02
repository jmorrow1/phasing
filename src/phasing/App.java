package phasing;

import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class App {
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("NO_CURSOR")) {
                PhasesPApplet.hideCursor = true;
            }
            if (args[i].equals("DEBUG")) {
                PhasesPApplet.showAnimationError = true;
            }
        }

        if (args.length >= 2) {
            try {
                int w = Integer.parseInt(args[0]);
                int h = Integer.parseInt(args[1]);
                PhasesPApplet.setInitialScreenSize(w, h);
            } catch (NumberFormatException e) {
            }
        }
        PApplet.main("phasing.PhasesPApplet");
    }
}