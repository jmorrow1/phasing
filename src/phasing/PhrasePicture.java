package phasing;

import java.io.Serializable;
import java.util.Random;

import geom.Polygon;
import geom.Rect;
import processing.core.PApplet;
import processing.data.JSONObject;

public class PhrasePicture implements JSONable {
	//class-scope
	private static int nextId = (int)'a';
	
	//phrase
	private Phrase phrase;
	
	//name
	private String name;
	private int nextCopyId = 1;
	
	//style data
	private DrawNote drawNoteFunc, drawRestFunc;
	private float blendAmt;
	
	//style parameters
	private static final int CIRCLE=0, DIAMOND=1, SQUARE=2;
	private final DrawNote addVertexFunc = new AddVertex();
	private final Style drawStyle = pa -> { pa.noStroke(); pa.fill(pa.getBlendedColor(blendAmt)); };
	private final Style restStyle = pa -> { pa.stroke(pa.getBlendedColor(blendAmt)); pa.fill(255); };
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	public PhrasePicture(Phrase phrase, PhasesPApplet pa) {
		this(phrase, "" + (char)nextId++, pa);
	}
	
	public PhrasePicture(Phrase phrase, String name, PhasesPApplet pa) {
		this.phrase = phrase;
		this.name = name;
		this.blendAmt = pa.random(1);
		int noteStyleType = pa.floor(pa.random(3));
		initDrawNoteFuncs(noteStyleType);
	}
	
	public PhrasePicture(PhrasePicture phrasePicture) {
		this.phrase = new Phrase(phrasePicture.phrase);
		this.name = new String(phrasePicture.name + phrasePicture.nextCopyId++); //TODO: make name assignment a bit more robust here
		this.blendAmt = phrasePicture.blendAmt;
		this.initDrawNoteFuncs(phrasePicture.noteStyleType());
	}
	
	public PhrasePicture(JSONObject json) {
		this.phrase = json.hasKey("phrase") ? new Phrase(json.getJSONObject("phrase")) : new Phrase();
		this.blendAmt = json.getFloat("blendAmt", (float)Math.random());
		initDrawNoteFuncs(json.getInt("noteStyleType", CIRCLE));
		this.name = json.getString("name", "?");
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.setJSONObject("phrase", phrase.toJSON());
		json.setFloat("blendAmt", blendAmt);
		json.setInt("noteStyleType", noteStyleType());
		json.setString("name", name);
		return json;
	}
	
	private void initDrawNoteFuncs(int noteStyleType) {
		switch (noteStyleType) {
			case CIRCLE : drawNoteFunc = new DrawCircle(drawStyle); drawRestFunc = new DrawCircle(restStyle); break;
			case DIAMOND : drawNoteFunc = new DrawDiamond(drawStyle); drawRestFunc = new DrawDiamond(restStyle); break;
			case SQUARE : drawNoteFunc = new DrawSquare(drawStyle); drawRestFunc = new DrawSquare(restStyle); break;
			default : initDrawNoteFuncs(CIRCLE); break;
		}
	}
	
	private int noteStyleType() {
		if (drawNoteFunc instanceof DrawCircle) return CIRCLE;
		if (drawNoteFunc instanceof DrawDiamond) return DIAMOND;
		if (drawNoteFunc instanceof DrawSquare) return SQUARE;
		return 0;
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/
	
	public void draw(Rect rect, PhasesPApplet pa) {
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
	
	public void iterateNotes(DrawNote drawNote, DrawNote drawRest, Rect rect, PhasesPApplet pa) {
		float x1 = pa.lerp(rect.getX1(), rect.getX2(), 0.1f);
		float x2 = pa.lerp(rect.getX2(), rect.getX1(), 0.1f);
		float x = x1;
		float dx = (x2 - x1) / phrase.getNumNotes();
				
		float y2 = pa.lerp(rect.getY2(), rect.getY1(), 0.15f);
		float y1 = pa.lerp(rect.getY1(), rect.getY2(), 0.15f);
		
		float radius = 0.025f * rect.getHeight();
		
		for (int i=0; i<phrase.getNumNotes(); i++) {
			int pitch = phrase.getSCPitch(i);
			float y = PApplet.map(pitch, phrase.minPitch(), phrase.maxPitch(), y2, y1);
			if (phrase.getSCDynamic(i) > 0) {
				drawNote.draw(x, y, radius, pa);
			}
			else {
				drawRest.draw(x, pa.lerp(y1, y2, 0.5f), radius, pa);
			}
			x += dx;
		}
	}
	
	/*************************
	 ***** Styling Notes *****
	 *************************/
	
	private interface Style {
		void apply(PhasesPApplet pa);
	}
	
	/*********************************
	 ***** Ways of Drawing Notes *****
	 *********************************/
	
	private interface DrawNote {
		void draw(float x, float y, float r, PhasesPApplet pa);
	}
	
	private class DrawCircle implements DrawNote {
		Style style;
		
		DrawCircle(Style style) {
			this.style = style;
		}
		
		public void draw(float x, float y, float r, PhasesPApplet pa) {
			style.apply(pa);
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
			style.apply(pa);
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
			style.apply(pa);
			r *= 1.25f;
			Polygon.drawPolygon(x, y, r, r, 4, pa.QUARTER_PI, pa);
		}
	}
	
	private class AddVertex implements DrawNote {
		public void draw(float x, float y, float r, PhasesPApplet pa) {
			pa.vertex(x, y);
		}
	}
	
	/*******************************
	 ***** Getters and Setters *****
	 *******************************/
	
	public Phrase getPhrase() {
		return phrase;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}