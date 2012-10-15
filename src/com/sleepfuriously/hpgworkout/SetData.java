/**
 * The SetData class is a nice way to encapsulate all
 * the data associated with an exercise set.
 */
package com.sleepfuriously.hpgworkout;


public class SetData {
	/** The id for this item in the database */
	int _id;

	/**
	 * Name of the exercise.  This should be exactly
	 * the same as the name in the corresponding ExerciseData.
	 */
	String name;

	/** The time and date (in milliseconds) of this set */
	long millis;

	/** The actual data for the set (float values) */
	float weight, dist, time, other;

	/** The actual data for the set (int values) */
	int reps, levels, cals;

	/** The condition code */
	int cond;

	/** Any notes attached to this set */
	String notes;

}
