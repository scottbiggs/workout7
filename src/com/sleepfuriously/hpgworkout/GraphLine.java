/**
 * This takes a bunch of points and maps them to a
 * rectangular area of your devising.  Add a Canvas and
 * a Paint and it'll draw a line graph of those points.
 *
 * Note: the points MUST be ordered along the X axis
 * 	(smallest X value first).
 *
 * Note: Nulls in the list are no problem.  They'll
 * 	just be skipped.
 *
 * Note: Most of the work is done in the constructor.
 * 	That way, the draw routines will be as quick as
 * 	possible.
 */
package com.sleepfuriously.hpgworkout;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;


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
	 * Our points after they have been mapped to the
	 * canvas' rectangle.
	 */
//	ArrayList<PointF> m_pts;
	protected PointF[] m_pts;

	/** The canvas draw area. */
	protected RectF m_canvas_rect;

	/**
	 * Used when drawing lines and points.  This is the
	 * screen location (same system as m_pts) of the last
	 * point that was drawn.
	 */
	protected PointF m_last_pt;

	/** Minimum distance between two points to draw a big dot */
	public Float m_min_dist = GView.DEFAULT_MIN_POINT_DISTANCE;

	//-------------------------------
	//	Methods
	//-------------------------------

	/*****************************
	 * Constructor.
	 *
	 * @param pts		List of points that we want to
	 * 					graph.  They need to be ordered
	 * 					in increasing x value.
	 *
	 * @param boundaries		The bounding box of the theoritical
	 * 						graph. Describe the min and max
	 * 						values of to display the points.
	 * 						Usually these will simply be the
	 * 						highest and lowest values of the
	 * 						points, but could be bigger.
	 *
	 * @param canvas			The area of the canvas to draw our
	 * 						graph in.  Need this to properly
	 * 						set up the mapping functions.
	 */
	GraphLine (PointF[] pts,
			RectF boundaries, RectF canvas) {
		m_pts = new PointF[pts.length];

		m_canvas_rect = canvas;
		GraphMap2D m = new GraphMap2D(boundaries, canvas);

		// Map each point.
		for (int i = 0; i < pts.length; i++) {
			if (pts[i] == null) {
				m_pts[i] = null;
			}
			else {
				m_pts[i] = m.map(pts[i]);
			}
		}
	} // constructor


	/****************************
	 * Call this to draw the graph.
	 *
	 * @param canvas		The Canvas to draw on (remember the
	 * 					coords that were supplied in construction).
	 *
	 * @param paint		The painter to use.  Should be completely
	 * 					set up (including the aliasing!).
	 */
	void draw (Canvas canvas, Paint paint) {
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
	void draw_pt (Canvas canvas, Paint paint, PointF pt) {
		float radius = 1f;

		if ((pt.x - m_last_pt.x > m_min_dist) ||
			(pt.y - m_last_pt.y > m_min_dist)) {
			radius = BIG_DOT_RADIUS;
		}
		canvas.drawCircle(pt.x, pt.y, radius, paint);
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
	void draw_line (Canvas canvas, Paint paint,
					PointF a, PointF b) {
		m_last_pt = a;
		canvas.drawLine(a.x, a.y, b.x, b.y, paint);
		if ((b.x - a.x > m_min_dist) ||
			(b.y - a.y > m_min_dist)) {
			draw_pt(canvas, paint, b);
		}
		m_last_pt = b;
	} // draw_line (canvas, paint)
}
