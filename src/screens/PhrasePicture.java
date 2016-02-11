package screens;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import geom.Polygon;
import geom.Rect;
import phases.PhasesPApplet;
import phases.Phrase;
import processing.core.PApplet;

public class PhrasePicture implements Serializable {
	//phrase
	private Phrase phrase;
	
	//style info
	private static final int CIRCLE=0, DIAMOND=1, SQUARE=2;
	private DrawNote drawNoteFunc, drawRestFunc, addVertexFunc;
	private float blendAmt;
	
	public PhrasePicture(Phrase phrase, PhasesPApplet pa) {
		this.phrase = phrase;
		this.blendAmt = pa.random(1);
		int noteStyleType = pa.floor(pa.random(3));
		
		Style drawStyle = () -> { pa.noStroke(); pa.fill(pa.getBlendedColor(blendAmt)); };
		Style restStyle = () -> { pa.stroke(pa.getBlendedColor(blendAmt)); pa.fill(255); };
		
		switch (noteStyleType) {
			case CIRCLE : drawNoteFunc = new DrawCircle(drawStyle); drawRestFunc = new DrawCircle(restStyle); break;
			case DIAMOND : drawNoteFunc = new DrawDiamond(drawStyle); drawRestFunc = new DrawDiamond(restStyle); break;
			case SQUARE : drawNoteFunc = new DrawSquare(drawStyle); drawRestFunc = new DrawSquare(restStyle); break;                                             
		}
		addVertexFunc = new AddVertex();
	}
	
	protected void draw(Rect rect, PhasesPApplet pa) {
		//draw lines between notes
		pa.strokeWeight(1);
		pa.stroke(pa.getBlendedColor(blendAmt));
		pa.noFill();
		pa.beginShape();
		iterateNotes(addVertexFunc, addVertexFunc, rect, pa);
		pa.endShape();
		
		//draw notes
		iterateNotes(drawNoteFunc, drawRestFunc, rect, pa);
	}
	
	protected void iterateNotes(DrawNote drawNote, DrawNote drawRest, Rect rect, PhasesPApplet pa) {
		float x1 = pa.lerp(rect.getX1(), rect.getX2(), 0.1f);
		float x2 = pa.lerp(rect.getX2(), rect.getX1(), 0.1f);
		float x = x1;
		float dx = (x2 - x1) / phrase.getNumNotes();
				
		float y2 = pa.lerp(rect.getY2(), rect.getY1(), 0.1f);
		float y1 = pa.lerp(rect.getY1(), rect.getY2(), 0.1f);
		
		float radius = 0.025f * rect.getHeight();
		
		for (int i=0; i<phrase.getNumNotes(); i++) {
			int pitch = phrase.getSCPitch(i);
			float y = PApplet.map(pitch, phrase.minPitch(), phrase.maxPitch(), y2, y1);
			if (phrase.getSCDynamic(i) > 0) {
				drawNote.draw(x, y, radius, pa);
			}
			else {
				drawRest.draw(x, y, radius, pa);
			}
			x += dx;
		}
	}
	
	protected Phrase getPhrase() {
		return phrase;
	}
	
	private interface Style extends Serializable {
		void apply();
	}
	
	private interface DrawNote extends Serializable {
		void draw(float x, float y, float r, PhasesPApplet pa);
	}
	
	private class DrawCircle implements DrawNote {
		Style style;
		
		DrawCircle(Style style) {
			this.style = style;
		}
		
		public void draw(float x, float y, float r, PhasesPApplet pa) {
			style.apply();
			pa.ellipseMode(pa.RADIUS); 
			pa.ellipse(x, y, r, r);
		}
	}
	
	private class DrawDiamond implements DrawNote {
		Style style;
		
		DrawDiamond(Style style) {
			this.style = style;
		}
		public void draw(float x, float y, float r, PhasesPApplet pa) {
			style.apply();
			r *= 1.25f;
			Polygon.drawPolygon(x, y, r, r, 4, 0, pa);
		}
	}
	
	private class DrawSquare implements DrawNote {
		Style style;
		
		DrawSquare(Style style) {
			this.style = style;
		}
		
		public void draw(float x, float y, float r, PhasesPApplet pa) {
			style.apply();
			r *= 1.25f;
			Polygon.drawPolygon(x, y, r, r, 4, pa.QUARTER_PI, pa);
		}
	}
	
	private class AddVertex implements DrawNote {
		public void draw(float x, float y, float r, PhasesPApplet pa) {
			pa.vertex(x, y);
		}
	}
}