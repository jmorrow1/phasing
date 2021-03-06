package controlp5;

import controlP5.Button;
import controlP5.ControllerView;
import phasing.PhasesPApplet;
import processing.core.PGraphics;

/**
 * Draws a ControlP5 button as either a plus sign (+) or a minus sign (-)
 * 
 * @author James Morrow
 *
 */
public class PlusMinusButtonView implements ControllerView<Button> {
    private boolean plus;

    /**
     * 
     * @param plus
     *            If true, it draws a plus sign (+) when display is called. If
     *            false, it draws a minus sign (-) when display is called.
     */
    public PlusMinusButtonView(boolean plus) {
        this.plus = plus;
    }

    @Override
    public void display(PGraphics pg, Button b) {
        pg.noStroke();
        int c = b.isMouseOver() ? b.getColor().getActive() : b.getColor().getForeground();
        pg.fill(b.getColor().getBackground());
        pg.rectMode(pg.CORNER);
        pg.rect(0, 0, b.getWidth(), b.getHeight());

        float x = b.getWidth() / 2f;
        float y = b.getHeight() / 2f;
        float halfWidth = b.getWidth() * 0.5f;
        float halfHeight = b.getHeight() * 0.5f;
        float arrowHeadLength = halfWidth * 0.6f;
        pg.strokeWeight(3);
        pg.stroke(c);
        pg.line(x - halfWidth, y, x + halfWidth, y);
        if (plus) {
            pg.line(x, y - halfHeight, x, y + halfHeight);
        }

    }
}
