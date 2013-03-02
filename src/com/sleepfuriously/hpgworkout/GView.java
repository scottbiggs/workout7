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
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.sleepfuriously.hpgworkout.R.color;
import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_text;
import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_line;
import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_box;
import static com.sleepfuriously.hpgworkout.GraphDrawPrimitives.draw_rect;


public class GView extends View {

	//----------------------------
	//	Constants
	//----------------------------

	private static final String tag = "GView";

	/** Number of pixels between two points */
	public static final float DEFAULT_MIN_POINT_DISTANCE = 10;

	/** The size of the text for the labels (x and y). */
	public static final float DEFAULT_LABEL_TEXT_SIZE = 12;

	/** The size of any messages we display */
	public static final float DEFAULT_MSG_TEXT_SIZE = 25;

	/** The number of pixels wide for the y-axis */
	public static final int DEFAULT_Y_AXIS_WIDTH = 33;

	/** The number of pixels to pad our drawing. */
	private static final int PADDING_LEFT = 26,
			PADDING_RIGHT = 26, PADDING_TOP = 46, PADDING_BOTTOM = 54;

	/** Number of pixels between y-axis lines in the graph. */
	private static final int VERT_LINE_SPACING = 70;

	/** Number of pixesl between x-axis lines in the graph. */
	private static final int HORIZ_LINE_SPACING = 30;

	/** The y value to draw labels along the X-axis. */
	private static final int X_AXIS_LABEL_Y = 5;

	/** Distance between the graph area and the x-axis area */
	private static final int X_AXIS_GAP = 10;

	/** Distance between a y-axis and the next one (or the graph area) */
	private static final int Y_AXIS_GAP = 8;

	/** Width of the graph lines. */
	protected final float LINE_STROKE_WIDTH = 1.8f;

	/** Size of the dots at the graph nodes */
	protected final float DOT_RADIUS = 2.9f;

	//----------------------------
	//	Data
	//----------------------------

	/** Very useful to have! */
	private Context m_context;

	/**
	 * Holds the all the data to be graphed.  The caller can reference
	 * them by their ID.
	 */
	private List<GraphCollection> m_graphlist = null;

	/**
	 * If not null, then this holds info about an X-axis
	 * to draw for our graph.  It'll be created externally,
	 * but this class will have to fill in the draw area.
	 */
	public GraphXAxis m_graph_x_axis = null;

	/** Used during onDraw(). */
	Paint m_paint = null;

	/** The size of the message text */
	protected float m_msg_text_size = DEFAULT_MSG_TEXT_SIZE;


	//-------------------------------
	//	Drawing Coordinate Data
	//-------------------------------

	/** The sizes of the drawing area, as reported by OS. */
	private int m_canvas_height, m_canvas_width;

	/**
	 * The sizes of the drawing area with padding taken into account
	 */
	private int m_usable_height, m_usable_width;

	/**
	 * Describes the whole canvas draw area of this
	 * widget (my coords).  Defined in onSizeChanged().
	 */
	private Rect m_canvas_rect = new Rect();

	/**
	 * Holds the whole canvas area of this widget
	 * AFTER padding has taken into account (my coords).
	 */
	private Rect m_canvas_padded_rect = new Rect();

	/**
	 *  Describes the area that the GraphLine classes
	 *  draw in (in my coord system).
	 */
//	private Rect m_graphline_rect = new Rect();
	private RectF m_graphline_rect = new RectF();

	/**
	 * Describes the draw area of the x-axis within this
	 * widget.
	 */
	private Rect m_x_axis_rect = new Rect();

	/**
	 * Temp to hold a rectangle for a y-axis.
	 * Used to prevent memory allocation during onDraw().
	 */
	private Rect m_y_axis_rect = new Rect();

	/**
	 * Used during onDraw() occassionally. It's defined outside
	 * to avoid declaring any memory during onDraw().
	 */
	Rect m_temp_rect = new Rect();


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
		m_paint.setStrokeWidth(0);		// hairline
		m_paint.setColor(getResources().getColor(color.ghost_white));
		m_paint.setAntiAlias(true);
		m_paint.setTextSize(DEFAULT_LABEL_TEXT_SIZE);

