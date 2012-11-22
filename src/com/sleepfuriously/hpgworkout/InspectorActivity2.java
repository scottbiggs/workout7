/**
 * This is a re-make of the InspectorActivity.  I've made some
 * MAJOR changes, so I decided to rename the damn thing.  They
 * consist of:
 * 		- Database stuff uses its own thread
 * 		- Works well in a tab
 * 		- Significantly cleaned up
 */
package com.sleepfuriously.hpgworkout;

import java.text.DecimalFormat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class InspectorActivity2
				extends BaseDialogActivity
				implements
						OnLongClickListener {

	//------------------
	//	Constants
	//------------------

	private static final String tag = "InspectorActivity";

	/** The id for the 'brief' menu selection */
//	protected static final int MENU_ID_BRIEF = 1;
	/** Id for the menu item to change the order that sets are listed */
	protected static final int MENU_ID_ORDER = 2;

	//------------------
	//	Widgets
	//------------------

	LinearLayout m_main_ll;

	ScrollView m_sv;

	TextView m_desc_tv;


	//------------------
	//	Data
	//------------------

	/**
	 * The id for the exercise set to center on.  This is only assigned
	 * during onCreate().  If this is a valid id number, then the
	 * set with the given id should be scrolled to when this Activity
	 * pops up the first time.
	 */
	private int m_set_id = -1;


	//--------------------
	//	All from the exercise database
	//--------------------
	/** Holds all info about this exercise. */
	protected ExerciseData m_ex_data = null;

//	protected int m_ex_id;
	protected String m_ex_name;

	/** The number of sets for this exercise. */
	protected int m_num_sets;

	/**
	 * This tells the Activity when it needs to load data.  It
	 * is set to TRUE by OTHER Activities (ASet, EditSet) when
	 * the database is changed and the list of exercise sets
	 * has changed.
	 *
	 * When the Inspector has reloaded, m_changed is moved to
	 * FALSE.
	 */
	public static boolean m_db_dirty = true;

	/**
	 * Similar to m_db_dirty, but this indicates that the
	 * database itself has changed, not that it *should* change.
	 * This flag is used to signal to the Grid whether or not
	 * it needs to reload when this Activity exits.
	 */
	protected boolean m_database_changed = false;

	/** A hack to get around scoping rules for scroll_to_child(id). */
	protected static int s_id;

	/**
	 * This indicates whether or not the main layout has
	 * been initialized.  Necessary because of the multi-threading
	 * nature of this class.
	 */
	protected boolean m_layout_initialized = false;

	/** Should we use the quick view or the regular view */
//	protected boolean m_prefs_quick_view = false;

	/** The order that we should sort the workout sets */
	protected boolean m_prefs_oldest_order = true;

	//------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inspector);

		// Load in our preferences.
		SharedPreferences prefs =
			PreferenceManager.getDefaultSharedPreferences(this);
//		m_prefs_quick_view = prefs.getBoolean(getString(R.string.prefs_inspector_quickview_key),
//											false);
		m_prefs_oldest_order = prefs.getBoolean(getString(R.string.prefs_inspector_oldest_first_key),
												true);
		set_order_msg();

		// Get our Intent and fill in the info that was passed
		// from the Grid to here (via ExerciseTabHostActivity).
		Intent itt = getIntent();
		m_ex_name = itt.getStringExtra(ExerciseTabHostActivity.KEY_NAME);
		if (m_ex_name == null) {
			Log.e(tag, "Cannot find the exercise name in onCreate()!!!");
			return;
		}

		// Get the ID.  If there's no specific set, then the value
		// should be -1.
		m_set_id = itt.getIntExtra(ExerciseTabHostActivity.KEY_SET_ID, -1);

		// Holds all the sets.
		m_main_ll = (LinearLayout) findViewById(R.id.inspector_all_sets_ll);

		m_db_dirty = true;	// True for first time.
	} // onCreate (.)



	//------------------------------
	@Override
	protected void onResume() {
		super.onResume();
//		Log.i(tag, "onResume()");

		if (m_db_dirty) {
			init_from_db();
		}

	} // onResume()

	//------------------------------
	//	Allows this Activity to send message to the caller
	//	when the user hits the back button.
	//
	@Override
	public void onBackPressed() {
		if (m_database_changed) {
			tabbed_set_result(RESULT_OK);
		}
		else {
			tabbed_set_result(RESULT_CANCELED);
		}
		finish();
	} // onBackPressed()


	//------------------------------
	//	Initialize the menu.
	//
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		menu.add(0, MENU_ID_BRIEF, 0, R.string.null_string);
		menu.add(0, MENU_ID_ORDER, 0, R.string.null_string);

		return super.onCreateOptionsMenu(menu);
	}


	//------------------------------
	//	I use this method to make sure that the correct
	//	message is displayed.
	//
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//		menu.findItem(MENU_ID_BRIEF).setTitle(m_prefs_quick_view ?
//							R.string.inspector_menu_verbose_label :
//							R.string.inspector_menu_brief_label);

		menu.findItem(MENU_ID_ORDER).setTitle(m_prefs_oldest_order ?
							R.string.inspector_menu_newest_first_label :
							R.string.inspector_menu_old_first_label);
		return super.onPrepareOptionsMenu(menu);
	}



	//------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		int id = item.getItemId();
		switch (id) {
//			case MENU_ID_BRIEF:
//				m_prefs_quick_view = !m_prefs_quick_view;
//				prefs.edit().putBoolean(getString(R.string.prefs_inspector_quickview_key),
//										m_prefs_quick_view)
//								.commit();
//				break;

			case MENU_ID_ORDER:
				m_prefs_oldest_order = !m_prefs_oldest_order;
				prefs.edit().putBoolean(getString(R.string.prefs_inspector_oldest_first_key),
										m_prefs_oldest_order)
								.commit();
				// todo:
				//	Need to reset which workout set shows first.
				//	Should be the default.
				// Show the default at the top.
				m_set_id = -1;
				set_order_msg();
				init_from_db();
				break;

			default:
				Log.e (tag, "Illegal id: " + id + ", in onOptionsItemSelected!");
				break;
		}
		return super.onOptionsItemSelected(item);
	}



	//------------------------------
	public boolean onLongClick(View v) {
		Intent itt;

		// Did they long-click one of the displayed exercise
		// sets?
		if (v.getClass() == LinearLayout.class) {
			itt = new Intent (this, EditSetActivity.class);
			itt.putExtra(EditSetActivity.ID_KEY, v.getId());
			startActivityForResult(itt, WGlobals.EDITSETACTIVITY);
		}

		return false;
	} // onLongClick (v)


	//------------------------------
	//	This Activity calls EditSetActivity.  When it
	//	returns, this method gets the result.
	//
	//	EditSetActivity:
	//		- If OK, then reload the exercise set.
	//		- CANCEL, do nothing.
	//
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
									Intent data) {
//		Log.i(tag, "onActivityResult()");

		if (resultCode == RESULT_CANCELED) {
			return;	// don't do anything
		}

		// todo:
		//	When we reload the Activity after modifying something,
		//	scroll to the thing that was modified, not the whatever
		//	was scrolled to when onCreate() was initially called.

		// This happens when a set has been edited/deleted.
		if (requestCode == WGlobals.EDITSETACTIVITY) {
			if (data == null) {
				Log.v(tag, "Intent data is NULL in onActivityResult()!");
				m_set_id = -1;
			}
			else {
				m_set_id = data.getIntExtra(EditSetActivity.ID_KEY, -1);
			}
			init_from_db();
			HistoryActivity.m_db_dirty = true;
			GraphActivity.m_db_dirty = true;
			m_database_changed = true;	// Used to tell the Grid that the DB changed.
		}

	} // onActivityResult (requestCode, resultCode, data)

	/***********************
	 * Scrolls the Activity (along its ScrollView) so that the child
	 * in question is at the top of the screen.
	 *
	 * side effects:
	 * 		uses a static variable: s_id to pass data to the Runnable.
	 *
	 * @param id		The ID of the View to scroll to.
	 * 				-1 means invalid id, so nothing is done.
	 */
	protected void scroll_to_child (int id) {
		Log.v (tag, "entering scroll_to_child (" + id + ")");

		// WARNING!  This is a HACK to get around scoping rules!
		s_id = id;

		// Set the scroll to the right value.
		m_sv = (ScrollView) findViewById(R.id.inspector_sv);

		// For the times we need to scroll to a given child of the
		// scrollview.
		if (id == -1) {
			m_sv.scrollTo(0, 0);		// Go to the top.
			return;
		}

		// Make it scroll, but first we have to wait for
		// everything to be set up.
		m_sv.post(new Runnable() {
			public void run() {
				// Now we can figure out heights.  Go through the
				// children, measuring them until we find the right
				// id.
				int height_of_views = 0;
				
//				Log.v (tag, "Starting to scroll " + m_main_ll.getChildCount() + " child views.");

				for (int i = 0; i < m_main_ll.getChildCount(); i++) {
					View child = m_main_ll.getChildAt(i);
					if (s_id == child.getId()) {
						break;
					}
					height_of_views += child.getHeight();
//					Log.v(tag, "  - added " + child.getHeight() + " to scroll amount...");
				}

//				Log.v(tag, "Scrolling: " + height_of_views);
				m_sv.scrollTo(0, height_of_views);
			}
		});
	} // scroll_to_child (id)


	/***********************
	 * Reads data from the database and populates the Activity.
	 *
	 * This is the hard part of initializing that is normally in onCreate().
	 * Sometimes we have to do this part during an onResume(), so it
	 * has been moved here.
	 *
	 * All the dirty details have been moved to an ASyncTask as database
	 * stuff can take a while.
	 */
	void init_from_db() {

		// Set the order description.
		m_desc_tv = (TextView) findViewById(R.id.inspector_description_tv);
		m_desc_tv.setText(m_prefs_oldest_order ?
							R.string.inspector_oldest_first_msg :
							R.string.inspector_newest_first_msg);


		// Start the AsyncTask.  It'll handle the rest.
		new InspectorSyncTask().execute();
	} // init_from_db()


	/***************************
	 * Helper to converts the given number to a nice string for
	 * display in the inspector as a weight, distance, etc.
	 *
	 * @param f		The FLOAT to turn into a string
	 * @return		A nice string, or an appropriate message
	 * 				if the user skipped this value.
	 */
	private String get_formatted_string (float f) {
		if (f < 0)
			return getString (R.string.inspector_skipped_value);
		return new DecimalFormat("#.###").format(f);
	}

	/***************************
	 * Helper to converts the given number to a nice string for
	 * display in the inspector as a weight, distance, etc.
	 *
	 * @param i		The INT to turn into a string
	 * @return		A nice string, or an appropriate message
	 * 				if the user skipped this value.
	 */
	private String get_formatted_string (int i) {
		if (i == -1) {
			return getString(R.string.inspector_skipped_value);
		}
		return ("" + i);
	}


	/********************
	 * Part of the UI thread while creating the display, this
	 * takes all the data for a set that was loaded in by the
	 * ASyncTask and puts those values into a new layout.
	 *
	 * Now this is filled in along the way, making a pleasing UI
	 * while the data is loading.  But this means that there
	 * will need to be a little bit of sorting as we figure out
	 * where to put the layout that was sent in.
	 *
	 * preconditions:
	 * 		m_main_ll		Needs to be ready to receive children.
	 *
	 * @param layout_values		The data for this set that was
	 * 							loaded from the DB.
	 */
	void make_set_layout (SetLayout layout_values) {
		if (!m_layout_initialized) {
			init_layout(layout_values.data.millis);
		}

		/** used to determine whether or not to draw a divider bar */
		boolean set_bar = false;

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout set_ll = (LinearLayout) inflater
				.inflate(R.layout.inspector_set, m_main_ll, false);

		// Put the day above the info rectangle
		TextView date_tv = (TextView) set_ll.findViewById(R.id.inspector_set_date_tv);
		MyCalendar cal = new MyCalendar(layout_values.data.millis);
		date_tv.setText(cal.print_date(this));

		// todo:		USE THIS!!!
		// Not using the title currently.
		TextView title_tv = (TextView) set_ll.findViewById(R.id.inspector_set_title_tv);
		title_tv.setText("");

		// The date and time of this set.
		setup_date (layout_values, set_ll);

		if (setup_reps(layout_values, set_ll))
			set_bar = true;

		if (setup_weight (layout_values, set_ll, set_bar))
			set_bar = true;

		if (setup_level (layout_values, set_ll, set_bar))
			set_bar = true;

		if (setup_cals (layout_values, set_ll, set_bar))
			set_bar = true;

		if (setup_dist (layout_values, set_ll, set_bar))
			set_bar = true;

		if (setup_time (layout_values, set_ll, set_bar))
			set_bar = true;

		if (setup_other (layout_values, set_ll, set_bar))
			set_bar = true;

		setup_stress (layout_values, set_ll);

		setup_notes (layout_values, set_ll);

		// Make this respond to long clicks.  Use the set ID.
		set_ll.setId(layout_values.data._id);
		set_ll.setOnLongClickListener(this);


		// Lastly we add this layout to m_main_ll.  But first,
		// we gotta figure out where to add it.
		//	O(n), sigh.
		//
		set_ll.setTag(layout_values.order);
		int i;
		for (i = 0; i < m_main_ll.getChildCount(); i++) {
			if (((Integer) (m_main_ll.getChildAt(i).getTag()))
					> layout_values.order) {
				break;
			}
		}

		m_main_ll.addView(set_ll, i);

	}  // make_set_layout (layout_values)

	/**************************
	 * Call this to reset the non-database part of the
	 * of the UI.
	 *
	 * preconditions:
	 * 	m_prefs_oldest_order		is properly set.
	 *
	 */
	void set_order_msg() {
		// Make ch
		if (m_desc_tv == null) {
			m_desc_tv = (TextView) findViewById(R.id.inspector_description_tv);
		}
		m_desc_tv.setText(m_prefs_oldest_order ?
							R.string.inspector_oldest_first_msg :
							R.string.inspector_newest_first_msg);
	} // init_ui()



	/********************
	 * Called the first time that make_set_layout() is called.
	 * This sets up all the general stuff that's germain to
	 * every set layout.
	 */
	protected void init_layout (long date_in_millis) {
		if (m_layout_initialized) {
			return;
		}

		// Tabula rasa.
		m_main_ll.removeAllViews();

		// If there are no sets, indicate so.
		TextView title_tv = (TextView) findViewById(R.id.inspector_title_tv);
		if (m_num_sets == 0) {
			title_tv.setText(R.string.inspector_empty);
		}
		else {
			title_tv.setVisibility(View.GONE);
			// Show the date, but only if there IS a specific date.
//			if (m_set_id != -1) {
//				MyCalendar cal = new MyCalendar(date_in_millis);
//				title_tv.setText(cal.get_month_text(this) + " "
//								+ cal.get_day() + ", "
//								+ cal.get_year());
//			}
		}
		m_layout_initialized = true;
	} // init_layout()


	/********************
	 * Goes through the m_main_ll and fills out the Date labels
	 * for the sets.  This is tricky, because we don't want to
	 * repeat ourselves (looks tacky!).
	 *
	 * preconditions:
	 *		The main layout (m_main_ll) is filled out with
	 *		all the sets that will be there.
	 */
	void trim_date_labels() {
		if (m_main_ll.getChildCount() == 0)
			return;

		String last_date = "", date;

		for (int i = 0; i < m_main_ll.getChildCount(); i++) {
			View child = m_main_ll.getChildAt(i);
			TextView tv = (TextView) child.findViewById(R.id.inspector_set_date_tv);
			date = (String) tv.getText();
			if (date.contentEquals(last_date)) {
				tv.setVisibility(View.GONE);
			}
			last_date = date;
		}

	} // make_date_labels()


	/********************
	 * Sets up the date and time portion of this Activity.
	 *
	 * @param vals			Holds the date to display
	 *
	 * @param set_ll			The linearLayout that holds this
	 * 						particular exercise set.  It'll
	 * 						have some Views added.
	 */
	private void setup_date (SetLayout vals, LinearLayout set_ll) {
		String str = "";
		MyCalendar set_date = new MyCalendar(vals.data.millis);

		// And display the time of this particular set.
		TextView time_tv = (TextView) set_ll.findViewById(R.id.inspector_set_time_tv);

		// Always do the time.
		str += set_date.print_time(false);

		time_tv.setText(str);
	} // setup_date (set_cursor, set_ll)

	/********************
	 * Sets up the reps portion.
	 *
	 * @param vals			Holds the reps to display
	 *
	 * @param set_ll			The linearLayout that holds this
	 * 						particular exercise set.  It'll
	 * 						have some Views added.
	 *
	 * @return	Whether or not set_bar need to be true.  Use
	 * 			this like:	if (setup_reps())
	 * 							set_bar = true;
	 */
	private boolean setup_reps (SetLayout vals,
							LinearLayout set_ll) {
		LinearLayout reps_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_reps_ll);

		if (m_ex_data.breps) {
			String data_str = get_formatted_string(vals.data.reps);
			TextView reps_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_reps_data);
			reps_data_tv.setText(data_str);
			if (m_ex_data.significant == DatabaseHelper.EXERCISE_COL_REP_NUM) {
				TextView reps_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_reps_label);
				reps_label_tv.setTypeface(null, Typeface.BOLD);
			}
			return true;
		}
		else {
			// If not applicable, make it disappear!
			reps_ll.setVisibility(View.GONE);
		}
		return false;
	} // setup_reps (set_cursor, ex_cursor, set_ll)


	/*********************
	 */
	private boolean setup_weight (SetLayout vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout weight_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_weight_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_weight_bar);

		if (m_ex_data.bweight) {
			TextView weight_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_weight_label);
			String weight_label_str = getString (R.string.inspector_set_weight_label,
					(Object[]) new String[] {m_ex_data.weight_unit});
			weight_label_tv.setText(weight_label_str);

			TextView weight_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_weight_data);
			weight_data_tv.setText(get_formatted_string(vals.data.weight));
			if (m_ex_data.significant == DatabaseHelper.EXERCISE_COL_WEIGHT_NUM) {
				weight_label_tv.setTypeface(null, Typeface.BOLD);
			}

			// The bar.  Only draw if set_bar is true.
			if (!set_bar) {
				bar.setVisibility(View.GONE);
				return true;
			}
		}
		else {
			// If not applicable, make it disappear!
			weight_ll.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		return false;
	} // setup_weight (set_cursor, ex_cursor, set_ll)


	/*********************
	 */
	private boolean setup_level (SetLayout vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout level_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_level_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_level_bar);

		if (m_ex_data.blevel) {
			String data_str = get_formatted_string(vals.data.levels);
			TextView level_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_level_data);
			level_data_tv.setText(data_str);
			if (m_ex_data.significant == DatabaseHelper.EXERCISE_COL_LEVEL_NUM) {
				TextView level_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_level_label);
				level_label_tv.setTypeface(null, Typeface.BOLD);
			}

			if (!set_bar) {
				bar.setVisibility(View.GONE);
				return true;
			}
		}
		else {
			// If not applicable, make it disappear!
			level_ll.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		return false;
	} // level


	/************************
	 *
	 */
	private boolean setup_cals (SetLayout vals,
							LinearLayout set_ll, boolean set_bar) {
		LinearLayout cals_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_calorie_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_calorie_bar);

		if (m_ex_data.bcals) {
			String data_str = get_formatted_string(vals.data.cals);
			TextView cals_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_calorie_data);
			cals_data_tv.setText(data_str);
			if (m_ex_data.significant == DatabaseHelper.EXERCISE_COL_CALORIE_NUM) {
				TextView cals_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_calorie_label);
				cals_label_tv.setTypeface(null, Typeface.BOLD);
			}
			if (!set_bar) {
				bar.setVisibility(View.GONE);
				return true;
			}
		}
		else {
			// If not applicable, make it disappear!
			cals_ll.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		return false;
	} // calories


	/***********************
	 *
	 */
	private boolean setup_dist (SetLayout vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout dist_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_dist_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_dist_bar);

		if (m_ex_data.bdist) {
			// Unit of Distance
			TextView dist_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_dist_label);
			String dist_label_str = getString (R.string.inspector_set_dist_label, m_ex_data.dist_unit);
			dist_label_tv.setText(dist_label_str);
			if (m_ex_data.significant == DatabaseHelper.EXERCISE_COL_DIST_NUM) {
				dist_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = get_formatted_string(vals.data.dist);
			TextView dist_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_dist_data);
			dist_data_tv.setText(data_str);

			if (!set_bar) {
				bar.setVisibility(View.GONE);
				return true;
			}
		}
		else {
			// If not applicable, make it disappear!
			dist_ll.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		return false;
	} // distance

	/***********************
	 *
	 */
	private boolean setup_time (SetLayout vals,
							LinearLayout set_ll, boolean set_bar) {
		LinearLayout time_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_time_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_time_bar);

		if (m_ex_data.btime) {
			// Unit of Time
			TextView time_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_time_label);
			String time_label_str = getString (R.string.inspector_set_time_label, m_ex_data.time_unit);
			time_label_tv.setText(time_label_str);
			if (m_ex_data.significant == DatabaseHelper.EXERCISE_COL_TIME_NUM) {
				time_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = get_formatted_string(vals.data.time);
			TextView time_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_time_data);
			time_data_tv.setText(data_str);

			if (!set_bar) {
				bar.setVisibility(View.GONE);
				return true;
			}
		}
		else {
			// If not applicable, make it disappear!
			time_ll.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		return false;
	} // time

	/***********************
	 *
	 */
	private boolean setup_other (SetLayout vals,
							LinearLayout set_ll, boolean set_bar) {
		LinearLayout other_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_other_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_other_bar);

		if (m_ex_data.bother) {
			TextView other_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_other_label);
			String other_label_str = getString (R.string.inspector_set_other_label, m_ex_data.other_title, m_ex_data.other_unit);
			other_label_tv.setText(other_label_str);
			if (m_ex_data.significant == DatabaseHelper.EXERCISE_COL_OTHER_NUM) {
				other_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = get_formatted_string(vals.data.other);

			TextView other_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_other_data);
			other_data_tv.setText(data_str);

			if (!set_bar) {
				bar.setVisibility(View.GONE);
				return true;
			}
		}
		else {
			// If not applicable, make it disappear!
			other_ll.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		return false;
	} // other

	/***********************
	 *
	 */
	private void setup_stress (SetLayout vals,
							LinearLayout set_ll) {
		ImageView stress_data_iv = (ImageView) set_ll.findViewById(R.id.inspector_set_stress_data);

		switch (vals.data.cond) {
			case DatabaseHelper.SET_COND_OK:
				stress_data_iv.setImageResource(R.drawable.stress_just_right);
				break;
			case DatabaseHelper.SET_COND_MINUS:
				stress_data_iv.setImageResource(R.drawable.stress_too_heavy);
				break;
			case DatabaseHelper.SET_COND_PLUS:
				stress_data_iv.setImageResource(R.drawable.stress_too_easy);
				break;
			case DatabaseHelper.SET_COND_INJURY:
				stress_data_iv.setImageResource(R.drawable.stress_injury);
				break;
			default:
				stress_data_iv.setImageResource(R.drawable.stress_error);
				break;
		}
	} // setup_stress (vals, set_ll)


	/***********************
	 *
	 */
	private void setup_notes (SetLayout vals,
							LinearLayout set_ll) {
		if ((vals.data.notes != null) && (vals.data.notes.length() > 0)) {
			// There's a note!  Display it.
			TextView notes_tv = (TextView) set_ll.findViewById(R.id.inspector_set_notes_tv);
			notes_tv.setText(vals.data.notes);
		}
	} // notes


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
				throw new SQLiteException("m_db not null when starting doInBackground() in InspectorActivity!");
		}
	} // test_m_db()


		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//	Classes
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**************************************************
	 * Operates in the background to load the UI stuff from our
	 * database, which can take a while.
	 *
	 * The types are: <params, progress, result>
	 */
	class InspectorSyncTask extends AsyncTask <Void, SetLayout, Void> {

		static final String tag = "InspectorSyncTask";

		//-------------------
		@Override
		protected void onPreExecute() {
			start_progress_dialog(R.string.loading_str);
			m_layout_initialized = false;
		} // onPreExecute

		//-------------------
		@Override
		protected Void doInBackground(Void... not_used) {

			try {
				test_m_db();
				m_db = WGlobals.g_db_helper.getReadableDatabase();

				// Read in all the info we need about this
				// exercise.
				m_ex_data = DatabaseHelper.getExerciseData(m_db, m_ex_name);

				// Get a cursor for the sets.  Then loop through
				// them one by one, creating a layout for each.
				// This requires filling in a SetLayout class
				// to hold the appropriate (and relevant) data
				// for that layout.
				Cursor set_cursor = null;
				try {
					set_cursor = DatabaseHelper.getAllSets(m_db, m_ex_name,
									m_prefs_oldest_order);
					m_num_sets = set_cursor.getCount();

					int counter = 0;
					while (set_cursor.moveToNext()) {

						SetLayout layout_values = new SetLayout();
						layout_values.data = DatabaseHelper.getSetData(set_cursor);
						layout_values.order = counter;
						publishProgress(layout_values);
						counter++;
					}

					// For the case where there are ZERO sets,
					if (m_num_sets == 0) {
						publishProgress((SetLayout)null);
					}

				} // end of set_cursor
				catch (SQLiteException e) {
					e.printStackTrace();
				}
				finally {
					if (set_cursor != null) {
						set_cursor.close();
						set_cursor = null;
					}
				}


			} // end of m_db usage
			catch (SQLiteException e) {
				e.printStackTrace();
			}
			finally {
				if (m_db != null) {
					m_db.close();
					m_db = null;
				}
				m_db_dirty = false;
			}

			return null;
		} // doInBackground(...)


		//-------------------
		//	Okay, gotta be careful with this one.  It happens in the
		//	UI thread, not the same thread as doInBackground().  That
		//	means that if any variables passed into this can be changed
		//	in doInBackground(), then this is a BIG problem!
		//
		//	input:
		//		set_array		This is an array of SetLayouts that
		//						were filled in by the background
		//						thread via doInBackground().  It has
		//						only ONE element, the most recently
		//						created SetLayout.  Let's use it
		//						to make a new exercise set layout!
		//
		@Override
		protected void onProgressUpdate(SetLayout... set_array) {
			super.onProgressUpdate(set_array);
			if (set_array[0] == null) {
				init_layout(0);
			}
			else {
				make_set_layout (set_array[0]);
			}
		} // onProgressUpdate (arg0)


		//-------------------
		@Override
		protected void onPostExecute(Void not_used) {
			// todo
			//	Put the date in a TextView.  But only do it if it's a different
			//	date from the earlier one.  This is a little tricky.
			//
			trim_date_labels();
			scroll_to_child (m_set_id);
			stop_progress_dialog();
		}

	} // class InspectorSyncTask


	/****************************************
	 * Holds the data needed to fill in an entire set's layout.
	 */
	class SetLayout {
		/** The order that this set should be displayed. Zero based. */
		int order;
		/** Holds all the data for the exercise set */
		SetData data;
	} // class SetLayout


}
