/**
 * This just draws the X axis of a graph with
 * the given specifications.  This includes the
 * little vertical lines and the labels.
 *
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import static java.lang.Float.NaN;

import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_line;


public class GraphXAxis {

	//-------------------------------
	//	Constants
	//-------------------------------
	private static final String tag = "GraphXAxis";

	/** Pixels long the horizontal ticks at the bottom */
	public final static int DEFAULT_TICK_HEIGHT = 5;

	//-------------------------------
	//	Data
	//-------------------------------

	/**
	 * The numbers that we want to indicate (originals).
	 */
	protected List<Float> m_orig;

	/**
	 * The numbers after they have been mapped to my
	 * screen coordinate system.
	 */
	protected float[] m_converted = null;

	/**
	 * The logical minimum and the logical max for
	 * the original array of numbers.  Note that these
	 * do NOT have to be within their actual boundaries!
	 * (That's how we zoom!)
	 *
	 * The initial setting of NaN is used to see if
	 * the user remembered to set this number or not.
	 */
	protected float	m_logical_left = NaN,
					m_logical_right = NaN;

	/**
	 * The area to draw the x-axis graphics (including
	 * the labels!).
	 */
	protected RectF m_draw_rect = null;

	/** Min distance between two tick-marks. */
	public float m_min_dist = GView.DEFAULT_MIN_POINT_DISTANCE;

	/**
	 * Describes the length in pixels of each tick.
	 */
	protected int m_tick_height = DEFAULT_TICK_HEIGHT;

	/** Labels for the x-axis */
	protected String m_label_start = null, m_label_end = null;

	/**
	 * Tells if we need to remap our original numbers
	 * to their screen coords.
	 */
	private boolean m_dirty = true;



	//-------------------------------
	//	Constructors
	//-------------------------------

	/*****************************
	 * Constructor.
	 *
	 * @param nums			An ordered list of numbers (increasing).
	 * 						They'll be mapped to screen coords for
	 * 						tick-marks along the x-axis.
	 *
	 * @param bounds_min		The logical minimum for these base
	 * 						numbers.  Doesn't have to be within
	 * 						their actual bounds (that's how we
	 * 						zoom in!).
	 *
	 * @param bounds_max		Logical max (original numbers).
	 *
	 * @param draw_area		The area of the canvas to draw our
	 * 						graph in.  Need this to properly
	 * 						set up the mapping functions.
	 * 						- Make sure that the width (left & right)
	 * 						are the same as the call to GraphLine!
	 * 						Otherwise the ticks won't match the
	 * 						graph.
	 * 						- The height (top & bottom) describe
	 * 						the total height (lines + font size).
	 */
	GraphXAxis (List<Float> nums,
				float bounds_min, float bounds_max,
				RectF draw_area) {
		m_orig = new ArrayList<Float>();
		m_dirty = true;
		set_bounds (bounds_min, bounds_max);
		set_draw_area(draw_area);
		set_nums(nums);
	} // constructor

	/*****************************
	 * Constructor.  Use this when you don't know the draw
	 * area yet.
	 *
	 * @param nums			An ordered list of numbers (increasing).
	 * 						They'll be mapped to screen coords for
	 * 						tick-marks along the x-axis.
	 *
	 * @param bounds_min		The logical minimum for these base
	 * 						numbers.  Doesn't have to be within
	 * 						their actual bounds (that's how we
	 * 						zoom in!).
	 *
	 * @param bounds_max		Logical max (original numbers).
	 */
	GraphXAxis (List<Float> nums,
				float bounds_min, float bounds_max) {
		m_orig = new ArrayList<Float>(nums);
		m_dirty = true;
		set_bounds (bounds_min, bounds_max);
	} // constructor

	/***************************
	 * Constructor for when you only know some points.
	 *
	 * @param nums			An ordered list of numbers (increasing).
	 * 						They'll be mapped to screen coords for
	 * 						tick-marks along the x-axis.
	 *
	 */
	GraphXAxis (List<Float> nums) {
		m_orig = new ArrayList<Float>(nums);
		m_dirty = true;
	} // constructor

	/***************************
	 * The most basic constructor.
	 */
	GraphXAxis () {
		m_orig = new ArrayList<Float>();
		m_dirty = true;
	} // constructor


	//-------------------------------
	//	Methods
	//-------------------------------

	/****************************
	 * Removes all the numbers from our list of
	 * original numbers.
	 */
	public void delete_nums() {
		m_orig.clear();
		m_dirty = true;
	}


	/****************************
	 * Takes a bunch of numbers and creates our internal
	 * array from them.  If you didn't construct with a list,
	 * you gotta do this before drawing.
	 *
	 * @param pts	A bunch of points that will REPLACE the
	 * 				current point list!
	 *
	 * @return	The quantity of numbers added.
	 */
	public int set_nums (List<Float> nums) {
		m_orig.clear();
		m_orig.addAll(nums);
		m_dirty = true;
		return m_orig.size();
	}

	/*****************************
	 * Adds the given numbers to our list of original
	 * numbers.
	 *
	 * @param nums	A bunch of numbers that'll be added
	 * 				to the current list.
	 *
	 * @return	The total quantity of numbers in our list.
	 */
	public int add_nums (List<Float> nums) {
		m_orig.addAll(nums);
		m_dirty = true;
		return m_orig.size();
	}

	/****************************
	 * Adds just one number to our list.
	 *
	 * side effects:
	 * 	m_orig		A point is added.
	 *
	 * 	m_converted	No longer is correctly mapped.
	 *
	 * @param num	The number to add.
	 *
	 * @return	The total quantity of numbers.
	 */
	public int add_num (float num) {
		m_orig.add(num);
		m_dirty = true;
		return m_orig.size();
	}

	/****************************
	 * This does the calculations necessary to go from
	 * the abstract list of numbers to the given screen
	 * coords.
	 *
	 * preconditions:
	 * 	m_logical_left	Correctly set.
	 * 	m_logical_right	Correctly set.
	 * 	m_canvas_rect	Correctly set.
	 * 	m_orig			Holds the numbers to map.
	 *
	 * side effects:
	 * 	m_converted		Created to reflect the correct
	 * 					mapping from m_orig.
	 */
	public void map_points() {
		m_converted = new float[m_orig.size()];
		GraphMap mapper = new GraphMap(m_logical_left, m_logical_right,
									m_draw_rect.left, m_draw_rect.right);

		// map each point
		for (int i = 0; i < m_orig.size(); i++) {
			if (m_orig.get(i) == null) {
				m_converted[i] = NaN;	// Indicates not valid
			}
			else {
				m_converted[i] = mapper.map(m_orig.get(i));
			}
		}
		m_dirty = false;
	} // map_points()



	/*****************************
	 * If the logical boundaries of this data changes, call this!
	 * (eg. when zooming).
	 *
	 * @param left		The logical left boundary (lowest number)
	 * 					in the original number space.
	 *
	 * @param right		The right boundary (highest number).
	 */
	public void set_bounds (float left, float right) {
		m_logical_left = left;
		m_logical_right = right;
		m_dirty = true;
	}

	/*****************************
	 * Has the caller set the boundary or not?
	 */
	public boolean is_bounds_set() {
		return m_logical_left != NaN;
	}

	/*****************************
	 * If the draw area changes, call this method!
	 *
	 * @param draw_area		The part of the canvas that we draw to
	 * 						(in my screen format!).
	 */
	public void set_draw_area (RectF draw_area) {
		if (m_draw_rect == null) {
			m_draw_rect = new RectF(draw_area);
		}
		else {
			m_draw_rect.set(draw_area);
		}
		m_dirty = true;
	}

	/*****************************
	 * Has the draw area rectangle been set?
	 */
	public boolean is_draw_area_set() {
		return m_draw_rect != null;
	}


	/*****************************
	 * Set the labels for the first and last tick-mark
	 * along the x-axis.  Supply null if you don't want
	 * a label (or don't call at all if you don't want
	 * any labels).
	 *
	 * @param start		Label for the first (left-most
	 * 					tick-mark.
	 *
	 * @param end		Label for the last tick-mark.
	 */
	public void set_labels (String start, String end) {
		m_label_start = start;
		m_label_end = end;
	}

	/*****************************
	 * Now that everything is set up, let draw the
	 * lines of the axis and the text to go with it.
	 *
	 * @param canvas
	 * @param paint		Should have its textsize set appropriately.
	 */
	public void draw(Canvas canvas, Paint paint) {
		draw_lines(canvas, paint);
		draw_labels(canvas, paint);
	} // draw()

	/*****************************
	 * Draws the line portions of the x-axis.
	 *
	 * Note:
	 * 	This only draws those little vertical lines that
	 * 	mark important x positions.  The big horizontal
	 *	lines that cross the screen are drawn in the
	 *	GraphYAxis class.
	 */
	private void draw_lines (Canvas canvas, Paint paint) {
		if (m_dirty) {
			Log.e(tag, "Trying to draw with dirty numbers!  But I'll do it anyway, even if it is slow.");
			map_points();
		}

		for (int i = 0; i < m_converted.length; i++) {
			if (m_converted[i] != NaN) {
				draw_line(canvas,
						m_converted[i], m_draw_rect.bottom,
						m_converted[i], m_draw_rect.top,
						paint);
			}
		}
	} // draw_lines()


	/*****************************
	 * Draws the labels on our x-axis.
	 */
	private void draw_labels (Canvas canvas, Paint paint) {

		// todo:

	} // draw_labels()


}
