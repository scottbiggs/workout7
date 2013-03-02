/**
 * This is my second attempt at making a class that graphs
 * data.  Draws a line graph from a list of points.
 *
 * Input:
 * 		World Points.  These are in world coordinates and need
 * 		to be ordered from left to right along the x-axis.
 *
 * 			add_world_pt (pt)		- appends one point to our list
 * 			add_world_pts (pt[])		- appends a list to the current list
 * 			clear_world_pts()		- removes all the points in the world list
 * 			get_num_world_pts()
 * 			get_world_pt_at (index)
 *
 *
 * 		World Rectangle.  This defines the window into the world
 * 		that we want to view.  Points outside of this rectangle will
 * 		not be visible.  Zooming and panning is done on the world
 * 		points by changing this rectangle.
 *
 * 			set_world_rect (rect)
 * 			get_world_rect()
 *
 * 		View Rect.  This is the rectangle that defines the area
 * 		of the screen to draw what is in the world rectangle.  And
 * 		yes, I'm using MY screen coordinate system (where 0,0 is in the
 * 		bottom left).
 *
 * 			set_view_rect (rect)
 * 			get_world_rect (rect)
 *
 * From these inputs a list of View Points are created which are the
 * line graph.
 *
 * To execute:
 *
 * 		draw (Canvas, Paint)
 *
 * That's that!
 *
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;


public class Graph2 {

	//--------------------------------
	//	Constants
	//--------------------------------

	private static final String tag = Graph2.class.getName();

	/** Size of big dots when drawn on the Canvas */
	public static final float BIG_DOT_RADIUS = 5.3f;

	/** The smallest size of a dot when drawn on a Canvas */
	public static final float SMALL_DOT_RADIUS = 1.3f;

	/**
	 * What to increment the dot radius by to make the
	 * difference viewable.
	 */
	public static final float DOT_RADIUS_INCREMENT = 0.8f;


	//--------------------------------
	//	Data
	//--------------------------------

	/** When TRUE, m_view_pts needs creating */
	private boolean m_dirty;

	/** Holds the original points to graph. ALL of them. In x-axis order. */
	protected List<PointD> m_world_pts = null;

	/** Holds the points to graph converted in screen (my) coordinates. */
	protected List<PointD> m_view_pts = null;

	/**
	 * The window of the world points to display.  Zooming and panning
	 * are accomplished by changing this rectangle.
	 */
	protected RectD m_world_rect = null;

	/** The part of the Canvas to display our graph. Uses my coord system. */
	protected RectF m_view_rect = null;


	/** Minimum distance necessary between two points to draw a big dot */
	public float m_min_dot_dist = GView.DEFAULT_MIN_POINT_DISTANCE;

	/**
	 * The size of the dot to draw for the points on the graph.
	 * It's set externally.
	 */
	public float m_dot_radius = SMALL_DOT_RADIUS;


	/**
	 * Used when drawing lines and points.  This is the
	 * screen location (same system as m_pts) of the last
	 * point that was drawn.  It's used by many methods,
	 * that's why it's a class data instead of a local var.
	 */
	protected PointD m_last_pt = new PointD();


	//--------------------------------
	//	Methods
	//--------------------------------

	/******************
	 * Constructor
	 */
	Graph2() {
		m_dirty = true;
	}


	/****************************
	 * Adds the given point to our list of WORLD COORDINATES.
	 *
	 * Please make sure that the x-value of the coordinates
	 * are never less than the point that preceded.  In other
	 * words, all points must be entered in INCREASING x-value.
	 *
	 * @param pt		The x-value is probably the date (in milliseconds)
	 * 				and the y-value is the actual value for this
	 * 				line-graph.
	 *
	 * @return		The number of points total after this one
	 * 				is added.
	 */
	public int add_world_pt (PointD pt) {
		if (m_world_pts == null) {
			m_world_pts = new ArrayList<PointD>();
		}
		m_world_pts.add(pt);
		m_dirty = true;
		return m_world_pts.size();
	} // add_world_pt (pt)

	/*****************************
	 * Similar to add_pt, this allows calls to add a bunch
	 * of points at the same time.
	 *
	 * @see com.sleepfuriously.hpgworkout.Graph2#add_pt(PointD)
	 *
	 * @param pts	List of PointDs in ascending x-value.
	 *
	 * @return	The number of points total after adding this list.
	 */
	public int add_world_pt (List<PointD> pts) {
		if (m_world_pts == null) {
			m_world_pts = new ArrayList<PointD>();
		}
		m_world_pts.addAll(pts);
		m_dirty = true;
		return m_world_pts.size();
	} // add_world_pts (pts)


	/*****************************
	 * Simply returns the current number of world points.
	 */
	public int get_num_world_pts() {
		if (m_world_pts == null) {
			return 0;
		}
		return m_world_pts.size();
	}


	/*****************************
	 * Removes all points from the list of World Points.
	 */
	public void clear_world_pts() {
		if (m_world_pts == null) {
			m_world_pts = new ArrayList<PointD>();
		}
		m_world_pts.clear();
		m_dirty = true;
	}


	/*****************************
	 * Curious about what a point is?  If you know WHERE
	 * it is, this can help.
	 *
	 * @param index		The index (starts at 0) of the point
	 * 					in question.
	 *
	 * @return	The PointD at the given index.  Returns null
	 * 			if the index is out of range or the point
	 * 			happens to be null.
	 */
	public PointD get_world_pt_at (int index) {
		if ((index < 0) || index >= m_world_pts.size()) {
			return null;
		}
		return m_world_pts.get(index);
	}


	/*****************************
	 * Just like it says, sets the Window to the world points
	 * to the given rectangle.  This is how you can zoom and
	 * pan the graph.
	 *
	 * @param rect		The new rect to set our world window.
	 */
	public void set_world_rect (RectD rect) {
		if (m_world_rect == null) {
			m_world_rect = new RectD (rect);
		}
		else {
			m_world_rect.set(rect);
		}
		m_dirty = true;
	}

	/****************************
	 * Curious about the world rect?  You should be; it's very
	 * useful.  Here's how to get a copy.
	 */
	public RectD get_world_rect() {
		return new RectD(m_world_rect);
	}


	/****************************
	 * Call this to define where in the Canvas we want to
	 * draw our graph.  You need to take care of the padding
	 * and such--just tell this class exactly where to draw
	 * the damn thing!
	 *
	 * @param rect		Our draw area for the graph in my
	 * 					special screen coords.
	 */
	public void set_view_rect (RectF rect) {
		if (m_view_rect == null) {
			m_view_rect = new RectF (rect);
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
		return new RectF(m_view_rect);
	}


	/***************************
	 * When you're ready to draw the graph, this'll do ya.
	 *
	 * @param canvas		What to draw on.
	 * @param paint		What to draw with.
	 */
	public void draw (Canvas canvas, Paint paint) {
		Log.d(tag, "draw()");

		if (m_dirty) {
			calc_view_pts();
		}

		boolean last_valid = false;
		m_last_pt.set(Double.NaN, Double.NaN);		// just in case

		for (int i = 0; i < m_view_pts.size(); i++) {
			PointD pt = m_view_pts.get(i);

			// Check for error condition.
			if (pt == null) {
				Log.e(tag, "draw():  Can't draw a point that's null!");
				last_valid = false;
			}
			// Check for an invalid point.
			else if (m_view_pts.get(i).y == -1d) {
				// if the last point was valid, draw a
				// dot there.  And that's all--this number
				// is not valid.
				if (last_valid) {
					draw_pt (canvas, paint, m_last_pt);
				}
				// Indicate that there is no valid point (duh).
				last_valid = false;
			}

			// Actually draw something.
			else {
				if (last_valid) {
					// Draw the line.
					draw_line (canvas, paint, m_last_pt, pt);

				}
				else {
					// Just draw a point.
					draw_pt (canvas, paint, pt);
					last_valid = true;
				}
			}
		}

	} // draw (canvas, paint)


	/****************************
	 * Actually does the drawing of a line.
	 *<p>
	 * preconditions:<br/>
	 * 		- m_min_dist is correctly set.<br/>
	 * 		- m_radius is correctly set.
	 *<p>
	 * side effects:
	 * 		m_last_pt		Will change this to be
	 * 						this point.
	 *
	 * @param canvas		What to draw on.
	 * @param paint		What to draw with.
	 * @param pt			The point to draw (in Canvas coords).
	 * @param radius		The requested radius for a point.
	 */
	private void draw_pt (Canvas canvas, Paint paint, PointD pt) {
		float radius = m_dot_radius;

		if ((pt.x - m_last_pt.x <= m_min_dot_dist) &&
			(pt.y - m_last_pt.y <= m_min_dot_dist)) {
			radius = 1;
		}

		GraphDrawPrimitives.draw_circle(canvas, pt.x, pt.y, radius, paint);
		m_last_pt.set(pt);
	} // draw_pt (canvas, paint, pt)


	/****************************
	 * Draws a line from a to b (inclusive).  If there is
	 * enough room, the second point is drawn large.
	 *
	 * preconditions:
	 * 		- m_min_dist is correctly set.
	 *
	 * side effects:
	 * 		m_last_pt		Will change this to be
	 * 						the end point.
	 *
	 * @param canvas		What to draw on.
	 * @param paint		What to draw with.
	 */
	private void draw_line (Canvas canvas, Paint paint,
							PointD a, PointD b) {
		m_last_pt.set(a);
		GraphDrawPrimitives.draw_line(canvas, a.x, a.y, b.x, b.y, paint);
		if ((b.x - a.x > m_min_dot_dist) ||
			(b.y - a.y > m_min_dot_dist)) {
			draw_pt(canvas, paint, b);
		}
		m_last_pt.set(b);
	} // draw_line (canvas, paint)




	/**************************
	 * You like math?  Well so does this routine.  Converts
	 * all the world points to view points.  See Foley/van Dam
	 * p. 212 for a real description of what's happening.
	 * <p>
	 * <b>preconditions</b>:<br/>
	 * 		m_world_pts<br/>
	 * 		m_world_rect<br/>
	 * 		m_view_rect
	 */
	protected void calc_view_pts() {
		Log.d(tag, "calc_view_pts()");
		Log.v(tag, "m_world_rect = " + m_world_rect.toString());

		// First, prep the view list.
		if (m_view_pts == null) {
			m_view_pts = new ArrayList<PointD>();
		}
		else {
			m_view_pts.clear();
		}

		// Useful to precalculate these numbers.
		double xratio = m_view_rect.width() / m_world_rect.width();
		double yratio = -m_view_rect.height() / m_world_rect.height();

		// Go through world points, converting each to a
		// view point.
		for (int i = 0; i < m_world_pts.size(); i++) {
			PointD view_pt = new PointD(m_world_pts.get(i));

			// Test to see if this data has an invalid y-value.  That
			// means that this is invalid data and should not be graphed.
			// But here, we just propogate that and let the draw routine
			// figure out what to do.
			if (view_pt.y != -1d) {
				// Only do the y-value calculation if is NOT -1.
				view_pt.y = (m_world_pts.get(i).y - m_world_rect.bottom)
							* yratio + m_view_rect.bottom;
			}

			// Always do the x calculation.
			view_pt.x = (view_pt.x - m_world_rect.left)
						* xratio + m_view_rect.left;
			m_view_pts.add(view_pt);
		}

		m_dirty = false;

	} // calc_view_pts()


}
