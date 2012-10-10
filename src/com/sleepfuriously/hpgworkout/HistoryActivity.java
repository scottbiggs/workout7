package com.sleepfuriously.hpgworkout;

import java.text.DecimalFormat;
import java.util.Date;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HistoryActivity
				extends BaseDialogActivity {

	//------------------------
	//	Constants
	//------------------------

	private static final String tag = "HistoryActivity";

	/**
	 * Key for the Intent to hold the exercise name that
	 * the calling Activity passes to this Activity.
	 */
	public static final String EX_NAME_KEY = "name";


	//------------------------
	//	Widget Data
	//------------------------

	protected LinearLayout m_ll;

	//------------------------
	//	Class Data
	//------------------------

	/** The name of this exercise */
	protected String m_ex_name;

	/** The aspect of this exercise that's significant */
	protected int m_significant = -1;


	/** Whether or not these aspects are used for this exercise */
	protected boolean m_reps = false, m_weight = false,
		m_level = false, m_cals = false, m_dist = false,
		m_time = false, m_other = false;

	/** The units for this units. */
	protected String m_weight_unit, m_dist_unit, m_time_unit,
		m_other_unit, m_other_title;

	//------------------------
	//	Static Data
	//------------------------
	/**
	 * This tells the Activity when it needs to load data.  It
	 * is set to true by OTHER Activities (ASet, EditSet) when
	 * the database is changed and the list of exercise sets
	 * has changed.
	 *
	 * When the Inspector has reloaded, m_changed is moved to
	 * false.
	 */
	public static boolean m_db_dirty = false;


	//-------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);

		m_ll = (LinearLayout) findViewById(R.id.history_ll);
		m_ll.removeAllViews();

		// Get the name of the exercise
		Intent itt = getIntent();
		m_ex_name = itt.getStringExtra(EX_NAME_KEY);

		// Set the title
//		String title = getString(R.string.history_title,
//				(Object[]) new String[] {m_ex_name});
//		TextView title_tv = (TextView) findViewById(R.id.history_title_tv);
//		title_tv.setText(title);

		init_from_database();

	} // onCreate(.)


	//-------------------------
	@Override
	protected void onResume() {
		super.onResume();

		Log.v(tag, "onResume()");

		// Do we need to reload the database and redraw everything?
		if (m_db_dirty) {
			Log.v(tag, "Reloading the History!");
			m_ll.removeAllViews();
			init_from_database();
			m_db_dirty = false;
		}

	} // onResume()

	/***************************
	 * Used by onCreate() and onResume(), this reads in data from
	 * the database and creates the appropriate display.
	 */
	private void init_from_database() {
		int col;

		// Start up the database
		try {
			test_m_db();
			if (m_db != null) {
				throw new SQLiteException("m_db was in use!!!");
			}
			m_db = WGlobals.g_db_helper.getReadableDatabase();

			Cursor cursor = null;

			// Load up info about this exercise.
			try {
				cursor = DatabaseHelper.
						getAllExerciseInfoByName(m_db, m_ex_name);

				if (cursor.moveToFirst()) {
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_SIGNIFICANT);
					m_significant = cursor.getInt(col);

					// Set all the booleans
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_REP);
					m_reps = (cursor.getInt(col)) == 1 ? true : false;

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT);
					m_weight = (cursor.getInt(col)) == 1 ? true : false;
					if (m_weight) {
						col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT);
						m_weight_unit = cursor.getString(col);
					}

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LEVEL);
					m_level = (cursor.getInt(col)) == 1 ? true : false;

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_CALORIES);
					m_cals = (cursor.getInt(col)) == 1 ? true : false;

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST);
					m_dist = (cursor.getInt(col)) == 1 ? true : false;
					if (m_dist) {
						col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST_UNIT);
						m_dist_unit = cursor.getString(col);
					}

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME);
					m_time = (cursor.getInt(col)) == 1 ? true : false;
					if (m_time) {
						col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME_UNIT);
						m_time_unit = cursor.getString(col);
					}

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER);
					m_other = (cursor.getInt(col)) == 1 ? true : false;
					if (m_other) {
						col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_TITLE);
						m_other_title = cursor.getString(col);
						col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_UNIT);
						m_other_unit = cursor.getString(col);
					}

				} // if we found info for this exercise

				else {
					Log.e(tag, "Cannot find the exercise!");
					return;
				}
			}
			catch (SQLiteException e) {
				e.printStackTrace();
			}
			finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}

			try {
				// Load in all the sets of this exercise.
				cursor = m_db.query(DatabaseHelper.SET_TABLE_NAME,
						null,	// columns (all)
						DatabaseHelper.SET_COL_NAME + "=?",
						new String[] {"" + m_ex_name},
						null,
						null,
						DatabaseHelper.SET_COL_DATEMILLIS + " DESC",	// Order by descending
						"10");	// limit

				if (cursor.getCount() > 0) {
					Log.v(tag, "The Cursor count is " + cursor.getCount());
					// Now that there's something to show,
					// change the display message.
//					TextView msg_tv = (TextView) findViewById(R.id.history_desc_tv);
//					msg_tv.setText(R.string.history_msg);

					// This does all the dirty work.
					build_list (cursor);
				} //  if there's something to look at

			} // TRY to get a Cursor
			catch (SQLiteException e) {
				e.printStackTrace();
			}
			finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}


		} // TRY - get a readable database
		catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (m_db != null) {
				m_db.close();
				m_db = null;
			}
		}
	} // init_from_database()


	/***************************
	 * Does the heavy work for making the UI.  Goes through
	 * a Cursor, adding each item to the screen.
	 *
	 * preconditions:
	 * 		m_ll		Correctly setup and ready.
	 *
	 * @param cursor		Has at least one item.
	 */
	private void build_list (Cursor cursor) {
		int col;
		String str;
		TextView tv;

		cursor.moveToFirst();
		do {
			LayoutInflater inflater = getLayoutInflater();
			LinearLayout set_ll = (LinearLayout)
					inflater.inflate(R.layout.history_row,
							m_ll, false);

			// Reps
			tv = (TextView) set_ll.findViewById(R.id.history_row_reps);
			if (m_reps) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_REPS, false);
				str = getString(R.string.label_reps, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Weight
			tv = (TextView) set_ll.findViewById(R.id.history_row_weight);
			if (m_weight) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_WEIGHT, true);
				str = getString(R.string.label_weight, m_weight_unit, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Level
			tv = (TextView) set_ll.findViewById(R.id.history_row_level);
			if (m_level) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_LEVELS, false);
				str = getString(R.string.label_level, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Calories
			tv = (TextView) set_ll.findViewById(R.id.history_row_cals);
			if (m_cals) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_CALORIES, false);
				str = getString(R.string.label_cals, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Distance
			tv = (TextView) set_ll.findViewById(R.id.history_row_dist);
			if (m_dist) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_DIST, true);
				str = getString(R.string.label_dist, m_dist_unit, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Time
			tv = (TextView) set_ll.findViewById(R.id.history_row_time);
			if (m_time) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_TIME, true);
				str = getString(R.string.label_time, m_time_unit, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Other
			tv = (TextView) set_ll.findViewById(R.id.history_row_other);
			if (m_other) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_OTHER, true);
				str = getString(R.string.label_other,
						m_other_title, m_other_unit, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}


			// Date and time
			tv = (TextView) set_ll.findViewById(R.id.history_row_date);
			fill_date (cursor, tv);

			// Stress Level
			tv = (TextView) set_ll.findViewById(R.id.history_row_stress_tv);
			col = cursor.getColumnIndex(DatabaseHelper.SET_COL_CONDITION);
			int stress = cursor.getInt(col);
			switch (stress) {
				case DatabaseHelper.SET_COND_OK:
					tv.setText(R.string.aset_cond_stress_ok);
					tv.setTextColor(getResources().getColor(R.color.ghost_white));
					break;
				case DatabaseHelper.SET_COND_PLUS:
					tv.setText(R.string.aset_cond_stress_too_easy);
					tv.setTextColor(getResources().getColor(R.color.green));
					break;
				case DatabaseHelper.SET_COND_MINUS:
					tv.setText(R.string.aset_cond_stress_too_hard);
					tv.setTextColor(Color.YELLOW);
					break;
				case DatabaseHelper.SET_COND_INJURY:
					tv.setText(R.string.aset_cond_stress_injury);
					tv.setTextColor(getResources().getColor(R.color.red));
					break;
			}

			m_ll.addView(set_ll);

		} while (cursor.moveToNext());
	} // build_list (cursor)


	/***********************
	 * Sets the date on the given TextView.  Assumes that the
	 * Cursor is pointing to the correct Exercise Set already.
	 */
	private void fill_date (Cursor cursor, TextView tv) {
		int col;

		col = cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
		long millis = cursor.getLong(col);
		Date date = new Date(millis);
		String date_str = DateFormat.getDateFormat(this).format(date);
		date_str = date_str + "\n" +
				DateFormat.getTimeFormat(this).format(date);

		tv.setText(date_str);
	}

	/***********************
	 * A helper method that looks at the Cursor and figures out
	 * the number that's returned.  If there is no number on
	 * that column, then the null string is returned.
	 *
	 * @param cursor		The Cursor that holds the data.  It
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
	private String get_data_str (Cursor cursor, String column_name,
	                             boolean is_float) {
		int col = cursor.getColumnIndex(column_name);

		// Null is a special case (it only happens when an
		// exericse definition has changed).
		String test = cursor.getString(col);
		if (test == null) {
			return getString(R.string.inspector_null_value);
		}

		if (is_float) {
			try {
				float f = cursor.getFloat(col);
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
			int num = cursor.getInt(col);
			if (num == -1) {
				return getString(R.string.inspector_skipped_value);
			}
			return ("" + num);
		}
	} // get_data_str (cursor, column_name)


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
				throw new SQLiteException("m_db not null when starting doInBackground() in HistoryActivity!");
		}
	} // test_m_db()

}
