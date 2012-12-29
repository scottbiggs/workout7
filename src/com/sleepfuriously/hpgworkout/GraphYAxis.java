/**
 * The sibling of GraphXAxis, this class draws the
 * Y axis according to the given specs.  It also
 * handles drawing any horizontal lines of the
 * graphing system.
 */
package com.sleepfuriously.hpgworkout;

import java.text.DecimalFormat;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import static java.lang.Float.NaN;

import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_line;
import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_text;


public class GraphYAxis {

	//-------------------------------
	//	Constants
	//-------------------------------
	private static final String tag = "GraphYAxis";

	/** Mininum pixels between lines along the y-axis */
	public static final float DEFAULT_LINE_SPACING = 70f;
	
	/**
	 * Because there's some slop in the size of letters,
	 * I nudge the labels a little by this much to make
	 * it look better aligned.
	 */
	private final static int LABEL_FUDGE_FACTOR = 2;
	
	//-------------------------------
	//	Data
	//-------------------------------

	/**
	 * The min and max y values.  Original
	 * values (not converted to screen coords).
	 * <p>
	 * This corresponds to the boundary rectangle
	 * of the GraphLine class.
	 */
	protected float m_orig_min = NaN, m_orig_max = NaN;

	/**
	 * The rectangle to draw our y axis.  It's part
	 * of the entire canvas, but uses my screen coords.
	 */
	protected RectF m_draw_area = null;

	public float m_line_spacing = DEFAULT_LINE_SPACING;

	//-------------------------------
	//	Constructors
	//-------------------------------

	/*****************************
	 * Constructor.
	 *
	 * @param min	The smallest value of the y-values
	 * 				that we're graphing.  Essentially this
	 * 				is just the small y-value of all the
	 * 				points in GraphLine.
	 *
	 * @param max	The largest of the y-values that we're
	 * 				graphing.  Should be same as the largest
	 * 				y-value point in the original list of
	 * 				the corresponding GraphLine class.
	 *
	 * @param draw_area	The area of the canvas to draw the
	 * 					y-axis in.  Need this to properly
	 * 					set up the mapping functions.
	 * 					- Make sure that the height (top & bottom)
	 * 					are the same as the call to GraphLine!
	 * 					Otherwise the ticks won't match the
	 * 					graph.
	 * 					- The width (left & right) describe
	 * 					the total width (lines and text).
	 */
	public GraphYAxis (float min, float max, RectF draw_area) {
		m_orig_min = min;
		m_orig_max = max;
		m_draw_area = draw_area;
	} // constructor

	/*****************************
	 * Constructor for when we don't know the draw
	 * area.
	 *
	 * @param min	The smallest value of the y-values
	 * 				that we're graphing.  Essentially this
	 * 				is just the small y-value of all the
	 * 				points in GraphLine.
	 *
	 * @param max	The largest of the y-values that we're
	 * 				graphing.  Should be same as the largest
	 * 				y-value point in the original list of
	 * 				the corresponding GraphLine class.
	 */
	public GraphYAxis (float min, float max) {
		m_orig_min = min;
		m_orig_max = max;
	}

	/*****************************
	 * The bare-bones constructor.
	 */
	public GraphYAxis() {
	}


	//-------------------------------
	//	Methods
	//-------------------------------

	/*****************************
	 * Sets the upper and lower logical range for the
	 * y-axis.  These are in the original number space.
	 *
	 * @param min	The lowest number we'll graph.
	 * @param max	The largest.
	 */
	public void set_range (float min, float max) {
		m_orig_min = min;
		m_orig_max = max;
	}


	/*****************************
	 * Returns the current logical floor of the
	 * y-axis.  Returns NaN if it hasn't been set.
	 */
	public float get_min() {
		return m_orig_min;
	}

	/*****************************
	 * Returns the current logical ceiling of the
	 * y-axis.  Returns NaN if it hasn't been set.
	 */
	public float get_max() {
		return m_orig_max;
	}

	/*****************************
	 * Given a number in the logical space, this
	 * maps where it should be along the y-axis in
	 * screen coordinates (yeah, using my screen
	 * coordinate system).
	 * <p>
	 * It's perfectly ok if <i>num</i> is out of
	 * our normal range.  This will extrapolate.
	 * <p>
	 * <b>NOTE</b>: If either the draw area or the
	 * min or the max have not been set, this will
	 * return NaN!
	 */
	protected float map (float num) {
		if ((m_orig_min == NaN) ||
			(m_orig_max == NaN) ||
			(m_draw_area == null)) {
			return NaN;
		}

		GraphMap mapper;
		mapper = new GraphMap(m_orig_min, m_orig_max,
							m_draw_area.bottom, m_draw_area.top);

		return mapper.map(num);
	} // map (num)


