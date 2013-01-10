/**
 * This class pops up from the GraphActivity when the user
 * wants to change how the graphs look (adding or removing
 * graphs or even combining some of them...maybe?).
 *
 * 	Input:
 * It needs to know the name of the exercise so it can
 * correctly load up and modify the exercise definition.
 *
 *	Output:
 * Consequently, if it returns the status OK, that means
 * that things have changed, and the caller *really* needs
 * to reload their stuff!  CANCEL means no change.
 */
package com.sleepfuriously.hpgworkout;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;

public class GraphOptionsActivity
					extends BaseDialogActivity
					implements
						OnClickListener,
						OnLongClickListener {

	//--------------------
	//	Widgets
	//--------------------

	/** The simple buttons for the aspects */
	private CheckBox m_reps_cb, m_weight_cb, m_level_cb,
		m_cals_cb, m_dist_cb, m_time_cb,
		m_other_cb;

	/**
	 * When pressed, this combines reps with another
	 * aspect (selected by the user).
	 */
	private MySpinner m_combine_with_reps;


	//--------------------
	//	Class Data
	//--------------------

	/** Holds all the info about this exercise */
	private ExerciseData m_ex_data;


	//--------------------
	//	Methods
	//--------------------


	/* (non-Javadoc)
	 * @see com.sleepfuriously.hpgworkout.BaseDialogActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
	}


	/* (non-Javadoc)
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		return false;
	}


}
