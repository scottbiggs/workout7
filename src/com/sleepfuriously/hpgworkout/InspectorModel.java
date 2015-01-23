package com.sleepfuriously.hpgworkout;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * This is the Model (using the MVC paradigm) for the Inspector class.
 * All permanent set data used by the Inspector is held, accessed,
 * and modified here.
 * <p>
 * USAGE:<br>
 * 1) Constructor.  Supply the required arguments. You can change them
 * later using supplied getters and setters.<br>
 *
 * 2) Get information about the exercise (not the set data, the actual
 * stuff relevant to the exercise itself) by calling {@link #get_exercise_data()}.<br>
 *
 * 3) How many sets are there for this exercise? Just call {@link #get_set_count()}
 * to answer that question.<br>
 *
 * 4) You can get a list of the sets with a call to {@link #get_all_sets()}.
 * Individual set information can be retrieved through their id via
 * {@link #get_set(int)}, but that's kind of a pain to use.  It's much
 * easier to get the whole schbang and loop through it yourself.<br>
 *<p>
 * If at any time the database changes OUTSIDE of this class (for example,
 * if the EditSetActivity changes the database!), call {@link #refresh_data()}.
 * This will flush the cache and make sure you're using updated data.
 * <p>
 * Make sure that you <b>never</b> write to any data retrieved here.  It's
 * all read-only.  You'll get yourself into a world of hurt otherwise (and
 * bugs that are hard to find).
 * <p>
 * Lastly, but MOST importantly, make sure that you use a seperate thread
 * (like an AsyncTask) for many of these methods.  The dangerous are marked
 * in their documentation.  I recommend heeding those warnings.  While a lot
 * of database stuff is pretty fast, you can never tell for these things.
 * Good luck.
 */
public class InspectorModel {

	//-----------------------
	//	Constants
	//-----------------------

	private static final String tag = "InspectorModel";


	//-----------------------
	//	Data
	//-----------------------

	/** The name of the exercise we're inspecting */
	private String m_ex_name;

	/**
	 * Holds all the relevant data to this exercise (NOT
	 * the exercise sets).
	 */
	private ExerciseData m_ex_data = null;

	/**
	 *  Holds all the sets for this particular exercise.
	 *  When this is null, it signals that a new refresh
	 *  is needed next time a request for data happens.
	 */
	private ArrayList<SetData> m_sets = null;

	/**
	 * This is a local copy of the user's preference
	 * for whether the set list is oldest first (true)
	 * or most-recent first (false).
	 */
	private boolean m_oldest_first;

	//-----------------------
	//	Public Methods
	//-----------------------

	/***********************
	 * Constructor
	 *
	 * @param	ex_name		The name of the exercise.
	 *
	 * @param	old_first	How to order the list of sets.
	 * 						True = oldest item first, false
	 * 						means to make the newest first.
	 */
	InspectorModel (String ex_name, boolean old_first) {
		set_exercise_name(ex_name);
		set_oldest_first(old_first);
	}

	/***********************
	 * Returns the exercise name this class is using.
	 * @return
	 */
	String get_exercise_name() {
		return m_ex_name;
	}

	/***********************
	 * Changes the exercise name to what is specified.
	 */
	void set_exercise_name (String ex_name) {
		m_ex_name = ex_name;
		m_ex_data = null;		// Signal that we need to
								// reload this data.
	}

	/***********************
	 * Returns true if the set list is ordered via
	 * the oldest first.  False means it's in reverse
	 * chronological order.
	 */
	boolean get_oldest_first() {
		return m_oldest_first;
	}

	/***********************
	 * Use this to set our oldest first ordering of the
	 * set list.
	 *
	 * @param old_first		True = chronological order,
	 * 						false is the reverse.
	 */
	void set_oldest_first (boolean old_first) {
		if (m_oldest_first != old_first) {
			m_oldest_first = old_first;
			m_sets = null;		// Note that we need to reload
								// our sets.
		}
	}


	/***********************
	 * Call this if the database changes externally
	 * for some reason.  This will cause any data accesses
	 * by this class to be read fresh (instead of using
	 * its internal cache for speed).
	 *
	 * This is NOT time intensive, so it's safe to be called
	 * within the UI thread.
	 */
	void refresh_data() {
		m_sets = null;		// Signal to read in new data
							// next request.
	} // refresh_data()


	/***********************
	 * Returns all the associated data that describes this
	 * exercise.  Does NOT include any workout set information.
	 *<p>
	 * NOTE: This needs to be run in a seperate thread from the
	 * UI.  We're accessing SQLite databases, which may take a bit
	 * of time.
	 *
	 * @return	Null if no information is found with the current
	 * 			exercise name.
	 */
	ExerciseData get_exercise_data() {
		if (m_ex_data == null) {
			// Need to get this info from the DB.
			SQLiteDatabase db = null;
			try {
				if (WGlobals.g_db_helper == null) {
					Log.e(tag, "get_exercise_data(): g_db_helper is NULL!");
					throw new SQLiteException("Attempting to get_exercise_data(), yet WGlobals.g_db_helper is null!");
				}
				db = WGlobals.g_db_helper.getReadableDatabase();
				m_ex_data = DatabaseHelper.getExerciseData(db, m_ex_name);
			} // end of DB usage
			catch (SQLiteException e) {
				e.printStackTrace();
			}
			finally {
				if (db != null) {
					db.close();
					db = null;
				}
			}
		}

		return m_ex_data;
	} // get_exercise_data()


	/***********************
	 * Returns a list of all the workout sets for this exercise.
	 *<p>
	 * NOTE:
	 * Please do NOT modify this list, as that may have odd
	 * side effects here!
	 *<p>
	 * NOTE: This MAY take a while, so to be safe run it
	 * outside of the UI thread.
	 *
	 * @return	An empty list if there are none, null on error.
	 */
	ArrayList<SetData> get_all_sets() {

		// Easy case first.
		if (m_sets != null)
			return m_sets;


		// Need to load up our sets.  This may take a
		// while.
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = WGlobals.g_db_helper.getReadableDatabase();
			cursor = DatabaseHelper
					.getAllSets(db, m_ex_name, m_oldest_first);

			// Loop through the cursor and build our array.
			m_sets = new ArrayList<SetData>();
			while (cursor.moveToNext()) {
				// Yup, that's all there is to it!
				m_sets.add(DatabaseHelper.getSetData(cursor));
			}
		}
		catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {					// Clean up!
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			if (db != null) {
				db.close();
				db = null;
			}
		}

		return m_sets;
	} // get_all_sets()


	/***********************
	 * Returns the number of sets for this exercise.
	 *<p>
	 * NOTE: This MAY take a while, so to be safe run it
	 * outside of the UI thread.
	 */
	int get_set_count() {
		if (m_sets == null) {
			get_all_sets();
		}
		return m_sets.size();
	}

	/***********************
	 * Determines if a set with the given id actually exists
	 * or not for this exercise.
	 *<p>
	 * NOTE: This MAY take a while, so to be safe run it
	 * outside of the UI thread.
	 *
	 * @param id		The id of the set in question.
	 */
	boolean exists_set (int id) {
		if (m_sets == null) {
			get_all_sets();
		}

		for (SetData a_set : m_sets) {
			if (a_set._id == id) {
				return true;
			}
		}
		return false;
	} // exists_set (id)


	/***********************
	 * Returns the specified exercise set.  Null if not valid.
	 *<p>
	 * NOTE: This MAY take a while, so to be safe run it
	 * outside of the UI thread.
	 */
	SetData get_set (int id) {
		if (m_sets == null) {
			get_all_sets();
		}

		for (SetData a_set : m_sets) {
			if (a_set._id == id) {
				return a_set;
			}
		}
		return null;		// not found
	}


	/***********************
	 * NOT IMPLEMENTED!
	 *<p>
	 * Don't know if this is necessary as the database is updated
	 * in the EditSetActivity.
	 */
	@Deprecated
	boolean remove_set() {
		// todo
		return true;
	}

	/***********************
	 * NOT IMPLEMENTED!
	 *<p>
	 * Don't know if this is necessary as the database is updated
	 * in the EditSetActivity.
	 */
	@Deprecated
	boolean change_set() {
		// todo
		return true;
	}


	//-----------------------
	//	Private Methods
	//-----------------------



}
