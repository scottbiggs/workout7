/**
 * Seperate little class so that I can put it in XML files.
 *
 * Don't know exactly why, but when I stopped trying to
 * implement onMeasure(), things started working.  Hmmmm.
 *
 * Usage:
 * 	1.	Instantiate.  This sets everything up, (including
 * 		clearing everything).
 *
 * 	2.	Create a GraphLine for every line graph that you want
 * 		to display.  This is easiest by calling add_graph_points()
 * 		for each group of points you want (this makes each point
 * 		group a line graph).
 *
 *	3.	If you need to start over, call clear().
 *
 *	4.	todo: Set the labels and the x/y axii.
 */
package com.sleepfuriously.hpgworkout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.sleepfuriously.hpgworkout.R.color;



public class GView extends View {

	//----------------------------
	//	Constants
	//----------------------------

	/** Number of pixels between two points */
	public static final float DEFAULT_MIN_POINT_DISTANCE = 6;

	private static final String tag = "GView";

	/** The number of pixels to pad our drawing. */
	private static final float PADDING_LEFT = 16,
			PADDING_RIGHT = 8, PADDING_TOP = 46, PADDING_BOTTOM = 54;

	/** Number of pixels between y-axis lines in the graph. */
	private static final int VERT_LINE_SPACING = 70;

	/** Number of pixesl between x-axis lines in the graph. */
	private static final int HORIZ_LINE_SPACING = 30;

	/** The y value to draw labels along the X-axis. */
	private static final int X_AXIS_LABEL_Y = 5;

	/** Width of the graph lines. */
	protected final float LINE_STROKE_WIDTH = 1.7f;

	/** Size of the dots at the graph nodes */
	protected final float DOT_RADIUS = 2.9f;

	//----------------------------
	//	Data
	//----------------------------

	/** Very useful to have! */
	private Context m_context;

	/**
	 * Holds the data to display.  These are the actual
	 * numbers that are graphed.  Matches to the corresponding
	 * m_graph_nums_date element.
	 *
	 *	NOTE:
	 * If m_graph_nums are added on a day that already exists,
	 * then that value will be added to the existing m_graph_num
	 * and a new item will NOT be created.
	 *
	 * todo:
	 * 	Take these out when GraphBase is done.
	 */
	@Deprecated
	private List<Float> m_graph_nums = new ArrayList<Float>();

	/**
	 * The match to m_graph_nums.  This supplies the date for
	 * that value.  The date is used to provide a proper x-axis
	 * alignment for the value.
	 *
	 * 	NOTE:
	 * Repeating dates causes the value of original date to be
	 * ADDED by the new value, instead of a new element added
	 * to the list.
	 *
	 * todo:
	 * 	Take these out when GraphBase is done.
	 */
	@Deprecated
	private List<MyCalendar> m_graph_nums_date = new ArrayList<MyCalendar>();

	/**
	 * Holds the all the lines to be graphed.  The caller can reference
	 * them by their ID.
	 */
	private List<GraphCollection> m_graphlist = null;

	/**
	 * If not null, then this holds info about an X-axis
	 * to draw for our graph.  It'll be created externally,
	 * but this class will have to fill in the draw area.
	 */
	public GraphXAxis m_graph_x_axis = null;

	/**
	 * This string defines the beginning of the x-axis.
	 * The y-axis is figured out by the data.
	 */
	private String m_x_label_start = "";
	private String m_x_label_finish = "";

	/** The sizes of the drawing area, as reported by OS. */
	private float m_official_height, m_official_width;

	/**
	 * The sizes of the drawing area with padding taken into account
	 */
	private float m_usable_height, m_usable_width;

	/** Describes the area that the graph draws in (in my coord system) */
	private RectF m_draw_rect = null;

	/**
	 * These define the parameters of the mapping function:
	 * 		[a, b]  =>  [r, s]
	 *
	 * 	See map_setup() and map() for more details.
	 *
	 * todo
	 * 	Remove this variable.  It's in other classes now.
	 */
//	private float a, b, r, s;
	@Deprecated
	GraphMap m_mapper;

	/** Used during onDraw(). */
	Paint m_paint = null;

	/** Used during onDraw() occassionally. */
	@Deprecated
	Rect m_temp_rect = null;

