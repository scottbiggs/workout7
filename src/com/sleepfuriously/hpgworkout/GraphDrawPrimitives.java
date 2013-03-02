/**
 * This is a wrapper for the primitive draw routines
 * using a Canvas.
 *
 * These routines convert from a sane coordinate system
 * (ie, one with 0,0 in the lower left) to the typical
 * coordinate system for screens (have 0,0 at the top
 * left).
 *
 * Just call these routines instead of your usual
 * Canvas.draw___().
 */
package com.sleepfuriously.hpgworkout;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.AndroidCharacter;
import android.util.Log;


public class GraphDrawPrimitives {

	private static final String tag = "GraphDrawPrimitives";


	/*******************************
	 * The main thing for this class is switching y-values.
	 * That's what this does.
	 *
	 * preconditions:
	 * 		m_y_converter	Already set.
	 *
	 * @param canvas		The Canvas we're working with.
	 *
	 * @param old_y		The y coordinate with 0,0 at the
	 * 					bottom left.
	 *
	 * @return		The y coordinate where 0,0 is the top
	 * 				left.  Simply (height - 1) - old_y .
	 */
	protected static float convert_y (Canvas canvas, float old_y) {
		Rect rect = canvas.getClipBounds();
		float y = rect.bottom - old_y;
//		Log.d(tag, "convert_y(" + old_y + ") --> " + y);
		return y;
	} // convert_y(canvas, old_y)


	/*******************************
	 * The main thing for this class is switching y-values.
	 * That's what this does.  This one is for doubles.
	 *
	 * preconditions:
	 * 		m_y_converter	Already set.
	 *
	 * @param canvas		The Canvas we're working with.
	 *
	 * @param old_y		The y coordinate with 0,0 at the
	 * 					bottom left.
	 *
	 * @return		The y coordinate where 0,0 is the top
	 * 				left.  Simply (height - 1) - old_y .
	 */
	protected static double convert_y (Canvas canvas, double old_y) {
		Rect rect = canvas.getClipBounds();
		double y = rect.bottom - old_y;
//		Log.d(tag, "convert_y(" + old_y + ") --> " + y);
		return y;
	} // convert_y(canvas, old_y)


	/********************************
	 * Int version.
	 */
	protected static int convert_y (Canvas canvas, int old_y) {
		Rect rect = canvas.getClipBounds();
		return rect.bottom - old_y;
	}

	/************************
	 * My sane replacement.  0,0 is in the lower left
	 * instead of the top left.  Otherwise, this is
	 * exactly the same.
	 *
	 * @see android.graphics.Canvas#drawCircle(float, float, float, Paint)
	 */
	public static void draw_circle (Canvas canvas, float x, float y,
									float radius, Paint paint) {
		Log.d(tag, "drawCircle called at " + x + ", " + y);
		y = convert_y(canvas, y);
		canvas.drawCircle(x, y, radius, paint);
	} // draw_circle (canvas, x, y, radius, paint)

	/************************
	 * My sane replacement.  0,0 is in the lower left
	 * instead of the top left.  Otherwise, this is
	 * exactly the same.
	 *
	 * This one is for doubles
	 *
	 * @see android.graphics.Canvas#drawCircle(float, float, float, Paint)
	 */
	public static void draw_circle (Canvas canvas, double x, double y,
									float radius, Paint paint) {
		Log.d(tag, "drawCircle called at " + x + ", " + y);
		y = convert_y(canvas, y);
		canvas.drawCircle((float)x, (float)y, radius, paint);
	} // draw_circle (canvas, x, y, radius, paint)


	/************************
	 * My sane replacement.  0,0 is in the lower left
	 * instead of the top left.  Otherwise, this is
	 * exactly the same.
	 *
	 * @see android.graphics.Canvas#drawLine(float, float, float, float, Paint)
	 */
	public static void draw_line (Canvas canvas,
								float ax, float ay,
								float bx, float by,
								Paint paint) {
		ay = convert_y(canvas, ay);
		by = convert_y(canvas, by);
		canvas.drawLine(ax, ay, bx, by, paint);
	} // draw_line, (canvas, ax, ay, bx, by, paint)

	/************************
	 * My sane replacement.  0,0 is in the lower left
	 * instead of the top left.  Otherwise, this is
	 * exactly the same.  This is the double version.
	 *
	 * @see android.graphics.Canvas#drawLine(float, float, float, float, Paint)
	 */
	public static void draw_line (Canvas canvas,
								double ax, double ay,
								double bx, double by,
								Paint paint) {
		ay = convert_y(canvas, ay);
		by = convert_y(canvas, by);
		canvas.drawLine((float) ax, (float) ay, (float) bx, (float) by,
						paint);
	} // draw_line, (canvas, ax, ay, bx, by, paint)