	/*****************************
	 * Call this to draw the y-axis.
	 * <p>
	 * <b>NOTE</b>: If either the draw area or the
	 * min or the max have not been set, this will
	 * do nothing.
	 *
	 * @param canvas		The Canvas to draw on.
	 *
	 * @param paint		The paint to use.  The color should
	 * 					already be set.
	 */
	public void draw (Canvas canvas, Paint paint) {
		if ((m_orig_min == NaN) ||
			(m_orig_max == NaN) ||
			(m_draw_area == null)) {
			return;
		}

		// Figuring out the right number of y-axis lines.  First,
		// make sure that we have the minimum based on how much
		// room there is.
		int num_y_lines = (int) (m_draw_area.height() / m_line_spacing);
		if (num_y_lines < 3) {
			num_y_lines = 3;		// Always need at least three lines
		}

		// Draw the y-axis lines.
		heckbert_loose_label(m_orig_min, m_orig_max,
							num_y_lines,
							canvas, paint);

	} // draw(...)


	/*********************
	 * Heckbert's version of drawing label ticks for
	 * a graph.  From Graphics Gems, v. 1.
	 *
	 * NOTE:
	 * 	This only works for non-negative numbers.
	 *
	 * side effect:
	 * 		The map_setup() is called and properly set for this graph.
	 * 		So successive calls to map() should work as long as the
	 * 		size of the screen hasn't changed (and it might!!!).
	 *
	 * @param min	Minimum value.
	 * @param max	Max.
	 * @param ntick	Preferred number of ticks
	 * @param canvas What to draw on.
	 * @param paint	 What to draw with.
	 *
	 * @return	The graphmin. This is the minimum number that the
	 * 			graph shows, and it should be used to subtract
	 * 			to find the bottom of our drawing!
	 */
	protected float heckbert_loose_label (float min, float max,
										int ntick,
										Canvas canvas,
										Paint paint) {
		int nfrac;
		float d;			// Tick mark spacing
		float graphmin, graphmax;	// graph range min & max
		float range, y;

		range = heckbert_nicenum(max - min, false);
		d = heckbert_nicenum(range / (ntick - 1), true);
		graphmin = (float) (Math.floor(min / d) * d);
		graphmax = (float) (Math.ceil(max / d) * d);

		// number of fractional digits
		nfrac = (int) Math.max(-Math.floor(Math.log10(d)), 0);

		// Need to map the y values to screen y values.
//		map_setup (graphmin, graphmax, 0, m_usable_height);
//		GraphMap mapper = new GraphMap(graphmin, graphmax, 0, m_draw_area.height());
		GraphMap mapper = new GraphMap(graphmin, graphmax,
									   m_draw_area.bottom, m_draw_area.top);


		for (y = graphmin; y <= graphmax + .5 * d; y += d) {
			paint.setAntiAlias(false);	// just a horiz line
//			Log.v (tag, "looping through label lines: y = " + y);
			float y2 = mapper.map(y);
//			float y2 = map(y);
//			y2 = conv_y(y2);

//			canvas.drawLine(m_draw_area.left, y2, m_draw_area.right, y2, paint);
			draw_line(canvas, m_draw_area.left, y2, m_draw_area.right, y2, paint);
			Log.v (tag, "\tline at " + y + ", converted to " + y2);

			String str = new DecimalFormat("#.######").format(y);

			Rect rect = new Rect();
			paint.getTextBounds(str, 0, str.length(), rect);

			paint.setAntiAlias(true);

			// -2 to seperate the text from the line
			draw_text(canvas, str, m_draw_area.left, y2 + 2, paint);
//			canvas.drawText(str, m_draw_area.left, y2 - 2, paint);
		}

		return graphmin;
	} // heckbert_loose_label (min, max, ntick)


	/*********************
	 * Finds a "nice" number that's close to x.
	 * From Graphics Gems, pp. 62-3.
	 *
	 * @param x		The number to find the nice number of.
	 *
	 * @param round	Should we round or not.  If not, then
	 * 				we take the ceiling.
	 *
	 * @return	The nice number version of x.
	 */
	protected float heckbert_nicenum (float x, boolean round) {
		int exp;		// Exponent of x.
		float frac;	// Fractional part of x.
		float nf;	// Nice, rounded fraction.

		exp = ((int) (Math.floor(Math.log10(x))));
		frac = x / (float)Math.pow(10f, exp);

		if (round) {
			if (frac < 1.5)
				nf = 1;
			else if (frac < 3)
				nf = 2;
			else if (frac < 7)
				nf = 5;
			else
				nf = 10;
		}
		else {
			if (frac <= 1)
				nf = 1;
			else if (frac <= 2)
				nf = 2;
			else if (frac <= 5)
				nf = 5;
			else nf = 10;
		}

		return nf * (float) Math.pow(10, exp);
	} // heckbert_nicenum (x, round)

}
