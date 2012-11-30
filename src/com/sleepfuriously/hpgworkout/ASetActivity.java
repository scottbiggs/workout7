/**
 * This is the Activity that pops up when the user
 * is filling in the details of a single set of a
 * workout.
 *
 * NO, these are no longer used.  They have been moved to
 * 	ExerciseTabHostActivity!!!
 *
 * input:
 * 	ROW_KEY		This is the row of the exercise that we're
 * 				doing.  What's a row?  It's the LORDER of
 * 				this exercise.
 *
 * output:
 * 	ID_KEY		The id of the SET of this exercise that
 * 				has just been created.  Not used if the
 * 				RESULT_CANCELLED is sent back.
 *
 * It may be called from the GridActivity or the
 * InspectorActivity.
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;
import java.util.Calendar;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class ASetActivity
				extends BaseDialogActivity
				implements
					OnClickListener,
					OnLongClickListener,
					TextWatcher,
					OnKeyListener {


	//------------------------
	//	Constants
	//------------------------

	private static final String tag = "ASetActivity";

	/** The custom width of each wheel. */
	public static final int WHEEL_DEFAULT_CUSTOM_WIDTH = 10;


	//------------------------
	//	Widget Data
	//------------------------

	Button m_done, m_clear;

	EditText m_reps_et, m_weight_et, m_level_et,
		m_dist_et,
		m_time_et,
		m_other_et,
		m_notes_et, m_calorie_et;


	RadioButton m_ok_rb, m_plus_rb, m_minus_rb, m_x_rb;

	WheelInt m_reps_wheels, m_level_wheels, m_calorie_wheels;

	WheelFloat m_weight_wheels, m_dist_wheels, m_time_wheels, m_other_wheels;


	//------------------------
	//	Class Data
	//------------------------

	/**
	 * This tells if any of the widgets for this
	 * Activity have been changed.
	 */
	private boolean m_widgets_dirty = false;

	/**
	 * True means that all the UI needs to be redone.
	 * This is probably caused by another activity (like
	 * EditExerciseActivity) making changes.
	 * It starts true because we need to start somewhere!
	 */
	public static boolean m_reset_widgets = true;

	/**
	 * When true, this indicates that the database has been changed.
	 * Primarily, this means that when Activity calls finish(),
	 * it needs to tell GridActivity to reload.
	 */
	public static boolean m_db_dirty = false;

	/** The name of this damn exercise! */
	private String m_exercise_name;

	/** Holds strings for 'Other' exercise */
	private String m_other_title, m_other_unit;

	/** So we know which exercise is the significant one. */
	private int m_significant;

	/** Whether or not this particular exercise applies these aspects */
	protected boolean m_reps, m_weight, m_levels, m_calories,
			m_distanced, m_timed, m_other;

	/**
	 * Is this the FIRST TIME the user has ever added a set for
	 * this exercise?
	 */
	protected boolean m_first_time = false;


	//------------------------
	//	Methods
	//------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);

		if (WGlobals.g_wheel) {
			setContentView(R.layout.aset_wheel);
			init_widgets_with_wheels();
			init_wheels();
		}
		else {
			setContentView(R.layout.aset);
			init_widgets_no_wheels();
		}

		m_reset_widgets = true;	// Fill the forms the first time this
								// activity is called.


	} // onCreate (.)

	/***********************
	 * Sets up all the global widgets variables for
	 * the layout that does NOT contain wheels.
	 */
	protected void init_widgets_no_wheels() {
		m_reps_et = (EditText) findViewById(R.id.aset_reps_et);
		m_reps_et.setOnLongClickListener(this);
		m_reps_et.setOnKeyListener(this);
		m_reps_et.addTextChangedListener(this);

		m_weight_et = (EditText) findViewById(R.id.aset_weight_et);
		m_weight_et.setOnLongClickListener(this);
		m_weight_et.setOnKeyListener(this);
		m_weight_et.addTextChangedListener(this);

		m_level_et = (EditText) findViewById(R.id.aset_level_et);
		m_level_et.setOnLongClickListener(this);
		m_level_et.setOnKeyListener(this);
		m_level_et.addTextChangedListener(this);

		m_calorie_et = (EditText) findViewById(R.id.aset_calorie_et);
		m_calorie_et.setOnLongClickListener(this);
		m_calorie_et.setOnKeyListener(this);
		m_calorie_et.addTextChangedListener(this);

		m_dist_et = (EditText) findViewById(R.id.aset_dist_et);
		m_dist_et.setOnLongClickListener(this);
		m_dist_et.setOnKeyListener(this);
		m_dist_et.addTextChangedListener(this);

		m_time_et = (EditText) findViewById(R.id.aset_time_et);
		m_time_et.setOnLongClickListener(this);
		m_time_et.setOnKeyListener(this);
		m_time_et.addTextChangedListener(this);

		m_other_et = (EditText) findViewById(R.id.aset_other_et);
		m_other_et.setOnLongClickListener(this);
		m_other_et.setOnKeyListener(this);
		m_other_et.addTextChangedListener(this);

		m_notes_et = (EditText) findViewById(R.id.aset_notes_et);
		m_notes_et.setOnLongClickListener(this);
		m_notes_et.setOnKeyListener(this);
		m_notes_et.addTextChangedListener(this);

		m_ok_rb = (RadioButton) findViewById(R.id.aset_cond_ok_rb);
		m_ok_rb.setOnLongClickListener(this);
		m_ok_rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_ok_rb.setChecked(true);
				m_plus_rb.setChecked(false);
				m_minus_rb.setChecked(false);
				m_x_rb.setChecked(false);
			}
		});

		m_plus_rb = (RadioButton) findViewById(R.id.aset_cond_plus_rb);
		m_plus_rb.setOnLongClickListener(this);
		m_plus_rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_ok_rb.setChecked(false);
				m_plus_rb.setChecked(true);
				m_minus_rb.setChecked(false);
				m_x_rb.setChecked(false);
			}
		});

		m_minus_rb = (RadioButton) findViewById(R.id.aset_cond_minus_rb);
		m_minus_rb.setOnLongClickListener(this);
		m_minus_rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_ok_rb.setChecked(false);
				m_plus_rb.setChecked(false);
				m_minus_rb.setChecked(true);
				m_x_rb.setChecked(false);
			}
		});

		m_x_rb = (RadioButton) findViewById(R.id.aset_cond_injury_rb);
		m_x_rb.setOnLongClickListener(this);
		m_x_rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_ok_rb.setChecked(false);
				m_plus_rb.setChecked(false);
				m_minus_rb.setChecked(false);
				m_x_rb.setChecked(true);
			}
		});

		m_done = (Button) findViewById(R.id.aset_enter_butt);
		m_done.setOnClickListener(this);
		m_done.setOnLongClickListener(this);

		m_clear = (Button) findViewById(R.id.aset_clear_butt);
		m_clear.setOnClickListener(this);
		m_clear.setOnLongClickListener(this);

	} // init_widgets_no_wheels()

	/***********************
	 * Sets up all the global widgets variables for
	 * the layout that DOES contain wheels.
	 */
	protected void init_widgets_with_wheels() {
		m_reps_et = (EditText) findViewById(R.id.aset_wheel_reps_result_et);
		m_reps_et.setOnLongClickListener(this);
		m_reps_et.setOnKeyListener(this);
		m_reps_et.addTextChangedListener(this);

		m_weight_et = (EditText) findViewById(R.id.aset_wheel_weight_et);
		m_weight_et.setOnLongClickListener(this);
		m_weight_et.setOnKeyListener(this);
		m_weight_et.addTextChangedListener(this);

		m_level_et = (EditText) findViewById(R.id.aset_wheel_level_et);
		m_level_et.setOnLongClickListener(this);
		m_level_et.setOnKeyListener(this);
		m_level_et.addTextChangedListener(this);

		m_calorie_et = (EditText) findViewById(R.id.aset_wheel_calorie_et);
		m_calorie_et.setOnLongClickListener(this);
		m_calorie_et.setOnKeyListener(this);
		m_calorie_et.addTextChangedListener(this);

		m_dist_et = (EditText) findViewById(R.id.aset_wheel_dist_et);
		m_dist_et.setOnLongClickListener(this);
		m_dist_et.setOnKeyListener(this);
		m_dist_et.addTextChangedListener(this);

		m_time_et = (EditText) findViewById(R.id.aset_wheel_time_et);
		m_time_et.setOnLongClickListener(this);
		m_time_et.setOnKeyListener(this);
		m_time_et.addTextChangedListener(this);

		m_other_et = (EditText) findViewById(R.id.aset_wheel_other_et);
		m_other_et.setOnLongClickListener(this);
		m_other_et.setOnKeyListener(this);
		m_other_et.addTextChangedListener(this);

		m_notes_et = (EditText) findViewById(R.id.aset_wheel_notes_et);
		m_notes_et.setOnLongClickListener(this);
		m_notes_et.setOnKeyListener(this);
		m_notes_et.addTextChangedListener(this);

		m_ok_rb = (RadioButton) findViewById(R.id.aset_wheel_cond_ok_rb);
		m_ok_rb.setOnLongClickListener(this);
		m_ok_rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_ok_rb.setChecked(true);
				m_plus_rb.setChecked(false);
				m_minus_rb.setChecked(false);
				m_x_rb.setChecked(false);
			}
		});

		m_plus_rb = (RadioButton) findViewById(R.id.aset_wheel_cond_plus_rb);
		m_plus_rb.setOnLongClickListener(this);
		m_plus_rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_ok_rb.setChecked(false);
				m_plus_rb.setChecked(true);
				m_minus_rb.setChecked(false);
				m_x_rb.setChecked(false);
			}
		});

		m_minus_rb = (RadioButton) findViewById(R.id.aset_wheel_cond_minus_rb);
		m_minus_rb.setOnLongClickListener(this);
		m_minus_rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_ok_rb.setChecked(false);
				m_plus_rb.setChecked(false);
				m_minus_rb.setChecked(true);
				m_x_rb.setChecked(false);
			}
		});

		m_x_rb = (RadioButton) findViewById(R.id.aset_wheel_cond_injury_rb);
		m_x_rb.setOnLongClickListener(this);
		m_x_rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_ok_rb.setChecked(false);
				m_plus_rb.setChecked(false);
				m_minus_rb.setChecked(false);
				m_x_rb.setChecked(true);
			}
		});

		m_done = (Button) findViewById(R.id.aset_wheel_enter_butt);
		m_done.setOnClickListener(this);
		m_done.setOnLongClickListener(this);

		m_clear = (Button) findViewById(R.id.aset_wheel_clear_butt);
		m_clear.setOnClickListener(this);
		m_clear.setOnLongClickListener(this);

	} // init_widgets_with_wheels()

	//------------------------
	@Override
	protected void onResume() {
//		Log.v(tag, "onResume()");
		if (m_reset_widgets) {
			fill_forms();
		}
		super.onResume();
	}


	//------------------------------
	//	Allows this Activity to send message to the caller
	//	when the user hits the back button.
	//
	@Override
	public void onBackPressed() {
		if (ExerciseTabHostActivity.m_dirty) {
			tabbed_set_result(RESULT_OK);
			Log.v(tag, "onBackPressed: setting result to OK");
		}
		else {
			tabbed_set_result(RESULT_CANCELED);
			Log.v(tag, "onBackPressed: setting result to CANCEL");
		}
		finish();
	}

	//------------------------
	@Override
	public void onClick(View v) {
		if (v == m_done) {
			test_to_save();
		} // m_ok

		else if (v == m_clear) {
			clear();
		}

	} // onClick (v)


	//------------------------
	@Override
	public boolean onLongClick(View v) {
		if (v == m_reps_et) {
			show_help_dialog (R.string.aset_reps_help_title,
					R.string.aset_reps_help_msg);
			return true;
		}

		else if (v == m_weight_et) {
			show_help_dialog (R.string.aset_weight_help_title,
					R.string.aset_weight_help_msg);
			return true;
		}

		else if (v == m_level_et) {
			show_help_dialog (R.string.aset_level_help_title,
					R.string.aset_level_help_msg);
			return true;
		}

		else if (v == m_calorie_et) {
			show_help_dialog (R.string.aset_calorie_help_title,
					R.string.aset_calorie_help_msg);
			return true;
		}

		else if (v == m_dist_et) {
			show_help_dialog (R.string.aset_distance_help_title,
					R.string.aset_distance_help_msg);
			return true;
		}

		else if (v == m_time_et) {
			show_help_dialog (R.string.aset_time_help_title,
					R.string.aset_time_help_msg);
			return true;
		}

		else if (v == m_other_et) {
			String[] args = {m_other_title, m_other_unit};
			show_help_dialog (R.string.aset_other_help_title, null,
					R.string.aset_other_help_msg, args);
			return true;
		}

		else if ((v == m_ok_rb) ||
				(v == m_minus_rb) ||
				(v == m_plus_rb) ||
				(v == m_x_rb)) {
			show_help_dialog (R.string.aset_radio_help_title,
					R.string.aset_radio_help_msg);
			return true;
		}

		else if (v == m_notes_et) {
			show_help_dialog (R.string.aset_notes_help_title,
					R.string.aset_notes_help_msg);
			return true;
		}

		else if (v == m_done) {
			show_help_dialog(R.string.aset_enter_help_title,
					R.string.aset_enter_help_msg);
			return true;
		}

		else if (v == m_clear) {
			show_help_dialog(R.string.aset_clear_button_help_title,
					R.string.aset_clear_button_help_msg);
			return true;
		}

		return false;
	} // onLongClick (v)


	//------------------------
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
//		Log.v (tag, "onKey() was hit!");
		m_widgets_dirty = true;
		return false;
	}

	//-----------------------------------
	// Since soft keyboards don't fire onKey(), this is needed
	// to see if a software keyboard made any changes.
	//
	@Override
	public void afterTextChanged(Editable s) {
		m_widgets_dirty = true;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}


	/********************
	 * Sets up the NumberWheel widgets, which require a bit
	 * of code to look and work correctly.
	 *
	 * preconditions:
	 * 		The wheel version of the ASet layout has already
	 * 		been loaded.
	 */
	private void init_wheels() {
		// The ints first
		m_reps_wheels = new WheelInt(this, new int[]
						{
						R.id.aset_wheel_reps_1,
						R.id.aset_wheel_reps_10,
						R.id.aset_wheel_reps_100,
						R.id.aset_wheel_reps_1000
						});
		m_reps_wheels.set_tv(m_reps_et);

		m_level_wheels = new WheelInt(this, new int[]
						{
						R.id.aset_wheel_level_1,
						R.id.aset_wheel_level_10,
						R.id.aset_wheel_level_100,
						R.id.aset_wheel_level_1000
						});
		m_level_wheels.set_tv(m_level_et);

		m_calorie_wheels = new WheelInt(this, new int[]
						{
						R.id.aset_wheel_calorie_1,
						R.id.aset_wheel_calorie_10,
						R.id.aset_wheel_calorie_100,
						R.id.aset_wheel_calorie_1000
						});
		m_calorie_wheels.set_tv(m_calorie_et);


		// The floats
		m_weight_wheels = new WheelFloat (this, new int[]
						{
						R.id.aset_wheel_weight_point,
						R.id.aset_wheel_weight_1,
						R.id.aset_wheel_weight_10,
						R.id.aset_wheel_weight_100,
						R.id.aset_wheel_weight_1000
						},
						1);
		m_weight_wheels.set_tv(m_weight_et);

		m_dist_wheels = new WheelFloat (this, new int[]
						{
						R.id.aset_wheel_dist_point,
						R.id.aset_wheel_dist_1,
						R.id.aset_wheel_dist_10,
						R.id.aset_wheel_dist_100,
						R.id.aset_wheel_dist_1000
						},
						1);
		m_dist_wheels.set_tv(m_dist_et);

		m_time_wheels = new WheelFloat (this, new int[]
						{
						R.id.aset_wheel_time_point,
						R.id.aset_wheel_time_1,
						R.id.aset_wheel_time_10,
						R.id.aset_wheel_time_100,
						R.id.aset_wheel_time_1000
						},
						1);
		m_time_wheels.set_tv(m_time_et);

		m_other_wheels = new WheelFloat (this, new int[]
						{
						R.id.aset_wheel_other_point,
						R.id.aset_wheel_other_1,
						R.id.aset_wheel_other_10,
						R.id.aset_wheel_other_100,
						R.id.aset_wheel_other_1000
						},
						1);
		m_other_wheels.set_tv(m_other_et);

	} // init_wheels()


	/********************
	 * Finishes the onCreate() method.  Basically, this
	 * fills in the widgets and activates/deactivates
	 * them as needed.
	 */
	private void fill_forms() {
		/**
		 * Indicates if we should draw a bar separating the
		 * row from the one above it.
		 */
		boolean need_bar = false;

		int col;		// used to fill in the views.
//		Log.v(tag, "entering fill_forms()");

		// Get the info from the Intent that GridActivity sent.
		Intent itt = getIntent();
		m_exercise_name = itt.getStringExtra(ExerciseTabHostActivity.KEY_NAME);

//		Log.i(tag, "fill_forms(), exercise name = " + m_exercise_name);

		if (m_exercise_name == null) {
			Toast.makeText(this, "ASetActivity: Problem trying to get the exercise name in fill_forms()", Toast.LENGTH_LONG).show();
			return;
		}

		// Read in that row from the database.
		// Here's the select statement:
		//		select * from exercise_table where _ID = <id>
		try {
			test_m_db();
			m_db = WGlobals.g_db_helper.getReadableDatabase();
			Cursor ex_cursor = null;
			try {
				ex_cursor = DatabaseHelper.getAllExerciseInfoByName(m_db, m_exercise_name);
				// Necessary!  Too bad no one told me that.
				if (ex_cursor.moveToFirst() == false) {
					Log.e(tag, "Problem with the Cursor in onCreate(): it's empty!");
					return;
				}

				// Exercise NAME
				{
					col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_NAME);
					m_exercise_name = ex_cursor.getString(col);
//					m_name_tv.setText(m_exercise_name);
				}

				Cursor set_cursor = null;
				try {
					set_cursor = DatabaseHelper.getLastSet(m_db, m_exercise_name);
					boolean is_set;	// Tells if the Cursor is valid or not.
					if (set_cursor == null) {
						is_set = false;
					}
					else {
						is_set = set_cursor.moveToFirst();
					}

					// If this is the first time we've done this set,
					// then the number of rows is 0.
					m_first_time = (set_cursor.getCount() == 0);


					// SIGNIFICANT
					{
						col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_SIGNIFICANT);
						m_significant = ex_cursor.getInt(col);
					}

					// Fill in the aspects.
					setup_reps (ex_cursor, set_cursor, is_set);
					need_bar = m_reps;

					setup_weights (ex_cursor, set_cursor, is_set);
					need_bar = turn_off_bar(WGlobals.g_wheel ?
												R.id.aset_wheel_weight_bar :
												R.id.aset_weight_bar,
											m_weight, need_bar);

					setup_levels (ex_cursor, set_cursor, is_set);
					need_bar = turn_off_bar(WGlobals.g_wheel ?
												R.id.aset_wheel_level_bar :
												R.id.aset_level_bar,
											m_levels, need_bar);

					setup_calories (ex_cursor, set_cursor, is_set);
					need_bar = turn_off_bar(WGlobals.g_wheel ?
												R.id.aset_wheel_cals_bar :
												R.id.aset_cals_bar,
											m_calories, need_bar);

					setup_dist (ex_cursor, set_cursor, is_set);
					need_bar = turn_off_bar(WGlobals.g_wheel ?
												R.id.aset_wheel_dist_bar :
												R.id.aset_dist_bar,
											m_distanced, need_bar);

					setup_time (ex_cursor, set_cursor, is_set);
					need_bar = turn_off_bar(WGlobals.g_wheel ?
												R.id.aset_wheel_time_bar :
												R.id.aset_time_bar,
											m_timed, need_bar);

					setup_other (ex_cursor, set_cursor, is_set);
					need_bar = turn_off_bar(WGlobals.g_wheel ?
												R.id.aset_wheel_other_bar :
												R.id.aset_other_bar,
											m_other, need_bar);

					setup_notes (ex_cursor, set_cursor, is_set);

				} // try querying a set
				catch (SQLiteException e) {
					e.printStackTrace();
				}
				finally {
					if (set_cursor != null) {
						set_cursor.close();
						set_cursor = null;
					}
				}

			} // try querying exercise info
			catch (SQLiteException e) {
				Log.e(tag, "problem querying the database in ASetActivity.fill_forms()!");
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

		m_reset_widgets = false;		// We've done our work here!

	} // fill_forms (row)

	/********************
	 * Simply fills in the information for the REPS aspect of
	 * the ASetActivity.  If there are no reps, then that
	 * part is set to GONE so it doesn't display.
	 *
	 * @param ex_cursor		Loaded and ready to go.
	 * @param set_cursor		Holds info about the last set
	 * @param set_valid		When TRUE, the set_cursor is valid.
	 * 						Otherwise, this is the FIRST TIME this
	 * 						exercise has been used.
	 */
	protected void setup_reps (Cursor ex_cursor, Cursor set_cursor,
							boolean set_valid) {
		TextView reps_label_tv;
		if (WGlobals.g_wheel)
			reps_label_tv = (TextView) findViewById(R.id.aset_wheel_reps_label_tv);
		else
			reps_label_tv = (TextView) findViewById(R.id.aset_reps_label_tv);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_REP);
		m_reps = ex_cursor.getInt(col) == 1 ? true : false;
		if (m_reps) {
			m_reps_et.setEnabled(m_reps);
			m_reps_et.setFocusable(m_reps);
			if (m_significant == DatabaseHelper.EXERCISE_COL_REP_NUM) {
				reps_label_tv.setTypeface(null, Typeface.BOLD);
			}

			if (set_valid) {
				col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_REPS);
				int reps = set_cursor.getInt(col);
				if (reps != -1) {
					m_reps_et.setText("" + reps);
					if (WGlobals.g_wheel)
						m_reps_wheels.set_value(reps);
				}
			}
		}
		else {
			// Reps aren't there, so get rid of this row!
			int id = WGlobals.g_wheel ?
					R.id.aset_wheel_reps_row :
						R.id.aset_reps_row;
			TableRow reps_row = (TableRow)findViewById(id);
			reps_row.setVisibility(View.GONE);
		}
	} // setup_reps (...)

	/************************
	 * Similar to above
	 */
	protected void setup_levels (Cursor ex_cursor, Cursor set_cursor,
								boolean set_valid) {
		TextView level_label_tv;
		if (WGlobals.g_wheel)
			level_label_tv = (TextView) findViewById(R.id.aset_level_label_tv);
		else
			level_label_tv = (TextView) findViewById(R.id.aset_wheel_level_label_tv);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LEVEL);
		m_levels = ex_cursor.getInt(col) == 1 ? true : false;
		if (m_levels) {
			m_level_et.setEnabled(m_levels);
			m_level_et.setFocusable(m_levels);
			if (m_significant == DatabaseHelper.EXERCISE_COL_LEVEL_NUM) {
				level_label_tv.setTypeface(null, Typeface.BOLD);
			}
			if (set_valid) {
				col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_LEVELS);
				int level = set_cursor.getInt(col);
				if (level != -1) {
					m_level_et.setText("" + level);
				}
			}
		}
		else {
			int id = WGlobals.g_wheel ?
					R.id.aset_wheel_level_row :
						R.id.aset_level_row;
			TableRow levels_row = (TableRow)
					findViewById(id);
			levels_row.setVisibility(View.GONE);
		}
	} // setup_levels (...)

	//-------------------------------
	protected void setup_calories (Cursor ex_cursor, Cursor set_cursor,
								boolean set_valid) {
		TextView calorie_label_tv;
		if (WGlobals.g_wheel)
			calorie_label_tv = (TextView) findViewById(R.id.aset_wheel_calorie_label_tv);
		else
			calorie_label_tv = (TextView) findViewById(R.id.aset_calorie_label_tv);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_CALORIES);
		m_calories = ex_cursor.getInt(col) == 1 ? true : false;
		if (m_calories) {
			m_calorie_et.setEnabled(m_calories);
			m_calorie_et.setFocusable(m_calories);
			if (m_significant == DatabaseHelper.EXERCISE_COL_CALORIE_NUM) {
				calorie_label_tv.setTypeface(null, Typeface.BOLD);
			}
			if (set_valid) {
				col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_CALORIES);
				int cals = set_cursor.getInt(col);
				if (cals != -1) {
					m_calorie_et.setText("" + cals);
				}
			}
		}
		else {
			int id = WGlobals.g_wheel ?
					R.id.aset_wheel_cals_row :
						R.id.aset_cals_row;
			TableRow cals_row = (TableRow)
					findViewById(id);
			cals_row.setVisibility(View.GONE);
		}
	} // setup_calories(...)

	/********************
	 * Simply fills in the information for the weight aspect of
	 * the ASetActivity.  If there is no weight aspect, then that
	 * part is set to GONE so it doesn't display.
	 *
	 * @param ex_cursor		Loaded and ready to go.
	 * @param set_cursor		Holds info about the last set
	 * @param set_valid		When TRUE, the set_cursor is valid.
	 * 						Otherwise, this is the FIRST TIME this
	 * 						exercise has been used.
	 */
	protected void setup_weights (Cursor ex_cursor, Cursor set_cursor,
								boolean set_valid) {
		TextView weight_label_tv;
		if (WGlobals.g_wheel) {
			weight_label_tv = (TextView) findViewById(R.id.aset_wheel_weight_label_tv);
		}
		else {
			weight_label_tv = (TextView) findViewById(R.id.aset_weight_label_tv);
		}

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT);
		m_weight = ex_cursor.getInt(col) == 1 ? true : false;
		if (m_weight) {
			m_weight_et.setEnabled(true);
			m_weight_et.setFocusable(true);
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT);
			String weight_unit = getString(R.string.aset_weight_hint,
					ex_cursor.getString(col));
