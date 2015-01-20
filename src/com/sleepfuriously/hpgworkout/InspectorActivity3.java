package com.sleepfuriously.hpgworkout;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * This is my 3rd writing of this Activity for the Inspector.
 * I'm doing this major change because I'm switching over to
 * a more MVC way of implementing this functionality.
 * <p>
 * The InspectorActivity3 is primarilyboth the View AND the
 * Controller.  The Model is held in the {@link InspectorModel}
 * class.
 * <p>
 * The MVC View methods will be in a seperate section,
 * and they will all start with v_.
 *<p>
 * Note that I may try to seperate the View and the Controller
 * in the future, but in Android it's kind of difficult.
 */
public class InspectorActivity3
				extends BaseDialogActivity
				implements
						OnLongClickListener {

	//------------------
	//	Constants
	//------------------

	private static final String tag = "InspectorActivity3";

	/** Id for the menu item to change the order that sets are listed */
	protected static final int MENU_ID_ORDER = 2;

	//------------------
	//	Widget Data
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

	/**
	 * This is the inflater that's used to create every single
	 * set layout.  Making it a class var is part of the
	 * optimization process.  Constructed in onCreate().
	 */
	LayoutInflater m_set_inflater = null;

	/** This TextView tells the user the current order that sets are displayed */
	TextView m_desc_tv = null;


	//------------------
	//	Controller Data
	//------------------

	/**
	 * External flag.  Other Activities use this to tell
	 * the Inspector that it needs to refresh its data.
	 *
	 * When the Inspector has reloaded, this is set to false.
	 *
	 * todo
	 * 		This is NOT used!!!  (and poorly named)
	 */
	public static boolean m_db_dirty = true;

	/**
	 * This flag is used to signal to the Grid whether or not
	 * it needs to reload when this Activity exits.
	 * todo
	 * 	Make this a better name.
	 */
	private static boolean m_signal_grid_that_database_changed = false;

	/**
	 * The id for the exercise set to center on.  This is initially
	 * seletected by the user by long-clicking on the grid, but
	 * it is reset whenever the user selects a set for editing.
	 * <p>
	 * If this is a valid id number, then the
	 * set with the given id should be scrolled to when this Activity
	 * pops up the first time.
	 *<p>
	 * If the number is invalid (-1), then scroll to the top (or don't
	 * scroll at all).
	 */
	private int m_set_id = -1;

	/** Tells if we're in landscape mode or not. */
	private boolean m_landscape = false;

	/** Indicates the order to display the sets */
	private boolean m_oldest_first = false;

	/**
	 * Holds instances of the AsyncTask.  It'd be nice to be set
	 * to null when completed.
	 */
	private AsyncTask<Void, Void, Void> m_task = null;

	/** A hack to get around scoping rules for v_scroll_to_child(id). */
	protected static int s_id;


	//--------------------
	//	Exercise Data
	//--------------------

	/**
	 * This is IT. This is the list that holds all the layouts
	 * (which are sets + an order).  This is filled out progressively
	 * during doInBackground() and tapped during onProgressUpdate()
	 * and catchup() via the AsyncTask.
	 */
	protected ArrayList<SetLayout> m_layout_list = null;

	/**
	 * This is the Model for our MVC pattern.  All interaction
	 * with data should be through this (pretty much read-only
	 * for this Activity).
	 */
	protected InspectorModel m_data = null;

	//--------------------
	//	Controller Methods
	//--------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inspector);


		// Get our Intent and fill in the info that was passed
		// from the Grid to here (via ExerciseTabHostActivity).
		// The intent will hold the ID of any specific exercise
		// set to center on.  If none specified, then use the
		// default of -1.
		Intent itt = getIntent();
		String ex_name = itt.getStringExtra(ExerciseTabHostActivity.KEY_NAME);
		if (ex_name == null) {
			Log.e(tag, "Cannot find the exercise name in onCreate()!!!");
			return;
		}
		m_set_id = itt.getIntExtra(ExerciseTabHostActivity.KEY_SET_ID, -1);

		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(this);
		m_oldest_first =
				prefs.getBoolean(getString(R.string.prefs_inspector_oldest_first_key),
								false);

		// Initialize data members that are universal to this
		// Activity.
		m_db_dirty = true;	// True for first time.
		m_landscape = (get_screen_orientation() ==
							Configuration.ORIENTATION_LANDSCAPE);
		m_data = new InspectorModel(ex_name, m_oldest_first);
		m_layout_list = new ArrayList<SetLayout>();

		// Create the UI that will ALWAYS be there for this Activity.
		m_main_ll = (LinearLayout) findViewById(R.id.inspector_all_sets_ll);
		if (m_landscape)
			m_hsv = (HorizontalScrollView) findViewById(R.id.inspector_hsv);
		else
			m_sv = (ScrollView) findViewById(R.id.inspector_sv);

		m_desc_tv = (TextView) findViewById(R.id.inspector_description_tv);
		v_set_order_msg();
		m_desc_tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				// Toggle the display order.  This is called when the user
				// clicks on it.
				v_toggle_set_order();
			}
		});


		// Preloading stuff to optimize the layout creator.
		m_set_inflater = getLayoutInflater();


		// Start building the View
		m_task = new BuildSetListAsyncTask();
		((BuildSetListAsyncTask) m_task).execute();
		start_progress_dialog();

	} // onCreate (.)


	/**************************
	 * Here I MUST tell the AsyncTask that we're going away.
	 * And while at it, I should stop any dialogs that are
	 * running too.
	 */
	@Override
	protected void onDestroy() {
		Log.d(tag, "onDestroy()");

		if (m_task != null) {
			// Tell the AsyncTask to stop because the Activity
			// is going away.  This should work regardless of
			// which type of AsyncTask is running.
			m_task.cancel(true);
		}

		// Kill a dialog if running (should already be killed
		// when we tell m_task to cancel, but this is just in
		// case).
		//	todo: remove this bit
		if (is_progress_dialog_active()) {
			stop_progress_dialog();
		}
		super.onDestroy();
	} // onDestroy()


	/**************************
	 * Need to tell the tab host a few
	 * things before exiting.
	 */
	@Override
	public void onBackPressed() {
		if ((ExerciseTabHostActivity.m_dirty) || (m_signal_grid_that_database_changed)) {
			tabbed_set_result(RESULT_OK);
		}
		else
			tabbed_set_result(RESULT_CANCELED);
		finish();
	} // onBackPressed()

	/**************************
	 *
	 */
	@Override
	public boolean onLongClick(View v) {
		Intent itt;

		// If they clicked on one of the sets, go ahead and
		// start the EditExerciseActivity for a result.
		if (v.getClass() == LinearLayout.class) {
			WGlobals.play_long_click();
			itt = new Intent (this, EditSetActivity.class);
			itt.putExtra(EditSetActivity.ID_KEY, v.getId());
			startActivityForResult(itt, WGlobals.EDITSETACTIVITY);
		}

		return false;
	} // onLongClick (v)


	/**************************
	 * When the EditSetActivity returns, this is called.
	 *
	 * @param resultCode:
	 * 			- CANCEL		do nothing
	 * 			- OK			then:
	 * 					- if no Intent, a set was deleted. Remove
	 * 					  that view.
	 * 					- Intent exists, that set was changed, so
	 * 					  modify that view.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
									Intent itt) {
		/** Holds the View for this set */
		LinearLayout layout = null;

		// Start by checking for the easy case.
		if (resultCode == RESULT_CANCELED) {
			return;
		}

		// Check for an illegal requestCode (make sure we got this
		// from the EditSetActivity)
		if (requestCode != WGlobals.EDITSETACTIVITY) {
			Log.e(tag, "Unknown requestCode in onActivityResult!!!");
			return;
		}

		// The set has been edited/deleted.
		if (itt == null) {
			Log.e(tag, "Intent data is NULL in onActivityResult()! Aborting!");
			return;
		}

		// Get the ID of the exercise set that was modified or
		// deleted by the user (it may or may not have been the
		// same ID that was used to start this Activity).
		//
		// I'm going ahead and making this the new main ID.  Kind
		// of makes sense for a user's perspective.
		m_set_id = itt.getIntExtra(EditSetActivity.ID_KEY, -1);
		switch (resultCode) {
			case EditSetActivity.RESULT_DELETED:
				// The workout set was deleted.  Remove the
				// View from our Activity.
				layout = (LinearLayout) m_main_ll.findViewById(m_set_id);
				if (layout == null) {
					Log.e(tag, "Can't find the deleted view in onActivityResult! Aborting!");
					return;	// No need to set any flags
				}
				m_main_ll.removeView(layout);
				break;

			case EditSetActivity.RESULT_TIME_CHANGED:
				// todo
				Log.e(tag, "need to implement this!!!");
				break;

				//----
				// Falls through... ??

			case RESULT_OK:
				// Redraw the set as the data has changed.
				m_task = new ChangeSetAsyncTask();
				m_task.execute();
				start_progress_dialog();
				break;

			default:
				Log.e(tag, "Illegal value for resultCode in onActivityResult! Aborting!");
				return;
		}

		// Indicate to the other Activities that something has changed.
		// todo: this could be redundant!
		ExerciseTabHostActivity.m_dirty = true;
		GraphActivity.m_db_dirty = true;
		AddSetActivity.m_reset_widgets = true;
		m_signal_grid_that_database_changed = true;


	} // onActivityResult (requestCode, resultCode, data)


	/**************************
	 * Initialize the menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ID_ORDER, 0, R.string.null_string);
		return super.onCreateOptionsMenu(menu);
	}

	/**************************
	 * Make sure that the correct message is displayed in
	 * the menu.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_ID_ORDER).setTitle(m_oldest_first ?
							R.string.inspector_menu_newest_first_label :
							R.string.inspector_menu_old_first_label);
		return super.onPrepareOptionsMenu(menu);
	} // onPrepareOptionsMenu(menu)

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case MENU_ID_ORDER:
				v_toggle_set_order();
				break;

			default:
				Log.e (tag, "Illegal id: " + id + ", in onOptionsItemSelected!");
				break;
		}
		return super.onOptionsItemSelected(item);
	} // onOptionsItemSelected(item)



	//------------------
	//	View Methods
	//------------------


	/**************************
	 * Call this when the user wants to reverse the order of the displayed
	 * sets.  This will start a new AsyncTask to redisplay/redraw all the
	 * exercise sets.
	 *
	 * preconditions:
	 *	m_oldest_first		correctly holds the current order
	 *
	 * side effects:
	 * 	m_oldest_first		Will be toggled.
	 * 	m_data 				Will have it's order toggled, too.
	 *  -The preferences will indicate the new change
	 */
	private void v_toggle_set_order() {
		// First change our data.
		m_oldest_first = !m_oldest_first;
		m_data.set_oldest_first(m_oldest_first);

		// Save it.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putBoolean(getString(R.string.prefs_inspector_oldest_first_key),
								m_oldest_first)
						.commit();

		// Show the default at the top.
		m_set_id = -1;
		v_set_order_msg();

		// Rebuild the set list
		m_task = new BuildSetListAsyncTask();
		m_task.execute();
		start_progress_dialog();
	} // v_toggle_set_order()


	/**************************
	 * Call this to set the correct message for the
	 * m_desc_tv (depends on the order we're displaying).
	 *
	 * preconditions:
	 * 	m_desc_tv		Is ready to be changed.
	 */
	private void v_set_order_msg() {
		m_desc_tv = (TextView) findViewById(R.id.inspector_description_tv);
		m_desc_tv.setText(m_oldest_first ?
							R.string.inspector_oldest_first_msg :
							R.string.inspector_newest_first_msg);
	} // v_set_order_msg()


	/***********************
	 * Scrolls the ScrollView so that the child layout
	 * in question is at the top of the screen.
	 *
	 * preconditions:
	 * 	m_main_ll	Ready.
	 * 	m_hsv		Ready.
	 * 	m_sv			Ready.
	 *
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
	private void v_scroll_to_child (int id) {
		// WARNING!  This is a HACK to get around scoping rules!
		s_id = id;

		// Set the scroll to the right value.
		if (m_landscape)
			m_hsv = (HorizontalScrollView) findViewById(R.id.inspector_hsv);
		else
			m_sv = (ScrollView) findViewById(R.id.inspector_sv);

		// For the times we need to scroll to a given child of the
		// scrollview.
		if (id == -1) {
			if (m_landscape)
				m_hsv.scrollTo(0, 0);	// Go to the left.
			else
				m_sv.scrollTo(0, 0);		// Go to the top.
			return;
		}

		// Make it scroll, but first we have to wait for
		// everything to be set up.
		if (m_landscape) {
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
						if (s_id == child.getId()) {
							break;
						}
						height_of_views += child.getHeight();
					}

					m_sv.scrollTo(0, height_of_views);
				}
			});
		}
	} // v_scroll_to_child (id)


	/***************************
	 * Helper to convert the given number to a nice string for
	 * display in the inspector as a weight, distance, etc.
	 *
	 * @param f		The FLOAT to turn into a string
	 * @return		A nice string, or an appropriate message
	 * 				if the user skipped this value.
	 */
	private String v_intfloat_to_string (float f) {
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
	private String v_intfloat_to_string (int i) {
		if (i == -1) {
			return getString(R.string.inspector_skipped_value);
		}
		return ("" + i);
	}


	/********************
	 * todo
	 * 		This is possibly where lots of optimizations can
	 * 		take place.
	 *
	 * Takes all the data for a set that was loaded in by the
	 * ASyncTask and puts those values into a new layout.
	 *
	 * Now this is filled in along the way, making a pleasing UI
	 * while the data is loading.  But this means that there
	 * will need to be a little bit of sorting as we figure out
	 * where to put the layout that was sent in, which is O(n).
	 *
	 * preconditions:
	 * 		m_main_ll		Needs to be ready to receive children.
	 *
	 * 		m_set_inflator	Ready to rock and roll.
	 *
	 * @param layout_values		The data for this set that was
	 * 							loaded from the DB.
	 *
	 * Postconditions:
	 * 		- Each layout SHOULD have its order also listed within
	 * 		it's TAG parameter.  Could be useful some time!
	 */
	private void v_create_set_layout (SetLayout layout_values) {

		LinearLayout set_ll = (LinearLayout) m_set_inflater
				.inflate(R.layout.inspector_set, m_main_ll, false);

		// Put the day above the info rectangle
		TextView date_tv = (TextView) set_ll.findViewById(R.id.inspector_set_date_tv);
		MyCalendar cal = new MyCalendar(layout_values.data.millis);
		date_tv.setText(cal.print_date(this));

		v_fill_set_layout(layout_values.data, set_ll);

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
	 * When it's discovered that there are no sets at all,
	 * use this method to inform the user that there's nothing
	 * to look at.
	 *
	 * preconditions:
	 * 		m_main_ll		Already cleared.
	 */
	private void v_display_no_sets() {
		TextView title_tv = (TextView) findViewById(R.id.inspector_title_tv);
		if (m_layout_list.size() == 0) {
			title_tv.setText(R.string.inspector_empty);
		}
		else {
			title_tv.setVisibility(View.GONE);
		}
	} // v_display_no_sets()


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
	private void v_fill_set_layout(SetData data,
								LinearLayout layout) {
		/** used to determine whether or not to draw a divider bar */
		boolean set_bar = false;

		// The date and time of this set.
		v_setup_date (data, layout);

		if (v_setup_reps(data, layout))
			set_bar = true;

		if (v_setup_weight (data, layout, set_bar))
			set_bar = true;

		if (v_setup_level (data, layout, set_bar))
			set_bar = true;

		if (v_setup_cals (data, layout, set_bar))
			set_bar = true;

		if (v_setup_dist (data, layout, set_bar))
			set_bar = true;

		if (v_setup_time (data, layout, set_bar))
			set_bar = true;

		if (v_setup_other (data, layout, set_bar))
			set_bar = true;

		v_setup_stress (data, layout);

		v_setup_notes (data, layout);
	} // v_fill_set_layout (layout_values, layout)


	/********************
	 * Goes through the m_main_ll and fills out the Date labels
	 * for the sets.  This is tricky, because we don't want to
	 * repeat ourselves (looks tacky!).
	 *
	 * preconditions:
	 *		The main layout (m_main_ll) is filled out with
	 *		all the sets that will be there.
	 */
	private void v_trim_date_labels() {
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

	} // v_trim_date_labels()


	/********************
	 * Sets up the date and time portion of this Activity.
	 *
	 * @param vals			Holds the date to display
	 *
	 * @param set_ll			The linearLayout that holds this
	 * 						particular exercise set.  It'll
	 * 						have some Views added.
	 */
	private void v_setup_date (SetData vals, LinearLayout set_ll) {
		String str = "";
		MyCalendar set_date = new MyCalendar(vals.millis);

		// And display the time of this particular set.
		TextView time_tv = (TextView) set_ll.findViewById(R.id.inspector_set_time_tv);

		// Always do the time.
		str += set_date.print_time(false);

		time_tv.setText(str);
	} // setup_date (vals, set_ll)


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
	private boolean v_setup_reps (SetData vals,
								LinearLayout set_ll) {
		LinearLayout reps_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_reps_ll);

		if (m_data.get_exercise_data().breps) {
			String data_str = v_intfloat_to_string(vals.reps);
			TextView reps_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_reps_data);
			reps_data_tv.setText(data_str);
			if (m_data.get_exercise_data().significant == DatabaseHelper.EXERCISE_COL_REP_NUM) {
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
	} // v_setup_reps (vals, set_ll)


	/*********************
	 */
	private boolean v_setup_weight (SetData vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout weight_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_weight_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_weight_bar);

		if (m_data.get_exercise_data().bweight) {
			TextView weight_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_weight_label);
			String weight_label_str = getString (R.string.inspector_set_weight_label,
					(Object[]) new String[] {m_data.get_exercise_data().weight_unit});
			weight_label_tv.setText(weight_label_str);

			TextView weight_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_weight_data);
			weight_data_tv.setText(v_intfloat_to_string(vals.weight));
			if (m_data.get_exercise_data().significant == DatabaseHelper.EXERCISE_COL_WEIGHT_NUM) {
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
	} // v_setup_weight (vals, set_ll, set_bar)


	/*********************
	 */
	private boolean v_setup_level (SetData vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout level_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_level_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_level_bar);

		if (m_data.get_exercise_data().blevel) {
			String data_str = v_intfloat_to_string(vals.levels);
			TextView level_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_level_data);
			level_data_tv.setText(data_str);
			if (m_data.get_exercise_data().significant == DatabaseHelper.EXERCISE_COL_LEVEL_NUM) {
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
	private boolean v_setup_cals (SetData vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout cals_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_calorie_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_calorie_bar);

		if (m_data.get_exercise_data().bcals) {
			String data_str = v_intfloat_to_string(vals.cals);
			TextView cals_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_calorie_data);
			cals_data_tv.setText(data_str);
			if (m_data.get_exercise_data().significant == DatabaseHelper.EXERCISE_COL_CALORIE_NUM) {
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
	private boolean v_setup_dist (SetData vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout dist_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_dist_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_dist_bar);

		if (m_data.get_exercise_data().bdist) {
			// Unit of Distance
			TextView dist_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_dist_label);
			String dist_label_str = getString (R.string.inspector_set_dist_label,
											m_data.get_exercise_data().dist_unit);
			dist_label_tv.setText(dist_label_str);
			if (m_data.get_exercise_data().significant == DatabaseHelper.EXERCISE_COL_DIST_NUM) {
				dist_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = v_intfloat_to_string(vals.dist);
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
	private boolean v_setup_time (SetData vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout time_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_time_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_time_bar);

		if (m_data.get_exercise_data().btime) {
			// Unit of Time
			TextView time_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_time_label);
			String time_label_str = getString (R.string.inspector_set_time_label,
											m_data.get_exercise_data().time_unit);
			time_label_tv.setText(time_label_str);
			if (m_data.get_exercise_data().significant == DatabaseHelper.EXERCISE_COL_TIME_NUM) {
				time_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = v_intfloat_to_string(vals.time);
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
	private boolean v_setup_other (SetData vals,
								LinearLayout set_ll, boolean set_bar) {
		LinearLayout other_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_other_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_other_bar);

		if (m_data.get_exercise_data().bother) {
			TextView other_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_other_label);
			String other_label_str = getString (R.string.inspector_set_other_label,
												m_data.get_exercise_data().other_title,
												m_data.get_exercise_data().other_unit);
			other_label_tv.setText(other_label_str);
			if (m_data.get_exercise_data().significant == DatabaseHelper.EXERCISE_COL_OTHER_NUM) {
				other_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = v_intfloat_to_string(vals.other);

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
	private void v_setup_stress (SetData vals,
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
	} // v_setup_stress (vals, set_ll)


	/***********************
	 */
	private void v_setup_notes (SetData vals,
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
	 *
	 * preconditions:
	 * 	m_main_ll	Already setup and ready to add layouts.
	 *
	 * 	m_layout_list	At least is initialized.
	 */
	private void v_catchup() {
		Log.d(tag, "Entering catchup()");

		// strategy:
		//	I'm assuming that we're adding the child nodes in
		//	the correct order.  Start with the next child to
		//	add and keep going until the list is finished.
		//
		//	Find where we are in the layout, and add successive
		//	items of m_task.m_layout_list until we max out.
		//
		int num_children = m_main_ll.getChildCount();
		while (num_children < m_layout_list.size()) {
			v_create_set_layout(m_layout_list.get(num_children));
			num_children++;
		}

	} // v_catchup()


	/************************
	 * This should be called after all the data has been
	 * loaded and we're caught up.  This does the finishing
	 * touches (like labels and scrolling to the correct
	 * datum).
	 */
	private void v_finish_ui() {
		v_trim_date_labels();
		v_scroll_to_child (m_set_id);
	}



	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//	Classes
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**************************
	 * Use this class to build the entire list of exercise sets.
	 * If any exist already, they'll be deleted and replaced with
	 * the new stuff.
	 * <p>
	 * NOTE:  I could put synchronized blocks everywhere, but
	 * I think just checking onCancelled periodically should
	 * be fine.  After all, it's just a workout app, not a
	 * medical monitor.
	 * <p>
	 * Preconditions:<br>
	 * 		m_data		setup and ready to roll!
	 *
	 * 		m_main_ll	At least it's constructed.
	 *
	 * PostConditions:
	 */
	class BuildSetListAsyncTask extends AsyncTask<Void, Void, Void> {
		private final static String tag = "BuildSetListAsyncTask";

		/************************
		 */
		@Override
		protected void onCancelled() {
			if (is_progress_dialog_active())
				stop_progress_dialog();
			super.onCancelled();
		}

		/************************
		 */
		@Override
		protected void onPreExecute() {
			if (isCancelled())
				return;

//			start_progress_dialog();

			// Tabula rasa
			m_main_ll.removeAllViews();
			m_layout_list.clear();
		}

		/************************
		 */
		@Override
		protected Void doInBackground(Void... arg0) {
			if (isCancelled())
				return null;

			// Get data from DB.  The
			ArrayList<SetData> set_list = m_data.get_all_sets();
			for (int i = 0; i < set_list.size(); i++) {
				SetLayout layout_item = new SetLayout();
				layout_item.data = set_list.get(i);
				layout_item.order = i;

				// Before adding, we have to test to make sure
				// the Activity still exists.
				if (isCancelled())
					return null;
				m_layout_list.add(layout_item);

				// todo
				//	Here is where we'd publish our progress.
				//	But I'm not sure it's necessary.
			}

			return null;
		} // doInBackground(...)


		/************************
		 */
		@Override
		protected void onProgressUpdate(Void... values) {
			if (isCancelled() == true)
				return;
			v_catchup();
		}

		/************************
		 */
		@Override
		protected void onPostExecute(Void result) {
			if (isCancelled())
				return;

			// Do the easy special case for no sets to
			// display.
			if (m_layout_list.size() == 0) {
				v_display_no_sets();
				return;
			}

			if (isCancelled())
				return;
			v_catchup();

			if (isCancelled())
				return;
			v_finish_ui();

			if (isCancelled())
				return;
			stop_progress_dialog();
		}

	} // class BuildSetListAsyncTask


	/**************************
	 * Use this AsyncTask to change an individual set.
	 * <p>
	 * Preconditions:<br>
	 * 		m_data		setup and ready to roll!
	 * <p>
	 * 		m_main_ll	At least it's constructed.
	 * <p>
	 * 		m_set_id		Indicating the proper set.
	 */
	class ChangeSetAsyncTask extends AsyncTask<Void, Void, Void> {
		private final static String tag = "ChangeSetAsyncTask";

		private SetData m_set_data = null;
		private LinearLayout m_set_view = null;


		@Override
		protected void onCancelled() {
			if (is_progress_dialog_active())
				stop_progress_dialog();
		}

		@Override
		protected void onPreExecute() {
			if (isCancelled())
				return;

//			start_progress_dialog();

			// Get our View to modify
			m_set_view = (LinearLayout) m_main_ll.findViewById(m_set_id);
			if (m_set_view == null) {
				Log.e(tag, "Can't find the view in onActivityResult! Aborting!");
			}
		}

		@Override
		protected Void doInBackground(Void... not_used) {
			// Make sure the data if fresh.  Then grab it.
			// This part could be slow.
			m_data.refresh_data();
			m_set_data = m_data.get_set(m_set_id);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (isCancelled())
				return;

			v_fill_set_layout(m_set_data, m_set_view);
			stop_progress_dialog();
		}


	} // class ChangeSetAsyncTask

}
