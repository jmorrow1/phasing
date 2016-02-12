package icons;

import phases.PhasesPApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class ScoreModeIcon implements Icon {
    private final int mode;
    
    public ScoreModeIcon(int mode) {
        this.mode = mode;
    }
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
    	//dot and camera
        pa.noStroke();
        pa.fill(0);
        pa.ellipseMode(pa.RADIUS);
        pa.ellipse(x - radius*0.5f, y + 0.5f*radius, 0.2f*radius, 0.2f*radius);
        pa.drawCameraIcon(x + radius*0.5f, y + 0.5f*radius, radius, 0.5f*radius);
        
        //velocity arrow
        pa.stroke(0);
    	pa.strokeWeight(radius/15f);
    	float arrowHeadSize = radius*0.2f;
        if (mode == MOVE_NOTES) {
        	pa.line(x + radius*0.5f, y + radius*0.1f, x + radius*0.5f, y - 0.75f*radius);
            pa.drawArrowHead(x + radius*0.5f, y - radius*0.75f, arrowHeadSize, -pa.HALF_PI, pa.PI*0.75f);
        }
        else if (mode == MOVE_SPAWN_POINT) {
        	pa.line(x - radius*0.5f, y + radius*0.1f, x - radius*0.5f, y - 0.75f*radius);
            pa.drawArrowHead(x - radius*0.5f, y - radius*0.75f, arrowHeadSize, -pa.HALF_PI, pa.PI*0.75f);
        }
    }
}