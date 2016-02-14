package phasing;

import processing.core.PApplet;

/**
 * An integer bound by arithmetic modulo some number.
 * That number is given when the ModInt object is constructed: it's the field called "divisor".
 * 
 * @author James Morrow
 *
 */
public class ModInt {
	private final String name;
	private int value;
	private final int divisor;

	public ModInt(int value, int divisor, String name) {
		setValue(value);
		this.divisor = (divisor > 0) ? divisor : 1;
		this.name = name;
	}
	
	public void setValue(int value) {
		if (0 <= value && value < divisor) {
			this.value = value;
		}
	}
	
	public void add(int n) {
		value = PhasesPApplet.remainder(value+n, divisor);
	}
	
	public void increment() {
		value = (value+1) % divisor;
	}
	
	public void decrement() {
		value = PhasesPApplet.remainder(value-1, divisor);
	}
	
	public int toInt() {
		return value;
	}
	
	public int getDivisor() {
		return divisor;
	}
	
	public String getName() {
		return name;
	}
}