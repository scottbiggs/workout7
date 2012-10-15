/**
 * This is just a data class to encapsulate all the
 * information about a particular exercise.
 */
package com.sleepfuriously.hpgworkout;

public class ExerciseData {
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
}
