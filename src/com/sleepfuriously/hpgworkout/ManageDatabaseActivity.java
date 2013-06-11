package com.sleepfuriously.hpgworkout;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

/**
 * This Activity allows the user to manage their various databases.
 * It is not accessible in the free version.
 */
public class ManageDatabaseActivity extends BaseDialogActivity
									implements
										OnClickListener,
										OnLongClickListener {

	/* (non-Javadoc)
	 * @see com.sleepfuriously.hpgworkout.BaseDialogActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_database);


	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}



}
