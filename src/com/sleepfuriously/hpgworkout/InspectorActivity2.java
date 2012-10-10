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
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnLongClickListener;
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


	//------------------
	//	Widgets
	//------------------

	LinearLayout m_main_ll;

	ScrollView m_sv;


	//------------------
	//	Data
	//------------------

	/**
	 * The id for the exercise set to center on.  This is only used
	 * during onCreate().  If this is a valid id number, then the
	 * set with the given id should be scrolled to when this Activity
	 * pops up the first time.
	 */
	private int m_set_id;


	/** The REAL date for this set. */
	private MyCalendar m_set_date;


	//--------------------
	//	All from the exercise database
	//--------------------
	protected int m_ex_id;
	protected String m_ex_name;
	protected int m_ex_type;
	protected int m_ex_group;
	protected boolean m_ex_weights;
	protected String m_ex_weight_unit;
	protected boolean m_ex_reps;
	protected boolean m_ex_dist;
	protected String m_ex_dist_unit;
	protected boolean m_ex_time;
	protected String m_ex_time_unit;
	protected boolean m_ex_level;
	protected boolean m_ex_cals;
	protected boolean m_ex_other;
	protected String m_ex_other_title;
	protected String m_ex_other_unit;
	protected int m_ex_significant = -1;
	protected int m_ex_lorder;


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

	/** A hack to get around scoping rules for scroll_to_child(id). */
	protected static int s_id;


	//------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inspector);

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

		if (m_db_dirty) {
			init_from_db();
		}


	} // onResume()


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
	//	This Activity calls either ASetActivity or
	//	EditSetActivity.  Both return and cause this method
	//	to hit.
	//
	//	ASetActivity:
	//		- If the result is OK, then the Database has been
	//		changed.  Need to add the new exercise set.
	//		- If the result is CANCEL, then don't do
	//		anything.
	//
	//	EditSetActivity:
	//		- If OK, then reload the exercise set.
	//		- CANCEL, do nothing.
	//
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	                                Intent data) {

		if (resultCode == RESULT_CANCELED) {
			return;	// don't do anything
		}

		// This happens when a set has been edited/deleted.
		if (requestCode == WGlobals.EDITSETACTIVITY) {
			init_from_db();
			HistoryActivity.m_db_dirty = true;
			GraphActivity.m_db_dirty = true;
			return;
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
	 */
	protected void scroll_to_child (int id) {
		// For the times we need to scroll to a given child of the
		// scrollview.
		if (id == -1) {
			return;		// Don't bother!
		}

		// WARNING!  This is a HACK to get around scoping rules!
		s_id = id;

		// Set the scroll the right value.
		m_sv = (ScrollView) findViewById(R.id.inspector_sv);

		// Make it scroll, but first we have to wait for
		// everything to be set up.
		m_sv.post(new Runnable() {
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

				Log.v(tag, "Scrolling: " + height_of_views);
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

		// Start the AsyncTask.  It'll handle the rest.
		new InspectorSyncTask().execute();
	} // init_from_db()


	/***********************
	 * A helper method that looks at the Cursor and figures out
	 * the number that's returned.  If there is no number on
	 * that column, then the null string is returned.
	 *
	 * @param set_cursor		The Cursor that holds the set data.  It
	 * 					should be primed and on the correct
	 * 					row.
	 *
	 * @param column_name	The String that defines the column
	 * 					in question.
	 *
	 * @param is_float	true = float
	 * 					false = int
	 *
	 * @return	A String that represents the number.  If the
	 * 			number is less than 0 (which means that the
	 * 			user skipped this), then a special string
	 * 			is output indicating that the user didn't
	 * 			supply this number.
	 */
	private String get_data_str (Cursor set_cursor, String column_name,
	                             boolean is_float) {
		int col = set_cursor.getColumnIndex(column_name);

		// Null is a special case (it only happens when an
		// exericse definition has changed).
		String test = set_cursor.getString(col);
		if (test == null) {
			return getString(R.string.inspector_null_value);
		}

		if (is_float) {
			try {
				float f = set_cursor.getFloat(col);
				if (f < 0) {
					return getString(R.string.inspector_skipped_value);
				}
				return new DecimalFormat("#.###").format(f);
			}
			catch (Exception e) {
				e.printStackTrace();
				return "float error!";
			}
		}
		else {
			int num = set_cursor.getInt(col);
			if (num == -1) {
				return getString(R.string.inspector_skipped_value);
			}
			return ("" + num);
		}
	} // get_data_str (cursor, column_name)


	/***************************
	 * Creates a display for a given set.  It needs a bunch
	 * of widgets to fill in (and will make some, too).  This
	 * will also do some database stuff so that it fills
	 * everything in right.
	 *
	 * More than one set could be represented, so this may
	 * call make_a_table() multiple times--once for each
	 * exercise set.
	 *
	 * preconditions:
	 * 		m_ex_name needs to be correct.
	 *
	 * @param id		The id (of the database) for the
	 * 				set we're checking out.
	 *
	 * @param db		A database ready to read.
	 *
	 * @param parent		A LinearLayout (already created)
	 * 				to hold the info for this set.  This
	 * 				set will be added.
	 *
	 * @param ex_cursor		This cursor points to the exercise
	 * 				info for this particular exercise.  It's
	 * 				used to inflate the rows properly.
	 *
	 * @param set_cursor		A cursor that holds info about the
	 * 				set to display.  It should be already on
	 * 				the correct row and contain ALL relevant
	 * 				columns.
	 *
	 * @param count	Tell me if this is the first time (1),
	 * 				the second time (2), and so on that
	 * 				this has been called.
	 */
	void make_set_display(int id,
	                      SQLiteDatabase db,
	                      LinearLayout parent,
	                      Cursor ex_cursor,
	                      Cursor set_cursor,
	                      int count) {
		boolean set_bar = false;		// used to determine whether or not to draw a divider bar

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout set_ll = (LinearLayout) inflater.inflate(R.layout.inspector_set, parent, false);


		// Put some info in.  Start with the title
		TextView title_tv = (TextView) set_ll.findViewById(R.id.inspector_set_title_tv);
		String title_str = getString(R.string.inspector_set_title, count);
		title_tv.setText(title_str);

		// Save the date of this set.  Since all the sets
		// are on the same date, the repetition won't fuck
		// things up.
		setup_date (set_cursor, set_ll);

		if (setup_reps (set_cursor, ex_cursor, set_ll))
			set_bar = true;

		if (setup_weight (set_cursor, ex_cursor, set_ll, set_bar))
			set_bar = true;

		if (setup_level (set_cursor, ex_cursor, set_ll, set_bar))
			set_bar = true;

		if (setup_cals (set_cursor, ex_cursor, set_ll, set_bar))
			set_bar = true;

		if (setup_dist (set_cursor, ex_cursor, set_ll, set_bar))
			set_bar = true;

		if (setup_time (set_cursor, ex_cursor, set_ll, set_bar))
			set_bar = true;

		if (setup_other (set_cursor, ex_cursor, set_ll, set_bar))
			set_bar = true;

		setup_stress (set_cursor,set_ll);

		setup_notes (set_cursor,set_ll);

		// Make this respond to long clicks.  Use the ID
		// that was sent in.
		set_ll.setId(id);
		set_ll.setOnLongClickListener(this);

		// Finally add this child and clean up
		parent.addView(set_ll);

	} // make_set_display (...)

	/********************
	 * Part of the UI thread while creating the display, this
	 * takes all the data for a set that was loaded in by the
	 * ASyncTask and puts those values into a new layout.
	 *
	 * preconditions:
	 * 		m_main_ll		Needs to be ready to receive children.
	 *
	 * @param layout_values		The data for this set that was
	 * 							loaded from the DB.
	 *
	 *
	 */
	void make_set_layout (SetLayout layout_values) {
		/** used to determine whether or not to draw a divider bar */
		boolean set_bar = false;

		LayoutInflater inflater = getLayoutInflater();
		LinearLayout set_ll = (LinearLayout) inflater
				.inflate(R.layout.inspector_set, m_main_ll, false);



		// todo
		// todo
		// todo
		// todo
		// todo

	}  // make_set_layout (layout_values)


	/********************
	 * Called after the user has edited a workout set,
	 * this clears the UI and reloads it with fresh
	 * data from the DB.
	 *
	 * @param ll		The LinearLayout to reload.
	 *
	 * @return	2:	The item was probably deleted. The
	 * 				InspectorActivity should delete this
	 * 				entire bit.  If it's the only bit, then
	 * 				it should exit entirely.
	 *
	 * 			1:	The InspectorActivity should immediately
	 * 				exit and return RESULT_OK to GridActivity
	 * 				because the date has changed and this
	 * 				set no longer matches with the others
	 * 				in this group of workout sets.
	 *
	 * 			0:	Continue normally.  Date has not changed.
	 */
	private int reload_set(LinearLayout ll) {

		// used to determine whether or not to draw a divider bar
		boolean set_bar = false;

		// When TRUE, we need to abort the Inspector and go back
		// to the GridActivity.
		int exit_code = 0;

		// The id for this exercise set happens to be the same
		// as the id for the corresponding LinearLayout.
		final int id = ll.getId();

		try {
			test_m_db();
			m_db = WGlobals.g_db_helper.getReadableDatabase();

			Cursor ex_cursor = null;
			try {
				ex_cursor = m_db.query(
							DatabaseHelper.EXERCISE_TABLE_NAME,
							null,			//	columns[]
				            DatabaseHelper.EXERCISE_COL_NAME + "=?",//selection
				            new String[] {m_ex_name},// selectionArgs[]
				            null,	//	groupBy
				            null,	//	having
				            null,	//	orderBy
				            null);
				ex_cursor.moveToFirst();

				// While we have it, save the lorder of this exercise.
				// It's needed if we have to send off a new ASetActivity.
//				col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LORDER);
//				m_lorder = ex_cursor.getInt(col);

				// Get all the columns for this particular set.
				Cursor set_cursor = null;
				try {
					set_cursor = m_db.query(
							DatabaseHelper.SET_TABLE_NAME,
							null,	// all columns
							DatabaseHelper.COL_ID + "=?",
							new String[] {"" + id},
							null, null, null, null);

					// Check to see if we got anything.  If this
					// item was deleted, then the result here will
					// be 0.
					if (set_cursor.getCount() == 1) {

						set_cursor.moveToFirst();

						// Save the date of this set.  Since all the sets
						// are on the same date, the repetition won't fuck
						// things up.
						MyCalendar old = new MyCalendar(m_set_date);
						setup_date (set_cursor, ll);
						if (!m_set_date.is_same_day(old)) {
							// This is a different day.  Abandon the
							// InspectorActivity and go straight to
							// the GridActivity, informing it to
							// reload.
							exit_code = 1;
						}
						else {
							if (setup_reps (set_cursor, ex_cursor, ll))
								set_bar = true;

							if (setup_weight (set_cursor, ex_cursor, ll, set_bar))
								set_bar = true;

							if (setup_level (set_cursor, ex_cursor, ll, set_bar))
								set_bar = true;

							if (setup_cals (set_cursor, ex_cursor, ll, set_bar))
								set_bar = true;

							if (setup_dist (set_cursor, ex_cursor, ll, set_bar))
								set_bar = true;

							if (setup_time (set_cursor, ex_cursor, ll, set_bar))
								set_bar = true;

							if (setup_other (set_cursor, ex_cursor, ll, set_bar))
								set_bar = true;

							setup_stress (set_cursor, ll);

							setup_notes (set_cursor, ll);
						}
					} // There was the right number of items found
					else {
						// Some sort of error--probably this
						// item was deleted.
						exit_code = 2;
					}

				} // <--try set_cursor
				catch (SQLException e) {
					e.printStackTrace();
				}
				finally {
					if (set_cursor != null) {
						set_cursor.close();
						set_cursor = null;
					}
				}
			} // <--try ex_cursor
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				if (ex_cursor != null) {
					ex_cursor.close();
					ex_cursor = null;
				}
			}

		}
		catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (m_db != null) {
				m_db.close();
				m_db = null;
			}
		}

		return exit_code;
	} // reload_set (ll)


	/********************
	 * Sets up the date and time portion of this Activity.
	 *
	 * @param set_cursor		The cursor for this workout
	 * 						set, primed to use.
	 *
	 * @param set_ll			The linearLayout that holds this
	 * 						particular exercise set.  It'll
	 * 						have some Views added.
	 */
	private void setup_date (Cursor set_cursor, LinearLayout set_ll) {
		int col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
		long date_millis = set_cursor.getLong(col);
		m_set_date = new MyCalendar(date_millis);

		// And display the time of this particular set.
		TextView time_tv = (TextView) set_ll.findViewById(R.id.inspector_set_time_tv);
		time_tv.setText(m_set_date.print_time(false));
	} // setup_date (set_cursor, set_ll)

	/********************
	 * Sets up the reps portion.
	 *
	 * @param set_cursor
	 * @param ex_cursor
	 * @param set_ll
	 *
	 * @return	Whether or not set_bar need to be true.  Use
	 * 			this like:	if (setup_reps())
	 * 							set_bar = true;
	 */
	private boolean setup_reps (Cursor set_cursor, Cursor ex_cursor,
	                         LinearLayout set_ll) {
		LinearLayout reps_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_reps_ll);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_REP);
		boolean reps = ex_cursor.getInt(col) == 1 ? true : false;
		if (reps) {
			String data_str = get_data_str (set_cursor,
					DatabaseHelper.SET_COL_REPS, false);
			TextView reps_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_reps_data);
			reps_data_tv.setText(data_str);
			if (m_ex_significant == DatabaseHelper.EXERCISE_COL_REP_NUM) {
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
	private boolean setup_weight (Cursor set_cursor, Cursor ex_cursor,
	                              LinearLayout set_ll, boolean set_bar) {
		LinearLayout weight_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_weight_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_weight_bar);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT);
		boolean weight = ex_cursor.getInt(col) == 1 ? true : false;
		if (weight) {
			// Unit of Weight
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT);
			String weight_unit = ex_cursor.getString(col);

			TextView weight_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_weight_label);
			String weight_label_str = getString (R.string.inspector_set_weight_label,
					(Object[]) new String[] {weight_unit});
			weight_label_tv.setText(weight_label_str);

			String data_str = get_data_str (set_cursor,
					DatabaseHelper.SET_COL_WEIGHT, true);
			TextView weight_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_weight_data);
			weight_data_tv.setText(data_str);
			if (m_ex_significant == DatabaseHelper.EXERCISE_COL_WEIGHT_NUM) {
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
	private boolean setup_level (Cursor set_cursor, Cursor ex_cursor,
	                              LinearLayout set_ll, boolean set_bar) {
		LinearLayout level_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_level_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_level_bar);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LEVEL);
		boolean level = ex_cursor.getInt(col) == 1 ? true : false;
		if (level) {
			String data_str = get_data_str (set_cursor,
					DatabaseHelper.SET_COL_LEVELS, false);
			TextView level_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_level_data);
			level_data_tv.setText(data_str);
			if (m_ex_significant == DatabaseHelper.EXERCISE_COL_LEVEL_NUM) {
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
	private boolean setup_cals (Cursor set_cursor, Cursor ex_cursor,
                              LinearLayout set_ll, boolean set_bar) {
		LinearLayout cals_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_calorie_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_calorie_bar);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_CALORIES);
		boolean cals = ex_cursor.getInt(col) == 1 ? true : false;
		if (cals) {
			String data_str = get_data_str (set_cursor,
					DatabaseHelper.SET_COL_CALORIES, false);
			TextView cals_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_calorie_data);
			cals_data_tv.setText(data_str);
			if (m_ex_significant == DatabaseHelper.EXERCISE_COL_CALORIE_NUM) {
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
	private boolean setup_dist (Cursor set_cursor, Cursor ex_cursor,
                              LinearLayout set_ll, boolean set_bar) {
		LinearLayout dist_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_dist_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_dist_bar);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST);
		boolean dist = ex_cursor.getInt(col) == 1 ? true : false;
		if (dist) {
			// Unit of Distance
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST_UNIT);
			String dist_unit = ex_cursor.getString(col);

			TextView dist_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_dist_label);
			String dist_label_str = getString (R.string.inspector_set_dist_label, dist_unit);
			dist_label_tv.setText(dist_label_str);
			if (m_ex_significant == DatabaseHelper.EXERCISE_COL_DIST_NUM) {
				dist_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = get_data_str (set_cursor,
					DatabaseHelper.SET_COL_DIST, true);
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
	private boolean setup_time (Cursor set_cursor, Cursor ex_cursor,
                              LinearLayout set_ll, boolean set_bar) {
		LinearLayout time_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_time_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_time_bar);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME);
		boolean time = ex_cursor.getInt(col) == 1 ? true : false;
		if (time) {
			// Unit of Time
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME_UNIT);
			String time_unit = ex_cursor.getString(col);

			TextView time_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_time_label);
			String time_label_str = getString (R.string.inspector_set_time_label, time_unit);
			time_label_tv.setText(time_label_str);
			if (m_ex_significant == DatabaseHelper.EXERCISE_COL_TIME_NUM) {
				time_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = get_data_str (set_cursor,
					DatabaseHelper.SET_COL_TIME, true);
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
	private boolean setup_other (Cursor set_cursor, Cursor ex_cursor,
                              LinearLayout set_ll, boolean set_bar) {
		LinearLayout other_ll = (LinearLayout) set_ll.findViewById(R.id.inspector_set_other_ll);
		View bar = set_ll.findViewById(R.id.inspector_set_other_bar);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER);
		boolean other = ex_cursor.getInt(col) == 1 ? true : false;
		if (other) {
			// Other Label (this is a combo of the title
			// and unit)--different from the others.
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_TITLE);
			String other_title = ex_cursor.getString(col);

			// the unit
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_UNIT);
			String other_unit = ex_cursor.getString(col);

			TextView other_label_tv = (TextView) set_ll.findViewById(R.id.inspector_set_other_label);
			String other_label_str = getString (R.string.inspector_set_other_label, other_title, other_unit);
			other_label_tv.setText(other_label_str);
			if (m_ex_significant == DatabaseHelper.EXERCISE_COL_OTHER_NUM) {
				other_label_tv.setTypeface(null, Typeface.BOLD);
			}

			String data_str = get_data_str (set_cursor,
					DatabaseHelper.SET_COL_OTHER, true);
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
	private void setup_stress (Cursor set_cursor,
                              LinearLayout set_ll) {
		TextView stress_data_tv = (TextView) set_ll.findViewById(R.id.inspector_set_stress_data);

		int col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_CONDITION);
		int stress_num = set_cursor.getInt(col);
		switch (stress_num) {
			case DatabaseHelper.SET_COND_OK:
				stress_data_tv.setText(R.string.aset_cond_stress_ok);
				break;
			case DatabaseHelper.SET_COND_MINUS:
				stress_data_tv.setText(R.string.aset_cond_stress_too_hard);
				break;
			case DatabaseHelper.SET_COND_PLUS:
				stress_data_tv.setText(R.string.aset_cond_stress_too_easy);
				break;
			case DatabaseHelper.SET_COND_INJURY:
				stress_data_tv.setText(R.string.aset_cond_stress_injury);
				break;
			default:
				stress_data_tv.setText(R.string.aset_cond_stress_error);
				break;
		}
	} // stress


	/***********************
	 *
	 */
	private void setup_notes (Cursor set_cursor,
                              LinearLayout set_ll) {
		int col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_NOTES);
		String notes = set_cursor.getString(col);
		if ((notes != null) && (notes.length() > 0)) {
			// There's a note!  Display it.
			TextView notes_tv = (TextView) set_ll.findViewById(R.id.inspector_set_notes_tv);
			notes_tv.setText(notes);
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


	/********************
	 * Loads all the info about the current exercise into
	 * the class variables.
	 *
	 * Called during an ASyncTask, this does NO UI stuff; it
	 * just grabs some data and save it.
	 *
	 * @param db		A database initialized and ready to READ.
	 *
	 * @return	TRUE  - Everything went as expected.
	 * 			FALSE - Something went wrong (probably the
	 * 					exercise couldn't be found).
	 */
	private boolean get_exercise_info (SQLiteDatabase db) {
		int col;
		Cursor ex_cursor = null;

		try {
			ex_cursor = DatabaseHelper.getAllExerciseInfoByName(db, m_ex_name);
			if ((ex_cursor == null) || (ex_cursor.getCount() != 1)) {
				return false;
			}

			// Okay, now we have a Cursor loaded with info about
			// our exercise.  Extract and save that data.
			ex_cursor.moveToFirst();


			col = ex_cursor.getColumnIndex(DatabaseHelper.COL_ID);
			m_ex_id = ex_cursor.getInt(col);

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TYPE);
			m_ex_type = ex_cursor.getInt(col);

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_GROUP);
			m_ex_group = ex_cursor.getInt(col);

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT);
			m_ex_weights = ex_cursor.getInt(col) == 1 ? true : false;
			if (m_ex_weights) {
				col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT);
				m_ex_weight_unit = ex_cursor.getString(col);
			}
			else
				m_ex_weight_unit = null;

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_REP);
			m_ex_reps = ex_cursor.getInt(col) == 1 ? true : false;

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST);
			m_ex_dist = ex_cursor.getInt(col) == 1 ? true : false;
			if (m_ex_dist) {
				col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST_UNIT);
				m_ex_dist_unit = ex_cursor.getString(col);
			}
			else
				m_ex_dist_unit = null;

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME);
			m_ex_time = ex_cursor.getInt(col) == 1 ? true : false;
			if (m_ex_time) {
				col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME_UNIT);
				m_ex_time_unit = ex_cursor.getString(col);
			}
			else
				m_ex_time_unit = null;

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LEVEL);
			m_ex_level = ex_cursor.getInt(col) == 1 ? true : false;

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_CALORIES);
			m_ex_cals = ex_cursor.getInt(col) == 1 ? true : false;

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER);
			m_ex_other = ex_cursor.getInt(col) == 1 ? true : false;
			if (m_ex_other) {
				col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_UNIT);
				m_ex_other_unit = ex_cursor.getString(col);
				col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_TITLE);
				m_ex_other_title = ex_cursor.getString(col);
			}
			else {
				m_ex_other_unit = null;
				m_ex_other_title = null;
			}

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_SIGNIFICANT);
			m_ex_significant = ex_cursor.getInt(col);

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LORDER);
			m_ex_lorder = ex_cursor.getInt(col);

		} // main try

		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			if (ex_cursor != null) {
				ex_cursor.close();
				ex_cursor = null;
			}
		}

		return true;
	} // get_exercise_info()



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

		//-------------------
		@Override
		protected void onPreExecute() {

			start_progress_dialog(R.string.loading_str);

			// Tabula rasa.
			m_main_ll.removeAllViews();

		} // onPreExecute

		//-------------------
		@Override
		protected Void doInBackground(Void... not_used) {

			try {
				test_m_db();
				m_db = WGlobals.g_db_helper.getReadableDatabase();

				// Read in all the info we need about this
				// exercise.  This works completely by
				// side effect.
				get_exercise_info (m_db);

				// Get a cursor for the sets.  Then loop through
				// them one by one, creating a layout for each.
				// This requires filling in a SetLayout class
				// to hold the appropriate (and relevant) data
				// for that layout.
				Cursor set_cursor = null;
				try {
					set_cursor = DatabaseHelper.getAllSets(m_db, m_ex_name, true);
					int i = 1, id, col;
					while (set_cursor.moveToNext()) {
						col = set_cursor.getColumnIndex(DatabaseHelper.COL_ID);
						id = set_cursor.getInt(col);

						SetLayout layout_values = new SetLayout();

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_NAME);
						layout_values.name = set_cursor.getString(col);

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
						layout_values.millis = set_cursor.getLong(col);

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_WEIGHT);
						layout_values.weight = set_cursor.getFloat(col);

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_REPS);
						layout_values.reps = set_cursor.getInt(col);

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_LEVELS);
						layout_values.levels = set_cursor.getInt(col);

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_CALORIES);
						layout_values.cals = set_cursor.getInt(col);

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_DIST);
						layout_values.dist = set_cursor.getFloat(col);

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_TIME);
						layout_values.time = set_cursor.getFloat(col);

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_OTHER);
						layout_values.other = set_cursor.getFloat(col);

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_CONDITION);
						layout_values.cond = set_cursor.getInt(col);

						col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_NOTES);
						layout_values.notes = set_cursor.getString(col);

						publishProgress(layout_values);
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












				// Before proceeding, we need to get some info from
				// our database.  First, some details about this
				// exercise.
