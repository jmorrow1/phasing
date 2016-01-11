package views;

import java.util.Arrays;

import geom.Rect;
import phases.PhasesPApplet;

public abstract class View extends Rect {
	protected PhasesPApplet pa;
	protected int opacity;
	private int[][] configIds;
	
	public View(Rect rect, int opacity, PhasesPApplet pa) {
		super(rect);
		this.opacity = opacity;
		this.pa = pa;
	}
	
	public abstract void update(float dNotept1, float dNotept2, int sign);
	
	public abstract int getValue(int index);
	public String getOptionConfigurationId() {
		String s = "";
		for (int i=0; i<numOptions(); i++) {
			s += getValue(i);
		}
		return s;
	}
	public abstract int numOptions();
	public abstract String showOption(int index);
	public String showCurrentSettings() {
		String s = "[";
		for (int i=0; i<numOptions(); i++) {
			s += showOption(i);
			if (i != numOptions()-1) {
				s += ", ";
			}
		}
		s += "]";
		return s;
	}
	public abstract int numValues(int optionVariableIndex);
	
	private int numPosConfigs() {
		int product = 1;
		for (int i=0; i<numOptions(); i++) {
			product *= numValues(i);
		}
		return product;
	}
	
	public int[][] getAllConfigIds() {
		if (configIds == null) {
			configIds = new int[numPosConfigs()][numOptions()];
			
			int n = 0;
			int[] id = new int[numOptions()];
			int digit = id.length-1;
			
			while (n < numPosConfigs()) {
				while (id[digit] >= numValues(digit)) {
					id[digit] = 0;
					digit--;
					id[digit]++;
				}
				
				digit = id.length-1;
				
				configIds[n] = Arrays.copyOf(id, id.length);
				n++;
				
				id[digit]++;
			}
		}
		
		return configIds;
	}
	
	public int numNeighboringConfigs() {
		int sum = 0;
		for (int i=0; i<numOptions(); i++) {
			sum += numValues(i)-1;
		}
		return sum;
	}
	
	public int[][] getAllNeighborConfigIds() {
		int[] id = getCurrentConfigId();
		int[][] ids = new int[numNeighboringConfigs()][];
		int n = 0;
		for (int i=0; i<id.length; i++) {
			for (int j=0; j<numValues(i)-1; j++) {
				id[i] = (id[i]+1) % numValues(i);
				ids[n++] = Arrays.copyOf(id, id.length);
			}
			id[i] = (id[i]+1) % numValues(i);
		}
		return ids;
	}
	
	public int[] getCurrentConfigId() {
		int[] currentConfigId = new int[numOptions()];
		for (int i=0; i<numOptions(); i++) {
			currentConfigId[i] = getValue(i);
		}
		return currentConfigId;
	}
	
	public abstract void adoptConfig(int[] id);
}
