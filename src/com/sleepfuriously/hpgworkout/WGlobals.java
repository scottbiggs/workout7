package com.sleepfuriously.hpgworkout;
/**
 * This class holds all sorts of global definitions and useful
 * things for the workout program.
 *
 * I made it extend Application so that it calls onCreate() and
 * onTerminate().  This way I can handle g_db_helper better.
 *
 */


import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class WGlobals extends Application {

	//---------------------------
	//	ACTIVITY CONSTANTS
	//---------------------------

		// These are useful for when the Activities call
		// each other.
	public static final int
//		ADDDISTITEMACTIVITY = 1,		// removed
		ADDEXERCISEACTIVITY = 2,
//		ADDTIMEITEMACTIVITY = 3,		// removed
//		ADDWEIGHTITEMACTIVITY = 4,	// removed
		BASEDIALOGACTIVITY = 5,
		EDITEXERCISEACTIVITY = 6,
		GRIDACTIVITY = 7,
		ROWEDITACTIVITY = 8,
		WELCOMEACTIVITY = 9,
		ASETACTIVITY = 10,
		INSPECTORACTIVITY = 11,
		EDITSETACTIVITY = 12,
		NUMBERACTIVITY = 13,
		TEXTACTIVITY = 14,
		STRESSACTIVITY = 15,
		PREFSACTIVITY = 16,
		GRAPHACTIVITY = 17,
		GRAPHSELECTORACTIVITY = 18,
		HISTORYACTIVITY = 19,	// todo: deprecated
		TESTACTIVITY = 20,		// todo:		Just for debugging!!
		EXERCISETABHOSTACTIVITY = 21;	// added as they're made


	//---------------------------
	//	PREFERENCE CONSTANTS
	//---------------------------

	/**
	 * The name of the preferences file.  It's found in
	 * data/data/com.sleepfuriously.hpgworkout/shared_prefs/
	 */
//	public static final String PREFS_FILE_NAME = "hpg_prefs.xml";


	//---------------------------
	//	WORKOUT CONSTANTS
	//---------------------------

		// This is the key to the bundle that is passed to
		// the EditExer Activity.  This string associated with
		// this key is the name of the exercise that is being
		// edited.
	public static final String EDIT_EXERCISE_KEY = "edit_key";

		// These describe the different types of
		// exercises.
	public static final int
		EXER_TYPE_NONE = -1,
		EXER_TYPE_ANAEROBIC = 0,
		EXER_TYPE_AEROBIC = 1,
		EXER_TYPE_BOTH = 2,
		EXER_TYPE_MISC = 3;

		// Describe the muscle groups that an
		// exercise can work on.
	public static final int
		EXER_GROUP_NONE = -1,
		EXER_GROUP_UPPER = 0,
		EXER_GROUP_LOWER = 1,
		EXER_GROUP_TRUNK = 2,
		EXER_GROUP_ALL = 3,
		EXER_GROUP_MISC = 4;


	//----------------------------------
	//	Global Variables
	//----------------------------------

	/** Helper to load our database */
	public static DatabaseHelper g_db_helper = null;


	//---------------------------
	//	PREFERENCE GLOBALS
	//---------------------------
	/**
	 * Tells whether or not this program should nag the user
	 * with lots of dialogs or not.
	 */
	public static boolean g_nag = true;

	/**
	 * Should the screen stay on or not.
	 */
	public static boolean g_stay_awake = false;

	/**
	 * Tells whether to play sounds or not.
	 */
	public static boolean g_sound = true;

	/** Volume of the sound (when it's ON). */
	// todo:
	//	Set the default and use this.
	public static int g_sound_volume;

	/**
	 * When TRUE, the history/inspector lists all the
	 * exercise sets in chronological order.  When
	 * FALSE, it lists them as most recent-first.
	 */
	public static boolean g_hist_chron;


	//----------------------------------
	//	Instance Methods
	//----------------------------------



	//----------------------------------
	//	Static Methods
	//----------------------------------


	/************************
	 * Call this anytime the preferences screen has been
	 * shown.  Who knows what's been changed!
	 *
	 * THEN: you MUST call act_on_prefs() after!  Yes, that's
	 * what really does stuff.  This method just loads the
	 * globals from the preference file.
	 *
	 * NOTE:
	 * 		Don't need a save_prefs(), as that's automatically
	 * 		done for us!
	 */
	public static void load_prefs (Activity activity) {
		SharedPreferences prefs =
			PreferenceManager.getDefaultSharedPreferences(activity);

		// Grab the prefs from the file and save them to our
		// global variables.
		g_stay_awake = prefs.getBoolean(activity.getString(R.string.prefs_display_key),
				false);
		g_sound = prefs.getBoolean(activity.getString(R.string.prefs_sound_key),
				true);
		g_nag = prefs.getBoolean(activity.getString(R.string.prefs_nag_key),
				true);
		g_hist_chron = prefs.getBoolean(activity.getString(R.string.prefs_inspector_oldest_first_key),
				true);

	} // load_prefs()


	/************************
	 * This method does all the system changes as demanded by
	 * the various preferences.  For example, if the user (in
	 * the preferences screen) tells the screen-saver to NOT
	 * turn off, this executes that command.
	 *
	 * Each Activity should call this during its onCreate().
	 *
	 * Also, call this anytime the prefs have been changed,
	 * but AFTER calling load_prefs().
	 *
	 * NOTE:		This is automatically during onCreate()
	 * 			with any activity that inherits from
	 * 			BaseDialogActivity and calls super.onCreate().
	 *
	 * NOTE2:	This has to be static because of the Activity
	 * 			parameter.
	 *
	 * preconditions:
	 * 		- all the prefs globals are properly set.
	 *
	 * @param	The Activity that is calling this.
	 */
	public static void act_on_prefs (Activity activity) {
		if (g_stay_awake) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		else {
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	} // act_on_prefs()


	//-----------------------------
	// This makes the program go full-screen!
	//
	//	input:
	//		activity		The activity that we want to display
	//					full screen.  'this' should work fine
	//					in most cases.
	//
	public static void tryFullScreen (Activity activity) {
		if (activity.requestWindowFeature (Window.FEATURE_NO_TITLE) == true) {
			activity.getWindow().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	} // tryFullScreen()


	//-----------------------------
	//	Displays the contents of the displayed cursor to
	//	the log file.
	//
	//	All logs are displayed as warnings.
	//
	public static void logCursorContents (Cursor cursor) {
		final String tag = "--logCursorContents()--";
		int i = 0, pos;

		pos = cursor.getPosition();
		Log.i (tag, "   cursor position = " + pos);
		Log.i (tag, "   number of columns = " + cursor.getColumnCount());
		Log.i (tag, "   number of rows = " + cursor.getCount());
		while (cursor.moveToNext()) {
			Log.i (tag, "   --> Row " + i + ":");
			for (int j = 0; j < cursor.getColumnCount(); j++) {
				Log.i (tag, "          Column " + j + " = (" + cursor.getColumnName(j) + ", " + cursor.getString(j) + ")");
			}
		}
		Log.i (tag, "  --end of cursor");

			// Reset the cursor to what it was when this was
			// called.
		cursor.moveToPosition(pos);
	} // log_cursor_contents()

}