//			m_weight_et.setHint(weight_unit);	// causes problems!!!
			weight_label_tv.setText(getString(R.string.aset_weight_label,
					weight_unit));
			if (m_significant == DatabaseHelper.EXERCISE_COL_WEIGHT_NUM) {
				weight_label_tv.setTypeface(null,
						Typeface.BOLD);
			}

			if (set_valid) {
				// Not the first time this set has been added.
				col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_WEIGHT);
				float weight = set_cursor.getFloat(col);
				if (weight != -1) {
					m_weight_et.setText("" + weight);
					if (WGlobals.g_wheel)
						m_weight_wheels.set_value(weight);
				}
			}
		}
		else {
			int id = WGlobals.g_wheel ?
					R.id.aset_wheel_weight_row :
						R.id.aset_weight_row;
			TableRow weight_row = (TableRow)
					findViewById(id);
			weight_row.setVisibility(View.GONE);
		}
	} // setup_weights(...)

	//-------------------------------
	protected void setup_dist (Cursor ex_cursor, Cursor set_cursor,
							boolean set_valid) {
		TextView dist_label_tv;
		if (WGlobals.g_wheel)
			dist_label_tv = (TextView) findViewById(R.id.aset_wheel_dist_label_tv);
		else
			dist_label_tv = (TextView) findViewById(R.id.aset_dist_label_tv);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST);
		m_distanced = ex_cursor.getInt(col) == 1 ? true : false;
		if (m_distanced) {
			m_dist_et.setEnabled(m_distanced);
			m_dist_et.setFocusable(m_distanced);
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST_UNIT);
			String dist_unit = getString(R.string.aset_distance_hint, ex_cursor.getString(col));
