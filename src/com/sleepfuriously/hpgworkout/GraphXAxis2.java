/**
 * My second version of the X axis drawer for the
 * Graph functionality.  This version supports
 * zooming and panning!  Wheeee!
 *
 * Note that this draws just the
 * little vertical lines and the labels.
 *
 * The method:
 *
 *	1.	Date List.  The numbers that represent the dates
 *		to plot along the x-axis.  They must be in
 *		ascending order.  These are akin to the
 *		World Points in the Graph2 class (just the
 *		x-values).
 *
 *			add_date (date)			- appends one date to the list
 *			add_dates (dates[])		- appends a list of dates.
 *			clear_dates()			- removes all dates
 *			get_num_dates()
 *			get_date_at (index)
 *
 *	2.	Date Window.  This defines the logical boundaries
 *		of the dates that we want to see, much like the
 *		World Rectangle in Graph2.  Any dates outside of
 *		this window will not be visible.  Change this
 *		for zooms and pans.
 *
 *			set_date_window (left, right)
 *			get_date_window_left()
 *			get_date_window_right()
 *
 *	3.	View Rect (drawing rect).  This is the rectangle
 *		that defines the area of the screen to draw
 *		everything related to the x-axis in.
 *
 * 	4.	Label List.  This is a list of strings that
 * 		correspond to the Date List.  The strings will
 * 		be drawn with each tic-mark (provided there's
 * 		enough room).
 *
 * From the Date List, a new list of will be created, which
 * define the locations of the tic-marks (screen coords).
 * Beneath each tic-mark, the appropriate string will be
 * displayed (space permitting).
 *
 * To make it all happen:
 *
 * 		draw (canvas, paint)
 *
 * Yay!
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import static java.lang.Double.NaN;

import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_line;
import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_text;


public class GraphXAxis2 {

	//-------------------------------
	//	Constants
	//-------------------------------
	private static final String tag = "GraphXAxis2";

	/** Pixels long the horizontal tics at the bottom */
	public final static int DEFAULT_TIC_HEIGHT = 9;

	public final static int DEFAULT_LABEL_PADDING_TOP = 4;


	//-------------------------------
	//	Data
	//-------------------------------

	/**
	 * When TRUE, the m_tics needs creating.
	 */
	private boolean m_dirty = true;


	/**
	 * The numbers (dates) that we want to create
	 * tic-marks for.  This is the original numbers
	 * as added directly by the caller.
	 */
	private List<Long> m_dates;

	/**
	 * The left and right side of the window we want
	 * to view of the dates (@see m_date_list).
	 */
	private double m_date_left = NaN, m_date_right = NaN;

	/**
	 * The x screen coordinates for all the tic-marks.
	 * These correspond directly to m_date_list and are
	 * created in calc_tics().
	 */
	private List<Double> m_tics;

	/**
	 * The part of the Canvas to display our graph.
	 * Uses my coord system.
	 */
	private Rect m_view_rect = null;


	/**
	 * The labels for the numbers.  Each element can be
	 * a string or null (to indicate no label for this
	 * number).  Or...the whole thing can be null which
	 * means that there are no labels at all.
	 */
	protected List<String> m_labels = null;


	/** Min distance between two tic-marks. */
	public double m_min_tic_dist = GView.DEFAULT_MIN_POINT_DISTANCE;

	/**
	 * Describes the length in pixels of each tic.
	 */
	protected int m_tic_height = DEFAULT_TIC_HEIGHT;

	/**
	 * Used when drawing tic-marks.  This is x-axis
	 * location (in screen coords) of the last
	 * number that was drawn.
	 */
	protected double m_last_num = Double.MAX_VALUE * -1;

	/**
	 * This tells us the right-most pixel that was drawn
	 * when drawing the last label.  Used to keep labels
	 * from overlapping each other.
	 */
	protected double m_last_label_pixel;


	//-------------------------------
	//	Methods
	//-------------------------------


	/***************************
	 * Constructor.
	 */
	GraphXAxis2 () {
		m_dirty = true;
	} // constructor


	/****************************
	 * Adds the given date (a long) to our list
	 * of dates.
	 *
	 * Please make sure that these number are entered
	 * in ASCENDING order!
	 *
	 * @param date	The date (in milliseconds since 1970).
	 * 				Should correspond to the x-value of a
	 * 				point in the Graph2 class.
	 *
	 * @return		The number of dates after this one is
	 * 				added.
	 */
	public int add_date (long date) {
		if (m_dates == null) {
			m_dates = new ArrayList<Long>();
		}
		m_dates.add(date);
		m_dirty = true;
		return m_dates.size();
	} // add_date (date)

	/*****************************
	 * Similar to add_date(), but this allows calls to add a
	 * bunch of points at the same time.
	 *
	 * @see com.sleepfuriously.hpgworkout.GraphXAxis2#add_date(long)
	 *
	 * @param pts	List of PointDs in ascending x-value.
	 *
	 * @return	The number of points total after adding this list.
	 */
	public int add_dates (List<Long> dates) {
		if (m_dates == null) {
			m_dates = new ArrayList<Long>();
		}
		m_dates.addAll(dates);
		m_dirty = true;
		return m_dates.size();
	} // add_dates (dates)


	/****************************
	 * If you want labels to the tic-marks, you gotta
	 * add those labels!  Here's a way to do it, one
	 * at a time.
	 *
	 * NOTE:
	 * 		The labels must match with the dates!  So
	 * 		it's all or nothing; either no labels or
	 * 		the same exact number as the dates.
	 *
	 * @param label		A label to add.  Yes, it's
	 * 					okay to add null if you don't
	 * 					want a label for the corresponding
	 * 					tic.
	 *
	 * @return	The total number of labels after this
	 * 			had been added.
	 */
	public int add_label (String label) {
		if (m_labels == null) {
			m_labels = new ArrayList<String>();
		}
		m_labels.add(label);
		return m_labels.size();
	}

	/*****************************
	 * This version of add_label allows you to
	 * add a list in one swoop.  Cool.
	 *
	 * @see com.sleepfuriously.hpgworkout.GraphXAxis2#add_label(String)
	 */
	public int add_labels (List<String> labels) {
		if (m_labels == null) {
			m_labels = new ArrayList<String>();
		}
		m_labels.addAll(labels);
		return m_labels.size();
	}


	/*****************************
	 * Simply returns the current number of dates in our list.
	 */
	public int get_num_world_pts() {
		if (m_dates == null) {
			return 0;
		}
		return m_dates.size();
	}


	/*****************************
	 * Removes all points from the list of World Points.
	 */
	public void clear_world_pts() {
		if (m_dates == null) {
			m_dates = new ArrayList<Long>();
		}
		m_dates.clear();
		m_dirty = true;
	}

	/*****************************
	 * Curious about what a date is?  If you know WHERE
	 * it is, this can help.
	 *
	 * @param index		The index (starts at 0) of the date
	 * 					in question.
	 *
	 * @return	The date at the given index.  Returns -1
	 * 			if the index is out of range.
	 */
	public long get_world_pt_at (int index) {
		if ((index < 0) || index >= m_dates.size()) {
			return -1;
		}
		return m_dates.get(index);
	}


	/*****************************
	 * Just like it says, sets the Window to the list of
	 * dates.  This is how you can zoom and pan the
	 * x-axis part of the graph.
	 *
	 * Think of this as setting a window at our list of
	 * dates.
	 *
	 * @param	left		The left-most (smallest number or
	 * 					earliest date) portion of the date
	 * 					list to display.
	 *
	 * @param	right	The right-most.  Must be greater than
	 * 					left! (duh)
	 *
	 */
	public void set_date_window (double left, double right) {
		m_date_left = left;
		m_date_right = right;
		m_dirty = true;
	}

	/****************************
	 * Returns what this class thinks is the left-most viewing
	 * area of the date list.
	 */
	public double get_date_window_left() {
		return m_date_left;
	}

	/****************************
	 * Returns what this class thinks is the right side of
	 * the viewing window of the date list.
	 */
	public double get_date_window_right() {
		return m_date_right;
	}


	/****************************
	 * Call this to define where in the Canvas we want to
	 * draw the x-axis.  This class will do the rest.
	 */
	public void set_view_rect (Rect rect) {
		if (m_view_rect == null) {
			m_view_rect = new Rect (rect);
		}
		else {
			m_view_rect.set(rect);
		}
		m_dirty = true;
	}

	/**************************
	 * Just in case you need to know what the view rect is,
	 * here's a routine to find out.
	 */
	public RectF get_view_rect() {
		if (m_view_rect == null)
			return null;
		return new RectF(m_view_rect);
	}


	/**************************
	 * Call when you're ready to make this visible!
	 *
	 * @param canvas		What to draw on.
	 * @param paint		What to draw with.
	 */
	public void draw (Canvas canvas, Paint paint) {
		if (m_dirty) {
			calc_view_pts();
		}

		// Reset this helper data.
		m_last_label_pixel = Double.MAX_VALUE * -1;

		for (int i = 0; i < m_tics.size(); i++) {
			double tic_pos = m_tics.get(i);
			// Only draw tic marks if
			//	1. It's actually a number
			//	2. There's room.
			if ((tic_pos != NaN) &&
				(m_last_num + m_min_tic_dist < tic_pos)) {

				draw_line (canvas,
						tic_pos, m_view_rect.top,
						tic_pos, m_view_rect.top - m_tic_height,
						paint);
				if (m_labels != null) {
					draw_label (canvas, paint, m_labels.get(i), tic_pos, true);
				}

			}
		}

	} // draw (canvas, paint)


	/**************************
	 * Converts all the numbers (dates) in m_dates
	 * into screen coordinates (x only) in m_tics.
	 * That way, they're easy to draw!
	 *
	 * preconditions:
	 * 	m_dates
	 *	m_date_left
	 *	m_date_right
	 *	m_view_rect
	 *
	 * side effects:
	 * 	m_tics
	 */
	private void calc_view_pts() {
		// Prep the new list.
		if (m_tics == null) {
			m_tics = new ArrayList<Double>();
		}
		else {
			m_tics.clear();
		}

		// A useful ratio to precalculate.
		double ratio = m_view_rect.width() / (m_date_right - m_date_left);

		// Go through the dates, converting each to a tic position.
		for (int i = 0; i < m_dates.size(); i++) {
			double pos = m_dates.get(i);

			// propogate NaN, otherwise do the calculation.
			if (pos != NaN) {
				pos = (pos - m_date_left) * ratio + m_view_rect.left;
			}
			m_tics.add(pos);
		}

		m_dirty = false;
	} // calc_view_pts()


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
	 * @param x_pos		The x_position of the tic-mark we're
	 * 					labelling.
	 * @param centered	Should the label be centered on the
	 * 					tic-mark?
	 */
	private void draw_label (Canvas canvas, Paint paint,
							String label, double x_pos,
							boolean centered) {
		Log.d(tag, "draw_label(): x_pos = " + x_pos + ", string = " + label + ", centereed = " + centered);
		double x, y;
		Rect rect = new Rect();

		// Get the current anti-alias state; turn it on.
		boolean alias = paint.isAntiAlias();
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
			y = m_view_rect.top - m_tic_height - rect.height() - DEFAULT_LABEL_PADDING_TOP;
			draw_text(canvas, label, x, y, paint);
			m_last_label_pixel = x + rect.width();
		}

		// Restore old anti-alias state.
		paint.setAntiAlias(alias);
	} // draw_label(...)


}
