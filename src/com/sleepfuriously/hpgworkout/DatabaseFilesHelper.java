/**
 * How the multiple databases works:
 * ---------------------------------
 * Each database is contained in its own file.  When a database file
 * is created, it is done with a unique name (db00001.sqlite,
 * db00002.sqlite, etc.).  If one is deleted, that number may be
 * re-used by subsequent databases.
 *
 * The names of these files are to be used ONLY within this class
 * (and any child classes).  Everything else will reference these
 * files via a User Name.  User Names should be unique, but this
 * class will not explicitly test for uniqueness before doing any
 * operations--that's up to the caller.
 *
 * The way that the user names and the actual file names for the
 * databases match is through the SharedPreferences.  Each FILE NAME
 * will be an entry (Name) for for a Shared Preference.  The Value
 * at that entry will be the corresponding USER name.
 *
 * Another item in the SharedPreferences is used as a counter,
 * DB_FILENAME_COUNTER.  This counter is used to create new and
 * unique database file names.
 *
 * USAGE:
 * 	1.	Always start by calling init().  This makes sure that
 * 		the database system is properly setup.
 *
 * 	2.	Find out information about the databases by calling
 * 		get_active_username() and get_all_usernames(), and
 * 		get_num().
 *
 * 	3.	Change which database file is active by calling
 * 		activate().
 *
 * 	4.	Add a database by calling add().  Duh.  But make sure
 * 		that the name is unique by calling is_name_used() first.
 *
 * 	5.	Remove a database by calling delete().  But be warned,
 * 		you can't remove the last database!
 *
 * 	6.	Rename a database with rename().  This does NOT make
 * 		the renamed database the Active database.
 *
 * 	7.	You can make a copy of a database with duplicate().
 * 		Again, this will not change which database is active.
 *
 *
 * NOMENCLATURE:
 * username - The name of the database that the user sees.  It is
 * defined in the Value portion of the SharedPreference file.
 *
 * filename - The name of the file for a database.  This is an
 * internal datum, only seen in this class.  It is also the Key
 * portion of a SharePreference.  It's Value is the username.
 *
 * active - The database that is active and open.
 */
package com.sleepfuriously.hpgworkout;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


/********************************
 * A bunch of static methods (and some internal data) to help
 * manipulations of the various database files.
 */
public class DatabaseFilesHelper {

	//--------------------------------------
	//	Data
	//--------------------------------------

	private static final String tag = "DatabaseFilesHelper";


	/** The string that precedes a number for all database names */
	protected static final String DB_FILENAME_PREFIX = "db";

	/** The suffix for all database names */
	protected static final String DB_FILENAME_SUFFIX = ".sqlite";

	/** The user name of the default database */
	protected static final String
			DB_DEFAULT_USERNAME = "default user";

	/** The name of default database file */
	public static final String DB_DEFAULT_FILENAME =
			DB_FILENAME_PREFIX + "00000" + DB_FILENAME_SUFFIX;


	/**
	 * The key to accessing a preference String that tells the
	 * filename for the currently active database.
	 */
	public static final String PREFS_CURRENT_DB_FILE_NAME_KEY =
									"prefs_current_db_name";


	/**
	 * The key to accessing how many databases have been created so
	 * far.  This counter is used to construct unique names
	 * for the database files.  Of course, the value is an int.
	 */
//	protected static final String DB_FILENAME_COUNTER = "db_filename_counter";



	//--------------------------------------
	//	Public Methods
	//--------------------------------------


	/****************************
	 * Call this when beginning to use databases.  It will
	 * make sure that everything is hunky-dory to continue.
	 *
	 * @param	ctx		The Context.  Needed to figure out where this
	 * 					method is being called.  Just supply the
	 * 					Activity, which has a Context built-in.
	 *
	 */
	public static void init (Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

		// While the caller sees a simple init, this is actually
		// quite a complex little thing.  First, we have to make
		// sure that a database actually is up and running.
		//
		// Next, make sure that all the data structures are ready
		// to be used.
		//
		// And while we're doing that, make sure nothing is broken
		// that already is set up to work.

		// Step one.  Is this the FIRST TIME EVER to call this?
		// No database will exist yet, and the SharedPreferences
		// will not be setup either.
		ArrayList<String> filenames = get_all_filenames(ctx);
		if ((filenames != null) && (filenames.size() != 0)) {

			// There is a database file, so start it up.
			if (WGlobals.g_db_helper != null) {
				Log.e(tag, "g_db_helper is not null when running init()!");
				close_active_db();	// trying to continue
			}

			String active_db_file = prefs.getString(PREFS_CURRENT_DB_FILE_NAME_KEY, null);
			if (active_db_file == null) {
				Log.e (tag, "Cannot get the current database file in init()!");
				return;	// can't continue
			}

			// Start up the database and exit.
			WGlobals.g_db_helper = new DatabaseHelper(ctx, active_db_file);
			return;
		}

		// todo Create the database with the default filename for the
		// very first time.
		String filename = get_next_file_name(ctx);
		WGlobals.g_db_helper = new DatabaseHelper(ctx, filename);

		// Make the relation between the new db filename and
		// the default username.
		prefs.edit().putString(filename, DB_DEFAULT_USERNAME).commit();

		// Save the name of the active file.
		prefs.edit().putString(PREFS_CURRENT_DB_FILE_NAME_KEY, filename).commit();

		// And indicate that we've increased our database file count.
		increment_filename_counter(ctx);

	} // init (ctx)


