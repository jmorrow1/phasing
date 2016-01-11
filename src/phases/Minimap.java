package phases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import geom.Circle;
import geom.Line;
import processing.core.PApplet;
import views.View;

public class Minimap {
	//view
	private View view;
	
	//geometrical data
	private Circle planet;
	private ArrayList<Line> edges = new ArrayList<Line>();
	private HashMap<int[], Circle> satellites = new HashMap<int[], Circle>();
	
	//parameters for constructing geometrical data
	private float cenx, ceny, halfWidth1, halfHeight1, halfWidth2, halfHeight2, nodeRadius;
	
	public Minimap(float cenx, float ceny, float halfWidth1, float halfHeight1, float halfWidth2, float halfHeight2, float nodeRadius, View view) {
		this.view = view;
		this.cenx = cenx;
		this.ceny = ceny;
		this.halfWidth1 = halfWidth1;
		this.halfHeight1 = halfHeight1;
		this.halfWidth2 = halfWidth2;
		this.halfHeight2 = halfHeight2;
		this.nodeRadius = nodeRadius;
		setupViewGraph(view.getAllNeighborConfigIds());
	}
	
	private void setupViewGraph(int[][] satelliteKeys) {
		int numSatellites = satelliteKeys.length;

		planet = new Circle(cenx, ceny, nodeRadius);
		
		satellites.clear();
		edges.clear();
		
		float theta = 0;
		float dTheta = PApplet.TWO_PI / numSatellites;
		for (int i=0; i<numSatellites; i++) {
			float angle = theta + PApplet.map((float)Math.random(), -1, 1, -dTheta*0.125f, dTheta*0.125f);
			
			float lerpAmt = (float)Math.random();
			Circle sat = new Circle(cenx + PApplet.cos(angle) * PApplet.lerp(halfWidth1, halfWidth2, lerpAmt),
					                ceny + PApplet.sin(angle) * PApplet.lerp(halfHeight1, halfHeight2, lerpAmt), nodeRadius);
			
			satellites.put(satelliteKeys[i], sat);
			
			float a1 = PApplet.atan2(planet.getY() - sat.getY(), planet.getX() - sat.getX());
			float a2 = a1 + PApplet.PI;
			edges.add(new Line(sat.getX() + PApplet.cos(a1) * sat.getRadius(), sat.getY() + PApplet.sin(a1) * sat.getRadius(),
						planet.getX() + PApplet.cos(a2) * planet.getRadius(), planet.getY() + PApplet.sin(a2) * planet.getRadius()));
			
			theta += dTheta;
		}
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
	
	public void mousePressed(PApplet pa) {
		boolean viewChanged = false;
		Set<Map.Entry<int[], Circle>> set = satellites.entrySet();
		for (Map.Entry<int[], Circle> entry : set) {
			Circle sat = entry.getValue();
			if (sat.intersects(pa.mouseX, pa.mouseY)) {
				view.adoptConfig(entry.getKey());
				viewChanged = true;
			}
		}
		
		if (viewChanged) {
			setupViewGraph(view.getAllNeighborConfigIds());
		}
	}
	
	public boolean intersects(float x, float y) {
		float maxRadius = PApplet.max(halfWidth2, halfHeight2) + nodeRadius;
		return cenx - maxRadius <= x && x <= cenx + maxRadius && ceny - maxRadius <= y && y <= ceny + maxRadius;
	}
}