		setWillNotDraw(false);	// The default, but just in case something weird happens
	} // init()


	//----------------------------
	//	Interesting fact:
	//		w and h correspond EXACTLY to the cliprect you get
	//		from onDraw()!
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//		Log.v(tag, "onSizeChanged(): w = " + w + ", h = " + h);
		super.onSizeChanged(w, h, oldw, oldh);
		m_canvas_height = h;
		m_canvas_width = w;

		m_usable_height = m_canvas_height - (PADDING_TOP + PADDING_BOTTOM);
		m_usable_width = m_canvas_width - (PADDING_LEFT + PADDING_RIGHT);

		// set the rects
		m_canvas_rect.set(0, h, w, 0);
		m_canvas_padded_rect.set(PADDING_LEFT,
								PADDING_BOTTOM + m_usable_height,
								PADDING_LEFT + m_usable_width,
								PADDING_BOTTOM);

		// Tell the GraphLines and the GraphYAxii classes to
		// update their drawing coordinates.
		update_changeable_rects();

//		Log.v(tag, "onSizeChanged(): usable width = " + m_usable_width +
//				", usable height = " + m_usable_height);

	} // onSizeChanged(w, h, oldw, oldh)


	//----------------------------
	@Override
	protected void onDraw(Canvas canvas) {
		// Are we loading? Then just exit.
		if (GraphActivity.m_loading) {
			Log.v(tag, "onDraw: EXITING because we're loading");
			return;
		}


		//	testing!
		// This successfully draws a box at the outermost
		// pixle of the widget.
//		Log.d(tag, "onDraw(), m_clip_rect = " + m_canvas_padded_rect);
//		float old_stroke_width = m_paint.getStrokeWidth();
//		m_paint.setStrokeWidth(1);
//		int old_color = m_paint.getColor();

		// Full canvas = red
//		m_paint.setColor(Color.RED);
//		Log.d (tag, "red box (m_canvas_rect) = " + m_canvas_rect);
//		draw_box(canvas, m_canvas_rect, m_paint);

		// padded rectangle = yellow
//		m_paint.setColor(Color.YELLOW);
//		Log.d (tag, "yellow box (m_canvas_padded_rect) = " + m_canvas_padded_rect);
//		draw_box(canvas, m_canvas_padded_rect, m_paint);

		// Graph rectangle = cyan
//		m_paint.setColor(Color.CYAN);
//		Log.d (tag, "cyan box (m_graphline_rect) = " + m_graphline_rect);
//		draw_box(canvas, m_graphline_rect, m_paint);

		// x-axis = green
//		m_paint.setColor(Color.GREEN);
//		Log.d (tag, "green box (m_x_axis) = " + m_x_axis_rect);
//		draw_box(canvas, m_x_axis_rect, m_paint);

		// Not supposed to be valid.
//		m_paint.setColor(Color.MAGENTA);
//		Log.d (tag, "magenta box (m_y_axis_rect) = " + m_y_axis_rect);
//		draw_box(canvas, m_y_axis_rect, m_paint);

//		m_paint.setStrokeWidth(old_stroke_width);
//		m_paint.setColor(old_color);
		//
		// end test

		// Check to see if there's anything to draw.
		if (has_data() == false) {
			String msg = this.getContext().getString(R.string.graph_no_sets_msg);
			float temp_text_size = m_paint.getTextSize();
			m_paint.setTextSize(m_msg_text_size);
			m_paint.getTextBounds(msg, 0, msg.length(), m_temp_rect);
			draw_text(canvas, msg,
					m_canvas_padded_rect.exactCenterX() - m_temp_rect.exactCenterX(),
					m_canvas_padded_rect.exactCenterY() - m_temp_rect.exactCenterY(),
					m_paint);
			m_paint.setTextSize(temp_text_size);	// Return to default size
			return;		// get outta here!
		}

		// Draw a background for the GraphLines area.
//		m_paint.setColor(getResources().getColor(color.fainter_white));
//		draw_box(canvas, m_graphline_rect, m_paint);
//		draw_rect(canvas, m_graphline_rect, m_paint);

		// Reset the color
		m_paint.setColor(getResources().getColor(color.ghost_white));

		//	Draw the x-axii here
		if (m_graph_x_axis != null) {
			if (m_graph_x_axis.is_draw_area_set() == false) {
				Log.w(tag, "onDraw(): m_graph_x_axis.is_draw_area_set() is false! Setting the draw area to continue, sigh.");
				m_graph_x_axis.set_draw_area(m_x_axis_rect);
			}
			m_paint.setColor(getResources().getColor(color.ghost_white));
			m_graph_x_axis.draw(canvas, m_paint);
		}


		// Draw the y-axii before the lines (so the graph lines
		// cover the y-axii lines).
		m_paint.setAntiAlias(true);
		m_paint.setStrokeWidth(0);
		for (int i = 0; i < m_graphlist.size(); i++) {	// Need the count, sigh.
			// Draw the y-axis, but only if it's non-null.
			if (m_graphlist.get(i).m_y_axis_graph != null) {
				if (m_graphlist.get(i).m_y_axis_graph.m_draw_area == null) {
					Log.e(tag, "onDraw() has to find the y-axis area!");
					m_graphlist.get(i).m_y_axis_graph.m_draw_area = find_y_axis_area(i);
				}
				m_paint.setColor(m_graphlist.get(i).m_color);
				m_graphlist.get(i).m_y_axis_graph.draw(canvas, m_paint);
			}
		}


		// Draw all the lines!
		m_paint.setAntiAlias(true);
		m_paint.setStrokeWidth(LINE_STROKE_WIDTH);
		for (GraphCollection graph : m_graphlist) {
//			if (graph.m_line_graph.is_draw_area_set() == false) {
			if (graph.m_line_graph.get_view_rect() == null) {
				Log.e(tag, "onDraw(): a GraphLine draw area is not set! Setting the draw area to continue, sigh.");
//				graph.m_line_graph.set_draw_area(m_graphline_rect);	// Could slow down the drawing quite a bit
				graph.m_line_graph.set_view_rect(m_graphline_rect);
			}
			m_paint.setColor(graph.m_color);
			graph.m_line_graph.draw(canvas, m_paint);
		}


	} // onDraw(canvas)


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
		paint.setTextSize(m_msg_text_size);

		String msg = this.getContext()
			.getString(R.string.graph_all_invalid_msg);

		TextPaint tpaint = new TextPaint(paint);
		StaticLayout layout =
			new StaticLayout(msg, tpaint,
				(int) m_usable_width,
				Layout.Alignment.ALIGN_CENTER,
				1.1f, 0, true);

		// Translate this canvas according to the layout's width
		// and height
		float dx = (m_canvas_width - layout.getWidth()) / 2f;
		float dy = (m_usable_height - layout.getHeight()) / 2f;
		canvas.translate(dx, dy);
		layout.draw(canvas);
	} // draw_invalid_entries()


	/***************************
	 * Working on existing data members, this determines
	 * if there's any data to draw or not.
	 *
	 * @return	true iff there's something to draw!  False
	 * 			otherwise.
	 */
	private boolean has_data() {
		for (GraphCollection collection : m_graphlist) {
			if (collection.has_data()) {
				return true;
			}
		}
		return false;
	} // has_data()


	/******************************
	 * Updates all the rectangles that depend on the number of
	 * GraphLines. Tells all the items in m_graphlist to
	 * update their draw areas.
	 * <p>
	 * <b>preconditions</b>:<br/>
	 * 		<i>m_canvas_padded_rect</i> is properly set.<br/>
	 * <p>
	 * <b>side effects</b>:<br/>
	 * 	<i>m_graphline_rect</i> will reflect the current number
	 * 		of GraphLine instances.<br/>
	 * 	<i>m_graphlist</i>'s subsidiary elements, specifically
	 * 		their .m_line_graph and .m_y_axis_graph.
	 * 	<i>m_x_axis_rect</i> changed to fit just below m_graphline_rect.
	 */
	private void update_changeable_rects() {
		m_graphline_rect = find_GraphLine_area(m_graphlist.size());
		if (m_graphline_rect == null) {
			Log.e (tag, "update_changeable_rects() can't make m_graphline_rect!");
			return;
		}

		// Loop through all the graphs (include the y-axis, too!)
		for (int i = 0; i < m_graphlist.size(); i++) {
			// The GraphLine instance
//			m_graphlist.get(i).m_line_graph.set_draw_area(m_graphline_rect);
			m_graphlist.get(i).m_line_graph.set_view_rect(m_graphline_rect);
//			m_graphlist.get(i).m_line_graph.map_points();

			// The GrapyYAxis instance
			m_graphlist.get(i).m_y_axis_graph.m_draw_area = find_y_axis_area(i);
		}

		// Rests just below m_graphline_rect
//		m_x_axis_rect.set(m_graphline_rect);
		m_x_axis_rect.set((int)m_graphline_rect.left, (int)m_graphline_rect.top, (int)m_graphline_rect.right, (int)m_graphline_rect.bottom);
		m_x_axis_rect.bottom = 0;
		m_x_axis_rect.top = (int) m_graphline_rect.bottom;
		m_x_axis_rect.top -= X_AXIS_GAP;

		// Set the x-axis
		if (m_graph_x_axis != null) {
			m_graph_x_axis.set_draw_area(m_x_axis_rect);
			m_graph_x_axis.map_points();
		}

	} // update_changeable_rects()


	/*******************
	 * Figures out the draw area for a y-axis.
	 * <p>
	 * <b>preconditions</b>:<br/>
	 * 	<i>m_canvas_padded_rect</i> must be correctly set.
	 *
	 * @param count		Which y-axis we're drawing (they start
	 * 					at zero).
	 *
	 * @return	A RectF that defines the drawing area for this
	 * 			particular GraphYAxis instance.  Use this RectF
	 * 			for that instance's m_draw_area.
	 */
	protected RectF find_y_axis_area (int count) {
		RectF draw_area = new RectF (m_canvas_padded_rect);

		// The gap is the spacing in between the various
		// y-axii.
		draw_area.left += count * (DEFAULT_Y_AXIS_WIDTH + Y_AXIS_GAP);

		// Makes the right size = to left side of line draw area
		draw_area.right = draw_area.left + DEFAULT_Y_AXIS_WIDTH;

		return draw_area;
	} // find_y_axis_area (count)


	/*******************
	 * Figures out the draw area for all the line graphs.
	 * This is essentially the usable draw area minus the
	 * area used by the y-axii.
	 * <p>
	 * <b>preconditions</b>:<br/>
	 * 	<i>m_canvas_padded_rect</i> must be correctly set.
	 *
	 * @param num_y_axii		How many y-axii there are.
	 *
	 * @return	-A RectF that defines the drawing area for all
	 * 			the GraphLine instances.  Use this rectangle
	 * 			to set their draw areas.<br/>
	 * 			-Returns null on an error.
	 */
	protected RectF find_GraphLine_area (int num_y_axii) {
//		Log.d(tag, "find_GraphLine_area() called, num_y_axii = " + num_y_axii);
		RectF draw_area = new RectF (m_canvas_padded_rect);
		draw_area.left += num_y_axii * (DEFAULT_Y_AXIS_WIDTH + Y_AXIS_GAP);
		if (draw_area.left >= draw_area.right) {
			Log.e (tag, "Error in find_GraphLine_area()! Don't have enough room to draw all the y-axii!");
			return null;
		}
		return draw_area;
	} // find_line_area (count)


	/********************
	 * Changes the size of the text in the x and y axii labels.
	 *
	 * @param size	The font size to use.
	 */
	public void set_label_size(float size) {
		m_paint.setTextSize(size);
	}


	/*********************
	 * Returns the current text size of the x and y axii labels.
	 */
	public float get_label_size() {
		return m_paint.getTextSize();
	}


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
		update_changeable_rects();
		return m_graphlist.size();
	} // add_graph_collection (grapher)


	/********************
	 * Removes all the data from this widget.
	 */
	public void clear() {
		m_graphlist.clear();
	}

	/*********************
	 * Scales the graph by the specified amount.  Use this
	 * to zoom in/out of the graph AFTER all the data has
	 * been properly set up.
	 * <p>
	 * Because of how this graph works, the zoom will only be
	 * along the x-axis (no y-zooming).
	 * <p>
	 * <b>NOTE</b>:	The amount is RELATIVE!  That means that
	 * a scale amount of 0 will do nothing. Reset the graph by
	 * calling scale_reset().
	 *
	 * @param amount		Amount of pixels to increase/decrease
	 * 					the view by.
	 */
	public void scale (float amount) {
		for (GraphCollection graph : m_graphlist) {
			RectD window = graph.m_line_graph.get_world_rect();
			window.left -= amount / 2f;
			window.right += amount / 2f;
			graph.m_line_graph.set_world_rect(window);
		}
	} // scale (amount)

	/**********************
	 * Resets the scaling to the default.
	 *
	 * todo
	 * 		implement this
	 */
	public void scale_reset() {
		for (GraphCollection graph : m_graphlist) {
			// todo
		}
	} // scale_reset()


	/**********************
	 * Pans the graph left or right by the specified number
	 * of pixels.
	 * <p>
	 * <b>NOTE</b>:	This is a RELATIVE pan!  That means that
	 * 0 will do nothing.  Call pan_reset() to set to original
	 * settings.<br/>
	 * Positive numbers to
	 * pan right (as if you're dragging right), and use negative
	 * numbers to pan left.
	 *
	 * @param x		The pan amount in pixels. Positive numbers
	 * 				scroll in the positive X direction or right (as
	 * 				if you're dragging right). Negatives go left.
	 */
	public void pan (float x) {
		// todo
	} // scroll (x)

	/**********************
	 * Resets the scroll to the default (which should be centered).
	 */
	public void pan_reset() {
		// todo
	} // scroll_reset()
}
