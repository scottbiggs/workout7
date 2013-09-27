/**
 * This is an Activity that looks like a dialog.  It's
 * used to allow the user to change the stress level
 * of a workout set.
 *
 * It's a copy of TextActivity.
 *
 * Call this Activity via startActivityForResult().  But
 * first, you gotta set the Intent...
 * 		1.	If there's an old value, supply the boolean
 * 			that indicates YES (true).
 * 		2.	If you did do step 1, supply that number
 * 			(you get it from DatabaseHelper.SET_COND_x).
 *
 *
 * NEXT, when this returns, in your onActivityResult(),
 * check the data Intent.  It'll hold the value if the
 * returnCode is RESULT_OK.  Otherwise, there may not
 * even be any value (the user aborted, hit "back", etc.).
 */
package com.sleepfuriously.hpgworkout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TableRow;


public class StressActivity
			extends
				BaseDialogActivity
			implements
				View.OnClickListener {

	//----------------------------
	//	Constants
	//----------------------------

	/**
	 * INPUT
	 * 		boolean
	 *
	 * The key for the boolean that indicates there will
	 * be another item in the Intent that holds the old
	 * stress value.  If you don't want to show it
	 * set this to FALSE).
	 */
	public static final String ITT_KEY_SHOW_OLD_BOOL = "old_bool";

	/**
	 * INPUT
	 * 		int
	 *
	 * Only used if the above is TRUE.  This holds the int
	 * to display as an old stress value that the user is
	 * replacing.
	 *
	 * The values are defined:  DatabaseHelper.SET_COND_
	 */
	public static final String ITT_KEY_OLD_STRESS = "old_stress";

	/**
	 * OUTPUT
	 * 		int
	 *
	 * The key for the RETURN VALUE of this intent.
	 */
	public static final String ITT_KEY_RETURN_STRESS = "new_string";


	private static final String tag = "StressActivity";


	//----------------------------
	//	Widgets
	//----------------------------
	Button m_cancel, m_done;
	ImageView m_help;

	/** The rows that the user can click on */
	TableRow m_ok_table, m_too_easy_table,
			m_too_hard_table, m_injury_table;

	/** The radio buttons that show the user's selection */
	RadioButton m_ok_rb, m_easy_rb,
				m_hard_rb, m_injury_rb;

	//----------------------------
	//	Data
	//----------------------------

	/** actual current stress number */
	private int m_stress_num = DatabaseHelper.SET_COND_NONE;

	/** Has the user made any changes? */
	boolean m_dirty = false;


	//----------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.enter_stress);

		m_cancel = (Button) findViewById(R.id.enter_stress_cancel_butt);
		m_help = (ImageView) findViewById(R.id.enter_stress_logo);
		m_done = (Button) findViewById(R.id.enter_stress_ok_butt);

		m_cancel.setOnClickListener(this);
		m_help.setOnClickListener(this);
		m_done.setOnClickListener(this);


		m_ok_table = (TableRow) findViewById(R.id.enter_stress_table_row_ok);
		m_ok_table.setOnClickListener(this);
		m_too_easy_table = (TableRow) findViewById(R.id.enter_stress_table_row_too_easy);
		m_too_easy_table.setOnClickListener(this);
		m_too_hard_table = (TableRow) findViewById(R.id.enter_stress_table_row_too_hard);
		m_too_hard_table.setOnClickListener(this);
		m_injury_table = (TableRow) findViewById(R.id.enter_stress_table_row_injury);
		m_injury_table.setOnClickListener(this);

		m_ok_rb = (RadioButton) findViewById(R.id.enter_stress_ok_rb);
		m_easy_rb = (RadioButton) findViewById(R.id.enter_stress_too_easy_rb);
		m_hard_rb = (RadioButton) findViewById(R.id.enter_stress_too_hard_rb);
		m_injury_rb = (RadioButton) findViewById(R.id.enter_stress_injury_rb);

		// If there's an old value, then get it. Also
		// set the TextView appropriately.
		Intent itt = getIntent();
		if (itt.getBooleanExtra(ITT_KEY_SHOW_OLD_BOOL, false)) {
			m_stress_num = itt.getIntExtra(ITT_KEY_OLD_STRESS,
					DatabaseHelper.SET_COND_NONE);
			set_radio_buttons (m_stress_num);
		}
		else {
			// No value, clear the radiogroup.
			set_radio_buttons(DatabaseHelper.SET_COND_NONE);
		}

	} // onCreate (.)


	//----------------------------
	@Override
	public void onClick(View v) {

		if (v == m_ok_table) {
			WGlobals.play_short_click();
			// Only do something if this is a new button.
			if (m_stress_num != DatabaseHelper.SET_COND_OK) {
				m_stress_num = DatabaseHelper.SET_COND_OK;
				set_radio_buttons(m_stress_num);
				m_dirty = true;
				m_done.setEnabled(true);
			}
		}

		if (v == m_too_easy_table) {
			WGlobals.play_short_click();
			if (m_stress_num != DatabaseHelper.SET_COND_PLUS) {
				m_stress_num = DatabaseHelper.SET_COND_PLUS;
				set_radio_buttons(m_stress_num);
				m_dirty = true;
				m_done.setEnabled(true);
			}
		}

		if (v == m_too_hard_table) {
			WGlobals.play_short_click();
			// Only do something if this is a new button.
			if (m_stress_num != DatabaseHelper.SET_COND_MINUS) {
				m_stress_num = DatabaseHelper.SET_COND_MINUS;
				set_radio_buttons(m_stress_num);
				m_dirty = true;
				m_done.setEnabled(true);
			}
		}
		if (v == m_injury_table) {
			WGlobals.play_short_click();
			// Only do something if this is a new button.
			if (m_stress_num != DatabaseHelper.SET_COND_INJURY) {
				m_stress_num = DatabaseHelper.SET_COND_INJURY;
				set_radio_buttons(m_stress_num);
				m_dirty = true;
				m_done.setEnabled(true);
			}
		}

		else if (v == m_done) {
			WGlobals.play_short_click();
			if (!m_dirty) {	// didn't do anything
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			Intent itt = new Intent();
			itt.putExtra(ITT_KEY_RETURN_STRESS, m_stress_num);
			setResult(RESULT_OK, itt);
			finish();
		} // done

		else if (v == m_help) {
			WGlobals.play_help_click();
			show_help_dialog(R.string.enter_stress_help_title,
					R.string.enter_stress_help_msg);
		}

		else if (v == m_cancel) {
			WGlobals.play_short_click();
			setResult(RESULT_CANCELED);
			finish();
		}

	} // onClick (v)


	/*********************
	 * Sets the radio buttons based on the given number.
	 * If the number is out of range, then all the buttons
	 * are turned off.
	 *
	 * NOTE:  This only does the UI stuff--does NOT keep
	 * 	track of any internal data!!!  No side effects!
	 *
	 * preconditions:
	 * 		All the widgets are set up and ready to go.
	 *
	 * @param button_num		The number of the radio
	 * 						button to pick.  Defined
	 * 						by the constants in DatabaseHelper.
	 */
	protected void set_radio_buttons (int button_num) {
		m_ok_rb.setChecked(false	);
		m_easy_rb.setChecked(false);
		m_hard_rb.setChecked(false);
		m_injury_rb.setChecked(false);

		switch (button_num) {
			case DatabaseHelper.SET_COND_OK:
				m_ok_rb.setChecked(true);
				break;
			case DatabaseHelper.SET_COND_PLUS:
				m_easy_rb.setChecked(true);
				break;
			case DatabaseHelper.SET_COND_MINUS:
				m_hard_rb.setChecked(true);
				break;
			case DatabaseHelper.SET_COND_INJURY:
				m_injury_rb.setChecked(true);
				break;
		}
	} // set_radio_buttons (button_num)

}