//			m_dist_et.setHint(dist_unit);
			dist_label_tv.setText(getString(R.string.aset_distance_label,
					dist_unit));
			if (m_significant == DatabaseHelper.EXERCISE_COL_DIST_NUM) {
				dist_label_tv.setTypeface(null, Typeface.BOLD);
			}
			if (set_valid) {
				col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_DIST);
				float dist = set_cursor.getFloat(col);
				if (dist != -1) {
					m_dist_et.setText("" + dist);
				}
			}
		}
		else {
			int id = WGlobals.g_wheel ?
					R.id.aset_wheel_dist_row :
						R.id.aset_dist_row;
			TableRow dist_row = (TableRow)
					findViewById(id);
			dist_row.setVisibility(View.GONE);
		}
	} // setup_dist(...)

	//-------------------------------
	protected void setup_time (Cursor ex_cursor, Cursor set_cursor,
							boolean set_valid) {
		TextView time_label_tv;
		if (WGlobals.g_wheel)
			time_label_tv = (TextView) findViewById(R.id.aset_wheel_time_label_tv);
		else
			time_label_tv = (TextView) findViewById(R.id.aset_time_label_tv);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME);
		m_timed = ex_cursor.getInt(col) == 1 ? true : false;
		if (m_timed) {
			m_time_et.setEnabled(m_timed);
			m_time_et.setFocusable(m_timed);
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME_UNIT);
			String time_unit = getString(R.string.aset_time_hint, ex_cursor.getString(col));
