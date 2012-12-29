/**
 * Using GraphMap, this does the same thing
 * but with 2D numbers (Points).
 *
 */
package com.sleepfuriously.hpgworkout;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class GraphMap2D {

	private static final String tag = "GraphMap2D";

	GraphMap x_mapper, y_mapper;


	/*******************************
	 * Constructor.
	 *
	 * This creates the mapping algorithm (see class docs for
	 * more details).
	 *
	 *  @param	src		The rectangular coordinate system to
	 *  					map FROM.
	 *  @param	dest		Where we want to map our point onto.
	 */
	GraphMap2D (RectF src, Rect dest) {
		if ((src.left == src.right) || (src.bottom == src.top)) {
			Log.e(tag, "a == b in Constructor!!!  Get ready for a divide by zero!!!");
		}
		x_mapper = new GraphMap(src.left, src.right, dest.left, dest.right);
		y_mapper = new GraphMap(src.bottom, src.top, dest.bottom, dest.top);
	} // map_setup (a, b, r, s)


	/***********************
	 * Using the parameters set up from map_setup(), this maps a number
	 * from [a, b] to [r, s].  Note that it's actually quite okay for
	 * n to be outside of the boundary of [a, b]; this method will extra-
	 * polate.
	 *
	 * @param pt		The 2D (coordinate) to map from our src space to
	 * 				the destination space.
	 *
	 * @return	The resulting point.
	 *
	 */
	public PointF map (PointF pt) {
		PointF new_pt = new PointF(x_mapper.map(pt.x),
								y_mapper.map(pt.y));
//		Log.d(tag, "mapping (" + pt.x + ", " + pt.y + ") --> (" + new_pt.x + ", " + new_pt.y + ")");
		return new_pt;
	} // map (n)

}
