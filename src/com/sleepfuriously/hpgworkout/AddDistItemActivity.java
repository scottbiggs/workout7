/**
 * This Activity pops up when the user wants to make a custom
 * distance.  It's just a copy of AddExercieWeightItemActivity.
 */
package com.sleepfuriously.hpgworkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddDistItemActivity extends BaseDialogActivity
						implements OnClickListener,
								   OnLongClickListener {

//	private static final String tag = "AddDistItemActivity";

	EditText m_dist_et;
	Button m_ok, m_help, m_cancel;


	//--------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_dist_item);

		m_dist_et = (EditText) findViewById(R.id.add_dist_item_et);
		m_dist_et.setOnLongClickListener(this);

		m_ok = (Button) findViewById(R.id.add_dist_item_ok_butt);
		m_ok.setOnClickListener(this);

		m_cancel = (Button) findViewById(R.id.add_dist_item_cancel_butt);
		m_cancel.setOnClickListener(this);

		m_help = (Button) findViewById(R.id.add_dist_item_help_butt);
		m_help.setOnClickListener(this);

	} // onCreate (.)


	//---------------------------------
	public void onClick(View v) {

		if (v == m_ok) {		// Clicked the ok button. Save and exit.
			Intent itt = getIntent();
			itt.putExtra(MySpinner.INTENT_KEY, m_dist_et.getText());
			setResult(RESULT_OK, itt);
			finish();
		}

		else if (v == m_cancel) {
			setResult(RESULT_CANCELED);
			finish();
		}

		else if (v == m_help) {
			show_help_dialog(R.string.add_dist_item_help_title,
							 R.string.add_dist_item_help_msg);
		}
	} // onClick(v)


	/*****************
	 *
	 * @param v
	 * @return
	 */
	public boolean onLongClick(View v) {
		if (v == m_dist_et) {
			show_help_dialog(R.string.add_dist_item_et_help_title,
						  R.string.add_dist_item_et_help_msg);
			return true;
		}
		return false;
	} // onLongClick (v)

}
