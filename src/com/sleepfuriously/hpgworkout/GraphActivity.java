/**
 * This displays a graph!
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GraphActivity
					extends
						BaseDialogActivity
					implements
						OnClickListener,
						OnLongClickListener {

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


	//-------------------------
	//	Widgets
	//-------------------------

	/**
	 * Where the data is actually drawn.  This is a custom View.
	 */
	GView m_view;

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

	/** The number of the most significant aspect of this exercise */
	protected int m_significant = -1;

	/** The number of times the user has done this exercise */
	protected int m_set_count;

	/** The number of days the user has done this exercise */
	protected int m_day_count;

	/** Holds all the data from our database */
	protected ArrayList<SetData> m_set_data;

	/**
	 * Holds the exact time of the first and last set.  This is used
	 * to figure out the labels in the x-axis.
	 */
	MyCalendar m_start_cal = null, m_end_cal = null;

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


	//-------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String str;

		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graphs);

		m_view = (GView) findViewById(R.id.graph_view);

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
		if (ExerciseTabHostActivity.m_tab_active) {
			title.setVisibility(View.GONE);
			ImageView logo = (ImageView) findViewById(R.id.graph_logo);
			logo.setVisibility(View.GONE);
		}
		else {
			// Otherwise, set the title to our exercise.
			str = getString(R.string.graph_title_msg, m_ex_name);
			title.setText(str);
		}

		// Set the aspect that we're displaying.
		set_aspect_and_units();

		// The main thing!