//				Cursor ex_cursor = null;
//				try {
//					ex_cursor = DatabaseHelper.getAllExerciseInfoByName(m_db, m_ex_name);
//					if (ex_cursor.moveToFirst() == false) {
//						Log.e(tag, "No ex_cursor data in init_from_db()!!! Aborting!");
//						return null;
//					}
//
//					// Also, save the significant number.  This'll be
//					// use to bold the appropriate line.
//					int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_SIGNIFICANT);
//					m_ex_significant = ex_cursor.getInt(col);
//
//					//	Loop through all the sets of this exercise, making the
//					//	display.
//					Cursor set_cursor = null;
//					try {
//						set_cursor = DatabaseHelper.getAllSets(m_db, m_ex_name, true);
//						int i = 1, id;
//						while (set_cursor.moveToNext()) {
//							col = set_cursor.getColumnIndex(DatabaseHelper.COL_ID);
//							id = set_cursor.getInt(col);
//
//							SetLayout layout_values = new SetLayout();
//							// todo
//							//	fill in a new SetLayout
//							publishProgress(layout_values);

//							SetDisplay values = new SetDisplay();
//							values.id = id;
//							values.db = m_db;
//							values.parent = m_main_ll;
//							values.ex_cursor = ex_cursor;
//							values.set_cursor = set_cursor;
//							values.count = i++;
//							publishProgress(values);
//							make_set_display(id, m_db, m_main_ll,
//									ex_cursor, set_cursor,
//									i++);

