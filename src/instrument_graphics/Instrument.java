package instrument_graphics;

import geom.Shape;
import phases.PhasesPApplet;
import processing.core.PApplet;

public interface Instrument {
	public Shape getShapeAtNoteIndex(int index);
	public void display(PApplet pa);
	/**
	 *
	 * @param midiPitch The midi pitch value
	 * @return True if the given midi pitch value cooresponds to a white piano key, false otherwise.
	 */
	public static boolean isWhiteKey(int midiPitch) {
		return !isBlackKey(midiPitch);
	}
	
	/**
	 * 
	 * @param midiPitch The midi pitch value
	 * @return True if the given midi pitch value cooresponds to a black piano key, false otherwise.
	 */
	public static boolean isBlackKey(int i) {
		i = PhasesPApplet.remainder(i, 12);
		return i == 1 || i == 3 || i == 6 || i == 8 || i == 10;
	}
}
