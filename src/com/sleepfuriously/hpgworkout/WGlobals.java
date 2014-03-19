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
		GRIDACTIVITY2 = 7,
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
		EXERCISETABHOSTACTIVITY = 21,
		GRAPHOPTIONSACTIVITY = 22,
		MANAGEDATABASEACTIVITY = 23,
		MANAGEDATABASEPOPUPACTIVITY = 24;	// added as they're made


	//---------------------------
	//	PREFERENCE CONSTANTS
	//---------------------------

	/**
	 * The name of the preferences file.  It's found in
	 * data/data/com.sleepfuriously.hpgworkout/shared_prefs/
	 */
//	public static final String PREFS_FILE_NAME = "hpg_prefs.xml";

	/** How many wheels to the left of the decimal point */
	public static final int DEFAULT_WHEELS_NUM_LEFT = 4;

	/** How many wheels to the right of the decimal point */
	public static final int DEFAULT_WHEELS_NUM_RIGHT = 1;

	/** How many pixels wide for each wheel */
//	public static final int DEFAULT_WHEEL_WIDTH = 10;
	public static final int DEFAULT_WHEEL_WIDTH = 40;

	/** How many pixels wide for a fat wheel */
//	public static final int DEFAULT_WHEEL_WIDTH_FAT = 45;
	public static final int DEFAULT_WHEEL_WIDTH_FAT = 65;

	/** Size of the text for the number wheels. */
	public static final int DEFAULT_WHEEL_TEXT_SIZE = 12;

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


	/**
	 * Defines the indices (handles) for the various sounds.
	 */
	public static final int
		SOUND_CLICK = 1,
		SOUND_LONG_CLICK = 2,
		SOUND_COMPLETE = 3,
		SOUND_HELP = 4,
		SOUND_WHEEL = 5,
		NUM_SOUNDS = 6;		// This needs to be the number of sounds here!!!!


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

	/**
	 * When TRUE, the history/inspector lists all the
	 * exercise sets in chronological order.  When
	 * FALSE, it lists them as most recent-first.
	 */
	public static boolean g_hist_chron;

	/**
	 * This user preference tells us whether they want
	 * to use the number wheels or not.
	 */
	public static boolean g_wheel;

	/**
	 * The number of pixels wide for each wheel.  Also
	 * set via preferences.
	 */
	public static int g_wheel_width = DEFAULT_WHEEL_WIDTH;

	/** Whether or not we use fat wheels or the normal slender ones */
	public static boolean g_wheel_width_fat = false;

	/**
	 * Size of the text for the wheels.
	 */
	public static int g_wheel_text_size = 12;


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
				false);

		g_wheel = prefs.getBoolean(activity.getString(R.string.prefs_wheel_key),
				true);

		String width_key = activity.getString(R.string.prefs_wheel_width_key);
		g_wheel_width_fat = prefs.getBoolean(width_key, false);
		if (g_wheel_width_fat) {
			g_wheel_width = DEFAULT_WHEEL_WIDTH_FAT;
		}
		else {
			g_wheel_width = DEFAULT_WHEEL_WIDTH;
		}

		g_wheel_text_size = Integer.parseInt(prefs.getString(activity.getString(R.string.prefs_wheel_text_size_key),
															"" + DEFAULT_WHEEL_TEXT_SIZE));
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


	/******************
	 * Plays a click sound.  But only if the user's preferences
	 * have sound turned on.
	 */
	public static void play_short_click() {
		if (g_sound) {
			SoundManager.playSound(SOUND_CLICK);
		}
	} // button_click()

	/******************
	 * Plays a sound for when the user long-clicks.
	 * But only if the user's preferences
	 * have sound turned on.
	 */
	public static void play_long_click() {
		if (g_sound) {
			SoundManager.playSound(SOUND_LONG_CLICK);
		}
	} // button_click()


	/******************
	 * Plays a sound for when the user selects a help.
	 * But only if the user's preferences
	 * have sound turned on.
	 */
	public static void play_help_click() {
		if (g_sound) {
			SoundManager.playSound(SOUND_HELP);
		}
	} // button_click()


	/******************
	 * Plays a sound for when a Wheel moves a tick.
	 */
	public static void play_wheel_sound() {
		if (g_sound) {
			SoundManager.playSound(SOUND_WHEEL);
		}
	} // button_click()


	/******************
	 * Plays a nice sound when the user does something good.
	 * But only if the user's preferences
	 * have sound turned on.
	 */
	public static void play_completion_sound() {
		if (g_sound) {
			SoundManager.playSound(SOUND_COMPLETE);
		}
	} // button_click()



}
