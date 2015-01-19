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
import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class InspectorActivity2
				extends BaseDialogActivity
				implements
						OnLongClickListener {

	//------------------
	//	Constants
	//------------------

	private static final String tag = "InspectorActivity2";

	/** Id for the menu item to change the order that sets are listed */
	protected static final int MENU_ID_ORDER = 2;

	//------------------
	//	Widgets
	//------------------

	/**
	 * The LinearLayout that holds all the Views (each View represents
	 * a workout set).
	 */
	LinearLayout m_main_ll;

	/** The ScrollView which holds all the workout sets. */
	ScrollView m_sv;

	/**
	 * If we're in landscape mode, this is used instead of
	 * m_sv.  Otherwise, this is null.
	 */
	HorizontalScrollView m_hsv = null;

	/** Tells if we're in landscape mode or not. */
//	private boolean m_landscape = false;

	TextView m_desc_tv;


	//------------------
	//	Data
	//------------------

	/**
	 * The id for the exercise set to center on.  This is only assigned
	 * during onCreate().  If this is a valid id number, then the
	 * set with the given id should be scrolled to when this Activity
	 * pops up the first time.
	 *
	 * If the number is invalid (-1), then scroll to the top (or don't
	 * scroll at all).
	 */
	private int m_set_id = -1;

	/**
	 * Since the ASyncTask is static, it may persist
	 * when this Activity is destroyed.  This variable
	 * will be passed back to us via
	 * onRetainLastConfiguationInstance() and
	 * getLastNonConfigurationInstance().
	 */
//	private InspectorASyncTask m_task = null;

	/**
	 * Holds instances of the AsyncTask.  Should be set
	 * to null when completed.
	 */
	private InspectorAsyncTask2 m_task = null;


	//--------------------
	//	All from the exercise database
	//--------------------

	/**
	 * Holds all the info about this particular exercise.  Filled
	 * in by the AsyncTask.
	 */
	public ExerciseData m_ex_data = null;

	/** the name of this exercise. */
	private String m_ex_name;

	/** The number of sets for this exercise. Filled in during Async. */
	protected int m_num_sets;

	/**
	 * This tells the Activity when it needs to load data.  It
	 * is set to TRUE by OTHER Activities (ASet, EditSet) when
	 * the database is changed and the list of exercise sets
	 * has changed.
	 *
	 * When the Inspector has reloaded, this is set to false.
	 */
	public static boolean m_db_dirty = true;

	/**
	 * Similar to m_db_dirty, but this indicates that the
	 * database itself has changed, not that it *should* change.
	 * This flag is used to signal to the Grid whether or not
	 * it needs to reload when this Activity exits.
	 */
	private static boolean m_database_changed = false;

	/** A hack to get around scoping rules for scroll_to_child(id). */
	protected static int s_id;

	/**
	 * This indicates whether or not the main layout has
	 * been initialized.  Necessary because of the multi-threading
	 * nature of this class.
	 *<p>
	 * todo: is this really needed?
	 */
	protected boolean m_async_layout_complete = false;

	/**
	 * This is IT. This is the list that holds all the layouts
	 * (which are sets + an order).  This is filled out progressively
	 * during doInBackground() and tapped during onProgressUpdate()
	 * and catchup() via the AsyncTask.
	 */
	public ArrayList<SetLayout> m_layout_list = null;



	//------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inspector);

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


//		m_task = (InspectorASyncTask) getLastNonConfigurationInstance();

		// Moved from onResume(), which is no longer needed as we use
		// onActivityResult() instead.
		init_from_db();
	} // onCreate (.)


//	//------------------------------
//	@Override
//	protected void onResume() {
//		super.onResume();
//		Log.i(tag, "onResume() start");
//
//		if (m_db_dirty) {
//			Log.d(tag, "onResume(): calling init_from_db() from onResume()");
//			init_from_db();
//		}
//		else {
//			Log.d(tag, "onResume(): m_db_dirty is false, so I'm not calling init_from_db().");
//		}
//
//	} // onResume()



