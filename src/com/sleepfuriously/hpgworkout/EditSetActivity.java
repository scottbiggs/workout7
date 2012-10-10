/**
 * This is the screen that pops up to edit an exercise set
 * or delete it entirely.
 */
package com.sleepfuriously.hpgworkout;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class EditSetActivity
					extends
						BaseDialogActivity
					implements
						OnClickListener,
						OnLongClickListener,
						DatePickerDialog.OnDateSetListener,
						TimePickerDialog.OnTimeSetListener {

	//-------------------------
	//	Constants
	//-------------------------

	private static final String tag = "EditSetActivity";

	/**
	 * The key to use when sending this Activity the
	 * id number which tells this Activity the ID of
	 * this set.
	 */
	public static final String ID_KEY = "id";


	//-------------------------
	//	Widgets
	//-------------------------

	/** The labels */
//	TextView m_calendar_date_label_tv, m_calendar_time_label_tv,
//		m_reps_label_tv, m_weight_label_tv, m_level_label_tv,
//		m_cals_label_tv, m_dist_label_tv, m_time_label_tv,
//		m_other_label_tv, m_stress_label_tv, m_notes_label_tv;
	TextView m_weight_label_tv, m_dist_label_tv,
		m_time_label_tv, m_other_label_tv;

	/** The data portion of this screen */
	TextView m_calendar_date_data_tv, m_calendar_time_data_tv,
		m_reps_data_tv, m_weight_data_tv, m_level_data_tv,
		m_cals_data_tv, m_dist_data_tv, m_time_data_tv,
		m_other_data_tv, m_stress_data_tv, m_notes_data_tv;

	/** The clearly-marked buttons for this screen */
	Button m_delete, m_done, m_cancel;
	ImageView m_help;


	//-------------------------
	//	Class Data
	//-------------------------

	/** When TRUE, the user changed something. */
	private boolean m_dirty = false;

	/** Holds the ID for this exercise set */
	private int m_id;

	/** The actual name of this exercise. Useful! */
	private String m_exercise_name;

	/** The number of the significant aspect of this exercise */
	private int m_significant;


	///////////////////
	// These variables hold the real data that is displayed
	// in the widgets.
	//
	//	NOTE:
	//		That -1 means it hasn't been initialized or
	//		that data is not available.  The user cannot
	//		enter negative numbers.
	//
	/** Holds the exact time this set takes place */
	private MyCalendar m_set_date;

	/** Actual rep count */
	private int m_reps = -1;

	/** Actual weight */
	private float m_weight = -1;

	/** Actual level */
	private int m_level = -1;

	/** Actual calories */
	private int m_calories = -1;

	/** Actual distance */
	private float m_dist = -1;

	/** Actual time */
	private float m_time = -1;

	/** actual other amount */
	private float m_other = -1;

	/** actual stress level */
	private int m_stress = DatabaseHelper.SET_COND_NONE;

	/**
	 * Holds the actual (not displayed, because I show
	 * something instead of empty) notes.
	 */
	private String m_notes = null;


	//-------------------------
	//	Sub Classes
	//-------------------------

	private class NumString {
		String str = null;
		int i = -1;
		float f = -1;
	}


	//-------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editset);

		Log.i(tag, "Entering onCreate()");

		m_weight_label_tv = (TextView) findViewById(R.id.editset_weight_label_tv);
//		m_level_label_tv = (TextView) findViewById(R.id.editset_level_label_tv);
//		m_cals_label_tv = (TextView) findViewById(R.id.editset_cals_label_tv);
		m_dist_label_tv = (TextView) findViewById(R.id.editset_dist_label_tv);
		m_time_label_tv = (TextView) findViewById(R.id.editset_time_label_tv);
		m_other_label_tv = (TextView) findViewById(R.id.editset_other_label_tv);
//		m_stress_label_tv = (TextView) findViewById(R.id.editset_stress_label_tv);
//		m_notes_label_tv = (TextView) findViewById(R.id.editset_notes_label_tv);

		m_calendar_date_data_tv = (TextView) findViewById(R.id.editset_date_data_tv);
		m_calendar_date_data_tv.setOnClickListener(this);
		m_calendar_time_data_tv = (TextView) findViewById(R.id.editset_calendar_time_data_tv);
		m_calendar_time_data_tv.setOnClickListener(this);
		m_reps_data_tv = (TextView) findViewById(R.id.editset_reps_data_tv);
		m_reps_data_tv.setOnClickListener(this);
		m_weight_data_tv = (TextView) findViewById(R.id.editset_weight_data_tv);
		m_weight_data_tv.setOnClickListener(this);
		m_level_data_tv = (TextView) findViewById(R.id.editset_level_data_tv);
		m_level_data_tv.setOnClickListener(this);
		m_cals_data_tv = (TextView) findViewById(R.id.editset_cals_data_tv);
		m_cals_data_tv.setOnClickListener(this);
		m_dist_data_tv = (TextView) findViewById(R.id.editset_dist_data_tv);
		m_dist_data_tv.setOnClickListener(this);
		m_time_data_tv = (TextView) findViewById(R.id.editset_time_data_tv);
		m_time_data_tv.setOnClickListener(this);
		m_other_data_tv = (TextView) findViewById(R.id.editset_other_data_tv);
		m_other_data_tv.setOnClickListener(this);
		m_stress_data_tv = (TextView) findViewById(R.id.editset_stress_data_tv);
		m_stress_data_tv.setOnClickListener(this);

		m_notes_data_tv = (TextView) findViewById(R.id.editset_notes_data_tv);
		m_notes_data_tv.setOnClickListener(this);

		m_delete = (Button) findViewById(R.id.editset_delete_butt);
		m_delete.setOnClickListener(this);
		m_done = (Button) findViewById(R.id.editset_ok_butt);
		m_done.setOnClickListener(this);
		m_cancel = (Button) findViewById(R.id.editset_cancel_butt);
		m_cancel.setOnClickListener(this);
		m_help = (ImageView) findViewById(R.id.editset_logo);
		m_help.setOnClickListener(this);

		// Load up stuff from the database.
		fill_forms();

		TextView name_tv = (TextView) findViewById(R.id.editset_ex_name_tv);
		name_tv.setText(m_exercise_name);
	} // onCreate (.)


	//-------------------------
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED) {
			return;
		}

		if (requestCode == WGlobals.NUMBERACTIVITY) {
			m_dirty = true;
			int ex_num = data.getIntExtra(NumberActivity.ITT_KEY_RETURN_NUM, -1);
			switch (ex_num) {
				case DatabaseHelper.EXERCISE_COL_REP_NUM:
					{
						m_reps = Integer.parseInt(data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE));
						m_reps_data_tv.setText("" + m_reps);
					}
					break;

				case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
					{
						m_weight = Float.parseFloat(data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE));
						m_weight_data_tv.setText("" + m_weight);
					}
					break;

				case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
					{
						m_level = Integer.parseInt(data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE));
						m_level_data_tv.setText("" + m_level);
					}
					break;

				case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
					{
						m_calories = Integer.parseInt(data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE));
						m_cals_data_tv.setText("" + m_calories);
					}
					break;

				case DatabaseHelper.EXERCISE_COL_DIST_NUM:
					{
						m_dist = Float.parseFloat(data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE));
						m_dist_data_tv.setText("" + m_dist);
					}
					break;

				case DatabaseHelper.EXERCISE_COL_TIME_NUM:
					{
						m_time = Float.parseFloat(data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE));
						m_time_data_tv.setText("" + m_time);
					}
					break;

				case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
					{
						m_other = Float.parseFloat(data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE));
						m_other_data_tv.setText("" + m_other);
					}
					break;

				default:
					Toast.makeText(this, "Illegal ex_num in onActivityResult()", Toast.LENGTH_LONG).show();
					break;
			}
		} // returned from NumberActivity


		else if (requestCode == WGlobals.TEXTACTIVITY) {
			m_dirty = true;
			// We got some text for our notes.
			m_notes = data.getStringExtra(TextActivity.ITT_KEY_RETURN_STRING);
			if ((m_notes != null) && (m_notes.length() > 0)) {
				// There's a note!  Display it.
				m_notes_data_tv.setText(m_notes);
			}
			else {
				m_notes_data_tv.setText(R.string.editset_empty_notes);
			}
		} // from a change to the notes.

		else if (requestCode == WGlobals.STRESSACTIVITY) {
			m_dirty = true;
			m_stress = data.getIntExtra(StressActivity.ITT_KEY_RETURN_STRESS, DatabaseHelper.SET_COND_NONE);
			switch (m_stress) {
				case DatabaseHelper.SET_COND_OK:
					m_stress_data_tv.setText(R.string.aset_cond_stress_ok);
					break;
				case DatabaseHelper.SET_COND_MINUS:
					m_stress_data_tv.setText(R.string.aset_cond_stress_too_hard);
					break;
				case DatabaseHelper.SET_COND_PLUS:
					m_stress_data_tv.setText(R.string.aset_cond_stress_too_easy);
					break;
				case DatabaseHelper.SET_COND_INJURY:
					m_stress_data_tv.setText(R.string.aset_cond_stress_injury);
					break;
				default:
					m_stress_data_tv.setText(R.string.aset_cond_stress_none);
					break;
			}
		} // stress change

	} // onActivityResult (...)


	//-------------------------
	public void onClick(View v) {
		if (v.getClass() == TextView.class) {
			if (v == m_calendar_date_data_tv) {
				new DatePickerDialog(this, this, m_set_date.get_year(), m_set_date.get_month(), m_set_date.get_day())
					.show();
			}
			else if (v == m_calendar_time_data_tv) {
				new TimePickerDialog(this, this, m_set_date.get_hour(), m_set_date.get_minutes(), true)
					.show();
			}
			else if (v == m_reps_data_tv) {
				activate_number_activity (DatabaseHelper.EXERCISE_COL_REP_NUM,
						getString(R.string.editset_reps_label),
						m_reps_data_tv.getText().toString(), false);
			}
			else if (v == m_weight_data_tv) {
				activate_number_activity (DatabaseHelper.EXERCISE_COL_WEIGHT_NUM,
						m_weight_label_tv.getText().toString(),
						m_weight_data_tv.getText().toString(), true);
			}
			else if (v == m_level_data_tv) {
				activate_number_activity (DatabaseHelper.EXERCISE_COL_LEVEL_NUM,
						getString(R.string.editset_level_label),
						m_level_data_tv.getText().toString(), false);
			}
			else if (v == m_cals_data_tv) {
				activate_number_activity (DatabaseHelper.EXERCISE_COL_CALORIE_NUM,
						getString(R.string.editset_cals_label),
						m_cals_data_tv.getText().toString(), false);
			}
			else if (v == m_dist_data_tv) {
				activate_number_activity (DatabaseHelper.EXERCISE_COL_DIST_NUM,
						m_dist_label_tv.getText().toString(),
						m_dist_data_tv.getText().toString(), true);
			}
			else if (v == m_time_data_tv) {
				activate_number_activity (DatabaseHelper.EXERCISE_COL_TIME_NUM,
						m_time_label_tv.getText().toString(),
						m_time_data_tv.getText().toString(), true);
			}
			else if (v == m_other_data_tv) {
				activate_number_activity (DatabaseHelper.EXERCISE_COL_OTHER_NUM,
						m_other_label_tv.getText().toString(),
						m_other_data_tv.getText().toString(), true);
			}
			else if (v == m_stress_data_tv) {
				activate_stress_activity();
			}
			else if (v == m_notes_data_tv) {
				activate_text_activity();
			}
		} // if TextView

		else if (v == m_delete) {
			String msg_args[] = {
			                     m_exercise_name,
			                     m_exercise_name,
			                     m_calendar_date_data_tv.getText().toString(),
			                     m_calendar_time_data_tv.getText().toString()
			};
			show_yes_no_dialog(R.string.editset_delete_warning_title,
					null,
					R.string.editset_delete_warning_msg,
					msg_args,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// YES, they want to delete it!
							delete_set();
							setResult(RESULT_OK);
							finish();
						}
					});
		}

		else if (v == m_help) {
			show_help_dialog(R.string.editset_help_title,
					R.string.editset_help_msg);
		}

		else if (v == m_done) {
			if (!m_dirty) {
				setResult(RESULT_CANCELED);
				finish();
			}
			else {
				save_and_exit();
			}
		}

		else if (v == m_cancel) {
			if (m_dirty && WGlobals.g_nag) {
				show_yes_no_dialog(R.string.editset_cancel_warning_title, null,
						R.string.editset_cancel_warning_msg, null,
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Yes, they want to cancel.
						setResult(RESULT_CANCELED);
						finish();
						}
				});

			}
			else {
				setResult(RESULT_CANCELED);
				finish();
			}
		} // cancel

	} // onClick (v)

	//-------------------------
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	//-------------------------
	//	Fires once a date has been set with a dialog.
	//
	public void onDateSet(DatePicker v,
	                      int year, int month, int day) {
		m_set_date.set_year_month_day(year, month, day);
		String date = String.format("%d/%02d/%d", m_set_date.get_month_12(), m_set_date.get_day(), m_set_date.get_year());
		m_calendar_date_data_tv.setText(date);
		m_dirty = true;
	} // onDateSet (v, year, month, day)


	//-------------------------
	//	Fires when the time has been changed via a TimePicker.
	//
	public void onTimeSet(TimePicker v, int hours, int mins) {
		m_set_date.set_time(hours, mins, 0);
		m_calendar_time_data_tv.setText(m_set_date.print_time(false));
		m_dirty = true;
	}


	/***********************
	 * A helper method that looks at the Cursor and figures out
	 * the number that's returned.  If there is no number on
	 * that column, then the proper empty string is returned.
	 *
	 * New and Improved!  This not only returns the string,
	 * but also returns the number, too!  But wait, there's
	 * more...you also get your choice of int or float!
	 *
	 * Now how much would you pay for that?
	 */
	private NumString get_data_str (Cursor cursor,
	                                String column_name,
	                                boolean is_float) {
		NumString ret = new NumString();
		int col = cursor.getColumnIndex(column_name);


		// If it's null, then treat a little differently.
		String test = cursor.getString(col);
		if (test == null) {
			ret.str = getString(R.string.inspector_null_value);
			return ret;
		}

		if (is_float) {
			ret.f = cursor.getFloat(col);
			if (ret.f == -1) {
				ret.str = getString(R.string.inspector_skipped_value);
				return ret;
			}
			ret.str = "" + ret.f;
		}
		else {
			ret.i = cursor.getInt(col);
			if (ret.i == -1) {
				ret.str = getString(R.string.inspector_skipped_value);
				return ret;
			}
			ret.str = "" + ret.i;
		}
		return ret;
	} // get_data_str (cursor, column_name, is_float)


	/***********************
	 * Calls a new Activity (which looks like a Dialog)
	 * to get a number.
	 *
	 * @param ex_num		The exercise number.  Used for
	 * 					callbacks.
	 *
	 * @param title		The title of this Screen. How
	 * 					about the set aspect that we're
	 * 					modifying?
	 *
	 * @param orig		The original number (in string
	 * 					form).  Use NULL if n/a.
	 *
	 * @param dec_point	Do you need a decimal point?
	 *
	 */
	private void activate_number_activity (int ex_num,
	                                       String title,
	                                       String orig,
	                                       boolean dec_point) {

		// Fill in the data for the new Activity.
		Intent itt = new Intent(this, NumberActivity.class);
		itt.putExtra(NumberActivity.ITT_KEY_TITLE, title);

		if (orig != null) {
			itt.putExtra(NumberActivity.ITT_KEY_OLD_VALUE_BOOL, true);
			itt.putExtra(NumberActivity.ITT_KEY_OLD_VALUE_STRING, orig);
		}
		else {
			itt.putExtra(NumberActivity.ITT_KEY_OLD_VALUE_BOOL, false);
		}

		itt.putExtra(NumberActivity.ITT_KEY_DECIMAL_BOOL, dec_point);

		itt.putExtra(NumberActivity.ITT_KEY_RETURN_NUM, ex_num);

		startActivityForResult(itt, WGlobals.NUMBERACTIVITY);

	} // activate_number_activity (title, orig, dec_point)


	/***********************
	 * This is called when the user wants to change their
	 * notes.  This calls up a new Activity to let 'em
	 * type in their note.  It's much simpler than the
	 * Activity above.  But we still have to grab the
	 * result in onActivityResult().
	 */
	private void activate_text_activity() {
		Intent itt = new Intent (this, TextActivity.class);
		String note = m_notes_data_tv.getText().toString();

		if ((m_notes != null) && (m_notes.length() > 0)) {
			// There's a note to display.
			itt.putExtra(TextActivity.ITT_KEY_OLD_NOTE_BOOL, true);
			itt.putExtra(TextActivity.ITT_KEY_OLD_NOTE_STRING, note);
		}
		else {
			itt.putExtra(TextActivity.ITT_KEY_OLD_NOTE_BOOL, false);
		}

		startActivityForResult(itt, WGlobals.TEXTACTIVITY);
	} // activate_text_activity()

	/***********************
	 * Pulls up the StressActivity.  Just like the two above
	 * methods.
	 */
	private void activate_stress_activity() {
		Intent itt = new Intent (this, StressActivity.class);
		if (m_stress != DatabaseHelper.SET_COND_NONE) {
			itt.putExtra(StressActivity.ITT_KEY_SHOW_OLD_BOOL, true);
			itt.putExtra(StressActivity.ITT_KEY_OLD_STRESS, m_stress);
		}
		else {
			itt.putExtra(StressActivity.ITT_KEY_SHOW_OLD_BOOL, false);
		}

		startActivityForResult(itt, WGlobals.STRESSACTIVITY);
	} // activate_stress_activity()


	/***********************
	 * Call this when everything is done and we want to
	 * save the current state and exit this Activity.
	 */
	private void save_and_exit() {

		ContentValues values = new ContentValues();

		values.put(DatabaseHelper.SET_COL_DATEMILLIS, m_set_date.m_cal.getTimeInMillis());
		values.put(DatabaseHelper.SET_COL_REPS, m_reps);
		values.put(DatabaseHelper.SET_COL_WEIGHT, m_weight);
		values.put(DatabaseHelper.SET_COL_LEVELS, m_level);
		values.put(DatabaseHelper.SET_COL_CALORIES, m_calories);
		values.put(DatabaseHelper.SET_COL_DIST, m_dist);
		values.put(DatabaseHelper.SET_COL_TIME, m_time);
		values.put(DatabaseHelper.SET_COL_OTHER, m_other);

		values.put(DatabaseHelper.SET_COL_CONDITION, m_stress);
		values.put(DatabaseHelper.SET_COL_NOTES, m_notes);

		m_db = WGlobals.g_db_helper.getWritableDatabase();
		int num_rows = m_db.update(DatabaseHelper.SET_TABLE_NAME,
						values,
						DatabaseHelper.COL_ID + "=" + m_id, null);
		m_db.close();

		Log.v(tag, "save_and_exit() updated " + num_rows + " rows");

//		// Notify other Activities to reload
//		InspectorActivity.m_db_dirty = true;
//		HistoryActivity.m_db_dirty = true;
//		GraphActivity.m_db_dirty = true;

		setResult(RESULT_OK);
		finish();
	} // save_and_exit()


	/**********************
	 * Removes this set from the database.
	 */
	private void delete_set() {
		m_db = WGlobals.g_db_helper.getWritableDatabase();
		m_db.delete(DatabaseHelper.SET_TABLE_NAME,
				DatabaseHelper.COL_ID + "=" + m_id, null);
		m_db.close();
	} // delete_set()


	/**********************
	 * Does the dirty work of finding out the milliseconds
	 * from the date and time that's currently displayed.
	 */
	private long get_millis() {
		String month_str, day_str, year_str,
			hour_str, min_str;
		int month, days, year, hours, mins;
		boolean pm;
		int counter;

		// Start by seperating all the components into their
		// respective strings.
		CharSequence date_str = m_calendar_date_data_tv.getText();
		if (date_str.charAt(0) == '1') {
			month_str = "" + date_str.charAt(0) + date_str.charAt(1);
			counter = 3;
		}
		else {
			month_str = "" + date_str.charAt(0);
			counter = 2;
		}

		day_str = "" + date_str.charAt(counter) + date_str.charAt(counter + 1);
		counter += 3;

		year_str = "" + date_str.charAt(counter) + date_str.charAt(counter + 1)
				+ date_str.charAt(counter + 2) + date_str.charAt(counter + 3);


		CharSequence time_str = m_calendar_time_data_tv.getText();
		if (time_str.charAt(0) == '1') {
			hour_str = "" + time_str.charAt(0) + time_str.charAt(1);
			counter = 3;
		}
		else {
			hour_str = "" + time_str.charAt(0);
			counter = 2;
		}

		min_str = "" + time_str.charAt(counter) + time_str.charAt(counter + 1);
		counter += 3;

		pm = time_str.charAt(counter) == 'p' ? true : false;


		// Now translate all these strings into numbers.
		year = Integer.parseInt(year_str);
		month = Integer.parseInt(month_str);
		days = Integer.parseInt(day_str);
		hours = Integer.parseInt(hour_str);
		mins = Integer.parseInt(min_str);
		if (pm) hours += 12;

		MyCalendar cal = new MyCalendar();
		cal.set_year_month_day(year, month, days);
		cal.set_time(hours, mins, 0);

		return cal.m_cal.getTimeInMillis();
	} // get_millis()


	/*********************
	 * Fills in all the widgets (which are identified earlier)
	 * with info from the database.
	 */
	private void fill_forms() {
		int col;
		boolean set_bar = false;		// used to determine whether or not to draw a divider bar

		// Get the info from the Intent that GridActivity sent.
		Intent itt = getIntent();
		m_id = itt.getIntExtra(ID_KEY, -1);
		if (m_id == -1) {
			Toast.makeText(this, "Problem trying to get the set ID in fill_forms()", Toast.LENGTH_LONG).show();
			Log.e(tag, "Problem trying to get the set ID in fill_forms()");
			return;
		}
		Log.v (tag, "fill_forms() for set id = " + m_id);


		try {
			m_db = WGlobals.g_db_helper.getReadableDatabase();

			Cursor set_cursor = null;
			try {
				set_cursor = m_db.query(
						DatabaseHelper.SET_TABLE_NAME,
						null,	// all columns
						DatabaseHelper.COL_ID + "=?",
						new String[] {"" + m_id},
						null, null, null, null);
				set_cursor.moveToFirst();

				// Grab the name.
				col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_NAME);
				m_exercise_name = set_cursor.getString(col);

				// Need to know some stuff about this exercise.
				Cursor ex_cursor = null;
				try {
					ex_cursor = m_db.query(
								DatabaseHelper.EXERCISE_TABLE_NAME,	// table
								null,			//	columns[]
					            DatabaseHelper.EXERCISE_COL_NAME + "=?",//selection
					            new String[] {m_exercise_name},// selectionArgs[]
					            null,	//	groupBy
					            null,	//	having
					            null,	//	orderBy
					            null);
					ex_cursor.moveToFirst();

					// Need to know a bit about this exercise
					col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_SIGNIFICANT);
					m_significant = ex_cursor.getInt(col);

					// Fill the widgets
					setup_date (set_cursor);
					set_bar = setup_reps (ex_cursor, set_cursor);
					set_bar = setup_weight (ex_cursor, set_cursor, set_bar);
					set_bar = setup_level (ex_cursor, set_cursor, set_bar);
					set_bar = setup_cals (ex_cursor, set_cursor, set_bar);
					set_bar = setup_dist (ex_cursor, set_cursor, set_bar);
					set_bar = setup_time (ex_cursor, set_cursor, set_bar);
					set_bar = setup_other (ex_cursor, set_cursor, set_bar);
					setup_stress (set_cursor);
					setup_notes (set_cursor);


				}
				catch (SQLiteException e) {
					e.printStackTrace();
				}
				finally {
					if (ex_cursor != null) {
						ex_cursor.close();
					}
				}
			}
			catch (SQLiteException e) {
				e.printStackTrace();
			}
			finally {
				if (set_cursor != null) {
					set_cursor.close();
				}
			}
		} // try m_db
		catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (m_db != null) {
				m_db.close();
				m_db = null;
			}
		}

	} // fill_forms()


	/**
	 * DATE and TIME
	 *
	 * @param set_cursor		A Cursor that holds all the
	 * 						info about this workout set.
	 */
	private void setup_date (Cursor set_cursor) {
		int col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_DATEMILLIS);
		long date_millis = set_cursor.getLong(col);
		m_set_date = new MyCalendar(date_millis);

		// And display the date and time of this particular set.
		String date = String.format("%d/%02d/%d", m_set_date.get_month_12(), m_set_date.get_day(), m_set_date.get_year());
		m_calendar_date_data_tv.setText(date);

		m_calendar_time_data_tv.setText(m_set_date.print_time(false));
	}


	/**
	 * REPS
	 *
	 * @param ex_cursor		A Cursor that holds all the
	 * 						info about this exercise.
	 *
	 * @param set_cursor		A Cursor that holds all the
	 * 						info about this workout set.
	 *
	 * @return	Whether or not to set the set_bar variable
	 * 			to true.
	 */
	private boolean setup_reps (Cursor ex_cursor, Cursor set_cursor) {
		LinearLayout reps_ll = (LinearLayout) findViewById(R.id.editset_reps_ll);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_REP);
		boolean reps = ex_cursor.getInt(col) == 1 ? true : false;
		if (reps) {
			NumString data = get_data_str (set_cursor, DatabaseHelper.SET_COL_REPS, false);
			m_reps_data_tv.setText(data.str);
			m_reps = data.i;
			if (DatabaseHelper.EXERCISE_COL_REP_NUM == m_significant) {
				TextView reps_label_tv = (TextView) findViewById(R.id.editset_reps_label_tv);
				reps_label_tv.setTypeface(null, Typeface.BOLD);
			}
			return true;
		}
		else {
			// If not applicable, make it disappear!
			reps_ll.setVisibility(View.GONE);
			return false;
		}
	} // reps


	/**
	 * WEIGHT
	 *
	 * @param set_bar	Current value of set_bar.  It's
	 * 					used to know if we need to draw
	 * 					it or not.
	 */
	private boolean setup_weight (Cursor ex_cursor, Cursor set_cursor,
	                              boolean set_bar) {
		LinearLayout weight_ll = (LinearLayout) findViewById(R.id.editset_weight_ll);
		View bar = findViewById(R.id.editset_weight_bar);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT);
		boolean weight = ex_cursor.getInt(col) == 1 ? true : false;
		if (weight) {
			// Unit of Weight
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT);
			String weight_unit = ex_cursor.getString(col);

			m_weight_label_tv = (TextView) findViewById(R.id.editset_weight_label_tv);
			String weight_label_str = getString (R.string.editset_weight_label, (Object[]) new String[] {weight_unit});
			m_weight_label_tv.setText(weight_label_str);
			if (DatabaseHelper.EXERCISE_COL_WEIGHT_NUM == m_significant) {
				m_weight_label_tv.setTypeface(null, Typeface.BOLD);
			}


			NumString data = get_data_str (set_cursor, DatabaseHelper.SET_COL_WEIGHT, true);
			m_weight_data_tv = (TextView) findViewById(R.id.editset_weight_data_tv);
			m_weight_data_tv.setText(data.str);
			m_weight = data.f;

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
	} // weight


	/**
	 * LEVEL
	 */
	private boolean setup_level (Cursor ex_cursor, Cursor set_cursor,
	                             boolean set_bar) {
		View bar = findViewById(R.id.editset_level_bar);
		LinearLayout level_ll = (LinearLayout)
			findViewById(R.id.editset_level_ll);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LEVEL);
		boolean level = ex_cursor.getInt(col) == 1 ? true : false;
		if (level) {
			NumString data = get_data_str (set_cursor, DatabaseHelper.SET_COL_LEVELS, false);
			m_level_data_tv = (TextView)
				findViewById(R.id.editset_level_data_tv);
			m_level_data_tv.setText(data.str);
			if (DatabaseHelper.EXERCISE_COL_LEVEL_NUM == m_significant) {
				TextView level_label_tv = (TextView) findViewById(R.id.editset_level_label_tv);
				level_label_tv.setTypeface(null, Typeface.BOLD);
			}
			m_level = data.i;

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

	/**
	 * CALORIES
	 */
	private boolean setup_cals (Cursor ex_cursor, Cursor set_cursor,
	                             boolean set_bar) {
		LinearLayout cals_ll = (LinearLayout) findViewById(R.id.editset_cals_ll);
		View bar = findViewById(R.id.editset_cals_bar);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_CALORIES);
		boolean cals = ex_cursor.getInt(col) == 1 ? true : false;
		if (cals) {
			NumString data = get_data_str (set_cursor, DatabaseHelper.SET_COL_CALORIES, false);
			m_cals_data_tv = (TextView) findViewById(R.id.editset_cals_data_tv);
			m_cals_data_tv.setText(data.str);
			if (DatabaseHelper.EXERCISE_COL_CALORIE_NUM == m_significant) {
				TextView cals_label_tv = (TextView) findViewById(R.id.editset_cals_label_tv);
				cals_label_tv.setTypeface(null, Typeface.BOLD);
			}
			m_calories = data.i;

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

	/**
	 * DIST
	 */
	private boolean setup_dist (Cursor ex_cursor, Cursor set_cursor,
	                              boolean set_bar) {
		LinearLayout dist_ll = (LinearLayout) findViewById(R.id.editset_dist_ll);
		View bar = findViewById(R.id.editset_dist_bar);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST);
		boolean dist = ex_cursor.getInt(col) == 1 ? true : false;
		if (dist) {
			// Unit of Distance
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST_UNIT);
			String dist_unit = ex_cursor.getString(col);

			m_dist_label_tv = (TextView) findViewById(R.id.editset_dist_label_tv);
			String dist_label_str = getString (R.string.inspector_set_dist_label, dist_unit);
			m_dist_label_tv.setText(dist_label_str);
			if (DatabaseHelper.EXERCISE_COL_DIST_NUM == m_significant) {
				m_dist_label_tv.setTypeface(null, Typeface.BOLD);
			}

			NumString data = get_data_str (set_cursor, DatabaseHelper.SET_COL_DIST, true);
			m_dist_data_tv = (TextView) findViewById(R.id.editset_dist_data_tv);
			m_dist_data_tv.setText(data.str);
			m_dist = data.f;

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


	/**
	 * TIME
	 */
	private boolean setup_time (Cursor ex_cursor, Cursor set_cursor,
	                              boolean set_bar) {
		LinearLayout time_ll = (LinearLayout) findViewById(R.id.editset_time_ll);
		View bar = findViewById(R.id.editset_time_bar);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME);
		boolean time = ex_cursor.getInt(col) == 1 ? true : false;
		if (time) {
			// Unit of Time
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME_UNIT);
			String time_unit = ex_cursor.getString(col);

			m_time_label_tv = (TextView) findViewById(R.id.editset_time_label_tv);
			String time_label_str = getString (R.string.inspector_set_time_label, time_unit);
			m_time_label_tv.setText(time_label_str);
			if (DatabaseHelper.EXERCISE_COL_TIME_NUM == m_significant) {
				m_time_label_tv.setTypeface(null, Typeface.BOLD);
			}

			NumString data = get_data_str (set_cursor, DatabaseHelper.SET_COL_TIME, true);
			m_time_data_tv = (TextView) findViewById(R.id.editset_time_data_tv);
			m_time_data_tv.setText(data.str);
			m_time = data.f;

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

	/**
	 * OTHER
	 */
	private boolean setup_other (Cursor ex_cursor, Cursor set_cursor,
	                              boolean set_bar) {
		LinearLayout other_ll = (LinearLayout) findViewById(R.id.editset_other_ll);
		View bar = findViewById(R.id.editset_other_bar);

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

			m_other_label_tv = (TextView) findViewById(R.id.editset_other_label_tv);
			String other_label_str = getString (R.string.inspector_set_other_label, other_title, other_unit);
			m_other_label_tv.setText(other_label_str);
			if (DatabaseHelper.EXERCISE_COL_OTHER_NUM == m_significant) {
				m_other_label_tv.setTypeface(null, Typeface.BOLD);
			}

			NumString data = get_data_str (set_cursor, DatabaseHelper.SET_COL_OTHER, true);
			m_other_data_tv = (TextView) findViewById(R.id.editset_other_data_tv);
			m_other_data_tv.setText(data.str);
			m_other = data.f;

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


	/**
	 * STRESS
	 */
	private void setup_stress (Cursor set_cursor) {
		int col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_CONDITION);
		m_stress = set_cursor.getInt(col);
		switch (m_stress) {
			case DatabaseHelper.SET_COND_OK:
				m_stress_data_tv.setText(R.string.aset_cond_stress_ok);
				break;
			case DatabaseHelper.SET_COND_MINUS:
				m_stress_data_tv.setText(R.string.aset_cond_stress_too_hard);
				break;
			case DatabaseHelper.SET_COND_PLUS:
				m_stress_data_tv.setText(R.string.aset_cond_stress_too_easy);
				break;
			case DatabaseHelper.SET_COND_INJURY:
				m_stress_data_tv.setText(R.string.aset_cond_stress_injury);
				break;
			default:
				m_stress_data_tv.setText(R.string.aset_cond_stress_error);
				break;
		}
	} // stress


	/**
	 * NOTES
	 */
	private void setup_notes (Cursor set_cursor) {
		int col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_NOTES);
		m_notes = set_cursor.getString(col);
		if ((m_notes != null) && (m_notes.length() > 0)) {
			// There's a note!  Display it.
			m_notes_data_tv.setText(m_notes);
		}
	} // notes


}