	/****************************
	 * The opposite of init(), this tries to clean everything
	 * up before the program exits.
	 *
	 * @param ctx
	 */
	public static void cleanup (Context ctx) {
		close_active_db();
	}


	/****************************
	 * Figures out all the user names for the databases.
	 *
	 * @param	ctx		The Context.  Needed to figure out where this
	 * 					method is being called.  Just supply the
	 * 					Activity, which has a Context built-in.
	 *
	 * @return	A list of all the user names.  If a user name did
	 * 			not exist for a corresponding file, then an empty
	 * 			string is placed in that location.
	 */
	public static ArrayList<String> get_all_user_names (Context ctx) {
		ArrayList<String> user_names = new ArrayList<String>();
		ArrayList<String> file_names = get_all_filenames(ctx);
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(ctx);
		for (String file_name : file_names) {
			user_names.add(prefs.getString(file_name, ""));
		}
		return user_names;
	}


	/****************************
	 * How many databases are there currently?  Use this
	 * to find out.
	 *
	 * @param	ctx		The Context.  Needed to figure out where this
	 * 					method is being called.  Just supply the
	 * 					Activity, which has a Context built-in.
	 *
	 * @return	The number of databases.  Can be zero.
	 */
	public static int get_num (Context ctx) {
		// Count the database files, which is the definitive
		// list.
		ArrayList<String> dbs = get_all_filenames(ctx);
		return dbs.size();
	} // get_num_dbs(ctx)


	/****************************
	 * Finds the user name for the currently active database.
	 *
	 * @param	ctx
	 *
	 * @return	The string used to identify the current database.<br>
	 * 			Null if there is no current database user name (can
	 * 			happen when the program first starts).
	 */
	public static String get_active_username (Context ctx) {
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(ctx);
		String file_name = prefs.getString(PREFS_CURRENT_DB_FILE_NAME_KEY, null);

		if (file_name == null) {
			Log.e (tag, "Cannot find the active filename in get_active_username()!");
			return null; // Hopefully this will cause a crash.
		}

		String user_name = get_user_name(file_name, ctx);
		return user_name;
	} // get_active_username (ctx)



	/****************************
	 * Make the database with the given username the Active
	 * database.
	 *
	 * @param username
	 * @param ctx
	 * @return	true - activation was successful<br>
	 * 			false - could not find the username--aborted.
	 */
	public static boolean activate (String username, Context ctx) {
		// Check for the easy case.
		String active_username = get_active_username(ctx);
		if ((active_username != null) && (username.equals(active_username))) {
			return true;
		}

		// Make sure that this username is actually valid.
		String filename = get_file_name(username, ctx);
		if (filename == null) {
			return false;
		}

		// Close the current database and reopen it with
		// the new filename.
		close_active_db();
		WGlobals.g_db_helper = new DatabaseHelper(ctx, filename);

		// Finally, note the change.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.edit().putString(PREFS_CURRENT_DB_FILE_NAME_KEY, filename).commit();

		return true;
	} // activate (username, ctx)


	// Returns number of DBs after the add.  -1 on error.
	/****************************
	 * Adds another database to this program.
	 *
	 * @param username	The username for this database.  Please
	 * 					make sure before-hand that this name
	 * 					is unique (for your own sanity).  Try
	 * 					is_name_used() to do so.
	 *
	 * @param ctx
	 *
	 * @return	The number of databases on this computer
	 * 			after this new one has been added.
	 */
	public static int add (String username, Context ctx) {
		String filename = get_next_file_name(ctx);

		// This should create the file with all the defaults.
		DatabaseHelper temp_db_helper = new DatabaseHelper(ctx, filename);
		temp_db_helper.close();
		temp_db_helper = null;

		// Save this info into our prefs.
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.edit().putString(filename, username).commit();


		// And indicate that we've increased our database file count.
		increment_filename_counter(ctx);
		return get_num(ctx);
	} // add (username, ctx)


