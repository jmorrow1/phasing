package icons;

import phases.PhasesPApplet;

public class InstrumentIcon implements Icon {
	private int instrument;

	public InstrumentIcon(int instrument) {
		this.instrument = instrument;
	}
	
	@Override
	public void draw(float x, float y, float radius, PhasesPApplet pa) {
		if (instrument == PIANO) {
			float w = 1.25f*radius;
            float h = radius;
            float y1 = y - h/2f;
            float y2 = y + h/2f;
            
            pa.strokeWeight(2);
            pa.rectMode(pa.CENTER);
            
            //white keys
            pa.fill(255);
            pa.stroke(0);
            pa.rect(x, y, w, h);
            pa.line(x - w/6f, y1, x - w/6f, y2);
            pa.line(x + w/6f, y1, x + w/6f, y2);
            
            //black keys
            pa.noStroke();
            pa.fill(0);
            pa.rectMode(pa.CORNER);
            float blackKeyWidth = 2*w/9f;
            pa.rect(x - w/6f - blackKeyWidth/2f, y - h/2f, blackKeyWidth, h*0.66f);
            pa.rect(x + w/6f - blackKeyWidth/2f, y - h/2f, blackKeyWidth, h*0.66f);
	        
		}
	}
}