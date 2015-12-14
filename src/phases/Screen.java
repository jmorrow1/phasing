package phases;

import java.awt.event.MouseListener;

public abstract class Screen {
	protected PhasesPApplet pa;
	
	public Screen(PhasesPApplet pa) {
		this.pa = pa;
		
	}
	public void mousePressed() {}
	public void mouseReleased() {}
	public void mouseMoved() {}
	public void keyPressed() {}
	public void keyReleased() {}
	
	public abstract void onEnter();
	public abstract void draw();
}
