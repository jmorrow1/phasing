package icons;

import phases.PhasesPApplet;

public class InstrumentIcon implements Icon {
	private final int instrument;

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
            
            pa.strokeWeight(radius/20f);
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
		else if (instrument == XYLOPHONE) {
			float w = 1.5f*radius;
			float h = radius;
			float dx = w / 3;
			float barWidth = 0.7f * dx;
			float barHeight = radius;
			
			float x1 = x - w/2f;
			float y1 = y - h/2f;
			
			pa.rectMode(pa.CORNER);
			for (int i=0; i<3; i++) {
				pa.fill(0);
				pa.rect(x1, y1, barWidth, barHeight);
				x1 += dx;
			}
		}
	}
}