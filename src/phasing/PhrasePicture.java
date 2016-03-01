package phasing;

import java.io.Serializable;
import java.util.Random;

import geom.Polygon;
import geom.Rect;
import processing.core.PApplet;
import processing.data.JSONObject;

/**
 * Represents a phrase with styling information that helps determine how to depict the phrase as a picture.
 * 
 * @author James Morrow
 *
 */
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
	
	/**
	 * Constructs a PhrasePicture for the given Phrase whose styling information will be randomly generated.
	 * It will automatically be assigned a unique name.
	 * 
	 * @param phrase The Phrase.
	 * @param pa The source of randomness for initializing styling information.
	 */
	public PhrasePicture(Phrase phrase, PhasesPApplet pa) {
		this(phrase, "" + (char)nextId++, pa);
	}
	
	/**
	 * Constructs a PhrasePicture for the given Phrase whose styling information will be randomly generated.
	 * @param phrase The Phrase.
	 * @param name The identifier of the PhrasePicture.
	 * @param pa The source of randomness for initializing styling information.
	 */
	public PhrasePicture(Phrase phrase, String name, PhasesPApplet pa) {
		this.phrase = phrase;
		this.name = name;
		this.blendAmt = pa.random(1);
		int noteStyleType = pa.floor(pa.random(3));
		initDrawNoteFuncs(noteStyleType);
	}
	
	/**
	 * Copy constructor. Copies the given PhrasePicture's Phrase and styling information but not its name.
	 * 
	 * @param phrasePicture The PhrasePicture to copy.
	 */
	public PhrasePicture(PhrasePicture phrasePicture) {
		this.phrase = new Phrase(phrasePicture.phrase);
		this.name = new String(phrasePicture.name + phrasePicture.nextCopyId++); //TODO: As this is written now, this name could collide with another name.
		this.blendAmt = phrasePicture.blendAmt;
		this.initDrawNoteFuncs(phrasePicture.noteStyleType());
	}
	
	/**
	 * Constructor for making a PhrasePicture from a JSONObject.
	 * 
	 * @param json A JSONObject representing a PhrasePicture.
	 */
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
	
	/**
	 * Initializes the functions for drawing notes and rests.
	 * 
	 * @param noteStyleType The code that determines which functions are initialized.
	 */
	private void initDrawNoteFuncs(int noteStyleType) {
		switch (noteStyleType) {
			case CIRCLE : drawNoteFunc = new DrawCircle(drawStyle); drawRestFunc = new DrawCircle(restStyle); break;
			case DIAMOND : drawNoteFunc = new DrawDiamond(drawStyle); drawRestFunc = new DrawDiamond(restStyle); break;
			case SQUARE : drawNoteFunc = new DrawSquare(drawStyle); drawRestFunc = new DrawSquare(restStyle); break;
			default : initDrawNoteFuncs(CIRCLE); break;
		}
	}
	
	/**
	 * 
	 * @return The code (CIRCLE, DIAMOND, or SQUARE) that represents the type of draw note function this object uses.
	 */
	private int noteStyleType() {
		if (drawNoteFunc instanceof DrawCircle) return CIRCLE;
		if (drawNoteFunc instanceof DrawDiamond) return DIAMOND;
		if (drawNoteFunc instanceof DrawSquare) return SQUARE;
		return 0;
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/
	
	/**
	 * Draws the Phrase within the given rect to the given PApplet.
	 * 
	 * @param rect The area in which to draw the Phrase.
	 * @param pa The PhasesPApplet to draw to and get color scheme information from.
	 */
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
	
	/**
	 * Iterates through the notes and rests in the phrase, performing the draw note function on notes and the draw rest function on rests as it goes.
	 * 
	 * @param drawNote The function used to draw notes.
	 * @param drawRest The function used to draw rests.
	 * @param rect The area in which to draw the Phrase's notes.
	 * @param pa The PApplet to draw to.
	 */
	private void iterateNotes(DrawNote drawNote, DrawNote drawRest, Rect rect, PhasesPApplet pa) {
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
	
	/**
	 * Changes a PhasesPApplet state in order to change the way it styles graphics (such as changes in fill color, stroke color, etc.).
	 * 
	 * @author James Morrow
	 *
	 */
	private interface Style {
		void apply(PhasesPApplet pa);
	}
	
	/*********************************
	 ***** Ways of Drawing Notes *****
	 *********************************/
	
	/**
	 * Draws a shape to a coordinate in space (x,y) with a radius r to a PhasesPApplet pa.
	 * 
	 * @author James Morrow
	 *
	 */
	private interface DrawNote {
		void draw(float x, float y, float r, PhasesPApplet pa);
	}
	
	/**
	 * Draws a circle with some style.
	 * 
	 * @author James Morrow
	 *
	 */
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
	
	/**
	 * Draws a diamond with some style.
	 * 
	 * @author James Morrow
	 *
	 */
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
	
	/**
	 * Draws a square with some style.
	 * 
	 * @author James Morrow
	 *
	 */
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
	
	/**
	 * Calls pa.vertex(x,y) on the PhasesPApplet pa.
	 * 
	 * @author James Morrow
	 *
	 */
	private class AddVertex implements DrawNote {
		public void draw(float x, float y, float r, PhasesPApplet pa) {
			pa.vertex(x, y);
		}
	}
	
	/*******************************
	 ***** Getters and Setters *****
	 *******************************/
	
	/**
	 * 
	 * @return The phrase this PhrasePicture contains.
	 */
	public Phrase getPhrase() {
		return phrase;
	}
	
	/**
	 * 
	 * @return The name of this PhrasePicture.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of this PhrasePicture.
	 * 
	 * @param name The new name.
	 */
	public void setName(String name) {
		this.name = name;
	}
}