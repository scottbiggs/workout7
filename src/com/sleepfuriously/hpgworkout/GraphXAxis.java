/**
 * This just draws the X axis of a graph with
 * the given specifications.  This includes the
 * little vertical lines and the labels.
 *
 */
package com.sleepfuriously.hpgworkout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.widget.HorizontalScrollView;

public class GraphXAxis {

	//-------------------------------
	//	Constants
	//-------------------------------
	private static final String tag = "GraphXAxis";

	public final static int DEFAULT_TICK_HEIGHT = 5;

	//-------------------------------
	//	Public Data
	//-------------------------------

	/**
	 * Describes the length in pixels of each tick.
	 */
	public int m_tick_height = DEFAULT_TICK_HEIGHT;

	/** Labels for the x-axis */
	public String m_label_start = null, m_label_end = null;


	//-------------------------------
	//	Data
	//-------------------------------

	/**
	 * Holds all the x values to display (tick-marks).  These
	 * are in my special screen coordinates (where the origin
	 * is in the bottom left).
	 */
	private float[] m_x_pts;

	/**
	 * Describes the draw area of the canvas (in my special coordinates).
	 * The width should match the width of the Graph so the ticks match.
	 * And the height is how we determine the length of the tick-marks.
	 */
	private RectF m_canvas_rect;


	//-------------------------------
	//	Methods
	//-------------------------------

	/*****************************
	 * Constructor.
	 *
	 * @param pts		Same as when calling GraphLine.
	 *
	 * @param boundaries		This should be the same as when
	 * 						constructing GraphLine.
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
	GraphXAxis (PointF[] pts,
				RectF boundaries, RectF draw_area) {
		m_x_pts = new float[pts.length];

		m_canvas_rect = draw_area;
		GraphMap mapper = new GraphMap(boundaries.left, boundaries.right,
									draw_area.left, draw_area.right);

		// Map each point.
		for (int i = 0; i < pts.length; i++) {
			m_x_pts[i] = mapper.map(pts[i].x);
		}

	} // constructor



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
		Log.d (tag, "draw_lines() called");
		// todo:
		//	Make this dependent on the actual area given.
		for (int i = 0; i < m_x_pts.length; i++) {
			GraphDrawPrimitives.draw_line(canvas, m_x_pts[i], 0, m_x_pts[i], 15, paint);	// Just guessing
		}
	} // draw_lines()


	/*****************************
	 * Draws the labels on our x-axis.
	 */
	private void draw_labels (Canvas canvas, Paint paint) {

	} // draw_labels()


}
