/**
 * This Activity displays the 2D grid of all the workouts.
 * From here the user selects workouts to do.
 *
 * Version #2:
 * 		This uses a STATIC AsyncTask, in an attempt to work
 * 		during an orientation change.
 *
 * UI Details:
 * -----------
 *
 * - A "loading" dialog displays when this loads the first
 * 	time.
 *
 * - The left column (which is always visible) is the list of
 * 	available exercises.
 *
 * - The top row is the date (this may or may not be visible).
 *
 * - At the bottom are two buttons:
 * 		- Add Exercise, which invokes the AddExerciseActivity
 * 		- Change Order, where the user can change the order of the
 * 			displayed exercises.
 *
 * - Scroll left/right, up/down to see the other workout days.
 *
 * - Tap on a left-column item to execute a workout set
 * 	on that exercise.
 *
 * - Long-tap a left-column item to edit/delete that exercise.
 *
 * - Tap on a previously done exercise to view its details (one of the cells)
 *
 * - Long-tap a previously done exercise to edit.
 *
 * - todo: When scrolling way back into the past, the system may pause
 * 	to load old data.  A message and "working" animation pops up.
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


//==================================================
public class GridActivity2 extends BaseDialogActivity
					implements OnClickListener,
							OnLongClickListener {


	//-------------------
	//	Constants
	//-------------------
	private static final String tag = "GridActivity";

	/**
	 *  Used for determining the unique ID for the items in
	 * the main grid.  If the number of columns is greater
	 * than this number, an error will occur.
	 */
	protected static final int ROW_BASE = 1000;


	//
	//	COLORS
	//

	/** The color for the column that denotes today. */
	public int TODAY_BACKGROUND_COLOR;

	/** Background color for pretty much everything else. */
	public int NORMAL_BACKGROUND_COLOR;

	/** Color of the text in the header portion of this screen. */
	public int HEADER_TEXT_COLOR;

	/** Text color in each cell */
	public int CELL_TEXT_COLOR;

	/** Padding for each cell of the grid. */
	public static final int
		GRID_CELL_PADDING_LEFT = 4,
		GRID_CELL_PADDING_RIGHT = 4,
		GRID_CELL_PADDING_TOP = 8,
		GRID_CELL_PADDING_BOTTOM = 8;

	//-------------------
	//	Widget Data
	//-------------------

	/** The buttons for this screen. */
	private Button m_add_butt, m_order_butt;

	private ImageView m_help_iv;

	/**
	 * The two tables of the grid.
	 *
	 * One is a table with one column; it's the left-most
	 * column that doesn't scroll.
	 *
	 * The other table is the grid that holds all the data
	 */
	TableLayout m_left_table, m_main_table;

	/** For handling the scroll of the main table */
	HorizontalScrollView m_main_table_sv;


	//-------------------
	//	Other Data
	//-------------------

	/**
	 * Since GridASyncTask is static, it may persist
	 * when this Activity is destroyed.  This variable
	 * will be passed back to us via
	 * onRetainLastConfiguationInstance() and
	 * getLastNonConfigurationInstance().
	 */
	private GridASyncTask m_task = null;

	/*
	 * This is used to number the rows (and keep track of
	 * how many we have).  It's incremented in onProgressUpdate().
	 */
	int m_row_count = 0;

	/**
	 * This is the column number for the column that represents
	 * the current date.  -1 means no column is relevant.
	 *
	 * This is set in add_row_dates() and used in add_row().
	 * And this is set by side-effect, so BE CAREFUL!
	 */
	protected int m_today_column = -1;

	/**
	 * Tells the order of the displays in the Inspector.  This
	 * preference var is needed when the user clicks on a day's
	 * workout set (so we know which set to send to the Inspector).
	 */
	protected boolean m_pref_oldest_first;


	//-------------------
	//	Methods
	//-------------------

	//------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
