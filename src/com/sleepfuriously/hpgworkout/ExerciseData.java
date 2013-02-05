/**
 * This is just a data class to encapsulate all the
 * information about a particular exercise.
 */
package com.sleepfuriously.hpgworkout;

import android.util.Log;

public class ExerciseData {

	private static final String tag = "ExerciseData";

	/** The DB id for this exercise */
	int _id;

	/** The name of the exercise */
	String name;

	/**
	 * A number indicating the exercise type (defined in arrays.xml)
	 */
	int type;

	/** The muscle group (defined in arrays.xml) */
	int group;

	/** These booleans determine if the given aspect is valid for
	 *  this exercise.
	 */
	boolean bweight, breps, bdist, btime, blevel, bcals, bother;

	String weight_unit, dist_unit, time_unit, other_title, other_unit;

	/** Which aspect is the most significant?  0 = n/a */
	int significant;

	/** The order this exercise appears in the list of all exercises. */
	int lorder;

	//----------------
	//	Graphing Info
	//----------------

	/**
	 * Whether or not to graph the given aspect.
	 * Defaults to false.
	 */
	boolean g_reps = false,
			g_weight = false,
			g_dist = false,
			g_time = false,
			g_level = false,
			g_cals = false,
			g_other = false;

	/**
	 * Do we multiply the number of reps with another
	 * aspect?  If so, then which aspect?  This number
	 * can be found in DatabaseHelper.EXERCISE_COL_*_NUM.
	 * <p>
	 * -1 means that this is not used.
	 */
	int g_with_reps = -1;


	//----------------
	//	Static Methods
	//----------------

	/**************************
	 * Goes through all the aspects of an exercise (like
	 * reps, weight, time, etc.) and counts how many are
	 * actually used for this exercise.
	 *
	 * @param data	The ExericiseData to look at.
	 *
	 * @return The number of entries .b____ that are true.
	 */
	public static int count_valid_aspects (ExerciseData data) {
		return	(data.bcals ? 1 : 0) +
				(data.breps ? 1 : 0) +
				(data.blevel ? 1 : 0) +
				(data.bweight ? 1 : 0) +
				(data.bdist ? 1 : 0) +
				(data.btime ? 1 : 0) +
				(data.bother ? 1 : 0);
	} // count_valid_aspects (data)

	/**************************
	 * Goes through all the parts of an exercise (like
	 * reps, weight, time, etc.) and counts how many are
	 * turned on for graphing.
	 *
	 * @param data	The ExericiseData to look at.
	 *
	 * @return The number of entries .g_xxx that are true.
	 */
	public static int count_valid_graph_aspects (ExerciseData data) {
		return	(data.g_reps ? 1 : 0) +
				(data.g_cals ? 1 : 0) +
				(data.g_level ? 1 : 0) +
				(data.g_weight ? 1 : 0) +
				(data.g_dist ? 1 : 0) +
				(data.g_time ? 1 : 0) +
				(data.g_other ? 1 : 0);
	} // count_valid_aspects (data)


	/*************************
	 * Another weird one. Suppose you have a number, like
	 * 6 (which is the EXERCISE_COL_REPS_NUM), and you want
	 * to know what is the corresponding number for the
	 * graphical component (EXERCISE_COL_GRAPH_REPS_NUM).
	 * This is your baby.
	 * <p>
	 * This static method takes one of those numbers and
	 * throws out the corresponding one.
	 *
	 * @param num	A number of an aspect as taken from
	 * 				a database column (defined in
	 * 				DatabaseHelper.java).
	 *
	 * @return	Another number: the column of the graphical
	 * 			aspect that is relevant.  _REPS will give
	 * 			you _GRAPH_REPS, and so on.<br/>
	 * 			-1 if there's an error (can't find the
	 * 			number).
	 */
	public static int get_graph_aspect (int num) {
		switch (num) {
			case DatabaseHelper.EXERCISE_COL_REP_NUM:
				return DatabaseHelper.EXERCISE_COL_GRAPH_REPS_NUM;
			case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
				return DatabaseHelper.EXERCISE_COL_GRAPH_LEVEL_NUM;
			case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
				return DatabaseHelper.EXERCISE_COL_GRAPH_CALS_NUM;
			case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
				return DatabaseHelper.EXERCISE_COL_GRAPH_WEIGHT_NUM;
			case DatabaseHelper.EXERCISE_COL_DIST_NUM:
				return DatabaseHelper.EXERCISE_COL_GRAPH_DIST_NUM;
			case DatabaseHelper.EXERCISE_COL_TIME_NUM:
				return DatabaseHelper.EXERCISE_COL_GRAPH_TIME_NUM;
			case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
				return DatabaseHelper.EXERCISE_COL_GRAPH_OTHER_NUM;
		}
		Log.e(tag, "Illegal value in get_graph_aspect(" + num + ")!");
		return -1;
	} // get_graph_aspect (data, num)


