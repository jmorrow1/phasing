package phasing;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Slider;
import controlp5.XView;
import geom.Rect;

/**
 * 
 * @author James Morrow
 *
 */
public class PauseMenu {
	//papplet
	private PhasesPApplet pa;
	
	//area
	private Rect rect;
	
	//controlp5
	private ControlP5 cp5;
	private Button closeButton;
	private Slider volumeSlider;
	
	/**************************
	 ***** Initialization *****
	 **************************/
	
	/**
	 * 
	 * @param rect The area in which to draw the pause menu.
	 * @param pa The PApplet to draw to.
	 */
	public PauseMenu(Rect rect, PhasesPApplet pa) {
		this.rect = rect;
		this.pa = pa;
		cp5 = new ControlP5(pa);
	}
	
	/**
	 * Initializes the slider that controls the master volume.
	 */
	private void initVolumeSlider() {
		int width = 100;
		int height = 20;
		volumeSlider = cp5.addSlider("adjustVolume")
				          .setSize(width, height)
				          .plugTo(this)
				          ;
	}
	
	/**
	 * Initializes the button that closes the pause menu.
	 */
	private void initCloseButton() {
		int width = 30;
		int height = 30;
		closeButton = cp5.addButton("closeMenu")
				         .setPosition(rect.getX2() - width, rect.getY1() + height)
				         .setSize(width, height)
				         .setView(new XView())
				         .plugTo(this)
				         ;
	}
	
	/*******************
	 ***** Drawing *****
	 *******************/
	
	/**
	 * Draws the pause menu.
	 */
	public void display() {
		pa.stroke(50);
		pa.strokeWeight(8);
		pa.fill(pa.getColor1());
		pa.rectMode(pa.CENTER);
		rect.display(pa);
	}
	
	/************************************
	 ***** ControlP5 Event Handling *****
	 ************************************/
	
	public void closeMenu(ControlEvent e) {
		pa.togglePause();
	}
	
	public void adjustVolume(ControlEvent e) {
		
	}
	
	/********************************
	 ***** Input Event Handling *****
	 ********************************/
	
	public void mousePressed() {
		
	}
	
	public void mouseReleased() {
		
	}
	
	public void mouseMoved() {
		
	}
	
	public void mouseDragged() {
		
	}
	
	public void keyPressed() {
		
	}
	
	public void keyReleased() {
		
	}
	
	/*********************************
	 ***** Screen Event Handling *****
	 *********************************/

	public void resized() {
		
	}

	public void onEnter() {
		cp5.show();
	}

	public void onExit() {
		cp5.hide();
	}
}