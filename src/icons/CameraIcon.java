package icons;

import phases.PhasesPApplet;

/**
 * 
 * @author James Morrow
 *
 */
public class CameraIcon implements Icon {
    private final int cameraType;
  
    public CameraIcon(int cameraType) {
        this.cameraType = cameraType;
    }
  
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        float x1 = x + 0.5f*radius;
        float x2 = x - 0.3f*radius;
        float x3 = x - 0.7f*radius;
        
        float w1 = radius;
        float w2 = 0.25f*radius;
        float h1 = 0.5f*radius;
        float h2 = 0.25f*radius;
        
        float y1 = y + 0.5f*radius;
        float y2 = y1 - radius*1.25f;
        float y3 = y1 - radius*0.66f;
        
        float arrowHeadSize = 0.2f*radius;
        
        pa.drawCameraIcon(x1, y1, w1, h1);
        
        pa.strokeWeight(radius/15f);
        pa.stroke(0);
        
        pa.line(x2, y1, x2, y2);
        pa.drawArrowHead(x2, y2, arrowHeadSize, -pa.HALF_PI, 0.75f*pa.PI);
        pa.line(x3, y1, x3, y3);
        pa.drawArrowHead(x3, y3, arrowHeadSize, -pa.HALF_PI, 0.75f*pa.PI);
        
        if (cameraType != FIXED) {
            float y4 = (cameraType == RELATIVE_TO_1) ? y3 : y2;
            pa.line(x1 + 0.05f*w1, y1, x1 + 0.05f*w1, y4);
            pa.drawArrowHead(x1 + 0.05f*w1, y4, arrowHeadSize, -pa.HALF_PI, 0.75f*pa.PI);
        }
       
        pa.stroke(0);
        pa.fill(255);
        pa.ellipseMode(pa.CENTER);
        pa.ellipse(x2, y1, w2, h2);
        pa.ellipse(x3, y1, w2, h2);
    }
}