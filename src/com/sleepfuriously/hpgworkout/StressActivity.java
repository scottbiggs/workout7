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
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;


public class StressActivity
			extends
				BaseDialogActivity
			implements
				OnCheckedChangeListener,
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
	Button m_cancel, m_help, m_done;

	/** The main thing! */
	RadioGroup m_stress_rg;


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
		m_help = (Button) findViewById(R.id.enter_stress_help_butt);
		m_done = (Button) findViewById(R.id.enter_stress_ok_butt);

		m_cancel.setOnClickListener(this);
		m_help.setOnClickListener(this);
		m_done.setOnClickListener(this);

		m_stress_rg = (RadioGroup) findViewById(R.id.enter_stress_rg);
		m_stress_rg.setOnCheckedChangeListener(this);

		// If there's an old value, then get it. Also
		// set the TextView appropriately.
		Intent itt = getIntent();
		if (itt.getBooleanExtra(ITT_KEY_SHOW_OLD_BOOL, false)) {
			m_stress_num = itt.getIntExtra(ITT_KEY_OLD_STRESS,
					DatabaseHelper.SET_COND_NONE);
			switch (m_stress_num) {
				case DatabaseHelper.SET_COND_OK:
					m_stress_rg.check(R.id.enter_stress_radio0);
					break;
				case DatabaseHelper.SET_COND_PLUS:
					m_stress_rg.check(R.id.enter_stress_radio1);
					break;
				case DatabaseHelper.SET_COND_MINUS:
					m_stress_rg.check(R.id.enter_stress_radio2);
					break;
				case DatabaseHelper.SET_COND_INJURY:
					m_stress_rg.check(R.id.enter_stress_radio3);
					break;
				default:
					m_stress_rg.check(-1);
					break;
			}
		}
		else {
			// No value, clear the radiogroup.
			m_stress_rg.check(-1);
		}

	} // onCreate (.)


	//----------------------------
	public void onClick(View v) {
		if (v == m_done) {
			if (!m_dirty) {	// didn't do anything
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			Intent itt = new Intent();
			int checked = m_stress_rg.getCheckedRadioButtonId();
			switch (checked) {
				case R.id.enter_stress_radio0:
					itt.putExtra(ITT_KEY_RETURN_STRESS, DatabaseHelper.SET_COND_OK);
					setResult(RESULT_OK, itt);
					break;
				case R.id.enter_stress_radio1:
					itt.putExtra(ITT_KEY_RETURN_STRESS, DatabaseHelper.SET_COND_PLUS);
					setResult(RESULT_OK, itt);
					break;
				case R.id.enter_stress_radio2:
					itt.putExtra(ITT_KEY_RETURN_STRESS, DatabaseHelper.SET_COND_MINUS);
					setResult(RESULT_OK, itt);
					break;
				case R.id.enter_stress_radio3:
					itt.putExtra(ITT_KEY_RETURN_STRESS, DatabaseHelper.SET_COND_INJURY);
					setResult(RESULT_OK, itt);
					break;
				default:			// they didn't do anything (sort of an error condition)
					Log.w(tag, "Unusual case in onClick(). checked = "+ checked);
					setResult(RESULT_CANCELED);
					finish();
					return;
			}
			finish();
		} // done

		else if (v == m_help) {
			show_help_dialog(R.string.enter_stress_help_title,
					R.string.enter_stress_help_msg);
		}

		else if (v == m_cancel) {
			setResult(RESULT_CANCELED);
			finish();
		}

	} // onClick (v)


	//----------------------------
	// Not much to do here as it's handled automatically.
	//
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		m_dirty = true;
	}

}
