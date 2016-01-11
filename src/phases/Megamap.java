package phases;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import generativedesign.Node;
import generativedesign.Spring;
import processing.core.PApplet;
import views.View;

public class Megamap {
	//geometrical parameters
	private float cenx, ceny, nodeRadius;
	
	//view
	private View view;
	
	//nodes and edges (with physics)
	private HashMap<int[], Node> nodeMap = new HashMap<int[], Node>();
	private Node[] nodeArray;
	private ArrayList<Spring> springs = new ArrayList<Spring>();
	
	public Megamap(float cenx, float ceny, float nodeRadius, View view) {
		this.view = view;
		this.cenx = cenx;
		this.ceny = ceny;
		this.nodeRadius = nodeRadius;
		
		int[][] nodeIds = view.getAllConfigIds();
		nodeArray = new Node[nodeIds.length];
		for (int i=0; i<nodeIds.length; i++) {
			Node n = new Node();
			nodeMap.put(nodeIds[i], n);
			nodeArray[i] = n;
		}
		
		for (int i=0; i<nodeIds.length; i++) {
			Node fromNode = nodeMap.get(nodeIds[i]);
			int[][] toNodeIds = view.getAllNeighborConfigIds(nodeIds[i]);
			for (int j=0; j<toNodeIds.length; j++) {
				Node toNode = nodeMap.get(toNodeIds[j]);
						
				//springs.add(new Spring(toNode, fromNode));
			}
		}
	}
	
	public void update() {
		for (int i=0; i<nodeArray.length; i++) {
			nodeArray[i].attract(nodeArray);
		}

	    for (Spring s : springs) {
	    	s.update();
	    }

	    for (int i=0; i<nodeArray.length; i++) {
	    	nodeArray[i].update();
	    } 
	}
	
	public void display(PApplet pa) {
		pa.stroke(0);
		pa.noFill();
		
		pa.ellipseMode(pa.RADIUS);
		
		Set<Entry<int[], Node>> set = nodeMap.entrySet();
		for (Entry<int[], Node> entry : set) {
			Node n = entry.getValue();
			pa.ellipse(n.x, n.y, nodeRadius, nodeRadius);
		}
		
		for (Spring s : springs) {
			pa.line(s.fromNode.x, s.fromNode.y, s.toNode.x, s.toNode.y);
		}
	}
}
