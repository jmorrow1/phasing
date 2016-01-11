package phases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import geom.Circle;
import geom.Line;
import processing.core.PApplet;
import views.View;

public class ViewMap {
	protected Circle planet;
	protected ArrayList<Line> edges = new ArrayList<Line>();
	protected HashMap<int[], Circle> satellites = new HashMap<int[], Circle>();
	protected View view;
	
	public ViewMap(View view) {
		this.view = view;
	}

	public void display(PApplet pa) {
		pa.strokeWeight(2);
		pa.stroke(0, 150);
		pa.noFill();
		planet.display(pa);
		Set<Map.Entry<int[], Circle>> set = satellites.entrySet();
		for (Map.Entry<int[], Circle> entry : set) {
			Circle circle = entry.getValue();
			circle.display(pa);
		}

		for (Line e : edges) {
			e.display(pa);
		}
	}
}