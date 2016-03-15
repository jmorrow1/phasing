package phasing;

import processing.core.PApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class App {
	public static void main(String[] args) {
		if (args.length >= 2) {
			try {
				int w = Integer.parseInt(args[0]);
				int h = Integer.parseInt(args[1]);
				PhasesPApplet.setInitialScreenSize(w, h);
			}
			catch (NumberFormatException e) {}
		}
		PApplet.main("phasing.PhasesPApplet");
	}
}