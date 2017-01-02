package controlp5;

import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.ControllerView;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * A scrollbar that works with the ControlP5 library. Terminology: Track: The
 * thing that extends the entire length of the scrollbar. The thing in which the
 * scroller moves. Scroller: The thing that moves along the track. Tick mark: An
 * abstract unit that cooresponds to the scroller's location along the track.
 * It's easier for the outside world to interface with tick marks as opposed to
 * geometrical data when doing logic with the state of the scrollbar.
 * 
 * @author James Morrow
 *
 */
public class Scrollbar extends Controller<Scrollbar> {
    // geometric data
    private float scrollerLow, scrollerHigh, scrollerLength;

    // tick data
    private int currentTick, ticksPerTrack, ticksPerScroller;

    // input logic
    private float mouseOffsetX, mouseOffsetY;
    private boolean draggingScroller;

    /**
     * 
     * @param cp5
     *            The ControlP5 instance
     * @param name
     *            The name of the controller
     * @param ticksPerScroller
     *            The size of the scroller (the thing the user can move along
     *            the track) in terms of tick marks
     * @param ticksPerTrack
     *            The size of the track (the thing that the scroller moves
     *            along) in terms of tick marks
     */
    public Scrollbar(ControlP5 cp5, String name, int ticksPerScroller, int ticksPerTrack) {
        super(cp5, name);

        this.ticksPerTrack = ticksPerTrack;
        this.ticksPerScroller = ticksPerScroller;
        this.currentTick = 0;

        updateGeometricalData();

        setView(new ControllerView<Scrollbar>() {
            public void display(PGraphics pg, Scrollbar s) {
                // track
                pg.strokeWeight(1);
                pg.noStroke();
                pg.fill(s.getColor().getBackground());
                pg.rectMode(pg.CORNER);
                pg.rect(0, 0, s.getWidth(), s.getHeight());

                // scroller
                if (s.isMouseOver()) {
                    pg.fill(s.getColor().getActive());
                } else {
                    pg.fill(s.getColor().getForeground());
                }
                pg.rectMode(pg.CORNERS);
                if (s.getWidth() >= s.getHeight()) {
                    pg.rect(scrollerLow, 0, scrollerHigh, s.getHeight());
                } else {
                    pg.rect(0, scrollerLow, s.getWidth(), scrollerHigh);
                }
            }
        });
    }

    /**
     * Updates the state of the geometrical data. This makes the Scrollbar's
     * geometrical data self-consistent and consistent with the rest of the data
     * fields.
     */
    private void updateGeometricalData() {
        if (ticksPerScroller == 0 && ticksPerTrack == 0) {
            ticksPerScroller = 1;
            ticksPerTrack = 1;
        }
        if (getWidth() >= getHeight()) {
            scrollerLength = PApplet.map(ticksPerScroller, 0, ticksPerTrack, 0, getWidth());
            scrollerLow = PApplet.map(currentTick, 0, ticksPerTrack, 0, getWidth());
            scrollerHigh = scrollerLow + scrollerLength;
        } else {
            scrollerLength = PApplet.map(ticksPerScroller, 0, ticksPerTrack, 0, getHeight());
            scrollerLow = PApplet.map(currentTick, 0, ticksPerTrack, 0, getHeight());
            scrollerHigh = scrollerLow + scrollerLength;
        }
    }