//		setup_data();

	} // onCreate (.)



	//-------------------------
	@Override
	protected void onResume() {
		super.onResume();

		Log.v(tag, "onResume()");

		// Do we need to reload the database and redraw everything?
		if (m_db_dirty) {
			Log.v(tag, "Redrawing the graph!");
			setup_data();
			m_db_dirty = false;
		}

	} // onResume()



	//-------------------------
	public void onClick(View v) {

	} // onClick (v)

	//-------------------------
	public boolean onLongClick(View v) {
		return false;
	}

	/************************
	 * Part of onCreate(), this looks into the database and figures
	 * out which aspect is most significant.  It then sets the title
	 * of the graph to the aspect and unit (if necessary).
	 */
	private void set_aspect_and_units() {
		int col;
		String str, unit;

		TextView tv = (TextView) findViewById(R.id.graph_description_tv);
		try {
			test_m_db();

			m_db = WGlobals.g_db_helper.getReadableDatabase();
			Cursor cursor = null;
			try {
				cursor = DatabaseHelper.getAllExerciseInfoByName(m_db, m_ex_name);
				cursor.moveToFirst();
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_SIGNIFICANT);
				int sig = cursor.getInt(col);
				switch (sig) {
					case DatabaseHelper.EXERCISE_COL_REP_NUM:
						tv.setText(R.string.aset_reps_label);
						break;

					case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
						col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT);
						unit = cursor.getString(col);
						str = getString(R.string.aset_weight_label, unit);
						tv.setText(str);
						break;

					case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
						tv.setText(R.string.aset_level_label);
						break;

					case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
						tv.setText(R.string.aset_calorie_label);
						break;

					case DatabaseHelper.EXERCISE_COL_DIST_NUM:
						col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST_UNIT);
						unit = cursor.getString(col);
						str = getString(R.string.aset_distance_label, unit);
						tv.setText(str);
						break;

					case DatabaseHelper.EXERCISE_COL_TIME_NUM:
						col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME_UNIT);
						unit = cursor.getString(col);
						str = getString(R.string.aset_time_label, unit);
						tv.setText(str);
						break;

					case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
						col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_TITLE);
						String other_title = cursor.getString(col);
						col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_UNIT);
						unit = cursor.getString(col);
						str = getString(R.string.aset_other_label, other_title, unit);
						tv.setText(str);
						break;
				}
			} // cursor

			catch (SQLiteException e) {
				e.printStackTrace();
			}
			finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}
		} // m_db

		catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (m_db != null) {
				m_db.close();
				m_db = null;
			}
		}

	} // set_aspect_and_units()


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
			int col;	// temp to hold column info.  Should be used briefly.
			Cursor cursor = null;

			// A place to hold the set data.
			SetData basic_set_data = new SetData();


			try {
				test_m_db();
				m_db = WGlobals.g_db_helper.getReadableDatabase();

				try {
					// Grab all the info about this exercise.
					cursor = m_db.query(
								DatabaseHelper.EXERCISE_TABLE_NAME,	// table
								null,			//	columns[]
					            DatabaseHelper.EXERCISE_COL_NAME + "=?",//selection
					            new String[] {m_ex_name},// selectionArgs[]
					            null,	//	groupBy
					            null,	//	having
					            null,	//	orderBy
					            null);

					cursor.moveToFirst();

					// Save which aspect is significant.
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_SIGNIFICANT);
					m_significant = cursor.getInt(col);

					// Save which aspects are used.
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_REP);
					if (col == -1) {
						basic_set_data.m_rep = false;
					}
					else {
						basic_set_data.m_rep = ((cursor.getInt(col) == 1) ? true : false);
					}

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LEVEL);
					if (col == -1) {
						basic_set_data.m_level = false;
					}
					else {
						basic_set_data.m_level = (cursor.getInt(col) == 1 ? true : false);
					}

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_CALORIES);
					if (col == -1) {
						basic_set_data.m_cal = false;
					}
					else {
						basic_set_data.m_cal = (cursor.getInt(col) == 1 ? true : false);
					}

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT);
					if (col == -1) {
						basic_set_data.m_weight = false;
					}
					else {
						basic_set_data.m_weight = (cursor.getInt(col) == 1 ? true : false);
					}

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST);
					if (col == -1) {
						basic_set_data.m_dist = false;
					}
					else {
						basic_set_data.m_dist = (cursor.getInt(col) == 1 ? true : false);
					}

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME);
					if (col == -1) {
						basic_set_data.m_time = false;
					}
					else {
						basic_set_data.m_time = (cursor.getInt(col) == 1 ? true : false);
					}

					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER);
					if (col == -1) {
						basic_set_data.m_other = false;
					}
					else {
						basic_set_data.m_other = (cursor.getInt(col) == 1 ? true : false);
					}

				} // try reading the EXERCISE table
				catch (SQLException e) {
					e.printStackTrace();
				}
				finally {
					if (cursor != null) {
						cursor.close();
						cursor = null;
					}

				}


				// Read in all the sets
				try {
					// Grab all the info about this exercise.
					cursor = m_db.query(
								DatabaseHelper.SET_TABLE_NAME,	// table
								null,			//	columns[]
					            DatabaseHelper.SET_COL_NAME + "=?",//selection
					            new String[] {m_ex_name},// selectionArgs[]
					            null,	//	groupBy
					            null,	//	having
					            DatabaseHelper.SET_COL_DATEMILLIS,	//	orderBy
					            null);

					// Record the number of set in this
					// exercise.
					m_set_count = cursor.getCount();

					// If there are not enough sets, don't do anything.
					if (m_set_count < 1) {
						Log.v(tag, "Not enough exercise sets to graph.");
						return null;
					}

					// todo
					//	Do something with the Day count.
					m_day_count = 0;
					MyCalendar last_day = new MyCalendar();
					last_day.make_illegal_date();

					// Go through the exercise sets one by one.
					while (cursor.moveToNext()) {
						// First, figure out when this happened.
						col = cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
						long date_millis = cursor.getLong(col);

						MyCalendar this_day = new MyCalendar(date_millis);
						if (this_day.is_same_day(last_day)) {
							last_day = this_day;
						}

					SetData new_set_data =
						new SetData(basic_set_data.m_rep,
								basic_set_data.m_level,
								basic_set_data.m_cal,
								basic_set_data.m_weight,
								basic_set_data.m_dist,
								basic_set_data.m_time,
								basic_set_data.m_other);

					//	add all the elements to new_set_data
					if (new_set_data.m_rep) {
						col = cursor.getColumnIndex(DatabaseHelper.SET_COL_REPS);
						new_set_data.m_reps = cursor.getInt(col);
					}
					if (new_set_data.m_level) {
						col = cursor.getColumnIndex(DatabaseHelper.SET_COL_LEVELS);
						new_set_data.m_levels = cursor.getInt(col);
					}
					if (new_set_data.m_cal) {
						col = cursor.getColumnIndex(DatabaseHelper.SET_COL_CALORIES);
						new_set_data.m_cals = cursor.getInt(col);
					}
					if (new_set_data.m_weight) {
						col = cursor.getColumnIndex(DatabaseHelper.SET_COL_WEIGHT);
						new_set_data.m_weights = cursor.getFloat(col);
					}
					if (new_set_data.m_dist) {
						col = cursor.getColumnIndex(DatabaseHelper.SET_COL_DIST);
						new_set_data.m_dists = cursor.getFloat(col);
					}
					if (new_set_data.m_time) {
						col = cursor.getColumnIndex(DatabaseHelper.SET_COL_TIME);
						new_set_data.m_times = cursor.getFloat(col);
					}
					if (new_set_data.m_other) {
						col = cursor.getColumnIndex(DatabaseHelper.SET_COL_OTHER);
						new_set_data.m_others = cursor.getFloat(col);
					}

					// Add this to our list.
					m_set_data.add(new_set_data);

					} // looping through all the rows (sets)

					// Setting the first and last labels of the x-axis.
					cursor.moveToFirst();
					col = cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
					m_start_cal = new MyCalendar(cursor.getLong(col));

					cursor.moveToLast();
					col = cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
					m_end_cal = new MyCalendar(cursor.getLong(col));


				} // try reading the SET table
				catch (SQLException e) {
					e.printStackTrace();
				}
				finally {
					if (cursor != null) {
						cursor.close();
						cursor = null;
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

			m_view.clear();

			// Set the start and end labels for the x-axis.
			if ((m_start_cal == null) || (m_end_cal == null)) {
				Log.v (tag, "Date not set in onPostExecute.");
				stop_progress_dialog();
				m_loading = false;
				m_view.invalidate();		// force GView to redraw
				return;
			}
			String start_str = "" + (m_start_cal.get_month() + 1) + "/" +
					m_start_cal.get_day() + "/" +
					m_start_cal.get_year_two_digit();
			String end_str = "" + (m_end_cal.get_month() + 1) + "/" +
					m_end_cal.get_day() + "/" +
					m_end_cal.get_year_two_digit();
			m_view.set_x_axis_labels(start_str, end_str);

			for (SetData set_data : m_set_data) {
				switch (m_significant) {
					case DatabaseHelper.EXERCISE_COL_REP_NUM:
						m_view.add_point(set_data.m_reps);
						break;
					case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
						m_view.add_point(set_data.m_levels);
						break;
					case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
						m_view.add_point(set_data.m_cals);
						break;
					case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
						m_view.add_point(set_data.m_weights);
						break;
					case DatabaseHelper.EXERCISE_COL_DIST_NUM:
						m_view.add_point(set_data.m_dists);
						break;
					case DatabaseHelper.EXERCISE_COL_TIME_NUM:
						m_view.add_point(set_data.m_times);
						break;
					case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
						m_view.add_point(set_data.m_others);
						break;
					default:
						Log.e(tag, "Can't find a significant aspect in onPostExecute!");
						break;
				}
			} // for all the sets

			stop_progress_dialog();
			m_view.invalidate();		// Necessary to make sure that
									// it's drawn AFTER all the db
									// stuff happens.
			m_loading = false;
		} // onPostExecute( result )

	} // class GraphSyncTask


	/*******************************************
	 * This class describes the data for
	 * a single exercise set.
	 *
	 */
	public class SetData {
		/**
		 * Booleans to tell if this aspect is relevant for
		 * this exercise.
		 */
		public boolean m_rep, m_level, m_cal, m_weight,
				m_dist, m_time, m_other;

		/** The actual quantities */
		public int m_reps, m_levels, m_cals;

		/** The actual quantities */
		public float m_weights, m_dists, m_times, m_others;

		/******************
		 * Basic Constructor
		 */
		public SetData() {
			clear_all();
		}

		/******************
		 * Constructor
		 * This is a quick way to set all the booleans in
		 * one shot.
		 */
		public SetData(boolean rep, boolean level, boolean cal,
		               boolean weight, boolean dist, boolean time,
		               boolean other) {
			init (rep, level, cal, weight, dist, time,
					other);
		}

		/*****************
		 * Initializes the booleans
		 */
		private void init (boolean rep, boolean level, boolean cal,
				boolean weight, boolean dist, boolean time,
				boolean other) {
			m_rep = rep;
			m_level = level;
			m_cal = cal;
			m_weight = weight;
			m_dist = dist;
			m_time = time;
			m_other = other;
		}

		/******************
		 * Clears all the booleans (sets them to false).
		 */
		public void clear_all() {
			m_rep = m_level = m_cal = m_weight = m_dist
				= m_time = m_other = false;
		}
	} // class SetData


}
