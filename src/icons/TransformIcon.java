package icons;

import phases.PhasesPApplet;

public class TransformIcon implements Icon {
    private final int transformType;
  
    public TransformIcon(int transformType) {
        this.transformType = transformType;
    }
  
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        pa.noFill();
        pa.stroke(0);
        pa.strokeWeight(radius/10f);
        
        float arrowHeadSize = radius / 4f;
        
        if (transformType == TRANSLATE) {
            float x1 = x - radius + 5;
            float x2 = x + radius - 5;
            pa.line(x1, y, x2, y);
            pa.drawArrowHead(x2, y, arrowHeadSize, 0, 0.75f*pa.PI);
        }
        else if (transformType == ROTATE) {
            pa.ellipseMode(pa.RADIUS); 
            pa.arc(x, y, radius - 10, radius - 10, pa.QUARTER_PI, pa.TWO_PI);
            pa.drawArrowHead(x + radius - 10, y, arrowHeadSize, pa.HALF_PI, 0.75f*pa.PI);
        }
    }
}