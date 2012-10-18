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
import android.view.View.OnClickListener;
import android.widget.Button;
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

	/** Nice place to hold all the info about this exercise. */
	protected ExerciseData m_ex_data;

	/** The name of this exercise */
	protected String m_ex_name;

	/** How many histories are displayed at a time. */
	protected int m_limit = 10;

	/**
	 * The starting number (offset) of the histories to display.
	 * Eg: to display histories [50..59], m_limit must be 10
	 * and this = 50.
	 *
	 * The actual formula: [offset..(offset + limit)]
	 */
	protected int m_offset = 0;

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

		Button more_butt = (Button) findViewById(R.id.history_more_butt);
		more_butt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				m_offset += m_limit;
				m_db_dirty = true;
				onResume();
			}
		});

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
			m_ex_data = DatabaseHelper.getExerciseData(m_db, m_ex_name);

			Cursor cursor = null;
			try {
				// Load in all the sets of this exercise.
				cursor = m_db.query(DatabaseHelper.SET_TABLE_NAME,
						null,	// columns (all)
						DatabaseHelper.SET_COL_NAME + "=?",
						new String[] {"" + m_ex_name},
						null,
						null,
						DatabaseHelper.SET_COL_DATEMILLIS + " DESC",	// Order by descending
						"" + m_offset + ", " + m_limit);	// Limit: which sets to display

				if (cursor.getCount() > 0) {
					Log.v(tag, "The Cursor count is " + cursor.getCount());

					// This does all the dirty work.
					build_list (cursor);
				} //  if there's something to look at
				else {
					// Nothing there.  Indicate so by turning this
					// TextView on (it's normally not visible and is
					// covered up by the sets).
					TextView tv = (TextView) findViewById(R.id.history_desc_tv);
					tv.setVisibility(View.VISIBLE);
				}

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
			if (m_ex_data.breps) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_REPS, false);
				str = getString(R.string.label_reps, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Weight
			tv = (TextView) set_ll.findViewById(R.id.history_row_weight);
			if (m_ex_data.bweight) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_WEIGHT, true);
				str = getString(R.string.label_weight, m_ex_data.weight_unit, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Level
			tv = (TextView) set_ll.findViewById(R.id.history_row_level);
			if (m_ex_data.blevel) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_LEVELS, false);
				str = getString(R.string.label_level, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Calories
			tv = (TextView) set_ll.findViewById(R.id.history_row_cals);
			if (m_ex_data.bcals) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_CALORIES, false);
				str = getString(R.string.label_cals, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Distance
			tv = (TextView) set_ll.findViewById(R.id.history_row_dist);
			if (m_ex_data.bdist) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_DIST, true);
				str = getString(R.string.label_dist, m_ex_data.dist_unit, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Time
			tv = (TextView) set_ll.findViewById(R.id.history_row_time);
			if (m_ex_data.btime) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_TIME, true);
				str = getString(R.string.label_time, m_ex_data.time_unit, str);
				tv.setText(str);
			}
			else {
				tv.setVisibility(View.GONE);
			}

			// Other
			tv = (TextView) set_ll.findViewById(R.id.history_row_other);
			if (m_ex_data.bother) {
				str = get_data_str(cursor, DatabaseHelper.SET_COL_OTHER, true);
				str = getString(R.string.label_other,
								m_ex_data.other_title, m_ex_data.other_unit, str);
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