	/************************
	 * A courtesy method to draw a box at the specified
	 * rectangle.  Like the others here, it uses my special
	 * coord system.
	 *
	 * @param rect	Defines where to draw the empty box.
	 */
	public static void draw_box (Canvas canvas, Rect rect,
								Paint paint) {
		draw_line(canvas,
				rect.left, rect.bottom,
				rect.left, rect.top,
				paint);
		draw_line(canvas,
				rect.left, rect.top,
				rect.right, rect.top,
				paint);
		draw_line(canvas,
				rect.right, rect.top,
				rect.right, rect.bottom,
				paint);
		draw_line(canvas,
				rect.right, rect.bottom,
				rect.left, rect.bottom,
				paint);
	}

	/************************
	 * A courtesy method to draw a box at the specified
	 * rectangle.  Like the others here, it uses my special
	 * coord system.
	 *
	 * @param rect	Defines where to draw the empty box.
	 */
	public static void draw_box (Canvas canvas, RectF rect,
								Paint paint) {
		draw_line(canvas,
				rect.left, rect.bottom,
				rect.left, rect.top,
				paint);
		draw_line(canvas,
				rect.left, rect.top,
				rect.right, rect.top,
				paint);
		draw_line(canvas,
				rect.right, rect.top,
				rect.right, rect.bottom,
				paint);
		draw_line(canvas,
				rect.right, rect.bottom,
				rect.left, rect.bottom,
				paint);
	} // draw_box (...)

	/************************
	 * A courtesy method to draw a box at the specified
	 * rectangle.  Like the others here, it uses my special
	 * coord system.
	 *
	 * @param rect	Defines where to draw the empty box.
	 */
	public static void draw_box (Canvas canvas, RectD rect,
								Paint paint) {
		draw_line(canvas,
				rect.left, rect.bottom,
				rect.left, rect.top,
				paint);
		draw_line(canvas,
				rect.left, rect.top,
				rect.right, rect.top,
				paint);
		draw_line(canvas,
				rect.right, rect.top,
				rect.right, rect.bottom,
				paint);
		draw_line(canvas,
				rect.right, rect.bottom,
				rect.left, rect.bottom,
				paint);
	} // draw_box (...)


	/************************
	 * This draws a filled rectangle onto the Canvas. This
	 * version handles ints.
	 *
	 * Replacement for drawRect(). Like the others, (0,0)
	 * is at the bottom left.
	 *
	 * @see android.graphics.Canvas#drawRect(Rect, Paint)
	 *
	 * @param rect	Defines where to draw the rectangle.
	 */
	public static void draw_rect (Canvas canvas, Rect rect, Paint paint) {
		canvas.drawRect(rect.left, convert_y (canvas, rect.top),
						rect.right, convert_y (canvas, rect.bottom),
						paint);
	} // draw_rect (canvas, rect, paint)

	/************************
	 * This draws a filled rectangle onto the Canvas.  This is
	 * the float version.
	 *
	 * Replacement for drawRect(). Like the others, (0,0)
	 * is at the bottom left.
	 *
	 * @see android.graphics.Canvas#drawRect(Rect, Paint)
	 *
	 * @param rect	Defines where to draw the rectangle.
	 */
	public static void draw_rect (Canvas canvas, RectF rect, Paint paint) {
		canvas.drawRect(rect.left, convert_y (canvas, rect.top),
						rect.right, convert_y (canvas, rect.bottom),
						paint);
	} // draw_rect (canvas, rect, paint)

	/************************
	 * This draws a filled rectangle onto the Canvas.  This is
	 * the float version.
	 *
	 * Replacement for drawRect(). Like the others, (0,0)
	 * is at the bottom left.
	 *
	 * @see android.graphics.Canvas#drawRect(Rect, Paint)
	 *
	 * @param rect	Defines where to draw the rectangle.
	 */
	public static void draw_rect (Canvas canvas, RectD rect, Paint paint) {
		canvas.drawRect((float)rect.left, (float)convert_y (canvas, rect.top),
						(float)rect.right, (float)convert_y (canvas, rect.bottom),
						paint);
	} // draw_rect (canvas, rect, paint)


	/************************
	 * My replacement for drawText().  Like all the others
	 * here, [0,0] is the bottom left instead of the top
	 * right, everything else like the regular.
	 *
	 * @see android.graphics.Canvas#drawText(String, float, float, Paint)
	 */
	public static void draw_text (Canvas canvas, String text,
								float x, float y,
								Paint paint) {
		canvas.drawText(text, x, convert_y (canvas, y), paint);
	}

	/************************
	 * My replacement for drawText().  Like all the others
	 * here, [0,0] is the bottom left instead of the top
	 * right, everything else like the regular.
	 *
	 * @see android.graphics.Canvas#drawText(String, float, float, Paint)
	 */
	public static void draw_text (Canvas canvas, String text,
								  double x, double y,
								  Paint paint) {
		canvas.drawText(text, (float)x, (float)convert_y (canvas, y), paint);
	}
}
