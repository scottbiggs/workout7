/**
 * This class pops up from the GraphActivity when the user
 * wants to change how the graphs look (adding or removing
 * graphs or even combining some of them...maybe?).
 *
 * Input:
 *	- It needs to know the name of the exercise so it can
 *	correctly load up and modify the exercise definition.
 *	Use ITT_KEY_EXERCISE_NAME as the key.
 *
 *	- A boolean needs to be set for all the valid aspects
 *	for this exercise.  Use ITT_KEY_ASPECT_ prefixes.
 *
 *	- And another boolean needs to be set for all the
 *	aspects that are currently graphed.  ITT_KEY_GRAPH_
 *	is the prefix to use for these.
 *
 *	- And lastly, an int needs to be set to determine
 *	what goes with reps (or -1 if none).  The key is
 *	ITT_KEY_WITH_REPS.
 *
 * Output:
 * 	- Status OK means that things have changed.
 *
 *	- An Intent will be sent with information about
 *	how to graph this class (a bunch of booleans and
 *	an int).  They'll use the ITT_KEY_GRAPH_ prefix
 *	and of course, the ITT_KEY_WITH_REPS for the int.
 *
 * CANCEL means no change.
 */
package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class GraphOptionsActivity
					extends BaseDialogActivity
					implements
						OnMySpinnerListener,
						OnClickListener,
						OnLongClickListener {

	//--------------------
	//	Constants
	//--------------------

	private static final String tag = "GraphOptionsActivity";

	/**
	 * See the class dox for complete description.
	 * <p>
	 * This is the key used to tell this Activity the
	 * name of the exercise to modify.
	 * <p>
	 * The value will be a string.
	 */
	public static final String ITT_KEY_EXERCISE_NAME = "name_key";

	/**
	 * These are the keys that tell this Activity
	 * which aspects of this exercise are valid.
	 * <p>
	 * The corresponding values are booleans.
	 */
	public static final String
			ITT_KEY_GRAPH_REPS = "key_graph_reps",
			ITT_KEY_GRAPH_LEVEL = "key_graph_level",
			ITT_KEY_GRAPH_CALS = "key_graph_cals",
			ITT_KEY_GRAPH_WEIGHT = "key_graph_weight",
			ITT_KEY_GRAPH_DIST = "key_graph_dist",
			ITT_KEY_GRAPH_TIME = "key_graph_time",
			ITT_KEY_GRAPH_OTHER = "key_graph_other";

	/**
	 * The key for the name of the other exercise aspect.
	 */
	public static final String
			ITT_KEY_GRAPH_OTHER_NAME = "key_graph_other_name";

	/**
	 * These keys have double duty.  When the Intent
	 * starts this Activity, these tell the Activity
	 * which aspects are graphed and which are not.
	 * <p>
	 * When used in onActivityForResult(), this informs
	 * the calling Activity which aspects were selected
	 * by the user to display.  Thus this Activity doesn't
	 * have to deal with any database stuff.  Yay!
	 * <p>
	 * All these keys are for boolean values.
	 */
	public static final String
			ITT_KEY_ASPECT_REPS = "key_aspect_reps",
			ITT_KEY_ASPECT_LEVEL = "key_aspect_level",
			ITT_KEY_ASPECT_CALS = "key_aspect_cals",
			ITT_KEY_ASPECT_WEIGHT = "key_aspect_weight",
			ITT_KEY_ASPECT_DIST = "key_aspect_dist",
			ITT_KEY_ASPECT_TIME = "key_aspect_time",
			ITT_KEY_ASPECT_OTHER = "key_aspect_other";

	/**
	 * Tells whatever Activity that's looking at this
	 * value which aspect to combine with Reps for a
	 * special combo graph.  Yeah, that means that this
	 * value goes both ways.
	 * <p>
	 * The value is an int.  -1 means NOT USED.
	 */
	public static final String ITT_KEY_WITH_REPS = "key_with_reps";


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
	private MySpinner m_combine_with_reps_myspin;

	/** The basic buttons */
	Button m_cancel, m_done;

	ImageView m_help;


	//--------------------
	//	Class Data
	//--------------------

	/**
	 * This is an array of aspects that the user may combine
	 * with the REPS aspect.  It's a list of CharSequences
	 * that's also used to make the display MySpinner.  This means
	 * that this and the MySpinner are EXACTLY the same order.
	 * Thus this can be used to see which item the user selected.
	 *
	 * Note:
	 * 		The first item (numbered 0) will ALWAYS be "none".
	 */
	ArrayList <CharSequence> m_with_reps_list = new ArrayList<CharSequence>();

	/**
	 * This goes hand-in-hand with m_with_reps_list.  It holds the
	 * aspect number (defined in DatabaseHelper) that corresponds
	 * to the aspects in m_with_reps_list.
	 *
	 * Note:
	 * 		The first item is always the 'none' selection (-1).
	 */
	ArrayList<Integer> m_with_reps_list_ref = new ArrayList<Integer>();

	/** Has the user made any changes? */
	boolean m_dirty = false;


	//--------------------
	//	Methods
	//--------------------


	//***********************
	/* (non-Javadoc)
	 * @see com.sleepfuriously.hpgworkout.BaseDialogActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.graph_options);

		m_cancel = (Button) findViewById(R.id.graph_options_cancel_butt);
		m_help = (ImageView) findViewById(R.id.graph_options_logo);
		m_done = (Button) findViewById(R.id.graph_options_ok_butt);

		m_cancel.setOnClickListener(this);
		m_help.setOnClickListener(this);
		m_done.setOnClickListener(this);

		m_reps_cb = (CheckBox) findViewById(R.id.graph_options_reps_check);
		m_reps_cb.setOnClickListener(this);
		m_reps_cb.setChecked(false);

		m_weight_cb = (CheckBox) findViewById(R.id.graph_options_weight_check);
		m_weight_cb.setOnClickListener(this);
		m_weight_cb.setChecked(false);

		m_cals_cb = (CheckBox) findViewById(R.id.graph_options_cals_check);
		m_cals_cb.setOnClickListener(this);
		m_cals_cb.setChecked(false);

		m_level_cb = (CheckBox) findViewById(R.id.graph_options_level_check);
		m_level_cb.setOnClickListener(this);
		m_level_cb.setChecked(false);

		m_dist_cb = (CheckBox) findViewById(R.id.graph_options_dist_check);
		m_dist_cb.setOnClickListener(this);
		m_dist_cb.setChecked(false);

		m_time_cb = (CheckBox) findViewById(R.id.graph_options_time_check);
		m_time_cb.setOnClickListener(this);
		m_time_cb.setChecked(false);

		m_other_cb = (CheckBox) findViewById(R.id.graph_options_other_check);
		m_other_cb.setOnClickListener(this);
		m_other_cb.setChecked(false);

		m_combine_with_reps_myspin = (MySpinner) findViewById(R.id.graph_options_with_msp);
		m_combine_with_reps_myspin.setOnLongClickListener(this);
		m_combine_with_reps_myspin.setMySpinnerListener(this);

		// For our With Reps lists, start with the first item always
		// indicating 'not used'.
		m_with_reps_list.add(getString(R.string.graph_options_with_no_selection));
		m_with_reps_list_ref.add(-1);

		// Read our info and set the view appropriately.
		read_intent();

	} // onCreate(.)


	//***********************
	/* (non-Javadoc)
	 * @see com.sleepfuriously.hpgworkout.BaseDialogActivity#onPause()
	 */
	@Override
	protected void onPause() {
		if ((MySpinner.m_dialog != null) && (MySpinner.m_dialog.isShowing())) {
			MySpinner.m_dialog.dismiss();
			MySpinner.m_dialog = null;
		}

		super.onPause();
	}


	//***********************
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (v == m_done) {
			if (!m_dirty) {	// didn't do anything (this shouldn't be possible!)
				Log.e(tag, "Clicked 'done' without anything being dirty!  How'd they do that?");
				setResult(RESULT_CANCELED);
				finish();
				return;
			}

			// Fill in the Intent and return the caller.
			Intent itt = new Intent();

			itt.putExtra(ITT_KEY_GRAPH_REPS, m_reps_cb.isChecked());
			itt.putExtra(ITT_KEY_GRAPH_LEVEL, m_level_cb.isChecked());
			itt.putExtra(ITT_KEY_GRAPH_CALS, m_cals_cb.isChecked());
			itt.putExtra(ITT_KEY_GRAPH_WEIGHT, m_weight_cb.isChecked());
			itt.putExtra(ITT_KEY_GRAPH_DIST, m_dist_cb.isChecked());
			itt.putExtra(ITT_KEY_GRAPH_TIME, m_time_cb.isChecked());
			itt.putExtra(ITT_KEY_GRAPH_OTHER, m_other_cb.isChecked());

			int selection = m_combine_with_reps_myspin.get_current_selection();
			if (selection == -1) {
				itt.putExtra(ITT_KEY_WITH_REPS, -1);
			}
			else {
				itt.putExtra(ITT_KEY_WITH_REPS, m_with_reps_list_ref.get(selection));
			}

			setResult(RESULT_OK, itt);
			finish();
		} // done

		else if (v == m_help) {
			show_help_dialog(R.string.graph_options_help_title,
							R.string.graph_options_help_msg);
		}

		else if (v == m_cancel) {
			setResult(RESULT_CANCELED);
			finish();
		}

		else {
			// If we got down to here, then they clicked on
			// checkbox
			m_dirty = true;
			m_done.setEnabled(true);
		}
	} // onClick(v)


	//***********************
	/* (non-Javadoc)
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		if (v == m_combine_with_reps_myspin) {
			show_help_dialog(R.string.graph_options_with_help_title,
							R.string.graph_options_with_help_msg);
			return true;
		}

		// todo
		//	MORE help!

		return false;
	} // onLongClick (v)


	//***********************
	@Override
	public void onMySpinnerSelected(MySpinner spinner, int position,
									boolean new_item) {
		m_dirty = true;
		m_done.setEnabled(true);

		m_combine_with_reps_myspin.set_selected(position);
		m_combine_with_reps_myspin.setText(m_with_reps_list.get(position));
	} // onMySpinnerSelected (...)


	/***********************
	 * Working by side-effect, this loads up
	 * the Intent sent to this Activity and sets
	 * all our data members appropriately.
	 */
	private void read_intent() {
		Intent itt = getIntent();
		View bar = null, last_bar = null;

		//
		//	NOTE:
		//		The ORDER of these aspects MUST match
		//		how they appear in the layout file!!!!
		//		Or baaaaad things can happen.
		//

		bar = findViewById(R.id.graph_options_reps_line);
		if (itt.getBooleanExtra(ITT_KEY_ASPECT_REPS, false) == false) {
			// Turn off the checkbox and the bar
			m_reps_cb.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		else {
			// Should we turn the checkbox on?
			m_reps_cb.setChecked(itt.getBooleanExtra(ITT_KEY_GRAPH_REPS, false));

			// Note that we don't add REPS to the "with reps" stuff!
			last_bar = bar;
		}

		bar = findViewById(R.id.graph_options_level_line);
		if (itt.getBooleanExtra(ITT_KEY_ASPECT_LEVEL, false) == false) {
			m_level_cb.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		else {
			m_level_cb.setChecked(itt.getBooleanExtra(ITT_KEY_GRAPH_LEVEL, false));

			m_with_reps_list.add(getString(R.string.addexer_level_label));
			m_with_reps_list_ref.add(DatabaseHelper.EXERCISE_COL_LEVEL_NUM);
			last_bar = bar;
		}

		bar = findViewById(R.id.graph_options_cals_line);
		if (itt.getBooleanExtra(ITT_KEY_ASPECT_CALS, false) == false) {
			m_cals_cb.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		else {
			m_cals_cb.setChecked(itt.getBooleanExtra(ITT_KEY_GRAPH_CALS, false));

			m_with_reps_list.add(getString(R.string.addexer_calorie_label));
			m_with_reps_list_ref.add(DatabaseHelper.EXERCISE_COL_CALORIE_NUM);
			last_bar = bar;
		}

		bar = findViewById(R.id.graph_options_weight_line);
		if (itt.getBooleanExtra(ITT_KEY_ASPECT_WEIGHT, false) == false) {
			m_weight_cb.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		else {
			m_weight_cb.setChecked(itt.getBooleanExtra(ITT_KEY_GRAPH_WEIGHT, false));

			m_with_reps_list.add(getString(R.string.addexer_weight_label));
			m_with_reps_list_ref.add(DatabaseHelper.EXERCISE_COL_WEIGHT_NUM);
			last_bar = bar;
		}

		bar = findViewById(R.id.graph_options_dist_line);
		if (itt.getBooleanExtra(ITT_KEY_ASPECT_DIST, false) == false) {
			m_dist_cb.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		else {
			m_dist_cb.setChecked(itt.getBooleanExtra(ITT_KEY_GRAPH_DIST, false));

			m_with_reps_list.add(getString(R.string.addexer_dist_label));
			m_with_reps_list_ref.add(DatabaseHelper.EXERCISE_COL_DIST_NUM);
			last_bar = bar;
		}

		bar = findViewById(R.id.graph_options_time_line);
		if (itt.getBooleanExtra(ITT_KEY_ASPECT_TIME, false) == false) {
			m_time_cb.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		else {
			m_time_cb.setChecked(itt.getBooleanExtra(ITT_KEY_GRAPH_TIME, false));

			m_with_reps_list.add(getString(R.string.addexer_time_label));
			m_with_reps_list_ref.add(DatabaseHelper.EXERCISE_COL_TIME_NUM);
			last_bar = bar;
		}

		bar = findViewById(R.id.graph_options_other_line);
		if (itt.getBooleanExtra(ITT_KEY_ASPECT_OTHER, false) == false) {
			m_other_cb.setVisibility(View.GONE);
			bar.setVisibility(View.GONE);
		}
		else {
			m_other_cb.setChecked(itt.getBooleanExtra(ITT_KEY_GRAPH_OTHER, false));

			// For the Other, we need to know the unit, which is in the intent.
			m_with_reps_list.add(itt.getStringExtra(ITT_KEY_GRAPH_OTHER_NAME));
			m_with_reps_list_ref.add(DatabaseHelper.EXERCISE_COL_OTHER_NUM);
			m_other_cb.setText(itt.getStringExtra(ITT_KEY_GRAPH_OTHER_NAME));
			last_bar = bar;
		}


		// Set the current selection of the MySpinner.
		// We're interested in where an exercise has Reps AND
		// another aspect.
		if (itt.getBooleanExtra(ITT_KEY_ASPECT_REPS, false) &&
			((itt.getBooleanExtra(ITT_KEY_ASPECT_LEVEL, false)) ||
			(itt.getBooleanExtra(ITT_KEY_ASPECT_CALS, false)) ||
			(itt.getBooleanExtra(ITT_KEY_ASPECT_WEIGHT, false)) ||
			(itt.getBooleanExtra(ITT_KEY_ASPECT_DIST, false)) ||
			(itt.getBooleanExtra(ITT_KEY_ASPECT_TIME, false)) ||
			(itt.getBooleanExtra(ITT_KEY_ASPECT_OTHER, false)))) {

			m_combine_with_reps_myspin.set_prompt(R.string.graph_options_with_prompt);


			// Display the current setting.
			int with = itt.getIntExtra(ITT_KEY_WITH_REPS, -1);
			String current_with_str = new String();
			switch (with) {
				case DatabaseHelper.EXERCISE_COL_REP_NUM:
					current_with_str = getString(R.string.addexer_rep_label);
					Log.e(tag, "Problem in read_intent(): can't combine reps with reps!");
					break;
				case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
					current_with_str = getString(R.string.addexer_level_label);
					break;
				case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
					current_with_str = getString(R.string.addexer_calorie_label);
					break;
				case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
					current_with_str = getString(R.string.addexer_weight_label);
					break;
				case DatabaseHelper.EXERCISE_COL_DIST_NUM:
					current_with_str = getString(R.string.addexer_dist_label);
					break;
				case DatabaseHelper.EXERCISE_COL_TIME_NUM:
					current_with_str = getString(R.string.addexer_time_label);
					break;
				case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
					current_with_str = itt.getStringExtra(ITT_KEY_GRAPH_OTHER_NAME);
					break;

				case -1:		// Don't do anything, use the default.
					break;

				default:
					Log.e (tag, "Illegal ITT_KEY_WITH_REPS value of " + with + " in read_intent()!");
					break;
			}
			m_combine_with_reps_myspin.setText(current_with_str);

			// Fill in the list
			m_combine_with_reps_myspin.set_array(m_with_reps_list);

			// Now turn on the right item in the list.
			m_combine_with_reps_myspin.set_selected(m_with_reps_list_ref.indexOf(with));
		}
		else {
			// Remove the whole LinearLayout with the MySpinner
			LinearLayout ll = (LinearLayout) findViewById(R.id.graph_options_with_ll);
			ll.setVisibility(View.GONE);
			if (last_bar != null) {
				last_bar.setVisibility(View.GONE);
			}
			else {
				Log.e (tag, "Illegal logic in read_intent! No bar was found to remove.");
			}
		}


	} // read_intent()

}