    /**
     * Updates the state of the tick mark cooresponding to the lowest point of
     * the scroller.
     * 
     */
    private void updateCurrentTick() {
        if (getWidth() >= getHeight()) {
            currentTick = PApplet.round(PApplet.map(scrollerLow, 0, getWidth(), 0, ticksPerTrack));
        } else {
            currentTick = PApplet.round(PApplet.map(scrollerLow, 0, getHeight(), 0, ticksPerTrack));
        }
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

    /**
     * Checks if the mouse pointer (which is the mouse location, relative to the
     * controller) is within the area of the scroller.
     * 
     * @return True, if the mouse pointer is within the area of the scroller.
     *         False otherwise.
     */
    private boolean touchesScroller() {
        if (getWidth() >= getHeight()) {
            return scrollerLow <= getPointer().x() && getPointer().x() <= scrollerHigh && 0 <= getPointer().y()
                    && getPointer().y() <= getHeight();
        } else {
            return 0 <= getPointer().x() && getPointer().x() <= getWidth() && scrollerLow <= getPointer().y()
                    && getPointer().y() <= scrollerHigh;
        }
    }

    @Override
    public void onPress() {
        if (touchesScroller()) {
            if (getWidth() >= getHeight()) {
                mouseOffsetX = getPointer().x() - scrollerLow;
                mouseOffsetY = getPointer().y() - 0;
            } else {
                mouseOffsetX = getPointer().x() - 0;
                mouseOffsetY = getPointer().y() - scrollerLow;
            }
            draggingScroller = true;
        } else {
            if (getWidth() >= getHeight()) {
                if (getPointer().x() < scrollerLow) {
                    setScrollerLowTick(currentTick - 1);
                } else if (getPointer().x() > scrollerHigh) {
                    setScrollerLowTick(currentTick + 1);
                }
            } else {
                if (getPointer().y() < scrollerLow) {
                    setScrollerLowTick(currentTick - 1);
                } else {
                    setScrollerLowTick(currentTick + 1);
                }
            }
        }
        setValue(scrollerLow);
    }

    @Override
    public void onDrag() {
        if (draggingScroller) {
            if (getWidth() >= getHeight()) {
                float newX1 = getPointer().x() - mouseOffsetX;
                float newX2 = getPointer().x() + (scrollerLength - mouseOffsetX);
                if (0 < getPointer().dx() && newX1 > 0) {
                    scrollerHigh = PApplet.constrain(newX2, 0, getWidth());
                    scrollerLow = scrollerHigh - scrollerLength;
                } else if (getPointer().dx() < 0 && newX2 < getWidth()) {
                    scrollerLow = PApplet.constrain(newX1, 0, getWidth());
                    scrollerHigh = scrollerLow + scrollerLength;
                }
            } else {
                float newY1 = getPointer().y() - mouseOffsetY;
                float newY2 = getPointer().y() + (scrollerLength - mouseOffsetY);
                if (0 < getPointer().dy() && newY1 > 0) {
                    scrollerHigh = PApplet.constrain(newY2, 0, getHeight());
                    scrollerLow = scrollerHigh - scrollerLength;
                } else if (getPointer().dy() < 0 && newY2 < getHeight()) {
                    scrollerLow = PApplet.constrain(newY1, 0, getHeight());
                    scrollerHigh = scrollerLow + scrollerLength;
                }
            }
            updateCurrentTick();
            updateGeometricalData();
            setValue(scrollerLow);
        }
    }

    @Override
    public void onRelease() {
        if (draggingScroller) {
            draggingScroller = false;
        }
    }

    /**
     * A hack to make it possible for mouse scroll events to adjust the state of
     * the scrollbar even when the mouse is not within the area of the
     * controller. Without this, the Scrollbar can only receive mouse scroll
     * events when the mouse is touching the controller.
     * 
     * @param sign
     *            The value of the mouse scrolle event
     */
    public void myOnScroll(int sign) {
        currentTick = PApplet.constrain(currentTick + sign, 0, ticksPerTrack - ticksPerScroller);
        updateGeometricalData();
    }

    /**
     * 
     * @return The number of tick marks associated with the scroller.
     */
    public int getTicksPerScroller() {
        return ticksPerScroller;
    }

    /**
     * Sets the number of tick marks associated with the scroller.
     * 
     * @param ticksPerScroller
     */
    public void setTicksPerScroller(int ticksPerScroller) {
        this.ticksPerScroller = PApplet.constrain(ticksPerScroller, 0, ticksPerTrack);
        updateGeometricalData();
    }

    /**
     * 
     * @return The length of the scroller in pixels.
     */
    public float getScrollerLength() {
        return scrollerLength;
    }

    /**
     * 
     * @return The length of the track in pixels.
     */
    public float getTrackLength() {
        return (getWidth() >= getHeight()) ? getWidth() : getHeight();
    }

    /**
     * 
     * @return The number of tick marks associated with the whole length of the
     *         scrollbar.
     */
    public int getNumTickMarks() {
        return ticksPerTrack;
    }

    /**
     * Sets the number of tick marks associated with the whole length of the
     * scrollbar.
     * 
     * @param ticksPerTrack
     */
    public void setNumTickMarks(int ticksPerTrack) {
        currentTick = (int) PApplet.map(currentTick, 0, this.ticksPerTrack, 0, ticksPerTrack);
        this.ticksPerTrack = ticksPerTrack;
        ticksPerScroller = (int) PApplet.constrain(ticksPerScroller, 0, ticksPerTrack);
        updateGeometricalData();
    }

    /**
     * 
     * @return The tick mark associated with the low end of the scroller
     */
    public int getLowTick() {
        return currentTick;
    }

    /**
     * Sets the tick mark associated with the low end of the scroller.
     * 
     * @param lowTick
     */
    public void setScrollerLowTick(int lowTick) {
        currentTick = PApplet.constrain(lowTick, 0, ticksPerTrack - ticksPerScroller);
        updateGeometricalData();
    }

    /**
     * 
     * @return The tick mark associated with the high end of the scroller
     */
    public int getHighTick() {
        return currentTick + ticksPerScroller;
    }

    /**
     * Sets the tick mark associated with the high end of the scroller.
     * 
     * @param highTick
     */
    public void setScrollerHighTick(int highTick) {
        currentTick = PApplet.constrain(highTick, ticksPerScroller, ticksPerTrack);
        updateGeometricalData();
    }
}