/**
 * This is the main screen of the Hyde Park Gym workout
 * app.
 *
 * This is version 4.
 * 	- Version 1 had a problem with a bunch of files being
 * 	accidentally deleted.
 * 	- Version 2 couldn't do ProgressDialogs and AsyncTasks
 * 	correctly (still don't know why).
 * 	- V. 3 had problems with the GIT version control.
 *
 * My test cases DID work correctly, so I'm adding stuff
 * TO one of those working cases to make yet another version.
 *
 * Wish me luck.
 */
package com.sleepfuriously.hpgworkout;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class WelcomeActivity extends BaseDialogActivity
					implements
						OnClickListener,
						OnLongClickListener {

	//------------------
	//	Constants
	//------------------

	private static final String tag = "WelcomeActivity";


	//------------------
	//	Widgets
	//------------------

	Button	m_start_butt,
			m_settings_butt,
			m_export_butt,
			m_graphs_butt,
			m_exit_butt,
			m_help_butt;

	ImageView m_help_logo_butt;


	//------------------
	//	Class Data
	//------------------



	//------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(tag, "entering onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		// Load in the default preferences (but don't
		// override the user's settings).
		PreferenceManager.setDefaultValues(this,
										R.xml.pref,
										false);	// Prevents overriding user's saved preferences.

		m_start_butt = (Button) findViewById(R.id.welcome_start_butt);
		m_start_butt.setOnClickListener(this);
		m_start_butt.setOnLongClickListener(this);

		m_settings_butt = (Button) findViewById(R.id.welcome_settings_butt);
		m_settings_butt.setOnClickListener(this);
		m_settings_butt.setOnLongClickListener(this);

		m_graphs_butt = (Button) findViewById(R.id.welcome_graph_butt);
		m_graphs_butt.setOnClickListener(this);
		m_graphs_butt.setOnLongClickListener(this);

		m_exit_butt = (Button) findViewById(R.id.welcome_exit_butt);
		m_exit_butt.setOnClickListener(this);
		m_exit_butt.setOnLongClickListener(this);

		m_help_butt = (Button) findViewById(R.id.welcome_help_butt);
		m_help_butt.setOnClickListener(this);

		m_help_logo_butt = (ImageView) findViewById(R.id.welcome_logo_id);
		m_help_logo_butt.setOnClickListener(this);

//		m_export_butt = (Button) findViewById(R.id.welcome_export_butt);
//		m_export_butt.setOnClickListener(this);
//		m_export_butt.setOnLongClickListener(this);
//		m_export_butt.setEnabled(false);

		// Get the DatabaseHelper going.  Since this is the
		// first Activity, this should take care of the life
		// cycle ot g_db_helper.
		WGlobals.g_db_helper = new DatabaseHelper(this);

	} // onCreate()


	//-----------------------------
	//	Just clean up the database helper.
	//
	@Override
	protected void onDestroy() {
		if (WGlobals.g_db_helper != null) {
			WGlobals.g_db_helper.close();
			WGlobals.g_db_helper = null;
		}
		super.onDestroy();
	} // onDestroy()


	//------------------------------
	@Override
	public void onClick(View v) {
		Intent itt;

		switch (v.getId()) {
			case R.id.welcome_start_butt:
				itt = new Intent (this, GridActivity.class);
				startActivity (itt);
				break;

			case R.id.welcome_settings_butt:
				itt = new Intent (this, PrefsActivity.class);
				startActivityForResult(itt, WGlobals.PREFSACTIVITY);
				break;

//			case R.id.welcome_export_butt:
//				break;

			case R.id.welcome_graph_butt:
				itt = new Intent (this, GraphSelectorActivity.class);
				startActivity(itt);
				break;

			case R.id.welcome_exit_butt:
				finish();
				break;

			case R.id.welcome_help_butt:
			case R.id.welcome_logo_id:
				show_help_dialog(R.string.welcome_help_help_title,
						R.string.welcome_help_help_msg);
				break;

			default:
				Log.e(tag, "Illegal value in onClick()!");
				break;
		}
	} // onClick(v)



	//------------------------------
	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
			case R.id.welcome_start_butt:
				show_help_dialog(R.string.welcome_start_help_title, R.string.welcome_start_help_msg);
				return true;

			case R.id.welcome_settings_butt:
				show_help_dialog(R.string.welcome_settings_help_title, R.string.welcome_settings_help_msg);
				return true;

//			case R.id.welcome_export_butt:
//				show_help_dialog(R.string.welcome_export_help_title, R.string.welcome_export_help_msg);
//				return true;

			case R.id.welcome_graph_butt:
				show_help_dialog(R.string.welcome_graphs_help_title, R.string.welcome_graphs_help_msg);
				return true;

			case R.id.welcome_exit_butt:
				show_help_dialog(R.string.welcome_exit_help_title, R.string.welcome_exit_help_msg);
				return true;
		}
		return false;
	} // onLongClick (v)



	//------------------------------
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
									Intent data) {
		switch (requestCode) {
			case WGlobals.PREFSACTIVITY:
				WGlobals.load_prefs(this);
				WGlobals.act_on_prefs (this);
				break;

		} // switch (requestCode)

	} // onActivityResult (requestCode, resultCode, data)


}