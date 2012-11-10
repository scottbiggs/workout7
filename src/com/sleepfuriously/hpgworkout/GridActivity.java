/**
 * This Activity displays the 2D grid of all the workouts.
 * From here the user selects workouts to do.
 *
 * UI Details:
 * -----------
 *
 * - A "working" animation displays when this loads the first
 * 	time.
 *
 * - The left column (which is always visible) is the list of
 * 	available exercises.
 *
 * - The top row is the date (this may or may not be visible).
 *
 * - At the bottom are three buttons:
 * 		- Add Exercise, which invokes the AddExerciseActivity
 * 		- Back, which exits this Activity and goes back to the
 * 			main screen.
 * 		- Order, where the user can change the order of the
 * 			displayed exercises.
 * 		- Help, which brings up a nice help dialog.
 *
 * - Scroll left/right, up/down to see the previous workouts.
 *
 * - Click on a left-column item to execute a workout set
 * 	on that exercise.
 *
 * - Long-click a left-column item to edit/delete that exercise.
 *
 * - Click on a previously done exercise to view its details
 *
 * - Long-click a previously done exercise to edit.
 *
 * - When scrolling way back into the past, the system may pause
 * 	to load old data.  A message and "working" animation pops up.
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
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
import android.widget.Toast;


//==================================================
public class GridActivity extends BaseDialogActivity
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

	//-------------------
	//	Widget Data
	//-------------------

	/** The buttons for this screen. */
	private Button m_add, m_done, m_order;

	private ImageView m_help;

	/**
	 * The two tables of the grid.
	 *
	 * One is a table with one column; it's the left-most
	 * column that doesn't scroll.
	 *
	 * The other table is the grid that holds all the data
	 */
	TableLayout m_left_table, m_main_table;


	//-------------------
	//	Other Data
	//-------------------

	private static GridSyncTask m_sync_task;

	/*
	 * This is used to number the rows (and keep track of
	 * how many we have).  It's incremented in onProgressUpdate().
	 */
	int m_row_count = 0;

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
	 * This is the column number for the column that represents
	 * the current date.  -1 means no column is relevant.
	 *
	 * This is set in add_row_dates() and used in add_row().
	 * And this is set by side-effect, so BE CAREFUL!
	 */
	protected int m_today_column = -1;


	//-------------------
	//	Methods
	//-------------------

	//------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maingrid);

		// Get UI references.
		m_add = (Button) findViewById(R.id.grid_add_exercise_button);
		m_add.setOnClickListener(this);
		m_add.setOnLongClickListener(this);

		m_done = (Button) findViewById(R.id.grid_done_button);
		m_done.setOnClickListener(this);
		m_done.setOnLongClickListener(this);

		m_order = (Button) findViewById(R.id.grid_order_button);
		m_order.setOnClickListener(this);
		m_order.setOnLongClickListener(this);

		m_help = (ImageView) findViewById(R.id.grid_logo);
		m_help.setOnClickListener(this);

		m_left_table = (TableLayout)findViewById(R.id.left_table);
		m_main_table = (TableLayout)findViewById(R.id.big_table);

		// Set the colors.
		TODAY_BACKGROUND_COLOR = getResources().getColor(R.color.hpg_blue_darker_still);
		NORMAL_BACKGROUND_COLOR = getResources().getColor(R.color.black);
		HEADER_TEXT_COLOR = getResources().getColor(R.color.hpg_orange_lighter);
		CELL_TEXT_COLOR = getResources().getColor(R.color.floral_white);


		// Start the AsyncTask.
		new GridSyncTask().execute();
	} // onCreate(.)


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

		// todo:
		//	Fix this.  We don't need to reload when something's
		//	cancelled!  Find a way to get a result_code from the
		//	tabhost!

//		if (result_code == Activity.RESULT_OK) {
//			Log.v (tag, "A set was added.");

			// Just clean up anyway.
			m_left_table.removeAllViews();
			m_main_table.removeAllViews();
			new GridSyncTask().execute();

