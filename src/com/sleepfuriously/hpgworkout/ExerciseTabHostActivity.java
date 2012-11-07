/**
 * This is the Activity that pops up when the user selects an
 * exercise or a cell in the grid.  It displays a tabbed Activity.
 * Which tab starts depends on how the user got here.
 */
package com.sleepfuriously.hpgworkout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class ExerciseTabHostActivity
					extends
						TabActivity
					implements
						OnClickListener,
						OnTabChangeListener {

	//---------------------
	//	Constants
	//---------------------

	private static final String tag = "ExerciseTabHostActivity";

	/**
	 * When the Grid calls this Activity, we need to know which
	 * tab (Activity) to display first.  Here's the key for that
	 * number.
	 *
	 * See the next group of INTs for what to pair
	 * with this key.
	 */
	public static final String TAB_START_KEY = "tab_start";

	/**
	 * The numbers designate the tabs to display first. These
	 * are the numbers you accompany the TAB_START_KEY with.
	 */
	public static final int
//		TAB_ASET = 0,			// History is no longer used.
//		TAB_INSPECTOR = 1,
//		TAB_HISTORY = 2,
//		TAB_GRAPH = 3,
//		TAB_EDIT = 4;
		TAB_ASET = 0,
		TAB_INSPECTOR = 1,
		TAB_GRAPH = 2,
		TAB_EDIT = 3;

	/** The key to access the name of this exercise */
	public static final String KEY_NAME = "name";

	/** The key to access the date a specific exercise set. */
	public static final String KEY_SET_ID = "set_id";

	/**
	 * Used to pass data from this Activity to whichever
	 * Activity called it via the Intent.
	 */
	public static final String RETURN_ID_KEY = "return_id";

	//---------------------
	//	Widgets
	//---------------------

	/** The Help dialog for all the tabs. */
	protected Dialog m_dialog = null;

	/**
	 * The button (or Image) that's pushed for help for ALL the
	 * different Activity screens.
	 */
	protected ImageView m_help;

	//---------------------
	//	Data
	//---------------------

	/** The name of the exercise we're looking at. */
	String m_ex_name;

	/** The _ID of the item that was originally clicked.  -1 if n/a. */
	int m_id = -1;

	/** The day that we're looking at.  Null if n/a. */
//	MyCalendar m_date = null;;

	/**
	 * Accessible outside of this class (particularly for the classes
	 * in the tabs!).  The tab Activities use this to tell if they
	 * need to reload or not.
	 */
	public static boolean m_dirty = false;

	/**
	 * This is used by the Activities within this tab to see if they
	 * are part of a tab, or being called natively.
	 */
	public static boolean m_tab_active = false;


	//--------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(tag, "entering onCreate()");
		m_tab_active = true;
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE | Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.exercise_tabhost);

		m_dirty = false;		// Reset when creating.

		m_help = (ImageView) findViewById(R.id.exercise_tabhost_logo);
		m_help.setOnClickListener(this);

		// Load up the data about the exercise we're looking at.
		Intent itt = getIntent();
		m_ex_name = itt.getStringExtra(KEY_NAME);
		m_id = itt.getIntExtra(KEY_SET_ID, -1);

		// Which is the first?
		int start = itt.getIntExtra(TAB_START_KEY, -1);
		if (start == -1) {
			Log.e(tag, "Trying to start an unknown tab!");
			return;
		}

		// Name this exercise.
		TextView tv = (TextView) findViewById(R.id.exercise_tabhost_name_tv);
		tv.setText(m_ex_name);

		//------------
		//	Do the Tabs
		//------------

		Log.v(tag, "\tbuilding tabs...");
		Resources res = getResources();	// Used to get Drawables (icons, etc.)
		TabHost th = getTabHost();	// Gets the TabHost from this Activity
		TabSpec spec;	// reusable for each tab

		// Create an Intent to launch the first tab. (ASet = 0)
		// NOTE 1:
		//		It seems that this Activity's onCreate is called immediately.
		//		From my tests, the first Activity is always called as
		//		it is defined.
		//
		{
			Intent aset_itt = new Intent(this, ASetActivity.class);
			aset_itt.putExtra(KEY_NAME, m_ex_name);
			aset_itt.putExtra(KEY_SET_ID, m_id);
			spec = th.newTabSpec("ASetActivity");// No one knows what this is for
			spec.setContent(aset_itt);
			spec.setIndicator(getString(R.string.exer_tabhost_tab_aset),
					res.getDrawable(R.drawable.wheel));
			th.addTab(spec);
		}
		Log.v(tag, "\t\tASet complete...");

		// Now the second tab (Inspector = 1)
		// NOTE:
		//		Need to create a NEW Intent--can't reuse the other one
		//		as that'll create a lot of confusion.
		{
			Intent inspector_itt = new Intent(this, InspectorActivity2.class);
			inspector_itt.putExtra(KEY_NAME, m_ex_name);
			inspector_itt.putExtra(KEY_SET_ID, m_id);
			spec = th.newTabSpec("InspectorActivity2");// No one knows what this is for
			spec.setContent(inspector_itt);
			spec.setIndicator(getString(R.string.exer_tabhost_tab_inspector),
					res.getDrawable(R.drawable.user));
			th.addTab(spec);
		}
		Log.v(tag, "\t\tInspector complete...");

