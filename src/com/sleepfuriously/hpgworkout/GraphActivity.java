/**
 * This Activity displays a/some graph/s!
 *
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class GraphActivity
					extends
						BaseDialogActivity
					implements
						OnClickListener,
						OnTouchListener {

	//-------------------------
	//	Constants
	//-------------------------

	private static final String tag = "GraphActivity";

	/**
	 * The only information this needs, is the ORDER number
	 * of the exercise that the user selected.  We'll use
	 * that info to figure out which exercise it is and
	 * display that info.
	 */
	public static final String
			NAME_KEY = "name";

	/** Goes in-between items in the legend part of the Activity */
	protected static final String DEFAULT_LEGEND_SPACER = "   ";


	/**
	 * The minimum distance between the two fingers for a touch
	 * event to be used (we ignore distances smaller than this).
	 */
	public static final float MIN_PINCH_DISTANCE = 10f;


	//-------------------------
	//	Widgets
	//-------------------------

	/**
	 * Where the data is actually drawn.  This is a custom View.
	 */
	GView m_view;

	/** The button to take the user to an options dialog */
	Button m_options_butt;


	//-------------------------
	//	Data
	//-------------------------

	/**
	 * The name of this exercise.
	 */
	private String m_ex_name;

	/** Needed for the ASyncTask to properly access this context */
