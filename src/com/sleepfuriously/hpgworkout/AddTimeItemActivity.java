/**
 * This Activity pops up when the user wants to make a custom
 * time.  It's just a copy of AddExercieWeightItemActivity.
 */
package com.sleepfuriously.hpgworkout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class AddTimeItemActivity extends BaseDialogActivity
						implements OnClickListener,
								TextWatcher,
								OnLongClickListener {

	private static final String tag = "AddTimeItemActivity";

	EditText m_time_et;
	Button m_ok, m_clear;
	ImageView m_help;


	/******************
	 *
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_time_item);

		m_time_et = (EditText) findViewById(R.id.add_time_item_et);
		m_time_et.setOnLongClickListener(this);
		m_time_et.addTextChangedListener(this);
		
		m_ok = (Button) findViewById(R.id.add_time_item_ok_butt);
		m_ok.setOnClickListener(this);

		m_clear = (Button) findViewById(R.id.add_time_item_clear_butt);
		m_clear.setOnClickListener(this);

		m_help = (ImageView) findViewById(R.id.add_time_item_logo);
		m_help.setOnClickListener(this);

//		Log.v(tag, "Exiting onCreate()!");
	}

	/******************
	 *
	 */
	@Override
	public void onClick(View v) {

		if (v == m_ok) {		// Clicked the ok button. Save and exit.
			Intent itt = getIntent();
			itt.putExtra(MySpinner.INTENT_KEY, m_time_et.getText());
			setResult(RESULT_OK, itt);
			finish();
		}

		else if (v == m_clear) {
			clear();
		}

		else if (v == m_help) {
			show_help_dialog(R.string.add_time_item_help_title,
						R.string.add_time_item_help_msg);
		}
	} // onClick(v)


	//-----------------------------
	@Override
	public boolean onLongClick(View v) {
		if (v == m_time_et) {
			show_help_dialog(R.string.add_time_item_et_help_title,
						R.string.add_time_item_et_help_msg);
			return true;
		}
		return false;
	} // onLongClick (v)


	/**************************
	 * Clears the EditText and disables the buttons.
	 */
	protected void clear() {
		m_time_et.setText("");
		m_clear.setEnabled(false);
		m_ok.setEnabled(false);
	}

	//-----------------------------
	@Override
	public void afterTextChanged(Editable s) {
		Editable curr_str = m_time_et.getText();
		if ((curr_str == null) || (curr_str.length() == 0)) {
//			Log.d(tag, "afterTextChanged(" + s + "), disabling the buttons!");
			m_clear.setEnabled(false);
			m_ok.setEnabled(false);
		}
		else {
//			Log.d(tag, "afterTextChanged(" + s + "), enabling the buttons.");
			m_clear.setEnabled(true);
			m_ok.setEnabled(true);
		}
	}

	//-----------------------------
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
									int after) {
	}

	//-----------------------------
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}


}
