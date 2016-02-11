package screens;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;
import processing.core.PApplet;

public class PictureStyle implements Serializable {
	private float blendAmt;
	
	public PictureStyle(PApplet pa) {
		this.blendAmt = pa.random(1);
	}
	
	protected void draw(Phrase phrase, Rect rect, PhasesPApplet pa) {
		//draw notes
		pa.ellipseMode(pa.RADIUS);
		pa.noStroke();
		pa.fill(pa.getBlendedColor(blendAmt));
		float dotRadius = 0.025f * rect.getHeight();
		iterateNotes((x, y) -> pa.ellipse(x, y, dotRadius, dotRadius), phrase, rect, pa);
		
		//draw lines between notes	
		pa.strokeWeight(1);
		pa.stroke(pa.getBlendedColor(blendAmt));
		pa.noFill();
		pa.beginShape();
		iterateNotes((x, y) -> pa.vertex(x, y), phrase, rect, pa);
		pa.endShape();
	}
	
	protected void iterateNotes(NoteDraw nd, Phrase phrase, Rect rect, PhasesPApplet pa) {
		float x1 = pa.lerp(rect.getX1(), rect.getX2(), 0.1f);
		float x2 = pa.lerp(rect.getX2(), rect.getX1(), 0.1f);
		float x = x1;
		float dx = (x2 - x1) / phrase.getNumNotes();
				
		float y2 = pa.lerp(rect.getY2(), rect.getY1(), 0.1f);
		float y1 = pa.lerp(rect.getY1(), rect.getY2(), 0.1f);
		
		for (int i=0; i<phrase.getNumNotes(); i++) {
			int pitch = phrase.getSCPitch(i);
			float y = pa.map(pitch, phrase.minPitch(), phrase.maxPitch(), y2, y1);
			nd.draw(x, y);
			x += dx;
		}
	}
	
	private interface NoteDraw {
		void draw(float x, float y);
	}
}