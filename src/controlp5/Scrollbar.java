package controlp5;

import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.ControllerView;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Scrollbar extends Controller<Scrollbar> {
    //geometric data
    private float scrollerX1, scrollerX2, scrollerWidth;
    
    //tick data
    private int currentTick, numTickMarks, ticksPerScroller;
    
    //input logic
    private float mouseOffsetX, mouseOffsetY;
    private boolean draggingScroller;
  
    public Scrollbar(ControlP5 cp5, String name, int ticksPerScroller, int numTickMarks) {
        super(cp5, name);      

        this.numTickMarks = numTickMarks;
        this.ticksPerScroller = ticksPerScroller;
        this.currentTick = 0;
        
        updateGeometricalData();
                
        setView(new ControllerView<Scrollbar>() {
            public void display(PGraphics pg, Scrollbar s) {
                //bar
                pg.strokeWeight(1);
                pg.noStroke();
                pg.fill(s.getColor().getBackground());
                pg.rectMode(pg.CORNER);
                pg.rect(0, 0, s.getWidth(), s.getHeight());
                
                //scroller
                pg.fill(s.getColor().getForeground());
                pg.rectMode(pg.CORNERS);
                pg.rect(scrollerX1, 0, scrollerX2, s.getHeight());
            }
        });
    }
    
    private void updateGeometricalData() {
        scrollerWidth = PApplet.map(ticksPerScroller, 0, numTickMarks, 0, getWidth());
        scrollerX1 = PApplet.map(currentTick, 0, numTickMarks, 0, getWidth());
        scrollerX2 = scrollerX1 + scrollerWidth;
    }
    
    private void updateCurrentTick() {
        currentTick = PApplet.round(PApplet.map(scrollerX1, 0, getWidth(), 0, numTickMarks));
    }
    
    @Override
    public Scrollbar setSize(int w, int h) {
        super.setSize(w, h);
        updateGeometricalData();
        return this;
    }

    @Override
    public Scrollbar setPosition(float x, float y) {
        super.setPosition(x, y);
        updateGeometricalData();
        return this;
    }

    private boolean touchesScroller() {
      return scrollerX1 <= getPointer().x() && getPointer().x() <= scrollerX2 &&
          0 <= getPointer().y() && getPointer().y() <= getHeight();
    }
    
    @Override
    public void onPress() {
        if (touchesScroller()) {
            mouseOffsetX = getPointer().x() - scrollerX1;
            mouseOffsetY = getPointer().y() - 0;
            draggingScroller = true;
        }
    }
    
    @Override
    public void onDrag() {
        if (draggingScroller) {
            float newX1 = getPointer().x() - mouseOffsetX;
            float newX2 = getPointer().x() + (scrollerWidth - mouseOffsetX);
            if (0 < getPointer().dx() && newX1 > 0) {
                scrollerX2 = PApplet.constrain(newX2, 0, getWidth());
                scrollerX1 = scrollerX2 - scrollerWidth;
            }
            else if (getPointer().dx() < 0 && newX2 < getWidth()) {
                scrollerX1 = PApplet.constrain(newX1, 0, getWidth());
                scrollerX2 = scrollerX1 + scrollerWidth;
            }
            updateCurrentTick();
            updateGeometricalData();
        }
    }
    
    @Override
    public void onRelease() {
        if (draggingScroller) {
            draggingScroller = false;
        }
    }
    
    public void myOnScroll(int sign) {
    	int dTick = -sign;
    	currentTick = PApplet.constrain(currentTick+dTick, 0, numTickMarks - ticksPerScroller);
    	updateGeometricalData();
    }

	public int getTicksPerScroller() {
		return ticksPerScroller;
	}

	public void setTicksPerScroller(int ticksPerScroller) {
		this.ticksPerScroller = ticksPerScroller;
	}

	public float getScrollerX1() {
		return scrollerX1;
	}

	public void setScrollerX1(float scrollerX1) {
		this.scrollerX1 = scrollerX1;
		updateCurrentTick();
	}

	public float getScrollerWidth() {
		return scrollerWidth;
	}

	public void setScrollerWidth(float scrollerWidth) {
		this.scrollerWidth = scrollerWidth;
		updateCurrentTick();
	}

	public float getScrollerX2() {
		return scrollerX2;
	}

	public int getNumTickMarks() {
		return numTickMarks;
	}

	public void setNumTickMarks(int numTickMarks) {
		currentTick = (int)PApplet.map(currentTick, 0, this.numTickMarks, 0, numTickMarks);
		this.numTickMarks = numTickMarks;
		updateGeometricalData();
	}
	
	public int getCurrentTick() {
		return currentTick;
	}
}