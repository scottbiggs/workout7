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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class AddSetActivity
				extends BaseDialogActivity
				implements
					OnClickListener,
					OnLongClickListener,
					TextWatcher,
					OnKeyListener,
					OnWheelIntListener,
					OnWheelFloatListener {


	//------------------------
	//	Constants
	//------------------------

	private static final String tag = "AddSetActivity";


	/** ID for the menu item to change the wheel width */
	protected static final int MENU_ID_WIDE_WHEELS = 1;


	//------------------------
	//	Widget Data
	//------------------------

	Button m_done, m_clear;

	/** Where the user can manually set numbers. */
	TextView m_reps_tv, m_weight_tv, m_level_tv,
		m_dist_tv,
		m_time_tv,
		m_other_tv,
		m_calorie_tv,
		m_notes_tv;

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


	/**
	 * Since the ASyncTask is static, it may persist
	 * when this Activity is destroyed.  This variable
	 * will be passed back to us via
	 * onRetainLastConfiguationInstance() and
	 * getLastNonConfigurationInstance().
	 */
	private ASetASyncTask m_task = null;


	//------------------------
	//	Methods
	//------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);

		WGlobals.g_sound = false;	// To prevent wheels from making noise during creation

		m_reset_widgets = true;	// Fill the forms the first time this
								// activity is called.

		// Try to grab a reference it from a previous
		// instance of this Activity.
		m_task = (ASetASyncTask) getLastNonConfigurationInstance();
//		Log.d(tag, "   onCreate(): after getLastNonConfigurationInstance() m_task = " + m_task);

	} // onCreate (.)


	//------------------------
	@Override
	protected void onResume() {
//		Log.v(tag, "onResume()");
		if (m_reset_widgets) {
			if (m_task == null) {
				// There is no ASyncTask running, so go ahead
				// and start it up.
				start_progress_dialog(R.string.loading_str);
				m_task = new ASetASyncTask(this);
				m_task.execute();
			}
			else {
				// There is already an ASyncTask running,
				// establish a connection to it.
				m_task.attach(this);

				// If the ASyncTask is still working, re-start
				// the progress dialog.
				if (m_task.isDone() == false) {
					start_progress_dialog(R.string.loading_str);
				}
				else {
					// Draw the Activity
					m_task.onPostExecute(null);
				}

//				catch_up();		not used in this Activity as there's no ProgressUpdate()
			}
		}
		super.onResume();
	} // onResume()


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
		if (m_task == null) {
//			Log.e(tag, "onRetainNonConfigurationInstance(), m_task is NULL! Aborting!");
			return null;	// nothing to return!
		}

		m_task.detach();		// Tells task to remove its reference
							// to this Activity as I'm about to
							// die.

		return m_task;	// Return the ASyncTask so the
						// new Activity can find it (and then
						// attach it to the ASyncTask).
	}


	/***********************
	 * Sets up all the global widgets variables for
	 * the layout that does NOT contain wheels.
	 */
	protected void init_widgets_no_wheels() {
		m_reps_tv = (TextView) findViewById(R.id.aset_reps_result_tv);
		m_reps_tv.setOnLongClickListener(this);
		m_reps_tv.setOnClickListener(this);

		m_weight_tv = (TextView) findViewById(R.id.aset_weight_result_tv);
		m_weight_tv.setOnLongClickListener(this);
		m_weight_tv.setOnClickListener(this);

		m_level_tv = (TextView) findViewById(R.id.aset_level_result_tv);
		m_level_tv.setOnLongClickListener(this);
		m_level_tv.setOnClickListener(this);

		m_calorie_tv = (TextView) findViewById(R.id.aset_calorie_result_tv);
		m_calorie_tv.setOnLongClickListener(this);
		m_calorie_tv.setOnClickListener(this);

		m_dist_tv = (TextView) findViewById(R.id.aset_dist_result_tv);
		m_dist_tv.setOnLongClickListener(this);
		m_dist_tv.setOnClickListener(this);

		m_time_tv = (TextView) findViewById(R.id.aset_time_result_tv);
		m_time_tv.setOnLongClickListener(this);
		m_time_tv.setOnClickListener(this);

		m_other_tv = (TextView) findViewById(R.id.aset_other_result_tv);
		m_other_tv.setOnLongClickListener(this);
		m_other_tv.setOnClickListener(this);

		m_notes_tv = (TextView) findViewById(R.id.aset_notes_et);
		m_notes_tv.setOnLongClickListener(this);
		m_notes_tv.setOnKeyListener(this);
		m_notes_tv.addTextChangedListener(this);

		m_ok_rb = (RadioButton) findViewById(R.id.aset_cond_ok_rb);
		m_ok_rb.setOnLongClickListener(this);
		m_ok_rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WGlobals.play_short_click();

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
				WGlobals.play_short_click();

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
				WGlobals.play_short_click();

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
				WGlobals.play_short_click();

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
		m_reps_tv = (TextView) findViewById(R.id.aset_wheel_reps_result_tv);
		m_reps_tv.setOnLongClickListener(this);
		m_reps_tv.setOnClickListener(this);

		m_weight_tv = (TextView) findViewById(R.id.aset_wheel_weight_result_tv);
		m_weight_tv.setOnLongClickListener(this);
		m_weight_tv.setOnClickListener(this);

		m_level_tv = (TextView) findViewById(R.id.aset_wheel_level_result_tv);
		m_level_tv.setOnLongClickListener(this);
		m_level_tv.setOnClickListener(this);

		m_calorie_tv = (TextView) findViewById(R.id.aset_wheel_calorie_result_tv);
		m_calorie_tv.setOnLongClickListener(this);
		m_calorie_tv.setOnClickListener(this);

		m_dist_tv = (TextView) findViewById(R.id.aset_wheel_dist_result_tv);
		m_dist_tv.setOnLongClickListener(this);
		m_dist_tv.setOnClickListener(this);

		m_time_tv = (TextView) findViewById(R.id.aset_wheel_time_result_tv);
		m_time_tv.setOnLongClickListener(this);
		m_time_tv.setOnClickListener(this);

		m_other_tv = (TextView) findViewById(R.id.aset_wheel_other_result_tv);
		m_other_tv.setOnLongClickListener(this);
		m_other_tv.setOnClickListener(this);

		m_notes_tv = (TextView) findViewById(R.id.aset_wheel_notes_et);
		m_notes_tv.setOnLongClickListener(this);

		m_ok_rb = (RadioButton) findViewById(R.id.aset_wheel_cond_ok_rb);
		m_ok_rb.setOnLongClickListener(this);
		m_ok_rb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				WGlobals.play_short_click();

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
				WGlobals.play_short_click();

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
				WGlobals.play_short_click();

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
				WGlobals.play_short_click();

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


	// What is THIS doing here???
	//
//	@Override
//	public void onWindowFocusChanged(boolean hasFocus) {
//		super.onWindowFocusChanged(hasFocus);
//		WGlobals.g_sound = true;		// Turn sound back on.
//	}


	//------------------------------
	//	Initialize the menu.
	//
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ID_WIDE_WHEELS, 0, R.string.null_string);

		return super.onCreateOptionsMenu(menu);
	}


	//------------------------------
	//	I use this method to make sure that the correct
	//	message is displayed.
	//
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_ID_WIDE_WHEELS).setTitle(WGlobals.g_wheel_width_fat ?
							R.string.aset_menu_wide_wheels_turn_off_msg :
							R.string.aset_menu_wide_wheels_turn_on_msg);
		return super.onPrepareOptionsMenu(menu);
	}



	//------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		int id = item.getItemId();
		switch (id) {
			case MENU_ID_WIDE_WHEELS:
				WGlobals.g_wheel_width_fat = !WGlobals.g_wheel_width_fat;
				prefs.edit().putBoolean(getString(R.string.prefs_wheel_width_key),
										WGlobals.g_wheel_width_fat)
								.commit();
				WGlobals.load_prefs(this);
				WGlobals.act_on_prefs(this);

				// Let's try starting over...Seems like overkill, but
				// it works!
				m_reset_widgets = true;
				onResume();
				break;

			default:
				Log.e (tag, "Illegal id: " + id + ", in onOptionsItemSelected!");
				break;
		}
		return super.onOptionsItemSelected(item);
	}



	//------------------------------
	//	Allows this Activity to send message to the caller
	//	when the user hits the back button.
	//
	@Override
	public void onBackPressed() {
		if (ExerciseTabHostActivity.m_dirty) {
			tabbed_set_result(RESULT_OK);
//			Log.v(tag, "onBackPressed: setting result to OK");
		}
		else {
			tabbed_set_result(RESULT_CANCELED);
//			Log.v(tag, "onBackPressed: setting result to CANCEL");
		}
		finish();
	}


	//-----------------------------------
	@Override
	public void onClick(View v) {
		WGlobals.play_short_click();

		if (v == m_done) {
			test_to_save();
		} // m_ok

		else if (v == m_clear) {
			clear();
		}

		else if (v == m_reps_tv) {
			activate_number_activity (DatabaseHelper.EXERCISE_COL_REP_NUM,
				getString(R.string.editset_reps_label),
				m_reps_tv.getText().toString(), false);
		}
		else if (v == m_weight_tv) {
			activate_number_activity (DatabaseHelper.EXERCISE_COL_WEIGHT_NUM,
				getString(R.string.editset_weight_label, m_task.m_ex_data.weight_unit),
				m_weight_tv.getText().toString(), true);
		}
		else if (v == m_calorie_tv) {
			activate_number_activity (DatabaseHelper.EXERCISE_COL_CALORIE_NUM,
				getString(R.string.editset_cals_label),
				m_calorie_tv.getText().toString(), false);
		}
		else if (v == m_dist_tv) {
			activate_number_activity (DatabaseHelper.EXERCISE_COL_DIST_NUM,
				getString(R.string.editset_dist_label, m_task.m_ex_data.dist_unit),
				m_dist_tv.getText().toString(), true);
		}
		else if (v == m_time_tv) {
			activate_number_activity (DatabaseHelper.EXERCISE_COL_TIME_NUM,
				getString(R.string.editset_time_label, m_task.m_ex_data.time_unit),
				m_time_tv.getText().toString(), true);
		}
		else if (v == m_level_tv) {
			activate_number_activity (DatabaseHelper.EXERCISE_COL_LEVEL_NUM,
				getString(R.string.editset_level_label),
				m_level_tv.getText().toString(), false);
		}
		else if (v == m_other_tv) {
			activate_number_activity (DatabaseHelper.EXERCISE_COL_OTHER_NUM,
				getString(R.string.editset_other_label, m_task.m_ex_data.other_title, m_task.m_ex_data.other_unit),
				m_other_tv.getText().toString(), true);
		}

	} // onClick (v)


	//-----------------------------------
	@Override
	public boolean onLongClick(View v) {
		WGlobals.play_long_click();

		if (v == m_reps_tv) {
			show_help_dialog (R.string.aset_reps_help_title,
					R.string.aset_reps_help_msg);
			return true;
		}

		else if (v == m_weight_tv) {
			show_help_dialog (R.string.aset_weight_help_title,
					R.string.aset_weight_help_msg);
			return true;
		}

		else if (v == m_level_tv) {
			show_help_dialog (R.string.aset_level_help_title,
					R.string.aset_level_help_msg);
			return true;
		}

		else if (v == m_calorie_tv) {
			show_help_dialog (R.string.aset_calorie_help_title,
					R.string.aset_calorie_help_msg);
			return true;
		}

		else if (v == m_dist_tv) {
			show_help_dialog (R.string.aset_distance_help_title,
					R.string.aset_distance_help_msg);
			return true;
		}

		else if (v == m_time_tv) {
			show_help_dialog (R.string.aset_time_help_title,
					R.string.aset_time_help_msg);
			return true;
		}

		else if (v == m_other_tv) {
			String[] args = {m_task.m_ex_data.other_title, m_task.m_ex_data.other_unit};
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

		else if (v == m_notes_tv) {
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


	//-----------------------------------
	//	This is the callback that happens whenever a wheelInt
	//	has changed.
	@Override
	public void onWheelIntChanged(int new_val) {
		m_widgets_dirty = true;
	}


	//-----------------------------------
	//	This is the callback that happens whenever a wheelInt
	//	has changed.
	@Override
	public void onWheelFloatChanged(float new_val) {
		m_widgets_dirty = true;
	}


	//-----------------------------------
	//	Needed to see if the notes has changed (and makes our
	//	widgets dirty).
	//
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		m_widgets_dirty = true;
		return false;
	}

	//-----------------------------------
	// Since soft keyboards don't fire onKey(), this is needed
	// to see if a software keyboard made any changes.
	//
	@Override
	public void afterTextChanged(Editable et) {
		m_widgets_dirty = true;
	} // afterTextChanged (et)

	//-----------------------------------
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	//-----------------------------------
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}


	//-------------------------
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED) {
			return;
		}

		if (requestCode == WGlobals.NUMBERACTIVITY) {
			m_widgets_dirty = true;
			m_done.setEnabled(true);
			int ex_num = data.getIntExtra(NumberActivity.ITT_KEY_RETURN_NUM, -1);
			switch (ex_num) {
				case DatabaseHelper.EXERCISE_COL_REP_NUM:
					{
						String reps = data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE);
						reps = test_for_empty_string (reps);
						if (WGlobals.g_wheel) {
							// Don't have to set the TV directly as it's connected to
							// the wheel.
							m_reps_wheels.set_value(Integer.parseInt(reps), true);
						}
						else {
							m_reps_tv.setText(reps);
						}
					}
					break;

				case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
					{
						String weight = data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE);
						weight = test_for_empty_string (weight);
						if (WGlobals.g_wheel) {
							m_weight_wheels.set_value(Float.parseFloat(weight), true);
						}
						else {
							m_weight_tv.setText(weight);
						}
					}
					break;

				case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
					{
						String level = data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE);
						level = test_for_empty_string (level);
						if (WGlobals.g_wheel) {
							m_level_wheels.set_value(Integer.parseInt(level), true);
						}
						else {
							m_level_tv.setText(level);
						}
					}
					break;

				case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
					{
						String cals = data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE);
						if (WGlobals.g_wheel) {
							m_calorie_wheels.set_value(Integer.parseInt(cals), true);
						}
						else {
							m_calorie_tv.setText(cals);
						}
					}
					break;

				case DatabaseHelper.EXERCISE_COL_DIST_NUM:
					{
						String dist = data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE);
						dist = test_for_empty_string (dist);
						if (WGlobals.g_wheel) {
							m_dist_wheels.set_value(Float.parseFloat(dist), true);
						}
						else {
							m_dist_tv.setText(dist);
						}
					}
					break;

				case DatabaseHelper.EXERCISE_COL_TIME_NUM:
					{
						String time = data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE);
						time = test_for_empty_string (time);
						if (WGlobals.g_wheel) {
							m_time_wheels.set_value(Float.parseFloat(time), true);
						}
						else {
							m_time_tv.setText(time);
						}
					}
					break;

				case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
					{
						String other = data.getStringExtra(NumberActivity.ITT_KEY_RETURN_VALUE);
						other = test_for_empty_string (other);
						if (WGlobals.g_wheel) {
							m_other_wheels.set_value(Float.parseFloat(other), true);
						}
						else {
							m_other_tv.setText(other);
						}
					}
					break;

				default:
					Toast.makeText(this, "Illegal ex_num in onActivityResult()", Toast.LENGTH_LONG).show();
					break;
			}
		} // returned from NumberActivity




	} // onActivityResult (...)


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
		if (m_task.m_ex_data.breps) {
			m_reps_wheels = new WheelInt(this, new int[]
							{
							R.id.aset_wheel_reps_1,
							R.id.aset_wheel_reps_10,
							R.id.aset_wheel_reps_100
//							R.id.aset_wheel_reps_1000
							});
			m_reps_wheels.set_tv(m_reps_tv);
			m_reps_wheels.setOnWheelIntChangedListener(this);
		}

		if (m_task.m_ex_data.blevel) {
			m_level_wheels = new WheelInt(this, new int[]
							{
							R.id.aset_wheel_level_1,
							R.id.aset_wheel_level_10,
							R.id.aset_wheel_level_100
//							R.id.aset_wheel_level_1000
							});
			m_level_wheels.set_tv(m_level_tv);
			m_level_wheels.setOnWheelIntChangedListener(this);
		}

		if (m_task.m_ex_data.bcals) {
			m_calorie_wheels = new WheelInt(this, new int[]
							{
							R.id.aset_wheel_calorie_1,
							R.id.aset_wheel_calorie_10,
							R.id.aset_wheel_calorie_100
//							R.id.aset_wheel_calorie_1000
							});
			m_calorie_wheels.set_tv(m_calorie_tv);
			m_calorie_wheels.setOnWheelIntChangedListener(this);
		}

		// The floats
		if (m_task.m_ex_data.bweight) {
			m_weight_wheels = new WheelFloat (this, new int[]
							{
							R.id.aset_wheel_weight_point,
							R.id.aset_wheel_weight_1,
							R.id.aset_wheel_weight_10,
							R.id.aset_wheel_weight_100
//							R.id.aset_wheel_weight_1000
							},
							1);
			m_weight_wheels.set_tv(m_weight_tv);
			m_weight_wheels.setOnWheelFloatChangedListener(this);
		}

		if (m_task.m_ex_data.bdist) {
			m_dist_wheels = new WheelFloat (this, new int[]
							{
							R.id.aset_wheel_dist_point,
							R.id.aset_wheel_dist_1,
							R.id.aset_wheel_dist_10,
							R.id.aset_wheel_dist_100
//							R.id.aset_wheel_dist_1000
							},
							1);
			m_dist_wheels.set_tv(m_dist_tv);
			m_dist_wheels.setOnWheelFloatChangedListener(this);
		}

		if (m_task.m_ex_data.btime) {
			m_time_wheels = new WheelFloat (this, new int[]
							{
							R.id.aset_wheel_time_point,
							R.id.aset_wheel_time_1,
							R.id.aset_wheel_time_10,
							R.id.aset_wheel_time_100
//							R.id.aset_wheel_time_1000
							},
							1);
			m_time_wheels.set_tv(m_time_tv);
			m_time_wheels.setOnWheelFloatChangedListener(this);
		}

		if (m_task.m_ex_data.bother) {
			m_other_wheels = new WheelFloat (this, new int[]
							{
							R.id.aset_wheel_other_point,
							R.id.aset_wheel_other_1,
							R.id.aset_wheel_other_10,
							R.id.aset_wheel_other_100
//							R.id.aset_wheel_other_1000
							},
							1);
			m_other_wheels.set_tv(m_other_tv);
			m_other_wheels.setOnWheelFloatChangedListener(this);
		}
	} // init_wheels()


	/********************
	 * This helper method takes a string (that SHOULD be a number)
	 * and fixes it.  If the string is null or zero length, then
	 * a zero is returned.  In all other cases the original string
	 * is returned.
	 *
	 * @param str	The string to examine.
	 *
	 * @return	If str is null or has a length of 0, then "0" is
	 * 			returned.  Otherwise str is returned.
	 */
	private String test_for_empty_string (String str) {
		if ((str == null) || (str.length() == 0)) {
			return "0";
		}
		return str;
	}


	/********************
	 * Working by side effect, this sets the width of
	 * all the wheels to the current wheel setting.
	 * <p>
	 * preconditions:<br/>
	 * 	g_wheel_width	Holds the correct wheel size.
	 * <p>
	 * 	all the wheel widgets are properly initialized.
	 * <p>
	 * side effects:<br/>
	 * 	- all the wheels will have their widths changed to
	 * 	match the current global wheel setting (which presumably
	 * 	has potentially changed).
	 */
	private void set_wheel_width() {
		if (m_task.m_ex_data.breps)
			m_reps_wheels.set_wheel_width(WGlobals.g_wheel_width);
		if (m_task.m_ex_data.blevel)
			m_level_wheels.set_wheel_width(WGlobals.g_wheel_width);
		if (m_task.m_ex_data.bcals)
			m_calorie_wheels.set_wheel_width(WGlobals.g_wheel_width);
		if (m_task.m_ex_data.bweight)
			m_weight_wheels.set_wheel_width(WGlobals.g_wheel_width);
		if (m_task.m_ex_data.bdist)
			m_dist_wheels.set_wheel_width(WGlobals.g_wheel_width);
		if (m_task.m_ex_data.btime)
			m_time_wheels.set_wheel_width(WGlobals.g_wheel_width);
		if (m_task.m_ex_data.bother)
			m_other_wheels.set_wheel_width(WGlobals.g_wheel_width);
	} // set_wheel_width()


	/********************
	 * Simply fills in the information for the REPS aspect of
	 * the ASetActivity.  If there are no reps, then that
	 * part is set to GONE so it doesn't display.
	 *
	 * preconditions:
	 * 		m_ex_data		Both of these are filled in or NULL if
	 * 		m_last_set		not applicable.
	 */
	protected void setup_reps() {
		TextView reps_label_tv;
		if (WGlobals.g_wheel)
			reps_label_tv = (TextView) findViewById(R.id.aset_wheel_reps_label_tv);
		else
			reps_label_tv = (TextView) findViewById(R.id.aset_reps_label_tv);

		if (m_task.m_ex_data.breps) {
			m_reps_tv.setEnabled(true);
			m_reps_tv.setFocusable(true);
			if (m_task.m_ex_data.significant == DatabaseHelper.EXERCISE_COL_REP_NUM) {
				reps_label_tv.setTypeface(null, Typeface.BOLD);
			}

			if (m_task.m_last_set != null) {
				if (m_task.m_last_set.reps != -1) {
					m_reps_tv.setText("" + m_task.m_last_set.reps);
					if (WGlobals.g_wheel)
						m_reps_wheels.set_value(m_task.m_last_set.reps, false);
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
	protected void setup_levels() {
		TextView level_label_tv;
		if (WGlobals.g_wheel)
			level_label_tv = (TextView) findViewById(R.id.aset_wheel_level_label_tv);
		else
			level_label_tv = (TextView) findViewById(R.id.aset_level_label_tv);

		if (m_task.m_ex_data.blevel) {
			m_level_tv.setEnabled(true);
			m_level_tv.setFocusable(true);
			if (m_task.m_ex_data.significant == DatabaseHelper.EXERCISE_COL_LEVEL_NUM) {
				level_label_tv.setTypeface(null, Typeface.BOLD);
			}
			if (m_task.m_last_set != null) {
				if (m_task.m_last_set.levels != -1) {
					m_level_tv.setText("" + m_task.m_last_set.levels);
					if (WGlobals.g_wheel)
						m_level_wheels.set_value(m_task.m_last_set.levels, false);
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
	protected void setup_calories() {
		TextView calorie_label_tv;
		if (WGlobals.g_wheel)
			calorie_label_tv = (TextView) findViewById(R.id.aset_wheel_calorie_label_tv);
		else
			calorie_label_tv = (TextView) findViewById(R.id.aset_calorie_label_tv);

		if (m_task.m_ex_data.bcals) {
			m_calorie_tv.setEnabled(true);
			m_calorie_tv.setFocusable(true);
			if (m_task.m_ex_data.significant == DatabaseHelper.EXERCISE_COL_CALORIE_NUM) {
				calorie_label_tv.setTypeface(null, Typeface.BOLD);
			}
			if (m_task.m_last_set != null) {
				if (m_task.m_last_set.cals != -1) {
					m_calorie_tv.setText("" + m_task.m_last_set.cals);
					if (WGlobals.g_wheel)
						m_calorie_wheels.set_value(m_task.m_last_set.cals, false);
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
	 * preconditions:
	 * 		m_ex_data		Both of these are filled in or NULL if
	 * 		m_last_set		not applicable.
	 */
	protected void setup_weights() {
		TextView weight_label_tv;
		if (WGlobals.g_wheel) {
			weight_label_tv = (TextView) findViewById(R.id.aset_wheel_weight_label_tv);
		}
		else {
			weight_label_tv = (TextView) findViewById(R.id.aset_weight_label_tv);
		}

		if (m_task.m_ex_data.bweight) {
			m_weight_tv.setEnabled(true);
			m_weight_tv.setFocusable(true);
			String weight_unit = getString(R.string.aset_weight_hint,
					m_task.m_ex_data.weight_unit);
			weight_label_tv.setText(getString(R.string.aset_weight_label,
					weight_unit));
			if (m_task.m_ex_data.significant == DatabaseHelper.EXERCISE_COL_WEIGHT_NUM) {
				weight_label_tv.setTypeface(null,
						Typeface.BOLD);
			}

			// Not the first time this set has been added.
			if (m_task.m_last_set != null) {
				if (m_task.m_last_set.weight != -1) {
					m_weight_tv.setText("" + m_task.m_last_set.weight);
					if (WGlobals.g_wheel)
						m_weight_wheels.set_value(m_task.m_last_set.weight, false);
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
	protected void setup_dist() 	{
		TextView dist_label_tv;
		if (WGlobals.g_wheel)
			dist_label_tv = (TextView) findViewById(R.id.aset_wheel_dist_label_tv);
		else
			dist_label_tv = (TextView) findViewById(R.id.aset_dist_label_tv);

		if (m_task.m_ex_data.bdist) {
			m_dist_tv.setEnabled(true);
			m_dist_tv.setFocusable(true);
//			m_dist_et.setHint(dist_unit);
			dist_label_tv.setText(getString(R.string.aset_distance_label,
					m_task.m_ex_data.dist_unit));
			if (m_task.m_ex_data.significant == DatabaseHelper.EXERCISE_COL_DIST_NUM) {
				dist_label_tv.setTypeface(null, Typeface.BOLD);
			}
			if (m_task.m_last_set != null) {
				if (m_task.m_last_set.dist != -1) {
					m_dist_tv.setText("" + m_task.m_last_set.dist);
					if (WGlobals.g_wheel)
						m_dist_wheels.set_value(m_task.m_last_set.dist, false);
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
	protected void setup_time() {
		TextView time_label_tv;
		if (WGlobals.g_wheel)
			time_label_tv = (TextView) findViewById(R.id.aset_wheel_time_label_tv);
		else
			time_label_tv = (TextView) findViewById(R.id.aset_time_label_tv);

		if (m_task.m_ex_data.btime) {
			m_time_tv.setEnabled(true);
			m_time_tv.setFocusable(true);
//			m_time_et.setHint(time_unit);
			if (m_task.m_ex_data.significant == DatabaseHelper.EXERCISE_COL_TIME_NUM) {
				time_label_tv.setTypeface(null, Typeface.BOLD);
			}
			time_label_tv.setText(getString(R.string.aset_time_label,
					m_task.m_ex_data.time_unit));
			if (m_task.m_last_set != null) {
				if (m_task.m_last_set.time != -1) {
					m_time_tv.setText("" + m_task.m_last_set.time);
					if (WGlobals.g_wheel)
						m_time_wheels.set_value(m_task.m_last_set.time, false);
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
	protected void setup_other() {
		TextView other_label_tv;
		if (WGlobals.g_wheel)
			other_label_tv = (TextView) findViewById(R.id.aset_wheel_other_label_tv);
		else
			other_label_tv = (TextView) findViewById(R.id.aset_other_label_tv);

		if (m_task.m_ex_data.bother) {
			m_other_tv.setEnabled(true);
			m_other_tv.setFocusable(true);

//			m_other_et.setHint(final_other_label);
			other_label_tv.setText(getString(R.string.aset_other_label_old,
					m_task.m_ex_data.other_title, m_task.m_ex_data.other_unit));
			if (m_task.m_ex_data.significant == DatabaseHelper.EXERCISE_COL_OTHER_NUM) {
				other_label_tv.setTypeface(null, Typeface.BOLD);
			}

			if (m_task.m_last_set != null) {
				if (m_task.m_last_set.other != -1) {
					m_other_tv.setText("" + m_task.m_last_set.other);
					if (WGlobals.g_wheel)
						m_other_wheels.set_value(m_task.m_last_set.other, false);
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
	protected void setup_notes() {
		if (m_task.m_last_set != null) {
			if (m_task.m_last_set.notes != null) {
				m_notes_tv.setHint(m_task.m_last_set.notes);
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

		if (m_reps_tv.isEnabled())
			count++;
		if (m_weight_tv.isEnabled())
			count++;
		if (m_level_tv.isEnabled())
			count++;
		if (m_calorie_tv.isEnabled())
			count++;
		if (m_dist_tv.isEnabled())
			count++;
		if (m_time_tv.isEnabled())
			count++;
		if (m_other_tv.isEnabled())
			count++;
		return count;
	} // get_num_enabled_forms()


	/*************************
	 * Clears all the active widgets, allowing the
	 * hints to show.
	 */
	private void clear() {
		if (m_reps_tv.isEnabled())
			m_reps_tv.setText(null);
		if (m_weight_tv.isEnabled())
			m_weight_tv.setText(null);
		if (m_level_tv.isEnabled())
			m_level_tv.setText(null);
		if (m_calorie_tv.isEnabled())
			m_calorie_tv.setText(null);
		if (m_dist_tv.isEnabled())
			m_dist_tv.setText(null);
		if (m_time_tv.isEnabled())
			m_time_tv.setText(null);
		if (m_other_tv.isEnabled())
			m_other_tv.setText(null);
		clear_stress();
		clear_wheels();
		m_notes_tv.setText(null);
		m_notes_tv.setHint(null);
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
			if (m_task.m_ex_data.breps)
				m_reps_wheels.reset(true);
			if (m_task.m_ex_data.blevel)
				m_level_wheels.reset(true);
			if (m_task.m_ex_data.bcals)
				m_calorie_wheels.reset(true);
			if (m_task.m_ex_data.bweight)
				m_weight_wheels.reset(true);
			if (m_task.m_ex_data.bdist)
				m_dist_wheels.reset(true);
			if (m_task.m_ex_data.btime)
				m_time_wheels.reset(true);
			if (m_task.m_ex_data.bother)
				m_other_wheels.reset(true);
		}
	}


	/*************************
	 * Displays a nice toast that both informs the user
	 * that they entered a set and encourages them.
	 *
	 * Also plays a nice sound.
	 *
	 * @param name	The name of the exercise they completed.
	 */
	private void entered_set_msg (String name) {
		String[] args = {name};
		int msg_id = 0;

		WGlobals.play_completion_sound();

		// What message we use depends on the condition.
		if (m_ok_rb.isChecked()) {
			// Pick one of the 'ok' messages.
			switch ((int) (Math.random() * 4)) {
				case 0:
					msg_id = R.string.aset_entered_ok_msg0;
					break;
				case 1:
					msg_id = R.string.aset_entered_ok_msg1;
					break;
				case 2:
					msg_id = R.string.aset_entered_ok_msg2;
					break;
				case 3:
					msg_id = R.string.aset_entered_ok_msg3;
					break;
				default:
					Log.e(tag, "Illegal random value for OK!");
					break;
			}
		}
		else if (m_plus_rb.isChecked()) {
			switch ((int) (Math.random() * 4)) {
				case 0:
					msg_id = R.string.aset_entered_plus_msg0;
					break;
				case 1:
					msg_id = R.string.aset_entered_plus_msg1;
					break;
				case 2:
					msg_id = R.string.aset_entered_plus_msg2;
					break;
				case 3:
					msg_id = R.string.aset_entered_plus_msg3;
					break;
				default:
					Log.e(tag, "Illegal random value for OK!");
					break;
			}
		}
		else if (m_minus_rb.isChecked()) {
			switch ((int) (Math.random() * 4)) {
				case 0:
					msg_id = R.string.aset_entered_minus_msg0;
					break;
				case 1:
					msg_id = R.string.aset_entered_minus_msg1;
					break;
				case 2:
					msg_id = R.string.aset_entered_minus_msg2;
					break;
				case 3:
					msg_id = R.string.aset_entered_minus_msg3;
					break;
				default:
					Log.e(tag, "Illegal random value for OK!");
					break;
			}
		}
		else if (m_x_rb.isChecked()) {
			switch ((int) (Math.random() * 4)) {
				case 0:
					msg_id = R.string.aset_entered_x_msg0;
					break;
				case 1:
					msg_id = R.string.aset_entered_x_msg1;
					break;
				case 2:
					msg_id = R.string.aset_entered_x_msg2;
					break;
				case 3:
					msg_id = R.string.aset_entered_x_msg3;
					break;
				default:
					Log.e(tag, "Illegal random value for OK!");
					break;
			}
		}
		else {		//SET_COND_NONE;
			Log.e(tag, "Illegal stress condition while saving!");
			return;
		}

		my_toast(this, msg_id, args);

	} // entered_set_msg (name)



	/*************************
	 * Assumes that it's time to save the data (no checks
	 * needed).  So put it into the DB and kill this Activity.
	 *
	 * We will return (in our Intent) the ID of this exercise.
	 */
	private void save() {
		String str;
		SQLiteDatabase db = null;

		try {
			db = WGlobals.g_db_helper.getWritableDatabase();

			// Collect our data for this set.
			ContentValues values = new ContentValues();

			// First, the name of the exercise!
			values.put(DatabaseHelper.SET_COL_NAME, m_task.m_ex_data.name);

			if (m_reps_tv.isEnabled()) {
				str = m_reps_tv.getText().toString();
				values.put(DatabaseHelper.SET_COL_REPS,
								my_parse(str, false).i);
			}

			if (m_weight_tv.isEnabled()) {
				str = m_weight_tv.getText().toString();
				values.put(DatabaseHelper.SET_COL_WEIGHT,
								my_parse(str, true).f);
			}

			if (m_level_tv.isEnabled()) {
				str = m_level_tv.getText().toString();
				values.put(DatabaseHelper.SET_COL_LEVELS,
								my_parse(str, false).i);
			}

			if (m_calorie_tv.isEnabled()) {
				str = m_calorie_tv.getText().toString();
				values.put(DatabaseHelper.SET_COL_CALORIES,
								my_parse(str, false).i);
			}

			if (m_dist_tv.isEnabled()) {
				str = m_dist_tv.getText().toString();
				values.put(DatabaseHelper.SET_COL_DIST,
								my_parse(str, true).f);
			}

			if (m_time_tv.isEnabled()) {
				str = m_time_tv.getText().toString();
				values.put(DatabaseHelper.SET_COL_TIME,
								my_parse(str, true).f);
			}

			if (m_other_tv.isEnabled()) {
				str = m_other_tv.getText().toString();
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
			values.put(DatabaseHelper.SET_COL_NOTES, m_notes_tv.getText().toString());

			// Don't forget the date/time!
			Calendar now = Calendar.getInstance();
			values.put(DatabaseHelper.SET_COL_DATEMILLIS, now.getTimeInMillis());

			// That's it!  Insert and we're done.
			db.insert(DatabaseHelper.SET_TABLE_NAME, null, values);
		}
		catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (db != null) {
				db.close();
				db = null;
			}
		}

		entered_set_msg (m_task.m_ex_data.name);

		// Indicate that the database has changed and tell the
		//	other activities to reset.
		m_db_dirty = true;
		InspectorActivity2.m_db_dirty = true;
		GraphActivity.m_db_dirty = true;
		ExerciseTabHostActivity.m_dirty = true;

		// After the save, clear the stress and cause the note to
		// turned into a hint.
		clear_stress();
		m_notes_tv.setHint(m_notes_tv.getText());
		m_notes_tv.setText(null);
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

			if (m_task.m_last_set == null) {
				// First time doing this set, so there's nothing
				// in any of the widgets.  Tell 'em about it and
				// get outahere!
				show_help_dialog(R.string.aset_nag_enter_empty_title,
						R.string.aset_nag_enter_empty_msg);
				return;
			}

			save();
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

		if (m_reps_tv.isEnabled()) {
			str = m_reps_tv.getText();
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

		if (m_weight_tv.isEnabled()) {
			str = m_weight_tv.getText();
			if ((str == null) || (str.length() == 0)) {
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_WEIGHT_NUM))
					return;
				blank_forms.add(getString (R.string.aset_weight_off_label));
			}
		}

		if (m_level_tv.isEnabled()) {
			str = m_level_tv.getText();
			if ((str == null) || (str.length() == 0)) {
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_LEVEL_NUM))
					return;
				blank_forms.add(getString (R.string.aset_level_label));
			}
		}

		if (m_calorie_tv.isEnabled()) {
			str = m_calorie_tv.getText();
			if ((str == null) || (str.length() == 0)) {
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_CALORIE_NUM))
					return;
				blank_forms.add(getString (R.string.aset_calorie_label));
			}
		}

		if (m_dist_tv.isEnabled()) {
			str = m_dist_tv.getText();
			if ((str == null) || (str.length() == 0)) {
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_DIST_NUM))
					return;
				blank_forms.add(getString (R.string.aset_distance_off_label));
			}
		}

		if (m_time_tv.isEnabled()) {
			str = m_time_tv.getText();
			if ((str == null) || (str.length() == 0)) {
				if (test_and_warn_significant(DatabaseHelper.EXERCISE_COL_TIME_NUM))
					return;
				blank_forms.add(getString (R.string.aset_time_off_label));
			}
		}

		if (m_other_tv.isEnabled()) {
			str = m_other_tv.getText();
			if ((str == null) || (str.length() == 0)) {
				// The other is a little different.  So do it man-
				// ually here.
				TextView other_label_tv =
						(TextView) findViewById(WGlobals.g_wheel ?
								R.id.aset_wheel_other_label_tv :
								R.id.aset_other_label_tv);

				if (m_task.m_ex_data.significant == DatabaseHelper.EXERCISE_COL_OTHER_NUM) {
					String args[] = {other_label_tv.getText().toString(),
									other_label_tv.getText().toString(), m_task.m_ex_data.name};
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
					WGlobals.play_short_click();
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
		if (m_task.m_ex_data.significant == column_num) {
			String column_array[] = getResources().getStringArray(R.array.exercise_column_names_array);

			String args[] = {column_array[column_num], column_array[column_num], m_task.m_ex_data.name};
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
//	private void test_m_db() {
//		if (m_db != null) {
//			// The database may be used by another tab.  Give
//			// it some time to finish.
//			try {
//				Thread.sleep(1000);
//			}
//			catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			if (m_db != null)
//				throw new SQLiteException("m_db not null when starting doInBackground() in ASetActivity!");
//		}
//	} // test_m_db()


	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//	Classes
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/******************************
	 * Starts a loading dialog while this asynchronously
	 * hits the database to get our data.
	 */
	static class ASetASyncTask extends AsyncTask <Void, Void, Void> {

		private static final String tag = "ASetSyncTask";

		/** TRUE while data is being loaded from the DB */
//		private boolean m_loading = false;

		/** Will be TRUE when the ASyncTask has finished. */
		boolean m_done = false;

		/**
		 * The Activity that is using this ASyncTask.
		 * This static class may ONLY access the activity
		 * through this data member.
		 * <p>
		 * NOTE: Make sure this is not NULL before using!!!
		 * (Actually, this is not necessary. Google promises that
		 * this will always be valid when done in a UI thread.)
		 */
		private AddSetActivity m_activity = null;

		/** Holds all info about this exercise. */
		protected ExerciseData m_ex_data = null;

		/**
		 * Holds the data for the most recently done set (which
		 * is used to fill in our UI).  This is NULL if there
		 * is no such set (first time the user sees this exercise).
		 */
		protected SetData m_last_set = null;



		/***************
		 * Constructor
		 *
		 * Needs a reference to the Activity that's creating
		 * this ASyncTask.  It's how this static class
		 * communicates with that Activity.
		 */
		public ASetASyncTask (AddSetActivity activity) {
//			Log.v(tag, "entering constructor, count = " + m_instance_counter +
//				", id = " + this.toString());
			attach (activity);
		} // constructor


		//---------------------
		//	Initializations here.
		//
		@Override
		protected void onPreExecute() {
			if (m_activity == null) {
				Log.e (tag, "m_activity is NULL!!! We're about to make a lot of errors!");
			}

//			m_loading = true;
			m_done = false;
		} // onPreExecute()


		//---------------------
		//	Load up our data here.
		//
		//	args		not used
		//
		@Override
		protected Void doInBackground(Void... args) {

//			Log.d (tag, "doInBackground() starting...");

			// Mark that loading has begun.
//			m_loading = true;

			// Get the info from the Intent that GridActivity sent.
			Intent itt = m_activity.getIntent();
			String exercise_name = itt.getStringExtra(ExerciseTabHostActivity.KEY_NAME);

			if (exercise_name == null) {
				Log.e(tag,"ASetActivity: Problem trying to get the exercise name in fill_forms()");
				return null;
			}

			SQLiteDatabase db = null;

			try {
				// Read in that row from the database.
				db = WGlobals.g_db_helper.getReadableDatabase();

				// Read in all the info we need about this
				// exercise.
				m_ex_data = DatabaseHelper.getExerciseData(db, exercise_name);

				Cursor set_cursor = null;
				try {
					set_cursor = DatabaseHelper.getLastSet(db, exercise_name);
					if (set_cursor.moveToFirst()) {
						// Fill in, but only if there IS something
						// to fill!  Otherwise leave this as NULL.
						m_last_set = DatabaseHelper.getSetData(set_cursor);
					}

				} catch (SQLiteException e) {
					e.printStackTrace();
				}
				finally {
					if (set_cursor != null) {
						set_cursor.close();
						set_cursor = null;
					}
				}
			}
			catch (SQLiteException e) {
				e.printStackTrace();
			}
			finally {
				if (db != null) {
					db.close();
					db = null;
				}
//				m_loading = false;	// Finished loading!
			}

			return null;
		} // doInBackground(.)


		//---------------------
		//	This is where the UI stuff is set.
		//
		//	result		Not used.
		//
		@Override
		protected void onPostExecute(Void result) {
			if (m_activity == null) {
				Log.e(tag, "onPostExecute() can't find the Activity!!! This is really bad news.");
			}

//			try {
//				while (m_loading == true) {
//					Log.e (tag, "m_loading is TRUE in onPostExecute()!  This should never happen!");
//					Thread.sleep(100);	// Wait 1/10 of a second.
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

			if (WGlobals.g_wheel) {
				m_activity.setContentView(R.layout.aset_wheel);
				m_activity.init_widgets_with_wheels();
				m_activity.init_wheels();
			}
			else {
				m_activity.setContentView(R.layout.aset);
				m_activity.init_widgets_no_wheels();
			}

			/**
			 * Indicates if we should draw a bar separating the
			 * row from the one above it.
			 */
			boolean need_bar = false;

			// Fill in the aspects.
			m_activity.setup_reps();
			need_bar = m_ex_data.breps;

			m_activity.setup_weights();
			need_bar =m_activity. turn_off_bar(WGlobals.g_wheel ?
										R.id.aset_wheel_weight_bar :
										R.id.aset_weight_bar,
										m_ex_data.bweight, need_bar);

			m_activity.setup_levels();
			need_bar = m_activity.turn_off_bar(WGlobals.g_wheel ?
										R.id.aset_wheel_level_bar :
										R.id.aset_level_bar,
										m_ex_data.blevel, need_bar);

			m_activity.setup_calories();
			need_bar = m_activity.turn_off_bar(WGlobals.g_wheel ?
										R.id.aset_wheel_cals_bar :
										R.id.aset_cals_bar,
										m_ex_data.bcals, need_bar);

			m_activity.setup_dist();
			need_bar = m_activity.turn_off_bar(WGlobals.g_wheel ?
										R.id.aset_wheel_dist_bar :
										R.id.aset_dist_bar,
										m_ex_data.bdist, need_bar);

			m_activity.setup_time();
			need_bar = m_activity.turn_off_bar(WGlobals.g_wheel ?
										R.id.aset_wheel_time_bar :
										R.id.aset_time_bar,
										m_ex_data.btime, need_bar);

			m_activity.setup_other();
			need_bar = m_activity.turn_off_bar(WGlobals.g_wheel ?
										R.id.aset_wheel_other_bar :
										R.id.aset_other_bar,
										m_ex_data.bother, need_bar);

			m_activity.setup_notes();

			m_reset_widgets = false;		// We've done our work here!

			m_done = true;	// Finally!

			if (m_activity.is_progress_dialog_active()) {
				m_activity.stop_progress_dialog();
			}
			WGlobals.g_sound = true;		// Since this was turned off during onCreate().

		} // onPostExecute(.)


		/***************
		 * Connects this task to an Activity, allowing
		 * this static class to communicate with that
		 * Activity (so it can get the data we're reading
		 * from the database!).
		 *
		 * @param activity	The Activity that wants to
		 * 					use the data.
		 */
		public void attach (AddSetActivity activity) {
			m_activity = activity;
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
			m_activity = null;
		}

		/****************
		 * Please call this when the connecting Activity
		 * goes away for good.  This will free up lots of
		 * resources!
		 */
		public void kill() {
//			Log.d(tag, "entering kill(), id = " + this.toString());

			// todo	garbage collect

			m_activity = null;
		}

		/*****************
		 * Call this to see if the ASyncTask is complete.
		 *
		 * The done state is reset (to false) when onPreExecute()
		 * is called, and terminated (true) during onPostExecute().
		 */
		public boolean isDone() {
			return m_done;
		}


	} // class ASetSyncTask


}
