/**
 * This just draws the X axis of a graph with
 * the given specifications.  This includes the
 * little vertical lines and the labels.
 *
 * The method:
 *
 *	1.	Define a list of numbers to plot alongthe x-axis.
 *		The list's only requirement is that the numbers
 *		are in increasing order.
 *		@see	 m_orig
 *
 *	2.	Define a list of strings that match the list of
 *		numbers.  An element can be null or "" to indicate
 *		that nothing will be written for that particular
 *		corresponding number.
 *		@see m_labels
 *
 * 	3.	Define the drawing rectangle.  This is area that
 * 		we're allowed to draw in.
 * 		@see m_draw_rect
 *
 *
 *
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import static java.lang.Double.NaN;

import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_line;
import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_text;


public class GraphXAxis {

	//-------------------------------
	//	Constants
	//-------------------------------
	private static final String tag = "GraphXAxis";

	/** Pixels long the horizontal ticks at the bottom */
	public final static int DEFAULT_TICK_HEIGHT = 9;

	public final static int DEFAULT_LABEL_PADDING_TOP = 4;


	//-------------------------------
	//	Data
	//-------------------------------

	/**
	 * The numbers that we want to indicate (originals).
	 */
	protected List<Double> m_orig;

	/**
	 * The labels for the numbers.  Each element can be
	 * a string or null (to indicate no label for this
	 * number).  Or...the whole thing can be null which
	 * means that there are no labels at all.
	 */
	protected List<String> m_labels = null;


	/**
	 * The numbers after they have been mapped to my
	 * screen coordinate system.
	 */
	protected double[] m_converted = null;

	/**
	 * The logical minimum and the logical max for
	 * the original array of numbers.  Note that these
	 * do NOT have to be within their actual boundaries!
	 * (That's how we zoom!)
	 *
	 * The initial setting of NaN is used to see if
	 * the user remembered to set this number or not.
	 */
	protected double		m_logical_left = NaN,
						m_logical_right = NaN;

	/**
	 * The area to draw the x-axis graphics (including
	 * the labels!).
	 */
	protected Rect m_draw_rect = null;

	/** Min distance between two tick-marks. */
	public double m_min_dist = GView.DEFAULT_MIN_POINT_DISTANCE;

	/**
	 * Describes the length in pixels of each tick.
	 */
	protected int m_tick_height = DEFAULT_TICK_HEIGHT;

	/**
	 * Used when drawing tick-marks.  This is x-axis
	 * location (in screen coords) of the last
	 * number that was drawn.
	 */
	protected double m_last_num = Double.MAX_VALUE * -1;

	/**
	 * This tells us the right-most pixel that was drawn
	 * when drawing the last label.  Used to keep labels
	 * from overlapping each other.
	 */
	protected double m_last_label_pixel = Double.MAX_VALUE * -1;

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
	 * @param labels			List of labels that correspond to the
	 * 						numbers.  Null can be used for a individual
	 * 						item or for the whole list.
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
	 * 						the total height (lines + text).
	 */
	GraphXAxis (List<Double> nums, List<String> labels,
				double bounds_min, double bounds_max,
				Rect draw_area) {
		m_orig = new ArrayList<Double>();
		set_nums(nums, labels);

//		m_dirty = true;	 --taken care of in set_nums()

		set_bounds (bounds_min, bounds_max);
		set_draw_area(draw_area);
	} // constructor

	/*****************************
	 * Constructor.  Use this when you don't know the draw
	 * area yet.
	 *
	 * @param nums			An ordered list of numbers (increasing).
	 * 						They'll be mapped to screen coords for
	 * 						tick-marks along the x-axis.
	 *
	 * @param labels			List of labels that correspond to the
	 * 						numbers.  Null can be used for a individual
	 * 						item or for the whole list.
	 *
	 * @param bounds_min		The logical minimum for these base
	 * 						numbers.  Doesn't have to be within
	 * 						their actual bounds (that's how we
	 * 						zoom in!).
	 *
	 * @param bounds_max		Logical max (original numbers).
	 */
	GraphXAxis (List<Double> nums, List<String> labels,
				double bounds_min, double bounds_max) {
		m_orig = new ArrayList<Double>();
		set_nums (nums, labels);

//		m_dirty = true;	 --taken care of in set_nums()

		set_bounds (bounds_min, bounds_max);
	} // constructor

	/***************************
	 * Constructor for when you only know some points.
	 *
	 * @param nums			An ordered list of numbers (increasing).
	 * 						They'll be mapped to screen coords for
	 * 						tick-marks along the x-axis.
	 *
	 * @param labels			List of labels that correspond to the
	 * 						numbers.  Null can be used for a individual
	 * 						item or for the whole list.
	 */
	GraphXAxis (List<Double> nums, List<String> labels) {
		m_orig = new ArrayList<Double>();
		set_nums(nums, labels);
//		m_dirty = true;	 --taken care of in set_nums()

	} // constructor

	/***************************
	 * The most basic constructor.
	 */
	GraphXAxis () {
		m_orig = new ArrayList<Double>();
		m_dirty = true;
	} // constructor


	//***************************
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {

		// todo
		//	clean up the lists

		super.finalize();
	}

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
	 * @param nums	A bunch of numbers that will REPLACE the
	 * 				current point list!
	 *
	 * @param labels		Labels corresponding the numbers.
	 * 					You can use null instead of a string
	 * 					to indicate no label for that number
	 * 					or send in null to indicate no labels
	 * 					at all.
	 *
	 * @return	The quantity of numbers added.
	 */
	public int set_nums (List<Double> nums, List<String> labels) {
		m_orig.clear();
		m_orig.addAll(nums);

		m_labels = labels;

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
	 * @param labels		Labels corresponding the numbers.
	 * 					You can use null instead of a string
	 * 					to indicate no label for that number
	 * 					or send in null to indicate no labels
	 * 					at all.
	 *
	 * @return	The total quantity of numbers in our list.
	 */
	public int add_nums (List<Double> nums, List<String> labels) {
		// Only add labels if there are any!
		if (labels != null) {
			if (m_labels == null) {
				// Originally, there weren't any labels, so we
				// need to make dummy labels to match the old
				// numbers.
				m_labels = new ArrayList<String>(m_orig.size());
				for (int i = 0; i < m_labels.size(); i++) {
					m_labels.set(i, null);
				}
			}
			// Now add all the new labels.
			m_labels.addAll(labels);
		}

		m_orig.addAll(nums);

		// Test to make sure we did this right.
		if (labels != null) {
			if (m_orig.size() != m_labels.size()) {
				Log.e (tag, "Error in add_nums()!  Sizes of the two lists don't match!");
			}
		}

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
	 * @param label	A label for the number.  If you don't
	 * 				want one, send in null.
	 *
	 * @return	The total quantity of numbers.
	 */
	public int add_num (double num, String label) {
		if (label != null) {
			if (m_labels == null) {
				// No labels yet, so we need to construct
				// a list to hold all the old labels.
				m_labels = new ArrayList<String>(m_orig.size());
				for (int i = 0; i < m_labels.size(); i++) {
					m_labels.set(i, null);
				}
			}
			m_labels.add(label);
		}

		m_orig.add(num);

		// Test!
		if (label != null) {
			if (m_orig.size() != m_labels.size()) {
				Log.e (tag, "Error in add_num()!  Sizes of the two lists don't match!");
			}
		}

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
	 * 	m_draw_rect		Correctly set.
	 * 	m_orig			Holds the numbers to map.
	 *
	 * side effects:
	 * 	m_converted		Created to reflect the correct
	 * 					mapping from m_orig.
	 */
	public void map_points() {
		if (m_logical_left == NaN) {
			Log.e(tag, "map_points() called without first setting m_logical_left!");
			return;
		}
		if (m_logical_right == NaN) {
			Log.e(tag, "map_points() called without first setting m_logical_right!");
			return;
		}
		if (m_orig == null) {
			Log.e(tag, "map_points() called without first setting m_orig!");
			return;
		}
		if (m_draw_rect == null) {
			Log.e(tag, "map_points() called without first setting m_draw_rect!");
			return;
		}


		m_converted = new double[m_orig.size()];
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
	public void set_bounds (double left, double right) {
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
	public void set_draw_area (Rect draw_area) {
		if (m_draw_rect == null) {
			m_draw_rect = new Rect(draw_area);
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
	 * Now that everything is set up, let draw the
	 * lines of the axis and the text to go with it.
	 * <p>
	 * <b>Note</b>:<br/>
	 * 	This only draws those little vertical lines that
	 * 	mark important x positions.  The big horizontal
	 *	lines that cross the screen are drawn in the
	 *	GraphYAxis class.
	 *
	 * @param canvas
	 * @param paint		Should have its textsize set appropriately.
	 */
	public void draw(Canvas canvas, Paint paint) {
		if (m_dirty) {
			Log.w(tag, "Trying to draw with dirty numbers!  But I'll do it anyway, even if it is slow.");
			map_points();
		}

		for (int i = 0; i < m_converted.length; i++) {
			if ((m_converted[i] != NaN) && (m_last_num + m_min_dist < m_converted[i])) {
				draw_line(canvas,
						m_converted[i], m_draw_rect.top,
						m_converted[i], m_draw_rect.top - m_tick_height,
						paint);
				m_last_num = m_converted[i];

				if (m_labels != null) {
					draw_label(canvas, paint, m_labels.get(i), (double)(m_converted[i]), true);
				}
			}
		}

	} // draw()


	/****************************
	 * Draws a single label.  Designed to be called within
	 * the draw_lines() loop, this draws the label for the
	 * corresponding line (if there's room).
	 * <p>
	 * <b>preconditions</b>:<br/>
	 * 	<i>m_last_label</i> is set to the proper position (the right-most
	 * 		pixel of the previous label).
	 *
	 * <p>
	 * <b>side effects</b>:<br/>
	 * 	<i>m_last_label</i> will be set to the right-most pixel of
	 * 	the last label that was drawn with room (which may not
	 * 	be THIS label).
	 *
	 * @param label		The string to display here (or null to
	 * 					display nothin').
	 * @param x_pos		The x_position of the tick-mark we're
	 * 					labelling.
	 * @param centered	Should the label be centered on the
	 * 					tick-mark?
	 */
	private void draw_label (Canvas canvas, Paint paint,
							String label, double x_pos,
							boolean centered) {
		if (m_labels == null) {
			return;		// No labels to draw!
		}

		double x, y;
		Rect rect = new Rect();

		paint.setAntiAlias(true);	// neat text

		// Find the bounding rectangle for this text.
		paint.getTextBounds(label, 0, label.length(), rect);

		// Set the drawing positions.
		x = x_pos;
		if (centered) {
			x -= rect.exactCenterX();
		}

		// Is there enough room?
		if (x > m_last_label_pixel)	{
			y = m_draw_rect.top - m_tick_height - rect.height() - DEFAULT_LABEL_PADDING_TOP;
			draw_text(canvas, label, x, y, paint);
			m_last_label_pixel = x + rect.width();
		}

	} // draw_label(...)


}
