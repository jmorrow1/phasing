package instrument_graphics;

import geom.Shape;
import phasing.PhasesPApplet;
import processing.core.PApplet;

/**
 * Gives a graphical representation of a musical instrument.
 * 
 * @author James Morrow
 *
 */
public interface Instrument {
	/**
	 * Gives the shape on the instrument cooresponding to the given the pitch value.
	 * If the given pitch is out of the instrument's range,
	 * the pitch will be wrapped into range before being used. That way, the method always
	 * returns a shape and never returns a null pointer.
	 * 
	 * @param pitchValue The integer value of the pitch.
	 * @return The shape associated with the given pitch value.
	 */
	public Shape pitchToShape(int pitchValue);
	
	/**
	 * Displays the instrument to the given PApplet.
	 * @param pa The PApplet to which the instrument is drawn.
	 */
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
