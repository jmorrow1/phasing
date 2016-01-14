package phases;

import processing.core.PApplet;

public class OptionValue <T extends Option> {
	private int index;
	private final T optionType;
	
	public OptionValue(T optionType, int index) {
		this.optionType = optionType;
		this.index = PApplet.constrain(index, 0, optionType.numValues);
	}
	
	public OptionValue<T> next() {
		index = (index+1) % optionType.numValues;
		return new OptionValue<T>(optionType, index);
	}
	
	public OptionValue<T> prev() {
		index = PhasesPApplet.remainder(index-1, optionType.numValues);
		return new OptionValue<T>(optionType, index);
	}
	
	public void set(OptionValue<T> val) {
		this.index = val.index;
	}
	
	public boolean equals(int i) {
		return index == i;
	}
	
	public int intValue() {
		return index;
	}
}