	/**
	 * Also used in onDraw().  Like the others, allocation is taken
	 * out of that method to speed things up (and eliminate garbage
	 * collection during that important method!).
	 */
	List <Float> m_unique_graph_nums = null;

	//----------------------------
	//	Constructors (ALL are needed for inflating!)
	//----------------------------
	public GView(Context context) {
		super(context);
		m_context = context;
		init();
	}

	public GView(Context context, AttributeSet attrs) {
		super(context, attrs);
		m_context = context;
		init();
	}

	public GView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		m_context = context;
		init();
	}


	/************************
	 * Does some initialization.
	 *
	 * Right now, this is for testing.
	 */
	private void init() {
		if (m_graphlist == null) {
			m_graphlist = new ArrayList<GraphCollection>();
		}

		clear();
		if (m_paint == null) {
			m_paint = new Paint();
		}

		if (m_temp_rect == null) {
			m_temp_rect = new Rect();
		}

		if (m_unique_graph_nums == null) {
			m_unique_graph_nums = new ArrayList<Float>();
		}

		setWillNotDraw(false);	// The default, but just in case something weird happens
	} // init()


	//----------------------------
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		m_official_height = (float) h;
		m_official_width = (float) w;
//		Log.v(tag, "onSizeChanged(): w = " + w + ", h = " + h);

		m_usable_height = m_official_height - (PADDING_TOP + PADDING_BOTTOM);
		m_usable_width = m_official_width - (PADDING_LEFT + PADDING_RIGHT);
//		Log.v(tag, "onSizeChanged(): usable width = " + m_usable_width +
//				", usable height = " + m_usable_height);

		// Update our draw rectangle
		if (m_draw_rect == null) {
			m_draw_rect = new RectF(0 + PADDING_LEFT, h - PADDING_TOP,
									w - PADDING_RIGHT, 0 + PADDING_BOTTOM);
		}
		else {
			m_draw_rect.set(0 + PADDING_LEFT, h - PADDING_TOP,
							w - PADDING_RIGHT, 0 + PADDING_BOTTOM);
		}

		for (GraphCollection graph : m_graphlist) {
			graph.m_line_graph.set_draw_area(m_draw_rect);
			graph.m_line_graph.map_points();
		}

	} // onSizeChanged(w, h, oldw, oldh)


	//----------------------------
	@Override
	protected void onDraw(Canvas canvas) {
		// Are we loading? Then just exit.
		if (GraphActivity.m_loading) {
			Log.v(tag, "onDraw: EXITING because we're loading");
			return;
		}

		// Some basic setup stuff.
		float dot_size = DOT_RADIUS;
		m_paint.setStrokeWidth(0);		// hairline (min. of 0)
		m_paint.setColor(getResources().getColor(color.ghost_white));
		m_paint.setAntiAlias(true);
		m_paint.setTextSize(20);


		// Check to see if there's anything to draw.
		if (m_graphlist.size() == 0) {
			String msg = this.getContext().getString(R.string.graph_no_sets_msg);
			m_paint.getTextBounds(msg, 0, msg.length(), m_temp_rect);
			canvas.drawText(msg,
					m_official_width / 2 - m_temp_rect.width() / 2,
					m_official_height / 2 - m_temp_rect.height() / 2,
					m_paint);
			return;
		}

		//
		// todo
		//	Draw the axii here
		//
		if (m_graph_x_axis != null) {
			if (m_graph_x_axis.is_draw_area_set() == false) {
				// set the draw_area
				RectF x_axis_rect = new RectF(m_draw_rect);
				x_axis_rect.bottom = 0;
				x_axis_rect.top = m_draw_rect.bottom;
				m_graph_x_axis.set_draw_area(x_axis_rect);
			}
			m_paint.setColor(getResources().getColor(color.ghost_white));
			m_graph_x_axis.draw(canvas, m_paint);
		}

//		m_paint.setColor(getResources().getColor(color.hpg_orange));
		m_paint.setAntiAlias(true);
		m_paint.setStrokeWidth(LINE_STROKE_WIDTH);

		// Draw all the lines
		for (GraphCollection graph : m_graphlist) {
			if (graph.m_line_graph.is_draw_area_set() == false) {
				graph.m_line_graph.set_draw_area(m_draw_rect);	// Could slow down the drawing quite a bit
			}
			m_paint.setColor(graph.m_color);
			graph.m_line_graph.draw(canvas, m_paint);
		}


	} // onDraw(canvas)

	//----------------------------
	//	This is called every time a draw is needed.  Therefore a dirty
	//	bit is superfluous.
	//
	//	Okay, I learned something.  The Canvas describes the entire
	//	screen, but we're only allowed to draw in the clipBounds.
	//	Yes, I believe they can be changed, but that's not interesting
	//	at the moment.
	//
	//	The clipBounds actually does some work for us:
	//		It translates from 0,0 (when we enter that location) to
	//		the top left of our view.  Yay!  And the bottom and right
	//		portion of the clipBounds are our height and width.  Double
	//		yay.  Now I get it.
	//