	//----------------
	//	Methods
	//----------------

	/**************************
	 * Kind of Complicated, sorry.  This sets one of
	 * aspects to true or false.  Which aspect? that's
	 * the hard part.  Each aspect is described here by
	 * the number set to it in DatabaseHelper as
	 * EXERCISE_COL_*_NUM.
	 * <p>
	 * Thus this method will make it easy to turn on/off
	 * aspects based on the num.  This does the switch
	 * statement so you don't have to.
	 *
	 *	side effects:
	 *		One of the aspects will be changed.
	 *
	 * @param num		The number of the column in the
	 * 					database for this aspect.  Note
	 * 					that this could be .bxxx or .g_xxx.
	 *
	 * @param value		True of false.  This is what we're
	 * 					setting the aspect's value TO.
	 */
	public void set_aspect_by_num (int num, boolean value) {
		switch (num) {
			case DatabaseHelper.EXERCISE_COL_REP_NUM:
				breps = value;
				break;
			case DatabaseHelper.EXERCISE_COL_LEVEL_NUM:
				blevel = value;
				break;
			case DatabaseHelper.EXERCISE_COL_CALORIE_NUM:
				bcals = value;
				break;
			case DatabaseHelper.EXERCISE_COL_WEIGHT_NUM:
				bweight = value;
				break;
			case DatabaseHelper.EXERCISE_COL_DIST_NUM:
				bdist = value;
				break;
			case DatabaseHelper.EXERCISE_COL_TIME_NUM:
				btime = value;
				break;
			case DatabaseHelper.EXERCISE_COL_OTHER_NUM:
				bother = value;
				break;

			case DatabaseHelper.EXERCISE_COL_GRAPH_REPS_NUM:
				g_reps = value;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_LEVEL_NUM:
				g_level = value;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_CALS_NUM:
				g_cals = value;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_WEIGHT_NUM:
				g_weight = value;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_DIST_NUM:
				g_dist = value;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_TIME_NUM:
				g_time = value;
				break;
			case DatabaseHelper.EXERCISE_COL_GRAPH_OTHER_NUM:
				g_other = value;
				break;
			default:
				Log.e(tag, "Illegal value in set_aspect_by_num(" + num + ")!");
				break;
		}
	} // set_aspect_by_num


	/**************************
	 * Checks to see if the specified aspect is the significant one
	 * or not.
	 */
	public boolean is_reps_significant() {
		return significant == DatabaseHelper.EXERCISE_COL_REP_NUM;
	}
	public boolean is_level_significant() {
		return significant == DatabaseHelper.EXERCISE_COL_LEVEL_NUM;
	}
	public boolean is_cals_significant() {
		return significant == DatabaseHelper.EXERCISE_COL_CALORIE_NUM;
	}
	public boolean is_weight_significant() {
		return significant == DatabaseHelper.EXERCISE_COL_WEIGHT_NUM;
	}
	public boolean is_dist_significant() {
		return significant == DatabaseHelper.EXERCISE_COL_DIST_NUM;
	}
	public boolean is_time_significant() {
		return significant == DatabaseHelper.EXERCISE_COL_TIME_NUM;
	}
	public boolean is_other_significant() {
		return significant == DatabaseHelper.EXERCISE_COL_OTHER_NUM;
	}

}