//			m_time_et.setHint(time_unit);
			if (m_significant == DatabaseHelper.EXERCISE_COL_TIME_NUM) {
				time_label_tv.setTypeface(null, Typeface.BOLD);
			}
			time_label_tv.setText(getString(R.string.aset_time_label,
					time_unit));
			if (set_valid) {
				col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_TIME);
				float time = set_cursor.getFloat(col);
				if (time != -1) {
					m_time_et.setText("" + time);
				}
			}
		}
		else {
			int id = WGlobals.g_wheel ?
					R.id.aset_wheel_time_row :
						R.id.aset_time_row;
			TableRow time_row = (TableRow)
					findViewById(id);
			time_row.setVisibility(View.GONE);
		}
	} // setup_time(...)

	//-------------------------------
	protected void setup_other (Cursor ex_cursor, Cursor set_cursor,
								boolean set_valid) {
		TextView other_label_tv;
		if (WGlobals.g_wheel)
			other_label_tv = (TextView) findViewById(R.id.aset_wheel_other_label_tv);
		else
			other_label_tv = (TextView) findViewById(R.id.aset_other_label_tv);

		int col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER);
		m_other = ex_cursor.getInt(col) == 1 ? true : false;
		if (m_other) {
			m_other_et.setEnabled(m_other);
			m_other_et.setFocusable(m_other);
			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_TITLE);
			m_other_title = ex_cursor.getString(col);