//	@Override
	protected void onDraw_old(Canvas canvas) {
		float dot_size = DOT_RADIUS;

		m_paint.setStrokeWidth(0);		// hairline (min. of 0)
		m_paint.setColor(getResources().getColor(color.ghost_white));

		// Are we loading? Then just exit.
		if (GraphActivity.m_loading) {
			Log.v(tag, "onDraw: EXITING because we're loading");
			return;
		}

		// Check to see if there's anything to draw.
		int num_sets = m_graph_nums.size();
		if (num_sets == 1) {
			dot_size += dot_size;	// Double dot size for singletons.
		}

		if (num_sets < 1) {
//			Log.d(tag, "onDraw(): num_sets = " + num_sets);
			m_paint.setAntiAlias(true);
			m_paint.setTextSize(20);

			String msg;
			switch (num_sets) {
				case 0:		// No sets
					msg = this.getContext().getString(R.string.graph_no_sets_msg);
					break;
//				case 1:		// Just one set, do something special and exit.
//					float y = m_graph_nums.get(0);
//					draw_1_set_graph(y, canvas, paint);
//					return;
				default:		// All other cases.
					msg = this.getContext().getString(R.string.graph_illegal_sets_msg);
					break;
			}
			m_paint.getTextBounds(msg, 0, msg.length(), m_temp_rect);
			canvas.drawText(msg,
					m_official_width / 2 - m_temp_rect.width() / 2,
					m_official_height / 2 - m_temp_rect.height() / 2,
					m_paint);
			return;
		}

		// Find the minimum and max.  This is complicated by the
		// fact that any value of -1 is invalid.
		float min, max;
		int first_valid = 0;		// The first valid value.
		while ((first_valid < m_graph_nums.size()) &&
				(m_graph_nums.get(first_valid) == -1)) {
			first_valid++;
		}
		if (first_valid == m_graph_nums.size()) {
			// All the entries are invalid!
			draw_invalid_entries (canvas, m_paint);
			return;
		}

		min = max = m_graph_nums.get(first_valid);
		for (int i = first_valid + 1; i < m_graph_nums.size(); i++) {
			float num = m_graph_nums.get(i);
			if (num != -1) {
				// Only do this if the number is valid
				if (num < min)
					min = num;
				if (num > max)
					max = num;
			}
		}

		// If the min and the max are the same, just modify the max and
		// min a little bit.
		if (min == max) {
			min--;
			max++;
		}

		// Figuring out the right number of y-axis lines.  First,
		// make sure that we have the minimum based on how much
		// room there is.
		int num_y_lines = (int) (m_usable_height / VERT_LINE_SPACING);
//		Log.v(tag, "Trying num_y_lines of " + num_y_lines);
		if (num_y_lines < 3) {
			num_y_lines = 3;		// Always need at least three lines
//			Log.i(tag, "Very little vertical room, manually setting num_y_lines to 3.");
		}

		// How many different numbers do we have to graph?  This affects
		// the graph tremendously.
		m_unique_graph_nums.add(m_graph_nums.get(0));
		for (int i = first_valid; i < m_graph_nums.size(); i++) {
			// If this number is not already in our list, add it.
			float f = m_graph_nums.get(i);
			if ((f != -1) && (m_unique_graph_nums.indexOf(f) == -1)) {	// The -1 is so that we're not testing invalid numbers
				m_unique_graph_nums.add(f);
			}
		}
		// Use this list of unique numbers to determine the number
		// or y-axis lines.
		if (m_unique_graph_nums.size() < 4) {
			num_y_lines = 3;
		}
		else if (m_unique_graph_nums.size() < 5) {
			num_y_lines = 4;
		}
//		Log.v(tag, "num_y_lines finally: " + num_y_lines);

		// Draw the y-axis lines.
		heckbert_loose_label(min, max, num_y_lines,
							canvas, m_paint);

		// Draw all the lines
		for (GraphCollection graph : m_graphlist) {
			graph.m_line_graph.draw(canvas, m_paint);
		}

		/**
		 * The number of horizontal pixels between graphed
		 * points.
		 */
//		float horiz_spacing = (m_usable_width - (LEFT_PADDING + RIGHT_PADDING))
//						/ (float) m_graph_nums.size();

		// Take two.  This version takes into account
		MyCalendar start_day = m_graph_nums_date.get(0);
		MyCalendar end_day = m_graph_nums_date.get(m_graph_nums.size() - 1);
		float num_days = (float) (end_day.get_difference_in_days(start_day));
		if (num_days != 0) {
			Log.v(tag, "num_days = " + num_days);
			float horiz_spacing = (m_usable_width - (PADDING_LEFT + PADDING_RIGHT))
									/ num_days;
			Log.v(tag, "horiz_spacing = " + horiz_spacing);

//			draw_x_axis_lines (horiz_spacing, canvas, m_paint);
			new_draw_x_axis_lines (canvas, m_paint);

//			draw_the_points (canvas, m_paint, horiz_spacing, dot_size);


			m_paint.setColor(getResources().getColor(color.hpg_orange));
			m_paint.setAntiAlias(true);
			m_paint.setStrokeWidth(LINE_STROKE_WIDTH);



			// TODO:
			//	Move this out of a critical loop and into a much less
			//	time sensitive place.
			//
			// Create the list of points
//			ArrayList<PointF> pts = new ArrayList<PointF>();
//			long first_day = m_graph_nums_date.get(0).get_absolute_days();
//			long last_day = -1;
//			float smallest = Float.MAX_VALUE;
//			float largest = -Float.MAX_VALUE;
//			for (int i = 0; i < m_graph_nums_date.size(); i++) {
//				long day = m_graph_nums_date.get(i).get_absolute_days();
//				if (day != last_day) {
//					float val = m_graph_nums.get(i);
//					// Add unique days to our list
//					pts.add(new PointF(day, val));
//					last_day = day;
//					if (val < smallest)
//						smallest = val;
//					if (val > largest)
//						largest = val;
//				}
//			}

//			RectF bounds_rect = new RectF(first_day, largest, last_day, smallest);

//			GraphLine line = new GraphLine(pts, bounds_rect, m_draw_rect);
//			line.draw(canvas, m_paint);


//			new_draw_the_points (pts, bounds_rect, draw_rect, canvas, m_paint);


		}

		else {
			// todo:
			//	handle the situation where all the sets (or the only set)
			//	fall on the same day.
		}

	} // onDraw (canvas)


	/*******************
	 * This version uses the GraphLine class.  This is
	 * just a test method, to make sure that the GraphLine
	 * class works.  All the prep work should be done
	 * earlier for efficiency.
	 *
	 * preconditions:
	 * 	There is more than one set to draw!
	 *
	 * @param pts_array		An array of 2D points to draw.  The vertical (y)
	 * 						component is simply the values.  The horiz
	 * 						aspect corresponds to some other value (probably
	 * 						the day that the exercise happened).
	 * 						This should be created as points are added.
	 *
	 * @param canvas		Something to draw on.
	 *
	 * @param paint		Something to paint with.
	 */
	@Deprecated
	protected void new_draw_the_points(ArrayList<PointF> pts_list,
									RectF bounds_rect, RectF draw_rect,
									Canvas canvas, Paint paint) {


		// todo:
		//	TESTING.  Trying to see exactly where our draw rectangle is.
//		{
//		Paint tpaint = new Paint(m_paint);
//		tpaint.setStrokeWidth(0);
//
//		// YELLOW:
//		tpaint.setColor(getResources().getColor(color.yellow));
//		Rect trect = canvas.getClipBounds();
//		Log.d(tag, "clipBounds = " + trect.toString());
//		canvas.drawCircle(trect.right, trect.top, 25, tpaint);
//		canvas.drawCircle(trect.right, trect.bottom, 25, tpaint);
//
//		// X
//		canvas.drawLine(trect.left, trect.top, trect.right, trect.bottom, tpaint);
//		canvas.drawLine(trect.left, trect.bottom, trect.right, trect.top, tpaint);
//
//		// box
//		canvas.drawLine(trect.left, trect.top, trect.left, trect.bottom, tpaint);
//		canvas.drawLine(trect.right, trect.top, trect.right, trect.bottom, tpaint);
//		canvas.drawLine(trect.left, trect.top, trect.right, trect.top, tpaint);
//		canvas.drawLine(trect.left, trect.bottom, trect.right, trect.bottom, tpaint);
//
//		}
		//
		// END Testing


		// find our drawing rect.  Remember that in my system
		// the origin is at the bottom left.
//		Rect cliprect = canvas.getClipBounds();
//		RectF draw_rect = new RectF(cliprect.left, cliprect.bottom,	// Yes, a little swapping because
//									cliprect.right, cliprect.top);	// of diff. coords.
//		Log.d (tag, "cliprect = " + cliprect);
//		Log.d(tag, "draw_rect = " + draw_rect);
//
//		draw_rect.left += PADDING_LEFT;
//		draw_rect.right -= PADDING_RIGHT;
//		draw_rect.bottom += PADDING_BOTTOM;
//		draw_rect.top -= PADDING_TOP;

		// Convert to a regular array.
//		PointF[] pts_array = (PointF[]) pts.toArray(new PointF[pts.size()]);

//		RectF bounds_rect = new RectF(first_day, largest, last_day, smallest);

//		GraphLine line = new GraphLine(pts_array,
//									bounds_rect, draw_rect);
//		GraphLine line = new GraphLine(pts_list, bounds_rect, draw_rect);
//		line.draw(canvas, paint);
	} // new_draw_the_points(...)


	/********************
	 * Draws the little vertical lines at the bottom of the screen.
	 * These give the user a bit of information about the x axis,
	 * including some labels as well.
	 *
	 * preconditions:
	 * 	m_graph_nums_date		Filled with appropriate data.
	 *
	 * @param canvas
	 * @param paint
	 */
	protected void new_draw_x_axis_lines (Canvas canvas, Paint paint) {

		// todo: remove this comment and actually use this!
//		GraphXAxis axis_drawer = new GraphXAxis(x_pts, boundaries, draw_area);
	} // new_draw_x_axis_lines(...)


	/********************
	 * Took the part that draws the lines out of the onDraw() method.
	 * This way we can call this at different times for different
	 * reasons.
	 *
	 * @param canvas
	 * @param paint
	 * @param horiz_spacing
	 * @param dot_size
	 */
	@Deprecated
	protected void draw_the_points(Canvas canvas, Paint paint,
								float horiz_spacing, float dot_size) {

		MyCalendar first_day = m_graph_nums_date.get(0);

		// Draw each point!
//		float last_x = 0 + (horiz_spacing / 2);
		float last_x = 0;
		last_x = conv_x(last_x);		// add the padding

		float last_y = conv_y(m_mapper.map(m_graph_nums.get(0)));
//		float last_y = conv_y(map(m_graph_nums.get(0)));	// quick way


		// (Since map_setup() has already been called very recently,
		// there's little need to do it again.)
		m_paint.setColor(getResources().getColor(color.hpg_orange));
		m_paint.setAntiAlias(true);
		m_paint.setStrokeWidth(LINE_STROKE_WIDTH);


		// Draw the first dot.  But only if the spacing is big
		// enough!
		if (horiz_spacing > DOT_RADIUS + 2) {
			draw_dot (last_x, last_y, m_graph_nums.get(0), dot_size,
						canvas, m_paint);
		}

		Log.d(tag, "m_graph_nums_date(0) day = " + m_graph_nums_date.get(0).get_day());

		// The LOOP!
		for (int i = 1; i < m_graph_nums_date.size(); i++) {
//			float x = i * horiz_spacing + (horiz_spacing / 2);

			Log.d(tag, "m_graph_nums_date(" + i + ") day = " + m_graph_nums_date.get(i).get_day());
			float x = m_graph_nums_date.get(i).get_difference_in_days(first_day);
			x *= horiz_spacing;
			x = conv_x(x);

			float y = m_graph_nums.get(i);
			y = m_mapper.map(y);
//			y = map(y);
			y = conv_y(y);

			// Only draw the line if this and the last ends are valid.
			if ((m_graph_nums.get(i - 1) != -1) &&
				(m_graph_nums.get(i) != -1)) {
				canvas.drawLine(last_x, last_y, x, y, m_paint);
				Log.d(tag, "Line drawn from " + last_x + ", " + last_y + " to "
					+ x + ", " + y);
			}
			if (m_graph_nums.get(i) != -1) {
				canvas.drawCircle(x, y, dot_size, m_paint);
				Log.d(tag, "Dot drawn at " + x + ", " + y);
			}
			last_x = x;
			last_y = y;
		}
	} // draw_the_points (canvas, paint, horiz_spacing, dot_size)

	/********************
	 * Draws a dot at the position indicated.  If orig_y is
	 * invalid (-1), then nothing is done.
	 *
	 * @param screen_x	The actual screen x coordinate.
	 * @param screen_y	The actual screen y coordinate.
	 * @param orig_y		The ORIGINAL y value.
	 * @param dot_size	Radius of the dot to draw.
	 * @param canvas
	 * @param paint
	 */
	protected void draw_dot (float screen_x, float screen_y,
							float orig_y, float dot_size,
							Canvas canvas, Paint paint) {
		if (orig_y == -1)
			return;

		canvas.drawCircle(screen_x, screen_y, dot_size, paint);
		Log.d(tag, "Dot drawn at " + screen_x + ", " + screen_y);
	} // draw_dot (...)


	/********************
	 * Display for when all the entries are invalid.  Could happen
	 * if the user changed the significant aspect of the exercise.
	 */
	protected void draw_invalid_entries (Canvas canvas, Paint paint) {
		paint.setAntiAlias(true);
		paint.setTextSize(19);

		String msg = this.getContext()
			.getString(R.string.graph_all_invalid_msg);

//		Rect rect = new Rect();
//		paint.getTextBounds(msg, 0, msg.length(), rect);
//		canvas.drawText(msg,
//				m_official_width / 2 - rect.width() / 2,
//				m_official_height / 2 - rect.height() / 2,
//				paint);

		TextPaint tpaint = new TextPaint(paint);
		StaticLayout layout =
			new StaticLayout(msg, tpaint,
				(int) m_usable_width,
				Layout.Alignment.ALIGN_CENTER,
				1.1f, 0, true);

		// Translate this canvas according to the layout's width
		// and height
		float dx = (m_official_width - layout.getWidth()) / 2f;
		float dy = (m_usable_height - layout.getHeight()) / 2f;
		canvas.translate(dx, dy);
		layout.draw(canvas);
	} // draw_invalid_entries()

	/********************
	 * Draws the graph for the case where there's just one set.  Similar
	 * to draw_one_number_graph().
	 *
	 * @param y			The single y value to draw.
	 * @param canvas
	 * @param paint
	 */
	protected void draw_1_set_graph (float y,
									Canvas canvas, Paint paint) {
		String msg = this.getContext().getString(R.string.graph_1_set_msg, "" + y);
		Rect rect = new Rect();
		paint.setAntiAlias(true);
		paint.setTextSize(20);
		paint.getTextBounds(msg, 0, msg.length(), rect);
		canvas.drawText(msg,
				m_official_width / 2 - rect.width() / 2,
				m_official_height / 2 - rect.height() / 2,
				paint);
		return;
	} // draw_1_set_graph (y, canvas, paint)


	/********************
	 * Draws the little vertical lines at the bottom of the screen.
	 * These give the user a bit of information about the x axis,
	 * including some labels as well.
	 *
	 * preconditions:
	 * 	m_graph_nums		Filled with appropriate data.
	 *
	 * @param horiz_spacing	Pixels per number in m_graph_nums.
	 * @param canvas		Ready to use.
	 * @param paint		ibid
	 */
	protected void draw_x_axis_lines (float horiz_spacing,
									Canvas canvas, Paint paint) {
		// Beginning to use the new GraphXAxis class.
		//

		// todo:
		//	This should probably be in a constructor somewhere.
//		Rect padding_rect = new Rect((int)PADDING_LEFT, (int)PADDING_TOP, (int)PADDING_RIGHT, (int)PADDING_BOTTOM);
//		GraphXAxis x_axis = new GraphXAxis(m_context, canvas, padding_rect);
//		x_axis.m_paint = new Paint();
//		x_axis.m_paint.setColor(getResources().getColor(color.ghost_white));
//		x_axis.m_paint.setAntiAlias(false);	// Just vert lines
//		x_axis.draw();
		//
		// End of new stuff

		paint.setColor(getResources().getColor(color.ghost_white));
		paint.setAntiAlias(false);	// Just vert lines

		// Don't draw all of the lines if they are bunched
		// together too closely.
		float first_x = -1;
		float last_x = -HORIZ_LINE_SPACING;
		float days_from_first;


		// find the first day and make that our base.
		MyCalendar first_day = m_graph_nums_date.get(0);
		MyCalendar last_day = m_graph_nums_date.get(m_graph_nums.size() - 1);

		for (int i = 0; i < m_graph_nums.size(); i++) {
			days_from_first = (float) m_graph_nums_date.get(i).get_difference_in_days(first_day);
//			float x = days_from_first * horiz_spacing + (horiz_spacing / 2f);
			float x = days_from_first * horiz_spacing;
			x = conv_x(x);

			if (x >= last_x + HORIZ_LINE_SPACING) {
				if (first_x == -1) {
					first_x = x;
				}
				canvas.drawLine((int)x, m_official_height,
					(int)x, m_official_height - X_AXIS_LABEL_Y,
					paint);
				last_x = x;
			}
		}

		// draw the labels at first_x & last_x.  I'm centering
		// the label over the line.
		paint.setAntiAlias(true);	// neat text
		Rect rect = new Rect();
		paint.getTextBounds(m_x_label_start,
				0, m_x_label_start.length(), rect);
		canvas.drawText(m_x_label_start,
				first_x - rect.width() / 2,
				m_official_height - (X_AXIS_LABEL_Y + 2), paint);

		paint.getTextBounds(m_x_label_finish,
				0, m_x_label_finish.length(), rect);
		canvas.drawText(m_x_label_finish,
				last_x - rect.width() / 2,
				m_official_height - (X_AXIS_LABEL_Y + 2), paint);
	} // draw_x_axis_lines (canvas, paint)


	/********************
	 * Takes the given number which is in the normal sort
	 * of graphing number (you know, where the y-axis gets
	 * bigger as it goes up) and converts it to a proper
	 * screen y coordinate.
	 *
	 * preconditions:
	 * 		- m_official_height (height of the drawing area) must be
	 * 		properly set.
	 * 		- m_vert_spacing must be set.
	 *
	 * 		PADDING must be correct.
	 *
	 * @param y	The value to convert.
	 *
	 * @return	The y value to actually draw.
	 */
	private float conv_y (float y) {
		return m_usable_height - y + PADDING_TOP;
	} // conv_y (y)


	/********************
	 * Converts usable coordinate to actual screen coordinate.
	 */
	private float conv_x (float x) {
		return PADDING_LEFT + x;
	}

