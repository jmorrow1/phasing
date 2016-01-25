package instrument_graphics;

import geom.Polygon;
import geom.Rect;
import geom.Shape;
import processing.core.PApplet;

public class Xylophone implements Instrument {
	private final int numOctaves;
	private final int numBars;
	private final Shape[] bars;
	
	public Xylophone(int numOctaves, Rect r) {
		this.numOctaves = numOctaves;
		this.numBars = numOctaves * 12;
		int numWhiteBars = numOctaves * 7;
		bars = new Shape[numBars];
		float dx = r.getWidth() / numWhiteBars;
		float barWidth = 0.75f*dx;
		float widthBetweenBars = 0.25f*dx;
		float unitBarHeight = r.getHeight() / 2f;
		initBars(r.getX1(), r.getY1() + 0.4f*r.getHeight(), barWidth, unitBarHeight, widthBetweenBars);
	}
	
	private void initBars(float x1, float y1, float barWidth, float unitBarHeight, float widthBetweenBars) {
		float dx = barWidth + widthBetweenBars;
		
		float prevBarHeight = 0;
	    float barHeight = 0;
	    float nextBarHeight = (int)barHeight(0, unitBarHeight); 	    
	    int i=0;
		while (i < numBars) {
	        barHeight = nextBarHeight;
	        nextBarHeight = barHeight(i+1, unitBarHeight);
	        if (Instrument.isWhiteKey(i)) {
	            bars[i] = makeWhiteBar(i, x1, y1, barWidth, barHeight, widthBetweenBars, -prevBarHeight*0.2f, -nextBarHeight*0.2f);
	            i++;
	            x1 += dx;
	        }
	        else {
	            Rect r = new Rect(x1 - dx/2f, y1 - (0.8f*barHeight), barWidth, barHeight, PApplet.CORNER);
	            bars[i++] = r;
	        }
	        prevBarHeight = barHeight;
	    }
	}
	
	private Shape makeWhiteBar(int barIndex, float x1, float y1, float barWidth, float barHeight, float widthBetweenBars, float prevBarHeight, float nextBarHeight) {
	    boolean leftNeighborIsBlack = Instrument.isBlackKey(barIndex-1);
	    boolean rightNeighborIsBlack = Instrument.isBlackKey(barIndex+1);
	    
	    float x2 = x1 + barWidth/2f - widthBetweenBars/2f;
	    float x3 = x1 + barWidth/2f + widthBetweenBars/2f;
	    float x4 = x1 + barWidth;
	    
	    float y2 = y1 - prevBarHeight;
	    float y3 = y1 - nextBarHeight;
	    float y4 = y1 + barHeight;
	    
	    if (!leftNeighborIsBlack && rightNeighborIsBlack) {
	        return new Polygon(new float[] {x1, y1, x3, y1, x3, y3, x4, y3, x4, y4, x1, y4, x1, y3});
	    }
	    else if (leftNeighborIsBlack && rightNeighborIsBlack) {
	        return new Polygon(new float[] {x1, y2, x2, y2, x2, y1, x3, y1, x3, y3, x4, y3, x4, y4, x1, y4});
	    }
	    else if (leftNeighborIsBlack && !rightNeighborIsBlack) {
	        return new Polygon(new float[] {x1, y2, x2, y2, x2, y1, x3, y1, x4, y1, x4, y4, x1, y4});
	    }
	    
	    return null;
	}

	@Override
	public Shape getShapeAtNoteIndex(int index) {
		if (0 <= index && index < bars.length) {
			return bars[index].clone();
		}
		else {
			System.err.println("index out of range in call to Piano.getKeyCopy(" + index + ")");
			return null;
		}
	}

	@Override
	public void display(PApplet pa) {
		pa.strokeWeight(1);
		pa.stroke(100);
	    pa.noFill();
	    for (Shape s : bars) {
            s.display(pa);
	    }
	}
	
	private static float barHeight(int n, float unitBarHeight) {
	    float ratio = 1 / (float)Math.sqrt(Math.pow(nroot(2, 12), n));
	    return ratio * unitBarHeight;
	}
	
	private static float nroot(float x, float n) {
	    return (float)Math.pow(x, 1 / n);
	}
}