	// Returns number of DBs after the delete.  -1 if we tried
	// to remove the last one (which was NOT done).
	/****************************
	 * Completely deletes a database, use cautiously!
	 * If this is the active database, the next one in the list
	 * will be activated.
	 * <p>
	 * <b>NOTE</b>:	You may NOT remove the last database.  There
	 * 				must be at least one!
	 *
	 * @param username	Identifies the database to remove.
	 *
	 * @param ctx
	 *
	 * @return	The number of databases AFTER this has been
	 * 			removed.  Or -1 if there was an error (like trying
	 * 			to remove the last database or an invalid
	 * 			username).
	 */
	public static int remove (String username, Context ctx) {

		// Make sure this isn't the last database.
		if (get_num(ctx) == 1) {
			Log.w(tag, "Tried to remove the last database, aborting!");
			return -1;
		}

		// Test to make sure everything exists.
		String filename = get_file_name(username, ctx);
		if (filename == null) {
			Log.e(tag, "Cannot find user '" + username + "'!  Deletion aborted!");
			return -1;
		}

		// If this is the active database, close it.
		boolean is_active = false;
		if (username.equals(get_active_file_name(ctx))) {
			is_active = true;
			close_active_db();
		}

		// Remove the file
		File file_handle = ctx.getDatabasePath(filename);
		if (file_handle.delete() == false) {
			Log.e(tag, "Problem deleting database file '" + filename + "'! Aborting! (the database may have been closed)");
			return -1;
		}

		// Remove the SharedPrefs entry
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.edit().remove(filename).commit();

		if (is_active) {
			// Need to activate another database.  How about
			// the first one I find?
			ArrayList<String> filenames = get_all_filenames(ctx);
			activate_filename(filenames.get(0), ctx);
		}

		return get_num(ctx);
	} // remove (username, ctx)


	/****************************
	 * Tired of a user name and want to change it?  This method
	 * will do the trick.
	 *
	 * @param old_user_name		The user name that's currently
	 * 							used to identify a database.
	 *
	 * @param new_user_name		The name to change it to.  Be warned:
	 * 							this does NOT check to make sure that
	 * 							this name is unique.
	 *
	 * @param ctx
	 *
	 * @return  true if the name was changed successfully. false
	 * 			if a problem arose (probably old_username was
	 * 			not found).
	 */
	public static boolean rename (String old_username, String new_username, Context ctx) {
		String filename = get_file_name(old_username, ctx);
		if (filename == null) {
			Log.w(tag, "Could not find '" + old_username + "' to change it in rename()!");
			return false;
		}

		// Note: Doesn't matter if this is the active database or
		// not as we're only changing the username, which is kept
		// in a SharedPref.
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.edit().putString(filename, new_username).commit();

		return true;
	} // rename (old, new, ctx)


	/***********************
	 * Before making a new username, it might be convenient to
	 * see if that name has already been used.  This will do that.
	 *
	 * @param username	The name to check.
	 *
	 * @param ctx
	 *
	 * @return	true - yup, it's already used here.<br>
	 * 			false - Can't find it--you're free to use this name.
	 */
	public static boolean is_name_used (String username, Context ctx) {
		ArrayList<String> names = get_all_user_names(ctx);
		for (String aname : names) {
			if (username.equals(aname)) {
				return true;
			}
		}
		return false;
	}




	//--------------------------------------
	//	Preferences
	//--------------------------------------

	/** int - The number of filenames that have been created */
	private static final String DB_FILENAME_COUNTER = "db_filename_counter";


	/************************
	 * Returns the number of database files that have been
	 * created.
	 */
	private static int get_filename_count (Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getInt(DB_FILENAME_COUNTER, 0);
	}

	/************************
	 * Increases the database file count by 1.  Note that this
	 * is the number of databases EVER created, not the current
	 * number.
	 */
	private static void increment_filename_counter (Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		int count = prefs.getInt(DB_FILENAME_COUNTER, 0);
		count++;
		prefs.edit().putInt(DB_FILENAME_COUNTER, count).commit();
	}



	//--------------------------------------
	//	Database Filename / Username
	//--------------------------------------















