/**
 * This is the Activity that pops up after the user has
 * long-clicked on an exercise name.  Here they have the
 * option to change aspects of this exercise or remove it
 * completely.
 *
 * This Activity was started for a result.  So before
 * calling finish(), we need to make sure that setResult()
 * is called first, supplying either Activity.RESULT_OK or
 * Activity.RESULT_CANCELLED.
 */
package com.sleepfuriously.hpgworkout;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class EditExerciseActivity
					extends
						BaseDialogActivity
					implements
						OnClickListener,
						OnLongClickListener,
						TextWatcher,
						OnMySpinnerListener {


	//-------------------
	//	Constants
	//-------------------

	private static final String tag = "EditExerciseActivity";

	//-------------------
	//	Widget Data
	//-------------------

	EditText m_exer_name_et,
		m_exer_other_name_et, m_exer_other_unit_et;

	CheckBox m_exer_rep_cb, m_exer_level_cb, m_exer_weight_cb,
			m_exer_dist_cb, m_exer_time_cb, m_exer_other_cb,
			m_exer_calorie_cb;

	RadioButton m_exer_rep_rb, m_exer_level_rb, m_exer_weight_rb,
			m_exer_dist_rb, m_exer_time_rb, m_exer_other_rb,
			m_exer_calorie_rb;

	MySpinner m_exer_type_msp, m_exer_group_msp,
		m_exer_weight_msp, m_exer_dist_msp, m_exer_time_msp;

	Button m_ok, m_delete;


	//-------------------
	//	Other Data
	//-------------------

	/**
	 * Holds the Database's official name of this
	 * exercise.
	 */
	String m_orig_exercise_name;

	/**
	 * Holds the order that this exercise appears.  It's
	 * not kept in any widgets, so I have to hang on to
	 * it with a data member directly.
	 */
	int m_lorder;

	/** TRUE when the user changes something. */
	boolean m_dirty = false;

	/**
	 * Used to prevent setting the dirty bit (above) when the
	 * program is setting the EditTexts.
	 */
	boolean m_et_locked = false;


	//-------------------
	//	Methods
	//-------------------

	/***********
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editexercise);

		// The standard buttons.
		m_ok = (Button) findViewById(R.id.edit_exer_ok_butt);
		m_ok.setOnClickListener(this);
		m_ok.setOnLongClickListener(this);

		m_delete = (Button) findViewById(R.id.edit_exer_delete_butt);
		m_delete.setOnClickListener(this);
		m_delete.setOnLongClickListener(this);

		// name
		m_exer_name_et = (EditText) findViewById (R.id.editexer_name_et);
		m_exer_name_et.addTextChangedListener(this);

		// type
		m_exer_type_msp = (MySpinner) findViewById (R.id.edit_exer_type_msp);
		m_exer_type_msp.setMySpinnerListener(this);
		m_exer_type_msp.set_array(R.array.type_array);
		m_exer_type_msp.set_prompt(R.string.addexer_type_str);
		m_exer_type_msp.set_icon(R.drawable.hpglogo_36x36);
		m_exer_type_msp.setOnLongClickListener(this);

		// group
		m_exer_group_msp = (MySpinner) findViewById (R.id.edit_exer_group_msp);
		m_exer_group_msp.setMySpinnerListener(this);
		m_exer_group_msp.set_array(R.array.muscle_group_array);
		m_exer_group_msp.set_prompt(R.string.addexer_musclegroup_str);
		m_exer_group_msp.set_icon(R.drawable.hpglogo_36x36);
		m_exer_group_msp.setOnLongClickListener(this);

		// reps
		m_exer_rep_cb = (CheckBox) findViewById(R.id.edit_exer_reps_cb);
		m_exer_rep_cb.setOnClickListener(this);
		m_exer_rep_cb.setOnLongClickListener(this);
		m_exer_rep_rb = (RadioButton) findViewById(R.id.edit_exer_reps_rad);
		m_exer_rep_rb.setOnClickListener(this);
		m_exer_rep_rb.setOnLongClickListener(this);

		// levels
		m_exer_level_cb = (CheckBox) findViewById(R.id.edit_exer_level_cb);
		m_exer_level_cb.setOnClickListener(this);
		m_exer_level_cb.setOnLongClickListener(this);
		m_exer_level_rb = (RadioButton) findViewById(R.id.edit_exer_level_rad);
		m_exer_level_rb.setOnClickListener(this);
		m_exer_level_rb.setOnLongClickListener(this);

		// calorie
		m_exer_calorie_cb = (CheckBox) findViewById(R.id.edit_exer_calorie_cb);
		m_exer_calorie_cb.setOnClickListener(this);
		m_exer_calorie_cb.setOnLongClickListener(this);
		m_exer_calorie_rb = (RadioButton) findViewById(R.id.edit_exer_calorie_rad);
		m_exer_calorie_rb.setOnClickListener(this);
		m_exer_calorie_rb.setOnLongClickListener(this);

		// weights
		m_exer_weight_rb = (RadioButton) findViewById(R.id.edit_exer_weights_rad);
		m_exer_weight_rb.setOnClickListener(this);
		m_exer_weight_rb.setOnLongClickListener(this);
		m_exer_weight_cb = (CheckBox) findViewById(R.id.edit_exer_weight_cb);
		m_exer_weight_cb.setOnClickListener(this);
		m_exer_weight_cb.setOnLongClickListener(this);
		m_exer_weight_msp = (MySpinner) findViewById (R.id.edit_exer_weight_unit_msp);
		m_exer_weight_msp.setMySpinnerListener(this);
		m_exer_weight_msp.set_array(R.array.weight_unit_array);
		m_exer_weight_msp.set_prompt(R.string.addexer_weight_unit_title);
		m_exer_weight_msp.set_icon(R.drawable.hpglogo_36x36);
		m_exer_weight_msp.setOnLongClickListener(this);

		Intent weight_itt = new Intent();
		weight_itt.setClassName(getPackageName(), AddWeightItemActivity.class.getName());
		m_exer_weight_msp.set_user_add(m_exer_weight_msp.length() - 1,
									weight_itt,
									m_exer_weight_msp.getId());
		m_exer_weight_msp.set_icon(R.drawable.hpglogo_36x36);
		m_exer_weight_msp.set_prompt(R.string.add_weight_item_prompt);

		// distanced
		m_exer_dist_rb = (RadioButton) findViewById(R.id.edit_exer_dist_rad);
		m_exer_dist_rb.setOnClickListener(this);
		m_exer_dist_rb.setOnLongClickListener(this);
		m_exer_dist_cb = (CheckBox) findViewById(R.id.edit_exer_dist_cb);
		m_exer_dist_cb.setOnClickListener(this);
		m_exer_dist_cb.setOnLongClickListener(this);
		m_exer_dist_msp = (MySpinner) findViewById (R.id.edit_exer_dist_unit_msp);
		m_exer_dist_msp.setMySpinnerListener(this);
		m_exer_dist_msp.set_array(R.array.dist_unit_array);
		m_exer_dist_msp.set_prompt(R.string.addexer_dist_unit_title);
		m_exer_dist_msp.set_icon(R.drawable.hpglogo_36x36);
		m_exer_dist_msp.setOnLongClickListener(this);

		Intent dist_itt = new Intent();
		dist_itt.setClassName(getPackageName(), AddDistItemActivity.class.getName());
		m_exer_dist_msp.set_user_add(m_exer_dist_msp.length() - 1,
									dist_itt,
									m_exer_dist_msp.getId());
		m_exer_dist_msp.set_icon(R.drawable.hpglogo_36x36);
		m_exer_dist_msp.set_prompt(R.string.add_dist_item_prompt);

		// time
		m_exer_time_rb = (RadioButton) findViewById(R.id.edit_exer_time_rad);
		m_exer_time_rb.setOnClickListener(this);
		m_exer_time_rb.setOnLongClickListener(this);
		m_exer_time_cb = (CheckBox) findViewById(R.id.edit_exer_time_cb);
		m_exer_time_cb.setOnClickListener(this);
		m_exer_time_cb.setOnLongClickListener(this);
		m_exer_time_msp = (MySpinner) findViewById (R.id.edit_exer_time_unit_msp);
		m_exer_time_msp.setMySpinnerListener(this);
		m_exer_time_msp.set_array(R.array.time_unit_array);
		m_exer_time_msp.set_prompt(R.string.addexer_time_unit_title);
		m_exer_time_msp.set_icon(R.drawable.hpglogo_36x36);
		m_exer_time_msp.setOnLongClickListener(this);

		Intent time_itt = new Intent();
		time_itt.setClassName(getPackageName(), AddTimeItemActivity.class.getName());
		m_exer_time_msp.set_user_add(m_exer_time_msp.length() - 1,
									time_itt,
									m_exer_time_msp.getId());
		m_exer_time_msp.set_icon(R.drawable.hpglogo_36x36);
		m_exer_time_msp.set_prompt(R.string.add_time_item_prompt);

		// other
		m_exer_other_rb = (RadioButton) findViewById(R.id.edit_exer_other_rad);
		m_exer_other_rb.setOnClickListener(this);
		m_exer_other_rb.setOnLongClickListener(this);
		m_exer_other_cb = (CheckBox) findViewById(R.id.edit_exer_other_cb);
		m_exer_other_cb.setOnClickListener(this);
		m_exer_other_cb.setOnLongClickListener(this);
		m_exer_other_name_et = (EditText) findViewById(R.id.edit_exer_other_name_et);
		m_exer_other_name_et.setOnLongClickListener(this);
		m_exer_other_name_et.addTextChangedListener(this);
		m_exer_other_unit_et = (EditText) findViewById(R.id.edit_exer_other_unit_et);
		m_exer_other_unit_et.setOnLongClickListener(this);
		m_exer_other_unit_et.addTextChangedListener(this);

		if (fill_forms() == false) {
			Toast.makeText(this, "Problems filling out the forms in EditExercise#onCreate()", Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED);
			finish();
		}

	} // onCreate();



	//-----------------------------
	//	Gotta get rid of those damn dialogs during an
	//	orientation change.
	@Override
	protected void onPause() {
		if ((MySpinner.m_dialog != null) && (MySpinner.m_dialog.isShowing())) {
			MySpinner.m_dialog.dismiss();
			MySpinner.m_dialog = null;
		}

		super.onPause();
	}




	/**************
	 * In case there was an orientation change, we
	 * can retrieve our custom data (from the MySpinners)
	 * here.
	 */
	@Override
	protected void onRestoreInstanceState(Bundle icicle) {
		String custom_name;
		int custom_pos;

		super.onRestoreInstanceState(icicle);

		custom_name = icicle.getString(DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT);
		if (custom_name != null) {
			// Found a weight.  Add it.
			m_exer_weight_msp.add_to_array(custom_name);
			custom_pos = m_exer_weight_msp.length() - 1;
			m_exer_weight_msp.set_selected(custom_pos);
			m_exer_weight_msp.setTextFromPos(custom_pos);
		}

		custom_name = icicle.getString(DatabaseHelper.EXERCISE_COL_DIST_UNIT);
		if (custom_name != null) {
			// Found a distance.  Add it.
			m_exer_dist_msp.add_to_array(custom_name);
			custom_pos = m_exer_dist_msp.length() - 1;
			m_exer_dist_msp.set_selected(custom_pos);
			m_exer_dist_msp.setTextFromPos(custom_pos);
		}

		custom_name = icicle.getString(DatabaseHelper.EXERCISE_COL_TIME_UNIT);
		if (custom_name != null) {
			// Found a time.  Add it.
			m_exer_time_msp.add_to_array(custom_name);
			custom_pos = m_exer_time_msp.length() - 1;
			m_exer_time_msp.set_selected(custom_pos);
			m_exer_time_msp.setTextFromPos(custom_pos);
		}

		// Restore the state of the 'other' EditTexts.
		boolean other = icicle.getBoolean(DatabaseHelper.EXERCISE_COL_OTHER);
		m_exer_other_name_et.setEnabled(other);
		m_exer_other_name_et.setFocusable(other);
//		m_exer_other_name_et.setFocusableInTouchMode(other);
		m_exer_other_unit_et.setEnabled(other);
		m_exer_other_unit_et.setFocusable(other);
//		m_exer_other_unit_et.setFocusableInTouchMode(other);

		// Turn on/off the radio buttons.
		m_exer_rep_rb.setEnabled(m_exer_rep_cb.isChecked());
		m_exer_level_rb.setEnabled(m_exer_level_cb.isChecked());
		m_exer_calorie_rb.setEnabled(m_exer_calorie_cb.isChecked());
		m_exer_weight_rb.setEnabled(m_exer_weight_cb.isChecked());
		m_exer_dist_rb.setEnabled(m_exer_dist_cb.isChecked());
		m_exer_time_rb.setEnabled(m_exer_time_cb.isChecked());
		m_exer_other_rb.setEnabled(m_exer_other_cb.isChecked());

	} // onRestoreInstanceState (icicle)

	/**************
	 * Time to save our custom data in case this is
	 * merely an orientation change.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		String str;

		super.onSaveInstanceState(outState);

		// Save our custom widget info.
		if (m_exer_weight_msp.get_custom_added()) {
			str = m_exer_weight_msp.get_custom_item();
			outState.putString(DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT,
					str);
		}

		if (m_exer_dist_msp.get_custom_added()) {
			str = m_exer_dist_msp.get_custom_item();
			outState.putString(DatabaseHelper.EXERCISE_COL_DIST_UNIT,
					str);
		}

		if (m_exer_time_msp.get_custom_added()) {
			str = m_exer_time_msp.get_custom_item();
			outState.putString(DatabaseHelper.EXERCISE_COL_TIME_UNIT,
					str);
		}

		// Save the active/inactive states of the 'other' EditTexts.
		// This is controlled by its checkbox.
		outState.putBoolean(DatabaseHelper.EXERCISE_COL_OTHER, m_exer_other_cb.isChecked());

	} // onSaveInstanceState (outState)






	/*************
	 * Reads data from the database and fills the widgets
	 * appropriately.
	 *
	 * The first thing is to see if the checkbox for that
	 * widget is true.  If it is, then fill in the other
	 * associated widgets. If NOT, then deactivate those
	 * widgets.
	 *
	 * @return	true - no problems
	 * 			false - problems.
	 */
	private boolean fill_forms() {
		int col;		// used to fill in the views.

		// Get the info from the Intent that GridActivity sent.
		Intent itt = getIntent();
		m_orig_exercise_name = itt.getStringExtra("name");
		if (m_orig_exercise_name == null) {
			Toast.makeText(this, "Problem trying to get the exercise name: " + m_orig_exercise_name + " in fill_forms()", Toast.LENGTH_LONG).show();
			return false;
		}

		Log.v(tag, "fill_forms() received an intent for exercise "
					+ m_orig_exercise_name);

		// Read in that row from the database.
		// Here's the select statement:
		//		select * from exercise_table where _ID = <id>
		m_db = WGlobals.g_db_helper.getReadableDatabase();
		try {
			Cursor cursor =
				m_db.query(
						DatabaseHelper.EXERCISE_TABLE_NAME,	// table
						null,			//	columns[]
						DatabaseHelper.EXERCISE_COL_NAME + "=?",//selection
						new String[] {m_orig_exercise_name},// selectionArgs[]
						null,	//	groupBy
						null,	//	having
						null,	//	orderBy
						null);	//	limit

			// Necessary!  Too bad no one told me that.
			cursor.moveToFirst();

			// Exercise NAME
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_NAME);
				String name = cursor.getString(col);

//				m_exer_name_et.setText(name);
				set_text_lock (m_exer_name_et, name);
			}

			// TYPE MySpinner
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TYPE);
				int type = cursor.getInt(col);
				m_exer_type_msp.set_selected(type);
				m_exer_type_msp.setText (m_exer_type_msp.get_item(type));
			}

			// GROUP spinner
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_GROUP);
				int group = cursor.getInt(col);
				m_exer_group_msp.set_selected(group);
				m_exer_group_msp.setText (m_exer_group_msp.get_item(group));
			}

			// REPS checkbox
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_REP);
				boolean reps = cursor.getInt(col) == 1 ? true : false;
				m_exer_rep_cb.setChecked(reps);
				m_exer_rep_rb.setEnabled(reps);
			}

			// LEVELS checkbox
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LEVEL);
				boolean levels = cursor.getInt(col) == 1 ? true : false;
				m_exer_level_cb.setChecked(levels);
				m_exer_level_rb.setEnabled(levels);
			}

			// CALORIES checkbox
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_CALORIES);
				boolean calories = cursor.getInt(col) == 1 ? true : false;
				m_exer_calorie_cb.setChecked(calories);
				m_exer_calorie_rb.setEnabled(calories);
			}

			// WEIGHTS widgets
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT);
				boolean weight = cursor.getInt(col) == 1 ? true : false;
				m_exer_weight_cb.setChecked(weight);
				m_exer_weight_msp.setEnabled(weight);
				m_exer_weight_rb.setEnabled(weight);
				if (weight) {
					m_exer_weight_msp.setClickable(true);
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT);
					String weight_unit = cursor.getString(col);
					int weight_pos = m_exer_weight_msp.get_pos(weight_unit);
					if (weight_pos == -1) {
						// Not in our array, so it's a custom.  Add it!
						m_exer_weight_msp.add_to_array(weight_unit);
						weight_pos = m_exer_weight_msp.length() - 1;
					}
					m_exer_weight_msp.set_selected(weight_pos);
					m_exer_weight_msp.setTextFromPos(weight_pos);
				}
			}

			// DISTANCE widgets
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST);
				boolean distanced = cursor.getInt(col) == 1 ? true : false;
				m_exer_dist_cb.setChecked(distanced);
				m_exer_dist_msp.setEnabled(distanced);
				m_exer_dist_rb.setEnabled(distanced);
				if (distanced) {
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_DIST_UNIT);
					String dist_unit = cursor.getString(col);
					int dist_pos = m_exer_dist_msp.get_pos(dist_unit);
					if (dist_pos == -1) {
						// Custom distance
						m_exer_dist_msp.add_to_array(dist_unit);
						dist_pos = m_exer_dist_msp.length() - 1;
					}
					m_exer_dist_msp.set_selected(dist_pos);
					m_exer_dist_msp.setTextFromPos(dist_pos);
				}
			}

			// TIME widgets
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME);
				boolean timed = cursor.getInt(col) == 1 ? true : false;
				m_exer_time_cb.setChecked(timed);
				m_exer_time_rb.setEnabled(timed);
				m_exer_time_msp.setEnabled(timed);
				if (timed) {
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_TIME_UNIT);
					String time_unit = cursor.getString(col);
					int time_pos = m_exer_time_msp.get_pos(time_unit);
					if (time_pos == -1) {
						m_exer_time_msp.add_to_array(time_unit);
						time_pos = m_exer_time_msp.length() - 1;
					}
					m_exer_time_msp.set_selected(time_pos);
					m_exer_time_msp.setTextFromPos(time_pos);
				}
			}

			// OTHER widgets
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER);
				boolean other = cursor.getInt(col) == 1 ? true : false;
				m_exer_other_cb.setChecked(other);
				m_exer_other_rb.setEnabled(other);
				m_exer_other_name_et.setEnabled(other);
				m_exer_other_name_et.setFocusable(other);	// Hack to work-around bug in setEnabled() for EditTexts.
				m_exer_other_unit_et.setEnabled(other);
				m_exer_other_unit_et.setFocusable(other);
				if (other) {
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_TITLE);
					String title = cursor.getString(col);
					if (title != "") {
						set_text_lock (m_exer_other_name_et, title);
//						m_exer_other_name_et.setText(title);
					}
					col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_OTHER_UNIT);
					String unit = cursor.getString(col);
					if (unit != "") {
//						m_exer_other_unit_et.setText(unit);
						set_text_lock (m_exer_other_unit_et, unit);
					}
				}
			}

			// SIGNIFICANT radio buttons
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_SIGNIFICANT);
				int sig = cursor.getInt(col);
				set_radio (sig);
			}

			// Lastly, we need to save the lorder of this
			// exercise.  It's needed to update the lorder
			// of the other exercises if this one is
			// deleted.
			// OH YEAH!  I need to put it in when saving, too!
			{
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_LORDER);
				m_lorder = cursor.getInt(col);
			}

			// Clean up.
			cursor.close();
		}
		catch (SQLException e) {
			Log.e(tag, "problem querying the database!");
			e.printStackTrace();
			if (m_db != null) {
				m_db.close();
				m_db = null;
			}
			return false;
		}

		if (m_db != null) {
			m_db.close();
			m_db = null;
		}

		return true;
	} // fill_forms()


	/*************
	 * Saves the data from the widgets into our database.
	 *
	 * preconditions:
	 * 		Everything has been checked and is
	 * 		ready to go.
	 */
	private void save_data() {
		m_db = WGlobals.g_db_helper.getWritableDatabase();

		// First, remove the current row.
		if (m_db.delete(DatabaseHelper.EXERCISE_TABLE_NAME,
				DatabaseHelper.EXERCISE_COL_NAME + "=?",
				new String[] {m_orig_exercise_name})
				== 0) {
			Toast.makeText(this, "Error deleting row in save_data()!", Toast.LENGTH_LONG).show();
			return;
		}

		// Collect the data.
		ContentValues values = new ContentValues();

		String new_name = m_exer_name_et.getText().toString();
		values.put (DatabaseHelper.EXERCISE_COL_NAME, new_name);
		values.put (DatabaseHelper.EXERCISE_COL_TYPE, m_exer_type_msp.get_current_selection());
		values.put (DatabaseHelper.EXERCISE_COL_GROUP, m_exer_group_msp.get_current_selection());
		values.put (DatabaseHelper.EXERCISE_COL_WEIGHT, m_exer_weight_cb.isChecked());
		values.put (DatabaseHelper.EXERCISE_COL_REP, m_exer_rep_cb.isChecked());
		values.put (DatabaseHelper.EXERCISE_COL_DIST, m_exer_dist_cb.isChecked());
		values.put (DatabaseHelper.EXERCISE_COL_TIME, m_exer_time_cb.isChecked());
		values.put (DatabaseHelper.EXERCISE_COL_LEVEL, m_exer_level_cb.isChecked());
		values.put (DatabaseHelper.EXERCISE_COL_CALORIES, m_exer_calorie_cb.isChecked());
		values.put (DatabaseHelper.EXERCISE_COL_OTHER, m_exer_other_cb.isChecked());

		String unit = "";
		if (m_exer_weight_cb.isChecked())
			unit = m_exer_weight_msp.getText().toString();
		values.put (DatabaseHelper.EXERCISE_COL_WEIGHT_UNIT, unit);

		unit = "";
		if (m_exer_dist_cb.isChecked())
			unit = m_exer_dist_msp.getText().toString();
		values.put (DatabaseHelper.EXERCISE_COL_DIST_UNIT, unit);

		unit = "";
		if (m_exer_time_cb.isChecked())
			unit = m_exer_time_msp.getText().toString();
		values.put (DatabaseHelper.EXERCISE_COL_TIME_UNIT, unit);

		unit = "";
		String name = "";
		if (m_exer_other_cb.isChecked()) {
			name = m_exer_other_name_et.getText().toString();
			unit = m_exer_other_unit_et.getText().toString();
		}
		values.put (DatabaseHelper.EXERCISE_COL_OTHER_TITLE, name);
		values.put (DatabaseHelper.EXERCISE_COL_OTHER_UNIT, unit);

		values.put(DatabaseHelper.EXERCISE_COL_SIGNIFICANT, get_radio());

		values.put(DatabaseHelper.EXERCISE_COL_LORDER, m_lorder);

		m_db.insert(DatabaseHelper.EXERCISE_TABLE_NAME, null, values);


		// Now that the exercise is redone, there's a problem:
		// what if the exercise is renamed?  All the workout
		// sets using this name no longer refer to this exercise.
		// Time to fix this.

		if (!m_orig_exercise_name.equals(new_name)) {
			fix_name (m_db, new_name);
		}

		m_db.close();
	} // save_data()


	/*************
	 * Changes the name portion of all the exercise sets that
	 * refer to this exercise.  Since the name has been changed,
	 * they now have the wrong name in their name column.  This
	 * fixes that.
	 *
	 * preconditions:
	 * 		m_orig_exercise_name holds the old name (the same
	 * 			that's in the SET table.
	 *
	 * 		new_name is different from m_orig_exercise_name
	 *
	 * @param db			A writable database, ready to roll.
	 *
	 * @param new_name	The name to change all these columns
	 * 					to.
	 */
	private void fix_name (SQLiteDatabase db, String new_name) {
		ContentValues values = new ContentValues();

		values.put(DatabaseHelper.SET_COL_NAME, new_name);

		int count = db.update(DatabaseHelper.SET_TABLE_NAME,
				values,
				DatabaseHelper.SET_COL_NAME + "='" + m_orig_exercise_name + "'",
				null);
		Log.i (tag, "fix_name() renamed " + count + " rows in the SET table.");
	} // fix_name (db, new_name)



	/*************
	 * Deletes the current entry from the database. That's
	 * all (everything has been okayed and has been or will
	 * be handled).
	 *
	 * preconditions:
	 * 		m_orig_exercise_name		is properly set.
	 */
	private void delete_entry() {
		int col, id;

		m_db = null;
		try {
			m_db = WGlobals.g_db_helper.getWritableDatabase();

			// Remove the current row.
			if (m_db.delete(DatabaseHelper.EXERCISE_TABLE_NAME,
					DatabaseHelper.EXERCISE_COL_NAME + "=?",
					new String[] {m_orig_exercise_name})
					== 0) {
				Log.e(tag, "Error deleting row in delete_entry()!  m_orig_exercise_name = " + m_orig_exercise_name);
				Toast.makeText(this, "Error deleting row in delete_entry()!", Toast.LENGTH_LONG).show();
				return;
			}

			Cursor cursor = null;
			try {
				cursor = m_db.query(
						DatabaseHelper.EXERCISE_TABLE_NAME,	// table
						new String[] {DatabaseHelper.COL_ID, DatabaseHelper.EXERCISE_COL_NAME, DatabaseHelper.EXERCISE_COL_LORDER},
						DatabaseHelper.EXERCISE_COL_LORDER + " > " + m_lorder,//selection
						null,// selectionArgs[]
						null,	//	groupBy
						null,	//	having
						DatabaseHelper.EXERCISE_COL_LORDER,	//	orderBy
						null);

				int num_rows = cursor.getCount();

				// Now all the other rows need to have their order
				// updated.
				for (int i = 0; i < num_rows; i++) {
					// Change the lorder of this row
					cursor.moveToNext();

					col = cursor.getColumnIndex(DatabaseHelper.COL_ID);
					id = cursor.getInt(col);

					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.EXERCISE_COL_LORDER, i + m_lorder);
					m_db.update(DatabaseHelper.EXERCISE_TABLE_NAME,
							values,
							DatabaseHelper.COL_ID + " = " + id,
							null);
				}
			} // try new cursor - EXERCISE_TABLE
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}

			// Now delete all the entries in the SET table
			// that use this exercise.
			int num = m_db.delete(DatabaseHelper.SET_TABLE_NAME,
					DatabaseHelper.SET_COL_NAME + "='" + m_orig_exercise_name + "'",
					null);
			Log.i(tag, "delete_entry() removed " + num + "set entries when deleting the " + m_orig_exercise_name + " exercise.");

		} // try new db

		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			if (m_db != null) {
				m_db.close();
				m_db = null;
			}
		}

	}  // delete_entry()


	/************
	 * Searches through an array defined in arrays.xml to
	 * see if the given string matches (not case sensitive).
	 *
	 * @param array_id	The ID of the array in the XML file.
	 *
	 * @param str		The string to search for.
	 *
	 * @return	Returns the element of the array that
	 * 			matched, or -1 if not found.
	 */
	int find_array_match (int array_id, String str) {
		String[] array = getResources().getStringArray(array_id);

		for (int i = 0; i < array.length; i++) {
			if (array[i].equalsIgnoreCase(str)) {
				return i;
			}
		}

		return -1;
	} // find_array_match (array_id, str)

	/*********************
	 * Sets the radio buttons.  This means it turns ON the
	 * given button and turns OFF all the others.
	 *
	 * @param which		The radio button to turn on.  They are
	 * 					numbered according the numbers in
	 * 					DatabaseHelper.
	 * 					-1 means NONE should be on.
	 */
	private void set_radio (int which) {
		// First, turn all of them off.  Yeah, it's kind of
		// hacky, but will always work and doesn't need
		// another variable to track.
		m_exer_rep_rb.setChecked(false);
		m_exer_level_rb.setChecked(false);
		m_exer_calorie_rb.setChecked(false);
		m_exer_weight_rb.setChecked(false);
		m_exer_dist_rb.setChecked(false);
		m_exer_time_rb.setChecked(false);
		m_exer_other_rb.setChecked(false);

		switch (which) {
			case DatabaseHelper.EXERCISE_COL_REP_NUM:
				m_exer_rep_rb.setChecked(true);
				break;
			case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
				m_exer_level_rb.setChecked(true);
				break;
			case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
				m_exer_calorie_rb.setChecked(true);
				break;
			case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
				m_exer_weight_rb.setChecked(true);
				break;
			case DatabaseHelper.EXERCISE_COL_DIST_NUM:
				m_exer_dist_rb.setChecked(true);
				break;
			case DatabaseHelper.EXERCISE_COL_TIME_NUM:
				m_exer_time_rb.setChecked(true);
				break;
			case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
				m_exer_other_rb.setChecked(true);
				break;

			default:
				Toast.makeText(this, "ILLEGAL column in set_radio (" + which + ")", Toast.LENGTH_LONG).show();
				break;
		}
	} // set_radio (which)


	/****************
	 * Returns which of the radio buttons is on.
	 * If none of 'em, then it returns -1.
	 *
	 * Assumes that no more than one radio button is on.
	 *
	 * @return	The radio button that's on.  This is
	 * 			based on the numbers defined in DatabaseHelper.
	 * 			Or -1.
	 */
	private int get_radio() {
		if (m_exer_rep_rb.isChecked())
			return DatabaseHelper.EXERCISE_COL_REP_NUM;
		else if (m_exer_level_rb.isChecked())
			return DatabaseHelper.EXERCISE_COL_LEVEL_NUM;
		else if (m_exer_calorie_rb.isChecked())
			return DatabaseHelper.EXERCISE_COL_CALORIE_NUM;
		else if (m_exer_weight_rb.isChecked())
			return DatabaseHelper.EXERCISE_COL_WEIGHT_NUM;
		else if (m_exer_dist_rb.isChecked())
			return DatabaseHelper.EXERCISE_COL_DIST_NUM;
		else if (m_exer_time_rb.isChecked())
			return DatabaseHelper.EXERCISE_COL_TIME_NUM;
		else if (m_exer_other_rb.isChecked())
			return DatabaseHelper.EXERCISE_COL_OTHER_NUM;

		return -1;
	} // get_radio()


	/****************
	 * The title says it all.  Just checks to see
	 * if all the radio buttons are off.
	 *
	 * @return	TRUE iff all radio buttons are off.
	 */
	private boolean is_all_radio_buttons_off() {
		if (!m_exer_rep_rb.isChecked() &&
			!m_exer_level_rb.isChecked() &&
			!m_exer_calorie_rb.isChecked() &&
			!m_exer_weight_rb.isChecked() &&
			!m_exer_dist_rb.isChecked() &&
			!m_exer_time_rb.isChecked() &&
			!m_exer_other_rb.isChecked()) {
			return true;
		}
		return false;
	} // is_all_radio_buttons_off()



	/*********************
	 * Only intercepting the back key.  It's just to make sure
	 * the user hasn't accidentally hit it when making changes
	 * while the page is still dirty.
	 *
	 * @param keyCode
	 * @param event
	 * @return	True - that this method completely handled
	 * 			the event.
	 * 			False - Let other handlers have a crack at this.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (m_dirty && WGlobals.g_nag && (keyCode == KeyEvent.KEYCODE_BACK)) {
			show_yes_no_dialog(R.string.editexer_cancel_warning_title, null,
					R.string.editexer_cancel_warning_msg, null,
					new View.OnClickListener() {
						public void onClick(View v) {
							// Yes, they want to cancel.
							setResult(RESULT_CANCELED);
							dismiss_all_dialogs();
							finish();
						}
				});
		return true;		// We handled this event!
		}

		return super.onKeyDown(keyCode, event);
	} // onKeyDown (keyCode, event)


	/*********************
	 * Handles all the click events.
	 */
	public void onClick(View v) {

		// Is it a radio button?
		if (v.getClass() == RadioButton.class) {
			m_dirty = true;
			if (v == m_exer_rep_rb)
				set_radio (DatabaseHelper.EXERCISE_COL_REP_NUM);

			else if (v == m_exer_level_rb)
				set_radio (DatabaseHelper.EXERCISE_COL_LEVEL_NUM);

			else if (v == m_exer_calorie_rb)
				set_radio (DatabaseHelper.EXERCISE_COL_CALORIE_NUM);

			else if (v == m_exer_weight_rb)
				set_radio (DatabaseHelper.EXERCISE_COL_WEIGHT_NUM);

			else if (v == m_exer_dist_rb)
				set_radio (DatabaseHelper.EXERCISE_COL_DIST_NUM);

			else if (v == m_exer_time_rb)
				set_radio (DatabaseHelper.EXERCISE_COL_TIME_NUM);

			else if (v == m_exer_other_rb)
				set_radio (DatabaseHelper.EXERCISE_COL_OTHER_NUM);
		} // if radio button

		// Is it a check button?
		else if (v.getClass() == CheckBox.class) {
			m_dirty = true;
			if (v == m_exer_rep_cb)
				set_rep_check (v);

			else if (v == m_exer_level_cb)
				set_level_check (v);

			else if (v == m_exer_calorie_cb)
				set_calorie_check (v);

			else if (v == m_exer_weight_cb)
				set_weight_check (v);

			else if (v == m_exer_dist_cb)
				set_dist_check (v);

			else if (v == m_exer_time_cb)
				set_time_check (v);

			else if (v == m_exer_other_cb)
				set_other_check (v);

		} // if checkbox

		// The main buttons at the bottom.
		if (v == m_ok) {
			if (!m_dirty) {		// Didn't do anything!
				setResult(RESULT_CANCELED);
				finish();
			}
			if (check_good_exercise(true) == false) {
				return;
			}
			save_data();		// YEAH!!!!!
			setResult(RESULT_OK);
			finish();
		}

		else if (v == m_delete) {
			//	a dialog and then delete!!!
			show_yes_no_dialog (R.string.editexer_delete_confirm_title,
								new String[] {m_orig_exercise_name},
								R.string.editexer_delete_confirm_msg,
								new String[] {m_orig_exercise_name},
								new View.OnClickListener() {

				public void onClick(View v) {
					// This happens ONLY if they said YES!
					delete_entry();
					setResult(RESULT_OK);
					dismiss_all_dialogs();
					finish();
				}
				});
		} // if delete an entry

	} // onClick (v)


	/*********************
	 * Long clicks are used for specific help on specific
	 * widgets.
	 *
	 * We return true so that the OS won't try to do anything
	 * after the long-click.
	 */
	public boolean onLongClick(View v) {

		// All the Radio Buttons have the same help.
		if (v.getClass() == RadioButton.class) {
			show_help_dialog(R.string.addexer_radio_help_title,
					R.string.addexer_radio_help_msg);
		}

		else if (v.getClass() == CheckBox.class) {
			if (v == m_exer_rep_cb)
//				showHelpDialog(R.string.addexer_check_help_title,
//						R.string.addexer_check_help_msg);
				show_help_dialog(R.string.addexer_rep_help_title,
						R.string.addexer_rep_help_msg);
			if (v == m_exer_level_cb)
				show_help_dialog(R.string.addexer_level_help_title,
						R.string.addexer_level_help_msg);
			if (v == m_exer_calorie_cb)
				show_help_dialog(R.string.addexer_calorie_help_title,
						R.string.addexer_calorie_help_msg);
			if (v == m_exer_weight_cb)
				show_help_dialog(R.string.addexer_weight_help_title,
						R.string.addexer_weight_help_msg);
			if (v == m_exer_dist_cb)
				show_help_dialog(R.string.addexer_dist_help_title,
						R.string.addexer_dist_help_msg);
			if (v == m_exer_time_cb)
				show_help_dialog(R.string.addexer_time_help_title,
						R.string.addexer_time_help_msg);
			if (v == m_exer_other_cb)
				show_help_dialog(R.string.addexer_other_help_title,
						R.string.addexer_other_help_msg);
		} // checkbox

		// Let's do the MySpinners...
		else if (v.getClass() == MySpinner.class) {
			if (v == m_exer_type_msp)
				show_help_dialog(R.string.addexer_type_help_title,
						R.string.addexer_type_help_msg);
			else if (v == m_exer_group_msp)
				show_help_dialog(R.string.addexer_musclegroup_help_title,
						R.string.addexer_musclegroup_help_msg);
			else if (v == m_exer_weight_msp)
				show_help_dialog(R.string.addexer_weight_unit_title,
						R.string.addexer_unit_msg);
			else if (v == m_exer_dist_msp)
				show_help_dialog(R.string.addexer_dist_unit_title,
						R.string.addexer_unit_msg);
			else if (v == m_exer_time_msp)
				show_help_dialog(R.string.addexer_time_unit_title,
						R.string.addexer_unit_msg);

		} // MySpinners

		else if (v == m_exer_other_name_et)
			show_help_dialog(R.string.add_other_name_et_help_title,
					R.string.add_other_name_et_help_msg);
		else if (v == m_exer_other_unit_et)
			show_help_dialog(R.string.add_other_unit_et_help_title,
					R.string.add_other_unit_et_help_msg);

		// The buttons at the bottom
		else if (v == m_ok)
			show_help_dialog(R.string.editexer_ok_title,
					R.string.editexer_ok_msg);

		else if (v == m_delete)
			show_help_dialog(R.string.editexer_delete_help_title,
					R.string.editexer_delete_help_msg);

		return true;
	}


	/*********************
	 * This is called whenever the user selects an
	 * item from a MySpinner.
	 */
	public void onMySpinnerSelected(MySpinner spinner, int position, boolean new_item) {
		m_dirty = true;
		spinner.setTextFromPos (position);
		spinner.set_selected(position);
	} // onMySpinnerSelected (spinner, position, newItem)


	/*********************
	 * Called when the user has created a custom name for one
	 * of the parts of this exercise.  I gotta tell the MySpinner
	 * what happened, so it can put the data in correctly.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED) {
			return;
		}

		if (data == null) {
			Toast.makeText(this, "onActivityResult(), but data == null!!  (requestCode = " + requestCode + ", resultCode = " + resultCode + ")", Toast.LENGTH_LONG).show();
			Log.e (tag, "onActivityResult(), but data == null!!  (requestCode = " + requestCode + ", resultCode = " + resultCode + ")");
			return;
		}

		// The did something.  Note it.
		m_dirty = true;

		// Figure out which MySpinner sent this and return it to 'em.
		if (requestCode == m_exer_weight_msp.getId()) {
			m_exer_weight_msp.activity_result(resultCode, data);
		}
		else if (requestCode == m_exer_dist_msp.getId()) {
			m_exer_dist_msp.activity_result(resultCode, data);
		}
		else if (requestCode == m_exer_time_msp.getId()) {
			m_exer_time_msp.activity_result(resultCode, data);
		}

	} // onActivityResult (requestCode, resultCode, data)


	//--------------------------------------
	//	To implement the TextWatcher interface.
	//--------------------------------------
	public void afterTextChanged(Editable s) {
		// not used
	}
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// not used
	}
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (!m_et_locked) {
		m_dirty = true;
		}
	}

	/********************
	 * Replaces the standard EditText.setText(str) method for this
	 * program.
	 *
	 * This provides a lock so that the dirty bit is not set when
	 * the program changes the EditTexts.
	 *
	 * @param et		The EditText to add text to.
	 *
	 * @param str	The string to add.
	 */
	private void set_text_lock (EditText et, String str) {
		m_et_locked = true;
		et.setText(str);
		m_et_locked = false;
	}



	/********************
	 * This takes care of when the checkbox for the
	 * REPETITION exercise is checked.  This is called
	 * as soon as the box is checked.
	 *
	 * side effects:
	 * 		All the REPETITION widgets may change.
	 *
	 * @param v		The View (which represents the REPETITION
	 * 				checkbox).  It may be cast as a CheckBox.
	 */
	private void set_rep_check (View v) {
		boolean on = ((CheckBox) v).isChecked();
		m_exer_rep_rb.setEnabled(on);
		m_exer_rep_rb.setFocusable(on);
//		m_exer_rep_rb.setFocusableInTouchMode(on);
		if (!on) {
			// The box has been turned off.  Turn off
			// the significant radio button.
			m_exer_rep_rb.setChecked(false);
		}
		else if (is_all_radio_buttons_off()) {
			// This is the first button to be turned on.
			// Set it as significant by default.
			m_exer_rep_rb.setChecked(true);
		}
	} // set_rep_check (v)

	private void set_level_check (View v) {
		boolean on = ((CheckBox) v).isChecked();
		m_exer_level_rb.setEnabled(on);
		m_exer_level_rb.setFocusable(on);
//		m_exer_level_rb.setFocusableInTouchMode(on);
		if (!on) {
			m_exer_level_rb.setChecked(false);
		}
		else if (is_all_radio_buttons_off()) {
			m_exer_level_rb.setChecked(true);
		}
	}

	private void set_calorie_check (View v) {
		boolean on = ((CheckBox) v).isChecked();
		m_exer_calorie_rb.setEnabled(on);
		m_exer_calorie_rb.setFocusable(on);
//		m_exer_calorie_rb.setFocusableInTouchMode(on);
		if (!on) {
			m_exer_calorie_rb.setChecked(false);
		}
		else if (is_all_radio_buttons_off()) {
			m_exer_calorie_rb.setChecked(true);
		}
	}

	private void set_weight_check (View v) {
		boolean on = ((CheckBox) v).isChecked();
		m_exer_weight_rb.setEnabled(on);
		m_exer_weight_msp.setEnabled(on);
		m_exer_weight_rb.setFocusable(on);
//		m_exer_weight_rb.setFocusableInTouchMode(on);
		m_exer_weight_msp.setFocusable(on);
//		m_exer_weight_msp.setFocusableInTouchMode(on);
		if (!on) {
			m_exer_weight_rb.setChecked(false);
		}
		else if (is_all_radio_buttons_off()) {
			m_exer_weight_rb.setChecked(true);
		}
	}

	private void set_dist_check (View v) {
		boolean on = ((CheckBox) v).isChecked();
		m_exer_dist_rb.setEnabled(on);
		m_exer_dist_msp.setEnabled(on);
		m_exer_dist_rb.setFocusable(on);
//		m_exer_dist_rb.setFocusableInTouchMode(on);
		m_exer_dist_msp.setFocusable(on);
//		m_exer_dist_msp.setFocusableInTouchMode(on);
		if (!on) {
			m_exer_dist_rb.setChecked(false);
		}
		else if (is_all_radio_buttons_off()) {
			m_exer_dist_rb.setChecked(true);
		}
	}

	private void set_time_check (View v) {
		boolean on = ((CheckBox) v).isChecked();
		m_exer_time_rb.setEnabled(on);
		m_exer_time_msp.setEnabled(on);
		m_exer_time_rb.setFocusable(on);
//		m_exer_time_rb.setFocusableInTouchMode(on);
		m_exer_time_msp.setFocusable(on);
//		m_exer_time_msp.setFocusableInTouchMode(on);
		if (!on) {
			m_exer_time_rb.setChecked(false);
		}
		else if (is_all_radio_buttons_off()) {
			m_exer_time_rb.setChecked(true);
		}
	}

	private void set_other_check (View v) {
		boolean on = ((CheckBox) v).isChecked();
		m_exer_other_rb.setEnabled(on);
		if (!on) {
			m_exer_other_rb.setChecked(false);
		}
		else if (is_all_radio_buttons_off()) {
			m_exer_other_rb.setChecked(true);
		}

		// EditTexts are tricky...
		m_exer_other_name_et.setEnabled(on);
		m_exer_other_name_et.setClickable(on);
		m_exer_other_name_et.setFocusable(on);
//		m_exer_other_name_et.setFocusableInTouchMode(on);
		m_exer_other_unit_et.setEnabled(on);
		m_exer_other_unit_et.setClickable(on);
		m_exer_other_unit_et.setFocusable(on);
//		m_exer_other_unit_et.setFocusableInTouchMode(on);
	}

	/****************
	 * This tests all the widgets to make sure that
	 * the exercise has valid settings.
	 *
	 * Valid exercises need:
	 * 		= A name.
	 * 		= At least one measurement checked.
	 * 		= A significant measurement selected.
	 * 		= If 'other' is selected, then the fields
	 * 			should be filled in.
	 *
	 * @param	warn		When TRUE, send warnings to
	 * 					the user through Toasts about
	 * 					what they need to do to fix
	 * 					the exercise.
	 *
	 * @return	TRUE iff this is a valid exercise.
	 */
	private boolean check_good_exercise (boolean warn) {
		String msg;

		// Is there a name?
		Editable name = m_exer_name_et.getText();
		if (name.length() == 0) {
			if (warn) {
				msg = getString(R.string.editexer_warning_no_name);
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
			return false;
		}

		// Make sure they selected a type and group.
		int selection = m_exer_type_msp.get_current_selection();
		if (selection < 0) {
			if (warn) {
				msg = getString(R.string.addexer_type_complaint);
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
			return false;
		}
		selection = m_exer_group_msp.get_current_selection();
		if (selection < 0) {
			if (warn) {
				msg = getString(R.string.addexer_musclegroup_complaint);
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
			return false;
		}

		// Is there at least one checkbox that's on.
		if (!m_exer_rep_cb.isChecked() &&
			!m_exer_level_cb.isChecked() &&
			!m_exer_calorie_cb.isChecked() &&
			!m_exer_weight_cb.isChecked() &&
			!m_exer_dist_cb.isChecked() &&
			!m_exer_time_cb.isChecked() &&
			!m_exer_other_cb.isChecked()) {
			if (warn) {
				msg = getString(R.string.editexer_warning_no_checked);
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
			return false;
		}

		// 'Other' fields
		if (m_exer_other_cb.isChecked()) {
			if ((m_exer_other_name_et.length() == 0) ||
				(m_exer_other_unit_et.length() == 0)) {
				if (warn) {
					msg = getString (R.string.addexer_other_complaint);
					Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
				}
				return false;
			}
		}

		// If Weighted, must be a unit.
		if (m_exer_weight_cb.isChecked() &&
			(m_exer_weight_msp.get_current_selection() == -1)) {
			if (warn) {
				msg = getString (R.string.editexer_warning_no_weight_unit);
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
			return false;
		}

		// Same for distanced
		if (m_exer_dist_cb.isChecked() &&
			(m_exer_dist_msp.get_current_selection() == -1)) {
			if (warn) {
				msg = getString (R.string.editexer_warning_no_dist_unit);
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
			return false;
		}

		// And timed.
		if (m_exer_time_cb.isChecked() &&
			(m_exer_time_msp.get_current_selection() == -1)) {
			if (warn) {
				msg = getString (R.string.editexer_warning_no_time_unit);
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
			return false;
		}

		// Gotta be something significant...
		if (is_all_radio_buttons_off()) {
			if (warn) {
				msg = getString(R.string.editexer_warning_no_significant);
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
			return false;
		}

		return true;
	} // check_good_exercise (warn)

}
