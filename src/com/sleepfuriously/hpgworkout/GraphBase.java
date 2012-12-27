/**
 * This class is the base class of all the graphing
 * classes.  The sort of stuff that they all need
 * to do (like converting between formats, understanding
 * screen sizes and densities, etc. etc.) are done here.
 *
 * I'm changing some stuff here.  Each point that is to be
 * graphed MUST be on different days!  This is a pretty
 * big difference from how it was done before.  Now it's
 * the responsibility of the CALLER to figure what to do
 * with data that falls on the same day.  (The caller has
 * a better idea what to do--this class is context ignorant.)
 *
 * NOTE:
 * 	All numbers used by the children classes for positions
 * 	should assume that 0,0 is the bottom left of the screen,
 *  just like the viewer sees it.  This class will take
 *  care of mapping the numbers (even when zoomed).
 *
 *  todo:
 *  		No longer used.  Try to take it out!!!
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

@Deprecated
public class GraphBase {

	//-------------------------------
	//	Constants
	//-------------------------------
	private static final String tag = "GraphBase";


	//-------------------------------
	//	Static Data
	//-------------------------------

	/** Very useful to have! */
	static protected Context m_context;

	/** Describes the padding for the drawing area. */
	static protected Rect m_padding;

	/**
	 * Holds the data to display.  These are the actual
	 * numbers that are graphed.  Matches to the corresponding
	 * m_graph_nums_date element.
	 *
	 *	NOTE:
	 * If m_graph_nums are added on a day that already exists,
	 * then that value will be added to the existing m_graph_num
	 * and a new item will NOT be created.
	 */
	static protected List<Float> m_graph_nums = new ArrayList<Float>();

	/**
	 * The match to m_graph_nums.  This supplies the date for
	 * that value.  The date is used to provide a proper x-axis
	 * alignment for the value.
	 *
	 * 	NOTE:
	 * Repeating dates causes the value of original date to be
	 * ADDED by the new value, instead of a new element added
	 * to the list.
	 */
	static protected List<MyCalendar> m_graph_nums_date = new ArrayList<MyCalendar>();

	/**
	 * These define the parameters of the mapping function:
	 * 		[a, b]  =>  [r, s]
	 *
	 * 	See map_setup() and map() for more details.
	 */
	static private float a, b, r, s;

	/** The Canvas we draw on */
	static private Canvas m_canvas = null;


	//-------------------------------
	//	Instance Data
	//-------------------------------

	/** The background color.  Defaults to black */
	public int m_background_color = 0;


	//-----------------------------------------------------
	//	Methods
	//-----------------------------------------------------

	/*****************************
	 * Constructor.
	 */
	public GraphBase (Context context, Canvas canvas, Rect padding) {
		m_context = context;
		m_canvas = canvas;
		m_padding = padding;
		init();
	}

	/******************************
	 * Does the basic initializing.
	 */
	private void init() {
		clear_all();
	}

	/********************
	 * Clears the screen and also the data.
	 */
	private void clear_all() {
		m_graph_nums.clear();
		m_graph_nums_date.clear();
		m_canvas.drawRGB(Color.red(m_background_color),
						Color.green(m_background_color),
						Color.blue(m_background_color));
	} // clear_all()


	/********************
	 * This allows you to add a point to this Widget, one
	 * at a time.  It's up to the caller to figure out how
	 * to organize the points.  And yes, there should be
	 * no more than one point per day.  Figure it out, dammit.
	 *
	 * NOTE:
	 * 		The points need to be added in order.
	 *
	 * NOTE 2:
	 * 		The points must all occur on different days.
	 *
	 * @param x		The value of this point.  It can be in
	 * 				any range--the class will figure things out.
	 *
	 * @param date	The time of this set.
	 *
	 * @return	The number of points currently in the
	 * 			list AFTER this one has been added.
	 * 			Or -1 on error.
	 */
	static public int add_point (float x, MyCalendar date) {
		m_graph_nums.add(x);
		m_graph_nums_date.add(date);
		return m_graph_nums.size();
	} // add_point (x, date)

	/********************
	 * Same as above, but allows you to add an entire array
	 * at once.  Note that the arrays MUST have the same size
	 * and should be respective of each other.
	 *
	 * @param vals	A bunch of values.
	 *
	 * @param dates	All the dates.  They need to match their
	 * 				respective points and all be on different
	 * 				days.
	 *
	 * @return	The total number of points now in the lists
	 * 			or -1 if an error detected.
	 */
	static public int add_points (Float[] vals, MyCalendar[] dates) {
		if (vals.length != dates.length) {
			return -1;
		}

		Collections.addAll(m_graph_nums, vals);
		Collections.addAll(m_graph_nums_date, dates);
		return m_graph_nums.size();
	}

	/******************************
	 * @return	The width of the drawable area (padding
	 * 			is already taken into account).
	 */
	static public int get_width() {
		return m_canvas.getWidth() -
				(m_padding.left + m_padding.right);
	}

	/******************************
	 * @return	The height of the drawable area (padding
	 * 			is already taken into account).
	 */
	static public int get_canvas_height() {
		return m_canvas.getHeight() -
				(m_padding.top + m_padding.bottom);
	}

	/******************************
	 * This is a general map function setup.  It sets some private
	 * globals so that calls to map() work correctly.
	 *
	 * The mapping goes like this:
	 * 		map the range [a, b] to the range [r, s]
	 * 		Please note that the range INCLUDES the given numbers!
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
	 *  @param	a1	The lowest of the FROM portion of the map
	 *  @param	b1	The highest of the FROM portion.
	 *  @param	a2	The lowest of the TO (destination) of the mapping.
	 *  @param	b2	The highest of the TO.
	 */
	static public void map_setup (float _a, float _b,
							float _r, float _s) {
		if (_a == _b) {
			Log.e(tag, "a == b in map_y_setup!!!  Get ready for a divide by zero!!!");
		}
		a = _a;
		b = _b;
		r = _r;
		s = _s;
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
	static public float map (float n) {
		return (n - a) * ((s - r) / (b - a)) + r;
	} // map (n)

}
