/**
 * Coalesces a bunch of stuff into one container.  This
 * class is used for each Graph (that's probably just ONE
 * aspect of an exercise) that's to be drawn within the
 * GView widget.
 *
 * Pretty much everything is public as this class just holds
 * stuff instead of does stuff.
 *
 */
package com.sleepfuriously.hpgworkout;

import android.graphics.RectF;

public class GraphCollection {

	/**
	 * A number that can be used to identify this instance.
	 * Don't have to use it.
	 */
	public int m_id = -1;


	public GraphLine m_line_graph = null;
//	public GraphXAxis m_x_axis_graph = null;
	public GraphYAxis m_y_axis_graph = null;

	/** The color to draw this particular graph */
	public int m_color;

	/** The logical limits of this graph */
	public RectF m_bounds;

}
