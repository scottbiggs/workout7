/**
 * Just like a PointF, except that this works with doubles.
 *
 * (I did something similar with RectD.)
 */
package com.sleepfuriously.hpgworkout;

import android.graphics.Point;
import android.graphics.PointF;

public class PointD {

	public double x, y;

	//------------------------
	//	Constructors
	//------------------------

	PointD() {
		set(0, 0);
	}
	PointD(double x, double y) {
		set (x, y);
	}
	PointD (PointD pt) {
		set (pt);
	}
	PointD (PointF pt) {
		set (pt);
	}
	PointD (Point pt) {
		set (pt);
	}

	//------------------------
	//	Methods
	//------------------------

	public void set (double x, double y) {
		this.x = x;	this.y = y;
	}

	public void set (PointD pt) {
		x = pt.x;	y = pt.y;
	}

	public void set (PointF pt) {
		x = pt.x;	y = pt.y;
	}

	public void set (Point pt) {
		x = pt.x;	y = pt.y;
	}

	public boolean equals (double x, double y) {
		return ((this.x == x) && (this.y == y));
	}

	/** Return the euclidian distance from (0,0) to the point */
	public double length() {
		return Math.sqrt(x * x + y * y);
	}

	/** Negate the point's coordinates . */
	public void negate() {
		x *= -1d;	y *= -1d;
	}

	@Override
	public String toString() {
		return "PointD (" + x + ", " + y + ")";
	}


}
