package phases;

import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.Slider;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

public class SliderPlus extends Slider {
	private PFont smallFont, largeFont;
	private FloatFormatter f;
	
	public SliderPlus(ControlP5 arg0, String arg1, PFont smallFont, PFont largeFont, FloatFormatter f) {
		super(arg0, arg1);
		this.smallFont = smallFont;
		this.largeFont = largeFont;
		this.f = f;
	}

	public SliderPlus(ControlP5 arg0, ControllerGroup<?> arg1, String arg2, float arg3, float arg4, float arg5,
			int arg6, int arg7, int arg8, int arg9) {
		super(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
	}

	public void draw(final PGraphics pg) {
		pg.pushMatrix();
		pg.translate(x(position) , y(position));
		_myControllerView.display(pg , this);

		//draw caption label
		pg.textFont(smallFont);
		pg.fill(0);
		pg.textAlign(pg.LEFT, pg.BOTTOM);
		pg.text(getLabel(), 0, 0);
		
		//draw value label
		pg.textFont(largeFont);
		pg.fill(255);
		String s = f.format(getValue());
		pg.textAlign(pg.CENTER, pg.CENTER);
		pg.text(s, 0, 0, getWidth(), getHeight());
		
		pg.popMatrix();
	}
}