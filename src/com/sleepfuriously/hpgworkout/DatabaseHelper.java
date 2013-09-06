/**
 * This class is used to open a database.
 *
 * In case you want to push or pull the database file,
 * here's the commands (from whatever directory you want
 * to store the database file):
 *
 * adb pull data/data/com.sleepfuriously.hpgworkout/databases/hpg.sqlite
 *
 * adb push hpg.sqlite data/data/com.sleepfuriously.hpgworkout/databases/
 *
 * Also, you can use Firefox's SQLite Manager to look at and alter
 * the database--works pretty well (but you have to load it every time).
 *
 */
package com.sleepfuriously.hpgworkout;

import java.io.IOException;
import java.util.Calendar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;


/**
 * Most of the methods and data are static--designed to help
 * access the database information.
 *
 * Composes of two parts:
 *
 *	I.	Pertaining to the Exercise Table
 *
 *	II.	Pertaining to the Set Table
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	//-----------------------
	//	Class Constants
	//-----------------------

	/**
	 * The name of the table that lists
	 * all the exercises.
	 */
	public static final String
			EXERCISE_TABLE_NAME = "exercises";

	/**
	 *  The name of the table that holds all the
	 *  workout data.
	 */
	public static final String
			SET_TABLE_NAME = "sets";


	/**
	 * Strings for the column names of the exercise
	 * definition table.
	 * <p>
	 * <b>NOTE</b>:<br />
	 * 		Corresponds to R.arrays.exercise_column_names_array.
	 * 		Update appropriately!
	 */
	public static final String
		COL_ID = "_id",					// for all tables (int)
		EXERCISE_COL_NAME = "name",		// Name of the exercise (string)
		EXERCISE_COL_TYPE = "type",		// Exercise type (int)
		EXERCISE_COL_GROUP = "muscle_group",	// muscle group (int)
		EXERCISE_COL_WEIGHT = "weights",// weights? (bool)
		EXERCISE_COL_WEIGHT_UNIT = "weight_unit",// weight unit (string)
		EXERCISE_COL_REP = "reps", 		// Reps? (bool)
		EXERCISE_COL_DIST = "distanced",// Distances? (bool)
		EXERCISE_COL_DIST_UNIT = "distance_unit",// Distance unit (string)
		EXERCISE_COL_TIME = "timed",	// timed? (bool)
		EXERCISE_COL_TIME_UNIT = "time_unit",	// time unit (string)
		EXERCISE_COL_LEVEL = "level",	// Levels? (bool)
		EXERCISE_COL_CALORIES = "calories",	// count calories? (bool)
		EXERCISE_COL_OTHER = "other",	// misc (bool)
		EXERCISE_COL_OTHER_TITLE = "other_title",// title of other (string)
		EXERCISE_COL_OTHER_UNIT = "other_unit",	// unit (string)
		EXERCISE_COL_SIGNIFICANT = "significant",// which is most significant, 0 = n/a (int)
		EXERCISE_COL_LORDER = "lorder",	// The order it appears (int)
			// New!!!
		EXERCISE_COL_GRAPH_WEIGHT = "g_weight", // Graph the weight aspect? (bool)
		EXERCISE_COL_GRAPH_REPS = "g_reps",		// (bool)
		EXERCISE_COL_GRAPH_DIST = "g_dist",		// (bool)
		EXERCISE_COL_GRAPH_TIME = "g_time",		// (bool)
		EXERCISE_COL_GRAPH_LEVEL = "g_level",	// (bool)
		EXERCISE_COL_GRAPH_CALS = "g_cals",		// (bool)
		EXERCISE_COL_GRAPH_OTHER = "g_other",	// (bool)
		EXERCISE_COL_GRAPH_WITH_REPS = "g_reps_with"; // Graph reps * aspect, the number of the aspect from the next list (int)


	/**
	 * These are the column numbers in the database for
	 * the various column names.
	 *<p>
	 * 	NOTE!
	 * These numbers are also used to determine which column
	 * is the most significant!
	 */
	public static final int
		EXERCISE_COL_ID_NUM = 0,
		EXERCISE_COL_NAME_NUM = 1,
		EXERCISE_COL_TYPE_NUM = 2,
		EXERCISE_COL_GROUP_NUM = 3,
		EXERCISE_COL_WEIGHT_NUM = 4,
		EXERCISE_COL_WEIGHT_UNIT_NUM = 5,
		EXERCISE_COL_REP_NUM = 6,
		EXERCISE_COL_DIST_NUM = 7,
		EXERCISE_COL_DIST_UNIT_NUM = 8,
		EXERCISE_COL_TIME_NUM = 9,
		EXERCISE_COL_TIME_UNIT_NUM = 10,
		EXERCISE_COL_LEVEL_NUM = 11,
		EXERCISE_COL_CALORIE_NUM = 12,
		EXERCISE_COL_OTHER_NUM = 13,
		EXERCISE_COL_OTHER_TITLE_NUM = 14,
		EXERCISE_COL_OTHER_UNIT_NUM = 15,
		EXERCISE_COL_SIGNIFICANT_NUM = 16,
		EXERCISE_COL_LORDER_NUM = 17,
			// new
		EXERCISE_COL_GRAPH_WEIGHT_NUM = 18,
		EXERCISE_COL_GRAPH_REPS_NUM = 19,
		EXERCISE_COL_GRAPH_DIST_NUM = 20,
		EXERCISE_COL_GRAPH_TIME_NUM = 21,
		EXERCISE_COL_GRAPH_LEVEL_NUM = 22,
		EXERCISE_COL_GRAPH_CALS_NUM = 23,
		EXERCISE_COL_GRAPH_OTHER_NUM = 24,
		EXERCISE_COL_GRAPH_REPS_WITH_NUM = 25,	// Refers to one of these numbers (4, 6, 7, 9, 11, 12, 13)

		EXERCISE_COL_NUM_ROWS = 26;		// The number of rows


	/********************
	 * Strings for the column names of the workout table
	 * where each row is a single exercise SET.
	 */
	public static final String
		SET_COL_NAME = "name",	// name of the exercise
		SET_COL_DATEMILLIS = "datemillis",	// time of set (long)
		SET_COL_WEIGHT = "weight",	// weight lifted (real)
		SET_COL_REPS = "reps",	// num reps done (int)
		SET_COL_LEVELS = "level",// level accomplished (int)
		SET_COL_CALORIES = "calories", // calorie count (int)
		SET_COL_DIST = "dist",	// distance travelled (real)
		SET_COL_TIME = "time",	// elapsed time (real)
		SET_COL_OTHER = "other",	// quantity of 'other' units (real)
		SET_COL_CONDITION = "condition",	// +, -, x, or none (int)
		SET_COL_NOTES = "notes";	// string to keep notes (string)

	/********************
	 * Different column data types and other useful strings.
	 */
	public static final String
			TEXT = "TEXT",
			INT = "INTEGER",
			DB_TYPE_REAL = "REAL",
			DB_TYPE_NULL = "NULL",
			DB_TYPE_BLOB = "BLOB",
			DEFAULT = "DEFAULT",
			COMMA = ", ",		// Note that this includes a space
			SEMICOLON = ";",
			SPACE = " ";

	/**********************
	 * String to create the exercise db.  Should match
	 * the constants above, but that makes things a lot
	 * messier.  When everything works, I'll straighten up.
	 */
	private static final String EXERCISE_TABLE_CREATE_STRING =
		"CREATE TABLE " + EXERCISE_TABLE_NAME
		+ " (" + COL_ID + SPACE + INT + SPACE + "PRIMARY KEY AUTOINCREMENT, "
		+ EXERCISE_COL_NAME + SPACE + TEXT + COMMA
		+ EXERCISE_COL_TYPE + SPACE + INT + COMMA
		+ EXERCISE_COL_GROUP + SPACE + INT + COMMA
		+ EXERCISE_COL_WEIGHT + SPACE + INT + COMMA
		+ EXERCISE_COL_WEIGHT_UNIT + SPACE + TEXT + COMMA
		+ EXERCISE_COL_REP + SPACE + INT + COMMA
		+ EXERCISE_COL_DIST + SPACE + INT + COMMA
		+ EXERCISE_COL_DIST_UNIT + SPACE + TEXT + COMMA
		+ EXERCISE_COL_TIME + SPACE + INT + COMMA
		+ EXERCISE_COL_TIME_UNIT + SPACE + TEXT + COMMA
		+ EXERCISE_COL_LEVEL + SPACE + INT + COMMA
		+ EXERCISE_COL_CALORIES + SPACE + INT + COMMA
		+ EXERCISE_COL_OTHER + SPACE + INT + COMMA
		+ EXERCISE_COL_OTHER_TITLE + SPACE + TEXT + COMMA
		+ EXERCISE_COL_OTHER_UNIT + SPACE + TEXT + COMMA
		+ EXERCISE_COL_LORDER + SPACE + INT + COMMA
		+ EXERCISE_COL_SIGNIFICANT + SPACE + INT + COMMA
			// new
		+ EXERCISE_COL_GRAPH_WEIGHT + SPACE + INT + COMMA
		+ EXERCISE_COL_GRAPH_REPS + SPACE + INT + COMMA
		+ EXERCISE_COL_GRAPH_DIST + SPACE + INT + COMMA
		+ EXERCISE_COL_GRAPH_TIME + SPACE + INT + COMMA
		+ EXERCISE_COL_GRAPH_LEVEL + SPACE + INT + COMMA
		+ EXERCISE_COL_GRAPH_CALS + SPACE + INT + COMMA
		+ EXERCISE_COL_GRAPH_OTHER + SPACE + INT + COMMA
		+ EXERCISE_COL_GRAPH_WITH_REPS + SPACE + INT
		+ ");";

	/**
	 * Like the above String, this is used to create a
	 * table in our database. But this one holds the data
	 * of all the sets of workouts the user did.
	 */
	private static final String SET_TABLE_CREATE_STRING =
		"CREATE TABLE " + SET_TABLE_NAME
		+ " (" + COL_ID + SPACE + INT + SPACE + "PRIMARY KEY AUTOINCREMENT, "
		+ SET_COL_NAME + SPACE + TEXT + COMMA
		+ SET_COL_DATEMILLIS + SPACE + INT + COMMA
		+ SET_COL_WEIGHT + SPACE + DB_TYPE_REAL + DEFAULT + "\'-1.0\'" + COMMA
		+ SET_COL_REPS + SPACE + INT + DEFAULT + "\'-1\'" + COMMA
		+ SET_COL_LEVELS + SPACE + INT + DEFAULT + "\'-1\'" + COMMA
		+ SET_COL_CALORIES + SPACE + INT + DEFAULT + "\'-1\'" + COMMA
		+ SET_COL_DIST + SPACE + DB_TYPE_REAL + DEFAULT + "\'-1.0\'" + COMMA
		+ SET_COL_TIME + SPACE + DB_TYPE_REAL + DEFAULT + "\'-1.0\'" + COMMA
		+ SET_COL_OTHER + SPACE + DB_TYPE_REAL + DEFAULT + "\'-1.0\'" + COMMA
		+ SET_COL_CONDITION + SPACE + INT + COMMA
		+ SET_COL_NOTES + SPACE + TEXT + ");";

	/**
	 * This is the SQL string to remove the Set Table.
	 */
	private static final String SET_TABLE_DELETE_STRING =
		"DROP TABLE IF EXISTS " + SET_TABLE_NAME;


	/**
	 * These numbers are used to tell which condition
	 * the exercise set is.
	 */
	public static final int
		SET_COND_NONE = -1,
		SET_COND_OK = 1,
		SET_COND_PLUS = 2,
		SET_COND_MINUS = 3,
		SET_COND_INJURY = 4;

	/** Current version of the Database */
	private static final int DATABASE_VERSION = 6;

	/** We'll try to upgrade if this version is found */
	private static final int DATABASE_VERSION_LAST = 5;

	private static final String tag = "---DatabaseHelper---";

	//-----------------------
	//	Data Members
	//-----------------------

	// Nice to have around.
	private Context m_context = null;

	/** The name of the database file that defines this instance. */
	private String m_filename;


	//-----------------------
	//	Methods
	//-----------------------

	//-----------------------
	//	Constructor
	//
	//	input:
	//		context		The context of the application.
	//					If you're calling from an Activity,
	//					just use 'this'.
	//
	//		name			The name of the database file.  If
	//					null, then the default filename is
	//					used.
	//
	//		factory		Used for creating Cursor objects.
	//					'null' is the default and will create
	//					a new Cursor.
	//
	//		version		The number of the database you want to
	//					open.  If the database file specified
	//					has an older (smaller) number, then
	//					onUpgrade() is automatically called.
	//					If this is 0, then the default is used.
	//
	public DatabaseHelper (Context context,
						String name,
						CursorFactory factory,
						int version) {
		super (context,
			name != null ? name : DatabaseFilesHelper.DB_DEFAULT_FILENAME,
			factory,
			version != 0 ? version : DATABASE_VERSION);

		Log.i(tag, "Called the full database Constructor!!!");
		m_context = context;
		m_filename = name != null ? name : DatabaseFilesHelper.DB_DEFAULT_FILENAME;
	} // constructor


	//-----------------------
	//	Simpler constructor
	//
	//		context		The context of the application.
	//					If you're calling from an Activity,
	//					just use 'this'.
	//
	public DatabaseHelper (Context context) {
		super (context,
			DatabaseFilesHelper.DB_DEFAULT_FILENAME,
			null,
			DATABASE_VERSION);

		Log.i(tag, "Called the minimal database Constructor!!!");
		m_context = context;
		m_filename = DatabaseFilesHelper.DB_DEFAULT_FILENAME;
	}

	//-----------------------
	//	Simpler constructor
	//
	//		context		The context of the application.
	//					If you're calling from an Activity,
	//					just use 'this'.
	//
	//		name			The name of the database file to use.
	//
	public DatabaseHelper (Context context, String name) {
		super (context,
			name,
			null,
			DATABASE_VERSION);

		Log.i(tag, "Called the 2nd minimal Constructor!!!");
		m_context = context;
		m_filename = name;
	}


	/***********************
	 * Returns the name of the file that this instance is
	 * using for its database.
	 */
	public String get_database_filename() {
		return m_filename;
	}



	//--------------------------------------
	//	Both Table Methods
	//--------------------------------------

	//-----------------------
	//	Called when the database is created for the
	//	first time.  Here's our chance to make the database
	//	just the way we want it.
	//
	@Override
	public void onCreate (SQLiteDatabase db) {
		Log.i(tag, "Creating new database file, " + EXERCISE_TABLE_CREATE_STRING);

		db.execSQL (EXERCISE_TABLE_CREATE_STRING);
		init_exercises2 (db);
//		init_exercises (db);

		db.execSQL(SET_TABLE_CREATE_STRING);
		init_sets (db);

	} // onCreate (db)


	//-----------------------
	//	Called when the constructor detects that we tried
	//	to load an old version.  Essentially the file name
	//	that was loaded is older (lower number) than the
	//	one specified in the constructor.  It's up to this
	//	method to fix things.
	//
	@Override
	public void onUpgrade (SQLiteDatabase db,
						int oldVersion,
						int newVersion) {
		Log.w (tag, "WARNING!  Entering onUpgrade(" + db.toString() + ", " + oldVersion + ", " + newVersion + ")");

		// If it's not version 5, delete and start all over.
		if (oldVersion != DATABASE_VERSION_LAST) {
			Log.w(tag, "   The table was very old, deleting and starting fresh.");
			db.execSQL("DROP TABLE IF EXISTS " + EXERCISE_TABLE_NAME);
			onCreate (db);
			db.close();
			return;
		}

		// Yup, this is version 5 (DATABASE_VERSION_LAST).
		// Let's try to update.
		Log.i(tag, "   Trying to update the exercise table (the set table should be unchanged).");
		try {
			db.execSQL("ALTER TABLE " + EXERCISE_TABLE_NAME
					+ " ADD " + EXERCISE_COL_GRAPH_CALS + " " + INT + " DEFAULT 0");
			db.execSQL("ALTER TABLE " + EXERCISE_TABLE_NAME
					+ " ADD " + EXERCISE_COL_GRAPH_DIST + " " + INT + " DEFAULT 0");
			db.execSQL("ALTER TABLE " + EXERCISE_TABLE_NAME
					+ " ADD " + EXERCISE_COL_GRAPH_LEVEL + " " + INT + " DEFAULT 0");
			db.execSQL("ALTER TABLE " + EXERCISE_TABLE_NAME
					+ " ADD " + EXERCISE_COL_GRAPH_OTHER + " " + INT + " DEFAULT 0");
			db.execSQL("ALTER TABLE " + EXERCISE_TABLE_NAME
					+ " ADD " + EXERCISE_COL_GRAPH_REPS + " " + INT + " DEFAULT 0");
			db.execSQL("ALTER TABLE " + EXERCISE_TABLE_NAME
					+ " ADD " + EXERCISE_COL_GRAPH_TIME + " " + INT + " DEFAULT 0");
			db.execSQL("ALTER TABLE " + EXERCISE_TABLE_NAME
					+ " ADD " + EXERCISE_COL_GRAPH_WEIGHT + " " + INT + " DEFAULT 0");
			db.execSQL("ALTER TABLE " + EXERCISE_TABLE_NAME
					+ " ADD " + EXERCISE_COL_GRAPH_WITH_REPS + " " + INT + " DEFAULT -1");

		} catch (SQLException e) {
			Log.e (tag, "Problem adding columns in onUpgrade()!");
			e.printStackTrace();
		}


		// Now that the new columns have been added, go through
		// each exercise and turn on the graphical column
		// that correlates to the significant aspect.
		String[] ex_names = getAllExerciseNamesStrArray(db);

		for (String name : ex_names) {
			int significant = getSignificantExerciseNum(db, name);

			// Find the corresponding graphical aspect.
			int graph_sig = ExerciseData.get_graph_aspect(significant);
			String col_name = get_exercise_string_from_col_num_db(graph_sig);

			ContentValues values = new ContentValues();
			values.put(col_name, true);		// Set that value to true.

			db.update(EXERCISE_TABLE_NAME, values,
						EXERCISE_COL_NAME + "=?", new String[] {name});
		}

	} // onUpgrade (db, old, new)


	//-----------------------
	//	Java-equivalent of a destructor.  But this only works
	//	if the garbage collector is on top of things.  That
	//	is done kind of sporadically, so this may be a whole
	//	bunch of crap.
	//
	@Override
	protected void finalize() throws Throwable {
		Log.i(tag, "Called finalize()!!!");
		super.finalize();
	} // destructor


	/*************************
	 * So you have the number for an exercise column in a db
	 * and would like the string-name for that column, yeah?
	 * Here's the place to get you that info.
	 * <p>
	 * NOTE:  Just for the EXERCISE table, not the SET table!
	 *
	 * @param col	The number of the column that you have.
	 * 				It's the number designated via
	 * 				EXERCISE_COL_..._NUM.
	 *
	 * @return	The string that defines that column.<br/>
	 * 			null if error.
	 */
	public static String get_exercise_string_from_col_num_db (int col) {
		switch (col) {
			case EXERCISE_COL_ID_NUM:
				return COL_ID;
			case EXERCISE_COL_NAME_NUM:
				return EXERCISE_COL_NAME;
			case EXERCISE_COL_TYPE_NUM:
				return EXERCISE_COL_TYPE;
			case EXERCISE_COL_GROUP_NUM:
				return EXERCISE_COL_GROUP;
			case EXERCISE_COL_WEIGHT_NUM:
				return EXERCISE_COL_WEIGHT;
			case EXERCISE_COL_WEIGHT_UNIT_NUM:
				return EXERCISE_COL_WEIGHT_UNIT;
			case EXERCISE_COL_REP_NUM:
				return EXERCISE_COL_REP;
			case EXERCISE_COL_DIST_NUM:
				return EXERCISE_COL_DIST;
			case EXERCISE_COL_DIST_UNIT_NUM:
				return EXERCISE_COL_DIST_UNIT;
			case EXERCISE_COL_TIME_NUM:
				return EXERCISE_COL_TIME;
			case EXERCISE_COL_TIME_UNIT_NUM:
				return EXERCISE_COL_TIME_UNIT;
			case EXERCISE_COL_LEVEL_NUM:
				return EXERCISE_COL_LEVEL;
			case EXERCISE_COL_CALORIE_NUM:
				return EXERCISE_COL_CALORIES;
			case EXERCISE_COL_OTHER_NUM:
				return EXERCISE_COL_OTHER;
			case EXERCISE_COL_OTHER_TITLE_NUM:
				return EXERCISE_COL_OTHER_TITLE;
			case EXERCISE_COL_OTHER_UNIT_NUM:
				return EXERCISE_COL_OTHER_UNIT;
			case EXERCISE_COL_SIGNIFICANT_NUM:
				return EXERCISE_COL_SIGNIFICANT;
			case EXERCISE_COL_LORDER_NUM:
				return EXERCISE_COL_LORDER;

			case EXERCISE_COL_GRAPH_WEIGHT_NUM:
				return EXERCISE_COL_GRAPH_WEIGHT;
			case EXERCISE_COL_GRAPH_REPS_NUM:
				return EXERCISE_COL_GRAPH_REPS;
			case EXERCISE_COL_GRAPH_DIST_NUM:
				return EXERCISE_COL_GRAPH_DIST;
			case EXERCISE_COL_GRAPH_TIME_NUM:
				return EXERCISE_COL_GRAPH_TIME;
			case EXERCISE_COL_GRAPH_LEVEL_NUM:
				return EXERCISE_COL_GRAPH_LEVEL;
			case EXERCISE_COL_GRAPH_CALS_NUM:
				return EXERCISE_COL_GRAPH_CALS;
			case EXERCISE_COL_GRAPH_OTHER_NUM:
				return EXERCISE_COL_GRAPH_OTHER;
			case EXERCISE_COL_GRAPH_REPS_WITH_NUM:
				return EXERCISE_COL_GRAPH_WITH_REPS;
			default:
				Log.e(tag, "Illegal value " + col + " in get_string_from_col_num()!");
				return null;
		}
	} // get_string_from_col_num_db(col)



	/*************************
	 * Use this as a quick way to see what's in the database.
	 *
	 * NOTE:
	 * 		You may want to call startManagingCursor() on the
	 * 		returned Cursor if you plan on having it stay a
	 * 		while.  If not, you better CLOSE() it!!!
	 *
	 * @param db				A database ready to read.
	 *
	 * @param table_name		Which table do we want to get?
	 * 						Supply 'null' to get the default
	 * 						table (Exercise Table).
	 *
	 * @return	A Cursor filled with EVERYTHING!
	 */
	public static Cursor getEntireTable (SQLiteDatabase db,
								final String table_name) {
		String name;
		if (null == table_name)
			name = EXERCISE_TABLE_NAME;
		else
			name = table_name;

		return db.query(name, null, null, null, null, null, null);
	} // getEntireTable (table_name)


	//--------------------------------------
	//	I. Exercise Table Methods
	//--------------------------------------

	/*********************
	 * Called to initially populate the exercise
	 * table with some exercises from the
	 * initial_exercises.xml file.
	 *
	 * Note that the first entry to the table has
	 * an _ID = 1.
	 *
	 * @param db		A write-able database, probably
	 * 				with nothing in it yet (except
	 * 				an exercise table that's already
	 * 				defined).
	 */
	private void init_exercises2 (SQLiteDatabase db) {
		ContentValues values;

		// Get access to resource files.
		Resources res = m_context.getResources();

		// Get an xml parser.
		XmlResourceParser parser = res.getXml(R.xml.initial_exercises);

		try {
			// loop until the end of the xml file
			int event = parser.getEventType();
			int counter = 0;
			while (event != XmlPullParser.END_DOCUMENT) {
				//Search for record tags, which define each exercise
				if ((event == XmlPullParser.START_TAG) &&
					(parser.getName().equals("record"))) {

					values = parse_init_exercise_values(parser, counter);
					db.insert(EXERCISE_TABLE_NAME, null, values);
				}
				event = parser.next();
			} // while
		}

		//Catch (some) errors
		catch (XmlPullParserException e) {
			Log.e(tag, e.getMessage(), e);
		}
		catch (IOException e) {
			Log.e(tag, e.getMessage(), e);
		}
		finally {
			//Close the xml file
			parser.close();
		}

	} // init_exercises (db)


	/************************
	 * Given a parser that's looking at a resource to parse
	 * a sample exercise, this creates a ContentValues and
	 * fills it with all the appropriate data.
	 * <p>
	 * NOTE: This will throw all sorts of exceptions if the
	 * 		data is improperly formatted.
	 * <p>
	 * @param parser		Primed and ready to read the resource.
	 *
	 * @param count		The count for this set of values.  Starts
	 * 					with 0.  Used to make the lorder.
	 *
	 * @return	A ContentValues ready to insert into a database.
	 */
	private ContentValues parse_init_exercise_values (XmlResourceParser parser,
													int count) {
		ContentValues values = new ContentValues();

		//Record tag found, now get values and insert record
		{
			String name = parser.getAttributeValue(null, EXERCISE_COL_NAME);
			values.put(EXERCISE_COL_NAME, name);
		}

		{
			String type_str = parser.getAttributeValue(null, EXERCISE_COL_TYPE);
			int type = Integer.parseInt(type_str);
			values.put(EXERCISE_COL_TYPE, type);
		}

		{
			String group_str = parser.getAttributeValue(null, EXERCISE_COL_GROUP);
			int group = Integer.parseInt(group_str);
			values.put(EXERCISE_COL_GROUP, group);
		}

		{
			String rep_str = parser.getAttributeValue(null, EXERCISE_COL_REP);
			boolean rep = Boolean.parseBoolean(rep_str);
			values.put(EXERCISE_COL_REP, rep);
			boolean g_rep = false;
			if (rep) {
				String g_rep_str = parser.getAttributeValue(null, EXERCISE_COL_GRAPH_REPS);
				g_rep = Boolean.parseBoolean(g_rep_str);
			}
			values.put(EXERCISE_COL_GRAPH_REPS, g_rep);
		}

		{
			String level_str = parser.getAttributeValue(null, EXERCISE_COL_LEVEL);
			boolean level = Boolean.parseBoolean(level_str);
			values.put(EXERCISE_COL_LEVEL, level);
			boolean g_level = false;
			if (level) {
				String g_level_str = parser.getAttributeValue(null, EXERCISE_COL_GRAPH_LEVEL);
				g_level = Boolean.parseBoolean(g_level_str);
			}
			values.put(EXERCISE_COL_GRAPH_LEVEL, g_level);
		}

		{
			String cals_str = parser.getAttributeValue(null, EXERCISE_COL_CALORIES);
			boolean cals = Boolean.parseBoolean(cals_str);
			values.put(EXERCISE_COL_CALORIES, cals);
			boolean g_cals = false;
			if (cals) {
				String g_cals_str = parser.getAttributeValue(null, EXERCISE_COL_GRAPH_CALS);
				g_cals = Boolean.parseBoolean(g_cals_str);
			}
			values.put(EXERCISE_COL_GRAPH_CALS, g_cals);
		}

		{
			String weight_str = parser.getAttributeValue(null, EXERCISE_COL_WEIGHT);
			boolean weight = Boolean.parseBoolean(weight_str);
			values.put(EXERCISE_COL_WEIGHT, weight);

			String weight_unit = "";
			boolean g_weight = false;

			if (weight) {
				weight_unit = parser.getAttributeValue(null, EXERCISE_COL_WEIGHT_UNIT);

				String g_weight_str = parser.getAttributeValue(null, EXERCISE_COL_GRAPH_WEIGHT);
				g_weight = Boolean.parseBoolean(g_weight_str);
			}
			values.put(EXERCISE_COL_WEIGHT_UNIT, weight_unit);
			values.put(EXERCISE_COL_GRAPH_WEIGHT, g_weight);
		}

		{
			String dist_str = parser.getAttributeValue(null, EXERCISE_COL_DIST);
			boolean dist = Boolean.parseBoolean(dist_str);
			values.put(EXERCISE_COL_DIST, dist);

			String dist_unit = "";
			boolean g_dist = false;

			if (dist) {
				dist_unit = parser.getAttributeValue(null, EXERCISE_COL_DIST_UNIT);

				String g_dist_str = parser.getAttributeValue(null, EXERCISE_COL_GRAPH_DIST);
				g_dist = Boolean.parseBoolean(g_dist_str);
			}
			values.put(EXERCISE_COL_DIST_UNIT, dist_unit);
			values.put(EXERCISE_COL_GRAPH_DIST, g_dist);
		}

		{
			String time_str = parser.getAttributeValue(null, EXERCISE_COL_TIME);
			boolean time = Boolean.parseBoolean(time_str);
			values.put(EXERCISE_COL_TIME, time);

			String time_unit = "";
			boolean g_time = false;

			if (time) {
				time_unit = parser.getAttributeValue(null, EXERCISE_COL_TIME_UNIT);

				String g_time_str = parser.getAttributeValue(null, EXERCISE_COL_GRAPH_TIME);
				g_time = Boolean.parseBoolean(g_time_str);
			}
			values.put(EXERCISE_COL_TIME_UNIT, time_unit);
			values.put(EXERCISE_COL_GRAPH_TIME, g_time);
		}

		{
			String other_str = parser.getAttributeValue(null, EXERCISE_COL_OTHER);
			boolean other = Boolean.parseBoolean(other_str);
			values.put(EXERCISE_COL_OTHER, other);

			String other_title = "";
			String other_unit = "";
			boolean g_other = false;

			if (other) {
				other_title = parser.getAttributeValue(null, EXERCISE_COL_OTHER_TITLE);
				other_unit = parser.getAttributeValue(null, EXERCISE_COL_OTHER_UNIT);

				String g_other_str = parser.getAttributeValue(null, EXERCISE_COL_GRAPH_OTHER);
				g_other = Boolean.parseBoolean(g_other_str);
			}
			values.put(EXERCISE_COL_OTHER_TITLE, other_title);
			values.put(EXERCISE_COL_OTHER_UNIT, other_unit);
			values.put(EXERCISE_COL_GRAPH_OTHER, g_other);
		}

		{
//			String lorder_str = parser.getAttributeValue(null, EXERCISE_COL_LORDER);
//			int lorder = Integer.parseInt(lorder_str);
			values.put(EXERCISE_COL_LORDER, count);
		}

		{
			String sig_str = parser.getAttributeValue(null, EXERCISE_COL_SIGNIFICANT);
			int sig = -1;
			if (sig_str.equals(EXERCISE_COL_REP)) {
				sig = EXERCISE_COL_REP_NUM;
			}
			else if (sig_str.equals(EXERCISE_COL_LEVEL)) {
				sig = EXERCISE_COL_LEVEL_NUM;
			}
			else if (sig_str.equals(EXERCISE_COL_CALORIES)) {
				sig = EXERCISE_COL_CALORIE_NUM;
			}
			else if (sig_str.equals(EXERCISE_COL_WEIGHT)) {
				sig = EXERCISE_COL_WEIGHT_NUM;
			}
			else if (sig_str.equals(EXERCISE_COL_DIST)) {
				sig = EXERCISE_COL_DIST_NUM;
			}
			else if (sig_str.equals(EXERCISE_COL_TIME)) {
				sig = EXERCISE_COL_TIME_NUM;
			}
			else if (sig_str.equals(EXERCISE_COL_OTHER)) {
				sig = EXERCISE_COL_OTHER_NUM;
			}
			else {
				Log.e(tag, "Hey, could not figure out the significant exercise in parse_init_exercise with the string '" + sig_str + "'.");
			}
//			Log.d(tag, "parse_init_exercise_values(), sig_str = " + sig_str + ", sig = " + sig);
			values.put(EXERCISE_COL_SIGNIFICANT, sig);
		}

		{
			String graph_with_reps_str = parser.getAttributeValue(null, EXERCISE_COL_GRAPH_WITH_REPS);
			int with_reps = Integer.parseInt(graph_with_reps_str);
			values.put(EXERCISE_COL_GRAPH_WITH_REPS, with_reps);
		}

		return values;
	} // parse_init_exercise_values (parser)


	/**************************
	 * Returns a Cursor loaded with all the names of the
	 * exercises.  They'll be sorted in the order that the
	 * user wants them.
	 *
	 * NOTE:
	 * 		You may want to call startManagingCursor() on the
	 * 		returned Cursor if you plan on having it stay a
	 * 		while.  If not, you better CLOSE() it!!!
	 *
	 * @param db 	A database ready to read from.
	 *
	 * @return	A Cursor with all the names.  It could be empty.
	 * 			Oh yeah, and it's pointing to BEFORE the first
	 * 			one, so you gotta to cursor.moveToFirst() or
	 * 			something to get any data.  YOU'VE BEEN WARNED!
	 */
	public static Cursor getAllExerciseNames (SQLiteDatabase db) {
		return db.query(EXERCISE_TABLE_NAME,	// table
			new String[] {EXERCISE_COL_NAME},//	columns[]
			null,	//selection
			null,	// selectionArgs[]
			null,	//	groupBy
			null,	//	having
			EXERCISE_COL_LORDER,	//	orderBy
			null);	//	limit
	} // getAllExerciseNames (activity)

	/***************************
	 * Like the above, but returns an array of Strings instead
	 * of a Cursor.  That way you don't need to worry about it.
	 *
	 * @param db		A database primed for reading.
	 *
	 * @return		An array of the names, ordered by LORDER.
	 *
	 * @see #getAllExerciseNames()
	 */
	public static String[] getAllExerciseNamesStrArray (SQLiteDatabase db) {
		int i, col;
		Cursor c = db.query(EXERCISE_TABLE_NAME,	// table
							new String[] {EXERCISE_COL_NAME},//	columns[]
								null,	//selection
								null,	// selectionArgs[]
								null,	//	groupBy
								null,	//	having
								EXERCISE_COL_LORDER,	//	orderBy
								null);	//	limit

		String name_array[] = new String[c.getCount()];
		i = 0;
		col = c.getColumnIndex(EXERCISE_COL_NAME);
		while (c.moveToNext()) {
			name_array[i++] = c.getString(col);
		}

		c.close();

		return name_array;
	} // getAllExerciseNameStrArray (db)


	/***************************
	 * Similar to above, but this is the super-easy version.
	 * You don't need to do ANY database stuff.  It's all done
	 * here, hidden from view.
	 * Again, all the names of the exercises are in an array,
	 * ordered in the user's preference.
	 *
	 * @return	An array of Strings.  Could have a size 0.
	 *
	 * @see #getAllExerciseNames()
	 */
	public static String[] getAllExerciseNames() {
		int i, col;

		SQLiteDatabase db = WGlobals.g_db_helper.getReadableDatabase();
		Cursor c = db.query(EXERCISE_TABLE_NAME,	// table
			new String[] {EXERCISE_COL_NAME},//	columns[]
			null,	//selection
			null,	// selectionArgs[]
			null,	//	groupBy
			null,	//	having
			EXERCISE_COL_LORDER,	//	orderBy
			null);	//	limit

		String name_array[] = new String[c.getCount()];
		i = 0;
		col = c.getColumnIndex(EXERCISE_COL_NAME);
		while (c.moveToNext()) {
			name_array[i++] = c.getString(col);
		}

		c.close();
		db.close();
		return name_array;
	} // getAllExerciseNames()


	/***************************
	 * This is a helper method.  It grabs ALL the rows of the
	 * exercise table, but only the specified columns.
	 *
	 * NOTE:
	 * 		The caller should call startManagingCursor() on the
	 * 		returned cursor if they plan for it to stick around
	 * 		a while.  If not, you better CLOSE() it!!!
	 *
	 * @param db			A database ready to read from.
	 *
	 * @param columns	An array of Strings, each item is the
	 * 					column identifier string.
	 * 					Null will give all columns.
	 *
	 * @param order		The String that is the column identifier
	 * 					to sort the list by.  EXERCISE_COL_LORDER
	 * 					is popular.
	 * 					Use 'null' for no ordering.
	 *
	 * @return	A Cursor for you to use.  Could be empty.  But
	 * 			it'll be pointing BEFORE the first entry.
	 * 			YOU'VE BEEN WARNED!
	 */
	public Cursor getAllExerciseRows (SQLiteDatabase db,
									String[] columns,
									String order) {
		return db.query(EXERCISE_TABLE_NAME,	// table
			columns,//	columns[]
			null,	//selection
			null,	// selectionArgs[]
			null,	//	groupBy
			null,	//	having
			order,	//	orderBy
			null);	//	limit
	} // getAllExerciseRows

	/***************************
	 * An easy way to get the significant exercise number.
	 *
	 * @param db			A database ready to read from.
	 *
	 * @param	The name of the exercise.
	 *
	 * @return	The number of the significant aspect of
	 * 			the exercise.  See EXERCISE_COL_*_NUM for
	 * 			the actual numbers.
	 * 			-1 if there's a problem
	 */
	public static int getSignificantExerciseNum (
							SQLiteDatabase db,
							String ex_name) {
		int col, sig = -1;

		Cursor c = null;
		try {
			c = db.query(
					EXERCISE_TABLE_NAME,	// table
					null,			//	columns[]
					DatabaseHelper.EXERCISE_COL_NAME + "=?",//selection
					new String[] {ex_name},// selectionArgs[]
					null,	//	groupBy
					null,	//	having
					null,	//	orderBy
					null);

			if (c.moveToFirst()) {
				col = c.getColumnIndex(EXERCISE_COL_SIGNIFICANT);
				sig = c.getInt(col);
			}
		}
		catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}

		return sig;
	} // getSignificantExerciseNum (ex_name)


	/****************************
	 * Returns a cursor with all the info about the named
	 * exercise.
	 *
	 * NOTES:
	 * 	- YOU BETTER CLOSE THAT CURSOR!!! Yeah, you've been warned.
	 *
	 *  - Don't forget to call Cursor.moveToFirst() !!!
	 *
	 * @param db		Ready to read.
	 * @param name	The name of the exercise
	 * @return	A cursor with one row (not set to it!).
	 * 			NULL if there was an error.
	 */
	public static Cursor getAllExerciseInfoByName (SQLiteDatabase db,
												String name) {
		return db.query(EXERCISE_TABLE_NAME,
				null,
				EXERCISE_COL_NAME + "=?",
				new String[] {name},// selectionArgs[]
				null,
				null,
				null);
	} // getAllExerciseInfoByName (db, name)

	/****************************
	 * Given a Cursor thats initialized AND pointing to an
	 * exercise row (not BEFORE), this creates and fills in
	 * an ExerciseData instance.
	 *
	 * preconditions:
	 * 		This *really* should be called within a try/catch
	 * 		block.  Do that anytime you fill in a Cursor from
	 * 		a database.  It's just good sense.
	 *
	 * @param cursor		Pointing to the exercise to fill in (and
	 * 					NOT before).  Also, ALL fields have been
	 * 					read in from the database!!!  Otherwise
	 * 					an exception will be thrown!
	 *
	 * @return	- A class instance of ExerciseData with all the
	 * 			fields filled in.
	 * 			- NULL if an error happened (detailed above).
	 */
	public static ExerciseData getExerciseData (Cursor cursor) {
		int col;
		ExerciseData data = new ExerciseData();

		// now load up the data:
		col = cursor.getColumnIndex(COL_ID);
		data._id = cursor.getInt(col);

		col = cursor.getColumnIndex(EXERCISE_COL_NAME);
		data.name = cursor.getString(col);

		col = cursor.getColumnIndex(EXERCISE_COL_TYPE);
		data.type = cursor.getInt(col);

		col = cursor.getColumnIndex(EXERCISE_COL_GROUP);
		data.group = cursor.getInt(col);

		col = cursor.getColumnIndex(EXERCISE_COL_WEIGHT);
		data.bweight = cursor.getInt(col) == 1 ? true : false;
		if (data.bweight) {
			col = cursor.getColumnIndex(EXERCISE_COL_WEIGHT_UNIT);
			data.weight_unit = cursor.getString(col);
			col = cursor.getColumnIndex(EXERCISE_COL_GRAPH_WEIGHT);
			data.g_weight = cursor.getInt(col) == 1 ? true : false;
		}

		col = cursor.getColumnIndex(EXERCISE_COL_REP);
		data.breps = cursor.getInt(col) == 1 ? true : false;
		col = cursor.getColumnIndex(EXERCISE_COL_GRAPH_REPS);
		data.g_reps = cursor.getInt(col) == 1 ? true : false;

		col = cursor.getColumnIndex(EXERCISE_COL_DIST);
		data.bdist = cursor.getInt(col) == 1 ? true : false;
		if (data.bdist) {
			col = cursor.getColumnIndex(EXERCISE_COL_DIST_UNIT);
			data.dist_unit = cursor.getString(col);
			col = cursor.getColumnIndex(EXERCISE_COL_GRAPH_DIST);
			data.g_dist = cursor.getInt(col) == 1 ? true : false;
		}

		col = cursor.getColumnIndex(EXERCISE_COL_TIME);
		data.btime = cursor.getInt(col) == 1 ? true : false;
		if (data.btime) {
			col = cursor.getColumnIndex(EXERCISE_COL_TIME_UNIT);
			data.time_unit = cursor.getString(col);
			col = cursor.getColumnIndex(EXERCISE_COL_GRAPH_TIME);
			data.g_time = cursor.getInt(col) == 1 ? true : false;
		}

		col = cursor.getColumnIndex(EXERCISE_COL_LEVEL);
		data.blevel = cursor.getInt(col) == 1 ? true : false;
		col = cursor.getColumnIndex(EXERCISE_COL_GRAPH_LEVEL);
		data.g_level = cursor.getInt(col) == 1 ? true : false;

		col = cursor.getColumnIndex(EXERCISE_COL_CALORIES);
		data.bcals = cursor.getInt(col) == 1 ? true : false;
		col = cursor.getColumnIndex(EXERCISE_COL_GRAPH_CALS);
		data.g_cals = cursor.getInt(col) == 1 ? true : false;

		col = cursor.getColumnIndex(EXERCISE_COL_OTHER);
		data.bother = cursor.getInt(col) == 1 ? true : false;
		if (data.bother) {
			col = cursor.getColumnIndex(EXERCISE_COL_OTHER_TITLE);
			data.other_title = cursor.getString(col);
			col = cursor.getColumnIndex(EXERCISE_COL_OTHER_UNIT);
			data.other_unit = cursor.getString(col);
			col = cursor.getColumnIndex(EXERCISE_COL_GRAPH_OTHER);
			data.g_other = cursor.getInt(col) == 1 ? true : false;
		}
		col = cursor.getColumnIndex(EXERCISE_COL_SIGNIFICANT);
		data.significant = cursor.getInt(col);

		col = cursor.getColumnIndex(EXERCISE_COL_LORDER);
		data.lorder = cursor.getInt(col);

		col = cursor.getColumnIndex(EXERCISE_COL_GRAPH_WITH_REPS);
		data.g_with_reps = cursor.getInt(col);

		return data;
	} // getExerciseData (cursor)


	/****************************
	 * Returns an ExerciseData instance with all the info filled
	 * in for the named exercise.  All you need is the name of
	 * the exercise (if there's more than one...I don't know what
	 * will happen).
	 *
	 * Use THIS method when loading up all the info for a single
	 * exercise.  Really!  That way, you don't have to worry about
	 * that damn cursor hanging around.
	 *
	 * @param db			DB ready to read.
	 * @param name		The name of the exercise.
	 * @return			- A class instance holding all the info about
	 * 					this exercise.
	 * 					- NULL if an error or the name could not be found.
	 */
	public static ExerciseData getExerciseData (SQLiteDatabase db,
												String name) {

		Cursor cursor = null;
		ExerciseData data = null;

		try {
			cursor = db.query(EXERCISE_TABLE_NAME,
							null,
							EXERCISE_COL_NAME + "=?",
							new String[] {name},// selectionArgs[]
							null,
							null,
							null);
			cursor.moveToFirst();	// Oh so important!

			// This is IT!  Yes, that's all there is, wheee!
			data = getExerciseData(cursor);
		}

		catch (SQLiteException e) {
			e.printStackTrace();
		}

		finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return data;
	} // getExerciseData (db, name)


	/****************************
	 * Returns an ExerciseData instance with all the info filled
	 * in for the named exercise.  This version needs the _ID of
	 * the exercise in question.
	 *
	 * Use THIS method when loading up all the info for a single
	 * exercise.  Really!  That way, you don't have to worry about
	 * that damn cursor hanging around.
	 *
	 * @param db			DB ready to read.
	 * @param name		The name of the exercise.
	 * @return			- A class instance holding all the info about
	 * 					this exercise.
	 * 					- NULL if an error or the name could not be found.
	 */
	public static ExerciseData getExerciseData (SQLiteDatabase db,
												int id) {
		Cursor cursor = null;
		ExerciseData data = null;

		try {
			cursor = db.query(EXERCISE_TABLE_NAME,
							null,
							COL_ID + "=?",
							new String[] {"" + id},// selectionArgs[]
							null,
							null,
							null);
			cursor.moveToFirst();	// Oh so important!

			// This is IT!  Yes, that's all there is, wheee!
			data = getExerciseData(cursor);
		}

		catch (SQLiteException e) {
			e.printStackTrace();
		}

		finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return data;

	} // getExerciseData (db, id)


	/****************************
	 * Returns a cursor with all the info about the named
	 * exercise.  This is the same as above, but the exercise
	 * is referred to by its column id, not its name.
	 *
	 * NOTE:
	 * 	YOU BETTER CLOSE THAT CURSOR!!! Yeah, you've been warned.
	 *
	 * @param db		Ready to read.
	 * @param name	The order (as the user sees it) of the row.
	 * @return	A cursor with one row (not set to it!).
	 * 			NULL if there was an error.
	 */
	public static Cursor getAllExerciseInfoByOrder (SQLiteDatabase db,
											int lorder) {
		return db.query(EXERCISE_TABLE_NAME,	// table
				null,			//	columns[] -- all of 'em
				DatabaseHelper.EXERCISE_COL_LORDER + "=" + lorder,//selection
				null,// selectionArgs[]
				null,	//	groupBy
				null,	//	having
				null);	//	orderBy
	} // getAllExerciseInfoByOrder (db, lorder)


	/**********************
	 * Given an LOrder (the row that an exercise appears on,
	 * as ordered by the user), this returns that exercise's
	 * name.
	 *
	 * @param db			A database ready for reading.
	 * @param lorder		The row that this exercise appears to
	 * 					the user (it's LOrder in the database).
	 *
	 * @return	The name of the exercise that corresponds to this
	 * 			row.
	 * 			Null, if an error occurs.
	 */
	public static String getNameFromLOrder (SQLiteDatabase db,
											int lorder) {
		String name = null;
		Cursor c = null;

		try {
			c =  db.query(EXERCISE_TABLE_NAME,	// table
					new String[] {EXERCISE_COL_NAME},	// just the name column
					EXERCISE_COL_LORDER + "=" + lorder,//selection
					null,// selectionArgs[]
					null,	//	groupBy
					null,	//	having
					null);	//	orderBy
			c.moveToFirst();
			int col = c.getColumnIndex(EXERCISE_COL_NAME);
			if (col == -1)
				Log.e(tag, "Illegal column in getNameFromLorder() - lorder = " + lorder);
			else
				name = c.getString(col);
		}
		catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}

		return name;
	} // getNameFromLOrder (db, lorder)

	/**************************
	 * Given an LOrder (the row that an exercise appears on,
	 * as ordered by the user), this returns that exercise's
	 * name.
	 *
	 * This is an even easier version--you don't need to
	 * have a db variable lying around for this one.
	 *
	 * @param db			A database ready for reading.
	 * @param lorder		The row that this exercise appears to
	 * 					the user (it's LOrder in the database).
	 *
	 * @return	The name of the exercise that corresponds to this
	 * 			row.
	 * 			Null, if an error occurs.
	 */
	public static String getNameFromLOrder (int lorder) {
		SQLiteDatabase db = null;
		String name = null;

		try {
			db = WGlobals.g_db_helper.getReadableDatabase();
			name = getNameFromLOrder(db, lorder);
		}
		catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (db != null) {
				db.close();
				db = null;
			}
		}
		return name;
	} // getNameFromLOrder (lorder)


	/********************
	 * Goes through the database and sees if there is an
	 * exercise with the given name.
	 * <p>
	 * NOTE:
	 * 	Case is ignored!!!
	 *
	 * @param db		A database ready for reading.
	 *
	 * @param name	The name to test against the DB
	 *
	 * @return	TRUE iff the name is identical to an
	 * 			existing name.
	 */
	public static boolean isExerciseNameExist (SQLiteDatabase db, String name) {
		int col;
		boolean ret_val = false;

		Cursor cursor = null;
		try {
			cursor = db.query(
					DatabaseHelper.EXERCISE_TABLE_NAME,	// table
					new String[] {DatabaseHelper.EXERCISE_COL_NAME},
					null,//selection
					null,// selectionArgs[]
					null,	//	groupBy
					null,	//	having
					null,	//	orderBy
					null);	//	limit

			while (cursor.moveToNext()) {
				col = cursor.getColumnIndex(DatabaseHelper.EXERCISE_COL_NAME);
				if (cursor.getString(col).equalsIgnoreCase(name)) {
					ret_val = true;
				}
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		return ret_val;
	} // is_duplicate_name (name)


	//--------------------------------------
	//	II. Set Table Methods
	//--------------------------------------

	/********************
	 * Initializes the set table.
	 */
	private void init_sets (SQLiteDatabase db) {
		ContentValues values = new ContentValues();

		Calendar time = Calendar.getInstance();
		long millis = time.getTimeInMillis();

		values.clear();
		values.put(SET_COL_DATEMILLIS, millis);
		values.put(SET_COL_NAME, "squats");
		values.put(SET_COL_REPS, 10);
		values.put(SET_COL_WEIGHT, 400f);
		values.put(SET_COL_CONDITION, SET_COND_OK);
		db.insert(SET_TABLE_NAME, null, values);


		time.add(Calendar.DAY_OF_MONTH, -4);
		millis = time.getTimeInMillis();

		values.clear();
		values.put(SET_COL_DATEMILLIS, millis);
		values.put(SET_COL_NAME, "squats");
		values.put(SET_COL_REPS, 10);
		values.put(SET_COL_WEIGHT, 200f);
		values.put(SET_COL_CONDITION, SET_COND_OK);
		db.insert(SET_TABLE_NAME, null, values);

		time.add(Calendar.DAY_OF_MONTH, -12);
		millis = time.getTimeInMillis();

		values.clear();
		values.put(SET_COL_DATEMILLIS, millis);
		values.put(SET_COL_NAME, "squats");
		values.put(SET_COL_REPS, 10);
		values.put(SET_COL_WEIGHT, 100f);
		values.put(SET_COL_CONDITION, SET_COND_OK);
		db.insert(SET_TABLE_NAME, null, values);
	} // init_sets (db)


	/************************
	 * Finds out how many sets are in a given exercise.  Note that
	 * this is pretty computationally expensive.  If you're going
	 * to make a Cursor from this set, you might as well do that
	 * and then call Cursor.getCount(), which would save you a lot
	 * of time.
	 *
	 * @param db		A database ready for reading.
	 * @param name	The name of the exercise.
	 * @return	The number of exercise sets for the specified
	 * 			exercise.
	 * 			-1 if couldn't find anything for the given name.
	 */
	public static int getNumSets (SQLiteDatabase db, String name) {
		Cursor c = null;
		int ret_val = -1;
		try {
			c = db.query(SET_TABLE_NAME,
						null,
						SET_COL_NAME + "=?",
						new String[] {"" + name},
						null,
						null,
						null);
			ret_val = c.getCount();
		}
		catch (SQLiteException e) {
			Log.v(tag, "getNumSets() can't find any sets for exercise " + name + "!");
		}
		finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
		return ret_val;
	} // getNumSets (db, name)


	/************************
	 * This returns the most recently entered set (sorted in
	 * descending order by date) for a given exercise.
	 *
	 * NOTE:
	 * 	YOU BETTER CLOSE THAT CURSOR!!! Yeah, you've been warned.
	 * 	Or...if you want this cursor to stick around for a while,
	 *  call startManagingCursor() so that the Activity takes care
	 *  of it.
	 *
	 * NOTE 2:
	 *	It's a GREAT idea to enclose this in a try/catch block
	 *	(catch with SQLiteException).  Yeah, that'd do it.
	 *
	 * @param db		A database ready for reading.
	 * @param name	The name of the exercise.
	 * @return	A cursor with one row: all the columns of
	 * 			this particular exercise set.
	 * 			This Cursor could be empty (error)!!!  If not,
	 * 			then it starts already moved to the item.
	 */
	public static Cursor getLastSet (SQLiteDatabase db,
									String name) {
		Cursor c = db.query(SET_TABLE_NAME,
				null,
				SET_COL_NAME + "=?",
				new String[] {"" + name},
				null,
				null,
				SET_COL_DATEMILLIS + " DESC", 	// descending by date
				"1");	// Just one.
		c.moveToFirst();		// A courtesy.
		return c;
	} // getLastSet (db, name)

	/*************************
	 * Returns a cursor with with ALL the sets of the named
	 * exercise.
	 *
	 * NOTE:
	 * 	YOU BETTER CLOSE THAT CURSOR!!! Yeah, you've been warned.
	 * 	Or...if you want this cursor to stick around for a while,
	 *  call startManagingCursor() so that the Activity takes care
	 *  of it.
	 *
	 * @param db		A database ready for reading.
	 * @param name	The name of the exercise.
	 * @param descend	true - order ascending, with oldest first.
	 * 					false - descending order, with newest first.
	 * @return	A cursor with all the sets: all the columns of
	 * 			this particular exercise set.
	 * 			This Cursor could be empty!!!
	 */
	public static Cursor getAllSets (SQLiteDatabase db,
									String name, boolean descend) {
		return db.query(SET_TABLE_NAME,
				null,
				SET_COL_NAME + "=?",
				new String[] {"" + name},
				null,
				null,
				SET_COL_DATEMILLIS + (descend ? " ASC" : " DESC")); // date ordered
	} // getAllSets (db, name, descend)


	/****************************
	 * Given a Cursor thats initialized AND pointing to an
	 * workout set's row (not BEFORE), this creates and fills
	 * in a SetData instance.
	 *
	 * preconditions:
	 * 		This *really* should be called within a try/catch
	 * 		block.  Do that anytime you fill in a Cursor from
	 * 		a database.  It's just good sense.
	 *
	 * @param cursor		Pointing to the set to fill in (and
	 * 					NOT before).  Also, ALL fields have
	 * 					been read in from the database!!!
	 * 					Otherwise an exception will be thrown!
	 *
	 * @return	- A class instance of SetData with all the
	 * 			fields filled in.
	 * 			- NULL if an error happened (detailed above).
	 */
	public static SetData getSetData (Cursor cursor) {
		int col;
		SetData data = new SetData();

		// now load up the data:
		col = cursor.getColumnIndex(COL_ID);
		data._id = cursor.getInt(col);

		col = cursor.getColumnIndex(SET_COL_NAME);
		data.name = cursor.getString(col);

		col = cursor.getColumnIndex(SET_COL_DATEMILLIS);
		data.millis = cursor.getLong(col);

		col = cursor.getColumnIndex(SET_COL_WEIGHT);
		data.weight = cursor.getFloat(col);

		col = cursor.getColumnIndex(SET_COL_REPS);
		data.reps = cursor.getInt(col);

		col = cursor.getColumnIndex(SET_COL_LEVELS);
		data.levels = cursor.getInt(col);

		col = cursor.getColumnIndex(SET_COL_CALORIES);
		data.cals = cursor.getInt(col);

		col = cursor.getColumnIndex(SET_COL_DIST);
		data.dist = cursor.getFloat(col);

		col = cursor.getColumnIndex(SET_COL_TIME);
		data.time = cursor.getFloat(col);

		col = cursor.getColumnIndex(SET_COL_OTHER);
		data.other = cursor.getFloat(col);

		col = cursor.getColumnIndex(SET_COL_CONDITION);
		data.cond = cursor.getInt(col);

		col = cursor.getColumnIndex(SET_COL_NOTES);
		data.notes = cursor.getString(col);

		return data;
	}  // getSetData(cursor)


	/****************************
	 * Yeah, just what you think it does.  The entire set table is
	 * completely deleted.  It'll be just like starting over (except
	 * that the exercises will still be there).
	 */
	public void remove_all_set_data() {
		SQLiteDatabase db = null;

		try {
			db = getWritableDatabase();

			// Just remove the set table, re-create, and re-initialize.
			db.execSQL(SET_TABLE_DELETE_STRING);
			db.execSQL(SET_TABLE_CREATE_STRING);
			init_sets(db);

		} catch (SQLiteException e) {
			Log.e(tag, "Problem in remove_all_set_data(); can't open database for writing!");
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
				db = null;
			}
		}

	} // remove_all_set_data()

}