//	private static Context m_context;

	//--------
	//	The following items hold all the info associated
	//	with our exercise.
	//--------

	/** Holds all the info about this exercise */
	protected ExerciseData m_exercise_data = null;

	/** Holds all the set data from our database to be processed later. */
	protected ArrayList<SetData> m_set_data;

	/**
	 * This tells the Activity when it needs to load data.  It
	 * is set to true by OTHER Activities (ASet, EditSet) when
	 * the database is changed and the list of exercise sets
	 * has changed.
	 *
	 * When the Inspector has reloaded, m_changed is moved to
	 * false.
	 */
	public static boolean m_db_dirty = true;

	/**
	 * This is true while this activity is waiting for the
	 * data to be loaded.  Necessary so that the user doesn't
	 * get a flash of an empty graph while the database is
	 * accessed.
	 */
	public static boolean m_loading = false;


	//--------
	//	To handle Touch Events
	//--------

	/**
	 * The states that we can be in (in relation to what the
	 * fingers are doing).
	 */
	static final int
			NONE = 0,
			DRAG = 1,
			ZOOM = 2;

	/** Our current state */
	private int m_touch_mode = NONE;

	private PointD	m_touch_start = new PointD();

	private float m_old_touch_dist = 1f;

	/**
	 * The position of the last move event
	 */
	private PointD m_last_touch_pos = new PointD();

	/**
	 * The distance that the fingers are apart from the last
	 * zoom event.
	 */
	private float m_last_zoom_dist = 1f;


	//-------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String str;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.graphs);

		m_options_butt = (Button) findViewById(R.id.graph_options_butt);
		m_options_butt.setOnClickListener(this);

		// Init buttons and the main graph View
		m_view = (GView) findViewById(R.id.graph_view);
		int text_size = getResources().getDimensionPixelSize(R.dimen.font_size_very_small);
		m_view.set_label_size(text_size);
		m_view.setOnTouchListener(this);

		// Make sure we load the database each time onCreate() is called.
		m_db_dirty = true;

		//	Read in the data that was passed to this
		//	Activity.  It should tell us which exercise
		//	we are looking at.
		Intent itt = getIntent();
		m_ex_name = itt.getStringExtra(NAME_KEY);
		if (m_ex_name == null) {
			Log.e(tag, "Illegal name for the exercise in onCreate()!");
			Log.e(tag, "\tGraph creation aborted.");
			return;
		}


		// If we're displaying graphs in a Tab, then the title and
		// logo need to disappear.
		TextView title = (TextView) findViewById(R.id.graph_title_tv);
		ImageView logo = (ImageView) findViewById(R.id.graph_logo);
		if (ExerciseTabHostActivity.m_tab_active) {
			title.setVisibility(View.GONE);
			logo.setVisibility(View.GONE);
		}
		else {
			// Otherwise, set the title to our exercise and set the
			// help button.
			str = getString(R.string.graph_title_msg, m_ex_name);
			title.setText(str);
			logo.setOnClickListener(this);

			String user = DatabaseFilesHelper.get_active_username(this);
			String possessive = getString(R.string.possessive_suffix);
			title.setText(user + possessive + " " + m_ex_name);
		}

		


		// Continued in onResume()

	} // onCreate (.)


	//-------------------------
	@Override
	protected void onResume() {
		super.onResume();

		// Do we need to reload the database and redraw everything?
		if (m_db_dirty) {
			Log.v(tag, "Redrawing the graph!");
			setup_data();
			m_db_dirty = false;
		}

	} // onResume()


	//-------------------------
	@Override
	public void onClick(View v) {
		Intent itt;

		WGlobals.play_short_click();

		// Did they hit the help button?
		if (v.getId() == R.id.graph_logo) {
			show_help_dialog(R.string.graph_help_title, R.string.graph_help_msg);
		}

		else if (v == m_options_butt) {
			itt = new Intent(this, GraphOptionsActivity.class);

			// fill in the Intent
			itt.putExtra(GraphOptionsActivity.ITT_KEY_EXERCISE_NAME, m_exercise_data.name);

			itt.putExtra(GraphOptionsActivity.ITT_KEY_EXERCISE_SIGNIFICANT, m_exercise_data.significant);

			itt.putExtra(GraphOptionsActivity.ITT_KEY_ASPECT_REPS, m_exercise_data.breps);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_ASPECT_LEVEL, m_exercise_data.blevel);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_ASPECT_CALS, m_exercise_data.bcals);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_ASPECT_WEIGHT, m_exercise_data.bweight);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_ASPECT_DIST, m_exercise_data.bdist);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_ASPECT_TIME, m_exercise_data.btime);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_ASPECT_OTHER, m_exercise_data.bother);

			if (m_exercise_data.bother) {
				itt.putExtra(GraphOptionsActivity.ITT_KEY_GRAPH_OTHER_NAME, m_exercise_data.other_title);
			}

			itt.putExtra(GraphOptionsActivity.ITT_KEY_GRAPH_REPS, m_exercise_data.g_reps);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_GRAPH_LEVEL, m_exercise_data.g_level);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_GRAPH_CALS, m_exercise_data.g_cals);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_GRAPH_WEIGHT, m_exercise_data.g_weight);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_GRAPH_DIST, m_exercise_data.g_dist);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_GRAPH_TIME, m_exercise_data.g_time);
			itt.putExtra(GraphOptionsActivity.ITT_KEY_GRAPH_OTHER, m_exercise_data.g_other);

			itt.putExtra(GraphOptionsActivity.ITT_KEY_WITH_REPS, m_exercise_data.g_with_reps);


			startActivityForResult(itt, WGlobals.GRAPHOPTIONSACTIVITY);
		}
	} // onClick(v)


	//-------------------------
	@Override
	public void onBackPressed() {
		Log.d(tag, "entering onBackPressed()");
		if (ExerciseTabHostActivity.m_dirty)
			setResult(RESULT_OK);
		else
			setResult(RESULT_CANCELED);
		finish();
	}



	//-------------------------
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.d(tag, "entering onKeyUp()");

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (ExerciseTabHostActivity.m_dirty) {
				Log.d(tag, "ExerciseTabHostActivity.m_dirty is true");
				setResult(RESULT_OK);
			}
			else {
				Log.d(tag, "ExerciseTabHostActivity.m_dirty is false");
				setResult(RESULT_CANCELED);
			}
			finish();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	//-------------------------
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		Log.d(tag, "entering onKeyDown()");

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (ExerciseTabHostActivity.m_dirty) {
				tabbed_set_result(RESULT_OK);
			}
			else {
				tabbed_set_result(RESULT_CANCELED);
			}
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}


	//***********************
	//	All touch events will be for zoom and moving around
	//	of the GView.
	//
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v != m_view) {
			return true;			// Ignore. We only care about the GView
		}

		// Handle the touch events
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				m_touch_start.set(event.getX(), event.getY());
				m_last_touch_pos.set(event.getX(), event.getY());
				m_touch_mode = DRAG;
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				m_touch_mode = NONE;
				break;

			case MotionEvent.ACTION_POINTER_DOWN:
				m_old_touch_dist = get_double_touch_spacing(event);
				if (m_old_touch_dist > MIN_PINCH_DISTANCE) {
					//	begin (setup) a zoom event
					m_touch_mode = ZOOM;
					m_last_zoom_dist = get_double_touch_spacing(event);
				}
				break;

			case MotionEvent.ACTION_MOVE:
				// todo:
				//	Only pan if we're not at an end of the screen.

				if (m_touch_mode == DRAG) {
//					Log.d(tag, "DRAG event.  Pan amount = " + event.getX());
					// Only interested in left-right pans
					m_view.pan((float) (m_last_touch_pos.x - event.getX()));
					m_view.invalidate();
					m_last_touch_pos.set(event.getX(), event.getY());
				}

				else if (m_touch_mode == ZOOM) {
					float new_dist = get_double_touch_spacing(event);
					if (new_dist > MIN_PINCH_DISTANCE) {
						// send a zoom event to GView
						float amount = new_dist - m_last_zoom_dist;
						m_view.scale(amount);
						m_last_zoom_dist = new_dist;
						m_view.invalidate();
					}
				}
				break;
		}

		return true;		// event was handled
	} // onTouch (v, event)


	/**********************
	 * Euclidean[sic] distance to calculate spacing.
	 *
	 * Note: actually, it's the Pythagorean distance.
	 */
	float get_double_touch_spacing (MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}


	/**********************
	 * Find the midpoint between the two finger events.
	 *
	 * @param point		Holds the result.  Passing a param
	 * 					like this avoids allocating a new
	 * 					var and the garbage collection.
	 *
	 * @param event		The event to analyze.
	 */
	void touch_midpoint (PointD point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2f, y / 2f);
	}

	/**********************
	 * Show touch events in the logcat.
	 */
	private void touch_dump_event (MotionEvent event) {
		String names[] = {
						"DOWN",
						"UP",
						"MOVE",
						"CANCEL",
						"OUTSIDE",
						"POINTER_DOWN",
						"POINTER_UP",
						"7?",
						"8?",
						"9?"
		};

		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int action_code = action & MotionEvent.ACTION_MASK;

		sb.append("event ACTION_").append(names[action_code]);
		if ((action_code == MotionEvent.ACTION_POINTER_DOWN) ||
			(action_code == MotionEvent.ACTION_POINTER_UP)) {
			sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}

		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#").append(i);
			sb.append("(pid ").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX());
			sb.append(",").append((int) event.getY());
			if (i + 1 < event.getPointerCount()) {
				sb.append(";");
			}
		}

		sb.append("]");
		Log.d(tag, sb.toString());
	} // touch_dump_event (event)


	//***********************
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((requestCode == WGlobals.GRAPHOPTIONSACTIVITY) &&
			(resultCode == RESULT_OK)) {
			// Grab the info from our intent and update
			// our data (and possibly the database, too).
			m_exercise_data.g_reps = data.getBooleanExtra(GraphOptionsActivity.ITT_KEY_GRAPH_REPS, false);
			m_exercise_data.g_level = data.getBooleanExtra(GraphOptionsActivity.ITT_KEY_GRAPH_LEVEL, false);
			m_exercise_data.g_cals = data.getBooleanExtra(GraphOptionsActivity.ITT_KEY_GRAPH_CALS, false);
			m_exercise_data.g_weight = data.getBooleanExtra(GraphOptionsActivity.ITT_KEY_GRAPH_WEIGHT, false);
			m_exercise_data.g_dist = data.getBooleanExtra(GraphOptionsActivity.ITT_KEY_GRAPH_DIST, false);
			m_exercise_data.g_time = data.getBooleanExtra(GraphOptionsActivity.ITT_KEY_GRAPH_TIME, false);
			m_exercise_data.g_other = data.getBooleanExtra(GraphOptionsActivity.ITT_KEY_GRAPH_OTHER, false);

			m_exercise_data.g_with_reps = data.getIntExtra(GraphOptionsActivity.ITT_KEY_WITH_REPS, -1);

			// Check for error condition of NOTHING being
			// graphed.  If so, err report and set to the
			// significant.
			test_and_fix_graph_settings();

			// Change the database
			save_data();

			m_db_dirty = true;
			onResume();

		} // return from GraphOptionsActivity

	} // onActivityResult(...)


	/************************
	 * Creates the legend that helps the user understand
	 * what the colors of the graph mean.
	 */
	private void construct_legend() {
		String str = null;
//		int len, start, end;
//		SpannableString spannable;

		TextView tv = (TextView) findViewById(R.id.graph_description_tv);

		// This time, let's try a SpannableStringBuilder
		StyleableSpannableStringBuilder builder =
				new StyleableSpannableStringBuilder();

		boolean needs_seperator = false;
		if (m_exercise_data.g_reps) {
			str = getString(R.string.reps_readable);
			builder.appendWithForegroundColor(str, getResources().getColor(R.color.color_reps),
											m_exercise_data.is_reps_significant());
			needs_seperator = true;
		}
		if (m_exercise_data.g_level) {
			if (needs_seperator)
				builder.append (DEFAULT_LEGEND_SPACER);
			str = getString(R.string.level_readable);
			builder.appendWithForegroundColor(str, getResources().getColor(R.color.color_level),
											m_exercise_data.is_level_significant());
			needs_seperator = true;
		}
		if (m_exercise_data.g_cals) {
			if (needs_seperator)
				builder.append (DEFAULT_LEGEND_SPACER);
			str = getString(R.string.cals_readable);
			builder.appendWithForegroundColor(str, getResources().getColor(R.color.color_cals),
											m_exercise_data.is_cals_significant());
			needs_seperator = true;
		}
		if (m_exercise_data.g_weight) {
			if (needs_seperator)
				builder.append (DEFAULT_LEGEND_SPACER);
			str = getString(R.string.weight_readable);
			builder.appendWithForegroundColor(str, getResources().getColor(R.color.color_weight),
											m_exercise_data.is_weight_significant());
			needs_seperator = true;
		}

		if (m_exercise_data.g_dist) {
			if (needs_seperator)
				builder.append (DEFAULT_LEGEND_SPACER);
			str = getString(R.string.dist_readable);
			builder.appendWithForegroundColor(str, getResources().getColor(R.color.color_dist),
											m_exercise_data.is_dist_significant());
			needs_seperator = true;
		}

		if (m_exercise_data.g_time) {
			if (needs_seperator)
				builder.append (DEFAULT_LEGEND_SPACER);
			str = getString(R.string.time_readable);
			builder.appendWithForegroundColor(str, getResources().getColor(R.color.color_time),
											m_exercise_data.is_time_significant());
			needs_seperator = true;
		}

		if (m_exercise_data.g_other) {
			if (needs_seperator)
				builder.append (DEFAULT_LEGEND_SPACER);
			str = m_exercise_data.other_title;
			builder.appendWithForegroundColor(str, getResources().getColor(R.color.color_other),
											m_exercise_data.is_other_significant());
			needs_seperator = true;
		}

		if (m_exercise_data.g_with_reps != -1) {
			if (needs_seperator)
				builder.append (DEFAULT_LEGEND_SPACER);

			// What is combined with reps?
			String with_name;
			if (m_exercise_data.g_with_reps == DatabaseHelper.EXERCISE_COL_OTHER_NUM) {
				with_name = m_exercise_data.other_title;
			}
			else {
				with_name = get_nice_string_from_aspect_num(this, m_exercise_data.g_with_reps);
			}
			str = getString(R.string.with_readable, with_name);
			builder.appendWithForegroundColor(str, getResources().getColor(R.color.color_with_reps),
											false);
			needs_seperator = true;
		}

		// Finally!
		tv.setText(builder);

	} // construct_legend()


	/************************
	 * Builds the list of GraphCollections for this
	 * Activity.
	 * <p>
	 *  preconditions:<br/>
	 *		<i>m_exercise_data</i> is fully loaded.<br/>
	 *		<i>m_set_data</i> is also fully loaded.
	 * <p>
	 *  side effects:<br/>
	 *  		<i>m_view</i>, our GView will have a collection
	 *  		added to it for every aspect that is used for this
	 *  		exercise.
	 *
	 */
	protected void construct_collections_for_aspects() {
		float radius = Graph2.BIG_DOT_RADIUS;

		if (m_exercise_data.g_reps) {
			add_new_collection(DatabaseHelper.EXERCISE_COL_REP_NUM,
							getResources().getColor(R.color.color_reps),
							radius);
			radius -= Graph2.DOT_RADIUS_INCREMENT;
		}

		if (m_exercise_data.g_cals) {
			add_new_collection(DatabaseHelper.EXERCISE_COL_CALORIE_NUM,
							getResources().getColor(R.color.color_cals),
							radius);
			radius -= Graph2.DOT_RADIUS_INCREMENT;
		}

		if (m_exercise_data.g_level) {
			add_new_collection(DatabaseHelper.EXERCISE_COL_LEVEL_NUM,
							getResources().getColor(R.color.color_level),
							radius);
			radius -= Graph2.DOT_RADIUS_INCREMENT;
		}

		if (m_exercise_data.g_weight) {
			add_new_collection(DatabaseHelper.EXERCISE_COL_WEIGHT_NUM,
							getResources().getColor(R.color.color_weight),
							radius);
			radius -= Graph2.DOT_RADIUS_INCREMENT;
		}

		if (m_exercise_data.g_dist) {
			add_new_collection(DatabaseHelper.EXERCISE_COL_DIST_NUM,
							getResources().getColor(R.color.color_dist),
							radius);
			radius -= Graph2.DOT_RADIUS_INCREMENT;
		}

		if (m_exercise_data.g_time) {
			add_new_collection(DatabaseHelper.EXERCISE_COL_TIME_NUM,
							getResources().getColor(R.color.color_time),
							radius);
			radius -= Graph2.DOT_RADIUS_INCREMENT;
		}

		if (m_exercise_data.g_other) {
			add_new_collection(DatabaseHelper.EXERCISE_COL_OTHER_NUM,
							getResources().getColor(R.color.color_other),
							radius);
			radius -= Graph2.DOT_RADIUS_INCREMENT;
		}
	} // construct_collections_for_aspects()


	/************************
	 * If the "with reps" portion needs to be graphed,
	 * then this is where it's set up.  Oh yeah, this
	 * also checks first to see if it needs to do anything
	 * at all, which is kind of nice.
	 * <p>
	 * <b>preconditions</b>:<br/>
	 * 		All the data from the DB is loaded
	 * 		and ready to poked and prodded.
	 * <p>
	 * <b>side effects</b>:<br/>
	 * 		Our GView will have another GraphCollection
	 * 		added (but only if there's a with_reps aspect
	 * 		to graph).
	 */
	private void construct_with_reps() {
		if ((m_exercise_data.g_with_reps == -1) ||
			(m_exercise_data.breps == false)) {
			return;
		}

		// Find the reps data and put it in a GraphCollection.
		GraphCollection collection = new GraphCollection();
		collection.m_line_graph = new Graph2();

		RectD bounds = new RectD(Double.MAX_VALUE, -Double.MAX_VALUE,
								-Double.MAX_VALUE, Double.MAX_VALUE);

		for (SetData set_data : m_set_data) {
			PointD pt = new PointD();

			pt.x = set_data.millis;
			if (pt.x < bounds.left)
				bounds.left = pt.x;
			if (pt.x > bounds.right)
				bounds.right = pt.x;


			switch (m_exercise_data.g_with_reps) {
				case DatabaseHelper.EXERCISE_COL_REP_NUM:
					Log.e(tag, "Error in setup_with_reps()! Tried to combine reps with itself!  Aborting.");
					return;

				case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
					pt.y = set_data.levels;
					break;
				case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
					pt.y = set_data.cals;
					break;
				case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
					pt.y = set_data.weight;
					break;
				case DatabaseHelper.EXERCISE_COL_DIST_NUM:
					pt.y = set_data.dist;
					break;
				case DatabaseHelper.EXERCISE_COL_TIME_NUM:
					pt.y = set_data.time;
					break;
				case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
					pt.y = set_data.other;
					break;
				default:
					Log.e (tag, "add_new_collection() cannot recognize the aspect!");
					break;
			}

			// Now multiply by the number of reps and find
			// the bounds.  BUT! Only do this if pt.y is
			// *not* -1 (which indicates that the value is
			// invalid).
			if ((pt.y != -1) && (set_data.reps != -1)) {
				pt.y *= set_data.reps;

				if (pt.y < bounds.bottom)
					bounds.bottom = pt.y;
				if (pt.y > bounds.top)
					bounds.top = pt.y;
			}

			collection.m_line_graph.add_world_pt(pt);
		}


		// HACK!  The GraphLine and the GraphYAxis classes hate
		// it when the boundaries are the same.  Test and fix
		// those situations here.
		if (bounds.left == bounds.right) {
			bounds.left -= 2;
			bounds.right += 2;
		}
		if (bounds.top == bounds.bottom) {
			bounds.top += 2;
			bounds.bottom -= 2;
		}

		collection.m_line_graph.set_world_rect(bounds);

		collection.m_id = DatabaseHelper.EXERCISE_COL_REP_NUM;	// Using this for ID. Convenient and unique.
		collection.m_color = getResources().getColor(R.color.color_with_reps);

		// Do the y-axis that's attached to this graph line.
		collection.m_y_axis_graph = new GraphYAxis(bounds.bottom, bounds.top);

		// Now tell the line_graph to modify its bounding rectangle
		// to match the one in the y_axis_graph.
		// TODO:
		//		Move things around so that we set the y-axis stuff
		//		BEFORE the GraphLine--that way we don't do things
		//		twice as we do here.
		RectD modified_rect = new RectD(bounds);
		modified_rect.bottom = collection.m_y_axis_graph.get_min();
		modified_rect.top = collection.m_y_axis_graph.get_max();
		collection.m_line_graph.set_world_rect(modified_rect);

		m_view.add_graph_collection(collection);
	} // setup_with_reps()


	/***********************
	 * Sets up the X-axis for the graph. This comprises
	 * mostly of dates for the graph.
	 * <p>
	 *  preconditions:<br/>
	 *		<i>m_set_data</i> is fully loaded.
	 * <p>
	 *  side effects:<br/>
	 *  		<i>m_view</i> will have a GraphXAxis class
	 *  			created for it.
	 */
	protected void construct_x_axis() {
//		m_view.m_graph_x_axis = new GraphXAxis();
		m_view.m_graph_x_axis = new GraphXAxis2();
		long left = Long.MAX_VALUE, right = -Long.MAX_VALUE;

		for (SetData set_data : m_set_data) {
			MyCalendar cal = new MyCalendar(set_data.millis);
			String str = cal.print_month_day_numbers();
			m_view.m_graph_x_axis.add_date(set_data.millis);
			m_view.m_graph_x_axis.add_label(str);
			if (set_data.millis < left)
				left = set_data.millis;
			if (set_data.millis > right)
				right = set_data.millis;
		}

		// Do we need to check to see if left = right?
		// Nope, it should be taken care of via construct_one_set().
//		m_view.m_graph_x_axis.set_bounds(left, right);
		m_view.m_graph_x_axis.set_date_window(left, right);

	} // construct_x_axis()


	/************************
	 * This is called when it's discovered that there's only
	 * one set to display.  This removes the GView and replaces
	 * it with a nice TextView that will display all our info.
	 * <p>
	 * preconditions:<br/>
	 * 	<i>m_set_data</i> is properly set with only ONE set.
	 */
	protected void construct_one_set() {
		// First, turn off the GView and turn on the TextView.
		m_view.setVisibility(View.GONE);

		ScrollView sv = (ScrollView) findViewById(R.id.graph_gview_sv);
		sv.setVisibility(View.VISIBLE);

		TextView tv = (TextView) findViewById(R.id.graph_gview_tv);
		tv.setVisibility(View.VISIBLE);

		// While we're at it, set the legend to invisible.
		TextView legend = (TextView) findViewById(R.id.graph_description_tv);
		legend.setVisibility(View.INVISIBLE);

		// And you know what? the options doesn't make sense either.
		m_options_butt.setVisibility(View.GONE);

		// Fill in the text.
		String str = getString(R.string.graph_one_set_msg, m_exercise_data.name);

		// Append to the string, depending on the current aspects.
		if (m_exercise_data.breps)
			str += "\n\treps: " + set_data_to_str(m_set_data.get(0).reps);
		if (m_exercise_data.blevel)
			str += "\n\tlevels: " + set_data_to_str(m_set_data.get(0).levels);
		if (m_exercise_data.bcals)
			str += "\n\tcalories: " + set_data_to_str(m_set_data.get(0).cals);
		if (m_exercise_data.bweight)
			str += "\n\tweight (" + m_exercise_data.weight_unit + "): " + set_data_to_str(m_set_data.get(0).weight);
		if (m_exercise_data.bdist)
			str += "\n\tdistance (" + m_exercise_data.dist_unit + "): " + set_data_to_str(m_set_data.get(0).dist);
		if (m_exercise_data.btime)
			str += "\n\ttime (" + m_exercise_data.time_unit + "): " + set_data_to_str(m_set_data.get(0).time);
		if (m_exercise_data.bother)
			str += "\n\t" + m_exercise_data.other_title + " (" + m_exercise_data.other_unit + "): " + set_data_to_str(m_set_data.get(0).other);
		switch (m_set_data.get(0).cond) {
			case DatabaseHelper.SET_COND_OK:
				str += "\n\tcondition: OK";
				break;
			case DatabaseHelper.SET_COND_MINUS:
				str += "\n\tcondition: Too Hard";
				break;
			case DatabaseHelper.SET_COND_PLUS:
				str += "\n\tcondition: Too Easy";
				break;
			case DatabaseHelper.SET_COND_INJURY:
				str += "\n\tcondition: Injury";
				break;
			case DatabaseHelper.SET_COND_NONE:
				break;
			default:
				Log.e (tag, "Illegal condition in construct_one_set()!");
				str += "\n\tcondition: unknown";
				break;
		}
		String notes = m_set_data.get(0).notes;
		if ((notes != null) && (notes.length() > 0)) {
			str += "\n\n\t" + notes;
		}

		tv.setText(str);
	} // construct_one_set()

	/************************
	 * Helper method that takes quantity in some set data
	 * and spits out a string for that number.  This is needed
	 * since a value of -1 mean NULL, not -1.
	 *
	 * @param num	The set data to convert to a string.
	 *
	 * @return	A printable string.
	 */
	private String set_data_to_str (int num) {
		if (num == -1) {
			return getString(R.string.graph_null);
		}
		return "" + num;
	}
	/**********************
	 * Float version.
	 * @see #set_data_to_str(int)
	 */
	private String set_data_to_str (float num) {
		if (num == -1) {
			return getString(R.string.graph_null);
		}
		return "" + num;
	}


	/************************
	 * Used during onCreate() and onResume(), this does two things:
	 * 	1.	Creates the ArrayList to hold the graph data
	 * 	2.	Starts the ASyncTask to read that data and
	 * 		draw the graph.
	 */
	private void setup_data() {
		// Set up this data list!
		if (m_set_data != null) {
			m_set_data.clear();
			m_set_data = null;
		}
		m_set_data = new ArrayList<SetData>();
		m_set_data.clear();

		// Start the AsyncTask.
		new GraphSyncTask().execute();

	} // setup_graph()


	/************************
	 * A nice thing to do before trying to access the database.
	 * This first tests to make sure that another thread is not
	 * using it.  But if it IS, then this waits a second before
	 * throwing an exception.
	 *
	 * NOTE:
	 * 		This needs to be called within a TRY, as this
	 * 		throws a SQLiteException!
	 *
	 * NOTE 2:
	 * 		This should probably be called during an ASyncTask, as
	 * 		it could take a long time!
	 */
	private void test_m_db() {
		if (m_db != null) {
			// The database may be used by another tab.  Give
			// it some time to finish.
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (m_db != null)
				throw new SQLiteException("m_db not null when starting doInBackground() in GraphActivity!");
		}
	} // test_m_db()


	/************************
	 * Goes through m_set_data and extracts all the data associated
	 * with a particular aspect.  This is then loaded up into a
	 * GraphCollection and added to our GView (m_view).
	 *<p>
	 * <b>preconditions</b>:<br/>
	 * 	<i>m_set_data</i>	Loaded up with all our data from the async
	 * 						task.
	 *<p>
	 * <b>side effect</b>:<br/>
	 * 	<i>m_view</i>		Will have a GraphCollection added to it.
	 *
	 * @param aspect		The aspect (identified by the
	 * 					DatabaseHelper.EXERCISE_COL_???_NUM)
	 * 					that this GraphCollection represents.
	 *
	 * @param color		The color to assign for this aspect.
	 *
	 * @param radius		The radius of the dots to draw for this
	 * 					graph's points.  They should be increasing
	 * 					in size so that the last ones drawn don't
	 * 					obscure the earlier ones.
	 */
	protected void add_new_collection(int aspect, int color, float radius) {

		// Find the reps data and put it in a GraphCollection.
		GraphCollection collection = new GraphCollection();
		collection.m_line_graph = new Graph2();

		RectD bounds = new RectD(Double.MAX_VALUE, -Double.MAX_VALUE,
								-Double.MAX_VALUE, Double.MAX_VALUE);

		for (SetData set_data : m_set_data) {
			PointD pt = new PointD();

			pt.x = set_data.millis;
			if (pt.x < bounds.left)
				bounds.left = pt.x;
			if (pt.x > bounds.right)
				bounds.right = pt.x;

			switch (aspect) {
				case DatabaseHelper.EXERCISE_COL_REP_NUM:
					pt.y = set_data.reps;
					break;
				case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
					pt.y = set_data.levels;
					break;
				case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
					pt.y = set_data.cals;
					break;
				case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
					pt.y = set_data.weight;
					break;
				case DatabaseHelper.EXERCISE_COL_DIST_NUM:
					pt.y = set_data.dist;
					break;
				case DatabaseHelper.EXERCISE_COL_TIME_NUM:
					pt.y = set_data.time;
					break;
				case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
					pt.y = set_data.other;
					break;
				default:
					Log.e (tag, "add_new_collection() cannot recognize the aspect!");
					break;
			}

			// Adjust our logical left/right boundary.  But only
			// if the value is not -1 (which indicates an invalid
			// entry).
			if (pt.y != -1) {
				if (pt.y < bounds.bottom)
					bounds.bottom = pt.y;
				if (pt.y > bounds.top)
					bounds.top = pt.y;
			}

			collection.m_line_graph.add_world_pt(pt);
		}

		// HACK!  The GraphLine and the GraphYAxis classes hate
		// it when the boundaries are the same.  Test and fix
		// those situations here.
		if (bounds.left == bounds.right) {
			bounds.left -= 2;
			bounds.right += 2;
		}
		if (bounds.top == bounds.bottom) {
			bounds.top += 2;
			bounds.bottom -= 2;
		}

		collection.m_line_graph.set_world_rect(bounds);
		collection.m_id = DatabaseHelper.EXERCISE_COL_REP_NUM;	// Using this for ID. Convenient and unique.
		collection.m_color = color;
		collection.m_line_graph.m_dot_radius = radius;

		// Do the y-axis that's attached to this graph line.
		collection.m_y_axis_graph = new GraphYAxis(bounds.bottom, bounds.top);

		// Now tell the line_graph to modify its bounding rectangle
		// to match the one in the y_axis_graph.
		// TODO:
		//		Move things around so that we set the y-axis stuff
		//		BEFORE the GraphLine--that way we don't do things
		//		twice as we do here.
		RectD modified_rect = new RectD(bounds);
		modified_rect.bottom = collection.m_y_axis_graph.get_min();
		modified_rect.top = collection.m_y_axis_graph.get_max();
		collection.m_line_graph.set_world_rect(modified_rect);

		m_view.add_graph_collection(collection);
	} // add_new_collection (aspect)


	/**************
	 * This tests the current settings
	 *
	 * preconditions:
	 * 	m_exercise_data		Should be filled out (either correctly
	 * 						or incorrectly).
	 *
	 * side effects:
	 * 	m_exercise_data		If all the graphs are turned off, this
	 * 						fixes that problem by turning the
	 * 						significant graph on.
	 *
	 * @return	true iff no problems were found. m_exercise_data
	 * 			will be unchanged.
	 */
	private boolean test_and_fix_graph_settings() {
		if ((m_exercise_data.g_reps) ||
			(m_exercise_data.g_level) ||
			(m_exercise_data.g_cals) ||
			(m_exercise_data.g_weight) ||
			(m_exercise_data.bdist) ||
			(m_exercise_data.g_time) ||
			(m_exercise_data.g_other)) {
			return true;
		}

		// Figure out the significant and turn it on.
		switch (m_exercise_data.significant) {
			case DatabaseHelper.EXERCISE_COL_GRAPH_REPS_NUM:
				m_exercise_data.g_reps = true;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_LEVEL_NUM:
				m_exercise_data.g_level = true;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_CALS_NUM:
				m_exercise_data.g_cals = true;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_WEIGHT_NUM:
				m_exercise_data.g_weight = true;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_DIST_NUM:
				m_exercise_data.g_dist = true;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_TIME_NUM:
				m_exercise_data.g_time = true;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_OTHER_NUM:
				m_exercise_data.g_other = true;
				break;
			default:
				Log.e (tag, "Can't find the significant aspect of the " + m_exercise_data.name + " exercise in test_and_fix_graph_settings()!");
				break;
		}
		return false;
	} // test_and_fix_graph_settings();


	/*************
	 * Saves the data from the widgets into our database.
	 *
	 * preconditions:
	 * 		Everything has been checked and is
	 * 		ready to go.
	 */
	private void save_data() {
		if (m_db != null) {
			Log.e (tag, "Trying to save_data(), but m_db is already being used! Aborting!");
			return;
		}

		try {
			m_db = WGlobals.g_db_helper.getWritableDatabase();

			// The data has already been loaded, so remove that
			// row and add in the modified row.
			if (m_db.delete(DatabaseHelper.EXERCISE_TABLE_NAME,
							DatabaseHelper.EXERCISE_COL_NAME + "=?",
							new String[] {m_exercise_data.name})
					== 0) {
				Log.e(tag, "Error deleting row in save_data()!");
				return;
			}

			ContentValues values = new ContentValues();
			values.put (DatabaseHelper.EXERCISE_COL_NAME, m_exercise_data.name);
			values.put (DatabaseHelper.EXERCISE_COL_TYPE, m_exercise_data.type);
			values.put (DatabaseHelper.EXERCISE_COL_GROUP, m_exercise_data.group);
			values.put (DatabaseHelper.EXERCISE_COL_WEIGHT, m_exercise_data.bweight);
			values.put (DatabaseHelper.EXERCISE_COL_REP, m_exercise_data.breps);
			values.put (DatabaseHelper.EXERCISE_COL_DIST, m_exercise_data.bdist);
			values.put (DatabaseHelper.EXERCISE_COL_TIME, m_exercise_data.btime);
			values.put (DatabaseHelper.EXERCISE_COL_LEVEL, m_exercise_data.blevel);
			values.put (DatabaseHelper.EXERCISE_COL_CALORIES, m_exercise_data.bcals);
			values.put (DatabaseHelper.EXERCISE_COL_OTHER, m_exercise_data.bother);

			values.put (DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT, m_exercise_data.weight_unit);
			values.put (DatabaseHelper.EXERCISE_COL_DIST_UNIT, m_exercise_data.dist_unit);
			values.put (DatabaseHelper.EXERCISE_COL_TIME_UNIT,  m_exercise_data.time_unit);
			values.put (DatabaseHelper.EXERCISE_COL_OTHER_TITLE, m_exercise_data.other_title);
			values.put (DatabaseHelper.EXERCISE_COL_OTHER_UNIT, m_exercise_data.other_unit);

			values.put(DatabaseHelper.EXERCISE_COL_SIGNIFICANT, m_exercise_data.significant);
			values.put(DatabaseHelper.EXERCISE_COL_LORDER, m_exercise_data.lorder);

			values.put(DatabaseHelper.EXERCISE_COL_GRAPH_REPS, m_exercise_data.g_reps);
			values.put(DatabaseHelper.EXERCISE_COL_GRAPH_LEVEL, m_exercise_data.g_level);
			values.put(DatabaseHelper.EXERCISE_COL_GRAPH_CALS, m_exercise_data.g_cals);
			values.put(DatabaseHelper.EXERCISE_COL_GRAPH_WEIGHT, m_exercise_data.g_weight);
			values.put(DatabaseHelper.EXERCISE_COL_GRAPH_DIST, m_exercise_data.g_dist);
			values.put(DatabaseHelper.EXERCISE_COL_GRAPH_TIME, m_exercise_data.g_time);
			values.put(DatabaseHelper.EXERCISE_COL_GRAPH_OTHER, m_exercise_data.g_other);

			values.put(DatabaseHelper.EXERCISE_COL_GRAPH_WITH_REPS, m_exercise_data.g_with_reps);

			m_db.insert(DatabaseHelper.EXERCISE_TABLE_NAME, null, values);

		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (m_db != null) {
				m_db.close();
				m_db = null;
			}
		}

	} // save_data()


	/*************
	 * Given the number of an aspect, this returns a nice
	 * string for that number.  Will work for the graphical
	 * aspects as well as the regular ones.
	 * <p>
	 * NOTE:
	 *		This is different from a similar-sounding method
	 *		in DatabaseActivity! This version gets a string
	 *		that's designed to be human-readable, whereas
	 *		the other version returns the actual database
	 *		column name.
	 *
	 * @param ctx	The context.  Needed as this is a static method.
	 *
	 * @param num	The number of the aspect (as specified
	 * 				in the 'significant' portion of ExerciseData).
	 *
	 * @return	A string represent that aspect that's human
	 * 			readable (from strings.xml in the general section).<br/>
	 * 			null on error.
	 */
	public static String get_nice_string_from_aspect_num (Context ctx, int num) {
		switch (num) {
			case DatabaseHelper.EXERCISE_COL_REP_NUM:
			case DatabaseHelper.EXERCISE_COL_GRAPH_REPS_NUM:
				return ctx.getString(R.string.reps_readable);
			case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
			case DatabaseHelper.EXERCISE_COL_GRAPH_LEVEL_NUM:
				return ctx.getString(R.string.level_readable);
			case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
			case DatabaseHelper.EXERCISE_COL_GRAPH_CALS_NUM:
				return ctx.getString(R.string.cals_readable);
			case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
			case DatabaseHelper.EXERCISE_COL_GRAPH_WEIGHT_NUM:
				return ctx.getString(R.string.weight_readable);
			case DatabaseHelper.EXERCISE_COL_DIST_NUM:
			case DatabaseHelper.EXERCISE_COL_GRAPH_DIST_NUM:
				return ctx.getString(R.string.dist_readable);
			case DatabaseHelper.EXERCISE_COL_TIME_NUM:
			case DatabaseHelper.EXERCISE_COL_GRAPH_TIME_NUM:
				return ctx.getString(R.string.time_readable);
			case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
			case DatabaseHelper.EXERCISE_COL_GRAPH_OTHER_NUM:
				Log.w(tag, "Do not call get_nice_string_from_aspect_num() for 'other'! Use the other's title instead.");
				return ctx.getString(R.string.other_readable);
		}
		Log.e(tag, "Illegal value in get_nice_string_from_aspect_num (" + num + ")!");
		return null;
	} // get_nice_string_from_aspect_num (num);



	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//	The Three Types:
	//		Params		- The info sent to the task when
	//					executed.  Not used here.
	//
	//		Progress		- Info that's passed to onProgressUpdate().
	//					It's everything that is needed to draw
	//					a row.
	//
	//		Result		- The type of the final result.  Also
	//					not used here.
	class GraphSyncTask extends AsyncTask <Void, SetData, Void> {

		/***************
		 * Called BEFORE the doInBackground(), this allows
		 * something to be done in the UI thread in prepara-
		 * tion for the long stuff, like starting a progress
		 * dialog.
		 */
		@Override
		protected void onPreExecute() {
			m_loading = true;
			start_progress_dialog(R.string.loading_str);
		}


		/***************
		 * This is the only method here that does NOT
		 * work in the UI thread.
		 *
		 * Calling publishProgress() forces onProgressUpdate()
		 * to be called (with the parameters that are passed).
		 */
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				test_m_db();
				m_db = WGlobals.g_db_helper.getReadableDatabase();
				m_exercise_data = DatabaseHelper.getExerciseData(m_db, m_ex_name);
				if (m_exercise_data == null) {
					Log.e(tag, "Error reading exercise info in doInBackground()! Aborting!");
					return null;
				}

				Cursor set_cursor = null;

				// Read in all the sets and store that information
				// in a list.
				try {
					// LOAD FROM DATABASE
					set_cursor = DatabaseHelper.getAllSets(m_db, m_ex_name, true);

					// If there are not enough sets, don't do anything.
					if (set_cursor.getCount() < 1) {
						Log.v(tag, "Not enough exercise sets to graph.");
						return null;
					}

					// MAIN LOOP:
					// Load up the exercise sets one by one into our list.
					while (set_cursor.moveToNext()) {
						SetData new_set_data = DatabaseHelper.getSetData(set_cursor);
						m_set_data.add(new_set_data);
						// todo
						//	Here is where we would publish our progress.
					}

				} // try reading the SET table

				catch (SQLException e) {
					e.printStackTrace();
				}
				finally {
					if (set_cursor != null) {
						set_cursor.close();
						set_cursor = null;
					}

				}

			} // try opening the DB

			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				if (m_db != null) {
					m_db.close();
					m_db = null;
				}
			}

			return null;
		} // doInBackground (...)

		/****************
		 * THIS is the part that has access to the UI
		 * thread!!!  Yay, I can do stuff here!
		 *
		 * @param data	This is the data gleaned from one
		 * 				workout set.  Use it to make the
		 * 				graph!
		 */
		@Override
		protected void onProgressUpdate(SetData ... set_data) {
			// todo:
			//	draw the points represented by this
//			m_view.add_point(data[0]);
		} // onProgressUpdate (...exercise_data)


		/*****************
		 * Called after doInBackground() has finished.
		 * Yup, you can do some more UI stuff here.
		 *
		 * I think this is an excellent place to dismiss
		 * the progress dialog.
		 */
		@Override
		protected void onPostExecute(Void result) {

			// If there's just one set, do something special
			if (m_set_data.size() == 1) {
				construct_one_set();
				m_view.invalidate();		// Necessary to make sure that
										// it's drawn AFTER all the db
										// stuff happens.
				m_loading = false;
				stop_progress_dialog();
				return;
			}

			construct_legend();

			m_view.clear();

			// If the aspect count is 0 (and there's no other graphs),
			// then there's been an error or we started with a database that wasn't
			// properly set.  So turn on the significant aspect.
			if ((ExerciseData.count_valid_graph_aspects(m_exercise_data) == 0) &&
				(m_exercise_data.g_with_reps == -1)) {
				Log.w(tag, "onPostExecute(), all the graphs are turned off! Turning on the most significant aspect...");
				int graph_aspect_num = ExerciseData.get_graph_aspect(m_exercise_data.significant);
				m_exercise_data.set_aspect_by_num(graph_aspect_num, true);
			}

			// If there is only one aspect possible, disable the
			// options button as it no longer makes sense.
			if (ExerciseData.count_valid_aspects(m_exercise_data) == 1) {
				m_options_butt.setVisibility(View.GONE);
			}

			// The main constructors...
			construct_collections_for_aspects();
			construct_with_reps();
			construct_x_axis();

			stop_progress_dialog();
			m_view.invalidate();		// Necessary to make sure that
									// it's drawn AFTER all the db
									// stuff happens.
			m_loading = false;
		} // onPostExecute( result )

	} // class GraphSyncTask

}
