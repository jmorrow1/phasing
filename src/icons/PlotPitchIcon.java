package icons;

import phases.PhasesPApplet;

public class PlotPitchIcon extends Icon {
    private boolean doPlotPitch;
  
    public PlotPitchIcon(boolean doPlotPitch) {
        this.doPlotPitch = doPlotPitch;
    }
    
    public void draw(float x, float y, float radius, PhasesPApplet pa) {
        float x1 = x - 0.6f*radius;
        float x2 = x + 0.6f*radius;
        float y1 = y - 0.6f*radius;
        float y2 = y + 0.6f*radius;
        float arrowHeadSize = 0.25f*radius;
        pa.stroke(0);
        pa.strokeWeight(3);
        
        pa.line(x1, y, x2, y);
        pa.drawArrowHead(x1, y, arrowHeadSize, pa.PI, 0.75f*pa.PI);
        pa.drawArrowHead(x2, y, arrowHeadSize, 0, 0.75f*pa.PI);
        if (doPlotPitch) {
            pa.line(x, y1, x, y2);
            pa.drawArrowHead(x, y1, arrowHeadSize, -pa.HALF_PI, 0.75f*pa.PI);
            pa.drawArrowHead(x, y2, arrowHeadSize, pa.HALF_PI, 0.75f*pa.PI);
        }
    }
}