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
import android.widget.TextView;

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


	//------------------
	//	Class Data
	//------------------


	//-----------------------
	//	UI Callback Methods
	//-----------------------

	//------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// Load in the default preferences (but don't
		// override the user's settings).  This is done
		// once and only once for the very first Activity
		// the very first time it's run.
		PreferenceManager.setDefaultValues(this,
										R.xml.pref,
										false);	// Prevents overriding user's saved preferences.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		// Setup the buttons
		Button start_butt = (Button) findViewById(R.id.welcome_start_butt);
		start_butt.setOnClickListener(this);
		start_butt.setOnLongClickListener(this);

		Button settings_butt = (Button) findViewById(R.id.welcome_settings_butt);
		settings_butt.setOnClickListener(this);
		settings_butt.setOnLongClickListener(this);

		Button graphs_butt = (Button) findViewById(R.id.welcome_graph_butt);
		graphs_butt.setOnClickListener(this);
		graphs_butt.setOnLongClickListener(this);

		Button exit_butt = (Button) findViewById(R.id.welcome_exit_butt);
		exit_butt.setOnClickListener(this);
		exit_butt.setOnLongClickListener(this);

		Button help_butt = (Button) findViewById(R.id.welcome_help_butt);
		help_butt.setOnClickListener(this);

		ImageView help_logo_butt = (ImageView) findViewById(R.id.welcome_logo_id);
		help_logo_butt.setOnClickListener(this);

		// Start the sound system.
		SoundManager.getInstance();
		SoundManager.initSounds(this);
		SoundManager.addSound(WGlobals.SOUND_CLICK, R.raw.button_click);
		SoundManager.addSound(WGlobals.SOUND_LONG_CLICK, R.raw.longclick);
		SoundManager.addSound(WGlobals.SOUND_COMPLETE, R.raw.wineclink);
		SoundManager.addSound(WGlobals.SOUND_HELP, R.raw.help_ding_single);
		SoundManager.addSound(WGlobals.SOUND_WHEEL, R.raw.wheel2);

		// Need to get the database going.  This MUST happend before
		// the next statement as it uses the database (probably).
		DatabaseFilesHelper.init(this);

		set_current_db_user();

	} // onCreate()


	//-----------------------------
	//	Just clean up the database helper.
	//
	@Override
	protected void onDestroy() {
		SoundManager.cleanup();
		DatabaseFilesHelper.cleanup(this);

		super.onDestroy();
	} // onDestroy()


	//------------------------------
	@Override
	public void onClick(View v) {
		Intent itt;

		switch (v.getId()) {
			case R.id.welcome_start_butt:
				WGlobals.play_short_click();
				itt = new Intent (this, GridActivity2.class);
				startActivity (itt);
				break;

			case R.id.welcome_settings_butt:
				WGlobals.play_short_click();
				itt = new Intent (this, PrefsActivity.class);
				startActivityForResult(itt, WGlobals.PREFSACTIVITY);
				break;

			case R.id.welcome_user_name_tv:
				WGlobals.play_short_click();
				itt = new Intent (this, ManageDatabaseActivity.class);
				startActivityForResult(itt, WGlobals.MANAGEDATABASEACTIVITY);
				break;

			case R.id.welcome_graph_butt:
				WGlobals.play_short_click();
				itt = new Intent (this, GraphSelectorActivity.class);
				startActivity(itt);
				break;

			case R.id.welcome_exit_butt:
				WGlobals.play_short_click();
				finish();
				break;

			case R.id.welcome_help_butt:
			case R.id.welcome_logo_id:
				WGlobals.play_help_click();
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
		WGlobals.play_long_click();
		switch (v.getId()) {
			case R.id.welcome_start_butt:
				show_help_dialog(R.string.welcome_start_help_title, R.string.welcome_start_help_msg);
				return true;

			case R.id.welcome_settings_butt:
				show_help_dialog(R.string.welcome_settings_help_title, R.string.welcome_settings_help_msg);
				return true;

			case R.id.welcome_graph_butt:
				show_help_dialog(R.string.welcome_graphs_help_title, R.string.welcome_graphs_help_msg);
				return true;

			case R.id.welcome_user_name_tv:
				show_help_dialog(R.string.welcome_manage_db_help_title, R.string.welcome_manage_db_help_msg);
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

			case WGlobals.MANAGEDATABASEACTIVITY:
				set_current_db_user();
				break;

		} // switch (requestCode)

	} // onActivityResult (requestCode, resultCode, data)


	/****************************
	 * Grabs the current database username and fills the appropriate
	 * UI widgets.
	 */
	private void set_current_db_user() {
		// Figure out which database we're using (it's saved in
		// the preferences).  This'll tell us which to load up
		// and which database file to fire up.
		TextView user_name_tv = (TextView) findViewById(R.id.welcome_user_name_tv);
		user_name_tv.setOnClickListener(this);
		user_name_tv.setOnLongClickListener(this);

		// Since this is the first Activity, go ahead and initialize
		// the database.  Once done, it'll also tell us the name of
		// the active database.
		user_name_tv.setText(DatabaseFilesHelper.get_active_username(this));

		String db_filename = WGlobals.g_db_helper.get_database_filename();
		String db_username = DatabaseFilesHelper.get_user_name(db_filename, this);
		Log.v(tag, "set_current_db_user(): g_db_helper usename = " + db_username);
	}

}