//						}
//					}
//					catch (SQLiteException e) {
//						e.printStackTrace();
//					}
//					finally {
//						if (set_cursor != null) {
//							set_cursor.close();
//							set_cursor = null;
//						}
//					}
//
//				}
//				catch (SQLException e) {
//					e.printStackTrace();
//				}
//				finally {
//					if (ex_cursor != null) {
//						ex_cursor.close();
//						ex_cursor = null;
//					}
//				}

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
		//	Yep, the Cursors are changed (especially set_cursor!).
		//	And that's the bug.
		//
		@Override
		protected void onProgressUpdate(SetLayout... arg0) {
			super.onProgressUpdate(arg0);

			// This does accesses the database, therefore is
			// not thread safe.
			make_set_layout (arg0[0]);
//			make_set_display(arg0[0].id, arg0[0].db, arg0[0].parent,
//					arg0[0].ex_cursor, arg0[0].set_cursor,
//					arg0[0].count);

		} // onProgressUpdate (arg0)


		//-------------------
		@Override
		protected void onPostExecute(Void not_used) {
			stop_progress_dialog();
			scroll_to_child (m_set_id);
		}

	} // class InspectorSyncTask


	/****************************************
	 * Holds the data needed to fill in an entire set's layout.
	 */
	class SetLayout {
		/**
		 * The DB id for this exercise set.  Used to identify this layout.
		 */
		int id;
		String name;
		long millis;
		float weight;
		int reps;
		int levels;
		int cals;
		float dist;
		float time;
		float other;
		int cond;
		String notes;
	} // class SetLayout


	/****************************************
	 * This holds data that is passed from doInBackground() to
	 * onProgressUpdate.  Yeah, just a data class.
	 *
	 */
//	class SetDisplay {
//		int id, count;
//		SQLiteDatabase db;
//		LinearLayout parent;
//		Cursor ex_cursor, set_cursor;
//	} // class SetDisplay

}
