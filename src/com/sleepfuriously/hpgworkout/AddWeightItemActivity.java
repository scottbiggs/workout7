/**
 * This Activity pops up when the user wants to make a custom
 * weight.  It's pretty simple as far as display, but creates
 * some interesting callbacks to make sure all the data is
 * passed around correctly.
 */
package com.sleepfuriously.hpgworkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddWeightItemActivity extends BaseDialogActivity
						implements OnClickListener,
								   OnLongClickListener {

//	private static final String tag = "AddWeightItemActivity";

	EditText m_weight_et;
	Button m_ok, m_help, m_cancel;


	//------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_weight_item);

		m_weight_et = (EditText) findViewById(R.id.add_weight_item_et);
		m_weight_et.setOnLongClickListener(this);

		m_ok = (Button) findViewById(R.id.add_weight_item_ok_butt);
		m_ok.setOnClickListener(this);

		m_cancel = (Button) findViewById(R.id.add_weight_item_cancel_butt);
		m_cancel.setOnClickListener(this);

		m_help = (Button) findViewById(R.id.add_weight_item_help_butt);
		m_help.setOnClickListener(this);

//		Log.v(tag, "Exiting onCreate()!");
	}

	//------------------------------
	public void onClick(View v) {

		if (v == m_ok) {		// Clicked the ok button. Save and exit.
			Intent itt = getIntent();
			itt.putExtra(MySpinner.INTENT_KEY, m_weight_et.getText());
			setResult(RESULT_OK, itt);
			finish();
		}

		else if (v == m_cancel) {
			setResult(RESULT_CANCELED);
			finish();
		}

		else if (v == m_help) {
			show_help_dialog(R.string.add_weight_item_help_title, R.string.add_weight_item_help_msg);
		}
	} // onClick(v)


	//------------------------------
	public boolean onLongClick(View v) {
		if (v == m_weight_et) {
			show_help_dialog(R.string.add_weight_item_et_help_title,
						  R.string.add_weight_item_et_help_msg);
			return true;
		}
		return false;
	} // onLongClick (v)

}
