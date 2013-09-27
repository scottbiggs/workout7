/**
 * This Activity pops up when the user wants to make a custom
 * distance.  It's just a copy of AddExercieWeightItemActivity.
 */
package com.sleepfuriously.hpgworkout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class AddDistItemActivity extends BaseDialogActivity
						implements OnClickListener,
								TextWatcher,
								OnLongClickListener {

//	private static final String tag = "AddDistItemActivity";

	EditText m_dist_et;
	Button m_ok, m_clear;
	ImageView m_help;


	//--------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_dist_item);

		m_dist_et = (EditText) findViewById(R.id.add_dist_item_et);
		m_dist_et.setOnLongClickListener(this);
		m_dist_et.addTextChangedListener(this);

		m_ok = (Button) findViewById(R.id.add_dist_item_ok_butt);
		m_ok.setOnClickListener(this);

		m_clear = (Button) findViewById(R.id.add_dist_item_clear_butt);
		m_clear.setOnClickListener(this);

		m_help = (ImageView) findViewById(R.id.add_dist_item_logo);
		m_help.setOnClickListener(this);

	} // onCreate (.)


	//---------------------------------
	@Override
	public void onClick(View v) {

		if (v == m_ok) {		// Clicked the ok button. Save and exit.
			WGlobals.play_short_click();
			Intent itt = getIntent();
			itt.putExtra(MySpinner.INTENT_KEY, m_dist_et.getText());
			setResult(RESULT_OK, itt);
			finish();
		}

		else if (v == m_clear) {
			WGlobals.play_short_click();
			clear();
		}

		else if (v == m_help) {
			WGlobals.play_help_click();
			show_help_dialog(R.string.add_dist_item_help_title,
							R.string.add_dist_item_help_msg);
		}
	} // onClick(v)


	//----------------------------------
	@Override
	public boolean onLongClick(View v) {
		WGlobals.play_long_click();
		if (v == m_dist_et) {
			show_help_dialog(R.string.add_dist_item_et_help_title,
						R.string.add_dist_item_et_help_msg);
			return true;
		}
		return false;
	} // onLongClick (v)


	/**************************
	 * Clears the EditText and disables the buttons.
	 */
	protected void clear() {
		m_dist_et.setText("");
		m_clear.setEnabled(false);
		m_ok.setEnabled(false);
	}


	//----------------------------------
	@Override
	public void afterTextChanged(Editable s) {
		Editable curr_str = m_dist_et.getText();
		if ((curr_str == null) || (curr_str.length() == 0)) {
			m_clear.setEnabled(false);
			m_ok.setEnabled(false);
		}
		else {
			m_clear.setEnabled(true);
			m_ok.setEnabled(true);
		}
	}

	//----------------------------------
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
									int after) {
	}

	//----------------------------------
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

}