//		}

	} // onActivityResult (request_code, result_code, data)


	//------------------------------
	@Override
	protected void onResume() {
		super.onResume();

		if ((m_sync_task != null) &&
			(m_sync_task.getStatus() == Status.RUNNING)) {
			start_progress_dialog(R.string.loading_str);
		}
	} // onResume()


	//------------------------------
	public void onClick(View v) {
		Intent itt;

		// Grab the clicks in the grid.  Since all the grid cells
		// are TextViews (and the ONLY TextViews in this Activity),
		// it's easy to figure out!
		if (v.getClass() == TextView.class) {
			start_exercise_tabs(v);
		}

		else {
			switch (v.getId()) {
				case R.id.grid_done_button:
					finish();
					break;

				case R.id.grid_add_exercise_button:
					// todo:
					//	debug
					Log.d(tag, "onClick(), about to call AddExerciseActivity, and m_db = " + m_db);
					itt = new Intent(this, AddExerciseActivity.class);
					startActivityForResult(itt, WGlobals.ADDEXERCISEACTIVITY);
					break;

				case R.id.grid_order_button:
					itt = new Intent (this, RowEditActivity.class);
					startActivityForResult(itt, WGlobals.ROWEDITACTIVITY);
					break;

				case R.id.grid_logo:
					show_help_dialog (R.string.grid_help_title,
									R.string.grid_help_msg);
					break;
			}

		} // else
	} // onClick(v)


	/*****************
	 * Handles long clicks.
	 *
	 * @param v
	 *
	 * @return true iff the click is completely handled
	 */
	public boolean onLongClick(View v) {
		Intent itt;	// For possible new Activities.

		int id = v.getId();

		if (id == m_add.getId()) {
			show_help_dialog(R.string.grid_add_title, R.string.grid_add_msg);
		}

		else if (id == m_order.getId()) {
			show_help_dialog(R.string.grid_order_title, R.string.grid_order_msg);
		}

		else if (id == m_done.getId()) {
			show_help_dialog(R.string.grid_done_title, R.string.grid_done_msg);
		}

		// Must be something in the grid
		else {
			// If the ID is odd, they clicked the header
			if (id % 2 == 1) {
				// Header.  Show them the details of this
				// exercise.
				itt = new Intent (this, EditExerciseActivity.class);

				itt.putExtra("name", ((TextView) v).getText());

				startActivityForResult(itt, WGlobals.EDITEXERCISEACTIVITY);
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
				return;		// clicked in empty space?
			}
			if (ge.m_count < 1) {
				Log.w(tag, "start_exercise_tabs() could not get an ID for the grid element!");
				itt.putExtra(ExerciseTabHostActivity.KEY_SET_ID, -1);
			}
			itt.putExtra(ExerciseTabHostActivity.KEY_SET_ID, ge.get_first_id());
//			itt.putExtra(ExerciseTabHostActivity.KEY_SET_ID, ge.get_last_id());
			Log.d(tag, "Just set the ID to " + ge.get_first_id());
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
	 * @param	lines		When true, draw horizontal lines
	 * 						to seperate the entries.
	 */
	void add_row_dates (String dates[], boolean lines) {
		// We'll need the current date.  "Today" will be a
		// different color than the other columns.
		MyCalendar today = new MyCalendar();
		String today_date_str = make_grid_date_string(today);

		// The Header/Exercise name.  This is an empty cell for the
		// first row.
		TableRow left_row = new TableRow(this);
		TextView left_cell = new TextView(this);
		left_cell.setPadding(4, 8, 4, 8);
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
				TableRow.LayoutParams lp = new TableRow.LayoutParams (1, TableRow.LayoutParams.FILL_PARENT);
				View line = new View(this);
				line.setLayoutParams(lp);
				line.setBackgroundColor(Color.WHITE);
				main_row.addView(line);
			}

			// Putting in the string.
			TextView cell = new TextView(this);
			cell.setText(dates[i]);
			cell.setPadding(4, 8, 4, 8);
			cell.setId(make_id (0, i));	// first row, ith element
			cell.setTextAppearance(this, R.style.listlike_button);
			cell.setGravity(Gravity.CENTER_HORIZONTAL);
			cell.setTextColor(CELL_TEXT_COLOR);
			if (today_date_str.equals(dates[i])) {
				cell.setBackgroundColor(TODAY_BACKGROUND_COLOR);
				m_today_column = i;
				Log.v(tag, "m_today_column = " + m_today_column);
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
	 * @param row_num		The index into the m_row_info array
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
	void add_row (int row_num, boolean lighter, boolean lines) {
		// The Header.  Just one item for this row.
		TableRow left_row = new TableRow(this);
		TextView left_cell = new TextView(this);
		left_cell.setText(m_row_info[row_num].exer_name);
		left_cell.setGravity(Gravity.RIGHT);
		left_cell.setPadding(4, 8, 4, 8);
		left_cell.setTextAppearance(this, R.style.listlike_button);
		left_cell.setTextColor(HEADER_TEXT_COLOR);
		left_cell.setBackgroundColor(NORMAL_BACKGROUND_COLOR);
		left_cell.setId(make_header_id(row_num));
		left_cell.setOnClickListener(this);
		left_cell.setOnLongClickListener(this);
		left_row.addView(left_cell);

		// The main table row.  An array of strings
		// are going into this row.
		TableRow main_row = new TableRow(this);

		// Loop for each item in this row.
		for (int i = 0; i < m_row_info[row_num].tag_array.length; i++) {

			// Do we want to make lines between items?
			if (lines) {
				TableRow.LayoutParams lp = new TableRow.LayoutParams (1, TableRow.LayoutParams.FILL_PARENT);
				View line = new View(this);
				line.setLayoutParams(lp);
				line.setBackgroundColor(Color.WHITE);
				main_row.addView(line);
			}

			// Putting in the string.
			TextView cell = new TextView(this);
			if (m_row_info[row_num].tag_array[i] != null) {
				String seperator = getString(R.string.grid_edit_seperator);
				String null_str = getString(R.string.grid_cell_negative_symbol);
				cell.setText(m_row_info[row_num].tag_array[i]
					.construct_set_cell_string(seperator, null_str));
			}
			else {
				cell.setText("");
			}
			cell.setPadding(4, 8, 4, 8);
			cell.setId(make_id (row_num, i));
			cell.setTextAppearance(this, R.style.listlike_button);
			cell.setGravity(Gravity.CENTER_HORIZONTAL);
			cell.setTextColor(CELL_TEXT_COLOR);
			if (i == m_today_column) {
				cell.setBackgroundColor(TODAY_BACKGROUND_COLOR);
			}
			else {
				cell.setBackgroundColor(NORMAL_BACKGROUND_COLOR);
			}
			cell.setOnClickListener(this);
			cell.setOnLongClickListener(this);
			if (m_row_info[row_num].tag_array[i] != null) {
				cell.setTag(m_row_info[row_num].tag_array[i]);
			}
			else {
				cell.setTag(null);
			}

			// Adding the column data to the row
			main_row.addView(cell);
		} // for each column

		// Add the row to the layouts
		m_left_table.addView(left_row, new TableLayout.LayoutParams());
		m_main_table.addView(main_row, new TableLayout.LayoutParams());

	} // add_row (head_str, row_strs, lines)

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
			Toast.makeText(GridActivity.this, "Too many columns!!!", Toast.LENGTH_LONG).show();
			try {
				Thread.sleep(Toast.LENGTH_LONG);
			}
			catch (InterruptedException e) {
			}
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
			}

		}

		return value;
	} //  get_significant_data()


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
	/**
	 * This is the info that's published from doInBackground()
	 * to onProgressUpdate().
	 *
	 * It contains all the information that add_row needs to add
	 * a row to the grid.
	 */
	class GridRow {
		/** Name of the exercise. Null if not used. */
		String exer_name = null;
		/** database ID of the exercise */
		int exer_id = -1;
		/** The number of the significant column for this exericise. */
		int exer_sig_marker = -1;
		/** This array that holds the info for all the cells in this row. */
		GridElement tag_array[] = null;
	} // class GridRow


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
	class GridSyncTask extends AsyncTask <Void, Integer, Void> {

		/**
		 * This is a list of all the days that the user has
		 * worked out.
		 */
		ArrayList <MyCalendar> mm_days_list;


		/***************
		 * Called BEFORE the doInBackground(), this allows
		 * something to be done in the UI thread in prepara-
		 * tion for the long stuff, like starting a progress
		 * dialog.
		 */
		@Override
		protected void onPreExecute() {
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
			Cursor ex_cursor = null;

			if (WGlobals.g_db_helper == null) {
				Log.e(tag, "Trying to do something in the background, but g_db_helper is null!!!");
				return null;
			}
			m_db = WGlobals.g_db_helper.getReadableDatabase();

			mm_days_list = construct_days_list();

			// Signal to onProgressUpdate() to make the first row.
			publishProgress (0);

			//////////////////////////////
			//	First Row (dates) is done.
			//////////////////////////////


			// Load up the exercises, ordered as the user wants
			// them (LORDER).
			try {
				ex_cursor =
					m_db.query(
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
					col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_NAME);
					m_row_info[pos].exer_name = ex_cursor.getString(col);
					col = ex_cursor.getColumnIndex(DatabaseHelper.COL_ID);
					m_row_info[pos].exer_id = ex_cursor.getInt(col);
					col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_SIGNIFICANT);
					m_row_info[pos].exer_sig_marker = ex_cursor.getInt(col);

					// Set up the tag_array.  It's used for making the
					// row and REQUIRED by add_row().
					m_row_info[pos].tag_array = construct_tag_array (m_db,
							m_row_info[pos].exer_name,
							m_row_info[pos].exer_sig_marker,
							mm_days_list);

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
					publishProgress(pos + 1);
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

			if (m_db != null) {
				m_db.close();
				m_db = null;
			}

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

			// If the first element in the array is "", then this
			// is the initial calling of this method. AND that
			// means the rest of the array contains dates to
			// display in the top-most row.

			if (row_num[0] == 0) {
				// Construct our array for the first row (which shows only
				// dates) and display it.
				String top_row[] = construct_first_row (mm_days_list);
				add_row_dates (top_row, true);
			}
			else {
				// This perpetuates the HACK in doInBackground()!
				add_row (row_num[0] - 1, false, true);
			}
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
			stop_progress_dialog();
			m_row_info = null;		// Don't need anymore.
			HorizontalScrollView horiz_sv = (HorizontalScrollView) findViewById(R.id.grid_horiz_sv);
			horiz_sv.fullScroll(View.FOCUS_RIGHT);
		}


		/******************
		 * Constructs an ArrayList that contains every day
		 * that the user has worked out.
		 *
		 * @param db		Readable database that's ready to fire.
		 *
		 * @return	List of MyCalendar objects.  Each object
		 * 			is the next day that the user worked out.
		 */
		ArrayList<MyCalendar> construct_days_list() {

			// Construct a list of all the individual days that
			// the user has exercised.
			ArrayList<MyCalendar> days_list = null;

			// Read from the DB what dates are stored there.
			Cursor days_cursor = null;
			try {
				days_cursor = m_db.query(
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
		 * @param days_list		MyCalendar arrayList of all the dates
		 * 						to display.
		 */
		String[] construct_first_row (ArrayList<MyCalendar> days_list) {
			String top_row[] = new String[days_list.size()];
			for (int i = 0; i < days_list.size(); i++) {
				MyCalendar tmp_cal = days_list.get(i);
				top_row[i] = make_grid_date_string (tmp_cal);
			}
			return top_row;
		} // construct_first_row



		/********************
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
		 * @param ex_name	The name of the exercise.
		 *
		 * @param ex_sig		The number (from the DatabaseHelper)
		 * 					of the significant part of this
		 * 					exercise.
		 *
		 * @param days_list	Holds the list of all the days that
		 * 					the user has worked out.
		 *
		 * @return	tag_array, as described above.  It could
		 * 			contain only empty (null) elements.  But
		 * 			it WILL contain the right amount of 'em.
		 */
		GridElement[] construct_tag_array (
								SQLiteDatabase db,
								String ex_name,
								int ex_sig,
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
					new String[] {ex_name},
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

				IntFloat set_sig = get_significant_data(db, set_id, ex_sig);

				// Ready to loop.
				int i = 0;
				do {
					if (days_list.get(i).is_same_day(set_date)) {
						if (tag_array[i] == null) {
							tag_array[i] = new GridElement();
						}
						tag_array[i].add(set_id, set_sig);
						tag_array[i].set_name(ex_name);
						if (cursor.moveToNext() == false) {
							break;
						}
						col = cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
						set_date = new MyCalendar (cursor.getLong(col));

						col = cursor.getColumnIndex(DatabaseHelper.COL_ID);
						set_id = cursor.getInt(col);

						set_sig = get_significant_data(db, set_id, ex_sig);
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

	} // class GridSyncTask

}
