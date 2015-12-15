package views;

import geom.Rect;
import phases.Phrase;
import processing.core.PApplet;
import processing.core.PFont;

public class SymbolicView extends View {
	private float x1, x2, width;
	//private PFont pfont;
	private PhraseGraphic a, b;
	private float pixelsPerWholeNote;
	private boolean cameraRelativeToMotion;
	private float dNoteptAcc;
	
	public SymbolicView(Rect rect, Phrase phrase, int color1, int color2, int opacity, PApplet pa) {
		super(rect, phrase, color1, color2, opacity, 0, pa);
		
		x1 = this.getX1() + this.getWidth()/20f;
		x2 = this.getX2() - this.getWidth()/20f;
		width = x2-x1;
		
		pixelsPerWholeNote = (x2-x1) / phrase.getTotalDuration();
		
		//pfont = pa.createFont("Gisha-Bold-48.vlw", 32);
		//pfont = pa.createFont("LilyUPCBold-48.vlw", 32);
		
		pa.textSize(32);
		
		NoteGraphic[] notesA = new NoteGraphic[phrase.getNumNotes()];
		NoteGraphic[] notesB = new NoteGraphic[phrase.getNumNotes()];
		float x = x1;
		float y = rect.getY1() + rect.getHeight()/2f;
		for (int i=0; i<phrase.getNumNotes(); i++) {
			notesA[i] = new NoteGraphic(phrase.convertPitch(phrase.getSCPitch(i), true), x, y - 15);
			notesB[i] = new NoteGraphic(phrase.convertPitch(phrase.getSCPitch(i), true), x, y + 15);
			x += pixelsPerWholeNote * phrase.getSCDuration(i);
		}
		
		a = new PhraseGraphic(notesA, color1, opacity);
		b = new PhraseGraphic(notesB, color2, opacity);
	}

	@Override
	public void update(float dNotept1, float dNotept2, int sign) {
		if (!cameraRelativeToMotion) {
			a.update(dNotept1);
			b.update(dNotept2);
		}
		else {
			dNoteptAcc += (dNotept2 - dNotept1);
			//if, on average, dNotept2 should be greater than dNotept1
			if (sign > 0) {
				if (dNoteptAcc > 0) {
					b.update(dNoteptAcc);
					dNoteptAcc = 0;
				}
				else {
					b.display();
				}
			}
			//if, on average, dNotept2 should be less than dNotept2
			else if (sign < 0) {
				if (dNoteptAcc < 0) {
					b.update(dNoteptAcc);
					dNoteptAcc = 0;
				}
				else {
					b.display();
				}
			}
			else {
				b.display();
			}
			a.display();
		}
		
		//draw white rectangles to make the phrase graphics appear to screen wrap
		pa.noStroke();
		pa.fill(255);
		pa.rectMode(pa.CORNER);
		pa.rect(this.getX1() + 1, this.getY1() + 1, this.getWidth()/20f, this.getHeight());
		pa.rect(this.getX2() - this.getWidth()/20f, this.getY1()+1, this.getWidth()/20f-1, this.getHeight()-1);
		
	}
	
	private class PhraseGraphic {
		NoteGraphic[] notes;
		int color;
		int opacity;
		
		PhraseGraphic(NoteGraphic[] notes, int color, int opacity) {
			this.notes = notes;
			this.color = color;
			this.opacity = opacity;
		}
		
		void update(float dNotept) {
			//translate
			float dx = pixelsPerWholeNote * dNotept;
			for (int i=0; i<notes.length; i++) {
				notes[i].x1 += dx;
			}
			
			//display
			display();
		}
		
		void display() {
			//wrap
			pa.fill(color, opacity);
			
			if (notes[0].x1 < x1) {
				while (notes[0].x2() < x1) {
					notes[0].x1 += width;
					leftShift(notes);
				}
				
				notes[0].displayShifted(width);
			}
			else if (notes[notes.length-1].x2() > x2) {
				while (notes[notes.length-1].x1 > x2) {
					notes[notes.length-1].x1 -= width;
					rightShift(notes);
				}
				
				notes[notes.length-1].displayShifted(-width);
			}
			
			pa.fill(color, opacity);
			pa.textAlign(pa.TOP, pa.CENTER);
			pa.textSize(32);
			for (int i=0; i<notes.length; i++) {
				if (notes[i].x1 < x2) {
					notes[i].display();
				}
			}
		}
	}
	
	private class NoteGraphic {
		String name;
		float x1, y1;
		float width;
		
		NoteGraphic(String name, float x, float y) {
			this.name = String.valueOf(name.charAt(0));
			this.x1 = x;
			this.y1 = y;
			//pa.textFont(pfont);
			this.width = pa.textWidth(this.name);
		}
		
		NoteGraphic(NoteGraphic n) {
			this.name = new String(n.name);
			this.x1 = n.x1;
			this.y1 = n.y1;
			this.width = width;
		}
		
		void display() {
			pa.text(name.charAt(0), x1, y1);
		}
		
		void displayShifted(float dx) {
			pa.textAlign(pa.TOP, pa.CENTER);
			pa.text(name, x1 + dx, y1);
		}
		
		float x2() {return x1+width;}
	}
	
	private static void leftShift(NoteGraphic[] xs) {
	    if (xs.length > 0) {
	        int i = xs.length-1;
	        NoteGraphic next = xs[i];
	        xs[i] = xs[0];
	        i--;
	        while (i >= 0) {
	        	NoteGraphic temp = xs[i];
	            xs[i] = next;
	            next = temp;
	            i--;
	        }
	    }
	}

	private static void rightShift(NoteGraphic[] xs) {
	    if (xs.length > 0) {
	        int i=0;
	        NoteGraphic prev = xs[i];
	        xs[i] = xs[xs.length-1];
	        i++;
	        while (i < xs.length) {
	        	NoteGraphic temp = xs[i];
	            xs[i] = prev;
	            prev = temp;
	            i++;
	        }
	    }
	}
	
	/*Settings*/
	
	private void setCameraRelativeToMotion(boolean cameraRelativeToMotion) {
		this.cameraRelativeToMotion = cameraRelativeToMotion;
	}
	
	public int numPresets() {
		return 1;
	}

	@Override
	public void loadPreset(int preset) {
		setCameraRelativeToMotion(true);
	}
}
