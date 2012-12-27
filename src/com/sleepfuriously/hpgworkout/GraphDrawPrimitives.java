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
		return rect.bottom - old_y;
	} // convert_y(old_y)

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
		y = convert_y(canvas, y);
		canvas.drawCircle(x, y, radius, paint);
//		Log.d(tag, "drawCircle called at " + x + ", " + y);
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
}