//	/*************************
//	 * Called when an Activity is destroyed during a
//	 * configuration/orientation change.  Whatever is
//	 * returned here can be retrieved by the new replacement
//	 * Activity by calling getlastNonConfigurationInstance().
//	 *
//	 * @see android.app.Activity#onRetainNonConfigurationInstance()
//	 */
//	@Override
//	public Object onRetainNonConfigurationInstance() {
//		m_task.detach();		// Tells task to remove its reference
//							// to this Activity as I'm about to
//							// die.
//
//		return m_task;	// Return the ASyncTask so the
//						// new Activity can find it (and then
//						// attach it to the ASyncTask).
//	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (m_task != null) {
			// Tell the AsyncTask to stop because the Activity
			// is going away.
			m_task.cancel(true);
		}

		// Kill a dialog if running.
		if (is_progress_dialog_active()) {
			stop_progress_dialog();
		}

		super.onDestroy();
	}


	//------------------------------
	//	Allows this Activity to send message to the caller
	//	when the user hits the back button.
	//
	@Override
	public void onBackPressed() {
		if ((ExerciseTabHostActivity.m_dirty) || (m_database_changed)) {
			tabbed_set_result(RESULT_OK);
		}
		else
			tabbed_set_result(RESULT_CANCELED);
		finish();
	} // onBackPressed()


	//------------------------------
	//	Initialize the menu.
	//
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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

		SharedPreferences prefs =
		PreferenceManager.getDefaultSharedPreferences(this);
		boolean prefs_oldest_order =
				prefs.getBoolean(getString(R.string.prefs_inspector_oldest_first_key),
								false);
		menu.findItem(MENU_ID_ORDER).setTitle(prefs_oldest_order ?
							R.string.inspector_menu_newest_first_label :
							R.string.inspector_menu_old_first_label);
		return super.onPrepareOptionsMenu(menu);
	}



	//------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		boolean prefs_oldest_order =
		prefs.getBoolean(getString(R.string.prefs_inspector_oldest_first_key),
						false);

		int id = item.getItemId();
		switch (id) {
//			case MENU_ID_BRIEF:
//				m_prefs_quick_view = !m_prefs_quick_view;
//				prefs.edit().putBoolean(getString(R.string.prefs_inspector_quickview_key),
//										m_prefs_quick_view)
//								.commit();
//				break;

			case MENU_ID_ORDER:
				prefs_oldest_order = !prefs_oldest_order;
				prefs.edit().putBoolean(getString(R.string.prefs_inspector_oldest_first_key),
										prefs_oldest_order)
								.commit();

				// Show the default at the top.
				m_set_id = -1;
				set_order_msg();
				Log.d(tag, "calling init_from_db() from onOptionsItemSelected()");
				init_from_db();
				break;

			default:
				Log.e (tag, "Illegal id: " + id + ", in onOptionsItemSelected!");
				break;
		}
		return super.onOptionsItemSelected(item);
	} // onOptionsItemSelected(item)


	//------------------------------
	@Override
	public boolean onLongClick(View v) {
		Intent itt;

		// Did they long-click one of the displayed exercise
		// sets?
		if (v.getClass() == LinearLayout.class) {
			WGlobals.play_long_click();
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
	//	Note that this is called BEFORE onResult()
	//
	//	EditSetActivity:
	//		- CANCEL, do nothing.
	//		- If OK, then
	//			- if no Intent, redraw everything as a set was deleted.
	//				NOTE: might be able to simply remove this View???
	//			- else reload the exercise set.
	//
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
									Intent itt) {
		if (resultCode == RESULT_CANCELED) {
			return;	// don't do anything
		}

		// This happens when a set has been edited/deleted.
		if (requestCode == WGlobals.EDITSETACTIVITY) {
			if (itt == null) {
				Log.e(tag, "Intent data is NULL in onActivityResult()! Aborting!");
				m_set_id = -1;
				return;
			}

			// Get the id of the exercise set that was modified or
			// deleted by the user.  We'll need to change the UI to
			// indicate the change.
			m_set_id = itt.getIntExtra(EditSetActivity.ID_KEY, -1);
			switch (resultCode) {
				case EditSetActivity.RESULT_DELETED: {	// Brackets are for scoping
					// The workout set was deleted.  Remove the
					// View from our Activity.
					LinearLayout view = (LinearLayout) m_main_ll.findViewById(m_set_id);
					if (view == null) {
						Log.e(tag, "Can't find the deleted view in onActivityResult! Aborting!");
						return;
					}
					m_main_ll.removeView(view);
					break;
				}

				case EditSetActivity.RESULT_TIME_CHANGED:
					// todo
					Log.e(tag, "need to implement this!!!");


					//----
					// Falls through...

				case RESULT_OK: {
					SetData set_data = null;
					SQLiteDatabase db = null;

					// The workout set was modified.  Read in the
					// new data and modify the View appropriately.

					// Get our View to modify
					LinearLayout view = (LinearLayout) m_main_ll.findViewById(m_set_id);
					if (view == null) {
						Log.e(tag, "Can't find the view in onActivityResult! Aborting!");
						return;
					}

					// Read in the new data.
					try {
						db = WGlobals.g_db_helper.getReadableDatabase();
						set_data = DatabaseHelper.getSetData (db, m_ex_name, m_set_id);
					}
					finally {
						if (db != null) {
							db.close();
							db = null;
						}
					}

					// Fill in the View.
					fill_set_layout(set_data, view);
					break;
				}

				default:
					Log.e(tag, "Illegal value for resultCode in onActivityResult! Aborting!");
					return;
			}

			// Indicate to the other Activities that something has changed.
			// todo: this could be redundant!
			ExerciseTabHostActivity.m_dirty = true;
			GraphActivity.m_db_dirty = true;
			AddSetActivity.m_reset_widgets = true;
			m_database_changed = true;	// Used to tell the Grid that the DB changed.


/*
			Log.d(tag, "calling init_from_db() from onActivityResult()");
			init_from_db();
			ExerciseTabHostActivity.m_dirty = true;
			GraphActivity.m_db_dirty = true;
			AddSetActivity.m_reset_widgets = true;
			m_database_changed = true;	// Used to tell the Grid that the DB changed.
*/
		}
		else {
			Log.e(tag, "Unknown requestCode in onActivityResult!!!");
		}

	} // onActivityResult (requestCode, resultCode, data)


	/***********************
	 * Scrolls the Activity (along its ScrollView) so that the child
	 * in question is at the top of the screen.
	 *
	 * preconditions:
	 * 		The layout is finished and completely set up.  That way,
	 * 		all the sizes are valid and we can scroll to the correct
	 * 		location.
	 *
	 * side effects:
	 * 		uses a static variable: s_id to pass data to the Runnable.
	 *
	 * @param id		The ID of the View to scroll to.
	 * 				-1 means invalid id, so nothing is done.
	 */
	protected void scroll_to_child (int id) {
//		Log.v (tag, "entering scroll_to_child (" + id + ")");

		// WARNING!  This is a HACK to get around scoping rules!
		s_id = id;

		int orientation = get_screen_orientation();

		// Set the scroll to the right value.
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			m_hsv = (HorizontalScrollView) findViewById(R.id.inspector_hsv);
		}
		else {
			m_sv = (ScrollView) findViewById(R.id.inspector_sv);
		}

		// For the times we need to scroll to a given child of the
		// scrollview.
		if (id == -1) {
			if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				m_hsv.scrollTo(0, 0);	// Go to the left.
			}
			else {
				m_sv.scrollTo(0, 0);		// Go to the top.
			}
			return;
		}

		// Make it scroll, but first we have to wait for
		// everything to be set up.
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			m_hsv.post(new Runnable() {
				@Override
				public void run() {
					// Now we can figure out heights.  Go through the
					// children, measuring them until we find the right
					// id.
					int width_of_views = 0;

					for (int i = 0; i < m_main_ll.getChildCount(); i++) {
						View child = m_main_ll.getChildAt(i);
						if (s_id == child.getId()) {
							break;
						}
						width_of_views += child.getWidth();
					}

					m_hsv.scrollTo(width_of_views, 0);
				}
			});
		}
		else {
			m_sv.post(new Runnable() {
				@Override
				public void run() {
					// Now we can figure out heights.  Go through the
					// children, measuring them until we find the right
					// id.
					int height_of_views = 0;

					for (int i = 0; i < m_main_ll.getChildCount(); i++) {
						View child = m_main_ll.getChildAt(i);
//						Log.d(tag, "Looping: i = " + i + ", id = " + child.getId() + ", height_of_views = " + height_of_views);
						if (s_id == child.getId()) {
//							Log.d(tag, "\tbreaking!");
							break;
						}
						height_of_views += child.getHeight();
					}

					m_sv.scrollTo(0, height_of_views);
//					Log.d(tag, "portrait scroll to " + height_of_views);
				}
			});
		}
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
	 *
	 * todo:
	 * 	I don't think this will work properly when the DB is dirty
	 * 	and the ASyncTask is finished or in-progress (isDone() = false).
	 */
	void init_from_db() {
		Log.d(tag, "init_from_db()");

		// Set the order description.
		m_desc_tv = (TextView) findViewById(R.id.inspector_description_tv);

		//---------------
		// todo:
		//	We need a preferences manager to simplify this.
		//---------------

		SharedPreferences prefs =
		PreferenceManager.getDefaultSharedPreferences(this);
		boolean prefs_oldest_order =
				prefs.getBoolean(getString(R.string.prefs_inspector_oldest_first_key),
								false);
		m_desc_tv.setText(prefs_oldest_order ?
							R.string.inspector_oldest_first_msg :
							R.string.inspector_newest_first_msg);

		// Start the ASyncTask!!!
//		if (m_task == null) {
//			// No ASync running, so start it up.
//			start_progress_dialog(R.string.loading_str);
//			m_task = new InspectorASyncTask(this, m_ex_name);
//			Log.d(tag, "init_from_db(): STARTING NEW ASYNCTASK!");
//			m_task.execute();
//		}
//		else {
////			Log.d(tag, "init_from_db(): taking the else clause...");
//
//			// Tell the ASyncTask who the Activity is (us!).
//			m_task.attach(this);
//
//			// If the ASyncTask is still working, restart
//			// the progress dialog.
//			if (m_task.isDone() == false) {
//				start_progress_dialog(R.string.loading_str);
//			}
//			else {
//				// The ASyncTask has completed.  Just catchup and
//				// finish.
//				catchup();
//				finish_ui();
//
//				// todo
//				//	DON'T!!! This could be VERY VERY BAD!!!  The ASyncTack
//				//	isn't guaranteed to still be around, so calling
//				//	this could cause a crash!!!
////				Log.e(tag, "About to call onPostExecute() outside of the ASyncTask! Get ready for something bad to happen!");
////				m_task.onPostExecute(null);
//			}
//		}

		m_task = new InspectorAsyncTask2();
		m_task.execute();

	} // init_from_db()


	/***************************
	 * Helper to convert the given number to a nice string for
	 * display in the inspector as a weight, distance, etc.
	 *
	 * @param f		The FLOAT to turn into a string
	 * @return		A nice string, or an appropriate message
	 * 				if the user skipped this value.
	 */
	private String intfloat_to_string (float f) {
		if (f < 0)
			return getString (R.string.inspector_skipped_value);
		return new DecimalFormat("#.###").format(f);
	}

	/***************************
	 * Helper to convert the given number to a nice string for
	 * display in the inspector as a weight, distance, etc.
	 *
	 * @param i		The INT to turn into a string
	 *
	 * @return		A nice string, or an appropriate message
	 * 				if the user skipped this value.
	 */
	private String intfloat_to_string (int i) {
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
	void create_set_layout (SetLayout layout_values) {
//		Log.d(tag, "entering create_set_layout()");
		if (!m_async_layout_complete) {
			init_layout(layout_values.data.millis);
		}

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout set_ll = (LinearLayout) inflater
				.inflate(R.layout.inspector_set, m_main_ll, false);

		// Put the day above the info rectangle
		TextView date_tv = (TextView) set_ll.findViewById(R.id.inspector_set_date_tv);
		MyCalendar cal = new MyCalendar(layout_values.data.millis);
		date_tv.setText(cal.print_date(this));

		fill_set_layout(layout_values.data, set_ll);

		// Make this respond to long clicks.  Use the set ID.
		LinearLayout clickable_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_ll);
		clickable_ll.setOnLongClickListener(this);
		clickable_ll.setId(layout_values.data._id);		// Needs the ID, too!

		// The id is how we access the layout!  VERY important!
		set_ll.setId(layout_values.data._id);

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

	}  // create_set_layout (layout_values)


	/**************************
	 * After the layout has been inflated, use this to populate
	 * it with data.
	 *
	 * @param	layout_values	A SetLayout that's already filled
	 * 							in with values for this exercise set.
	 *
	 * @param	layout			The LinearLayout to display this
	 * 							set (already inflated from
	 * 							R.layout.inspector_set).  It'll be
	 * 							modified as appropriate for this set.
	 */
	private void fill_set_layout(SetData data,
								LinearLayout layout) {
		/** used to determine whether or not to draw a divider bar */
		boolean set_bar = false;

		// The date and time of this set.
		setup_date (data, layout);

		if (setup_reps(data, layout))
			set_bar = true;

		if (setup_weight (data, layout, set_bar))
			set_bar = true;

		if (setup_level (data, layout, set_bar))
			set_bar = true;

		if (setup_cals (data, layout, set_bar))
			set_bar = true;

		if (setup_dist (data, layout, set_bar))
			set_bar = true;

		if (setup_time (data, layout, set_bar))
			set_bar = true;

		if (setup_other (data, layout, set_bar))
			set_bar = true;

		setup_stress (data, layout);

		setup_notes (data, layout);
	} // fill_set_layout (layout_values, layout)


	/**************************
	 * Call this to set the correct message for the
	 * m_desc_tv (depends on the order we're displaying).
	 */
	void set_order_msg() {
		// Make ch
		if (m_desc_tv == null) {
			m_desc_tv = (TextView) findViewById(R.id.inspector_description_tv);
		}

		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(this);
		boolean prefs_oldest_order =
				prefs.getBoolean(getString(R.string.prefs_inspector_oldest_first_key),
								false);
		m_desc_tv.setText(prefs_oldest_order ?
							R.string.inspector_oldest_first_msg :
							R.string.inspector_newest_first_msg);
	} // init_ui()


	/********************
	 * Called the first time that make_set_layout() is called.
	 * This sets up all the general stuff that's germain to
	 * every set layout.
	 *
	 * todo:
	 * 		The param date_in_millis is NOT used!!!
	 */
	protected void init_layout (long date_in_millis) {
		Log.d(tag, "entering init_layout().");
		if (m_async_layout_complete) {
			return;
		}

//		if (m_task == null) {
//			Log.e(tag, "m_task is null in init_layout()!");
//		}

		Log.d(tag, "init_layout: tabula rasa!");

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
//		m_layout_complete = true;		Moved to the asynctask
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
	private void setup_date (SetData vals, LinearLayout set_ll) {
		String str = "";
		MyCalendar set_date = new MyCalendar(vals.millis);

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
	private boolean setup_reps (SetData vals,
								LinearLayout set_ll) {
		LinearLayout reps_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_reps_ll);

		if (m_ex_data.breps) {
			String data_str = intfloat_to_string(vals.reps);
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
	private boolean setup_weight (SetData vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout weight_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_weight_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_weight_bar);

		if (m_ex_data.bweight) {
			TextView weight_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_weight_label);
			String weight_label_str = getString (R.string.inspector_set_weight_label,
					(Object[]) new String[] {m_ex_data.weight_unit});
			weight_label_tv.setText(weight_label_str);

			TextView weight_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_weight_data);
			weight_data_tv.setText(intfloat_to_string(vals.weight));
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
	private boolean setup_level (SetData vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout level_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_level_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_level_bar);

		if (m_ex_data.blevel) {
			String data_str = intfloat_to_string(vals.levels);
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
	 */
	private boolean setup_cals (SetData vals,
							LinearLayout set_ll, boolean set_bar) {
		LinearLayout cals_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_calorie_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_calorie_bar);

		if (m_ex_data.bcals) {
			String data_str = intfloat_to_string(vals.cals);
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
	 */
	private boolean setup_dist (SetData vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout dist_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_dist_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_dist_bar);

		if (m_ex_data.bdist) {
			// Unit of Distance
			TextView dist_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_dist_label);
			String dist_label_str = getString (R.string.inspector_set_dist_label,
											m_ex_data.dist_unit);
			dist_label_tv.setText(dist_label_str);
			if (m_ex_data.significant == DatabaseHelper.EXERCISE_COL_DIST_NUM) {
				dist_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = intfloat_to_string(vals.dist);
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
	 */
	private boolean setup_time (SetData vals,
							LinearLayout set_ll, boolean set_bar) {
		LinearLayout time_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_time_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_time_bar);

		if (m_ex_data.btime) {
			// Unit of Time
			TextView time_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_time_label);
			String time_label_str = getString (R.string.inspector_set_time_label,
											m_ex_data.time_unit);
			time_label_tv.setText(time_label_str);
			if (m_ex_data.significant == DatabaseHelper.EXERCISE_COL_TIME_NUM) {
				time_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = intfloat_to_string(vals.time);
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
	 */
	private boolean setup_other (SetData vals,
							LinearLayout set_ll, boolean set_bar) {
		LinearLayout other_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_other_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_other_bar);

		if (m_ex_data.bother) {
			TextView other_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_other_label);
			String other_label_str = getString (R.string.inspector_set_other_label,
												m_ex_data.other_title,
												m_ex_data.other_unit);
			other_label_tv.setText(other_label_str);
			if (m_ex_data.significant == DatabaseHelper.EXERCISE_COL_OTHER_NUM) {
				other_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = intfloat_to_string(vals.other);

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
	 */
	private void setup_stress (SetData vals,
							LinearLayout set_ll) {
		ImageView stress_data_iv = (ImageView) set_ll.findViewById(R.id.inspector_set_stress_data);

		switch (vals.cond) {
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
	 */
	private void setup_notes (SetData vals,
							LinearLayout set_ll) {
		TextView notes_tv = (TextView) set_ll.findViewById(R.id.inspector_set_notes_tv);
		TextView notes_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_notes_label_tv);
		View bar = set_ll.findViewById(R.id.inspector_set_notes_bar);

		if ((vals.notes != null) && (vals.notes.length() > 0)) {
			// Make sure it's visible!
			bar.setVisibility(View.VISIBLE);
			notes_label_tv.setVisibility(View.VISIBLE);
			notes_tv.setVisibility(View.VISIBLE);

			// There's a note!  Display it.
			notes_tv.setText(vals.notes);
		}
		else {
			// No notes, so display nothing.
			bar.setVisibility(View.GONE);
			notes_label_tv.setVisibility(View.GONE);
			notes_tv.setVisibility(View.GONE);
		}
	} // notes


	/************************
	 * Called during onProgressUpdate() or anytime after
	 * the database is completely loaded.  This causes the
	 * UI to "catch up" to whatever data has been loaded.
	 */
	protected void catchup() {
		Log.d(tag, "Entering catchup()");

//		if (m_task == null) {
//			Log.e(tag, "Trying to catchup without an ASyncTask!");
//			return;		// Nothing to catch up to!
//		}

		if (m_layout_list == null) {
			Log.e(tag, "m_layout_list is NULL while catching up!");
			return;
		}

		if (m_layout_list.size() == 0) {
			init_layout(0);
			return;
		}

// todo
//				HEY!  I don't think this will ever be called now
//				that I made some changes!
		if (!m_async_layout_complete) {
//			init_layout(layout_values.data.millis);
			init_layout(0);
		}

		// strategy:
		//	I'm assuming that we're adding the child nodes in
		//	the correct order.
		//
		//	Find where we are in the layout, and add successive
		//	items of m_task.m_layout_list until we max out.
		//
		int num_children = m_main_ll.getChildCount();
//		Log.d(tag, "catchup(): num_children = " + num_children + ", layout_list.size() = " + m_task.m_layout_list.size());
		while (num_children < m_layout_list.size()) {
			create_set_layout(m_layout_list.get(num_children));
			num_children++;
		}

	} // catchup()


	/************************
	 * This should be called after all the data has been
	 * loaded and we're caught up.  This does the finishing
	 * touches (like scrolling to the correct datum and
	 * stopping the dialog).
	 *
	 * side effects:
	 * 	m_db_dirty		set to false
	 */
	protected void finish_ui() {
		trim_date_labels();
		scroll_to_child (m_set_id);
		stop_progress_dialog();
		m_db_dirty = false;
	}


		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//	Classes
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

//	/**************************************************
//	 * Operates in the background to load the UI stuff from our
//	 * database, which can take a while.
//	 *
//	 * The types are: <params, progress, result>
//	 */
//	@Deprecated
//	static class InspectorASyncTask
////					extends AsyncTask <Void, SetLayout, Void> {
//					extends AsyncTask <Void, Void, Void> {
//
//		static final String tag = "InspectorASyncTask";
//
//		/** TRUE while data is being loaded from the DB */
////		private boolean m_loading = false;
//
//		/** Will be TRUE when the ASyncTask has finished. */
//		@Deprecated
//		boolean m_done = false;
//
//		/**
//		 * The Activity that is using this ASyncTask.
//		 * This static class may ONLY access the activity
//		 * through this data member.
//		 * <p>
//		 * NOTE: Make sure this is not NULL before using!!!
//		 * (Actually, this is not necessary. Google promises that
//		 * this will always be valid when done in a UI thread.)
//		 */
//		@Deprecated
//		private InspectorActivity2 m_activity = null;
//
//		/** Holds all info about this exercise. */
//		@Deprecated
//		public ExerciseData m_ex_data = null;
//
//		/** The name of this exercise */
//		@Deprecated
//		public String m_ex_name;
//
//		/**
//		 * The order that we should sort the workout sets.
//		 * True means Oldest First.  False means most recent first.
//		 */
////		public boolean m_prefs_oldest_order = true;
//
//		/** The number of sets for this exercise. */
//		@Deprecated
//		public int m_num_sets = -1;
//
//		/**
//		 * This is IT. This is the list that holds all the layouts
//		 * (which are sets + an order).  This is filled out progressively
//		 * during doInBackground() and tapped during onProgressUpdate()
//		 * and catchup().
//		 */
//		@Deprecated
//		public ArrayList<SetLayout> m_layout_list = null;
//
//
//		/***************
//		 * Constructor
//		 *
//		 * Needs a reference to the Activity that's creating
//		 * this ASyncTask.  It's how this static class
//		 * communicates with that Activity.
//		 *
//		 * input:
//		 * 		activity		The Activity that wants this ASyncTask to
//		 * 					run.
//		 *
//		 * 		ex_name		The name of the exercise we're inspecting.
//		 */
//		public InspectorASyncTask (InspectorActivity2 activity, String ex_name) {
////			Log.v(tag, "entering constructor, count = " + m_instance_counter +
////				", id = " + this.toString());
//			m_layout_list = new ArrayList<SetLayout>();
//			m_ex_name = ex_name;
//			attach (activity);
//		} // constructor
//
//
//		//---------------------
//		//	Initializations here.
//		//
//		@Override
//		protected void onPreExecute() {
//			if (m_activity == null) {
//				Log.e (tag, "m_activity is NULL!!! We're about to make a lot of errors!");
//			}
//
////			m_loading = true;
//			m_done = false;
//
//			m_activity.m_async_layout_complete = false;
//
//			// Moved to Activity
////			start_progress_dialog(R.string.loading_str);
//
//			// Load in our preferences.
////			SharedPreferences prefs =
////				PreferenceManager.getDefaultSharedPreferences(m_activity);
////			m_prefs_oldest_order =
////				prefs.getBoolean(m_activity.getString(R.string.prefs_inspector_oldest_first_key),
////								false);
//
//
//		} // onPreExecute
//		//-------------------
//
//		@Override
//		protected Void doInBackground(Void... not_used) {
//			// todo: is this necessary? We just set it in
//			//	onPreExecute()!
//			//
//			// Mark that loading has begun.
////			m_loading = true;
//
//			SQLiteDatabase db = null;
//			try {
//				if (WGlobals.g_db_helper == null) {
//					Log.e(tag, "doInBackground(): g_db_helper is NULL!");
//					throw new SQLiteException("doInBackground is starting up, yet WGlobals.g_db_helper is null!");
//				}
//				db = WGlobals.g_db_helper.getReadableDatabase();
//
//				// Read in all the info we need about this
//				// exercise.
//				m_ex_data = DatabaseHelper.getExerciseData(db, m_ex_name);
//
//				SharedPreferences prefs =
//				PreferenceManager.getDefaultSharedPreferences(m_activity);
//				boolean prefs_oldest_order =
//				prefs.getBoolean(m_activity.getString(R.string.prefs_inspector_oldest_first_key),
//								false);
//
//				// Get a cursor for the sets.  Then loop through
//				// them one by one, creating a layout for each.
//				// This requires filling in a SetLayout class
//				// to hold the appropriate (and relevant) data
//				// for that layout.
//				Cursor set_cursor = null;
//				try {
//					set_cursor = DatabaseHelper
//									.getAllSets(db, m_ex_name,
//												prefs_oldest_order);
//					m_num_sets = set_cursor.getCount();
//
//					int counter = 0;
//					while (set_cursor.moveToNext()) {
//
//						SetLayout layout_values = new SetLayout();
//						layout_values.data = DatabaseHelper.getSetData(set_cursor);
//						layout_values.order = counter;
//
//						m_layout_list.add(layout_values);
////						publishProgress(layout_values);
//						publishProgress();
//						counter++;
//					}
//
//
//					// For the case where there are ZERO sets,
//					if (m_num_sets == 0) {
////						publishProgress((SetLayout)null);
//						publishProgress();
//					}
//
//				} // end of set_cursor
//				catch (SQLiteException e) {
//					e.printStackTrace();
//				}
//				finally {
//					if (set_cursor != null) {
//						set_cursor.close();
//						set_cursor = null;
//					}
//				}
//
//
//			} // end of m_db usage
//			catch (SQLiteException e) {
//				e.printStackTrace();
//			}
//			finally {
//				if (db != null) {
//					db.close();
//					db = null;
//				}
//				m_db_dirty = false;
////				m_loading = false;		// Done loading!
//			}
//
//			return null;
//		} // doInBackground(...)
//
//
//		//-------------------
//		//	Okay, gotta be careful with this one.  It happens in the
//		//	UI thread, not the same thread as doInBackground().  That
//		//	means that if any variables passed into this can be changed
//		//	in doInBackground(), then this is a BIG problem!
//		//
//		//	input:
//		//		set_array		This is an array of SetLayouts that
//		//						were filled in by the background
//		//						thread via doInBackground().  It has
//		//						only ONE element, the most recently
//		//						created SetLayout.  Let's use it
//		//						to make a new exercise set layout!
//		//
//		@Override
////		protected void onProgressUpdate(SetLayout... set_array) {
//		protected void onProgressUpdate(Void... not_used) {
//			if (m_activity == null) {
//				Log.e(tag, "onProgressUpdate() can't find the Activity!!! Can't do anything, so I'm simply returning.");
//				return;
//			}
//
//			m_activity.catchup();
//
////			super.onProgressUpdate(set_array);	// todo: is this nec.?
////			super.onProgressUpdate();	// todo: is this nec.?
//
////			if (set_array[0] == null) {
////				m_activity.init_layout(0);
////			}
////			else {
////				m_activity.make_set_layout (set_array[0]);
////			}
//		} // onProgressUpdate (arg0)
//
//
//		//-------------------
//		//	Note that this is called ONCE and once only!  This class will
//		//	self-destruct once it's done (the ASyncTask doesn't hang
//		//	around).
//		//
//		@Override
//		protected void onPostExecute(Void not_used) {
//			Log.d(tag, "onPostExecute(), id = " + this);
//			if (m_activity == null) {
//				Log.e(tag, "onPostExecute() can't find the Activity!!! This is really bad news.");
//			}
//
//			m_activity.catchup();
//
//			m_activity.finish_ui();
//
//			// Finish our UI
////			m_activity.trim_date_labels();
////			m_activity.scroll_to_child (m_activity.m_set_id);
////			m_activity.stop_progress_dialog();
////			m_db_dirty = false;
//
//			m_done = true;	// finally!
//		} // onPostExecute()
//
//
//		/***************
//		 * Connects this task to an Activity, allowing
//		 * this static class to communicate with that
//		 * Activity (so it can get the data we're reading
//		 * from the database!).
//		 *
//		 * @param activity	The Activity that wants to
//		 * 					use the data.
//		 */
//		public void attach (InspectorActivity2 activity) {
//			m_activity = activity;
//		}
//
//		/***************
//		 * Removes our connection to whatever Activity
//		 * we're attached to (or does nothing if we're
//		 * not attached to anything).
//		 *
//		 * This is an important call when the Activity
//		 * goes away (like during an orientation change)
//		 * so that we're not using invalid pointers!
//		 */
//		public void detach() {
////			Log.d(tag, "entering detach(), id = " + this.toString());
//			m_activity = null;
//		}
//
//		/****************
//		 * Please call this when the connecting Activity
//		 * goes away for good.  This will free up lots of
//		 * resources!
//		 */
//		public void kill() {
////			Log.d(tag, "entering kill(), id = " + this.toString());
//
//			// todo	garbage collect
//
//			m_activity = null;
//		}
//
//		/*****************
//		 * Call this to see if the ASyncTask is complete.
//		 *
//		 * The done state is reset (to false) when onPreExecute()
//		 * is called, and terminated (true) during onPostExecute().
//		 */
//		public boolean isDone() {
//			return m_done;
//		}
//
//
//	} // class InspectorASyncTask
//

	/**************************************************
	 * New Version for my AsyncTask.  I realized that I don't really
	 * need to do a static--my users are fine if I restart during
	 * a configuration change.  All I need to do is display a waiting
	 * dialog while the data is loaded and the UI is drawn.
	 *
	 * When the parent Activity stops or destroys itself, then is
	 * MUST call AsyncTask.cancel(true).  This signals the AsyncTask
	 * to stop processing.
	 *
	 * 	Method:
	 * 	~~~~~~
	 *
	 *	The data is retrieved from the DB in the doInBackground()
	 *	method.
	 *
	 *	The UI is built during the publish() / onProgressUpdate() method
	 *	and then completed in onPostExecute().
	 *
	 * 	In onPostExecute(), when the layout is completed, then a
	 * 	flag is set to indicate that the layout is done.
	 *
	 * 	Before any variable of the parent Activity is used,
	 * 	we MUST remember to check to see if this task has been
	 *  CANCELLED.  If it is, then stop everything.
	 *
	 * Side Effects:
	 * ~~~~~~~~~~~~
	 * m_async_layout_complete	Set to false when begun and set to
	 * 							true when done.
	 *
	 * m_ex_data		Filled in to hold all the information about this
	 * 				exercise.
	 *
	 * m_num_sets	Changed to indicate the number of sets for this
	 * 				exercise.
	 *
	 * m_layout_list		The main list for all the set layouts.  Filled
	 * 					in here and used throughout the Activity
	 * 					(primarily in the catchup() method).
	 */
	class InspectorAsyncTask2 extends AsyncTask<Void, Void, Void> {

		private final static String tag = "InspectorAsyncTask2";

		/*****************
		 * Any initializations are done here.  The Activity should
		 * really be running, but I'm putting a test here for
		 * completeness.
		 */
		@Override
		protected void onPreExecute() {
			if (isCancelled() == true) {
				Log.v(tag, "onPreExecute() called but the Activity has Terminated!");
				return;
			}

			start_progress_dialog();

			// Create the Layout List if necessary.
			if (m_layout_list == null) {
				m_layout_list = new ArrayList<SetLayout>();
			}
			m_layout_list.clear();

			// Just starting.
			m_async_layout_complete = false;
		} // onPreExecute


		/*****************
		 *
		 */
		@Override
		protected Void doInBackground(Void... not_used) {
			if (isCancelled()) {
				Log.v(tag, "doInBackground(), but cancelled at the beginning. Aborting.");
				return null;
			}

			SQLiteDatabase db = null;
			try {
				if (WGlobals.g_db_helper == null) {
					Log.e(tag, "doInBackground(): g_db_helper is NULL!");
					throw new SQLiteException("doInBackground is starting up, yet WGlobals.g_db_helper is null!");
				}
				db = WGlobals.g_db_helper.getReadableDatabase();

				// Read in all the info we need about this
				// exercise.
				m_ex_data = DatabaseHelper.getExerciseData(db, m_ex_name);
				if (m_ex_data == null) {
					Log.e(tag, "Error finding out about this exercise in doInBackground(). Aborting!");
					return null;
				}

				// Find out whether to list the sets oldest or newest first.
				SharedPreferences prefs =
					PreferenceManager.getDefaultSharedPreferences(InspectorActivity2.this);
				boolean prefs_oldest_order =
				prefs.getBoolean(getString(R.string.prefs_inspector_oldest_first_key),
								false);

				// Get a cursor for the sets.  Then loop through
				// them one by one, creating a layout for each.
				// This requires filling in a SetLayout class
				// to hold the appropriate (and relevant) data
				// for that layout.
				Cursor set_cursor = null;
				try {
					set_cursor = DatabaseHelper
									.getAllSets(db, m_ex_name,
												prefs_oldest_order);
					m_num_sets = set_cursor.getCount();

					int counter = 0;
					while (set_cursor.moveToNext()) {

						// Quick check: has the Activity been destroyed?
						if (isCancelled()) {
							return null;
						}

						SetLayout layout_values = new SetLayout();
						layout_values.data = DatabaseHelper.getSetData(set_cursor);
						layout_values.order = counter;

						m_layout_list.add(layout_values);
//						publishProgress(layout_values);
						publishProgress();
						counter++;
					}


					// For the case where there are ZERO sets,
					if (m_num_sets == 0) {
//						publishProgress((SetLayout)null);
						publishProgress();
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
				if (db != null) {
					db.close();
					db = null;
				}
			}

			return null;
		} // doInBackground(...)


		/*****************
		 * Right now I just call catchup().  Data is passed through
		 * data members (side effect).
		 */
		@Override
		protected void onProgressUpdate(Void... not_used) {
			if (isCancelled() == true) {
				Log.v(tag, "onProgressUpdate() called but the Activity has Terminated!");
				return;
			}
			catchup();
		}

		/*****************
		 */
		@Override
		protected void onPostExecute(Void not_used) {
			if (isCancelled()) {
				Log.v(tag, "onPostExecute() called but the Activity has Terminated!");
				return;
			}

			catchup();
			finish_ui();

			// Indicate that the UI operation is complete.
			m_async_layout_complete = true;
		}

	} // class InspectorAsyncTask2


}
