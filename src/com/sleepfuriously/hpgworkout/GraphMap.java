/**
 * A class to encapsulate mapping functions.
 *
 * Use this to map numbers from one range to
 * another.
 *
 * The mapping goes like this:
 * 		map the range [a, b] to the range [r, s]
 * 		Note that the range INCLUDES the given numbers!
 *
 * So if the map is [1, 4] to [0, 100] then calling map(1) = 0,
 * map(2) = 33.3, map(3) = 66.7, map(4) = 100.
 *
 * This generally will be used so that [a, b] is the range of
 * of the numbers to graph, and [r, s] is the size of the drawing
 * window.
 *
 *  NOTE:
 *  		a != b		This will cause an divide by zero
 *  					(and doesn't make sense anyway).
 *
 *  NOTE:
 *  		It's actually okay to try to map numbers that
 *  		are out of the original range.  This class will
 *  		extrapolate.
 *
 */
package com.sleepfuriously.hpgworkout;

import android.util.Log;

public class GraphMap {

	private static final String tag = "GraphMap";

	float a, b, r, s, delta_ab, delta_rs;


	/*******************************
	 * Constructor.
	 *
	 * This creates the mapping algorithm (see class docs for
	 * more details).
	 *
	 *  @param	a	The lowest of the FROM portion of the map
	 *  @param	b	The highest of the FROM portion.
	 *  @param	r	The lowest of the TO (destination) of the mapping.
	 *  @param	s	The highest of the TO.
	 *
	 */
	GraphMap (float _a, float _b,
			float _r, float _s) {
		if (_a == _b) {
			Log.e(tag, "a == b in Constructor!!!  Get ready for a divide by zero!!!");
		}
		a = _a;
		b = _b;
		r = _r;
		s = _s;
		delta_ab = b - a;
		delta_rs = s - r;
//		Log.d(tag, "Setting up map: [" + a + ", " + b + "] --> [" + r + ", " + s + "]");
	} // map_setup (a, b, r, s)


	/***********************
	 * Using the parameters set up from map_setup(), this maps a number
	 * from [a, b] to [r, s].  Note that it's actually quite okay for
	 * n to be outside of the boundary of [a, b]; this method will extra-
	 * polate.
	 *
	 * @param n		The number to map from [a, b] into the [r, s] space.
	 *
	 * @return	The resulting number in the [r, s] space.
	 */
	public float map (float n) {
		float val = (n - a) * (delta_rs / delta_ab) + r;
//		Log.d(tag, "mapping " + n + " --> " + val);
		return val;
	} // map (n)

}
