package icons;

import phasing.PhasesPApplet;
import views.ViewVariableInfo;

/**
 * 
 * @author James Morrow
 *
 */
public interface Icon extends ViewVariableInfo {
	public void draw(float cenx, float ceny, float radius, PhasesPApplet pa);
}