//		// (History = 2)
//		{
//			Intent hist_itt = new Intent(this, HistoryActivity.class);
//			hist_itt.putExtra(KEY_NAME, m_ex_name);
//			hist_itt.putExtra(KEY_SET_ID, m_id);
//			spec = th.newTabSpec("HistoryActivity");// No one knows what this is for
//			spec.setContent(hist_itt);
//			spec.setIndicator(getString(R.string.exer_tabhost_tab_history),
//					res.getDrawable(R.drawable.users));
//			th.addTab(spec);
//		}
//		Log.v(tag, "\t\tHistory complete...");

		// (Graph = 2)
		{
			Intent graph_itt = new Intent(this, GraphActivity.class);
			graph_itt.putExtra(KEY_NAME, m_ex_name);
			graph_itt.putExtra(KEY_SET_ID, m_id);
			spec = th.newTabSpec("GraphActivity");// No one knows what this is for
			spec.setContent(graph_itt);
			spec.setIndicator(getString(R.string.exer_tabhost_tab_graph),
					res.getDrawable(R.drawable.web));
			th.addTab(spec);
		}
		Log.v(tag, "\t\tGraph complete...");

		// (EditExercise = 3)
		{
			Intent edit_itt = new Intent(this, EditExerciseActivity.class);
			edit_itt.putExtra(KEY_NAME, m_ex_name);
			edit_itt.putExtra(KEY_SET_ID, m_id);
			spec = th.newTabSpec("EditExerciseActivity");// No one knows what this is for
			spec.setContent(edit_itt);
			spec.setIndicator(getString(R.string.exer_tabhost_tab_edit_exercise),
					res.getDrawable(R.drawable.writingpad));
			th.addTab(spec);
		}
		Log.v(tag, "\t\tEdit complete...");

		Log.v(tag, "\t\tAll tabs built.");

		// Finally set the start tab.
		th.setCurrentTab(start);

		Log.v(tag, "\tCurrent tab set.");

		th.setOnTabChangedListener(this);


		Log.v(tag, "onCreate done.");
	} // onCreate(.)


	//--------------------------------
	@Override
	protected void onDestroy() {
		Log.v(tag, "onDestroy()");
		m_tab_active = false;
		super.onDestroy();
	}


	//--------------------------------
	@Override
	protected void onPause() {
		Log.v(tag, "onPause()");
		super.onPause();
	}


	//--------------------------------
	@Override
	protected void onStop() {
		Log.v(tag, "onStop()");
		super.onStop();
	}


	//--------------------------------
	public void onClick(View v) {
		if (v == m_help) {
			int title = -1, msg = -1;

			// Which tab is showing?
			TabHost th = getTabHost();	// Gets the TabHost from this Activity
			switch (th.getCurrentTab()) {
				case TAB_ASET:
					title = R.string.aset_help_title;
					msg = R.string.aset_help_msg;
					break;
				case TAB_INSPECTOR:
					title = R.string.inspector_help_title;
					msg = R.string.inspector_help_msg;
					break;
//				case TAB_HISTORY:
//					title = R.string.history_help_title;
//					msg = R.string.history_help_msg;
//					break;
				case TAB_GRAPH:
					title = R.string.graph_help_title;
					msg = R.string.graph_help_msg;
					break;
				case TAB_EDIT:
					title = R.string.editexer_help_title;
					msg = R.string.editexer_help_msg;
					break;
				default:
					Log.e (tag, "Illegal tab number!!!");
					break;
			}

			show_help_dialog(title, msg);
		} // hit help
	} // onClick(v)


	//--------------------------------
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.v (tag, "back key: onKeyDown()");
		}
		return super.onKeyDown(keyCode, event);
	}


	//--------------------------------
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.v (tag, "back key: onKeyUp()");
		}
		return super.onKeyUp(keyCode, event);
	}


	//--------------------------------
	//	The input is the name of the Class that the tab changes TO.
	//	Seems to happen AFTER onCreate() (if it's called at all).
	//
	//	NOTE:
	//		I've seen this called BEFORE onCreate(), too!!!
	//		I guess, it's just something to inform this Activity
	//		that a tab has changed--it cannot be relied upon for
	//		timing.
	//
	public void onTabChanged(String tabId) {
		int tab = getTabHost().getCurrentTab();
		Log.v(tag, "onTabChanged().  current tab = " + tab);

		Log.v(tag, "onTabChanged().  tabId = " + tabId);
	} // onTabChanged(tabId)


	/***********************
	 * Based on BaseDialogActivity
	 */
	protected void show_help_dialog (int title_id, int msg_id) {

		// Build a new Dialog.
		m_dialog = new Dialog(this);

		// This prevents the automatic title area from being made.
		// And it has to be the first thing, too.
		m_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		m_dialog.setContentView(R.layout.dialog_help);

		// Fill in the Views (title & msg).
		TextView title = (TextView) m_dialog.findViewById(R.id.dialog_help_title_tv);
		if (title_id == -1)
			title.setText("");
		else
			title.setText(title_id);

		TextView msg = (TextView) m_dialog.findViewById(R.id.dialog_help_msg_tv);
		if (msg_id == -1)
			msg.setText("");
		else
			msg.setText(msg_id);

		Button ok_butt = (Button) m_dialog.findViewById(R.id.dialog_help_ok_butt);
		ok_butt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				m_dialog.dismiss();
				m_dialog = null;		// Allows garbage collection
			}
		});
		m_dialog.show();
	} // showHelpDialog (title_id, msg_id)

}
