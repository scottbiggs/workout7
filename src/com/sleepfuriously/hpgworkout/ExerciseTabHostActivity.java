/**
 * This is the Activity that pops up when the user selects an
 * exercise or a cell in the grid.  It displays a tabbed Activity.
 * Which tab starts depends on how the user got here.
 */
package com.sleepfuriously.hpgworkout;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
		TAB_ASET = 0,
		TAB_INSPECTOR = 1,
//		TAB_HISTORY = 2,		// History is no longer used.
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
//		Log.v(tag, "entering onCreate()");
		m_tab_active = true;
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE | Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.exercise_tabhost);

		// Do the preferences.
		WGlobals.load_prefs(this);
		WGlobals.act_on_prefs (this);

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
		String user = DatabaseFilesHelper.get_active_username(this);
		String possessive = getString(R.string.possessive_suffix);
		tv.setText(user + possessive + " " + m_ex_name);

		//------------
		//	Do the Tabs
		//------------

		TabHost th = getTabHost();	// Gets the TabHost from this Activity
		setup_tabs(th);

		// Set the start tab.
		th.setCurrentTab(start);
		th.setOnTabChangedListener(this);
//		Log.v(tag, "onCreate done.");
	} // onCreate(.)


	//--------------------------------
	@Override
	protected void onDestroy() {
		Log.v(tag, "onDestroy()");
		m_tab_active = false;
		super.onDestroy();
	}


	/**************************
	 * Creates tabs for the normal (portrait) orientation.
	 * Call this during onCreate().
	 *
	 * @param	th		Reference to our TabHost.
	 */
	private void setup_tabs (TabHost th) {
		boolean landscape =
				getResources().getConfiguration().orientation ==
						Configuration.ORIENTATION_LANDSCAPE;

		if (landscape) {
			// Seperates the tabs a bit.
			th.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
		}


		{
			// Create an Intent to launch the first tab. (ASet = 0)
			// NOTE 1:
			//		It seems that this Activity's onCreate is called immediately.
			//		From my tests, the first Activity is always called as
			//		it is defined.
			//
			Intent aset_itt = new Intent(this, AddSetActivity.class);
			aset_itt.putExtra(KEY_NAME, m_ex_name);
			aset_itt.putExtra(KEY_SET_ID, m_id);

			setup_a_tab (th, "ASetActivity",
						R.string.exer_tabhost_tab_aset,
						R.drawable.wheel_aset,
						aset_itt, landscape);
		}

		{
			// Now the second tab (Inspector = 1)
			// NOTE:
			//		Need to create a NEW Intent--can't reuse the other one
			//		as that'll create a lot of confusion.
			Intent inspector_itt = new Intent(this, InspectorActivity2.class);
			inspector_itt.putExtra(KEY_NAME, m_ex_name);
			inspector_itt.putExtra(KEY_SET_ID, m_id);
			setup_a_tab (th, "InspectorActivity",
						R.string.exer_tabhost_tab_inspector,
						R.drawable.user_inspector,
						inspector_itt, landscape);
		}


		{
			// (Graph = 2)
			Intent graph_itt = new Intent(this, GraphActivity.class);
			graph_itt.putExtra(KEY_NAME, m_ex_name);
			graph_itt.putExtra(KEY_SET_ID, m_id);
			setup_a_tab (th, "GraphActivity",
						R.string.exer_tabhost_tab_graph,
						R.drawable.web_graph,
						graph_itt, landscape);
		}

		{
			// (EditExercise = 3)
			Intent edit_itt = new Intent(this, EditExerciseActivity.class);
			edit_itt.putExtra(KEY_NAME, m_ex_name);
			edit_itt.putExtra(KEY_SET_ID, m_id);
			setup_a_tab (th, "EditExerciseActivity",
						R.string.exer_tabhost_tab_edit_exercise,
						R.drawable.writingpad_edit_exercise,
						edit_itt, landscape);
		}

	} // portrait_tab_setup()


	/**************************
	 * Adds a tab with the given tag and intent to the TabHost.
	 * Depending on whether we're in landscape mode, this'll
	 * figure out which tab-type to make (thin or full).
	 *
	 * @param th			The TabHost to stuff this tab into.
	 * @param tab_tag	The tag for this tab (no one seems to know what
	 * 					this is for.
	 * @param name_id	The resource id for the display string to use
	 * 					for this tab.
	 * @param icon_id	A reference to the resource for this tab's icon.
	 * 					Ignored if landscape == true.
	 * @param intent		The intent to start the Activity that will show
	 * 					this tab's contents.  Eg: new Intent(this, FooActivity.class)
	 * @param landscape	True if we're in landscape mode.  We'll do a thin
	 * 					tab with no icon in this case.
	 */
	private void setup_a_tab (TabHost th,
								final String tab_tag,
								final int name_id,
								final int icon_id,
								Intent intent,
								boolean landscape) {
		if (landscape) {
			setup_thin_tab(th, tab_tag, name_id, intent);
		}
		else {
			setup_full_tab(th, tab_tag, name_id, icon_id, intent);
		}
	} // setup_a_tab(...)


	/**************************
	 * Adds a tab with the given tag and intent
	 * to the TabHost.  This makes a REGULAR tab,
	 * including a label AND icon.
	 *
	 * @param th			The TabHost to stuff this tab into.
	 * @param tab_tag	The tag for this tab (no one seems to know what
	 * 					this is for.
	 * @param name_id	The resource id for the display string to use
	 * 					for this tab.
	 * @param icon_id	A reference to the resource for this tab's icon.
	 * @param intent		The intent to start the Activity that will show
	 * 					this tab's contents.  Eg: new Intent(this, FooActivity.class)
	 */
	private void setup_full_tab (TabHost th,
								final String tab_tag,
								final int name_id,
								final int icon_id,
								Intent intent) {
		TabSpec spec = th.newTabSpec(tab_tag);
		spec.setContent(intent);
		spec.setIndicator(getString(name_id), getResources().getDrawable(icon_id));
		th.addTab(spec);
	} // setup_full_tab(...)

	/****************************
	 * Adds a tab with an Activity for its content, but this
	 * version shows ONLY the text--no icon!  Nice for landscape
	 * mode.
	 *
	 * @param th			The TabHost to stuff this tab into.
	 * @param tab_tag	The tag for this tab (no one seems to know what
	 * 					this is for.
	 * @param name_id	The resource id for the display string to use
	 * 					for this tab.
	 * @param intent		The intent to start the Activity that will show
	 * 					this tab's contents.  Eg: new Intent(this, FooActivity.class)
	 */
	private void setup_thin_tab (TabHost th,
								final String tab_tag,
								final int name_id,
								Intent intent) {
		View tabview = LayoutInflater.from(th.getContext())
										.inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) tabview.findViewById(R.id.tabsText);
		tv.setText(getString(name_id));

		// Here it is in a more normal form.
		TabSpec spec = th.newTabSpec(tab_tag);
		spec.setIndicator(tabview);
		spec.setContent(intent);

		th.addTab(spec);
	} // setup_thin_tab(th, tab_tag, intent


	//--------------------------------
	@Override
	public void onClick(View v) {
		WGlobals.play_short_click();

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
//			Log.v (tag, "back key: onKeyDown()");
		}
		return super.onKeyDown(keyCode, event);
	}


	//--------------------------------
	//	todo:
	//		This never seems to be called.
	//
	@Override
	public void onBackPressed() {
		Log.v(tag, "onBackPressed()");
		if (m_dirty)
			setResult(RESULT_OK);
		else
			setResult(RESULT_CANCELED);
		finish();
//		super.onBackPressed();
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
	@Override
	public void onTabChanged(String tabId) {
		WGlobals.play_short_click();
//		int tab = getTabHost().getCurrentTab();
//		Log.v(tag, "onTabChanged().  current tab = " + tab);
//		Log.v(tag, "onTabChanged().  tabId = " + tabId);
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
			@Override
			public void onClick(View v) {
				WGlobals.play_short_click();
				m_dialog.dismiss();
				m_dialog = null;		// Allows garbage collection
			}
		});
		m_dialog.show();
	} // showHelpDialog (title_id, msg_id)

}
