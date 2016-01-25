package controlp5;

import controlP5.Button;
import controlP5.ControllerView;
import phases.PhasesPApplet;
import processing.core.PGraphics;

/**
 * Draws a ControlP5 button as an arrow that is pointed either left or right.
 * @author James Morrow
 *
 */
public class ArrowButtonView implements ControllerView<Button> {
    private boolean pointsRight;
    
    /**
     * 
     * @param pointsRight If true, the arrow points right. If false, the arrow points left.
     */
    public ArrowButtonView(boolean pointsRight) {
        this.pointsRight = pointsRight;
    }
  
    @Override
    public void display(PGraphics pg, Button b) {
        pg.noStroke();
        int c = b.isMouseOver() ? b.getColor().getActive() : b.getColor().getForeground();
        pg.fill(b.getColor().getBackground());
        pg.rectMode(pg.CORNER);
        pg.rect(0, 0, b.getWidth(), b.getHeight());
         
        float x = b.getWidth()/2f;
        float y = b.getHeight()/2f;
        float halfWidth = b.getWidth() * 0.4f;
        float arrowHeadLength = halfWidth*0.6f;
        pg.strokeWeight(3);
        pg.stroke(c);
        pg.line(x - halfWidth, y, x + halfWidth, y);
        if (pointsRight) {
            PhasesPApplet.drawArrowHead(x + halfWidth, y, arrowHeadLength, 0, 0.75f*pg.PI, pg); 
        }
        else {
        	PhasesPApplet.drawArrowHead(x - halfWidth, y, arrowHeadLength, pg.PI, 0.75f*pg.PI, pg);
        }
    }
}