package icons;

import phases.Option.Transform;
import phases.PhasesPApplet;

public class TransformIcon extends Icon {
    public TransformIcon(int value) {
        super(value);
    }
  
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        pa.noFill();
        pa.stroke(0);
        pa.strokeWeight(4);
        
        float arrowHeadSize = radius / 4f;
        
        if (value == Transform.TRANSLATE) {
            float x1 = x - radius + 5;
            float x2 = x + radius - 5;
            pa.line(x1, y, x2, y);
            pa.drawArrowHead(x2, y, arrowHeadSize, 0, 0.75f*pa.PI);
        }
        else if (value == Transform.ROTATE) {
            pa.ellipseMode(pa.RADIUS); 
            pa.arc(x, y, radius - 10, radius - 10, pa.QUARTER_PI, pa.TWO_PI);
            pa.drawArrowHead(x + radius - 10, y, arrowHeadSize, pa.HALF_PI, 0.75f*pa.PI);
        }
        else if (value == Transform.ROTATE_Z) {
             
        }
    }
}