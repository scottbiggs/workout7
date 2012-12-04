/**
 * This Activity pops up when the user wants to make a custom
 * weight.  It's pretty simple as far as display, but creates
 * some interesting callbacks to make sure all the data is
 * passed around correctly.
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

public class AddWeightItemActivity extends BaseDialogActivity
						implements OnClickListener,
								TextWatcher,
								OnLongClickListener {

	private static final String tag = "AddWeightItemActivity";

	EditText m_weight_et;
	Button m_ok, m_clear;
	ImageView m_help;


	//------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_weight_item);

		m_weight_et = (EditText) findViewById(R.id.add_weight_item_et);
		m_weight_et.setOnLongClickListener(this);
		m_weight_et.addTextChangedListener(this);

		m_ok = (Button) findViewById(R.id.add_weight_item_ok_butt);
		m_ok.setOnClickListener(this);

		m_clear = (Button) findViewById(R.id.add_weight_item_clear_butt);
		m_clear.setOnClickListener(this);

		m_help = (ImageView) findViewById(R.id.add_weight_item_logo);
		m_help.setOnClickListener(this);

//		Log.v(tag, "Exiting onCreate()!");
	}

	//------------------------------
	@Override
	public void onClick(View v) {

		if (v == m_ok) {		// Clicked the ok button. Save and exit.
			Intent itt = getIntent();
			itt.putExtra(MySpinner.INTENT_KEY, m_weight_et.getText());
			setResult(RESULT_OK, itt);
			finish();
		}

		else if (v == m_clear) {
			clear();
		}

		else if (v == m_help) {
			show_help_dialog(R.string.add_weight_item_help_title, R.string.add_weight_item_help_msg);
		}
	} // onClick(v)


	//------------------------------
	@Override
	public boolean onLongClick(View v) {
		if (v == m_weight_et) {
			show_help_dialog(R.string.add_weight_item_et_help_title,
						R.string.add_weight_item_et_help_msg);
			return true;
		}
		return false;
	} // onLongClick (v)


	/**************************
	 * Clears the EditText and disables the buttons.
	 */
	protected void clear() {
		m_weight_et.setText("");
		m_clear.setEnabled(false);
		m_ok.setEnabled(false);
	}


	//-----------------------------
	@Override
	public void afterTextChanged(Editable arg0) {
		Editable curr_str = m_weight_et.getText();
		if ((curr_str == null) || (curr_str.length() == 0)) {
			m_clear.setEnabled(false);
			m_ok.setEnabled(false);
		}
		else {
			m_clear.setEnabled(true);
			m_ok.setEnabled(true);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
									int arg3) {
	}

	@Override
	public void onTextChanged(CharSequence str, int arg1, int arg2, int arg3) {
	}
}
