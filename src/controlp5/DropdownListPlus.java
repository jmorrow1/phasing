package controlp5;

import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.DropdownList;
import processing.core.PGraphics;

/**
 * A subclass of ControlP5's DropdownList that circumvents an IndexOutOfBounds
 * bug that occurs with DropdownList.
 * 
 * @author James Morrow
 *
 */
public class DropdownListPlus extends DropdownList {

    /**
     * 
     * @param theControlP5
     *            The ControlP5 instance
     * @param theName
     *            The controller name
     */
    public DropdownListPlus(ControlP5 theControlP5, String theName) {
        super(theControlP5, theName);
    }

    public void draw(final PGraphics pg) {
        pg.pushMatrix();
        pg.translate(x(position), y(position));

        // draw white rectangle
        pg.noStroke();
        pg.fill(255);
        pg.rectMode(pg.CORNER);
        pg.rect(0, 0, this.getWidth(), this.getHeight());

        _myControllerView.display(pg, this);
        pg.popMatrix();
    }

    public void onRelease() {
        try {
            super.onRelease();
        }
        // the following line circumvents the IndexOutOfBounds bug:
        catch (IndexOutOfBoundsException e) {
        }
    }

}