	/****************************
	 * Finds all the databases in our database directory.  Please
	 * note that for this program's sanity, a database is simply
	 * a file with the .sqlite suffix (defined in DB_FILENAME_SUFFIX).
	 *
	 * Possible Bug:
	 * 	May not work if the default database doesn't exist.
	 *
	 * @param	ctx		The Context.  Needed to figure out where this
	 * 					method is being called.  Just supply the
	 * 					Activity, which has a Context built-in.
	 *
	 * @return	A List of strings.  Each are the complete file
	 * 			name (including path and suffix) of a database
	 * 			file.<br>
	 * 			Null if there's nothing to list.
	 */
	private static ArrayList<String> get_all_filenames (Context ctx) {
		// Gets full path + filename.  NOTE: filename does not need to exist!
		File db_path = ctx.getDatabasePath(DB_DEFAULT_FILENAME);
		String orig_path = db_path.toString();

		// Strip the name off to get the actual path.
		String actual_path = orig_path.substring(0, orig_path.length() - DB_DEFAULT_FILENAME.length());
//		Log.d(tag, "The actual path is: " + actual_path);

		// Now get a list of all the files in this directory.  This'll
		// be all the databases we have.
		File f = new File(actual_path);
		File[] files = f.listFiles();
		if (files == null) {
			return null;		// Nothing in the directory.
		}
		ArrayList<String> file_list = new ArrayList<String>();
		for (File a_file : files) {
			// Make sure it's not a directory
			if (a_file.isFile()) {
				// Make sure that it has the appropriate suffix
				String file_name = a_file.toString();
				if (file_name.endsWith(DB_FILENAME_SUFFIX)) {
					file_list.add(a_file.toString());
//					Log.d(tag, "File: " + a_file.toString());
				}
			}
		}
		return file_list;
	} // get_all_db_file_names(ctx)


	/****************************
	 * Finds the corresponding user name for the given file name.
	 *
	 * @param file_name		The name of the file.
	 *
	 * @param ctx
	 *
	 * @return	The corresponding user name.  NULL if not found.
	 */
	private static String get_user_name(String file_name, Context ctx) {
		SharedPreferences prefs =
				PreferenceManager.getDefaultSharedPreferences(ctx);
		String user_name = prefs.getString(file_name, null);
		return user_name;
	}


	/****************************
	 * Finds the corresponding filename to the given user name.
	 * O(n).
	 *
	 * @param user_name		The string for the user's name.
	 *
	 * @param ctx
	 *
	 * @return	The database filename that corresponds to the
	 * 			given user name.  NULL if not found.
	 */
	private static String get_file_name (String user_name, Context ctx) {
		ArrayList<String> file_names = get_all_filenames(ctx);
		for (String file_name : file_names) {
			String tmp_user = get_user_name(file_name, ctx);
			if (user_name.equals(tmp_user)) {
				return file_name;
			}
		}
		return null;
	}




	/****************************
	 * Finds the name of the file for the currently active database.
	 *
	 * @param ctx
	 *
	 * @return  A String for the file name of the current database.
	 * 			Null if no database is currently active.
	 */
	private static String get_active_file_name (Context ctx) {
		if (WGlobals.g_db_helper == null) {
			return null;
		}

		String filename = WGlobals.g_db_helper.get_database_filename();
		return filename;
	}


	/****************************
	 * Checks to see if the current database is active or not.
	 *
	 * @param ctx
	 * @return	true - yes, it's active and running.<br>
	 * 			false - nope, there is no database running.
	 * 			Furthermore, there may not even BE a current
	 * 			database at all (first time running the program
	 * 			or the current has been deleted).
	 */
//	public static boolean is_current_db_active (Context ctx) {
//		if (WGlobals.g_db_helper == null) {
//			return false;
//		}
//		return true;
//	}

		//---------------
		// Actors
		//



	/****************************
	 * Makes the given user name the current database.
	 *
	 * @param user_name		The user name that identifies
	 * 						the database to make current.
	 *
	 * @param ctx
	 *
	 * @return	true iff success
	 */
//	public static boolean make_current (String user_name, Context ctx) {
//		// get the filename
//		String new_db_file = get_file_name(user_name, ctx);
//		if (new_db_file == null) {
//			return false;	// Can't find the file.
//		}
//
//		// Close the current database.
//		close_current_db();
//
//		// Open the global database with the new file name.
//		WGlobals.g_db_helper = new DatabaseHelper(ctx, new_db_file);
//
//		SharedPreferences prefs =
//				PreferenceManager.getDefaultSharedPreferences(ctx);
//		prefs.edit().putString(PREFS_CURRENT_DB_FILE_NAME_KEY,
//							new_db_file).commit();
//
//		return true;
//	}