//			m_other_label_tv.setText(getString (R.string.aset_other_label, m_other_title));

			col = ex_cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_UNIT);
			m_other_unit = ex_cursor.getString(col);
			String final_other_label = getString (R.string.aset_other_hint, m_other_unit);
//			m_other_et.setHint(final_other_label);
			other_label_tv.setText(getString(R.string.aset_other_label_old,
					m_other_title, m_other_unit));
			if (m_significant == DatabaseHelper.EXERCISE_COL_OTHER_NUM) {
				other_label_tv.setTypeface(null, Typeface.BOLD);
			}
			if (set_valid) {
				col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_OTHER);
				float other = set_cursor.getFloat(col);
				if (other != -1) {
					m_other_et.setText("" + other);
				}
			}
		}
		else {
			int id = WGlobals.g_wheel ?
					R.id.aset_wheel_other_row :
						R.id.aset_other_row;
			TableRow other_row = (TableRow)
					findViewById(id);
			other_row.setVisibility(View.GONE);
		}
	} // setup_other(...)

	/*************************
	 * Reads in the note from the database and displays it
	 * as a hint in the notes EditText.
	 *
	 *  This only does anything if set_valid.
	 */
	protected void setup_notes (Cursor ex_cursor, Cursor set_cursor,
								boolean set_valid) {
		if (set_valid) {
			int col = set_cursor.getColumnIndex(DatabaseHelper.SET_COL_NOTES);
			String note = set_cursor.getString(col);
			if (note != null) {
				m_notes_et.setHint(note);
//				Log.d(tag, "Just set m_notes_et to: " + note);
			}
		}
	}

	/***********************
	 * This is a convenice method, doing several things
	 * and using some side effects.  The main part is
	 * to turn off unnecessary separator bars between
	 * the exercise aspects.
	 *
	 * @param id			The id of the bar in question.
	 * @param exists		Does the exercise BELOW the bar exist?
	 * @param above		Have we displayed an exercise above?
	 * 					This is the result from previous calls
	 * 					to this method.
	 *
	 * @return		This simply returns exists OR above.  It's
	 * 				used to call the next iteration of this
	 * 				method (it'll be the above parameter).
	 */
	protected boolean turn_off_bar (int id,
									boolean exists,
									boolean above) {
		if (!(exists && above)) {
			turn_off_widget (id);
		}
		return exists || above;
	}

	/***********************
	 * Turns the given widget to GONE.
	 */
	protected void turn_off_widget (int id) {
		View widget = findViewById(id);
		widget.setVisibility(View.GONE);
	}


	/*************************
	 * Just like it says: goes through and counts how many
	 * forms are currently enabled.
	 *
	 * preconditions:
	 * 		Should be called AFTER the Activity is completely
	 * 		created.
	 */
	private int get_num_enabled_forms() {
		int count = 0;

		if (m_reps_et.isEnabled())
			count++;
		if (m_weight_et.isEnabled())
			count++;
		if (m_level_et.isEnabled())
			count++;
		if (m_calorie_et.isEnabled())
			count++;
		if (m_dist_et.isEnabled())
			count++;
		if (m_time_et.isEnabled())
			count++;
		if (m_other_et.isEnabled())
			count++;
		return count;
	} // get_num_enabled_forms()


	/*************************
	 * Clears all the active widgets, allowing the
	 * hints to show.
	 */
	private void clear() {
		if (m_reps_et.isEnabled())
			m_reps_et.setText(null);
		if (m_weight_et.isEnabled())
			m_weight_et.setText(null);
		if (m_level_et.isEnabled())
			m_level_et.setText(null);
		if (m_calorie_et.isEnabled())
			m_calorie_et.setText(null);
		if (m_dist_et.isEnabled())
			m_dist_et.setText(null);
		if (m_time_et.isEnabled())
			m_time_et.setText(null);
		if (m_other_et.isEnabled())
			m_other_et.setText(null);
		clear_stress();
		clear_wheels();
		m_notes_et.setText(null);
		m_notes_et.setHint(null);
	} // clear()

	/*************************
	 * Clears the stress condition radio buttons.
	 * This is not really a clear, it makes the
	 * ok button on and the others off.
	 */
	void clear_stress() {
		m_ok_rb.setChecked(true);
		m_plus_rb.setChecked(false);
		m_minus_rb.setChecked(false);
		m_x_rb.setChecked(false);
	}

	/*************************
	 * If any wheels are displayed, this sets them all
	 * to zero.
	 */
	void clear_wheels() {
		if (WGlobals.g_wheel) {
			if (m_reps) {
				m_reps_wheels.reset();
			}
			if (m_weight) {
				m_weight_wheels.reset();
			}
		}
	}

	/*************************
	 * Assumes that it's time to save the data (no checks
	 * needed).  So put it into the DB and kill this Activity.
	 *
	 * We will return (in our Intent) the ID of this exercise.
	 */
	private void save() {
		String str;

		if (m_db != null) {
			Log.e (tag, "Database already opened when trying to save! Aborting!");
			return;
		}
		try {
			test_m_db();
			m_db = WGlobals.g_db_helper.getWritableDatabase();

			// Collect our data for this set.
			ContentValues values = new ContentValues();

			// First, the name of the exercise!
			values.put(DatabaseHelper.SET_COL_NAME, m_exercise_name);

			if (m_reps_et.isEnabled()) {
				str = m_reps_et.getText().toString();
				values.put(DatabaseHelper.SET_COL_REPS,
								my_parse(str, false).i);
			}

			if (m_weight_et.isEnabled()) {
				str = m_weight_et.getText().toString();
				values.put(DatabaseHelper.SET_COL_WEIGHT,
								my_parse(str, true).f);
			}

			if (m_level_et.isEnabled()) {
				str = m_level_et.getText().toString();
				values.put(DatabaseHelper.SET_COL_LEVELS,
								my_parse(str, false).i);
			}

			if (m_calorie_et.isEnabled()) {
				str = m_calorie_et.getText().toString();
				values.put(DatabaseHelper.SET_COL_CALORIES,
								my_parse(str, false).i);
			}

			if (m_dist_et.isEnabled()) {
				str = m_dist_et.getText().toString();
				values.put(DatabaseHelper.SET_COL_DIST,
								my_parse(str, true).f);
			}

			if (m_time_et.isEnabled()) {
				str = m_time_et.getText().toString();
				values.put(DatabaseHelper.SET_COL_TIME,
								my_parse(str, true).f);
			}

			if (m_other_et.isEnabled()) {
				str = m_other_et.getText().toString();
				values.put(DatabaseHelper.SET_COL_OTHER,
								my_parse(str, true).f);
			}

			// The condition (stress).
			int cond;
			if (m_ok_rb.isChecked())
				cond =  DatabaseHelper.SET_COND_OK;
			else if (m_plus_rb.isChecked())
				cond = DatabaseHelper.SET_COND_PLUS;
			else if (m_minus_rb.isChecked())
				cond = DatabaseHelper.SET_COND_MINUS;
			else if (m_x_rb.isChecked())
				cond = DatabaseHelper.SET_COND_INJURY;
			else {
				cond = DatabaseHelper.SET_COND_NONE;
				Log.e(tag, "Illegal stress condition while saving!");
			}

			values.put(DatabaseHelper.SET_COL_CONDITION, cond);

			// The notes
			values.put(DatabaseHelper.SET_COL_NOTES, m_notes_et.getText().toString());

			// Don't forget the date/time!
			Calendar now = Calendar.getInstance();
			values.put(DatabaseHelper.SET_COL_DATEMILLIS, now.getTimeInMillis());

			// That's it!  Insert and we're done.
			m_db.insert(DatabaseHelper.SET_TABLE_NAME, null, values);
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

		String[] args = {m_exercise_name};
		my_toast(this, R.string.aset_entered_msg, args);
		// todo
		//	Maybe turn the EditTexts into hints???  Go to the History tab???
		//	DEFINITELY a sound should play (if not in silent mode).

		// Indicate that the database has changed and tell the
		//	other activities to reset.
		m_db_dirty = true;
		InspectorActivity2.m_db_dirty = true;
//		HistoryActivity.m_db_dirty = true;
		GraphActivity.m_db_dirty = true;
		ExerciseTabHostActivity.m_dirty = true;

		// After the save, clear the stress and cause the note to
		// turned into a hint.
		clear_stress();
		m_notes_et.setHint(m_notes_et.getText());
		m_notes_et.setText(null);
	} // save()


	/*********************
	 * Tests to see if all the forms are filled out properly.
	 * If any aren't, this nags the user to see if they
	 * want to save anyway (reminding them about all the forms
	 * that are blank).
	 *
	 * If the user decides that they want to save anyway, then
	 * save we do!
	 *
	 * If all the forms are okay, then this saves the data
	 * and does a nice dance to let the user know that a save
	 * occurred.
	 *
	 * preconditions:
	 * 		m_widgets_dirty		Is correctly set (true iff the user
	 * 					has made any changes).
	 *
	 * postconditions:
	 * 		- Saves the workout set to the database.
	 * 		- OR does nothing if forms have not been filled out
	 * 		  and the user wants the chance to change them.
	 */
	private void test_to_save() {
		// First test to see if the user has done anything yet
		// (if the widgets are not dirty).  If this is the FIRST
		// time they ever did this exercise, then warn them.
		// Otherwise, this is no big deal as the forms are already
		// filled out.
		if (!m_widgets_dirty) {

			if (m_first_time) {
				// First time doing this set, so there's nothing
				// in any of the widgets.  Tell 'em about it and
				// get outahere!
				show_help_dialog(R.string.aset_nag_enter_empty_title,
						R.string.aset_nag_enter_empty_msg);
				return;
			}

			if (WGlobals.g_nag) {
				// Warn them that they haven't made any changes.
				show_yes_no_dialog(R.string.aset_nag_no_changes_title,
						R.string.aset_nag_no_changes_msg,
						new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								save();
								dismiss_all_dialogs();
							}
						});
			}
			else {	// Don't nag, just save
				save();
			}

			return;
		} // not dirty


		// The method for this method:
		// 	- Make a list of the widgets that are NOT filled in.
		//	- If the list is empty, then save and exit.
		//	- Otherwise: send the user a show_yes_no_dialog,
		//		enumerating the widgets that aren't filled in.
		//	- Provide a listener that responds to if they say
		//		yes (save and exit) or no (remove dialog and
		//		do nothing).

		CharSequence str;
		ArrayList<String> blank_forms = new ArrayList<String>();

		if (m_reps_et.isEnabled()) {
			str = m_reps_et.getText();
			if ((str == null) || (str.length() == 0)) {

				// If this (reps) is the significant, then we
				// immediately tell the user that this is a
				// problem and return--don't go any further.
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_REP_NUM))
					return;

				// Otherwise, add this to our list of skipped
				// exercises.
				blank_forms.add(getString(R.string.aset_reps_label));
			}
		}

		if (m_weight_et.isEnabled()) {
			str = m_weight_et.getText();
			if ((str == null) || (str.length() == 0)) {
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_WEIGHT_NUM))
					return;
				blank_forms.add(getString (R.string.aset_weight_off_label));
			}
		}

		if (m_level_et.isEnabled()) {
			str = m_level_et.getText();
			if ((str == null) || (str.length() == 0)) {
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_LEVEL_NUM))
					return;
				blank_forms.add(getString (R.string.aset_level_label));
			}
		}

		if (m_calorie_et.isEnabled()) {
			str = m_calorie_et.getText();
			if ((str == null) || (str.length() == 0)) {
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_CALORIE_NUM))
					return;
				blank_forms.add(getString (R.string.aset_calorie_label));
			}
		}

		if (m_dist_et.isEnabled()) {
			str = m_dist_et.getText();
			if ((str == null) || (str.length() == 0)) {
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_DIST_NUM))
					return;
				blank_forms.add(getString (R.string.aset_distance_off_label));
			}
		}

		if (m_time_et.isEnabled()) {
			str = m_time_et.getText();
			if ((str == null) || (str.length() == 0)) {
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_TIME_NUM))
					return;
				blank_forms.add(getString (R.string.aset_time_off_label));
			}
		}

		if (m_other_et.isEnabled()) {
			str = m_other_et.getText();
			if ((str == null) || (str.length() == 0)) {
				// The other is a little different.  So do it man-
				// ually here.
				TextView other_label_tv =
						(TextView) findViewById(WGlobals.g_wheel ?
								R.id.aset_wheel_other_label_tv :
								R.id.aset_other_label_tv);

				if (m_significant == DatabaseHelper.EXERCISE_COL_OTHER_NUM) {
					String args[] = {other_label_tv.getText().toString(),
									other_label_tv.getText().toString(), m_exercise_name};
					show_help_dialog(R.string.aset_nag_significant_title, null,
							R.string.aset_nag_significant_msg, args);
					return;
				}
				blank_forms.add(other_label_tv.getText().toString());
			}
		}

		// Now the blank_forms list is ready.  If it's empty,
		// then the user has filled out all the forms.
		// YAY!! This is the best case!
		if (blank_forms.isEmpty()) {
			save();
			return;		// Shouldn't happen, but just in case...
		}

		// If ALL the forms are empty, do the same thing as
		// we did if m_widgets_dirty was false.
		if (get_num_enabled_forms() == blank_forms.size()) {
			show_help_dialog(R.string.aset_nag_enter_empty_title,
							R.string.aset_nag_enter_empty_msg);
			return;
		}

		// At this point, there are some blank forms.  Show
		// them a yes_no_dialog with the forms they skipped.
		// But NOT if they do not want nagging.
		if (WGlobals.g_nag) {
			String title, msg, blanks = "";
			title = getString (R.string.aset_nag_title);

			if (blank_forms.size() > 1) {
				for (String form_name : blank_forms) {
					blanks += "\t" + form_name + "\n";
				}
				msg = getString(R.string.aset_nag_msg_plural,
						blanks);
			}
			else {
				msg = getString(R.string.aset_nag_msg_singular,
						blank_forms.get(0));
			}

			show_yes_no_dialog (title, msg, new OnClickListener() {
				@Override
				public void onClick(View v) {
					// They said yes, so
					save();
					dismiss_all_dialogs();
				}
			});
		}
		else {
			save();
		}
	} // test_to_save_and_exit()


	/**************************
	 * Tests to see if the specified aspect of the current
	 * exercise is the significant part of this exercise.
	 *
	 * If is IS significant, then a warning message pops up
	 * telling the user that they have to at least fill this
	 * widget out.
	 *
	 * preconditions:
	 *		The specified form is not filled out.
	 *
	 * @param column_num		The DatabaseHelper column num of
	 * 						the widget in question.
	 *
	 * @return		true		Yes, this IS the significant aspect
	 * 						exercise.  AND the user has been warned.
	 * 				false	Nope, and nothing has been done.
	 */
	private boolean test_and_warn_significant (int column_num) {
		if (m_significant == column_num) {
			String column_array[] = getResources().getStringArray(R.array.exercise_column_names_array);

			String args[] = {column_array[column_num], column_array[column_num], m_exercise_name};
			show_help_dialog(R.string.aset_nag_significant_title, null,
					R.string.aset_nag_significant_msg, args);
			return true;
		}
		return false;

	} // test_and_warn_significant (...)

	/**********************
	 * My replacement for Integer.parseInt(), which freaks
	 * out if the string is in the wrong format.
	 *
	 * @param str	The string to turn into an int.
	 *
	 * @param is_float	true		We're reading a float.
	 * 					false	int
	 *
	 * @return	The int value of str, or -1 if there's an error,
	 * 			such as an empty or non-existent string.
	 * 			This is fine, as all my values are non-negative.
	 */
	private IntFloat my_parse (String str, boolean is_float) {
		IntFloat num = new IntFloat(-1);

		// Check for an empty string or null.
		if ((str == null) || str.contentEquals("")) {
			Log.v(str, "my_parse trying to parse an empty string!");
			return num;
		}

		if (is_float) {
			try {
				num.set(Float.parseFloat(str));
			} catch (NumberFormatException e) {
				Log.e(tag, "Float Parse exception in my_parse(). The string is '" + str
					+ "'. ");
			}
		}
		else {
			try {
				num.set(Integer.parseInt(str));
			} catch (NumberFormatException e) {
				Log.e(tag, "Integer Parse exception in my_parse(). The string is " + str
					+ "'. '");
			}
		}
		return num;
	} // my_parse (str)

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
				throw new SQLiteException("m_db not null when starting doInBackground() in ASetActivity!");
		}
	} // test_m_db()


}
