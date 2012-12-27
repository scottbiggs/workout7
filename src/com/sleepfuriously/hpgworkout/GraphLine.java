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
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;


public class GraphLine {

	//-------------------------------
	//	Constants
	//-------------------------------
	private static final String tag = "GraphLine";

	/** Size of big dots when drawn on the Canvas */
	public static final float BIG_DOT_RADIUS = 2.9f;

	//-------------------------------
	//	Data
	//-------------------------------

	/**
	 * The original points for this class.  All mapping is
	 * done relative to these.
	 */
	protected List<PointF> m_orig_pts;

	/**
	 * Our points after they have been mapped to the
	 * canvas' rectangle.
	 */
	protected PointF[] m_pts = null;

	/** The logical bounds of the numbers */
	protected RectF m_rect_bounds;

	/** The canvas draw area. */
	protected RectF m_draw_rect;

	/**
	 * Used when drawing lines and points.  This is the
	 * screen location (same system as m_pts) of the last
	 * point that was drawn.
	 */
	protected PointF m_last_pt = new PointF();

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
	public GraphLine (List<PointF> pts, RectF bounds, RectF draw_area) {
		m_orig_pts = new ArrayList<PointF>(pts);		// todo: creating a copy of pts is redundant here!
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
	public GraphLine (List<PointF> pts, RectF bounds) {
		m_pts_dirty = true;
		set_bounds(bounds);
		m_orig_pts = new ArrayList<PointF>(pts);
	}

	/***************************
	 * Constructor for when you only know some points.
	 *
	 * @param pts
	 */
	public GraphLine (List<PointF> pts) {
		m_pts_dirty = true;
		m_orig_pts = new ArrayList<PointF>(pts);
	}

	/***************************
	 * The most basic constructor.
	 */
	public GraphLine() {
		m_orig_pts = new ArrayList<PointF>();
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
	public int set_points (List<PointF> pts) {
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
	public int add_points (List<PointF> pts) {
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
	public int add_point (PointF pt) {
		m_orig_pts.add(pt);
		m_pts_dirty = true;
		return m_orig_pts.size();
	}

	/****************************
	 * This does the calculations for the points of this class.
	 *
	 * preconditions:
	 * 	m_rect_bounds	Correctly set.
	 * 	m_canvas_rect	Correctly set.
	 * 	m_orig_pts		Holds the numbers to map.
	 *
	 * side effects:
	 * 	m_pts	Created to reflect the correct mapping from m_orig_pts.
	 */
	public void map_points() {
		m_pts = new PointF[m_orig_pts.size()];
		GraphMap2D mapper2D = new GraphMap2D(m_rect_bounds, m_draw_rect);

		// Map each point.
		for (int i = 0; i < m_orig_pts.size(); i++) {
			if (m_orig_pts.get(i) == null) {
				m_pts[i] = null;
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
	public void set_rects (RectF bounds, RectF draw_area) {
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
	public void set_draw_area (RectF draw_area) {
		if (m_draw_rect == null) {
			m_draw_rect = new RectF(draw_area);
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
		m_last_pt.set(0, 0);		// Clear

		for (int i = 0; i < m_pts.length; i++) {
			if (m_pts[i] == null) {
				last_valid = false;
			}
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
	 *
	 * preconditions:
	 * 		- m_min_dist is correctly set.
	 *
	 * side effects:
	 * 		m_last_pt		Will change this to be
	 * 						this point.
	 *
	 * @param canvas		What to draw on.
	 * @param paint		What to draw with.
	 * @param pt			The point to draw (in Canvas coords).
	 */
	private void draw_pt (Canvas canvas, Paint paint, PointF pt) {
		float radius = 1f;

		if ((pt.x - m_last_pt.x > m_min_dist) ||
			(pt.y - m_last_pt.y > m_min_dist)) {
			radius = BIG_DOT_RADIUS;
		}
		GraphDrawPrimitives.draw_circle(canvas, pt.x, pt.y, radius, paint);
		m_last_pt = pt;
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
					PointF a, PointF b) {
		m_last_pt = a;
		GraphDrawPrimitives.draw_line(canvas, a.x, a.y, b.x, b.y, paint);
		if ((b.x - a.x > m_min_dist) ||
			(b.y - a.y > m_min_dist)) {
			draw_pt(canvas, paint, b);
		}
		m_last_pt = b;
	} // draw_line (canvas, paint)
}