//		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maingrid);

		// Get UI references.
		m_add_butt = (Button) findViewById(R.id.grid_add_exercise_button);
		m_add_butt.setOnClickListener(this);
		m_add_butt.setOnLongClickListener(this);

		m_order_butt = (Button) findViewById(R.id.grid_order_button);
		m_order_butt.setOnClickListener(this);
		m_order_butt.setOnLongClickListener(this);

		m_help_iv = (ImageView) findViewById(R.id.grid_logo);
		m_help_iv.setOnClickListener(this);

		m_left_table = (TableLayout)findViewById(R.id.left_table);
		m_main_table = (TableLayout)findViewById(R.id.big_table);

		// Set the colors.
		TODAY_BACKGROUND_COLOR = getResources().getColor(R.color.hpg_blue_darker_still);
		NORMAL_BACKGROUND_COLOR = getResources().getColor(R.color.black);
		HEADER_TEXT_COLOR = getResources().getColor(R.color.hpg_orange_lighter);
		CELL_TEXT_COLOR = getResources().getColor(R.color.floral_white);

		m_main_table_sv = (HorizontalScrollView) findViewById(R.id.grid_horiz_sv);

		TextView title_tv = (TextView) findViewById(R.id.grid_title);
		String current_user = DatabaseFilesHelper.get_active_username(this);
		String title =  getString(R.string.grid_title, current_user);
		title_tv.setText(title);

		// Start the AsyncTask.  This is complicated, as
		// I'm using a new system with a static ASyncTask.
		start_async_task();
	} // onCreate(.)


	/*********************
	 * Does all the work of starting the GridASyncTask.  This
	 * will connect to an existing GridASyncTask or start a
	 * new one if necessary.
	 */
	private void start_async_task() {
//		Log.v(tag, "entering start_async_task()");

		// First, try to grab a reference it from a previous
		// instance of this Activity.
		m_task = (GridASyncTask) getLastNonConfigurationInstance();

		if (m_task == null) {
			// There is no GridASyncTask running, so go ahead
			// and start it up.
			start_progress_dialog(R.string.loading_str);
			Log.d(tag, "start_async_task(): starting a new m_task with a progress dialog.");
			m_task = new GridASyncTask(this);
			m_task.execute();
		}
		else {
			Log.d(tag, "start_async_task(): already an m_task, attaching (no need to start a progress dialog).");

			// There is already a GridASyncTask running,
			// establish a connection to it.
			m_task.attach(this);

			// If the ASyncTask is still working, re-start
			// the progress dialog.
			if (m_task.isDone() == false) {
				start_progress_dialog(R.string.loading_str);
			}

			catch_up();
		}
	} // start_async_task()


	/**************************
	 * Call this to have the UI catch up to all the data
	 * that's stored in the GridASyncTask.
	 * <p>
	 * <b>preconditions</b>:
	 * 	m_task is VALID and pointing to a real object (not null)
	 */
	private void catch_up() {
//		Log.v(tag, "entering catch_up()");

		// Note that we're going to LESS THAN EQUAL here!!!
		for (int i = 0; i <= m_task.m_last_completed_row; i++) {
			m_task.onProgressUpdate(new Integer[] {i});
		}

		// Scroll all the way to the right.
		m_main_table_sv.post(new Runnable() {
			@Override
			public void run() {
				m_main_table_sv.fullScroll(View.FOCUS_RIGHT);
			}
		});

	} // catch_up()


	//------------------------------
	@Override
	protected void onResume() {
		Log.v(tag, "entering onResume()");

		super.onResume();

		// Load up the prefs
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(this);
		m_pref_oldest_first =
				prefs.getBoolean(getString(R.string.prefs_inspector_oldest_first_key),
								false);
	} // onResume()


	//------------------------------
	//	Called when a called Activity is done, returning
	//	focus to this Activity.
	//
	//	If it's the AddExerciseActivity, we have a chance to
	//	see if the user added an exercise or just cancelled.
	//
	//	If they DID add a new exercise, we need to reload
	//	the grid.
	//
	//	Same for EditExerciseActivity.
	//
	//	And for RowEditActivity, too!
	//
	//	Oh yeah, this happens just before onResume() is called.
	//
	//	input:
	//		request_code		Not used.
	//		result_code		Tells if AddExercise was cancelled.
	//		data				Not used.
	//
	@Override
	protected void onActivityResult(int request_code,
									int result_code,
									Intent data) {
//		Log.v(tag, "entering onActivityResult()");

		// This means that the Database has changed.  Reload
		// everything.
		if (result_code == RESULT_OK) {
			m_left_table.removeAllViews();
			m_main_table.removeAllViews();

			m_task = null;	// Signal to restart.
			start_async_task();
		}

	} // onActivityResult (request_code, result_code, data)


	/*************************
	 * Called when an Activity is destroyed during a
	 * configuration/orientation change.  Whatever is
	 * returned here can be retrieved by the new replacement
	 * Activity by calling getlastNonConfigurationInstance().
	 *
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
//		Log.v(tag, "entering onRetainNonConfigurationInstance()");

		m_task.detach();		// Tells task to remove its reference
							// to this Activity as I'm about to
							// die.

		return m_task;	// Return the GridSynceTask so the
						// new Activity can find it (and then
						// attach it to the GridASyncTask).
	}


	/**************************
	 * The loading and the GridASyncTask is done.  Turn
	 * off any dialogs that are still going.
	 */
	public void loading_done() {
//		Log.v(tag, "entering load_done()");

		HorizontalScrollView horiz_sv = (HorizontalScrollView) findViewById(R.id.grid_horiz_sv);
		stop_progress_dialog();
		horiz_sv.fullScroll(View.FOCUS_RIGHT);
	}


	//------------------------------
	@Override
	public void onClick(View v) {
		Intent itt;

		// Grab the clicks in the grid.  Since all the grid cells
		// are TextViews (and the ONLY TextViews in this Activity),
		// it's easy to figure out!
		if (v.getClass() == TextView.class) {
			start_exercise_tabs(v);

			// testing...
//			TableRow.LayoutParams params = (LayoutParams) v.getLayoutParams();
//			Toast.makeText(this, "column = " + params.column, Toast.LENGTH_SHORT).show();
		}

		else {
			switch (v.getId()) {
				case R.id.grid_add_exercise_button:
					WGlobals.play_short_click();
					itt = new Intent(this, AddExerciseActivity.class);
					startActivityForResult(itt, WGlobals.ADDEXERCISEACTIVITY);
					break;

				case R.id.grid_order_button:
					WGlobals.play_short_click();
					itt = new Intent (this, RowEditActivity.class);
					startActivityForResult(itt, WGlobals.ROWEDITACTIVITY);
					break;

				case R.id.grid_logo:
					WGlobals.play_help_click();
					show_help_dialog (R.string.grid_help_title,
									R.string.grid_help_msg);
					break;
			}
		} // else

	} // onClick(v)


	/*****************
	 * Overridden to clean up some memory.
	 *
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (m_task != null) {
			m_task.kill();		// Garbage Collect some memory

			// todo:
			//	Shouldn't we derefernece m_task by setting it to null?
		}
		super.onBackPressed();
	}


	/*****************
	 * Handles long clicks.
	 *
	 * @param v
	 *
	 * @return true iff the click is completely handled
	 */
	@Override
	public boolean onLongClick(View v) {
		Intent itt;	// For possible new Activities.

		int id = v.getId();

		if (id == m_add_butt.getId()) {
			WGlobals.play_long_click();
			show_help_dialog(R.string.grid_add_title, R.string.grid_add_msg);
		}

		else if (id == m_order_butt.getId()) {
			WGlobals.play_long_click();
			show_help_dialog(R.string.grid_order_title, R.string.grid_order_msg);
		}

		// Must be something in the grid
		else {
			// If the ID is odd, they clicked the header
			if (id % 2 == 1) {
				WGlobals.play_long_click();

				// Go to the tab Activity, but tell it to activate
				// the EditExercise tab.
				itt = new Intent (this, ExerciseTabHostActivity.class);

				// Put a null for the date.
				itt.putExtra(ExerciseTabHostActivity.KEY_SET_ID, -1);
				itt.putExtra(ExerciseTabHostActivity.TAB_START_KEY,
							ExerciseTabHostActivity.TAB_EDIT);

				// The name of the exercise is displayed in the textview.
				itt.putExtra(ExerciseTabHostActivity.KEY_NAME,
							((TextView) v).getText());
				startActivityForResult(itt, WGlobals.EXERCISETABHOSTACTIVITY);
			}
			else {
				// They clicked on a group of exercises for
				// particular day.
				// If there's just one workout set, then go
				// straight to EditSet.  If there's more than
				// one, go to Inspector.

				GridElement ge = (GridElement) v.getTag();
				if (ge == null) {
					return true;		// clicked in empty space?
				}
				int set_count = ge.size();

				if (set_count == 1) {
					WGlobals.play_long_click();

					// Go to EditSetActivity
					itt = new Intent (this, EditSetActivity.class);
					itt.putExtra(EditSetActivity.ID_KEY, ge.get_first_id());
					startActivityForResult(itt, WGlobals.EDITSETACTIVITY);
				}

				else {
					// Do the same thing as a regular click.
					onClick(v);
				}
			}

		} // something in the main grid

		return true;
	} // onLongClick (v)

	/******************
	 * Starts the ASetActivity.
	 *
	 * The user has pressed on either an exercise or a grid cell.
	 * So they probably want to do something specific to an exercise
	 * like do a set or inspect sets they've already done.  Call this
	 * to set things up and start the ExerciseTabHostActivity for
	 * the proper exercise.
	 *
	 * @param v		The View that was pressed.  This will be
	 * 				translated so that the correct tab shows
	 * 				up in the TabHost.
	 */
	private void start_exercise_tabs (View v) {
		Intent itt;
		int id = v.getId();

		itt = new Intent (this, ExerciseTabHostActivity.class);

		// Is this a row header (and therefore a click on an exercise)
		// or a grid cell (and therefore call up the Inspector)?
		if (id % 2 == 1) {
			WGlobals.play_short_click();

			// Start a loading dialog
			start_progress_dialog(R.string.loading_str);

			// If the id is odd, they clicked the header.  So we'll
			// go directly to the ASet tab.
			itt.putExtra(ExerciseTabHostActivity.KEY_SET_ID, -1);
			// Put a null for the date.
			itt.putExtra(ExerciseTabHostActivity.TAB_START_KEY,
					ExerciseTabHostActivity.TAB_ASET);
			// The name of the exercise is displayed in the textview.
			itt.putExtra(ExerciseTabHostActivity.KEY_NAME, ((TextView) v).getText());
		}

		else {
			// They clicked in the grid.  Add the id.
			GridElement ge = (GridElement) v.getTag();
			if (ge == null) {
				return;		// clicked in empty space? Then they get no sound!
			}

			WGlobals.play_short_click();

			if (ge.m_count < 1) {
				Log.w(tag, "start_exercise_tabs() could not get an ID for the grid element!");
				itt.putExtra(ExerciseTabHostActivity.KEY_SET_ID, -1);
			}
			if (m_pref_oldest_first) {
				itt.putExtra(ExerciseTabHostActivity.KEY_SET_ID, ge.get_first_id());
			}
			else {
				itt.putExtra(ExerciseTabHostActivity.KEY_SET_ID, ge.get_last_id());
			}
			itt.putExtra(ExerciseTabHostActivity.TAB_START_KEY,
					ExerciseTabHostActivity.TAB_INSPECTOR);
			// The name is also in the tag.
			itt.putExtra(ExerciseTabHostActivity.KEY_NAME, ge.get_name());
		}


		startActivityForResult(itt, WGlobals.EXERCISETABHOSTACTIVITY);
	} // start_aset (view_id)


	/*****************
	 * Adds the first row (which is just a list of dates) to
	 * the GridActivity.  It should be called once, and that call
	 * needs to be before any calls to add_row().
	 *
	 * side effects:
	 * 	m_today_column	This global is set if one of the display
	 * 					dates is discovered to be today.
	 *
	 * @param dates		This is an array of the dates to display.
	 *
	 * @param lines		When true, draw horizontal lines
	 * 					to seperate the entries.
	 */
	void add_row_dates (String dates[], boolean lines) {
//		Log.v(tag, "entering add_row_dates(), lines = " + lines);

		// We'll need the current date.  "Today" will be a
		// different color than the other columns.
		MyCalendar today = new MyCalendar();
		String today_date_str = make_grid_date_string(today);

		// The Header/Exercise name.  This is an empty cell for the
		// first row.
		TableRow left_row = new TableRow(this);
		TextView left_cell = new TextView(this);
		left_cell.setPadding(GRID_CELL_PADDING_LEFT,  GRID_CELL_PADDING_TOP,  GRID_CELL_PADDING_RIGHT,  GRID_CELL_PADDING_BOTTOM);
		left_cell.setTextAppearance(this, R.style.listlike_button);
		left_cell.setTextColor(HEADER_TEXT_COLOR);
		left_cell.setBackgroundColor(NORMAL_BACKGROUND_COLOR);
		left_row.addView(left_cell);

		// This is row we put the dates in.
		TableRow main_row = new TableRow(this);

		// Loop for each item in this row.
		for (int i = 0; i < dates.length; i++) {

			// Do we want to make lines between items?
			if (lines) {
				TableRow.LayoutParams lp =
						new TableRow.LayoutParams (1,
								TableRow.LayoutParams.FILL_PARENT);
				View line = new View(this);
				line.setLayoutParams(lp);
				line.setBackgroundColor(Color.WHITE);
				main_row.addView(line);
			}

			// Putting in the string.
			TextView cell = new TextView(this);
			cell.setText(dates[i]);
			cell.setPadding(GRID_CELL_PADDING_LEFT,  GRID_CELL_PADDING_TOP,  GRID_CELL_PADDING_RIGHT,  GRID_CELL_PADDING_BOTTOM);
			cell.setId(make_id (0, i));	// first row, ith element
			cell.setTextAppearance(this, R.style.listlike_button);
			cell.setGravity(Gravity.CENTER_HORIZONTAL);
			cell.setTextColor(CELL_TEXT_COLOR);
			if (today_date_str.equals(dates[i])) {
				cell.setBackgroundColor(TODAY_BACKGROUND_COLOR);
				m_today_column = i;
//				Log.v(tag, "m_today_column = " + m_today_column);
			}
			else {
				cell.setBackgroundColor(NORMAL_BACKGROUND_COLOR);
			}
			cell.setOnClickListener(this);
			cell.setOnLongClickListener(this);

			// Adding the column data to the row
			main_row.addView(cell);
		} // for each column

		// Add the row to the layouts
		m_left_table.addView(left_row, new TableLayout.LayoutParams());
		m_main_table.addView(main_row, new TableLayout.LayoutParams());
	} // add_first_row (day_list)


	/*****************
	 * This is a faster version--I set the columns for the
	 * lines directly and don't bother drawing empty TextViews
	 * (except for the current day column--I need its background).
	 *
	 * Adds a given row at a time, starting from top for the
	 * first call and going down with each successive call.
	 *
	 * preconditions:
	 * 		m_left_table		ready to receive data
	 *
	 * 		m_main_table		also ready
	 *
	 * 		m_row_info[]		Holds the row that we need!
	 *
	 * @param row_info		The array of data for this row.  Holds
	 * 						more info than we need, but is very
	 * 						convenient.
	 *
	 * @param row_num		The index into the row_info array
	 * 						for this row.  This is also the row
	 * 						number (start at 0 for top-most row).
	 *
	 * @param	lighter		When true, draw this row in a lighter
	 * 						color than normal.  This helps the
	 * 						user distinguish rows more easily.
	 *
	 * @param	lines		When true, draw horizontal lines
	 * 						to seperate the entries.
	 *
	 */
	void add_row_faster2 (GridRow row_info[], int row_num, boolean lighter, boolean lines) {
		/** for simplification */
		GridElement [] row_array = row_info[row_num].tag_array;

		// The Header.  Just one item for this row.
		TableRow left_row = make_row_header (row_info[row_num].exer_name, row_num);

		// The main table row.  An array of strings
		// are going into this row.
		TableRow main_row = new TableRow(this);

		/** The actual columns in the table */
		int column_count = 0;
//		Log.d(tag, "add_row_faster2: tag_array.length = " + row_array.length);

		// Loop through the various days that the
		// user has exercised (in order, or course).
		for (int i = 0; i < row_array.length; i++) {

			// Do the lines
			if (lines) {
				View line = make_line (false);
				main_row.addView(line);
				// Get the params for this View so we can set
				// its column.
				TableRow.LayoutParams params = (TableRow.LayoutParams) line.getLayoutParams();
				params.column = column_count;
				column_count++;
			}

			// Do nothing under these conditions:
			//	1.  No data for this day.
			//	2.	This is NOT the first column--each row needs at least 1 item
			//	3.	The day we're looking at is NOT the current day--current day is always colored
			if ((row_array[i] == null) && (i != 0) && (i != m_today_column)) {
//				Log.d(tag, "add_row_faster2: found a null at i = " + i);
				column_count++;
				continue;
			}

			// Preparing the string for this cell.
			String cell_str = null;

			// This must be checked because there can be nulls if
			// we're displaying the current day (to make the column
			// look good).
			TextView tv;
			if (row_array[i] != null) {
				cell_str = "" + row_array[i].m_count;
				tv = make_cell_tv (cell_str,
								row_num,
								i,
								row_array[i]);
			}
			else {
				tv = make_blank_cell_tv(i);	// a little faster to make than full-blown cells
			}

			main_row.addView(tv);
			column_count++;
		}

		// Add the row to the layouts
		m_left_table.addView(left_row, new TableLayout.LayoutParams());
		m_main_table.addView(main_row, new TableLayout.LayoutParams());
	} // add_row_faster2 (head_str, row_strs, lines)


	/***********************
	 * Helper functions to simplify the logic of add_row().
	 *
	 * Creates the header of each row.
	 *
	 * @param str	The string to put in this header cell.
	 *
	 * @param id		The id to put in for the TextView so
	 * 				that when it's clicked, you know which
	 * 				TextView it is (use the row_num).
	 */
	private TableRow make_row_header (String str, int id) {
		TableRow left_row = new TableRow(this);
		TextView tv = new TextView(this);
		tv.setText(str);
		tv.setGravity(Gravity.RIGHT);
		tv.setPadding(GRID_CELL_PADDING_LEFT,  GRID_CELL_PADDING_TOP,  GRID_CELL_PADDING_RIGHT,  GRID_CELL_PADDING_BOTTOM);
		tv.setTextAppearance(this, R.style.listlike_button);
		tv.setTextColor(HEADER_TEXT_COLOR);
		tv.setBackgroundColor(NORMAL_BACKGROUND_COLOR);
		tv.setId(make_header_id(id));
		tv.setOnClickListener(this);
		tv.setOnLongClickListener(this);
		left_row.addView(tv);
		return left_row;
	}

	/***********************
	 * Helper functions to simplify the logic of add_row().
	 *
	 * Creates a View that looks like a vertical line
	 * and returns it.
	 *
	 * @param top	True iff this is the top line (column
	 * 				headers - dates).
	 */
	private View make_line (boolean top) {
		View line = new View(this);
		TableRow.LayoutParams lp = null;

		// Only need to set the params for the top row.  The
		// lines in successive rows will inherit this width.
		// And this parameter screws with the column param.
		// Yeah, it's a hack, sigh.
		if (top) {
			lp = new TableRow.LayoutParams (1, TableRow.LayoutParams.FILL_PARENT);
			line.setLayoutParams(lp);
		}
		line.setBackgroundColor(Color.WHITE);
		return line;
	}

	/***********************
	 * Helper functions to simplify the logic of add_row().
	 *
	 * Creates a TextView suitable for display as a cell in
	 * the grid.
	 *
	 * @param str		The string to display in this cell.
	 *
	 * @param row_num	The index into the row_info array
	 * 					for this row.  This is also the row
	 * 					number (start at 0 for top-most row).
	 * 					Used to make the cell's id.
	 *
	 * @param index		The index into the tag_array
	 * 					where this cell resides (i).  Used
	 * 					to determine the cell's id.
	 *
	 * @param tag		The object to put into the TextView's
	 * 					tag.  Null is perfectly valid.
	 */
	private TextView make_cell_tv (String str,
								int row_num,
								int index,
								Object tag) {
		TextView cell = new TextView(this);
		cell.setText(str);
		cell.setPadding(GRID_CELL_PADDING_LEFT,  GRID_CELL_PADDING_TOP,  GRID_CELL_PADDING_RIGHT,  GRID_CELL_PADDING_BOTTOM);
		cell.setId(make_id (row_num, index));
		cell.setTextAppearance(this, R.style.listlike_button);
		cell.setGravity(Gravity.CENTER_HORIZONTAL);
		cell.setTextColor(CELL_TEXT_COLOR);

		if (index == m_today_column) {
			cell.setBackgroundColor(TODAY_BACKGROUND_COLOR);
		}
		else {
			cell.setBackgroundColor(NORMAL_BACKGROUND_COLOR);
		}
		cell.setOnClickListener(this);
		cell.setOnLongClickListener(this);
		cell.setTag(tag);
		return cell;
	}

	/***************
	 * Like the above method, except that this is a little faster and designed
	 * only for empty cells.
	 *
	 * side effects:
	 * 		Uses m_today_column to determine the cell's background color.
	 *
	 * @param index		The index into the tag_array
	 * 					where this cell resides (i).  Used
	 * 					to determine the cell's id.
	 *
	 * @return	A TextView suitable for inserting into the main grid.
	 */
	private TextView make_blank_cell_tv (int index) {
		TextView cell = new TextView(this);
		cell.setPadding(GRID_CELL_PADDING_LEFT,  GRID_CELL_PADDING_TOP,  GRID_CELL_PADDING_RIGHT,  GRID_CELL_PADDING_BOTTOM);

		if (index == m_today_column) {
			cell.setBackgroundColor(TODAY_BACKGROUND_COLOR);
		}
		else {
			cell.setBackgroundColor(NORMAL_BACKGROUND_COLOR);
		}
		return cell;
	} // make_blank_cell_tv()


	/***************
	 * Creates an unique ID number given a specified row and column.
	 *
	 * Used in the creation of the main grid; each grid item needs
	 * a unique ID so that it can be identified when it's clicked.
	 *
	 * All main grid IDs are even.  All header IDs are odd.
	 *
	 * @param 	row
	 * @param 	column
	 * @return	unique ID
	 */
	int make_id (int row, int column) {
		if (column > ROW_BASE) {
			Log.e(tag, "make_id (" + row + ", " + column + ") has too many columns!  -- ABORTING!");
			finish();
		}
		return (row * ROW_BASE + column) * 2;
	}

	/***************
	 * Opposite of above.  Finds the row from the given ID.
	 *
	 * Only works on the main grid IDs.
	 *
	 * @param 	id
	 * @return	row (as was initially created)
	 */
	int get_id_row (int id) {
		return (id / 2) / ROW_BASE;
	}
	int get_id_column (int id) {
		return (id / 2) % ROW_BASE;
	}

	/***************
	 * Creates a unique header ID.  This will be odd and inter-
	 * leaved with the grid IDs.  But there should be no overlaps.
	 *
	 * @param 	row
	 * @return	unique ID
	 */
	int make_header_id (int row) {
		return row * 2 + 1;
	}

	/****************
	 * Given a unique ID, this finds which header row it resides.
	 *
	 * NOTE:		This is to be used for the column with the
	 * 			exercise names ONLY!
	 *
	 * @param 	id
	 * @return	The row it was originally said to be on.
	 * 			-1 if the row number is invalid.
	 */
	int get_header_row (int id) {
		if (id % 2 == 0) {		// Cannot be an even number
			return -1;
		}
		int row = ((id - 1) / 2);
		if (row < 0) {
			return -1;
		}
		return row;
	}


	/********************
	 * Creates a string suitable for displaying in the top
	 * row of the Grid.
	 *
	 * @param date	The MyCalendar structure for this date.
	 *
	 * @return	The string to display.
	 */
	protected String make_grid_date_string (MyCalendar date) {
		String ret_str;
		MyCalendar now = new MyCalendar();

		if (now.get_year() == date.get_year()) {
			ret_str = date.get_month_text(this)
					+ " " + date.get_day();
		}
		else {
			// Different year, we need to be explicit with it.
			ret_str = date.get_month_text(this)
					+ " " + date.get_day()
					+ ", " + date.get_year();
		}
		return ret_str;
	} // make_grid_date_string (date)


		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//	Classes
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
	static class GridASyncTask extends AsyncTask <Void, Integer, Void> {

		private static final String tag = "GridASyncTask";

		/**
		 * The Activity that is using this ASyncTask.
		 * This static class may ONLY access the activity
		 * through this data member.
		 * <p>
		 * NOTE: Make sure this is not NULL before using!!!
		 */
		GridActivity2 m_the_grid = null;

		/**
		 * This is a list of all the days that the user has
		 * worked out.
		 */
		ArrayList <MyCalendar> m_days_list;


		/**
		 * This is the main data structure for this Activity.
		 * Each element of this array holds all the information
		 * to draw a row.  add_row() will know what to do with it.
		 *
		 * Once used to construct the grid, these should be deleted
		 * to save space.  onPostExecute() would be a good place.
		 */
		GridRow m_row_info[] = null;

		/**
		 * Holds info about the various exercises.  Each entry is hashed
		 * about the name of the exercise, allowing quick access to the
		 * exercise data.
		 */
		HashMap<String, ExerciseData> m_exercise_data = new HashMap<String, ExerciseData>();

		/**
		 * Whenever a row is completed (filled in completely from the
		 * DB), this number is changed to reflect that completed row.
		 * If no rows are completed, it's -1.
		 *
		 * NOTE: this uses the same numbering conventsion as
		 * onProgressUpdate().  Pay attention!
		 */
		int m_last_completed_row = -1;

		/**
		 * This is a lock to be used for the onProgressUpdate()
		 * method.  There's a chance that that method can be
		 * called multiple times before it exits, so I'm using
		 * this to avoid that problem.
		 */
//		int m_progress_lock = 0;

		/** Will be TRUE when the ASyncTask has finished. */
		boolean m_done = false;


		/***************
		 * Constructor
		 *
		 * Needs a reference to the Activity that's creating
		 * this ASyncTask.  It's how this static class
		 * communicates with that Activity.
		 */
		public GridASyncTask (GridActivity2 activity) {
			Log.v(tag, "entering constructor, id = " + this.toString());

			attach (activity);
		} // constructor

		/***************
		 * Called BEFORE the doInBackground(), this allows
		 * something to be done in the UI thread in prepara-
		 * tion for the long stuff, like starting a progress
		 * dialog (uh, this call has been moved to the various
		 * places that initialize the GridASyncTask).
		 */
		@Override
		protected void onPreExecute() {
//			Log.d(tag, "onPreExecute() starting, id = " + this.toString());
			m_done = false;
			m_last_completed_row = -1;
			m_exercise_data.clear();
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
//			Log.d(tag, "doInBackground() starting, id = " + this.toString());
			SQLiteDatabase db = null;
			int col;

			if (WGlobals.g_db_helper == null) {
				Log.e(tag, "Trying to do something in the background, but g_db_helper is null!!!");
				return null;
			}

			/** for debugging */
			long time = SystemClock.uptimeMillis();

			try {
				db = WGlobals.g_db_helper.getReadableDatabase();

				m_days_list = construct_days_list(db);

				// Signal to onProgressUpdate() to make the first row.
//				Log.d(tag, "   doInBackground(): calling publishProgress(0)");
				publishProgress (0);
//				Log.d(tag, "   doInBackground(): finished calling publishProgress(0)");
				m_last_completed_row = 0;	// Completed the first row.

				//////////////////////////////
				//	First Row (dates) is done.
				//////////////////////////////


				// Load up the exercises, ordered as the user wants
				// them (LORDER).
				Cursor ex_cursor = null;
				try {
					ex_cursor =
						db.query(
							DatabaseHelper.EXERCISE_TABLE_NAME,	// table
							new String[] {DatabaseHelper.COL_ID,
										DatabaseHelper.EXERCISE_COL_NAME,
										DatabaseHelper.EXERCISE_COL_SIGNIFICANT},	//	columns[]
							null,//selection
							null,// selectionArgs[]
							null,	//	groupBy
							null,	//	having
							DatabaseHelper.EXERCISE_COL_LORDER,	//	orderBy
							null);	//	limit

					// Now we know how many elements in a row there
					// are (days_list.size).  And we know how many rows
					// there are. It's time to allocate our
					// m_row_info array of GridRows.
					m_row_info = new GridRow[ex_cursor.getCount()];
					while (ex_cursor.moveToNext()) {
						int pos = ex_cursor.getPosition();

						m_row_info[pos] = new GridRow();

						// Grab the info about this exercise from the cursor.
						ExerciseData ex_data = new ExerciseData();

						col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_NAME);
						ex_data.name = ex_cursor.getString(col);
						m_row_info[pos].exer_name = ex_data.name;

						col = ex_cursor.getColumnIndex(DatabaseHelper.COL_ID);
						ex_data._id = ex_cursor.getInt(col);
						m_row_info[pos].exer_id = ex_data._id;

						col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_SIGNIFICANT);
						ex_data.significant = ex_cursor.getInt(col);
						m_row_info[pos].exer_sig_marker = ex_data.significant;


						// Set up the tag_array.  It's used for making the
						// row and REQUIRED by add_row().
//						m_row_info[pos].tag_array = construct_tag_array (db,
//								m_row_info[pos].exer_name,
//								m_row_info[pos].exer_sig_marker,
//								m_days_list);

						m_row_info[pos].tag_array = construct_tag_array_faster (db,
								ex_data,
								m_days_list);

						// HACK!
						// This is a little odd.  Position 0 is special.
						// But the only way to communicate this is through
						// onProgressUpdate.  It knows that position 0 is
						// special and will do something different.
						// But this creates a gap: where's the first array
						// element?  Well onProgressUpdate will subtract 1
						// from anything that is not 0 and send it to the
						// normal method, add_rows--which doesn't understand
						// this special relationship.
//						Log.d(tag, "   doInBackground(): calling publishProgress(), pos + 1= " + pos + 1);
						publishProgress(pos + 1);
//						Log.d(tag, "   doInBackground(): done calling publishProgress()");
						m_last_completed_row = pos + 1;
					} // while there's still a row
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
				finally {
					if (ex_cursor != null) {
						ex_cursor.close();
						ex_cursor = null;
					}

				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			}
			finally {
				if (db != null) {
					db.close();
					db = null;
				}
			}

			Log.d(tag, "time for doInBackground: " + (SystemClock.uptimeMillis() - time));

			return null;
		} // doInBackground (...)

		/****************
		 * THIS is the part that has access to the UI
		 * thread!!!  Yay, I can do stuff here!
		 *
		 * @param row_num	This is the index to the mm_row_info
		 * 					array for the row to display next.
		 * 					It's also the row number (starting
		 * 					at 0 for the top-most date row).
		 */
		@Override
		protected void onProgressUpdate(Integer ... row_num) {
//			Log.d(tag, "entering onProgressUpdate()");

			// Check for a lock condition--probably not
			// necessary
//			while (m_progress_lock > 0) {
//				Log.e(tag, "onProgressUpdate() trying to run before it's finished! m_progress_lock = " + m_progress_lock);
//				SystemClock.sleep(25); // Wait 1/40 of a second
//			}

			// Lock this method!
//			m_progress_lock++;

//			Log.d (tag, "onProgressUpdate() row_num = " + row_num[0]);

			while (m_the_grid == null) {
				// todo
				// 	HACK!!!  This causes progress to be halted
				//	until the new activity comes back!  Do Better!!!
				//
				// Wait until we are attached to a grid
				SystemClock.sleep(50); // Wait 1/20 of a second
				Log.e(tag, "onProgressUpdate() waiting for m_the_grid to be valid.");
			}

			// If the first element in the array is "", then this
			// is the initial calling of this method. AND that
			// means the rest of the array contains dates to
			// display in the top-most row.

			if (row_num[0] == 0) {
				// Construct our array for the first row (which shows only
				// dates) and display it.
				String top_row[] = construct_first_row (m_days_list);
				m_the_grid.add_row_dates (top_row, true);
			}
			else {
				// This perpetuates the HACK in doInBackground()!
//				m_the_grid.add_row (m_row_info, row_num[0] - 1, false, true);
				m_the_grid.add_row_faster2 (m_row_info, row_num[0] - 1, false, true);
			}

			// Unlock this method
//			m_progress_lock--;
		} // onProgressUpdate (...exercise_data)


		/*****************
		 * Called after doInBackground() has finished.
		 * Yup, you can do some more UI stuff here.
		 */
		@Override
		protected void onPostExecute(Void result) {
			Log.v(tag, "entering onPostExecute(), id = " + this.toString());

			if (m_the_grid != null) {
				m_the_grid.loading_done();	// Tell the activity to dismiss
											// the progress dialog.
			}
			// Note that we're done.
			m_done = true;
			Log.v(tag, "finishing onPostExecute().");
		}


		/*****************
		 * Call this to see if the GridASyncTask is complete.
		 *
		 * The done state is reset (to false) when onPreExecute()
		 * is called, and terminated (true) during onPostExecute().
		 */
		public boolean isDone() {
			return m_done;
		}


		/********************
		 * Finds the significant data from the Set table using the
		 * supplied id.
		 *
		 * @param	db	A database: locked and loaded!
		 *
		 * @param	id	The _ID (DatabaseHelper.COL_ID) of the exercise
		 * 				set to examine.
		 *
		 * @param	significant		The number indicating which data
		 * 							is the most significant for this
		 * 							exercise.
		 *
		 * @param	occurances		The number of times (including this
		 * 							one) that this exercise was done
		 * 							on this day.
		 *
		 * @returns	The quantity from the database of the significant
		 * 			item.  It could be an int or a float, depending
		 * 			on the item.
		 * 			-1 if an error happened or nothing existed.
		 */
		protected IntFloat get_significant_data (SQLiteDatabase db, int id,
											int significant) {
			Cursor cursor = null;
			int col;
			IntFloat value = new IntFloat(-1);


			// Create the correct columns string for our database
			// query.  For efficiency, we want to tap JUST THE ONE
			// column that we want: the most significant column.
			String columns[] = new String[1];
			switch (significant) {
				case DatabaseHelper.EXERCISE_COL_DIST_NUM:
					columns[0] = DatabaseHelper.SET_COL_DIST;
					value.is_float = true;
					break;
				case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
					columns[0] = DatabaseHelper.SET_COL_LEVELS;
					break;
				case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
					columns[0] = DatabaseHelper.SET_COL_CALORIES;
					break;
				case DatabaseHelper.EXERCISE_COL_REP_NUM:
					columns[0] = DatabaseHelper.SET_COL_REPS;
					break;
				case DatabaseHelper.EXERCISE_COL_TIME_NUM:
					columns[0] = DatabaseHelper.SET_COL_TIME;
					value.is_float = true;
					break;
				case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
					columns[0] = DatabaseHelper.SET_COL_WEIGHT;
					value.is_float = true;
					break;
				case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
					columns[0] = DatabaseHelper.SET_COL_OTHER;
					value.is_float = true;
					break;
				default:
					Log.e(tag, "Illegal significant value in get_significant_data(): " + significant);
					return value;
			}

			// This should get just the specified column of the
			// row specified by ID.
			try {
				cursor = db.query(DatabaseHelper.SET_TABLE_NAME,
						columns,
						DatabaseHelper.COL_ID + " = " + id,	// selection
						null,	// selectionArgs[]
						null,	// groupBy
						null,	// having
						null);	// orderBy

				if (cursor.moveToFirst() == false) {
					Log.e(tag, "Could not find the significant database value in get_significant_data(): " + significant);
					cursor.close();
					return value;
				}
				col = cursor.getColumnIndex(columns[0]);
				if (value.is_float) {
					value.set(cursor.getFloat(col));
				}
				else {
					value.set(cursor.getInt(col));
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}

			}

			return value;
		} //  get_significant_data()


		/******************
		 * Constructs an ArrayList that contains every day
		 * that the user has worked out.
		 *
		 * @param db		Readable database that's ready to fire.
		 *
		 * @return	List of MyCalendar objects.  Each object
		 * 			is the next day that the user worked out.
		 */
		ArrayList<MyCalendar> construct_days_list(SQLiteDatabase db) {
//			Log.d(tag, "entering construct_days_list()");

			// Construct a list of all the individual days that
			// the user has exercised.
			ArrayList<MyCalendar> days_list = null;

			// Read from the DB what dates are stored there.
			Cursor days_cursor = null;
			try {
				days_cursor = db.query(
					DatabaseHelper.SET_TABLE_NAME,
					new String[] {DatabaseHelper.SET_COL_DATEMILLIS},
					null,	// selection
					null,	// selectionArgs[]
					null,	// groupBy
					null,	// having
					DatabaseHelper.SET_COL_DATEMILLIS,	// orderBy
					null);


				days_list = new ArrayList<MyCalendar>();

				int counter = 0;
				while (days_cursor.moveToNext()) {
					int col = days_cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
					long millis = days_cursor.getLong(col);
					MyCalendar cal = new MyCalendar(millis);

					// First time through you gotta add it.
					if (days_list.size() == 0) {
						days_list.add(cal);
						counter++;
					}
					else {
						// Test: is this a different day? then add it.
						MyCalendar other = days_list.get(counter - 1);
						if (!cal.is_same_day(other)) {
							days_list.add(cal);
							counter++;
						}
					}
				} // while all Sets
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				if (days_cursor != null) {
					days_cursor.close();
					days_cursor = null;
				}
			}

			return days_list;
		} // construct_days_list()


		/*****************
		 * Creates a human-readable version of the given list of
		 * dates.  This is special: the first array element is blank
		 * because that's what add_row() needs for the first row of
		 * the grid (which is what we're preparing, duh!).
		 *
		 * preconditions:
		 * 		m_the_grid		Is Valid!!!
		 *
		 * @param days_list		MyCalendar arrayList of all the dates
		 * 						to display.
		 */
		String[] construct_first_row (ArrayList<MyCalendar> days_list) {
			String top_row[] = new String[days_list.size()];
			for (int i = 0; i < days_list.size(); i++) {
				MyCalendar tmp_cal = days_list.get(i);
				top_row[i] = m_the_grid.make_grid_date_string (tmp_cal);
			}
			return top_row;
		} // construct_first_row



		/********************
		 * The FAST version.  Doesn't bother getting any Significant
		 * data.
		 *
		 * Creates the array that holds important information
		 * about each workout set for a given exercise.  This
		 * information is needed when the user clicks on the
		 * textview that this represents.
		 *
		 * Since this info will be stuffed in the Tag of each
		 * TextView, the returned array is called tag_array.
		 *
		 * @param db			Readable and ready to go.
		 *
		 * @param ex_data	Data about the exercise.  It only
		 * 					needs the name, _ID, and significant.
		 *
		 * @param days_list	Holds the list of all the days that
		 * 					the user has worked out.
		 *
		 * @return	tag_array, as described above.  It could
		 * 			contain only empty (null) elements.  But
		 * 			it WILL contain the right amount of 'em.
		 */
		GridElement[] construct_tag_array_faster (
								SQLiteDatabase db,
								ExerciseData ex_data,
								ArrayList<MyCalendar> days_list) {
			int col;		// used for quick column references

			if (db == null) {
				Log.e(tag, "Uninitialized database in construct_tag_array()!!!");
			}

			// Allocate and initialize the tag_array.
			GridElement tag_array[] = new GridElement[days_list.size()];
			for (int i = 0; i < tag_array.length; i++) {
				tag_array[i] = null;
			}

			// Get all the sets matching this exercise
			Cursor cursor = null;
			try {
				cursor = db.query(
					DatabaseHelper.SET_TABLE_NAME, // table
					new String[] {DatabaseHelper.COL_ID,
								DatabaseHelper.SET_COL_DATEMILLIS}, // columns
					DatabaseHelper.SET_COL_NAME + " =? ",
					new String[] {ex_data.name},
					null,
					null,
					DatabaseHelper.SET_COL_DATEMILLIS,	// orderBy
					null);

				// Construct the tag_array.  First, check to see if
				// there's anything at all to add to it.
				if (cursor.moveToFirst() == false) {
					// No matching exercise sets, so we're done!
					return tag_array;
				}

				// Get info from this cursor: What day did this set
				// happen?  What's the ID for this set?  And what
				// is the value of the significant part of the set?
				col = cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
				MyCalendar set_date = new MyCalendar (cursor.getLong(col));

				col = cursor.getColumnIndex(DatabaseHelper.COL_ID);
				int set_id = cursor.getInt(col);

				// Ready to loop.  Not doing anything other than counting
				// the number of sets per day.
				int i = 0;
				do {
					if (days_list.get(i).is_same_day(set_date)) {
						if (tag_array[i] == null) {
							tag_array[i] = new GridElement();
						}
						tag_array[i].add(set_id);
						tag_array[i].set_name(ex_data.name);
						if (cursor.moveToNext() == false) {
							break;
						}
						col = cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
						set_date = new MyCalendar (cursor.getLong(col));

						col = cursor.getColumnIndex(DatabaseHelper.COL_ID);
						set_id = cursor.getInt(col);
					}
					else {
						i++;
					}
				} while (i < days_list.size());
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}

			return tag_array;
		} // construct_tag_array (...)


		/***************
		 * Connects this task to an Activity, allowing
		 * this static class to communicate with that
		 * Activity (so it can get the data we're reading
		 * from the database!).
		 *
		 * @param activity	The Activity that wants to
		 * 					use the data.
		 */
		public void attach (GridActivity2 activity) {
			m_the_grid = activity;
		}

		/***************
		 * Removes our connection to whatever Activity
		 * we're attached to (or does nothing if we're
		 * not attached to anything).
		 *
		 * This is an important call when the Activity
		 * goes away (like during an orientation change)
		 * so that we're not using invalid pointers!
		 */
		public void detach() {
//			Log.d(tag, "entering detach(), id = " + this.toString());
			m_the_grid = null;
		}

		/****************
		 * Please call this when the connecting Activity
		 * goes away for good.  This will free up lots of
		 * resources!
		 */
		public void kill() {
//			Log.d(tag, "entering kill(), id = " + this.toString());
			m_row_info = null;	// GC
			detach();		// Why wasn't this done before?
		}

	} // class GridASyncTask

}
