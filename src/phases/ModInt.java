package phases;

import processing.core.PApplet;

public class ModInt {
	private final String name;
	private int value;
	private final int divisor;
	
	public ModInt(int value, int divisor, String name) {
		this.value = value;
		this.divisor = (divisor > 0) ? divisor : 1;
		this.name = name;
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