//	/********************
//	 * Adds a set of points to be graphed.  The points will be
//	 * stretched to fill the rectangle as best possible.
//	 *
//	 * @param points		A list of x and y coordinates to be shown
//	 * 					in a graph.
//	 * @param color		A color to paint this graph.
//	 * @param bounds		This rectangle describes the logical boundaries
//	 * 					of points.  The left-right are the logical beginning
//	 * 					and end of the x-axis.  The y-axis is similar.
//	 *
//	 * @param start		The string to display at the beginning of the
//	 * 					x-axis.
//	 * @param end		The end-point display string for the x-axis.
//	 *
//	 * @return	A handle to refer to this graph later.
//	 * 			-1 on error.
//	 */
//	public int add_graph_points (ArrayList<PointF> points, int color,
//								RectF bounds,
//								String start, String end) {
//		if (m_draw_rect == null) {
//			Log.e (tag, "add_graph_points(): m_draw_rect has not been allocated yet!");
//			return -1;
//		}
//
//		GraphLine line = new GraphLine(points, bounds, m_draw_rect);
//		int id = m_graphlist.size();	// Set the ID of this GraphLine
//		line.set_id(id);
//		m_graphlist.add(line);
//		return id;
//	} // add_graph_points(points)
//
	/********************
	 * Adds a collection for a graph to display in this widget.
	 *
	 * The collection contains the axii and the line graph
	 * objects necessary to display a graph in this widget.
	 *
	 * @param grapher	The GraphCollection to add.  Doesn't
	 * 					have to be complete yet.
	 *
	 * @return	The total number of GraphCollections this class
	 * 			holds after adding this one.
	 */
	public int add_graph_collection(GraphCollection grapher) {
		m_graphlist.add(grapher);
		return m_graphlist.size();
	} // add_graph_collection (grapher)

	/********************
	 * This allows you to add a point to this Widget, one
	 * at a time.
	 *
	 * O(n)
	 *
	 * NOTE:
	 * 		The points need to be added in order.
	 *
	 * NOTE 2:
	 * 		Adding a value with the same DAY as an existing
	 * 		point will NOT cause a new item to be added.
	 * 		Instead, the value will be added to the existing
	 * 		value on that same day.
	 *
	 * @param x		The value of this point.  It can be in
	 * 				any range--the class will figure things out.
	 *
	 * @param date	The time of this set.
	 *
	 * @return	1	The point was added to the list.
	 * 			0	The point matched the date of an existing
	 * 				point which was incremented by x amount.
	 *
	 * todo:
	 * 	Allow points to be added to a specific graph
	 */
	@Deprecated
	public int add_point (float x, MyCalendar date) {
		for (int i = 0; i < m_graph_nums.size(); i++) {
			if (date.is_same_day(m_graph_nums_date.get(i))) {
				float temp = m_graph_nums.get(i) + x;
				m_graph_nums.set(i, temp);
				return 0;
			}
		}

		m_graph_nums.add(x);
		m_graph_nums_date.add(date);

		return 1;
	} // add_point (x, date)


	/********************
	 * Same as above, but allows you to add an entire array
	 * at once.
	 *
	 * NOTE:
	 * 	This routine is pretty dumb.  It assumes that all
	 * 	the dates are on different days.
	 *
	 * todo:
	 * 	FIX THIS DUMBNESS!!  I'll keep it deprecated until it's fixed.
	 *
	 * @param vals	A bunch of values.
	 *
	 * @param dates	All the dates.  The should be all seperate
	 * 				days!!!
	 *
	 * todo:
	 * 	Allow points to be added to a specific graph
	 */
	@Deprecated
	public void add_points (Float[] vals, MyCalendar[] dates) {
		Collections.addAll(m_graph_nums, vals);
		Collections.addAll(m_graph_nums_date, dates);
	}

	/********************
	 * Removes all the points from this widget.
	 *
	 * todo:
	 * 	Moved to GraphBase. Take it out when it's done.
	 */
	public void clear() {
		m_graph_nums.clear();
		m_graph_nums_date.clear();
		m_graphlist.clear();
	}

	/**********************
	 * Sets the label strings at the bottom of the graph.
	 * These will probably be the beginning/ending dates.
	 *
	 * @param start		The label to denote the beginning
	 * 					of the graph.
	 *
	 * @param finish		The label for the end.
	 */
	public void set_x_axis_labels (String start, String finish) {
		m_x_label_start = start;
		m_x_label_finish = finish;
	}


