/**
 * This takes a bunch of points and maps them to a
 * rectangular area of your devising.  Add a Canvas and
 * a Paint and it'll draw a line graph of those points.
 *
 * TO USE:
 * 		1.	Instantiate by one of the various constructors.
 *
 * 		NOTE:	You can set the ID of this any time after
 * 				instantiating.  Id's are not used by this
 * 				class, but may be useful in some cases.
 *
 * 		2.	Add the points (using any of the various means).
 *
 * 		3.	Set the two rectangles, Bounds and Draw Area.
 * 			This will cause the points to be mapped (which
 * 			is necessary for efficient draw() calls!).
 *
 * 		4.	Call map_points().  This prepares the points
 * 			for quick and efficient draws.
 *
 * 		5.	If you need to add any more points, call
 * 			map_points() after!  You'll really be sorry if
 * 			you don't (performance hit).
 *
 * 		6.	Draw().  This is the only part that connects
 * 			with the UI, so everything else can be done
 * 			in other threads.
 *
 * Note: the points MUST be ordered along the X axis
 * 	(smallest X value first).
 *
 * Note: Nulls in the list are no problem.  They'll
 * 	just be skipped.		todo: test this!!!
 *
 * Note: Most of the work is done in the constructor.
 * 	That way, the draw routines will be as quick as
 * 	possible.
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

@Deprecated
public class GraphLine {

	//-------------------------------
	//	Constants
	//-------------------------------
	private static final String tag = "GraphLine";

	/** Size of big dots when drawn on the Canvas */
	public static final float BIG_DOT_RADIUS = 4.7f;

	/** The smallest size of a dot when drawn on a Canvas */
	public static final float SMALL_DOT_RADIUS = 1.8f;

	/**
	 * What to increment the dot radius by to make the
	 * difference viewable.
	 */
	public static final float DOT_RADIUS_INCREMENT = 0.37f;


	//-------------------------------
	//	Data
	//-------------------------------

	/**
	 * The size of the dot to draw for the points on the graph.
	 * It's set externally.
	 */
	public float m_radius = SMALL_DOT_RADIUS;

	/**
	 * The original points for this class.  All mapping is
	 * done relative to these.
	 */
	protected List<PointD> m_orig_pts;

	/**
	 * Our points after they have been mapped to the
	 * canvas' rectangle.
	 */
	protected PointD[] m_pts = null;

	/** The logical bounds of the numbers */
	protected RectF m_rect_bounds;

	/** Holds the logical rect for resetting */
	protected RectF m_orig_rect_bounds = null;

	/** The canvas draw area. */
	protected Rect m_draw_rect;

	/**
	 * Used when drawing lines and points.  This is the
	 * screen location (same system as m_pts) of the last
	 * point that was drawn.
	 */
	protected PointD m_last_pt = new PointD();

	/** Minimum distance between two points to draw a big dot */
	public float m_min_dist = GView.DEFAULT_MIN_POINT_DISTANCE;


	/** Tells if any of the points needs to be mapped */
	private boolean m_pts_dirty = true;


	//-------------------------------
	//	Constructors
	//-------------------------------

	/*****************************
	 * Constructor.
	 *
	 * @param pts		List of points that we want to
	 * 					graph.  They need to be ordered
	 * 					in increasing x value.
	 *
	 * @param bounds		The bounding box of the theoritical
	 * 						graph. Describe the min and max
	 * 						values of to display the points.
	 * 						Usually these will simply be the
	 * 						highest and lowest values of the
	 * 						points, but could be bigger.
	 *
	 * @param draw_area		The area of the canvas to draw our
	 * 						graph in.  Need this to properly
	 * 						set up the mapping functions.
	 */
	public GraphLine (List<PointD> pts, RectF bounds, Rect draw_area) {
		m_orig_pts = new ArrayList<PointD>(pts);
		m_pts_dirty = true;
		set_bounds(bounds);
		set_draw_area(draw_area);
		set_points (pts);
	} // constructor

	/***************************
	 * Constructor.  Use this when you don't know the draw
	 * area yet.
	 *
	 * @param pts		List of points that we want to
	 * 					graph.  They need to be ordered
	 * 					in increasing x value.
	 *
	 * @param bounds		The bounding box of the theoritical
	 * 						graph. Describe the min and max
	 * 						values of to display the points.
	 * 						Usually these will simply be the
	 * 						highest and lowest values of the
	 * 						points, but could be bigger.
	 *
	 */
	public GraphLine (List<PointD> pts, RectF bounds) {
		m_pts_dirty = true;
		set_bounds(bounds);
		m_orig_pts = new ArrayList<PointD>(pts);
	}

	/***************************
	 * Constructor for when you only know some points.
	 *
	 * @param pts
	 */
	public GraphLine (List<PointD> pts) {
		m_pts_dirty = true;
		m_orig_pts = new ArrayList<PointD>(pts);
	}

	/***************************
	 * The most basic constructor.
	 */
	public GraphLine() {
		m_orig_pts = new ArrayList<PointD>();
		m_pts_dirty = true;
	}


	//-------------------------------
	//	Methods
	//-------------------------------


	/***************************
	 * Clears out all the  points of this class.  Use with
	 * caution (duh).
	 */
	public void delete_points() {
		m_orig_pts.clear();
		m_pts_dirty = true;
	}

	/****************************
	 * Takes a bunch of points and creates our internal
	 * array from them.  If you didn't construct with these
	 * parameters, you gotta do this before drawing.
	 *
	 * @param pts	A bunch of points that will REPLACE the
	 * 				current point list!
	 *
	 * @return	The number of points added.
	 */
	public int set_points (List<PointD> pts) {
		// Clear our original list and copy the new list into it.
		m_orig_pts.clear();
		m_orig_pts.addAll(pts);
		m_pts_dirty = true;
		return m_orig_pts.size();
	} // set_points (pts, bounds, draw_area)

	/****************************
	 * Adds the points to our current points list.
	 *
	 * preconditions:
	 * 	The rectangles have been set.
	 *
	 * @param pts	A bunch of points that will be added
	 * 				to the current point list.
	 *
	 * @return	The total number of points in our list.
	 */
	public int add_points (List<PointD> pts) {
		m_orig_pts.addAll(pts);
		m_pts_dirty = true;
		return m_orig_pts.size();
	} // add_points (pts)


	/****************************
	 * Adds just one point to our list of points.  Please
	 * call map_points() before calling draw() for optimal
	 * performance.
	 *
	 * side effects:
	 * 	m_orig_pts		A point is added.
	 *
	 * 	m_pts			No longer is correctly mapped.
	 *
	 * @param pt		The 2D point to add.
	 *
	 * @return	The number of points total.
	 */
	public int add_point (PointD pt) {
		m_orig_pts.add(pt);
		m_pts_dirty = true;
		return m_orig_pts.size();
	}

	/****************************
	 * This does the calculations for the points of this class.
	 * <p>
	 * NOTE: if the y-value (data value) of an original point
	 * (from m_orig_pts) is -1, then that means that the
	 * y-value is null. So that value is NOT processed, and is
	 * passed along to the corresponding m_pts.y value.
	 *<p>
	 * preconditions:
	 * 	m_rect_bounds	Correctly set.
	 * 	m_canvas_rect	Correctly set.
	 * 	m_orig_pts		Holds the numbers to map.
	 *
	 * side effects:
	 * 	m_pts	Created to reflect the correct mapping from m_orig_pts.
	 */
	public void map_points() {
//		Log.d(tag, "map_points(): " + m_rect_bounds + " ==> " + m_draw_rect);
		m_pts = new PointD[m_orig_pts.size()];

		GraphMap2D mapper2D = new GraphMap2D(m_rect_bounds, m_draw_rect);

		// Map each point.
		for (int i = 0; i < m_orig_pts.size(); i++) {
			// Check for uninitialized variable
			if (m_orig_pts.get(i) == null) {
				m_pts[i] = null;
				Log.e(tag, "map_points(): can't handle a null m_orig_pts!");
			}
			// Check for a null value (which is -1 here).
			else if (m_orig_pts.get(i).y == -1) {
				m_pts[i] = new PointD(m_orig_pts.get(i).x, -1);	// The x is never used, but here for completeness.
			}
			else {
				m_pts[i] = mapper2D.map(m_orig_pts.get(i));
			}
		}
		m_pts_dirty = false;

	} // map_points(pts)


	/****************************
	 * Call this whenever either rectangle changes that describes
	 * this data.  This may be more convenient than starting a
	 * brand-new instance of this class.
	 *
	 * @param bounds			The logical starting and stopping points
	 * 						of the class data (x ~ left/right, y ~ top/bottom).
	 *
	 * @param draw_area		The part of the canvas that we draw to
	 * 						(using my screen coords!).
	 */
	public void set_rects (RectF bounds, Rect draw_area) {
		set_bounds (bounds);
		set_draw_area (draw_area);
	}

	/*****************************
	 * If the logical boundaries of this data changes, call this!
	 *
	 * @param bounds			The logical starting and stopping points
	 * 						of the class data (x ~ left/right, y ~ top/bottom).
	 */
	public void set_bounds (RectF bounds) {
		if (m_rect_bounds == null) {
			m_rect_bounds = new RectF(bounds);
		}
		else {
			m_rect_bounds.set(bounds);
		}
		m_pts_dirty = true;
	}

	/*****************************
	 * Has the boundary rectangle been set?
	 */
	public boolean is_bounds_set() {
		return m_rect_bounds != null;
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
		m_pts_dirty = true;
	}

	/*****************************
	 * Has the draw area rectangle been set?
	 */
	public boolean is_draw_area_set() {
		return m_draw_rect != null;
	}


	/****************************
	 * Call this to draw the graph.
	 *
	 * side effects:
	 * 	m_last_pt		Cleared by this, and then changed
	 * 					by calls to other methods.
	 *
	 * @param canvas		The Canvas to draw on (remember the
	 * 					coords that were supplied in construction).
	 *
	 * @param paint		The painter to use.  Should be completely
	 * 					set up (including the aliasing!).
	 */
	public void draw (Canvas canvas, Paint paint) {
		if (m_pts_dirty) {
			Log.w(tag, "Trying to draw with dirty points!  But I'll do it anyway, even if it is slow.");
			map_points();
		}

		/** Was the last point a valid one? */
		boolean last_valid = false;
		m_last_pt.set(0, 0);		// Clear		!!! If this is still set to the last point, we have a problem!

		for (int i = 0; i < m_pts.length; i++) {
			// Check for error condition.
			if (m_pts[i] == null) {
				Log.e(tag, "draw():  Can't draw a point that's null!");
				last_valid = false;
			}

			// Check for an invalid point.
			else if (m_pts[i].y == -1) {
				last_valid = false;
			}

			// Actually draw the line.
			else {
				if (last_valid) {
					draw_line(canvas, paint, m_last_pt, m_pts[i]);
				}
				else {
					draw_pt (canvas, paint, m_pts[i]);
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
		float radius = m_radius;

		if ((pt.x - m_last_pt.x <= m_min_dist) &&
			(pt.y - m_last_pt.y <= m_min_dist)) {
			radius = 1;
		}

//		Log.d (tag, "draw_pt() at " + pt.x + ", " + pt.y);
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
//		Log.d(tag, "drawing line from (" + a.x + ", " + a.y + ") to (" + b.x + ", " + b.y + ")");
		if ((b.x - a.x > m_min_dist) ||
			(b.y - a.y > m_min_dist)) {
			draw_pt(canvas, paint, b);
		}
		m_last_pt.set(b);
	} // draw_line (canvas, paint)


	/*****************************
	 * Does a zoom effect on the data here.  The number
	 * represents how much bigger or smaller to draw our
	 * line graph.
	 * <p>
	 * Strategy: Change the starting and ending points of
	 * the <i>logical</i> x-axis to reflect the scale amount.
	 * Then recalculate the m_pts to reflect the change.  We
	 * don't really zoom along the y-axis, so that stuff won't
	 * change.
	 *
	 * @param amount		The amount of pixels to zoom.  Positive
	 * 					zooms out, negaitve zooms in.
	 */
	public void scale (float amount) {
		if (m_orig_rect_bounds == null) {
			// This is the first time this has been called,
			// so save the original so that we can reset if
			// necessary.
			m_orig_rect_bounds = new RectF(m_rect_bounds);
		}

		m_rect_bounds.left -= amount / 2f;
		m_rect_bounds.right += amount / 2f;

		Log.d(tag, "scale(): amount = " + amount);
		Log.d(tag, "    m_pts[0] = (" + m_pts[0].x + ", " + m_pts[0].y + ")");
		map_points();
		Log.d(tag, "        ==> m_pts[0] = (" + m_pts[0].x + ", " + m_pts[0].y + ")");
	} // scale (amount)

	/****************************
	 * Resets the scale to the original settings.
	 */
	public void scale_reset() {
		m_rect_bounds.set(m_orig_rect_bounds);
		map_points();
	}

}