	/****************************
	 * The database that is marked as current will be activated
	 * if it isn't already.  If no database is selected as
	 * current, then nothing is done.
	 * <p>
	 * <b>Assumption</b>: that if a database is open, it is assumed
	 * to be current.  In other words, we never activate a database
	 * that isn't current.
	 *
	 * @return  true - The current database is now active.<br>
	 * 			false - There is no current database, so nothing
	 * 				was done.
	 */
//	public static boolean activate_current (String user_name, Context ctx) {
//		if (WGlobals.g_db_helper != null) {
//			return true;		// already active
//		}
//
//		// Get the name of the current database file from our
//		// SharedPrefs
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
//		String filename = prefs.getString(PREFS_CURRENT_DB_FILE_NAME_KEY, null);
//		if (filename == null) {
//			// No current has been set.  Probably because this is
//			// the first time the program has been run or the
//			// current was deleted.
//			return false;
//		}
//
//		return activate_filename (filename, ctx);
//
//	}

	/****************************
	 * Given the FILEname of a database, this causes that file to
	 * be active.  If another file was active, then it is closed
	 * before making this one active.  Does NOTHING else.
	 *
	 * NOTE: You MUST make sure that this is the current db!
	 *
	 * @param filename	The name of the database file to activate.
	 *
	 * @param ctx
	 * @return	true - successfully activated.<br>
	 * 			false - problems activating (finding the file probably).
	 */
	private static boolean activate_filename (String filename, Context ctx) {
		close_active_db();

		WGlobals.g_db_helper = new DatabaseHelper(ctx, filename);
		if (WGlobals.g_db_helper == null) {
			return false;
		}
		return true;
	}


	/****************************
	 * Opens the current database and makes it active.  If the
	 * current database is already open, then there's nothing to
	 * do.  If this is the first time this program has run, this
	 * will create, initialize, and activate the default database.
	 * @param ctx
	 * @return
	 */
//	public static boolean open_current_db (Context ctx) {
//		// First, check to see if it's already open!
//		if (WGlobals.g_db_helper != null) {
//			return true;
//		}
//
//		// Get the current filename.
//		String filename = get_current_file_name(ctx);
//		WGlobals.g_db_helper = new DatabaseHelper(ctx, new_db_file);
//
//	}




	/****************************
	 * Need a new file name for a database?  Then this is the
	 * method for you!  Finds the next available unique database
	 * file name and returns it to you.
	 *
	 * @param	ctx		The Context.  Needed to figure out where this
	 * 					method is being called.  Just supply the
	 * 					Activity, which has a Context built-in.
	 *
	 * @return	A name that's available to use as a database file.
	 */
	private static String get_next_file_name (Context ctx) {

		// How many databases have we created so far?
		int count = get_filename_count(ctx);
		count++;		// Increment

		// Construct the new file name.  We want this form:
		// db00013.sqlite for the 13th database file.
		String new_name = String.format("%s%05d%s",
						DB_FILENAME_PREFIX, count, DB_FILENAME_SUFFIX);

		return new_name;
	}


	/****************************
	 * Checks to see if the string is already used for a database
	 * user name.
	 *
	 * @param name		The name to check.
	 *
	 * @param	ctx		The Context.  Needed to figure out where this
	 * 					method is being called.  Just supply the
	 * 					Activity, which has a Context built-in.
	 *
	 * @return	True if this name already exists, false otherwise.
	 */
//	public static boolean does_user_name_exist (String name,
//												Context ctx) {
//		ArrayList<String> user_names = get_all_user_names(ctx);
//		for (String user_name : user_names) {
//			if (name.equals(user_name)) {
//				return true;
//			}
//		}
//		return false;
//	}




	/****************************
	 * Closes the currently open database.  This is necessary before
	 * deleting it or opening a new database as the current.
	 */
	public static void close_active_db() {
		if (WGlobals.g_db_helper == null) {
			// The global db isn't open--it's already closed!
			return;
		}

		WGlobals.g_db_helper.close();
		WGlobals.g_db_helper = null;
	}




	/****************************
	 * Changes the user name of the current database.
	 *
	 * @param new_name  The new user name to apply to the current
	 * 					database.
	 *
	 * @param ctx
	 * @return  true if all went well.  False for an error.
	 */
//	public static boolean change_current_user_name (String new_name,
//													Context ctx) {
//		String filename = get_current_file_name(ctx);
//		if (filename == null) {
//			return false;
//		}
//
//		close_current_db();
//
//		SharedPreferences prefs =
//				PreferenceManager.getDefaultSharedPreferences(ctx);
//		prefs.edit().putString(filename, new_name).commit();
//
//		// Now re-open our database.
//		return open (filename, ctx);
//	}




}
