package icons;

import phases.PhasesPApplet;
import views.ViewVariableInfo;

/**
 * 
 * @author James Morrow
 *
 */
public interface Icon extends ViewVariableInfo {
	public void draw(float x, float y, float radius, PhasesPApplet pa);
}
