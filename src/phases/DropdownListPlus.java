package phases;

import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.DropdownList;

public class DropdownListPlus extends DropdownList {

	public DropdownListPlus(ControlP5 theControlP5, String theName) {
		super(theControlP5, theName);
	}

	protected DropdownListPlus(ControlP5 theControlP5, ControllerGroup<?> theGroup, String theName, int theX, int theY,
			int theW, int theH) {
		super(theControlP5, theGroup, theName, theX, theY, theW, theH);
	}
	
	public void onRelease() {
		try {
			super.onRelease();
		}
		catch (IndexOutOfBoundsException e) {
		}
	}

}