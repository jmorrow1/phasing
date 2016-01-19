package controlp5;

import controlP5.Button;
import controlP5.ControllerView;
import phases.PhasesPApplet;
import processing.core.PGraphics;

public class ArrowButtonView implements ControllerView<Button> {
    private boolean faceRight;
    
    public ArrowButtonView(boolean faceRight) {
        this.faceRight = faceRight;
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
        if (faceRight) {
            PhasesPApplet.drawArrowHead(x + halfWidth, y, arrowHeadLength, 0, 0.75f*pg.PI, pg); 
        }
        else {
        	PhasesPApplet.drawArrowHead(x - halfWidth, y, arrowHeadLength, pg.PI, 0.75f*pg.PI, pg);
        }
    }
}