//	/*********************
//	 * Hack as Android doesn't support this Math library.
//	 *
//	 * @param 	f
//	 * @return	The exponent component of f.
//	 */
//	public static int getExponent(double f) {
//		return (int) (Math.log(f)/Math.log(2));
//	}

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
		graphmin = (float) (android.util.FloatMath.floor(min / d) * d);
		graphmax = (float) (android.util.FloatMath.ceil(max / d) * d);

		// number of fractional digits
		nfrac = (int) Math.max(-Math.floor(Math.log10(d)), 0);

		// Need to map the y values to screen y values.
//		map_setup (graphmin, graphmax, 0, m_usable_height);
		m_mapper = new GraphMap(graphmin, graphmax, 0, m_usable_height);

		for (y = graphmin; y <= graphmax + .5 * d; y += d) {
//			Log.v (tag, "looping through label lines: y = " + y);
			float y2 = m_mapper.map(y);
//			float y2 = map(y);
			y2 = conv_y(y2);
			paint.setAntiAlias(false);	// just a horiz line
			canvas.drawLine(PADDING_LEFT, y2,
					m_official_width - PADDING_RIGHT, y2, paint);
//			Log.v (tag, "\tline at " + y + ", converted to " + y2);

			String str = new DecimalFormat("#.######").format(y);

			Rect rect = new Rect();
			paint.getTextBounds(str, 0, str.length(), rect);

			paint.setAntiAlias(true);
			canvas.drawText(str, PADDING_LEFT, y2 - 2, paint);	// -2 to seperate the text from the line
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
