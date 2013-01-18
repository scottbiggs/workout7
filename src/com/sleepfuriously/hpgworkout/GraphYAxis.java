/**
 * The sibling of GraphXAxis, this class draws the
 * Y axis according to the given specs.  It also
 * handles drawing any horizontal lines of the
 * graphing system.
 */
package com.sleepfuriously.hpgworkout;

import java.text.DecimalFormat;

import android.graphics.Canvas;
import android.graphics.Color;
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

	/** Default color for the horizontal lines across the graph */
	public final static int DEFAULT_Y_LINE_COLOR = Color.LTGRAY;

	/** Default number of tick-marks in the y axis */
	public final static int DEFAULT_NUM_TICKS = 4;

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
	 * The min and max y values.  <i>Original</i>
	 * values (not converted to screen coords).
	 */
	protected float m_orig_min = NaN, m_orig_max = NaN;

	/**
	 * The graph limits as the Heckbert algorithm sees
	 * them.  Based on m_orig_min and max, but might
	 * define a larger spread to make the graph look
	 * nice.
	 * <p>
	 * When polled, these are the numbers returned and should
	 * be used by the corresponding GraphLine class.
	 */
	protected float m_heckbert_min = NaN, m_heckbert_max = NaN;

	/**
	 * The rectangle to draw our y axis.  It's part
	 * of the entire canvas, but uses my screen coords.
	 */
	protected RectF m_draw_area = null;

	public float m_line_spacing = DEFAULT_LINE_SPACING;

	/** The color to draw the horizontal lines */
	public int m_line_color = DEFAULT_Y_LINE_COLOR;

	/** The spacing the for the tic-marks. */
	private float m_tick_spacing = NaN;


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
		set_range(min, max);
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
		set_range(min, max);
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
		heckbert_calc_range();
	}


	/*****************************
	 * Returns the current logical floor of the
	 * y-axis.  Returns NaN if it hasn't been set.
	 */
	public float get_min() {
		return m_heckbert_min;
	}

	/*****************************
	 * Returns the current logical ceiling of the
	 * y-axis.  Returns NaN if it hasn't been set.
	 */
	public float get_max() {
		return m_heckbert_max;
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
		Log.e (tag, "Is this ever called?");

		Log.d(tag, "map(): [" + m_orig_min + ".." + m_orig_max + "]  ==>  ["
			+ m_draw_area.bottom + ".." + m_draw_area.top + "]");

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

		// Draw the y-axis lines.
		heckbert_loose_label(canvas, paint);

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
	 * 			to find the bottom of our drawing!<br/>
	 * 			- NaN on error.
	 */
	protected float heckbert_loose_label (//float min, float max,
//										int ntick,
										Canvas canvas,
										Paint paint) {
		int nfrac;
//		float d;			// Tick mark spacing
//		float m_heckbert_min, m_heckbert_max;	// graph range min & max
		float /* range,*/ y;

//		range = heckbert_nicenum(max - min, false);
//		d = heckbert_nicenum(range / (ntick - 1), true);
//		m_heckbert_min = (float) (Math.floor(min / d) * d);
//		m_heckbert_max = (float) (Math.ceil(max / d) * d);

		if (Float.isNaN(m_heckbert_min) || Float.isNaN(m_heckbert_max)) {
			Log.e(tag, "Can't continue--the heckbert min or max has not been calculated yet! Aborting!");
			return NaN;
		}

		// number of fractional digits
		nfrac = (int) Math.max(-Math.floor(Math.log10(m_tick_spacing)), 0);

		// Need to map the y values to screen y values.
		GraphMap mapper = new GraphMap(m_heckbert_min, m_heckbert_max,
									m_draw_area.bottom, m_draw_area.top);
//		Log.d(tag, "heckbert_loose_label: [" + m_heckbert_min + ".." + m_heckbert_max + "]  ==>  ["
//										+ m_draw_area.bottom + ".." + m_draw_area.top + "]");


		int text_color = paint.getColor();

		for (y = m_heckbert_min;
			y <= m_heckbert_max + .5 * m_tick_spacing;
			y += m_tick_spacing) {
			paint.setAntiAlias(false);	// just a horiz line
//			paint.setColor(m_line_color);
			float y2 = mapper.map(y);

			draw_line(canvas, m_draw_area.left, y2, m_draw_area.right, y2, paint);

			String str = new DecimalFormat("#.######").format(y);

			Rect rect = new Rect();
			paint.getTextBounds(str, 0, str.length(), rect);

			paint.setAntiAlias(true);

			paint.setColor(text_color);
			// +2 to seperate the text from the line
			draw_text(canvas, str, m_draw_area.left, y2 + 2, paint);
		}

		return m_heckbert_min;
	} // heckbert_loose_label (min, max, ntick)


	/*********************
	 * Working mostly by side effect, this figures out
	 * the maximum and minimum of the range as the
	 * Heckbert algorithm sees fit.
	 * <p>
	 * Also note that this code was simply lifted from
	 * the Heckbert algorithm.
	 * <p>
	 * <b>preconditions</b>:<br/>
	 * 	<i>m_orig_min</i> and <i>m_orig_max</i> need to
	 * 	be set to the lowest and highest values of our
	 *	data set.
	 * <p>
	 * <b>side effects</b>:<br/>
	 * 	<i>m_heckbert_min</i> and <i>m_heckbert_max</i>
	 * 	are both set by this method.
	 * <p>
	 * 	<i>m_tick_spacing</i> is set here.
	 */
	void heckbert_calc_range () {
		float range;
		int num_ticks = DEFAULT_NUM_TICKS;

		if (Float.isNaN(m_orig_min) || Float.isNaN(m_orig_max)) {
			Log.e (tag, "Trying to calculate the Heckbert range before the original min and max are set. Aborting!");
			return;
		}

		if (m_draw_area != null) {
			// Figuring out the right number of y-axis lines.  First,
			// make sure that we have the minimum based on how much
			// room there is.
			num_ticks = (int) (m_draw_area.height() / m_line_spacing);
			if (num_ticks < DEFAULT_NUM_TICKS) {
				num_ticks = DEFAULT_NUM_TICKS;
				Log.v(tag, "heckbert_cal_range(): resetting to default number of ticks.");
			}
		}

		range = heckbert_nicenum(m_orig_max - m_orig_min, false);
		m_tick_spacing = heckbert_nicenum(range / (num_ticks - 1), true);
		m_heckbert_min = (float) (Math.floor(m_orig_min / m_tick_spacing) * m_tick_spacing);
		m_heckbert_max = (float) (Math.ceil(m_orig_max / m_tick_spacing) * m_tick_spacing);
	} // calc_heckbert_range (ntick)


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
