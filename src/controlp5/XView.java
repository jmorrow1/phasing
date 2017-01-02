package controlp5;

import controlP5.Button;
import controlP5.ControllerView;
import processing.core.PGraphics;

/**
 * 
 * @author James Morrow
 *
 */
public class XView implements ControllerView<Button> {
    @Override
    public void display(PGraphics pg, Button b) {
        pg.noStroke();
        int c = b.isMouseOver() ? b.getColor().getActive() : b.getColor().getForeground();
        pg.fill(b.getColor().getBackground());
        pg.rectMode(pg.CORNER);
        pg.rect(0, 0, b.getWidth(), b.getHeight());

        float x = b.getWidth() / 2f;
        float y = b.getHeight() / 2f;
        float halfWidth = b.getWidth() * 0.4f;
        float halfHeight = b.getHeight() * 0.4f;
        float arrowHeadLength = halfWidth * 0.6f;
        pg.strokeWeight(3);
        pg.stroke(c);

        pg.line(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight);
        pg.line(x - halfWidth, y + halfHeight, x + halfWidth, y - halfHeight);
    }
}
